package jmips.cpu;

import jmips.utils.ReadableSeedRandom;

/**
 * Java implementation of a MIPS32 4Kc processor
 */
public final class Cpu {
	// Number of TLB entries in Cop0
	public static final int NUM_TLB_ENTRIES = 16;

	// Some bitmasks inside Cop0 registers
	public static final int INDEX_MASK  = 0x0000000F;
	public static final int INDEX_PROBE = 0x80000000;

	public static final int ENTRYLO_GLOBAL    = 0x00000001;
	public static final int ENTRYLO_VALID     = 0x00000002;
	public static final int ENTRYLO_DIRTY     = 0x00000004;
	public static final int ENTRYLO_PFN_MASK  = 0x03FFFFC0;
	public static final int ENTRYLO_COHERENCY_MASK  = 0x00000038;
	public static final int ENTRYLO_COHERENCY_SHIFT = 3;
	public static final int ENTRYLO_WRITE_MASK = ENTRYLO_GLOBAL | ENTRYLO_VALID | ENTRYLO_DIRTY |
	                                             ENTRYLO_PFN_MASK | ENTRYLO_COHERENCY_MASK;

	public static final int ENTRYHI_ASID_MASK = 0x000000FF;
	public static final int ENTRYHI_VPN2_MASK = 0xFFFFE000;
	public static final int ENTRYHI_WRITE_MASK = ENTRYHI_ASID_MASK | ENTRYHI_VPN2_MASK;

	public static final int STATUS_IE  = 0x00000001; // Interrupt enabled
	public static final int STATUS_EXL = 0x00000002; // Exception
	public static final int STATUS_ERL = 0x00000004; // Error
	public static final int STATUS_UM  = 0x00000010; // User mode
	public static final int STATUS_INT_MASK  = 0x0000FF00;
	public static final int STATUS_INT_SHIFT = 8;
	public static final int STATUS_NMI = 0x00080000; // Nmi
	public static final int STATUS_SR  = 0x00100000; // Soft reset
	public static final int STATUS_TS  = 0x00200000; // TLB Shutdown
	public static final int STATUS_BEV = 0x00400000; // Bootstrap exception vector
	public static final int STATUS_RE  = 0x02000000; // Reverse-endian in user mode
	public static final int STATUS_RP  = 0x08000000; // Reduced power mode
	public static final int STATUS_COP_MASK  = 0xF0000000;
	public static final int STATUS_COP_SHIFT = 28;
	public static final int STATUS_WRITE_MASK = STATUS_IE | STATUS_EXL | STATUS_ERL | STATUS_UM | STATUS_INT_MASK |
	                                            STATUS_NMI | STATUS_SR | STATUS_TS | STATUS_BEV | STATUS_RE |
	                                            STATUS_RP | STATUS_COP_MASK;

	public static final int CAUSE_EXCCODE_MASK  = 0x0000007C;
	public static final int CAUSE_EXCCODE_SHIFT = 2;
	public static final int CAUSE_INTERRUPT_MASK = 0x0000FF00;
	public static final int CAUSE_INTERRUPT_SHIFT = 8;
	public static final int CAUSE_WP = 0x00400000; // Watch exception deferred
	public static final int CAUSE_IV = 0x00800000; // Use special interrupt vector
	public static final int CAUSE_CE_MASK = 0x30000000; // Coprocessor unit
	public static final int CAUSE_CE_SHIFT = 28;
	public static final int CAUSE_BD = 0x80000000; // In delay slot
	public static final int CAUSE_WRITE_MASK = (3 << CAUSE_INTERRUPT_SHIFT) | CAUSE_WP | CAUSE_IV;

	public static final int CONTEXT_PTE_MASK = 0xFF800000;
	public static final int CONTEXT_WRITE_MASK = CONTEXT_PTE_MASK;

	// Timer IRQ
	public static final int TIMER_IRQ = 7;

	// Memory error codes
	public static final int MEMORY_ERROR_NOERROR = 0;                // Memory operation succeeded
	public static final int MEMORY_ERROR_BUS_ERROR_INSTRUCTION = 1;  // I/O Bus error for opcode
	public static final int MEMORY_ERROR_BUS_ERROR_DATA = 2;         // I/O Bus error for data
	public static final int MEMORY_ERROR_ADDRESS_ERROR_LOAD = 3;     // Address error for store
	public static final int MEMORY_ERROR_ADDRESS_ERROR_STORE = 4;    // Unaligned address
	public static final int MEMORY_ERROR_TLB_INVALID_LOAD = 5;       // TLB Invalid for load
	public static final int MEMORY_ERROR_TLB_INVALID_STORE = 6;      // TLB Invalid for store 
	public static final int MEMORY_ERROR_TLB_REFILL_LOAD = 7;        // TLB Refill for load
	public static final int MEMORY_ERROR_TLB_REFILL_STORE = 8;       // TLB Refill for store
	public static final int MEMORY_ERROR_TLB_MOD = 9;                // TLB Dirty

	// Instance fields
	private final int[] gpr = new int[32];
	private int hi, lo;
	private int pc, npc;
	private int beforeCounter;
	private long counter;
	private boolean delaySlot;

	private boolean halted;
	private int memoryError;

	private final Ram ram;
	private final Device ioController;

	// Coprocessor0 Instance fields
	private int Index;
	private int EntryLo0;
	private int EntryLo1;
	private int Context;
	private int PageMask;
	private int Wired;
	private int Reserved1;
	private int BadVAddr;
	private int EntryHi;
	private int Compare;
	private int Status;
	private int Cause;
	private int EPC;
	private final int PRId = 0x00018000; // Revision 1.1
	private int Config = 0x80008082;
	private final int Config1 = 0x1E190C80; // cache 2-way, no fpu, no ejtag, no mips16, no watch and no performance counter
	private int LLAddr;
	private int WatchLo;
	private int WatchHi;
	private int Reserved2;
	private int Reserved3;
	private int Reserved4;
	private int Debug;
	private int DEPC;
	private int Reserved5;
	private int ErrCtrl;
	private int Reserved6;
	private int TagLo, DataLo;
	private int Reserved7;
	private int ErrorEPC;
	private int DESAVE;

	private boolean kernelMode= true;
	private boolean bigEndian = true;
	private int ASID;

	private boolean loadLinkedStatus = false;

	private final ReadableSeedRandom random = new ReadableSeedRandom(0);
	private final TlbEntry[] tlbEntries = new TlbEntry[NUM_TLB_ENTRIES];;
	private TlbEntry lastTlbEntryCode;
	private TlbEntry lastTlbEntryData;

	public Cpu(int ramSize, Device io) {
		this.ram = new Ram(ramSize);
		this.ioController = io;
		initTLB();
		hardReset();
	}

	public void hardReset() {
		resetTLB();
		reset();
	}

	public void reset() {
		exception_RESET();
	}

	public void setGpr(int regno, int val) {
		if (regno != Mips.GPR_ZR) gpr[regno] = val;
	}

