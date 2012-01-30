package jmips.cpu;

public final class Cpu {

	/* Register constants */
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

	/* Auxiliary functions to decode the opcode */
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

	/* Instance fields */
	private final int[] gpr = new int[32];
	private int hi, lo;
	private int pc, _nextPc, nextPc;
	private long counter;
	private boolean delaySlot, nextDelaySlot;

	boolean halted;
	private boolean success;

	private final Coprocessor0 cop0;
	private final MemoryManager memoryManager;

	public Cpu(int ramOffset, int ramSize) {
		this.cop0 = new Coprocessor0(this);
		this.memoryManager = new MemoryManager(ramOffset, ramSize);
	}

	public MemoryManager getMemoryManager() {
		return memoryManager;
	}

	public void hardReset() {
		cop0.hardReset();
		reset();
	}

	public void reset() {
		cop0.exception_RESET();
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

	public void setProgramCounter(int pc) {
		this.pc = pc;
		this._nextPc = pc;
		this.nextPc = pc + 4;
		this.delaySlot = false;
		this.nextDelaySlot = false;
		this.halted = false;
	}

	public int getProgramCounter() {
		return this.pc;
	}

	public int getNextProgramCounter() {
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

	public boolean isBigEndian() {
		return cop0.isBigEndian();
	}

	public byte read8(int address) {
		int physicalAddress = cop0.translate(address, false, true);
		if (cop0.translationError()) {
			success = false;
			return 0;
		}
		byte b = memoryManager.read8(physicalAddress);
		if (memoryManager.error()) {
			success = false;
			cop0.exception_BUS_ERROR(true);
			return 0;
		}
		success = true;
		return b;
	}

	public short read16(int address) {
		if ((address & 1) != 0) {
			cop0.exception_ADDRESS_ERROR(address, true);
			success = false;
			return 0;
		}

		int physicalAddress = cop0.translate(address, false, true);
		if (cop0.translationError()) {
			success = false;
			return 0;
		}
		short s = memoryManager.read16(physicalAddress, isBigEndian());
		if (memoryManager.error()) {
			success = false;
			cop0.exception_BUS_ERROR(true);
			return 0;
		}
		success = true;
		return s;
	}

	public int read32(int address) {
		return read32(address, false);
	}

	public int read32(int address, boolean linked) {
		if ((address & 3) != 0) {
			cop0.exception_ADDRESS_ERROR(address, true);
			success = false;
			return 0;
		}

		//if (linked) pc += 4;
		int physicalAddress = cop0.translate(address, false, true);
		//if (linked) pc -= 4;
		if (cop0.translationError()) {
			success = false;
			return 0;
		}
		int v = memoryManager.read32(physicalAddress, isBigEndian());
		if (memoryManager.error()) {
			success = false;
			cop0.exception_BUS_ERROR(true);
			return 0;
		}
		if (linked) cop0.loadLinked(physicalAddress);
		success = true;
		return v;
	}

	public void write8(int address, byte value) {
		int physicalAddress = cop0.translate(address, true, true);
		if (cop0.translationError()) {
			success = false;
			return;
		}
		memoryManager.write8(physicalAddress, value);
		success = !memoryManager.error();
		if (!success) cop0.exception_BUS_ERROR(true);
	}

	public void write16(int address, short value) {
		if ((address & 1) != 0) {
			cop0.exception_ADDRESS_ERROR(address, false);
			success = false;
			return;
		}

		int physicalAddress = cop0.translate(address, true, true);
		if (cop0.translationError()) {
			success = false;
			return;
		}
		memoryManager.write16(physicalAddress, value, isBigEndian());
		success = !memoryManager.error();
		if (!success) cop0.exception_BUS_ERROR(true);
	}

	public void write32(int address, int value) {
		write32(address, value, false);
	}

	public boolean write32(int address, int value, boolean conditional) {
		if ((address & 3) != 0) {
			cop0.exception_ADDRESS_ERROR(address, false);
			success = false;
			return false;
		}

		if (conditional && !cop0.canStoreConditional()) {
			success = true;
			return false;
		}

		int physicalAddress = cop0.translate(address, true, true);
		if (cop0.translationError()) {
			success = false;
			return false;
		}
		memoryManager.write32(physicalAddress, value, isBigEndian());
		success = !memoryManager.error();
		if (!success) cop0.exception_BUS_ERROR(true);
		return success;
	}

	public int read32UnalignedLeft(int address, int oldValue) {
		int alignedAddress = address & (~3);
		int value = read32(alignedAddress);
		if (success) {
			int shift = (isBigEndian()) ? (address & 3) : 3 - (address & 3);
			shift <<= 3;
			return (oldValue & ~((-1) << shift)) | (value << shift); 
		}
		return 0;
	}

	public int read32UnalignedRight(int address, int oldValue) {
		int alignedAddress = address & (~3);
		int value = read32(alignedAddress);
		if (success) {
			int shift = (!isBigEndian()) ? (address & 3) : 3 - (address & 3);
			shift <<= 3;
			return (oldValue & ~((-1) >>> shift)) | (value >>> shift); 
		}
		return 0;
	}

	public void write32UnalignedLeft(int address, int value) {
		int alignedAddress = address & (~3);
		int physicalAddress = cop0.translate(alignedAddress, true, true);
		if (cop0.translationError()) {
			success = false;
			return;
		}
		int oldValue = memoryManager.read32(physicalAddress, isBigEndian());
		success = !memoryManager.error();

		if (success) {
			int shift = (isBigEndian()) ? (address & 3) : 3 - (address & 3);
			shift <<= 3;
			int newValue = (oldValue & ~((-1) >>> shift)) | (value >>> shift);
			memoryManager.write32(physicalAddress, newValue, isBigEndian());
			success = !memoryManager.error();
		}
		if (!success) cop0.exception_BUS_ERROR(true);
	}

	public void write32UnalignedRight(int address, int value) {
		int alignedAddress = address & (~3);
		int physicalAddress = cop0.translate(alignedAddress, true, true);
		if (cop0.translationError()) {
			success = false;
			return;
		}
		int oldValue = memoryManager.read32(physicalAddress, isBigEndian());
		success = !memoryManager.error();

		if (success) {
			int shift = (!isBigEndian()) ? (address & 3) : 3 - (address & 3);
			shift <<= 3;
			int newValue = (oldValue & ~((-1) << shift)) | (value << shift); 
			memoryManager.write32(physicalAddress, newValue, isBigEndian());
			success = !memoryManager.error();
		}
		if (!success) cop0.exception_BUS_ERROR(true);
	}

	public boolean success() {
		return success;
	}

	public void raiseIrq(int irqno) {
		cop0.raiseIrq(irqno);
	}

	public void lowerIrq(int irqno) {
		cop0.lowerIrq(irqno);
	}

	public boolean checkInterrupts() {
		return cop0.checkInterrupts();
	}

	public int fetchOpcode() {
		if ((pc & 3) != 0) {
			cop0.exception_ADDRESS_ERROR(pc, true);
			success = false;
			return 0;
		}

		int physicalAddress = cop0.translate(pc, false, false);
		if (cop0.translationError()) {
			success = false;
			return 0;
		}
		int v = memoryManager.read32(physicalAddress, isBigEndian());
		if (memoryManager.error()) {
			success = false;
			cop0.exception_BUS_ERROR(false);
			return 0;
		}
		success = true;
		return v;
	}

	public String disassemble() {
		int opcode = fetchOpcode();
		if (success) return Disassemble.disassemble(pc, opcode);
		return null;
	}

	public void step() {
		step(1);
	}

	public void step(int num) {
		if (halted) return;
		checkInterrupts();
		int before = getCounter();
		while(num-- > 0 && !halted) {
			int opcode = fetchOpcode();
			if (!success) {
				opcode = fetchOpcode();
				if (!success) {
					halted = true;
					break;
				}
			}
			if (success) {
				nextDelaySlot = false;
				_nextPc = nextPc;
				nextPc = nextPc + 4;
				microstep(opcode);
				delaySlot = nextDelaySlot;
				pc = _nextPc;
			}
		}
		int after = getCounter();
		cop0.checkTimerInterrupt(before, after);
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
		cop0.exception_BREAK();
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
			cop0.returnFromException();
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
		if (success) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lbu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address) & 0xFF;
		if (success) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lh(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address);
		if (success) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lhu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address) & 0xFFFF;
		if (success) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void ll(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read32(address, true);
		if (success) {
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
		if (success) {
			setGpr(I_RT(opcode), val);
		}
	}

	private void lwl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = I_RT(opcode);
		int val = read32UnalignedLeft(address, gpr[rt]);
		if (success) {
			setGpr(rt, val);
		}
	}

	private void lwr(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = I_RT(opcode);
		int val = read32UnalignedRight(address, gpr[rt]);
		if (success) {
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
			setGpr(rt, cop0.moveFromCoprocessor(rd, sel));
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
			cop0.moveToCoprocessor(rd, sel, gpr[rt]);
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
		boolean ok = write32(address, gpr[rt], true);
		if (success) {
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
		cop0.exception_SYSCALL();
	}

	private void teq(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs == rt) {
			trap();
		}
	}

	private void teqi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs == imm) {
			trap();
		}
	}

