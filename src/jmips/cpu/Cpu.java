package jmips.cpu;

import java.util.Random;

/**
 * Java implementation of a MIPS32 4Kc processor
 */
public final class Cpu {
	// General purpose register constants
	public static final int GPR_ZR = 0;
	public static final int GPR_AT = 1;
	public static final int GPR_V0 = 2;
	public static final int GPR_V1 = 3;
	public static final int GPR_A0 = 4;
	public static final int GPR_A1 = 5;
	public static final int GPR_A2 = 6;
	public static final int GPR_A3 = 7;
	public static final int GPR_T0 = 8;
	public static final int GPR_T1 = 9;
	public static final int GPR_T2 = 10;
	public static final int GPR_T3 = 11;
	public static final int GPR_T4 = 12;
	public static final int GPR_T5 = 13;
	public static final int GPR_T6 = 14;
	public static final int GPR_T7 = 15;
	public static final int GPR_S0 = 16;
	public static final int GPR_S1 = 17;
	public static final int GPR_S2 = 18;
	public static final int GPR_S3 = 19;
	public static final int GPR_S4 = 20;
	public static final int GPR_S5 = 21;
	public static final int GPR_S6 = 22;
	public static final int GPR_S7 = 23;
	public static final int GPR_T8 = 24;
	public static final int GPR_T9 = 25;
	public static final int GPR_K0 = 26;
	public static final int GPR_K1 = 27;
	public static final int GPR_GP = 28;
	public static final int GPR_SP = 29;
	public static final int GPR_FP = 30;
	public static final int GPR_RA = 31;

	// Coprocessor0 register constants
	public static final int COP0_REG_INDEX = 0;            // Index into TLB array
	public static final int COP0_REG_RANDOM = 1;           // Random generated index into the TLB array
	public static final int COP0_REG_ENTRYLO0 = 2;         // Low portion of the TLB entry for even virtual pages
	public static final int COP0_REG_ENTRYLO1 = 3;         // Low portion of the TLB entry for odd virtual pages
	public static final int COP0_REG_CONTEXT = 4;          // Pointer to page table entry in memory
	public static final int COP0_REG_PAGEMASK = 5;         // Controls the page sizes in TLB entries
	public static final int COP0_REG_WIRED = 6;            // Controls the number of fixed TLB entries
	public static final int COP0_REG_RESERVED1 = 7;        // Reserved
	public static final int COP0_REG_BADVADDR = 8;         // ASddress for the most recent address-related exception
	public static final int COP0_REG_COUNT = 9;            // Processor cycle count
	public static final int COP0_REG_ENTRYHI = 10;         // High-order portion of the TLB entry
	public static final int COP0_REG_COMPARE = 11;         // Timer interrupt control
	public static final int COP0_REG_STATUS = 12;          // Processor status and control
	public static final int COP0_REG_CAUSE = 13;           // Cause of last exception
	public static final int COP0_REG_EPC = 14;             // Program counter at last exception
	public static final int COP0_REG_PRID = 15;            // Processor identification and revision
	public static final int COP0_REG_CONFIG = 16;          // Configuration registers
	public static final int COP0_REG_LLADDR = 17;          // Load linked address
	public static final int COP0_REG_WATCHLO = 18;         // Watchpoint address(low order)
	public static final int COP0_REG_WATCHHI = 19;         // Watchpoint address(high order) and mask
	public static final int COP0_REG_RESERVED2 = 20;       // Reserved
	public static final int COP0_REG_RESERVED3 = 21;       // Reserved
	public static final int COP0_REG_RESERVED4 = 22;       // Reserved
	public static final int COP0_REG_DEBUG = 23;           // Debug control and exception status
	public static final int COP0_REG_DEPC = 24;            // Program counter at last debug exception
	public static final int COP0_REG_RESERVED5 = 25;       // Reserved
	public static final int COP0_REG_ERRCTRL = 26;         // Control access to data for CACHE instruction
	public static final int COP0_REG_RESERVED6 = 27;       // Reserved
	public static final int COP0_REG_TAGLO = 28;           // Low-order portion of cache tag interface
	public static final int COP0_REG_RESERVED7 = 29;       // Reserved
	public static final int COP0_REG_ERROREPC = 30;        // Program counter at last error
	public static final int COP0_REG_DESAVE = 31;          // Debug handler scratchpad register


	// Exception codes
	public static final int EXCEPTION_CODE_INT = 0;        // Interrupt
	public static final int EXCEPTION_CODE_MOD = 1;        // TLB modification exception
	public static final int EXCEPTION_CODE_TLBL = 2;       // TLB exception (load or instruction fetch)
	public static final int EXCEPTION_CODE_TLBS = 3;       // TLB exception (store)
	public static final int EXCEPTION_CODE_ADEL = 4;       // Address error exception (load or instruction fetch)
	public static final int EXCEPTION_CODE_ADES = 5;       // Address error exception (store)
	public static final int EXCEPTION_CODE_IBE = 6;        // Bus Error exception (instruction fetch)
	public static final int EXCEPTION_CODE_DBE = 7;        // Bus Error exception (data reference: load or store)
	public static final int EXCEPTION_CODE_SYS = 8;        // Syscall exception
	public static final int EXCEPTION_CODE_BP = 9;         // Breakpoint exception
	public static final int EXCEPTION_CODE_RI = 10;        // Reserved Instruction exception
	public static final int EXCEPTION_CODE_CPU = 11;       // Coprocessor Unusable exception
	public static final int EXCEPTION_CODE_OV = 12;        // Overflow exception
	public static final int EXCEPTION_CODE_TR = 13;        // Trap exception
	public static final int EXCEPTION_CODE_WATCH = 23;     // Refrence to WatchHi/WatchLo address
	public static final int EXCEPTION_CODE_MCHECK = 24;    // Machine check exception

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