	public int getGpr(int regno) {
		return gpr[regno];
	}

	public int getHi() {
		return hi;
	}

	public int getLo() {
		return lo;
	}

	public void setHi(int hi) {
		this.hi = hi;
	}

	public void setLo(int lo) {
		this.lo = lo;
	}

	public Ram getRam() {
		return ram;
	}

	public boolean isHalted() {
		return halted;
	}

	public void setPc(int pc) {
		this.pc = pc;
		this.delaySlot = false;
		this.halted = false;
	}

	public int getPc() {
		return this.pc;
	}

	public int getCounter() {
		return (int) (counter >> 1);
	}

	public void setCounter(int counter) {
		this.counter = (((long) counter) << 1);
	}

	public boolean isBranchDelaySlot() {
		return delaySlot;
	}

	// Load and store operations don't raise exceptions, but read and write do!
	private byte _load8phys(final int physicalAddress) {
		byte ret;
		if (physicalAddress < ram.getRamSize()) {
			ret = ram.read8(physicalAddress);
		} else {
			ret = ioController.read8(physicalAddress);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
		return ret;
	}

	private void _store8phys(final int physicalAddress, byte value) {
		if (physicalAddress < ram.getRamSize()) {
			ram.write8(physicalAddress, value);
		} else {
			ioController.write8(physicalAddress, value);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
	}

	private short _load16phys(final int physicalAddress) {
		short ret;
		if (physicalAddress < ram.getRamSize()) {
			ret = ram.read16(physicalAddress);
		} else {
			ret = ioController.read16(physicalAddress);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
		if (!bigEndian) ret = Helper.byteSwap(ret);
		return ret;
	}

	private void _store16phys(final int physicalAddress, short value) {
		if (!bigEndian) value = Helper.byteSwap(value);
		if (physicalAddress < ram.getRamSize()) {
			ram.write16(physicalAddress, value);
		} else {
			ioController.write16(physicalAddress, value);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
	}

	private int _load32phys(final int physicalAddress) {
		int ret;
		if (physicalAddress < ram.getRamSize()) {
			ret = ram.read32(physicalAddress);
		} else {
			ret = ioController.read32(physicalAddress);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
		if (!bigEndian) ret = Helper.byteSwap(ret);
		return ret;
	}

	private void _store32phys(final int physicalAddress, int value) {
		if (!bigEndian) value = Helper.byteSwap(value);
		if (physicalAddress < ram.getRamSize()) {
			ram.write32(physicalAddress, value);
		} else {
			ioController.write32(physicalAddress, value);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
	}

	public byte load8phys(final int physicalAddress) {
		memoryError = MEMORY_ERROR_NOERROR;
		return _load8phys(physicalAddress);
	}

	public void store8phys(final int physicalAddress, byte value) {
		memoryError = MEMORY_ERROR_NOERROR;
		_store8phys(physicalAddress, value);
	}

	public short load16phys(final int physicalAddress) {
		if ((physicalAddress & 1) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;
			return _load16phys(physicalAddress);
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
			return 0;
		}
	}

	public void store16phys(final int physicalAddress, short value) {
		if ((physicalAddress & 1) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;
			_store16phys(physicalAddress, value);
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}

	public int load32phys(final int physicalAddress) {
		if ((physicalAddress & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;
			return _load32phys(physicalAddress);
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
			return 0;
		}
	}

	public void store32phys(final int physicalAddress, int value) {
		if ((physicalAddress & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;
			_store32phys(physicalAddress, value);
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}


	public byte load8(final int address) {
		memoryError = MEMORY_ERROR_NOERROR;

		int physicalAddress = translate(address, false, true);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			return _load8phys(physicalAddress);
		}
		return 0;
	}

	public void store8(final int address, byte value) {
		memoryError = MEMORY_ERROR_NOERROR;

		int physicalAddress = translate(address, true, true);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			_store8phys(physicalAddress, value);
		}
	}

	public short load16(final int address) {
		if ((address & 1) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = translate(address, false, true);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				return _load16phys(physicalAddress);
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
		}
		return 0;
	}

	public void store16(final int address, short value) {
		if ((address & 1) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = translate(address, true, true);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				_store16phys(physicalAddress, value);
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}

	public int load32(final int address) {
		if ((address & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = translate(address, false, true);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				return _load32phys(physicalAddress);
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
		}
		return 0;
	}

	public void store32(final int address, int value) {
		if ((address & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = translate(address, true, true);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				_store32phys(physicalAddress, value);
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}

	private void raiseMemoryException(final int address) {
		switch(memoryError) {
		case MEMORY_ERROR_ADDRESS_ERROR_LOAD:
			exception_ADDRESS_ERROR(address, true);
			break;
		case MEMORY_ERROR_ADDRESS_ERROR_STORE:
			exception_ADDRESS_ERROR(address, false);
			break;
		case MEMORY_ERROR_BUS_ERROR_DATA:
			exception_BUS_ERROR(true);
			break;
		case MEMORY_ERROR_BUS_ERROR_INSTRUCTION:
			exception_BUS_ERROR(false);
			break;
		case MEMORY_ERROR_TLB_INVALID_LOAD:
			exception_TLB_INVALID(address, true);
			break;
		case MEMORY_ERROR_TLB_INVALID_STORE:
			exception_TLB_INVALID(address, false);
			break;
		case MEMORY_ERROR_TLB_REFILL_LOAD:
			exception_TLB_REFILL(address, true);
			break;
		case MEMORY_ERROR_TLB_REFILL_STORE:
			exception_TLB_REFILL(address, false);
			break;
		case MEMORY_ERROR_TLB_MOD:
			exception_TLB_MOD(address);
			break;
		}
	}

	public byte read8(final int address) {
		byte ret = load8(address);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}

	public void write8(final int address, byte value) {
		store8(address, value);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public short read16(final int address) {
		short ret = load16(address);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}

	public void write16(final int address, short value) {
		store16(address, value);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public int read32(final int address) {
		int ret = load32(address);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}
	
	public void write32(final int address, int value) {
		store32(address, value);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public int read32linked(final int address) {
		int ret;
		if ((address & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = translate(address, false, true);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				ret = _load32phys(physicalAddress);
				if (memoryError == MEMORY_ERROR_NOERROR) {
					loadLinkedStatus = true;
					LLAddr = physicalAddress >>> 4;
				}
			} else {
				ret = 0;
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
			ret = 0;
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}


	public boolean write32conditional(final int address, int value) {
		if ((address & 3) == 0) {
			if (loadLinkedStatus) {
				write32(address, value);
				if (memoryError == MEMORY_ERROR_NOERROR) {
					loadLinkedStatus = false;
					return true;
				}
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return false;
	}

	public int read32UnalignedLeft(final int address, int oldValue) {
		int alignedAddress = address & (~3);
		int value = read32(alignedAddress);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			int shift = (bigEndian) ? (address & 3) : 3 - (address & 3);
			shift <<= 3;
			return (oldValue & ~((-1) << shift)) | (value << shift); 
		} else {
			return 0;
		}
	}

	public int read32UnalignedRight(final int address, int oldValue) {
		int alignedAddress = address & (~3);
		int value = read32(alignedAddress);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			int shift = (!bigEndian) ? (address & 3) : 3 - (address & 3);
			shift <<= 3;
			return (oldValue & ~((-1) >>> shift)) | (value >>> shift); 
		} else {
			return 0;
		}
	}

	public void write32UnalignedLeft(final int address, int value) {
		int alignedAddress = address & (~3);

		memoryError = MEMORY_ERROR_NOERROR;
		int physicalAddress = translate(alignedAddress, true, true);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			int oldValue = _load32phys(physicalAddress);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				int shift = (bigEndian) ? (address & 3) : 3 - (address & 3);
				shift <<= 3;
				int newValue = (oldValue & ~((-1) >>> shift)) | (value >>> shift);
				_store32phys(physicalAddress, newValue);
			}
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public void write32UnalignedRight(final int address, int value) {
		int alignedAddress = address & (~3);

		memoryError = MEMORY_ERROR_NOERROR;
		int physicalAddress = translate(alignedAddress, true, true);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			int oldValue = _load32phys(physicalAddress);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				int shift = (!bigEndian) ? (address & 3) : 3 - (address & 3);
				shift <<= 3;
				int newValue = (oldValue & ~((-1) << shift)) | (value << shift); 
				_store32phys(physicalAddress, newValue);
			}
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public int fetchOpcode() {
		int ret = 0;
		if ((pc & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = translate(pc, false, false);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				if (physicalAddress < ram.getRamSize()) {
					ret = ram.read32(physicalAddress);
				} else {
					ret = ioController.read32(physicalAddress);
					if (ioController.ioError())
						memoryError = MEMORY_ERROR_BUS_ERROR_INSTRUCTION;
				}
				if (!bigEndian) ret = Helper.byteSwap(ret);
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(pc);
		return ret;
	}

	public int getLastMemoryError() {
		return memoryError;
	}

	public void step() {
		step(1);
	}

	private int safeFetchOpcode() {
		int opcode = fetchOpcode();
		if (memoryError == MEMORY_ERROR_NOERROR) {
			return opcode;
		} else {
			return fetchOpcode();
		}
	}

	public void step(int num) {
		checkInterrupts();
		beforeCounter = getCounter();
		while(num > 0 && !halted) {
			int opcode;

			delaySlot = false;
			npc = pc;
			opcode = safeFetchOpcode();
			if (memoryError != MEMORY_ERROR_NOERROR) {
				halted = true;
				break;
			}
			//System.out.print(disassemble(1));

			pc = pc + 4;
			counter++;
			stepMips(opcode);

			if (delaySlot) {
				int nextPc = npc;

				npc = pc;
				opcode = fetchOpcode();
				if (memoryError == MEMORY_ERROR_NOERROR) {
					pc = pc + 4;
					counter++;
					stepMips(opcode);
					if (delaySlot) {
						pc = nextPc;
					}
				}
			}
			num--;
		}
		if (num > 0) counter += num;
		delaySlot = false;
		npc = pc;

		checkTimerInterrupt(beforeCounter, getCounter());
	}

	public String disassemble(int count) {
		return disassemble(pc, count);
	}

	public String disassemble(int address, int count) {
		StringBuilder sb = new StringBuilder();
		address -= 4 * (count / 2);
		for(int i = 0; i < count; i++) {
			int opcode = load32(address + 4 * i);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				sb.append(String.format("0x%08X: ", address + 4 * i));
				sb.append(Mips.disassemble(opcode, address + 4 * i));
			} else {
				sb.append("Invalid memory location");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void stepMips(int opcode) {
		switch (Mips.DECODE_OP(opcode)) {
		case Mips.I_SPECIAL: stepSpecial(opcode); break;
		case Mips.I_REGIMM:  stepRegImm(opcode); break;
		case Mips.I_J:       j(opcode); break;
		case Mips.I_JAL:     jal(opcode); break;
		case Mips.I_BEQ:     beq(opcode); break;
		case Mips.I_BNE:     bne(opcode); break;
		case Mips.I_BLEZ:    blez(opcode); break;
		case Mips.I_BGTZ:    bgtz(opcode); break;

		case Mips.I_ADDI:    addi(opcode); break;
		case Mips.I_ADDIU:   addiu(opcode); break;
		case Mips.I_SLTI:    slti(opcode); break;
		case Mips.I_SLTIU:   sltiu(opcode); break;
		case Mips.I_ANDI:    andi(opcode); break;
		case Mips.I_ORI:     ori(opcode); break;
		case Mips.I_XORI:    xori(opcode); break;
		case Mips.I_LUI:     lui(opcode); break;

		case Mips.I_COP0:    stepCop0(opcode); break;
		case Mips.I_COP1:    invalid(1); break;
		case Mips.I_COP2:    invalid(2); break;
		case Mips.I_COP1X:   invalid(1); break;
		case Mips.I_BEQL:    beql(opcode); break;
		case Mips.I_BNEL:    bnel(opcode); break;
		case Mips.I_BLEZL:   blezl(opcode); break;
		case Mips.I_BGTZL:   bgtzl(opcode); break;

		case Mips.I_SPECIAL2: stepSpecial2(opcode); break;

		case Mips.I_LB:      lb(opcode); break;
		case Mips.I_LH:      lh(opcode); break;
		case Mips.I_LWL:     lwl(opcode); break;
		case Mips.I_LW:      lw(opcode); break;
		case Mips.I_LBU:     lbu(opcode); break;
		case Mips.I_LHU:     lhu(opcode); break;
		case Mips.I_LWR:     lwr(opcode); break;

		case Mips.I_SB:      sb(opcode); break;
		case Mips.I_SH:      sh(opcode); break;
		case Mips.I_SWL:     swl(opcode); break;
		case Mips.I_SW:      sw(opcode); break;
		case Mips.I_SWR:     swr(opcode); break;
		case Mips.I_CACHE:
			if (checkCoprocessor(0))
				cache(opcode);
			break;

		case Mips.I_LL:      ll(opcode); break;
		case Mips.I_LWC1:    invalid(1); break;
		case Mips.I_LWC2:    invalid(2); break;
		case Mips.I_PREF:    pref(opcode); break;
		case Mips.I_LDC1:    invalid(1); break;
		case Mips.I_LDC2:    invalid(2); break;

		case Mips.I_SC:      sc(opcode); break;
		case Mips.I_SWC1:    invalid(1); break;
		case Mips.I_SWC2:    invalid(2); break;
		case Mips.I_SDC1:    invalid(1); break;
		case Mips.I_SDC2:    invalid(2); break;
		default: reserved(); break;
		}
	}

	private void stepSpecial(int opcode) {
		switch(Mips.DECODE_FUNCT(opcode)) {
		case Mips.I_SPEC_SLL:     sll(opcode); break;
		case Mips.I_SPEC_COP1:    invalid(1); break;

		case Mips.I_SPEC_SRL:     srl(opcode); break;
		case Mips.I_SPEC_SRA:     sra(opcode); break;
		case Mips.I_SPEC_SLLV:    sllv(opcode); break;
		case Mips.I_SPEC_SRLV:    srlv(opcode); break;
		case Mips.I_SPEC_SRAV:    srav(opcode); break;

		case Mips.I_SPEC_JR:      jr(opcode); break;
		case Mips.I_SPEC_JALR:    jalr(opcode); break;
		case Mips.I_SPEC_MOVZ:    movz(opcode); break;
		case Mips.I_SPEC_MOVN:    movn(opcode); break;
		case Mips.I_SPEC_SYSCALL: syscall(opcode); break;
		case Mips.I_SPEC_BREAK:   break_(opcode); break;
		case Mips.I_SPEC_SYNC:    sync(opcode); break;

		case Mips.I_SPEC_MFHI:    mfhi(opcode); break;
		case Mips.I_SPEC_MTHI:    mthi(opcode); break;
		case Mips.I_SPEC_MFLO:    mflo(opcode); break;
		case Mips.I_SPEC_MTLO:    mtlo(opcode); break;

		case Mips.I_SPEC_MULT:    mult(opcode); break;
		case Mips.I_SPEC_MULTU:   multu(opcode); break;
		case Mips.I_SPEC_DIV:     div(opcode); break;
		case Mips.I_SPEC_DIVU:    divu(opcode); break;

		case Mips.I_SPEC_ADD:     add(opcode); break;
		case Mips.I_SPEC_ADDU:    addu(opcode); break;
		case Mips.I_SPEC_SUB:     sub(opcode); break;
		case Mips.I_SPEC_SUBU:    subu(opcode); break;
		case Mips.I_SPEC_AND:     and(opcode); break;
		case Mips.I_SPEC_OR:      or(opcode); break;
		case Mips.I_SPEC_XOR:     xor(opcode); break;
		case Mips.I_SPEC_NOR:     nor(opcode); break;

		case Mips.I_SPEC_SLT:     slt(opcode); break;
		case Mips.I_SPEC_SLTU:    sltu(opcode); break;

		case Mips.I_SPEC_TGE:     tge(opcode); break;
		case Mips.I_SPEC_TGEU:    tgeu(opcode); break;
		case Mips.I_SPEC_TLT:     tlt(opcode); break;
		case Mips.I_SPEC_TLTU:    tltu(opcode); break;
		case Mips.I_SPEC_TEQ:     teq(opcode); break;
		case Mips.I_SPEC_TNE:     tne(opcode); break;

		default: reserved(); break;
		}
	}

	private void stepSpecial2(int opcode) {
		switch(Mips.DECODE_FUNCT(opcode)) {
		case Mips.I_SPEC2_MADD:  madd(opcode); break;
		case Mips.I_SPEC2_MADDU: maddu(opcode); break;
		case Mips.I_SPEC2_MUL:   mul(opcode); break;
		case Mips.I_SPEC2_MSUB:  msub(opcode); break;
		case Mips.I_SPEC2_MSUBU: msubu(opcode); break;
		case Mips.I_SPEC2_CLZ:   clz(opcode); break;
		case Mips.I_SPEC2_CLO:   clo(opcode); break;
		case Mips.I_SPEC2_SDBBP: sdbbp(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepRegImm(int opcode) {
		switch(Mips.DECODE_RT(opcode)) {
		case Mips.I_REGIMM_BLTZ:    bltz(opcode); break;
		case Mips.I_REGIMM_BGEZ:    bgez(opcode); break;
		case Mips.I_REGIMM_BLTZL:   bltzl(opcode); break;
		case Mips.I_REGIMM_BGEZL:   bgezl(opcode); break;
		case Mips.I_REGIMM_TGEI:    tgei(opcode); break;
		case Mips.I_REGIMM_TGEIU:   tgeiu(opcode); break;
		case Mips.I_REGIMM_TLTI:    tlti(opcode); break;
		case Mips.I_REGIMM_TLTIU:   tltiu(opcode); break;
		case Mips.I_REGIMM_TEQI:    teqi(opcode); break;
		case Mips.I_REGIMM_TNEI:    tnei(opcode); break;
		case Mips.I_REGIMM_BLTZAL:  bltzal(opcode); break;
		case Mips.I_REGIMM_BGEZAL:  bgezal(opcode); break;
		case Mips.I_REGIMM_BLTZALL: bltzall(opcode); break;
		case Mips.I_REGIMM_BGEZALL: bgezall(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepCop0(int opcode) {
		if (checkCoprocessor(0)) {
			int rs = Mips.DECODE_RS(opcode);
			switch(rs) {
			case Mips.I_COP0_MFC0: mfc0(opcode); break;
			case Mips.I_COP0_MTC0: mtc0(opcode); break;
			default:
				if (rs >= Mips.I_COP0_CO_MIN && rs <= Mips.I_COP0_CO_MAX)
					stepCop0Co(opcode);
				else
					reserved();
				break;
			}
		}
	}

	private void stepCop0Co(int opcode) {
		switch(Mips.DECODE_FUNCT(opcode)) {
		case Mips.I_COP0CO_TLBR:  tlbr(opcode); break;
		case Mips.I_COP0CO_TLBWI: tlbwi(opcode); break;
		case Mips.I_COP0CO_TLBWR: tlbwr(opcode); break;
		case Mips.I_COP0CO_TLBP:  tlbp(opcode); break;
		case Mips.I_COP0CO_ERET:  eret(opcode); break;
		case Mips.I_COP0CO_DERET: deret(opcode); break;
		case Mips.I_COP0CO_WAIT:  wait(opcode); break;
		default: reserved(); break;
		}
	}

	public void add(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs + rt;
		if (checkOverflow(rs, rt, result, true)) return;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void addi(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		int result = rs + imm;
		if (checkOverflow(rs, imm, result, true)) return;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void addiu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		int result = rs + imm;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void addu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs + rt;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void and(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs & rt;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void andi(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int immu = Mips.DECODE_IMM16U(opcode);
		int result = rs & immu;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void beq(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs == rt) {
			branch(opcode);
		}
	}

	public void beql(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs == rt) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bgez(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		}
	}

	public void bgezal(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		link();
		if (rs >= 0) {
			branch(opcode);
		}
	}

	public void bgezall(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		link();
		if (rs >= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bgezl(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bgtz(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs > 0) {
			branch(opcode);
		}
	}

	public void bgtzl(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs > 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void blez(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs <= 0) {
			branch(opcode);
		}
	}

	public void blezl(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs <= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bltz(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		}
	}

	public void bltzal(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		link();
		if (rs < 0) {
			branch(opcode);
		}
	}

	public void bltzall(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		link();
		if (rs < 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bltzl(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bne(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs != rt) {
			branch(opcode);
		}
	}

	public void bnel(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs != rt) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void break_(int opcode) {
		exception_BREAK();
	}

	public void cache(int opcode) {
		// No cache emulation is done
	}

	public void clo(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = Helper.countLeadingOnes(rs);
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void clz(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = Helper.countLeadingZeros(rs);
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void deret(int opcode) {
		//TODO
	}

	public void div(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rt == 0) {
			lo = hi = 0;
		} else {
			lo = rs / rt;
			hi = rs % rt;
		}
	}

	public void divu(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[Mips.DECODE_RT(opcode)] & 0xFFFFFFFFL;
		if (rt == 0) {
			lo = hi = 0;
		} else {
			lo = (int) (rs / rt);
			hi = (int) (rs % rt);
		}
	}

	public void eret(int opcode) {
		returnFromException();
	}

	public void j(int opcode) {
		npc = Mips.DECODE_JUMP(opcode, pc - 4);
		delaySlot = true;
	}

	public void jal(int opcode) {
		link();
		j(opcode);
	}

	public void jalr(int opcode) {
		link(Mips.DECODE_RD(opcode));
		jr(opcode);
	}

	public void jr(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		npc = rs;
		delaySlot = true;
	}

	public void lb(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(Mips.DECODE_RT(opcode), val);
		}
	}

	public void lbu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address) & 0xFF;
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(Mips.DECODE_RT(opcode), val);
		}
	}

	public void lh(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(Mips.DECODE_RT(opcode), val);
		}
	}

	public void lhu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address) & 0xFFFF;
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(Mips.DECODE_RT(opcode), val);
		}
	}

	public void ll(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read32linked(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(Mips.DECODE_RT(opcode), val);
		}
	}

	public void lui(int opcode) {
		int imm = Mips.DECODE_IMM16(opcode);
		int result = imm << 16;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void lw(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read32(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(Mips.DECODE_RT(opcode), val);
		}
	}

	public void lwl(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = Mips.DECODE_RT(opcode);
		int val = read32UnalignedLeft(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, val);
		}
	}

	public void lwr(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = Mips.DECODE_RT(opcode);
		int val = read32UnalignedRight(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, val);
		}
	}

	public void madd(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)];
		long rt = gpr[Mips.DECODE_RT(opcode)];
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo + rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void maddu(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[Mips.DECODE_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo + rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void mfc0(int opcode) {
		int rt = Mips.DECODE_RT(opcode);
		int rd = Mips.DECODE_RD(opcode);
		int sel = Mips.DECODE_COP0SEL(opcode);
		setGpr(rt, getCop0Reg(rd, sel));
	}

	public void mfhi(int opcode) {
		setGpr(Mips.DECODE_RD(opcode), hi);
	}

	public void mflo(int opcode) {
		setGpr(Mips.DECODE_RD(opcode), lo);
	}

	public void movn(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rt != 0) {
			int rs = gpr[Mips.DECODE_RS(opcode)];
			setGpr(Mips.DECODE_RD(opcode), rs);
		}
	}

	public void movz(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rt == 0) {
			int rs = gpr[Mips.DECODE_RS(opcode)];
			setGpr(Mips.DECODE_RD(opcode), rs);
		}
	}

	public void msub(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)];
		long rt = gpr[Mips.DECODE_RT(opcode)];
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo - rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void msubu(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[Mips.DECODE_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo - rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void mtc0(int opcode) {
		int rt = Mips.DECODE_RT(opcode);
		int rd = Mips.DECODE_RD(opcode);
		int sel = Mips.DECODE_COP0SEL(opcode);
		setCop0Reg(rd, sel, gpr[rt]);
	}

	public void mthi(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		hi = rs;
	}

	public void mtlo(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		lo = rs;
	}

	public void mul(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs * rt;
		setGpr(Mips.DECODE_RD(opcode), result);
		// hi = lo = 0; // Unpredictable
	}

	public void mult(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)];
		long rt = gpr[Mips.DECODE_RT(opcode)];
		long result = rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void multu(int opcode) {
		long rs = gpr[Mips.DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[Mips.DECODE_RT(opcode)] & 0xFFFFFFFFL;
		long result = rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void nor(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = ~(rs | rt);
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void or(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs | rt;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void ori(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int immu = Mips.DECODE_IMM16U(opcode);
		int result = rs | immu;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void pref(int opcode) {
		//TODO
	}

	public void sb(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[Mips.DECODE_RT(opcode)];
		write8(address, (byte) rt);
	}

	public void sc(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = Mips.DECODE_RT(opcode);
		boolean ok = write32conditional(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, ok ? 1 : 0);
		}
	}

	public void sdbbp(int opcode) {
		//TODO
	}

	public void sh(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[Mips.DECODE_RT(opcode)];
		write16(address, (short) rt);
	}

	public void sll(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int sa = Mips.DECODE_SA(opcode);
		int result = rt << sa;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void sllv(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = rt << rs;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void slt(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = (rs < rt) ? 1 : 0;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void slti(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		int result = (rs < imm) ? 1 : 0;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void sltiu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		int result = (Helper.compareUnsigned(rs, imm) < 0) ? 1 : 0;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	public void sltu(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = (Helper.compareUnsigned(rs, rt) < 0) ? 1 : 0;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void sra(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int sa = Mips.DECODE_SA(opcode);
		int result = rt >> sa;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void srav(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = rt >> rs;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void srl(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int sa = Mips.DECODE_SA(opcode);
		int result = rt >>> sa;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void srlv(int opcode) {
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int result = rt >>> rs;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void sub(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs - rt;
		if (checkOverflow(rs, rt, result, false)) return;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void subu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs - rt;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void sw(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[Mips.DECODE_RT(opcode)];
		write32(address, rt);
	}

	public void swl(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[Mips.DECODE_RT(opcode)];
		write32UnalignedLeft(address, rt);
	}

	public void swr(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int offset = Mips.DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[Mips.DECODE_RT(opcode)];
		write32UnalignedRight(address, rt);
	}

	public void sync(int opcode) {
		// No cache emulation is done
	}

	public void syscall(int opcode) {
		exception_SYSCALL();
	}

	public void teq(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs == rt) {
			exception_TRAP();
		}
	}

	public void teqi(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		if (rs == imm) {
			exception_TRAP();
		}
	}

	public void tge(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs >= rt) {
			exception_TRAP();
		}
	}

	public void tgei(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		if (rs >= imm) {
			exception_TRAP();
		}
	}

	public void tgeiu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		if (Helper.compareUnsigned(rs, imm) >= 0) {
			exception_TRAP();
		}
	}

	public void tgeu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (Helper.compareUnsigned(rs, rt) >= 0) {
			exception_TRAP();
		}
	}

	public void tlbp(int opcode) {
		tlbProbe();
	}

	public void tlbr(int opcode) {
		tlbRead();
	}

	public void tlbwi(int opcode) {
		tlbWriteIndex();
	}

	public void tlbwr(int opcode) {
		tlbWriteRandom();
	}

	public void tlt(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs < rt) {
			exception_TRAP();
		}
	}

	public void tlti(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		if (rs < imm) {
			exception_TRAP();
		}
	}

	public void tltiu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		if (Helper.compareUnsigned(rs, imm) < 0) {
			exception_TRAP();
		}
	}

	public void tltu(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (Helper.compareUnsigned(rs, rt) < 0) {
			exception_TRAP();
		}
	}

	public void tne(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		if (rs != rt) {
			exception_TRAP();
		}
	}

	public void tnei(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int imm = Mips.DECODE_IMM16(opcode);
		if (rs != imm) {
			exception_TRAP();
		}
	}

	public void wait(int opcode) {
		halted = true;
	}

	public void xor(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int rt = gpr[Mips.DECODE_RT(opcode)];
		int result = rs ^ rt;
		setGpr(Mips.DECODE_RD(opcode), result);
	}

	public void xori(int opcode) {
		int rs = gpr[Mips.DECODE_RS(opcode)];
		int immu = Mips.DECODE_IMM16U(opcode);
		int result = rs ^ immu;
		setGpr(Mips.DECODE_RT(opcode), result);
	}

	private void invalid(int copno) {
		if (checkCoprocessor(copno))
			exception_RESERVED();
	}

	private void reserved() {
		exception_RESERVED();
	}

	private boolean checkCoprocessor(int copno) {
		if (!isCoprocessorAvailable(copno)) {
			exception_COPROCESS_UNUSABLE(copno);
			return false;
		}
		return true;
	}

	private boolean checkOverflow(int a, int b, int result, boolean sum) {
		boolean overflow = false;
		if (sum) {
			overflow = ((a < 0) && (b < 0) && (result > 0)) ||
			           ((a > 0) && (b > 0) && (result < 0));
		} else {
			overflow = ((a <= 0) && (b > 0) && (result > 0)) ||
			           ((a >= 0) && (b < 0) && (result < 0));
		}
		if (overflow) exception_INTEGER_OVERFLOW();
		return overflow;
	}

	private void branch(int opcode) {
		npc = Mips.DECODE_BRANCH(opcode, pc - 4);
		delaySlot = true;
	}

	private void link() {
		link(Mips.GPR_RA);
	}

	private void link(int regno) {
		setGpr(regno, pc + 4);
	}

	private void skipDelaySlot() {
		pc += 4;
	}


	public boolean isCoprocessorAvailable(int copno) {
		if (copno == 0 && kernelMode) return true;
		return (Status & (1 << (STATUS_COP_SHIFT + copno))) != 0;
	}

	private int readRegisterRandom() {
		return Wired + random.nextRandomInt(NUM_TLB_ENTRIES - Wired);
	}

	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}

	public long getRangomSeed() {
		return random.getSeed();
	}

	private static int changeValue(int oldValue, int newValue, int mask) {
		return (oldValue & ~ mask) | (newValue & mask);
	}

	private void writeStatus(int value) {
		Status = value & STATUS_WRITE_MASK;
		kernelMode = (value & (STATUS_UM | STATUS_EXL | STATUS_ERL)) != STATUS_UM;
		if (!kernelMode) {
			bigEndian = (value & STATUS_RE) == 0;
		} else {
			bigEndian = true;
		}
	}

	public void setCop0Reg(int reg, int sel, int value) {
		switch(reg) {
		case Mips.COP0_INDEX:
			Index = changeValue(Index, value, INDEX_MASK);
			break;
		case Mips.COP0_RANDOM:
			// Ignore random writes
			break;
		case Mips.COP0_ENTRYLO0:
			EntryLo0 = value & ENTRYLO_WRITE_MASK;
			break;
		case Mips.COP0_ENTRYLO1:
			EntryLo1 = value & ENTRYLO_WRITE_MASK;
			break;
		case Mips.COP0_CONTEXT:
			Context = changeValue(Context, value, CONTEXT_WRITE_MASK);
			break;
		case Mips.COP0_PAGEMASK:
			PageMask = value & 0x01FFE000;
			break;
		case Mips.COP0_WIRED:
			Wired = value & INDEX_MASK;
			break;
		case Mips.COP0_RESERVED1:
			Reserved1 = value;
			break;
		case Mips.COP0_BADVADDR:
			// Ignore BadVAddr writes
			break;
		case Mips.COP0_COUNT:
			checkTimerInterrupt(beforeCounter, getCounter());
			setCounter(value);
			beforeCounter = value;
			break;
		case Mips.COP0_ENTRYHI:
			EntryHi = value & ENTRYHI_WRITE_MASK;
			ASID = value & ENTRYHI_ASID_MASK;
			break;
		case Mips.COP0_COMPARE:
			Compare = value;
			beforeCounter = getCounter();
			lowerIrq(TIMER_IRQ);
			break;
		case Mips.COP0_STATUS:
			writeStatus(value);
			break;
		case Mips.COP0_CAUSE:
			Cause = changeValue(Cause, value, CAUSE_WRITE_MASK);
			break;
		case Mips.COP0_EPC:
			EPC = value;
			break;
		case Mips.COP0_PRID:
			// Ignore PRId writes
			break;
		case Mips.COP0_CONFIG:
			if (sel == 0) {
				Config = changeValue(Config,  value, 0x07);
			}
			// Ignores Config1 writes
			break;
		case Mips.COP0_LLADDR:
			// Ignore LLAddr writes
			break;
		case Mips.COP0_WATCHLO:
			WatchLo = value;
			break;
		case Mips.COP0_WATCHHI:
			WatchHi = value;
			break;
		case Mips.COP0_RESERVED2:
			Reserved2 = value;
			break;
		case Mips.COP0_RESERVED3:
			Reserved3 = value;
			break;
		case Mips.COP0_RESERVED4:
			Reserved4 = value;
			break;
		case Mips.COP0_DEBUG:
			Debug = value;
			break;
		case Mips.COP0_DEPC:
			DEPC = value;
			break;
		case Mips.COP0_RESERVED5:
			Reserved5 = value;
			break;
		case Mips.COP0_ERRCTRL:
			ErrCtrl = value;
			break;
		case Mips.COP0_RESERVED6:
			Reserved6 = value;
			break;
		case Mips.COP0_TAGLO:
			if (sel == 0) TagLo = value;
			else DataLo = value;
			break;
		case Mips.COP0_RESERVED7:
			Reserved7 = value;
			break;
		case Mips.COP0_ERROREPC:
			ErrorEPC = value;
			break;
		case Mips.COP0_DESAVE:
			DESAVE = value;
			break;
		}
	}

	public int getCop0Reg(int reg, int sel) {
		int retval = 0;
		switch(reg) {
		case Mips.COP0_INDEX:
			retval = Index;
			break;
		case Mips.COP0_RANDOM:
			retval = readRegisterRandom();
			break;
		case Mips.COP0_ENTRYLO0:
			retval = EntryLo0;
			break;
		case Mips.COP0_ENTRYLO1:
			retval = EntryLo1;
			break;
		case Mips.COP0_CONTEXT:
			retval = Context;
			break;
		case Mips.COP0_PAGEMASK:
			retval = PageMask;
			break;
		case Mips.COP0_WIRED:
			retval = Wired;
			break;
		case Mips.COP0_RESERVED1:
			retval = Reserved1;
			break;
		case Mips.COP0_BADVADDR:
			retval = BadVAddr;
			break;
		case Mips.COP0_COUNT:
			retval = getCounter();
			break;
		case Mips.COP0_ENTRYHI:
			retval = EntryHi;
			break;
		case Mips.COP0_COMPARE:
			retval = Compare;
			break;
		case Mips.COP0_STATUS:
			retval = Status;
			break;
		case Mips.COP0_CAUSE:
			checkTimerInterrupt(beforeCounter, getCounter());
			retval = Cause;
			break;
		case Mips.COP0_EPC:
			retval = EPC;
			break;
		case Mips.COP0_PRID:
			retval = PRId;
			break;
		case Mips.COP0_CONFIG:
			if (sel == 0) retval = Config;
			else retval = Config1;
			break;
		case Mips.COP0_LLADDR:
			retval = LLAddr;
			break;
		case Mips.COP0_WATCHLO:
			retval = WatchLo;
			break;
		case Mips.COP0_WATCHHI:
			retval = WatchHi;
			break;
		case Mips.COP0_RESERVED2:
			retval = Reserved2;
			break;
		case Mips.COP0_RESERVED3:
			retval = Reserved3;
			break;
		case Mips.COP0_RESERVED4:
			retval = Reserved4;
			break;
		case Mips.COP0_DEBUG:
			retval = Debug;
			break;
		case Mips.COP0_DEPC:
			retval = DEPC;
			break;
		case Mips.COP0_RESERVED5:
			retval = Reserved5;
			break;
		case Mips.COP0_ERRCTRL:
			retval = ErrCtrl;
			break;
		case Mips.COP0_RESERVED6:
			retval = Reserved6;
			break;
		case Mips.COP0_TAGLO:
			if (sel == 0) retval = TagLo;
			else retval = DataLo;
			break;
		case Mips.COP0_RESERVED7:
			retval = Reserved7;
			break;
		case Mips.COP0_ERROREPC:
			retval = ErrorEPC;
			break;
		case Mips.COP0_DESAVE:
			retval = DESAVE;
			break;
		}
		return retval;
	}

	private int exceptionPc() {
		return delaySlot ? npc - 4 : npc;
	}

	private void exception_RESET() {
		final int statusSet = STATUS_BEV | STATUS_ERL;
		final int statusMask = STATUS_RP | STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL;
		Wired = 0;
		Config = changeValue(Config, 2, 0x07);
		writeStatus(changeValue(Status, statusSet, statusMask));
		ErrorEPC = exceptionPc();

		setPc(0xBFC00000);
	}

	private void exception_SOFT_RESET() {
		final int statusSet = STATUS_SR | STATUS_BEV | STATUS_ERL;
		final int statusMask = STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL;
		writeStatus(changeValue(Status, statusSet, statusMask));
		ErrorEPC = exceptionPc();

		setPc(0xBFC00000);
	}

	private void exception_NMI() {
		final int statusSet = STATUS_BEV | STATUS_NMI | STATUS_ERL;
		final int statusMask = STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL;
		writeStatus(changeValue(Status, statusSet, statusMask));
		ErrorEPC = exceptionPc();

		setPc(0xBFC00000);
	}

	private void exception_GENERAL(int code, int copno, boolean offsetToZero) {
		int vectorOffset;

		if ((Status & STATUS_EXL) == 0) {
			if (delaySlot) {
				EPC = exceptionPc();
				Cause |= CAUSE_BD;
			} else {
				EPC = exceptionPc();
				Cause &= ~CAUSE_BD;
			}
			if (offsetToZero) {
				vectorOffset = 0;
			} else if (code == Mips.EXCEPTION_CODE_INT && ((Cause & CAUSE_IV) != 0)) {
				vectorOffset = 0x200;
			} else {
				vectorOffset = 0x180;
			}
		} else {
			vectorOffset = 0x180;
		}
		Cause = (Cause & ~CAUSE_CE_MASK) | (copno << CAUSE_CE_SHIFT);
		Cause = (Cause & ~CAUSE_EXCCODE_MASK) | (code << CAUSE_EXCCODE_SHIFT);
		writeStatus(Status | STATUS_EXL);
		if ((Status & STATUS_BEV) != 0) {
			setPc(0xBFC00200 + vectorOffset);
		} else {
			setPc(0x80000000 + vectorOffset);
		}
	}

	private void exception_MCHECK() {
		exception_GENERAL(Mips.EXCEPTION_CODE_MCHECK, 0, false);
		Status |= STATUS_TS;
	}

	private void exception_INTERRUPT() {
		exception_GENERAL(Mips.EXCEPTION_CODE_INT, 0, false);
	}

	private void exception_ADDRESS_ERROR(int badVAddr, boolean load) {
		exception_GENERAL(load ? Mips.EXCEPTION_CODE_ADEL : Mips.EXCEPTION_CODE_ADES, 0, false);
		BadVAddr = badVAddr;
	}

	private void exception_TLB_REFILL(int badVAddr, boolean load) {
		exception_GENERAL(load ? Mips.EXCEPTION_CODE_TLBL : Mips.EXCEPTION_CODE_TLBS, 0, true);
		BadVAddr = badVAddr;
		Context = (Context & CONTEXT_PTE_MASK) | ((badVAddr & ENTRYHI_VPN2_MASK) >>> 9);
		EntryHi = (EntryHi & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	private void exception_TLB_INVALID(int badVAddr, boolean load) {
		exception_GENERAL(load ? Mips.EXCEPTION_CODE_TLBL : Mips.EXCEPTION_CODE_TLBS, 0, false);
		BadVAddr = badVAddr;
		Context = (Context & CONTEXT_PTE_MASK) | ((badVAddr & ENTRYHI_VPN2_MASK) >>> 9);
		EntryHi = (EntryHi & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	private void exception_BUS_ERROR(boolean data) {
		exception_GENERAL(data ? Mips.EXCEPTION_CODE_DBE : Mips.EXCEPTION_CODE_IBE, 0, false);
	}

	private void exception_SYSCALL() {
		exception_GENERAL(Mips.EXCEPTION_CODE_SYS, 0, false);
	}

	private void exception_BREAK() {
		exception_GENERAL(Mips.EXCEPTION_CODE_BP, 0, false);
	}

	private void exception_RESERVED() {
		exception_GENERAL(Mips.EXCEPTION_CODE_RI, 0, false);
	}

	private void exception_COPROCESS_UNUSABLE(int copno) {
		exception_GENERAL(Mips.EXCEPTION_CODE_CPU, copno, false);
	}

	private void exception_INTEGER_OVERFLOW() {
		exception_GENERAL(Mips.EXCEPTION_CODE_OV, 0, false);
	}

	private void exception_TRAP() {
		exception_GENERAL(Mips.EXCEPTION_CODE_TR, 0, false);
	}

	private void exception_TLB_MOD(int badVAddr) {
		exception_GENERAL(Mips.EXCEPTION_CODE_MOD, 0, false);
		BadVAddr = badVAddr;
		Context = badVAddr;
	}

	private void returnFromException() {
		int pc;
		loadLinkedStatus = false;
		if ((Status & STATUS_ERL) != 0) {
			writeStatus(Status & (~STATUS_ERL));
			pc = ErrorEPC;
		} else {
			writeStatus(Status & (~STATUS_EXL));
			pc = EPC;
		}
		setPc(pc);
	}

	public void raiseIrq(int irqno) {
		Cause |= 1 << (CAUSE_INTERRUPT_SHIFT + irqno);
	}

	public void lowerIrq(int irqno) {
		Cause &= ~(1 << (CAUSE_INTERRUPT_SHIFT + irqno));
	}

	public boolean interruptEnabled() {
		return (Status & (STATUS_IE | STATUS_EXL | STATUS_ERL)) == STATUS_IE;
	}

	private boolean checkInterrupts() {
		if (interruptEnabled()) {
			int mask = (Status & STATUS_INT_MASK) >>> STATUS_INT_SHIFT;
			int pending = (Cause & CAUSE_INTERRUPT_MASK) >>> CAUSE_INTERRUPT_SHIFT;
			if ((mask & pending) != 0) {
				exception_INTERRUPT();
				return true;
			}
		}
		return false;
	}

	private boolean checkTimerInterrupt(int before, int after) {
		int diff = Compare - before;
		if (diff <= 0) return false;
		if (after - before < diff) return false;

		raiseIrq(TIMER_IRQ);
		checkInterrupts();
		return true;
	}


	// TLB functions
	private void initTLB() {
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntries[i] = new TlbEntry();
		}
		lastTlbEntryCode = lastTlbEntryData = tlbEntries[0];
	}

	private void resetTLB() {
		for(int i = 0; i < NUM_TLB_ENTRIES; i++)
			tlbEntries[i].setInitialized(false);
	}

	private int translate(int address, boolean write, boolean data) {
		if (kernelMode) {
			if ((address & 0xC0000000) == 0x80000000) { // kseg0 or kseg1 
				return address & 0x1FFFFFFF;
			}
		} else { // User Mode
			if ((address & 0x80000000) != 0) {
				memoryError = write ? MEMORY_ERROR_ADDRESS_ERROR_STORE :
				                      MEMORY_ERROR_ADDRESS_ERROR_LOAD;
				return 0;
			}
			
		}

		TlbEntryPage tlbEntryPage;
		TlbEntry tlbEntry = (data) ? lastTlbEntryData : lastTlbEntryCode;

		if (tlbEntry.isInitialized()) {
			tlbEntryPage = tlbEntry.match(address, ASID);
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}

		// Perform the TLB search
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			tlbEntry = tlbEntries[idx];

			if (!tlbEntry.isInitialized()) continue;
			tlbEntryPage = tlbEntry.match(address, ASID); 
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}

		if ((Status & STATUS_EXL) != 0) {
			memoryError = write ? MEMORY_ERROR_TLB_INVALID_STORE :
			                      MEMORY_ERROR_TLB_INVALID_LOAD;
		} else {
			memoryError = write ? MEMORY_ERROR_TLB_REFILL_STORE :
			                      MEMORY_ERROR_TLB_REFILL_LOAD;
		}
		return 0;
	}


	private int tlbPageTranslate(TlbEntry tlbEntry, TlbEntryPage tlbEntryPage,
	                             int address, boolean write, boolean data) {
		if (data) lastTlbEntryData = tlbEntry;
		else lastTlbEntryCode = tlbEntry;
		if (!tlbEntryPage.isValid()) {
			memoryError = write ? MEMORY_ERROR_TLB_INVALID_STORE :
			                      MEMORY_ERROR_TLB_INVALID_LOAD;
			return 0;
		}
		if (write && !tlbEntryPage.isDirty()) {
			memoryError = MEMORY_ERROR_TLB_MOD;
			return 0;
		}
		address &= ~(tlbEntry.getPageMask() | tlbEntry.getSelectionBit());
		address |= tlbEntryPage.getPFN();
		return address;
	}

	private void tlbProbe() {
		int ASID = EntryHi & ENTRYHI_ASID_MASK;
		int VPN2 = EntryHi & ENTRYHI_VPN2_MASK;
		Index = INDEX_PROBE;
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			TlbEntry tlbEntry = tlbEntries[idx];
			TlbEntryPage tlbEntryPage;

			if (!tlbEntry.isInitialized()) continue;
			tlbEntryPage = tlbEntry.match(VPN2, ASID); 
			if (tlbEntryPage != null) {
				Index = idx;
				return;
			}
		}
	}

	private void tlbRead() {
		TlbEntry entry = tlbEntries[Index & INDEX_MASK];
		PageMask = (~entry.getPageMask()) & ENTRYHI_VPN2_MASK;
		EntryHi = entry.getVPN2() | entry.getASID();
		EntryLo0 = entry.getPage0().convertPageToEntryLo(entry.isGlobal());
		EntryLo1 = entry.getPage1().convertPageToEntryLo(entry.isGlobal());
	}

	private void tlbWrite(int index) {
		TlbEntry tlbEntry;
		int mask = (~PageMask) & ENTRYHI_VPN2_MASK;
		int VPN2 = EntryHi & mask;
		int ASID = EntryHi & ENTRYHI_ASID_MASK;
		boolean global = ((EntryLo0 & ENTRYLO_GLOBAL) != 0) &&
		                 ((EntryLo1 & ENTRYLO_GLOBAL) != 0);

		// Checks for multiple entries
		for (int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			tlbEntry = tlbEntries[idx];
			if (!tlbEntry.isInitialized() || idx == index) continue;
			if ((tlbEntry.getVPN2() & mask) == VPN2) {
				if (global || tlbEntry.isGlobal() || tlbEntry.getASID() == ASID) {
					exception_MCHECK();
					return;
				}
			}
		}
		tlbEntry = tlbEntries[index];
		tlbEntry.setInitialized(true);
		tlbEntry.setPageMask(mask);
		tlbEntry.setVPN2(VPN2);
		tlbEntry.setASID(ASID);
		tlbEntry.setGlobal(global);
		tlbEntry.setSelectionBit(mask ^ (mask >> 1));
		tlbEntry.getPage0().configurePageFromEntryLo(EntryLo0);
		tlbEntry.getPage1().configurePageFromEntryLo(EntryLo1);
	}

	private void tlbWriteRandom() {
		tlbWrite(readRegisterRandom());
	}

	private void tlbWriteIndex() {
		if ((Index & INDEX_PROBE) == 0)
			tlbWrite(Index & INDEX_MASK);
		else
			tlbWriteRandom();
	}
}
