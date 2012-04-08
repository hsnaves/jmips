package jmips.cpu;

import static jmips.cpu.Mips.*;
import jmips.cpu.disasm.LabelResolver;

/**
 * Java implementation of a MIPS32 4Kc processor
 */
public final class Cpu {

	// Instance fields
	private final int[] gpr = new int[32];
	private int hi, lo;

	private int pc, exceptionPc, nextPc;
	private long counter;

	private boolean halted;
	private int memoryError;

	private final Ram ram;
	private final Device ioController;
	private final Cop0 cop0;

	private boolean bigEndian = true;
	private boolean loadLinkedStatus = false;

	private LabelResolver labelResolver;

	public Cpu(int ramSize, Device io) {
		this.ram = new Ram(ramSize);
		this.cop0 = new Cop0();
		this.ioController = io;
		hardReset();
	}

	public void setLabelResolver(LabelResolver labelResolver) {
		this.labelResolver = labelResolver;
	}

	public void hardReset() {
		cop0.resetTlb();
		reset();
	}

	public void reset() {
		cop0.exceptionReset(this);
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

	public Ram getRam() {
		return ram;
	}

	public Cop0 getCop0() {
		return cop0;
	}

	public boolean isHalted() {
		return halted;
	}

	public void setPc(int pc) {
		this.pc = pc;
		this.nextPc = pc + 4;
		this.exceptionPc = pc;
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
		boolean delaySlot;
		if (exceptionPc == pc)
			delaySlot = (nextPc != (exceptionPc + 4));
		else
			delaySlot = (pc != (exceptionPc + 4));

		return delaySlot;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public void setBigEndian(boolean bigEndian) {
		this.bigEndian = bigEndian;
	}

	public boolean isLoadLinkedStatus() {
		return loadLinkedStatus;
	}

	public void setLoadLinkedStatus(boolean loadLinkedStatus) {
		this.loadLinkedStatus = loadLinkedStatus;
	}

	public int getCop0Reg(int reg, int sel) {
		return cop0.getCop0Reg(this, reg, sel);
	}

	public void setCop0Reg(int reg, int sel, int value) {
		cop0.setCop0Reg(this, reg, sel, value);
	}

	public void raiseIrq(int irqno, boolean raise) {
		cop0.raiseIrq(irqno, raise);
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

		int physicalAddress = cop0.translate(address, false);
		if (physicalAddress >= 0) {
			return _load8phys(physicalAddress);
		} else {
			memoryError = -physicalAddress;
			return 0;
		}
	}

	public void store8(final int address, byte value) {
		memoryError = MEMORY_ERROR_NOERROR;

		int physicalAddress = cop0.translate(address, true);
		if (physicalAddress >= 0) {
			_store8phys(physicalAddress, value);
		} else {
			memoryError = -physicalAddress;
		}
	}

	public short load16(final int address) {
		if ((address & 1) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = cop0.translate(address, false);
			if (physicalAddress >= 0) {
				return _load16phys(physicalAddress);
			} else {
				memoryError = -physicalAddress;
				return 0;
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
			return 0;
		}
	}

	public void store16(final int address, short value) {
		if ((address & 1) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = cop0.translate(address, true);
			if (physicalAddress >= 0) {
				_store16phys(physicalAddress, value);
			} else {
				memoryError = -physicalAddress;
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}

	public int load32(final int address) {
		if ((address & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = cop0.translate(address, false);
			if (physicalAddress >= 0) {
				return _load32phys(physicalAddress);
			} else {
				memoryError = -physicalAddress;
				return 0;
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_LOAD;
			return 0;
		}
	}

	public void store32(final int address, int value) {
		if ((address & 3) == 0) {
			memoryError = MEMORY_ERROR_NOERROR;

			int physicalAddress = cop0.translate(address, true);
			if (physicalAddress >= 0) {
				_store32phys(physicalAddress, value);
			} else {
				memoryError = -physicalAddress;
			}
		} else {
			memoryError = MEMORY_ERROR_ADDRESS_ERROR_STORE;
		}
	}

	private void raiseMemoryException(final int address) {
		switch(memoryError) {
		case MEMORY_ERROR_ADDRESS_ERROR_LOAD:
			cop0.exceptionAddressError(this, address, true);
			break;
		case MEMORY_ERROR_ADDRESS_ERROR_STORE:
			cop0.exceptionAddressError(this, address, false);
			break;
		case MEMORY_ERROR_BUS_ERROR_DATA:
			cop0.exceptionBusError(this, true);
			break;
		case MEMORY_ERROR_BUS_ERROR_INSTRUCTION:
			cop0.exceptionBusError(this, false);
			break;
		case MEMORY_ERROR_TLB_INVALID_LOAD:
			cop0.exceptionTlbInvalid(this, address, true);
			break;
		case MEMORY_ERROR_TLB_INVALID_STORE:
			cop0.exceptionTlbInvalid(this, address, false);
			break;
		case MEMORY_ERROR_TLB_REFILL_LOAD:
			cop0.exceptionTlbRefill(this, address, true);
			break;
		case MEMORY_ERROR_TLB_REFILL_STORE:
			cop0.exceptionTlbRefill(this, address, false);
			break;
		case MEMORY_ERROR_TLB_MOD:
			cop0.exceptionTlbModified(this, address);
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
		int ret = read32(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			loadLinkedStatus = true;
			cop0.setLoadLinkedRegister(address);
		}
		return ret;
	}


	public boolean write32conditional(final int address, int value) {
		memoryError = MEMORY_ERROR_NOERROR;
		if (loadLinkedStatus) {
			write32(address, value);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				loadLinkedStatus = false;
				return true;
			}
		}
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
		 // Pretend that we are writing, even though we are reading first
		int physicalAddress = cop0.translate(alignedAddress, true);
		if (physicalAddress >= 0) {
			int oldValue = _load32phys(physicalAddress);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				int shift = (bigEndian) ? (address & 3) : 3 - (address & 3);
				shift <<= 3;
				int newValue = (oldValue & ~((-1) >>> shift)) | (value >>> shift);
				_store32phys(physicalAddress, newValue);
			}
		} else {
			memoryError = -physicalAddress;
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public void write32UnalignedRight(final int address, int value) {
		int alignedAddress = address & (~3);

		memoryError = MEMORY_ERROR_NOERROR;
		 // Pretend that we are writing, even though we are reading first
		int physicalAddress = cop0.translate(alignedAddress, true);
		if (physicalAddress >= 0) {
			int oldValue = _load32phys(physicalAddress);
			if (memoryError == MEMORY_ERROR_NOERROR) {
				int shift = (!bigEndian) ? (address & 3) : 3 - (address & 3);
				shift <<= 3;
				int newValue = (oldValue & ~((-1) << shift)) | (value << shift); 
				_store32phys(physicalAddress, newValue);
			}
		} else {
			memoryError = -physicalAddress;
		}
		if (memoryError != MEMORY_ERROR_NOERROR)
			raiseMemoryException(address);
	}

	public int fetchOpcode() {
		int ret = load32(pc);
		if (memoryError != MEMORY_ERROR_NOERROR) {
			if (memoryError == MEMORY_ERROR_BUS_ERROR_DATA)
				memoryError = MEMORY_ERROR_BUS_ERROR_INSTRUCTION;
			raiseMemoryException(pc);
		}
		return ret;
	}

	public int getLastMemoryError() {
		return memoryError;
	}

	public void step() {
		step(1);
	}

	public void step(int num) {
		cop0.checkInterrupts(this);
		while(num > 0 && !halted) {
			num--;
			counter++;

			checkTimerInterrupt();

			int opcode = fetchOpcode();
			if (memoryError != MEMORY_ERROR_NOERROR) {
				opcode = fetchOpcode();
			}

			pc = nextPc;
			nextPc += 4;

			stepMips(opcode);
			exceptionPc = pc;
		}
		if (num > 0) counter += num;
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
				sb.append(Mips.disassemble(labelResolver, opcode, address + 4 * i, true));
			} else {
				sb.append("Invalid memory location");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public void stepMips(int opcode) {
		switch (DECODE_OP(opcode)) {
		case I_SPECIAL: stepSpecial(opcode); break;
		case I_REGIMM:  stepRegImm(opcode); break;
		case I_J:       j(opcode); break;
		case I_JAL:     jal(opcode); break;
		case I_BEQ:     beq(opcode); break;
		case I_BNE:     bne(opcode); break;
		case I_BLEZ:    blez(opcode); break;
		case I_BGTZ:    bgtz(opcode); break;

		case I_ADDI:    addi(opcode); break;
		case I_ADDIU:   addiu(opcode); break;
		case I_SLTI:    slti(opcode); break;
		case I_SLTIU:   sltiu(opcode); break;
		case I_ANDI:    andi(opcode); break;
		case I_ORI:     ori(opcode); break;
		case I_XORI:    xori(opcode); break;
		case I_LUI:     lui(opcode); break;

		case I_COP0:    stepCop0(opcode); break;
		case I_COP1:    invalid(1); break;
		case I_COP2:    invalid(2); break;
		case I_COP1X:   invalid(1); break;
		case I_BEQL:    beql(opcode); break;
		case I_BNEL:    bnel(opcode); break;
		case I_BLEZL:   blezl(opcode); break;
		case I_BGTZL:   bgtzl(opcode); break;

		case I_SPECIAL2: stepSpecial2(opcode); break;

		case I_LB:      lb(opcode); break;
		case I_LH:      lh(opcode); break;
		case I_LWL:     lwl(opcode); break;
		case I_LW:      lw(opcode); break;
		case I_LBU:     lbu(opcode); break;
		case I_LHU:     lhu(opcode); break;
		case I_LWR:     lwr(opcode); break;

		case I_SB:      sb(opcode); break;
		case I_SH:      sh(opcode); break;
		case I_SWL:     swl(opcode); break;
		case I_SW:      sw(opcode); break;
		case I_SWR:     swr(opcode); break;
		case I_CACHE:
			if (checkCoprocessor(0))
				cache(opcode);
			break;

		case I_LL:      ll(opcode); break;
		case I_LWC1:    invalid(1); break;
		case I_LWC2:    invalid(2); break;
		case I_PREF:    pref(opcode); break;
		case I_LDC1:    invalid(1); break;
		case I_LDC2:    invalid(2); break;

		case I_SC:      sc(opcode); break;
		case I_SWC1:    invalid(1); break;
		case I_SWC2:    invalid(2); break;
		case I_SDC1:    invalid(1); break;
		case I_SDC2:    invalid(2); break;
		default: reserved(); break;
		}
	}

	private void stepSpecial(int opcode) {
		switch(DECODE_FUNCT(opcode)) {
		case I_SPEC_SLL:     sll(opcode); break;
		case I_SPEC_COP1:    invalid(1); break;

		case I_SPEC_SRL:     srl(opcode); break;
		case I_SPEC_SRA:     sra(opcode); break;
		case I_SPEC_SLLV:    sllv(opcode); break;
		case I_SPEC_SRLV:    srlv(opcode); break;
		case I_SPEC_SRAV:    srav(opcode); break;

		case I_SPEC_JR:      jr(opcode); break;
		case I_SPEC_JALR:    jalr(opcode); break;
		case I_SPEC_MOVZ:    movz(opcode); break;
		case I_SPEC_MOVN:    movn(opcode); break;
		case I_SPEC_SYSCALL: syscall(opcode); break;
		case I_SPEC_BREAK:   break_(opcode); break;
		case I_SPEC_SYNC:    sync(opcode); break;

		case I_SPEC_MFHI:    mfhi(opcode); break;
		case I_SPEC_MTHI:    mthi(opcode); break;
		case I_SPEC_MFLO:    mflo(opcode); break;
		case I_SPEC_MTLO:    mtlo(opcode); break;

		case I_SPEC_MULT:    mult(opcode); break;
		case I_SPEC_MULTU:   multu(opcode); break;
		case I_SPEC_DIV:     div(opcode); break;
		case I_SPEC_DIVU:    divu(opcode); break;

		case I_SPEC_ADD:     add(opcode); break;
		case I_SPEC_ADDU:    addu(opcode); break;
		case I_SPEC_SUB:     sub(opcode); break;
		case I_SPEC_SUBU:    subu(opcode); break;
		case I_SPEC_AND:     and(opcode); break;
		case I_SPEC_OR:      or(opcode); break;
		case I_SPEC_XOR:     xor(opcode); break;
		case I_SPEC_NOR:     nor(opcode); break;

		case I_SPEC_SLT:     slt(opcode); break;
		case I_SPEC_SLTU:    sltu(opcode); break;

		case I_SPEC_TGE:     tge(opcode); break;
		case I_SPEC_TGEU:    tgeu(opcode); break;
		case I_SPEC_TLT:     tlt(opcode); break;
		case I_SPEC_TLTU:    tltu(opcode); break;
		case I_SPEC_TEQ:     teq(opcode); break;
		case I_SPEC_TNE:     tne(opcode); break;

		default: reserved(); break;
		}
	}

	private void stepSpecial2(int opcode) {
		switch(DECODE_FUNCT(opcode)) {
		case I_SPEC2_MADD:  madd(opcode); break;
		case I_SPEC2_MADDU: maddu(opcode); break;
		case I_SPEC2_MUL:   mul(opcode); break;
		case I_SPEC2_MSUB:  msub(opcode); break;
		case I_SPEC2_MSUBU: msubu(opcode); break;
		case I_SPEC2_CLZ:   clz(opcode); break;
		case I_SPEC2_CLO:   clo(opcode); break;
		case I_SPEC2_SDBBP: sdbbp(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepRegImm(int opcode) {
		switch(DECODE_RT(opcode)) {
		case I_REGIMM_BLTZ:    bltz(opcode); break;
		case I_REGIMM_BGEZ:    bgez(opcode); break;
		case I_REGIMM_BLTZL:   bltzl(opcode); break;
		case I_REGIMM_BGEZL:   bgezl(opcode); break;
		case I_REGIMM_TGEI:    tgei(opcode); break;
		case I_REGIMM_TGEIU:   tgeiu(opcode); break;
		case I_REGIMM_TLTI:    tlti(opcode); break;
		case I_REGIMM_TLTIU:   tltiu(opcode); break;
		case I_REGIMM_TEQI:    teqi(opcode); break;
		case I_REGIMM_TNEI:    tnei(opcode); break;
		case I_REGIMM_BLTZAL:  bltzal(opcode); break;
		case I_REGIMM_BGEZAL:  bgezal(opcode); break;
		case I_REGIMM_BLTZALL: bltzall(opcode); break;
		case I_REGIMM_BGEZALL: bgezall(opcode); break;
		default: reserved(); break;
		}
	}

	private void stepCop0(int opcode) {
		if (checkCoprocessor(0)) {
			int rs = DECODE_RS(opcode);
			switch(rs) {
			case I_COP0_MFC0: mfc0(opcode); break;
			case I_COP0_MTC0: mtc0(opcode); break;
			default:
				if (rs >= I_COP0_CO_MIN && rs <= I_COP0_CO_MAX)
					stepCop0Co(opcode);
				else
					reserved();
				break;
			}
		}
	}

	private void stepCop0Co(int opcode) {
		switch(DECODE_FUNCT(opcode)) {
		case I_COP0CO_TLBR:  tlbr(opcode); break;
		case I_COP0CO_TLBWI: tlbwi(opcode); break;
		case I_COP0CO_TLBWR: tlbwr(opcode); break;
		case I_COP0CO_TLBP:  tlbp(opcode); break;
		case I_COP0CO_ERET:  eret(opcode); break;
		case I_COP0CO_DERET: deret(opcode); break;
		case I_COP0CO_WAIT:  wait(opcode); break;
		default: reserved(); break;
		}
	}

	public void add(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs + rt;
		if (checkOverflow(rs, rt, result, true)) return;
		setGpr(DECODE_RD(opcode), result);
	}

	public void addi(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		int result = rs + imm;
		if (checkOverflow(rs, imm, result, true)) return;
		setGpr(DECODE_RT(opcode), result);
	}

	public void addiu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		int result = rs + imm;
		setGpr(DECODE_RT(opcode), result);
	}

	public void addu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs + rt;
		setGpr(DECODE_RD(opcode), result);
	}

	public void and(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs & rt;
		setGpr(DECODE_RD(opcode), result);
	}

	public void andi(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int immu = DECODE_IMM16U(opcode);
		int result = rs & immu;
		setGpr(DECODE_RT(opcode), result);
	}

	public void beq(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs == rt) {
			branch(opcode);
		}
	}

	public void beql(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs == rt) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bgez(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		}
	}

	public void bgezal(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		link();
		if (rs >= 0) {
			branch(opcode);
		}
	}

	public void bgezall(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		link();
		if (rs >= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bgezl(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bgtz(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs > 0) {
			branch(opcode);
		}
	}

	public void bgtzl(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs > 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void blez(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs <= 0) {
			branch(opcode);
		}
	}

	public void blezl(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs <= 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bltz(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		}
	}

	public void bltzal(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		link();
		if (rs < 0) {
			branch(opcode);
		}
	}

	public void bltzall(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		link();
		if (rs < 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bltzl(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void bne(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs != rt) {
			branch(opcode);
		}
	}

	public void bnel(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs != rt) {
			branch(opcode);
		} else {
			skipDelaySlot();
		}
	}

	public void break_(int opcode) {
		cop0.exceptionBreak(this);
	}

	public void cache(int opcode) {
		// No cache emulation is done
	}

	public void clo(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int result = Helper.countLeadingOnes(rs);
		setGpr(DECODE_RD(opcode), result);
	}

	public void clz(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int result = Helper.countLeadingZeros(rs);
		setGpr(DECODE_RD(opcode), result);
	}

	public void deret(int opcode) {
		//TODO
	}

	public void div(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rt == 0) {
			lo = hi = 0;
		} else {
			lo = rs / rt;
			hi = rs % rt;
		}
	}

	public void divu(int opcode) {
		long rs = gpr[DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[DECODE_RT(opcode)] & 0xFFFFFFFFL;
		if (rt == 0) {
			lo = hi = 0;
		} else {
			lo = (int) (rs / rt);
			hi = (int) (rs % rt);
		}
	}

	public void eret(int opcode) {
		cop0.returnFromException(this);
	}

	public void j(int opcode) {
		jump(DECODE_JUMP(opcode, exceptionPc));
	}

	public void jal(int opcode) {
		link();
		j(opcode);
	}

	public void jalr(int opcode) {
		link(DECODE_RD(opcode));
		jr(opcode);
	}

	public void jr(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		jump(rs);
	}

	public void lb(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(DECODE_RT(opcode), val);
		}
	}

	public void lbu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address) & 0xFF;
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(DECODE_RT(opcode), val);
		}
	}

	public void lh(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(DECODE_RT(opcode), val);
		}
	}

	public void lhu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address) & 0xFFFF;
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(DECODE_RT(opcode), val);
		}
	}

	public void ll(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read32linked(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(DECODE_RT(opcode), val);
		}
	}

	public void lui(int opcode) {
		int imm = DECODE_IMM16(opcode);
		int result = imm << 16;
		setGpr(DECODE_RT(opcode), result);
	}

	public void lw(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int val = read32(address);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(DECODE_RT(opcode), val);
		}
	}

	public void lwl(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = DECODE_RT(opcode);
		int val = read32UnalignedLeft(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, val);
		}
	}

	public void lwr(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = DECODE_RT(opcode);
		int val = read32UnalignedRight(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, val);
		}
	}