	public static final int STATUS_IE  = 0x00000001;
	public static final int STATUS_EXL = 0x00000002;
	public static final int STATUS_ERL = 0x00000004;
	public static final int STATUS_UM  = 0x00000010;
	public static final int STATUS_INT_MASK  = 0x0000FF00;
	public static final int STATUS_INT_SHIFT = 8;
	public static final int STATUS_NMI = 0x00080000;
	public static final int STATUS_SR  = 0x00100000;
	public static final int STATUS_TS  = 0x00200000;
	public static final int STATUS_BEV = 0x00400000;
	public static final int STATUS_RE  = 0x02000000;
	public static final int STATUS_RP  = 0x08000000;
	public static final int STATUS_COP_MASK  = 0xF0000000;
	public static final int STATUS_COP_SHIFT = 28;
	public static final int STATUS_WRITE_MASK = STATUS_IE | STATUS_EXL | STATUS_ERL | STATUS_UM | STATUS_INT_MASK |
	                                            STATUS_NMI | STATUS_SR | STATUS_TS | STATUS_BEV | STATUS_RE |
	                                            STATUS_RP | STATUS_COP_MASK;

	public static final int CAUSE_EXCCODE_MASK  = 0x0000007C;
	public static final int CAUSE_EXCCODE_SHIFT = 2;
	public static final int CAUSE_INTERRUPT_MASK = 0x0000FF00;
	public static final int CAUSE_INTERRUPT_SHIFT = 8;
	public static final int CAUSE_WP = 0x00400000;
	public static final int CAUSE_IV = 0x00800000;
	public static final int CAUSE_CE_MASK = 0x30000000;
	public static final int CAUSE_CE_SHIFT = 28;
	public static final int CAUSE_BD = 0x80000000;
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
	private int pc, _nextPc, nextPc;
	private long counter;
	private boolean delaySlot, nextDelaySlot;

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

	private final Random rand = new Random(System.currentTimeMillis());
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
		if (regno != GPR_ZR) gpr[regno] = val;
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

	public boolean isHalted() {
		return halted;
	}

	public void setPc(int pc) {
		this.pc = pc;
		this._nextPc = pc;
		this.nextPc = pc + 4;
		this.delaySlot = false;
		this.nextDelaySlot = false;
		this.halted = false;
	}

	public int getPc() {
		return this.pc;
	}