	private void tge(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs >= rt) {
			trap();
		}
	}

	private void tgei(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs >= imm) {
			trap();
		}
	}

	private void tgeiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (Utils.compareUnsigned(rs, imm) >= 0) {
			trap();
		}
	}

	private void tgeu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (Utils.compareUnsigned(rs, rt) >= 0) {
			trap();
		}
	}

	private void tlbp(int opcode) {
		if (checkCoprocessor(0)) {
			cop0.tlbProbe();
		}
	}

	private void tlbr(int opcode) {
		if (checkCoprocessor(0)) {
			cop0.tlbRead();
		}
	}

	private void tlbwi(int opcode) {
		if (checkCoprocessor(0)) {
			cop0.tlbWriteIndex();
		}
	}

	private void tlbwr(int opcode) {
		if (checkCoprocessor(0)) {
			cop0.tlbWriteRandom();
		}
	}

	private void tlt(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs < rt) {
			trap();
		}
	}

	private void tlti(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs < imm) {
			trap();
		}
	}

	private void tltiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (Utils.compareUnsigned(rs, imm) < 0) {
			trap();
		}
	}

	private void tltu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (Utils.compareUnsigned(rs, rt) < 0) {
			trap();
		}
	}

	private void tne(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		if (rs != rt) {
			trap();
		}
	}

	private void tnei(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs != imm) {
			trap();
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
			cop0.exception_RESERVED();
	}

	private void reserved() {
		cop0.exception_RESERVED();
	}

	private boolean checkCoprocessor(int copno) {
		if (!cop0.isCoprocessorAvailable(copno)) {
			cop0.exception_COPROCESS_UNUSABLE(copno);
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
		if (overflow) cop0.exception_INTEGER_OVERFLOW();
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

	private void trap() {
		cop0.exception_TRAP();
	}
}