	public void madd(int opcode) {
		long rs = gpr[DECODE_RS(opcode)];
		long rt = gpr[DECODE_RT(opcode)];
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo + rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void maddu(int opcode) {
		long rs = gpr[DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[DECODE_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo + rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void mfc0(int opcode) {
		int rt = DECODE_RT(opcode);
		int rd = DECODE_RD(opcode);
		int sel = DECODE_COP0SEL(opcode);
		setGpr(rt, cop0.getCop0Reg(this, rd, sel));
	}

	public void mfhi(int opcode) {
		setGpr(DECODE_RD(opcode), hi);
	}

	public void mflo(int opcode) {
		setGpr(DECODE_RD(opcode), lo);
	}

	public void movn(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		if (rt != 0) {
			int rs = gpr[DECODE_RS(opcode)];
			setGpr(DECODE_RD(opcode), rs);
		}
	}

	public void movz(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		if (rt == 0) {
			int rs = gpr[DECODE_RS(opcode)];
			setGpr(DECODE_RD(opcode), rs);
		}
	}

	public void msub(int opcode) {
		long rs = gpr[DECODE_RS(opcode)];
		long rt = gpr[DECODE_RT(opcode)];
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo - rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void msubu(int opcode) {
		long rs = gpr[DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[DECODE_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) hi) << 32) | ((long) lo);
		long result = hilo - rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void mtc0(int opcode) {
		int rt = DECODE_RT(opcode);
		int rd = DECODE_RD(opcode);
		int sel = DECODE_COP0SEL(opcode);
		cop0.setCop0Reg(this, rd, sel, gpr[rt]);
	}

	public void mthi(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		hi = rs;
	}

	public void mtlo(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		lo = rs;
	}

	public void mul(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs * rt;
		setGpr(DECODE_RD(opcode), result);
		// hi = lo = 0; // Unpredictable
	}

	public void mult(int opcode) {
		long rs = gpr[DECODE_RS(opcode)];
		long rt = gpr[DECODE_RT(opcode)];
		long result = rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void multu(int opcode) {
		long rs = gpr[DECODE_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[DECODE_RT(opcode)] & 0xFFFFFFFFL;
		long result = rs * rt;
		lo = (int) result;
		hi = (int) (result >> 32);
	}

	public void nor(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = ~(rs | rt);
		setGpr(DECODE_RD(opcode), result);
	}

	public void or(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs | rt;
		setGpr(DECODE_RD(opcode), result);
	}

	public void ori(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int immu = DECODE_IMM16U(opcode);
		int result = rs | immu;
		setGpr(DECODE_RT(opcode), result);
	}

	public void pref(int opcode) {
		//TODO
	}

	public void sb(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[DECODE_RT(opcode)];
		write8(address, (byte) rt);
	}

	public void sc(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = DECODE_RT(opcode);
		boolean ok = write32conditional(address, gpr[rt]);
		if (memoryError == MEMORY_ERROR_NOERROR) {
			setGpr(rt, ok ? 1 : 0);
		}
	}

	public void sdbbp(int opcode) {
		//TODO
	}

	public void sh(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[DECODE_RT(opcode)];
		write16(address, (short) rt);
	}

	public void sll(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int sa = DECODE_SA(opcode);
		int result = rt << sa;
		setGpr(DECODE_RD(opcode), result);
	}

	public void sllv(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int rs = gpr[DECODE_RS(opcode)];
		int result = rt << rs;
		setGpr(DECODE_RD(opcode), result);
	}

	public void slt(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int rs = gpr[DECODE_RS(opcode)];
		int result = (rs < rt) ? 1 : 0;
		setGpr(DECODE_RD(opcode), result);
	}

	public void slti(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		int result = (rs < imm) ? 1 : 0;
		setGpr(DECODE_RT(opcode), result);
	}

	public void sltiu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		int result = (Helper.compareUnsigned(rs, imm) < 0) ? 1 : 0;
		setGpr(DECODE_RT(opcode), result);
	}

	public void sltu(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int rs = gpr[DECODE_RS(opcode)];
		int result = (Helper.compareUnsigned(rs, rt) < 0) ? 1 : 0;
		setGpr(DECODE_RD(opcode), result);
	}

	public void sra(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int sa = DECODE_SA(opcode);
		int result = rt >> sa;
		setGpr(DECODE_RD(opcode), result);
	}

	public void srav(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int rs = gpr[DECODE_RS(opcode)];
		int result = rt >> rs;
		setGpr(DECODE_RD(opcode), result);
	}

	public void srl(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int sa = DECODE_SA(opcode);
		int result = rt >>> sa;
		setGpr(DECODE_RD(opcode), result);
	}

	public void srlv(int opcode) {
		int rt = gpr[DECODE_RT(opcode)];
		int rs = gpr[DECODE_RS(opcode)];
		int result = rt >>> rs;
		setGpr(DECODE_RD(opcode), result);
	}

	public void sub(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs - rt;
		if (checkOverflow(rs, rt, result, false)) return;
		setGpr(DECODE_RD(opcode), result);
	}

	public void subu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs - rt;
		setGpr(DECODE_RD(opcode), result);
	}

	public void sw(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[DECODE_RT(opcode)];
		write32(address, rt);
	}

	public void swl(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[DECODE_RT(opcode)];
		write32UnalignedLeft(address, rt);
	}

	public void swr(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int offset = DECODE_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[DECODE_RT(opcode)];
		write32UnalignedRight(address, rt);
	}

	public void sync(int opcode) {
		// No cache emulation is done
	}

	public void syscall(int opcode) {
		cop0.exceptionSyscall(this);
	}

	public void teq(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs == rt) {
			cop0.exceptionTrap(this);
		}
	}

	public void teqi(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		if (rs == imm) {
			cop0.exceptionTrap(this);
		}
	}

	public void tge(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs >= rt) {
			cop0.exceptionTrap(this);
		}
	}

	public void tgei(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		if (rs >= imm) {
			cop0.exceptionTrap(this);
		}
	}

	public void tgeiu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		if (Helper.compareUnsigned(rs, imm) >= 0) {
			cop0.exceptionTrap(this);
		}
	}

	public void tgeu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (Helper.compareUnsigned(rs, rt) >= 0) {
			cop0.exceptionTrap(this);
		}
	}

	public void tlbp(int opcode) {
		cop0.tlbProbe();
	}

	public void tlbr(int opcode) {
		cop0.tlbRead();
	}

	public void tlbwi(int opcode) {
		cop0.tlbWriteIndex(this);
	}

	public void tlbwr(int opcode) {
		cop0.tlbWriteRandom(this);
	}

	public void tlt(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs < rt) {
			cop0.exceptionTrap(this);
		}
	}