	public int getNextPc() {
		return this.nextPc;
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

	private void _store8phys(final int physicalAddress, final byte value) {
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
			ret = ram.read16(physicalAddress, bigEndian);
		} else {
			ret = ioController.read16(physicalAddress, bigEndian);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
		return ret;
	}

	private void _store16phys(final int physicalAddress, final short value) {
		if (physicalAddress < ram.getRamSize()) {
			ram.write16(physicalAddress, value, bigEndian);
		} else {
			ioController.write16(physicalAddress, value, bigEndian);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
	}

	private int _load32phys(final int physicalAddress) {
		int ret;
		if (physicalAddress < ram.getRamSize()) {
			ret = ram.read32(physicalAddress, bigEndian);
		} else {
			ret = ioController.read32(physicalAddress, bigEndian);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
		return ret;
	}

	private void _store32phys(final int physicalAddress, int value) {
		if (physicalAddress < ram.getRamSize()) {
			ram.write32(physicalAddress, value, bigEndian);
		} else {
			ioController.write32(physicalAddress, value, bigEndian);
			if (ioController.ioError())
				memoryError = MEMORY_ERROR_BUS_ERROR_DATA; 
		}
	}

	public byte load8phys(final int physicalAddress) {
		memoryError = MEMORY_ERROR_NOERROR;
		return _load8phys(physicalAddress);
	}

	public void store8phys(final int physicalAddress, final byte value) {
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

	public void store16phys(final int physicalAddress, final short value) {
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

	public void store32phys(final int physicalAddress, final int value) {
		if ((physicalAddress & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;
			_store32phys(physicalAddress, value);
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}


	public byte load8(int address) {
		memoryError = MEMORY_ERROR_NOERROR;

		int physicalAddress = translate(address, false, true);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			return _load8phys(physicalAddress);
		}
		return 0;
	}

	public void store8(int address, byte value) {
		memoryError = MEMORY_ERROR_NOERROR;

		int physicalAddress = translate(address, true, true);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			_store8phys(physicalAddress, value);
		}
	}

	public short load16(int address) {
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

	public void store16(int address, short value) {
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

	public int load32(int address) {
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

	public void store32(int address, int value) {
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

	private void raiseMemoryException(int address) {
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

	public byte read8(int address) {
		byte ret = load8(address);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}

	public void write8(int address, byte value) {
		store8(address, value);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public short read16(int address) {
		short ret = load16(address);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}

	public void write16(int address, short value) {
		store16(address, value);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public int read32(int address) {
		int ret = load32(address);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
		return ret;
	}
	
	public void write32(int address, int value) {
		store32(address, value);
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public int read32linked(int address) {
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


	public boolean write32conditional(int address, int value) {
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

	public int read32UnalignedLeft(int address, int oldValue) {
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

	public int read32UnalignedRight(int address, int oldValue) {
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

	public void write32UnalignedLeft(int address, int value) {
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

	public void write32UnalignedRight(int address, int value) {
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
					ret = ram.read32(physicalAddress, bigEndian);
				} else {
					ret = ioController.read32(physicalAddress, bigEndian);
					if (ioController.ioError())
						memoryError = MEMORY_ERROR_BUS_ERROR_INSTRUCTION;
				}
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

	public String disassemble(int count) {
		return disassemble(pc, count);
	}

	public String disassemble(int address, int count) {
		StringBuilder sb = new StringBuilder();
		address -= 4 * (count / 2);
		for(int i = 0; i < count; i++) {
			int opcode = load32(address + 4 * i);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				sb.append(Disassemble.disassemble(address + 4 * i, opcode));
			} else {
				sb.append("Invalid memory location");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void step() {
		step(1);
	}

	public void step(int num) {
		checkInterrupts();
		int before = getCounter();
		while(num > 0 && !halted) {
			int opcode = fetchOpcode();
			if (memoryError != MEMORY_ERROR_NOERROR) {
				opcode = fetchOpcode();
				if (memoryError != MEMORY_ERROR_NOERROR) {
					halted = true;
					break;
				}
			}
			nextDelaySlot = false;
			_nextPc = nextPc;
			nextPc = nextPc + 4;
			microstep(opcode);
			delaySlot = nextDelaySlot;
			pc = _nextPc;
			num--;
			if (num == 0 && delaySlot) num++;
		}
		counter += num;

		int after = getCounter();
		checkTimerInterrupt(before, after);
	}

	private void microstep(int opcode) {
		counter++;

		switch (I_OP(opcode)) {
		case 0: stepSpecial(opcode); break;
		case 1: stepRegImm(opcode); break;
		case 2: j(opcode); break;
		case 3: jal(opcode); break;
		case 4: beq(opcode); break;
		case 5: bne(opcode); break;
		case 6: blez(opcode); break;
		case 7: bgtz(opcode); break;

		case 8: addi(opcode); break;
		case 9: addiu(opcode); break;
		case 10: slti(opcode); break;
		case 11: sltiu(opcode); break;
		case 12: andi(opcode); break;
		case 13: ori(opcode); break;
		case 14: xori(opcode); break;
		case 15: lui(opcode); break;

		case 16: stepCop0(opcode); break;
		case 17: invalid(1); break;
		case 18: invalid(2); break;
		case 19: invalid(3); break;
		case 20: beql(opcode); break;
		case 21: bnel(opcode); break;
		case 22: blezl(opcode); break;
		case 23: bgtzl(opcode); break;

		case 24: reserved(); break;
		case 25: reserved(); break;
		case 26: reserved(); break;
		case 27: reserved(); break;
		case 28: stepSpecial2(opcode); break;
		case 29: reserved(); break;
		case 30: reserved(); break;
		case 31: reserved(); break;

		case 32: lb(opcode); break;
		case 33: lh(opcode); break;
		case 34: lwl(opcode); break;
		case 35: lw(opcode); break;
		case 36: lbu(opcode); break;
		case 37: lhu(opcode); break;
		case 38: lwr(opcode); break;
		case 39: reserved(); break;

		case 40: sb(opcode); break;
		case 41: sh(opcode); break;
		case 42: swl(opcode); break;
		case 43: sw(opcode); break;
		case 44: reserved(); break;
		case 45: reserved(); break;
		case 46: swr(opcode); break;
		case 47: cache(opcode); break;

		case 48: ll(opcode); break;
		case 49: reserved(); break; // ???
		case 50: reserved(); break; // ???
		case 51: pref(opcode); break;
		case 52: reserved(); break;
		case 53: reserved(); break; // ???
		case 54: reserved(); break; // ???
		case 55: reserved(); break;

		case 56: sc(opcode); break;
		case 57: reserved(); break; // ???
		case 58: reserved(); break; // ???
		case 59: reserved(); break;
		case 60: reserved(); break;
		case 61: reserved(); break; // ???
		case 62: reserved(); break; // ???
		case 63: reserved(); break;
		}
	}

	private void stepSpecial(int opcode) {
		switch(I_FUNCT(opcode)) {
		case 0: sll(opcode); break;
		case 1: reserved(); break; // ???
		case 2: srl(opcode); break;
		case 3: sra(opcode); break;
		case 4: sllv(opcode); break;
		case 6: srlv(opcode); break;
		case 7: srav(opcode); break;

		case 8: jr(opcode); break;
		case 9: jalr(opcode); break;
		case 10: movz(opcode); break;
		case 11: movn(opcode); break;
		case 12: syscall(opcode); break;
		case 13: break_(opcode); break;
		case 15: sync(opcode); break;

		case 16: mfhi(opcode); break;
		case 17: mthi(opcode); break;
		case 18: mflo(opcode); break;
		case 19: mtlo(opcode); break;

		case 24: mult(opcode); break;
		case 25: multu(opcode); break;
		case 26: div(opcode); break;
		case 27: divu(opcode); break;

		case 32: add(opcode); break;
		case 33: addu(opcode); break;
		case 34: sub(opcode); break;
		case 35: subu(opcode); break;
		case 36: and(opcode); break;
		case 37: or(opcode); break;
		case 38: xor(opcode); break;
		case 39: nor(opcode); break;

		case 42: slt(opcode); break;
		case 43: sltu(opcode); break;

		case 48: tge(opcode); break;
		case 49: tgeu(opcode); break;
		case 50: tlt(opcode); break;
		case 51: tltu(opcode); break;
		case 52: teq(opcode); break;
		case 54: tne(opcode); break;

		default: reserved(); break;
		}
	}

	private void stepSpecial2(int opcode) {
		switch(I_FUNCT(opcode)) {
		case 0: madd(opcode); break;
		case 1: maddu(opcode); break;
		case 2: mul(opcode); break;
		case 4: msub(opcode); break;
		case 5: msubu(opcode); break;
		case 32: clz(opcode); break;
		case 33: clo(opcode); break;
		case 63: sdbbp(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepRegImm(int opcode) {
		switch(I_RT(opcode)) {
		case 0: bltz(opcode); break;
		case 1: bgez(opcode); break;
		case 2: bltzl(opcode); break;
		case 3: bgezl(opcode); break;
		case 8: tgei(opcode); break;
		case 9: tgeiu(opcode); break;
		case 10: tlti(opcode); break;
		case 11: tltiu(opcode); break;
		case 12: teqi(opcode); break;
		case 14: tnei(opcode); break;
		case 16: bltzal(opcode); break;
		case 17: bgezal(opcode); break;
		case 18: bltzall(opcode); break;
		case 19: bgezall(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepCop0(int opcode) {
		switch(I_RS(opcode)) {
		case 0: mfc0(opcode); break;
		case 4: mtc0(opcode); break;
		case 16:
		case 17:
		case 18:
		case 19:
		case 20:
		case 21:
		case 22:
		case 23:
		case 24:
		case 25:
		case 26:
		case 27:
		case 28:
		case 29:
		case 30:
		case 31: stepCop0Co(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepCop0Co(int opcode) {
		switch(I_FUNCT(opcode)) {
		case 1: tlbr(opcode); break;
		case 2: tlbwi(opcode); break;
		case 6: tlbwr(opcode); break;
		case 8: tlbp(opcode); break;
		case 24: eret(opcode); break;
		case 31: deret(opcode); break;
		case 32: wait(opcode); break;
		default: reserved(); break;
		}
	}

	private void add(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs + rt;
		if (checkOverflow(rs, rt, result, true)) return;
		setGpr(I_RD(opcode), result);
	}

	private void addi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = rs + imm;
		if (checkOverflow(rs, imm, result, true)) return;
		setGpr(I_RT(opcode), result);
	}

	private void addiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = rs + imm;
		setGpr(I_RT(opcode), result);
	}

	private void addu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs + rt;
		setGpr(I_RD(opcode), result);
	}

	private void and(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs & rt;
		setGpr(I_RD(opcode), result);
	}

	private void andi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs & immu;
		setGpr(I_RT(opcode), result);
	}

	private void beq(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs == rt) {
			branch(opcode);
		}
	}

	private void beql(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs == rt) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void bgez(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		}
	}

	private void bgezal(int opcode) {
		int rs = gpr[I_RS(opcode)];
		link();
		if (rs >= 0) {
			branch(opcode);
		}
	}

	private void bgezall(int opcode) {
		int rs = gpr[I_RS(opcode)];
		link();
		if (rs >= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void bgezl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void bgtz(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs > 0) {
			branch(opcode);
		}
	}

	private void bgtzl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs > 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void blez(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs <= 0) {
			branch(opcode);
		}
	}

	private void blezl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs <= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void bltz(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		}
	}

	private void bltzal(int opcode) {
		int rs = gpr[I_RS(opcode)];
		link();
		if (rs < 0) {
			branch(opcode);
		}
	}

	private void bltzall(int opcode) {
		int rs = gpr[I_RS(opcode)];
		link();
		if (rs < 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void bltzl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void bne(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs != rt) {
			branch(opcode);
		}
	}

	private void bnel(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs != rt) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	private void break_(int opcode) {
		exception_BREAK();
	}

	private void cache(int opcode) {
		if (checkCoprocessor(0)) {
			// No cache emulation is done
		}
	}

	private void clo(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int result = Utils.countLeadingOnes(rs);
		setGpr(I_RD(opcode), result);
	}

	private void clz(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int result = Utils.countLeadingZeros(rs);
		setGpr(I_RD(opcode), result);
	}

	private void deret(int opcode) {
		//TODO
	}

	private void div(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rt == 0) {
			lo = hi = 0;
		} else {
			lo = rs / rt;
			hi = rs % rt;
		}
	}

	private void divu(int opcode) {
		long rs = gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		if (rt == 0) {
			lo = hi = 0;
		} else {
			lo = (int) (rs / rt);
			hi = (int) (rs % rt);
		}
	}

	private void eret(int opcode) {
		if (checkCoprocessor(0)) {
			returnFromException();
		}
	}

	private void j(int opcode) {
		nextPc = I_JUMP(opcode, pc);
		nextDelaySlot = true;
	}

	private void jal(int opcode) {
		link();
		j(opcode);
	}

	private void jalr(int opcode) {
		link(I_RD(opcode));
		jr(opcode);
	}

	private void jr(int opcode) {
		int rs = gpr[I_RS(opcode)];
		nextPc = rs;
		nextDelaySlot = true;
	}

	private void lb(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lbu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address) & 0xFF;
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lh(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lhu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address) & 0xFFFF;
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void ll(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read32linked(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lui(int opcode) {
		int imm = I_IMM16(opcode);
		int result = imm << 16;
		setGpr(I_RT(opcode), result);
	}

	private void lw(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read32(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lwl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = I_RT(opcode);
		int val = read32UnalignedLeft(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, val);
		}
	}

	private void lwr(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = I_RT(opcode);
		int val = read32UnalignedRight(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, val);
		}
	}

	private void madd(int opcode) {
		long rs = gpr[I_RS(opcode)];
		long rt = gpr[I_RT(opcode)];
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo + rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	private void maddu(int opcode) {
		long rs = gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo + rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	private void mfc0(int opcode) {
		if (checkCoprocessor(0)) {
			int rt = I_RT(opcode);
			int rd = I_RD(opcode);
			int sel = I_COP0SEL(opcode);
			setGpr(rt, getCop0Reg(rd, sel));
		}
	}

	private void mfhi(int opcode) {
		setGpr(I_RD(opcode), hi);
	}

	private void mflo(int opcode) {
		setGpr(I_RD(opcode), lo);
	}

	private void movn(int opcode) {
		int rt = gpr[I_RT(opcode)];
		if (rt != 0) {
			int rs = gpr[I_RS(opcode)];
			setGpr(I_RD(opcode), rs);
		}
	}

	private void movz(int opcode) {
		int rt = gpr[I_RT(opcode)];
		if (rt == 0) {
			int rs = gpr[I_RS(opcode)];
			setGpr(I_RD(opcode), rs);
		}
	}

	private void msub(int opcode) {
		long rs = gpr[I_RS(opcode)];
		long rt = gpr[I_RT(opcode)];
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo - rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	private void msubu(int opcode) {
		long rs = gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo - rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	private void mtc0(int opcode) {
		if (checkCoprocessor(0)) {
			int rt = I_RT(opcode);
			int rd = I_RD(opcode);
			int sel = I_COP0SEL(opcode);
			setCop0Reg(rd, sel, gpr[rt]);
		}
	}

	private void mthi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		hi = rs;
	}

	private void mtlo(int opcode) {
		int rs = gpr[I_RS(opcode)];
		lo = rs;
	}

	private void mul(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs * rt;
		setGpr(I_RD(opcode), result);
		// hi = lo = 0; // Unpredictable
	}

	private void mult(int opcode) {
		long rs = gpr[I_RS(opcode)];
		long rt = gpr[I_RT(opcode)];
		long result = rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	private void multu(int opcode) {
		long rs = gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		long result = rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	private void nor(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = ~(rs | rt);
		setGpr(I_RD(opcode), result);
	}

	private void or(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs | rt;
		setGpr(I_RD(opcode), result);
	}

	private void ori(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs | immu;
		setGpr(I_RT(opcode), result);
	}

	private void pref(int opcode) {
		//TODO
	}

	private void sb(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[I_RT(opcode)];
		write8(address, (byte) rt);
	}

	private void sc(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = I_RT(opcode);
		boolean ok = write32conditional(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, ok ? 1 : 0);
		}
	}

	private void sdbbp(int opcode) {
		//TODO
	}

	private void sh(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[I_RT(opcode)];
		write16(address, (short) rt);
	}

	private void sll(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt << sa;
		setGpr(I_RD(opcode), result);
	}

	private void sllv(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = rt << rs;
		setGpr(I_RD(opcode), result);
	}

	private void slt(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = (rs < rt) ? 1 : 0;
		setGpr(I_RD(opcode), result);
	}

	private void slti(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = (rs < imm) ? 1 : 0;
		setGpr(I_RT(opcode), result);
	}

	private void sltiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = (Utils.compareUnsigned(rs, imm) < 0) ? 1 : 0;
		setGpr(I_RT(opcode), result);
	}

	private void sltu(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = (Utils.compareUnsigned(rs, rt) < 0) ? 1 : 0;
		setGpr(I_RD(opcode), result);
	}

	private void sra(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt >> sa;
		setGpr(I_RD(opcode), result);
	}

	private void srav(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = rt >> rs;
		setGpr(I_RD(opcode), result);
	}

	private void srl(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt >>> sa;
		setGpr(I_RD(opcode), result);
	}

	private void srlv(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = rt >>> rs;
		setGpr(I_RD(opcode), result);
	}

	private void sub(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs - rt;
		if (checkOverflow(rs, rt, result, false)) return;
		setGpr(I_RD(opcode), result);
	}

	private void subu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs - rt;
		setGpr(I_RD(opcode), result);
	}

	private void sw(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[I_RT(opcode)];
		write32(address, rt);
	}

	private void swl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[I_RT(opcode)];
		write32UnalignedLeft(address, rt);
	}

	private void swr(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[I_RT(opcode)];
		write32UnalignedRight(address, rt);
	}

	private void sync(int opcode) {
		// No cache emulation is done
	}

	private void syscall(int opcode) {
		exception_SYSCALL();
	}

	private void teq(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs == rt) {
			exception_TRAP();
		}
	}

	private void teqi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs == imm) {
			exception_TRAP();
		}
	}

	private void tge(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs >= rt) {
			exception_TRAP();
		}
	}

	private void tgei(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs >= imm) {
			exception_TRAP();
		}
	}

	private void tgeiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (Utils.compareUnsigned(rs, imm) >= 0) {
			exception_TRAP();
		}
	}

	private void tgeu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (Utils.compareUnsigned(rs, rt) >= 0) {
			exception_TRAP();
		}
	}

	private void tlbp(int opcode) {
		if (checkCoprocessor(0)) {
			tlbProbe();
		}
	}

	private void tlbr(int opcode) {
		if (checkCoprocessor(0)) {
			tlbRead();
		}
	}

	private void tlbwi(int opcode) {
		if (checkCoprocessor(0)) {
			tlbWriteIndex();
		}
	}

	private void tlbwr(int opcode) {
		if (checkCoprocessor(0)) {
			tlbWriteRandom();
		}
	}

	private void tlt(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs < rt) {
			exception_TRAP();
		}
	}

	private void tlti(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs < imm) {
			exception_TRAP();
		}
	}

	private void tltiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (Utils.compareUnsigned(rs, imm) < 0) {
			exception_TRAP();
		}
	}

	private void tltu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (Utils.compareUnsigned(rs, rt) < 0) {
			exception_TRAP();
		}
	}

	private void tne(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs != rt) {
			exception_TRAP();
		}
	}

	private void tnei(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs != imm) {
			exception_TRAP();
		}
	}

	private void wait(int opcode) {
		if (checkCoprocessor(0)) {
			halted = true;
		}
	}

	private void xor(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs ^ rt;
		setGpr(I_RD(opcode), result);
	}

	private void xori(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs ^ immu;
		setGpr(I_RT(opcode), result);
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
		nextPc = I_BRANCH(opcode, pc);
		nextDelaySlot = true;
	}

	private void link() {
		link(GPR_RA);
	}

	private void link(int regno) {
		setGpr(regno, _nextPc + 4);
	}

	private void skipDelaySlot() {
		_nextPc += 4;
	}


	public boolean isCoprocessorAvailable(int copno) {
		if (copno == 0 && kernelMode) return true;
		return (Status & (1 << (STATUS_COP_SHIFT + copno))) != 0;
	}

	private int readRegisterRandom() {
		return Wired + rand.nextInt(NUM_TLB_ENTRIES - Wired);
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
		case COP0_REG_INDEX:
			Index = changeValue(Index, value, INDEX_MASK);
			break;
		case COP0_REG_RANDOM:
			// Ignore random writes
			break;
		case COP0_REG_ENTRYLO0:
			EntryLo0 = value & ENTRYLO_WRITE_MASK;
			break;
		case COP0_REG_ENTRYLO1:
			EntryLo1 = value & ENTRYLO_WRITE_MASK;
			break;
		case COP0_REG_CONTEXT:
			Context = changeValue(Context, value, CONTEXT_WRITE_MASK);
			break;
		case COP0_REG_PAGEMASK:
			PageMask = value & 0x01FFE000;
			break;
		case COP0_REG_WIRED:
			Wired = value & INDEX_MASK;
			break;
		case COP0_REG_RESERVED1:
			Reserved1 = value;
			break;
		case COP0_REG_BADVADDR:
			// Ignore BadVAddr writes
			break;
		case COP0_REG_COUNT:
			setCounter(value);
			break;
		case COP0_REG_ENTRYHI:
			EntryHi = value & ENTRYHI_WRITE_MASK;
			ASID = value & ENTRYHI_ASID_MASK;
			break;
		case COP0_REG_COMPARE:
			Compare = value;
			lowerIrq(TIMER_IRQ);
			break;
		case COP0_REG_STATUS:
			writeStatus(value);
			break;
		case COP0_REG_CAUSE:
			Cause = changeValue(Cause, value, CAUSE_WRITE_MASK);
			break;
		case COP0_REG_EPC:
			EPC = value;
			break;
		case COP0_REG_PRID:
			// Ignore PRId writes
			break;
		case COP0_REG_CONFIG:
			if (sel == 0) {
				Config = changeValue(Config,  value, 0x07);
			}
			// Ignores Config1 writes
			break;
		case COP0_REG_LLADDR:
			// Ignore LLAddr writes
			break;
		case COP0_REG_WATCHLO:
			WatchLo = value;
			break;
		case COP0_REG_WATCHHI:
			WatchHi = value;
			break;
		case COP0_REG_RESERVED2:
			Reserved2 = value;
			break;
		case COP0_REG_RESERVED3:
			Reserved3 = value;
			break;
		case COP0_REG_RESERVED4:
			Reserved4 = value;
			break;
		case COP0_REG_DEBUG:
			Debug = value;
			break;
		case COP0_REG_DEPC:
			DEPC = value;
			break;
		case COP0_REG_RESERVED5:
			Reserved5 = value;
			break;
		case COP0_REG_ERRCTRL:
			ErrCtrl = value;
			break;
		case COP0_REG_RESERVED6:
			Reserved6 = value;
			break;
		case COP0_REG_TAGLO:
			if (sel == 0) TagLo = value;
			else DataLo = value;
			break;
		case COP0_REG_RESERVED7:
			Reserved7 = value;
			break;
		case COP0_REG_ERROREPC:
			ErrorEPC = value;
			break;
		case COP0_REG_DESAVE:
			DESAVE = value;
			break;
		}
	}

	public int getCop0Reg(int reg, int sel) {
		int retval = 0;
		switch(reg) {
		case COP0_REG_INDEX:
			retval = Index;
			break;
		case COP0_REG_RANDOM:
			retval = readRegisterRandom();
			break;
		case COP0_REG_ENTRYLO0:
			retval = EntryLo0;
			break;
		case COP0_REG_ENTRYLO1:
			retval = EntryLo1;
			break;
		case COP0_REG_CONTEXT:
			retval = Context;
			break;
		case COP0_REG_PAGEMASK:
			retval = PageMask;
			break;
		case COP0_REG_WIRED:
			retval = Wired;
			break;
		case COP0_REG_RESERVED1:
			retval = Reserved1;
			break;
		case COP0_REG_BADVADDR:
			retval = BadVAddr;
			break;
		case COP0_REG_COUNT:
			retval = getCounter();
			break;
		case COP0_REG_ENTRYHI:
			retval = EntryHi;
			break;
		case COP0_REG_COMPARE:
			retval = Compare;
			break;
		case COP0_REG_STATUS:
			retval = Status;
			break;
		case COP0_REG_CAUSE:
			retval = Cause;
			break;
		case COP0_REG_EPC:
			retval = EPC;
			break;
		case COP0_REG_PRID:
			retval = PRId;
			break;
		case COP0_REG_CONFIG:
			if (sel == 0) retval = Config;
			else retval = Config1;
			break;
		case COP0_REG_LLADDR:
			retval = LLAddr;
			break;
		case COP0_REG_WATCHLO:
			retval = WatchLo;
			break;
		case COP0_REG_WATCHHI:
			retval = WatchHi;
			break;
		case COP0_REG_RESERVED2:
			retval = Reserved2;
			break;
		case COP0_REG_RESERVED3:
			retval = Reserved3;
			break;
		case COP0_REG_RESERVED4:
			retval = Reserved4;
			break;
		case COP0_REG_DEBUG:
			retval = Debug;
			break;
		case COP0_REG_DEPC:
			retval = DEPC;
			break;
		case COP0_REG_RESERVED5:
			retval = Reserved5;
			break;
		case COP0_REG_ERRCTRL:
			retval = ErrCtrl;
			break;
		case COP0_REG_RESERVED6:
			retval = Reserved6;
			break;
		case COP0_REG_TAGLO:
			if (sel == 0) retval = TagLo;
			else retval = DataLo;
			break;
		case COP0_REG_RESERVED7:
			retval = Reserved7;
			break;
		case COP0_REG_ERROREPC:
			retval = ErrorEPC;
			break;
		case COP0_REG_DESAVE:
			retval = DESAVE;
			break;
		}
		return retval;
	}

	private void exception_RESET() {
		Wired = 0;
		Config = changeValue(Config, 2, 0x07);
		writeStatus(changeValue(Status, STATUS_BEV | STATUS_ERL, STATUS_RP | STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL));
		ErrorEPC = isBranchDelaySlot() ? getPc() - 4 : getPc();

		setPc(0xBFC00000);
	}

	private void exception_SOFT_RESET() {
		writeStatus(changeValue(Status, STATUS_SR | STATUS_BEV | STATUS_ERL, STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL));
		ErrorEPC = isBranchDelaySlot() ? getPc() - 4 : getPc();

		setPc(0xBFC00000);
	}

	private void exception_NMI() {
		writeStatus(changeValue(Status, STATUS_BEV | STATUS_NMI | STATUS_ERL, STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL));
		ErrorEPC = isBranchDelaySlot() ? getPc() - 4 : getPc();

		setPc(0xBFC00000);
	}

	private void exception_GENERAL(int code, int copno, boolean offsetToZero) {
		int vectorOffset;

		if ((Status & STATUS_EXL) == 0) {
			if (isBranchDelaySlot()) {
				EPC = getPc() - 4;
				Cause |= CAUSE_BD;
			} else {
				EPC = getPc();
				Cause &= ~CAUSE_BD;
			}
			if (offsetToZero) {
				vectorOffset = 0;
			} else if (code == EXCEPTION_CODE_INT && ((Cause & CAUSE_IV) != 0)) {
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
		exception_GENERAL(EXCEPTION_CODE_MCHECK, 0, false);
		Status |= STATUS_TS;
	}

	private void exception_INTERRUPT() {
		exception_GENERAL(EXCEPTION_CODE_INT, 0, false);
	}

	private void exception_ADDRESS_ERROR(int badVAddr, boolean load) {
		exception_GENERAL(load ? EXCEPTION_CODE_ADEL : EXCEPTION_CODE_ADES, 0, false);
		BadVAddr = badVAddr;
	}

	private void exception_TLB_REFILL(int badVAddr, boolean load) {
		exception_GENERAL(load ? EXCEPTION_CODE_TLBL : EXCEPTION_CODE_TLBS, 0, true);
		BadVAddr = badVAddr;
		Context = (Context & CONTEXT_PTE_MASK) | ((badVAddr & ENTRYHI_VPN2_MASK) >>> 9);
		EntryHi = (EntryHi & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	private void exception_TLB_INVALID(int badVAddr, boolean load) {
		exception_GENERAL(load ? EXCEPTION_CODE_TLBL : EXCEPTION_CODE_TLBS, 0, false);
		BadVAddr = badVAddr;
		Context = (Context & CONTEXT_PTE_MASK) | ((badVAddr & ENTRYHI_VPN2_MASK) >>> 9);
		EntryHi = (EntryHi & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	private void exception_BUS_ERROR(boolean data) {
		exception_GENERAL(data ? EXCEPTION_CODE_DBE : EXCEPTION_CODE_IBE, 0, false);
	}

	private void exception_SYSCALL() {
		exception_GENERAL(EXCEPTION_CODE_SYS, 0, false);
	}

	private void exception_BREAK() {
		exception_GENERAL(EXCEPTION_CODE_BP, 0, false);
	}

	private void exception_RESERVED() {
		exception_GENERAL(EXCEPTION_CODE_RI, 0, false);
	}

	private void exception_COPROCESS_UNUSABLE(int copno) {
		exception_GENERAL(EXCEPTION_CODE_CPU, copno, false);
	}

	private void exception_INTEGER_OVERFLOW() {
		exception_GENERAL(EXCEPTION_CODE_OV, 0, false);
	}

	private void exception_TRAP() {
		exception_GENERAL(EXCEPTION_CODE_TR, 0, false);
	}

	private void exception_TLB_MOD(int badVAddr) {
		exception_GENERAL(EXCEPTION_CODE_MOD, 0, false);
		BadVAddr = badVAddr;
		Context = badVAddr;
	}

	private void returnFromException() {
		int newPc;
		loadLinkedStatus = false;
		if ((Status & STATUS_ERL) != 0) {
			writeStatus(Status & (~STATUS_ERL));
			newPc = ErrorEPC;
		} else {
			writeStatus(Status & (~STATUS_EXL));
			newPc = EPC;
		}
		setPc(newPc);
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
			tlbEntries[i].initialized = false;
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

		if (tlbEntry.initialized) {
			tlbEntryPage = tlbMatchEntry(tlbEntry, address, ASID);
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}

		// Perform the TLB search
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			tlbEntry = tlbEntries[idx];

			if (!tlbEntry.initialized) continue;
			tlbEntryPage = tlbMatchEntry(tlbEntry, address, ASID); 
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

	private TlbEntryPage tlbMatchEntry(TlbEntry entry, int address, int ASID) {
		if ((entry.PageMask & address) == entry.VPN2) {
			if (entry.global || entry.ASID == ASID) {
				if ((address & entry.selectionBit) == 0) {
					return entry.page0;
				} else {
					return entry.page1;
				}
			}
		}
		return null;
	}

	private int tlbPageTranslate(TlbEntry tlbEntry, TlbEntryPage tlbEntryPage, int address, boolean write, boolean data) {
		if (data) lastTlbEntryData = tlbEntry;
		else lastTlbEntryCode = tlbEntry;
		if (!tlbEntryPage.valid) {
			memoryError = write ? MEMORY_ERROR_TLB_INVALID_STORE :
			                      MEMORY_ERROR_TLB_INVALID_LOAD;
			return 0;
		}
		if (write && !tlbEntryPage.dirty) {
			memoryError = MEMORY_ERROR_TLB_MOD;
			return 0;
		}
		address &= ~(tlbEntry.PageMask | tlbEntry.selectionBit);
		address |= tlbEntryPage.PFN;
		return address;
	}

	private int convertPageToEntryLo(TlbEntryPage page, boolean global) {
		int entry = page.PFN >>> 6;
		entry |= page.cacheability << ENTRYLO_COHERENCY_SHIFT;
		if (page.dirty) entry |= ENTRYLO_DIRTY;
		if (page.valid) entry |= ENTRYLO_VALID;
		if (global) entry |= ENTRYLO_GLOBAL;
		return entry;
	}

	private void configurePageFromEntryLo(TlbEntryPage page, int entry) {
		page.PFN = (entry << 6) & 0xFFFFF000;
		page.cacheability = (entry & ENTRYLO_COHERENCY_MASK) >> ENTRYLO_COHERENCY_SHIFT;
		page.dirty = (entry & ENTRYLO_DIRTY) != 0;
		page.valid = (entry & ENTRYLO_VALID) != 0;
	}

	private void tlbProbe() {
		int ASID = EntryHi & ENTRYHI_ASID_MASK;
		int VPN2 = EntryHi & ENTRYHI_VPN2_MASK;
		Index = INDEX_PROBE;
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			TlbEntry tlbEntry = tlbEntries[idx];
			TlbEntryPage tlbEntryPage;

			if (!tlbEntry.initialized) continue;
			tlbEntryPage = tlbMatchEntry(tlbEntry, VPN2, ASID); 
			if (tlbEntryPage != null) {
				Index = idx;
				return;
			}
		}
	}

	private void tlbRead() {
		TlbEntry entry = tlbEntries[Index & INDEX_MASK];
		PageMask = (~entry.PageMask) & ENTRYHI_VPN2_MASK;
		EntryHi = entry.VPN2 | entry.ASID;
		EntryLo0 = convertPageToEntryLo(entry.page0, entry.global);
		EntryLo1 = convertPageToEntryLo(entry.page1, entry.global);
	}

	private void tlbWrite(int index) {
		TlbEntry tlbEntry;
		int mask = (~PageMask) & ENTRYHI_VPN2_MASK;
		int VPN2 = EntryHi & mask;
		int ASID = EntryHi & ENTRYHI_ASID_MASK;
		boolean global = ((EntryLo0 & ENTRYLO_GLOBAL) != 0) && ((EntryLo1 & ENTRYLO_GLOBAL) != 0);

		// Checks for multiple entries
		for (int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			tlbEntry = tlbEntries[idx];
			if (!tlbEntry.initialized || idx == index) continue;
			if ((tlbEntry.VPN2 & mask) == VPN2) {
				if (global || tlbEntry.global || tlbEntry.ASID == ASID) {
					exception_MCHECK();
					return;
				}
			}
		}
		tlbEntry = tlbEntries[index];
		tlbEntry.initialized = true;
		tlbEntry.PageMask = mask;
		tlbEntry.VPN2 = VPN2;
		tlbEntry.ASID = ASID;
		tlbEntry.global = global;
		tlbEntry.selectionBit = mask ^ (mask >> 1);
		configurePageFromEntryLo(tlbEntry.page0, EntryLo0);
		configurePageFromEntryLo(tlbEntry.page1, EntryLo1);
		//System.out.printf("Idx: %d PageMask = 0x%08X, mask = 0x%08X\n", index, PageMask, mask);
		//System.out.println(tlbEntry);
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

	private static final class TlbEntry {
		private int PageMask; // Negation of original PageMask and'ed with ENTRYHI_VPN2_MASK
		private int VPN2;     // Masked VPN2
		private int selectionBit;
		private boolean global;
		private int ASID;
		private final TlbEntryPage page0 = new TlbEntryPage();
		private final TlbEntryPage page1 = new TlbEntryPage();
		private boolean initialized;

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("PageMask: 0x%08X\n", PageMask));
			sb.append(String.format("VPN2: 0x%08X\n", VPN2));
			sb.append(String.format("Selection Bit: 0x%08X\n", selectionBit));
			sb.append(String.format("ASID: 0x%08X\n", ASID));
			sb.append("Global: ").append(global).append("\n");
			sb.append("Initialized: ").append(initialized).append("\n");
			sb.append("Page 0: ").append(page0.toString()).append("\n");
			sb.append("Page 1: ").append(page1.toString()).append("\n");
			return sb.toString();
		}
	}

	private static final class TlbEntryPage {
		private int PFN;
		private int cacheability;
		private boolean dirty;
		private boolean valid;

		@Override
		public String toString() {
			return String.format("PFN: %08X Cacheability: %d %s %s", PFN,
			                     cacheability, dirty ? "DT" : "ND", valid ? "VL" : "NV");
		}
	}


	// Auxiliary functions to decode the opcode
	public static int I_OP(int opcode) {
		return (opcode >>> 26);
	}

	public static int I_FUNCT(int opcode) {
		return opcode & 0x3F;
	}

	public static int I_RS(int opcode) {
		return ((opcode >> 21) & 0x1F);
	}

	public static int I_RT(int opcode) {
		return ((opcode >> 16) & 0x1F);
	}

	public static int I_RD(int opcode) {
		return ((opcode >> 11) & 0x1F);
	}

	public static int I_SA(int opcode) {
		return ((opcode >> 6) & 0x1F);
	}

	public static int I_IMM16(int opcode) {
		return (int) ((short) opcode);
	}

	public static int I_IMM16U(int opcode) {
		return opcode & 0xFFFF;
	}

	public static int I_BRANCH(int opcode, int pc) {
		return pc + 4 + 4 * I_IMM16(opcode);
	}

	public static int I_JUMP(int opcode, int pc) {
		return ((pc & 0xF0000000) | ((opcode & 0x3FFFFFF) << 2));
	}

	public static int I_SYSCALLCODE(int opcode) {
		return ((opcode >> 6) & 0xFFFFF);
	}

	public static int I_TRAPCODE(int opcode) {
		return ((opcode >> 6) & 0x3FF);
	}

	public static int I_WAITCODE(int opcode) {
		return ((opcode >> 6) & 0x7FFFF);
	}

	public static int I_COP0SEL(int opcode) {
		return (opcode & 0x07);
	}
}