	public void tlti(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		if (rs < imm) {
			cop0.exceptionTrap(this);
		}
	}

	public void tltiu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		if (Helper.compareUnsigned(rs, imm) < 0) {
			cop0.exceptionTrap(this);
		}
	}

	public void tltu(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (Helper.compareUnsigned(rs, rt) < 0) {
			cop0.exceptionTrap(this);
		}
	}

	public void tne(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		if (rs != rt) {
			cop0.exceptionTrap(this);
		}
	}

	public void tnei(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int imm = DECODE_IMM16(opcode);
		if (rs != imm) {
			cop0.exceptionTrap(this);
		}
	}

	public void wait(int opcode) {
		halted = true;
	}

	public void xor(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int rt = gpr[DECODE_RT(opcode)];
		int result = rs ^ rt;
		setGpr(DECODE_RD(opcode), result);
	}

	public void xori(int opcode) {
		int rs = gpr[DECODE_RS(opcode)];
		int immu = DECODE_IMM16U(opcode);
		int result = rs ^ immu;
		setGpr(DECODE_RT(opcode), result);
	}

	private void invalid(int copno) {
		if (checkCoprocessor(copno))
			cop0.exceptionReserved(this);
	}

	private void reserved() {
		cop0.exceptionReserved(this);
	}

	private boolean checkCoprocessor(int copno) {
		if (!cop0.isCoprocessorAvailable(copno)) {
			cop0.exceptionCoprocessorUnusable(this, copno);
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
		if (overflow) cop0.exceptionIntegerOverflow(this);
		return overflow;
	}

	private void branch(int opcode) {
		jump(DECODE_BRANCH(opcode, exceptionPc));
	}

	private void jump(int address) {
		nextPc = address;
	}

	private void link() {
		link(GPR_RA);
	}

	private void link(int regno) {
		setGpr(regno, exceptionPc + 8);
	}

	private void skipDelaySlot() {
		pc += 4;
		nextPc = pc + 4;
	}

	public int exceptionPc() {
		if (isBranchDelaySlot()) {
			return exceptionPc - 4;
		} else {
			return exceptionPc;
		}
	}

	public boolean checkTimerInterrupt() {
		if (((long) cop0.getCompareRegister()) * 2  == counter) {
			raiseIrq(TIMER_IRQ, true);
			cop0.checkInterrupts(this);
			return true;
		} else {
			return false;
		}
	}
}
