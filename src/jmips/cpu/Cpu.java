package jmips.cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

	public final int[] gpr = new int[32];
	public int hi, lo;
	public int pc, next_pc;
	public boolean delay_slot, next_delay_slot;

	private final Ram ram;
	private final int ram_base;

	public boolean memory_ok;
	public boolean halted;

	public final Coprocessor0 cop0;

	private final List<IODevice> devices = new ArrayList<IODevice>();
	private final List<Integer> deviceOffsets = new ArrayList<Integer>();
	private final List<Integer> deviceSizes = new ArrayList<Integer>();

	public Cpu(int ram_base, int ram_size) {
		this.ram_base = ram_base;
		this.ram = new Ram(ram_size);
		this.cop0 = new Coprocessor0(this);
	}

	public void registerDevice(IODevice device, int offset) {
		devices.add(device);
		deviceOffsets.add(offset);
		deviceSizes.add(device.size());
	}

	private int findDevice(int address) {
		for(int i = 0; i < deviceOffsets.size(); i++) {
			int offset = deviceOffsets.get(i);
			int size = deviceSizes.get(i);
			if ((address >= offset) && (address < offset + size)) {
				return i;
			}
		}
		return -1;
	}

	private boolean isMainMemory(int address) {
		return ((address >= ram_base) && (address < ram_base + ram.size()));
	}

	public byte read8(int address) {
		if (isMainMemory(address)) {
			memory_ok = true;
			return ram.read8(address - ram_base);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				byte b = dev.read8(address - offset);
				memory_ok = !dev.bus_error();
				return b;
			} else {
				memory_ok = false;
				return 0;
			}
		}
	}

	public short read16(int address) {
		if (isMainMemory(address)) {
			memory_ok = true;
			return ram.read16(address - ram_base, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				short s = dev.read16(address - offset, true);
				memory_ok = !dev.bus_error();
				return s;
			} else {
				memory_ok = false;
				return 0;
			}
		}
	}

	public int read32(int address) {
		if (isMainMemory(address)) {
			memory_ok = true;
			return ram.read32(address - ram_base, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				int v = dev.read32(address - offset, true);
				memory_ok = !dev.bus_error();
				return v;
			} else {
				memory_ok = false;
				return 0;
			}
		}
	}

	public void write8(int address, byte value) {
		if (isMainMemory(address)) {
			memory_ok = true;
			ram.write8(address - ram_base, value);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write8(address - offset, value);
				memory_ok = !dev.bus_error();
			} else {
				memory_ok = false;
			}
		}
	}

	public void write16(int address, short value) {
		if (isMainMemory(address)) {
			memory_ok = true;
			ram.write16(address - ram_base, value, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write16(address - offset, value, true);
				memory_ok = !dev.bus_error();
			} else {
				memory_ok = false;
			}
		}
	}

	public void write32(int address, int value) {
		if (isMainMemory(address)) {
			memory_ok = true;
			ram.write32(address - ram_base, value, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write32(address - offset, value, true);
				memory_ok = !dev.bus_error();
			} else {
				memory_ok = false;
			}
		}
	}

	public void reset() {
		Arrays.fill(gpr, 0);
		hi = lo = 0;
		memory_ok = true;
		pc = 0x08000000;
		next_pc = pc + 4;
	}

	public void raiseIrq(int irqno) {
	}

	public void lowerIrq(int irqno) {
	}


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

	public void step() {
		int opcode = read32(pc);

		delay_slot = false;
		pc = next_pc;
		next_pc = pc + 4;
		step(opcode);
	}

	private void step(int opcode) {
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
		case 17: invalid(); break;
		case 18: invalid(); break;
		case 19: invalid(); break;
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
		case 49: invalid(); break;
		case 50: invalid(); break;
		case 51: pref(opcode); break;
		case 52: reserved(); break;
		case 53: invalid(); break;
		case 54: invalid(); break;
		case 55: reserved(); break;

		case 56: sc(opcode); break;
		case 57: invalid(); break;
		case 58: invalid(); break;
		case 59: reserved(); break;
		case 60: reserved(); break;
		case 61: invalid(); break;
		case 62: invalid(); break;
		case 63: reserved(); break;
		}
	}

	private void stepSpecial(int opcode) {
		switch(I_FUNCT(opcode)) {
		case 0: sll(opcode); break;
		case 1: invalid(); break;
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
		if (check_overflow(rs, rt, result, true)) return;
		write_reg(I_RD(opcode), result);
	}

	private void addi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = rs + imm;
		if (check_overflow(rs, imm, result, true)) return;
		write_reg(I_RT(opcode), result);
	}

	private void addiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = rs + imm;
		write_reg(I_RT(opcode), result);
	}

	private void addu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs + rt;
		write_reg(I_RD(opcode), result);
	}

	private void and(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs & rt;
		write_reg(I_RD(opcode), result);
	}

	private void andi(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs & immu;
		write_reg(I_RT(opcode), result);
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
			skip_delay_slot();
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
			skip_delay_slot();
		}
	}

	private void bgezl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs >= 0) {
			branch(opcode);
		} else {
			skip_delay_slot();
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
			skip_delay_slot();
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
			skip_delay_slot();
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
			skip_delay_slot();
		}
	}

	private void bltzl(int opcode) {
		int rs = gpr[I_RS(opcode)];
		if (rs < 0) {
			branch(opcode);
		} else {
			skip_delay_slot();
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
			skip_delay_slot();
		}
	}

	private void break_(int opcode) {
		//TODO
	}

	private void cache(int opcode) {
		// No cache emulation is done
	}

	private void clo(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int result = Utils.countLeadingOnes(rs);
		write_reg(I_RD(opcode), result);
	}

	private void clz(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int result = Utils.countLeadingZeros(rs);
		write_reg(I_RD(opcode), result);
	}

	private void deret(int opcode) {
		//TODO
	}

	private void div(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RS(opcode)];
		lo = rs / rt;
		hi = rs % rt;
	}

	private void divu(int opcode) {
		long rs = gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		lo = (int) (rs / rt);
		hi = (int) (rs % rt);
	}

	private void eret(int opcode) {
		//TODO
	}

	private void j(int opcode) {
		next_pc = I_JUMP(opcode, pc - 4);
		delay_slot = true;
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
		next_pc = rs;
		delay_slot = true;
	}

	private void lb(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address);
		if (memory_ok) {
			write_reg(I_RT(opcode), val);
		}
	}

	private void lbu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read8(address) & 0xFF;
		if (memory_ok) {
			write_reg(I_RT(opcode), val);
		}
	}

	private void lh(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address);
		if (memory_ok) {
			write_reg(I_RT(opcode), val);
		}
	}

	private void lhu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read16(address) & 0xFFFF;
		if (memory_ok) {
			write_reg(I_RT(opcode), val);
		}
	}

	private void ll(int opcode) {
		//TODO
	}

	private void lui(int opcode) {
		int imm = I_IMM16(opcode);
		int result = imm << 16;
		write_reg(I_RT(opcode), result);
	}

	private void lw(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = read32(address);
		if (memory_ok) {
			write_reg(I_RT(opcode), val);
		}
	}

	private void lwl(int opcode) {
		//TODO
	}

	private void lwr(int opcode) {
		//TODO
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
		//TODO
	}

	private void mfhi(int opcode) {
		write_reg(I_RD(opcode), hi);
	}

	private void mflo(int opcode) {
		write_reg(I_RD(opcode), lo);
	}

	private void movn(int opcode) {
		int rt = gpr[I_RT(opcode)];
		if (rt != 0) {
			int rs = gpr[I_RS(opcode)];
			write_reg(I_RD(opcode), rs);
		}
	}

	private void movz(int opcode) {
		int rt = gpr[I_RT(opcode)];
		if (rt == 0) {
			int rs = gpr[I_RS(opcode)];
			write_reg(I_RD(opcode), rs);
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
		//TODO
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
		write_reg(I_RD(opcode), result);
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
		write_reg(I_RD(opcode), result);
	}

	private void or(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs | rt;
		write_reg(I_RD(opcode), result);
	}

	private void ori(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs | immu;
		write_reg(I_RT(opcode), result);
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
		//TODO
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
		write_reg(I_RD(opcode), result);
	}

	private void sllv(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = rt << rs;
		write_reg(I_RD(opcode), result);
	}

	private void slt(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = (rs < rt) ? 1 : 0;
		write_reg(I_RD(opcode), result);
	}

	private void slti(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = (rs < imm) ? 1 : 0;
		write_reg(I_RT(opcode), result);
	}

	private void sltiu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = (Utils.compareUnsigned(rs, imm) < 0) ? 1 : 0;
		write_reg(I_RT(opcode), result);
	}

	private void sltu(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = (Utils.compareUnsigned(rs, rt) < 0) ? 1 : 0;
		write_reg(I_RD(opcode), result);
	}

	private void sra(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt >> sa;
		write_reg(I_RD(opcode), result);
	}

	private void srav(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = rt >> rs;
		write_reg(I_RD(opcode), result);
	}

	private void srl(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt >>> sa;
		write_reg(I_RD(opcode), result);
	}

	private void srlv(int opcode) {
		int rt = gpr[I_RT(opcode)];
		int rs = gpr[I_RS(opcode)];
		int result = rt >>> rs;
		write_reg(I_RD(opcode), result);
	}

	private void sub(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs - rt;
		if (check_overflow(rs, rt, result, false)) return;
		write_reg(I_RD(opcode), result);
	}

	private void subu(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs - rt;
		write_reg(I_RD(opcode), result);
	}

	private void sw(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = gpr[I_RT(opcode)];
		write32(address, rt);
	}

	private void swl(int opcode) {
		//TODO
	}

	private void swr(int opcode) {
		//TODO
	}

	private void sync(int opcode) {
		// No cache emulation is done
	}

	private void syscall(int opcode) {
		//TODO
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
		//TODO
	}

	private void tlbr(int opcode) {
		//TODO
	}

	private void tlbwi(int opcode) {
		//TODO
	}

	private void tlbwr(int opcode) {
		//TODO
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
		halted = true;
	}

	private void xor(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int rt = gpr[I_RT(opcode)];
		int result = rs ^ rt;
		write_reg(I_RD(opcode), result);
	}

	private void xori(int opcode) {
		int rs = gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs ^ immu;
		write_reg(I_RT(opcode), result);
	}

	private void invalid() {
		//TODO
	}

	private void reserved() {
		//TODO
	}


	private boolean check_overflow(int a, int b, int result, boolean sum) {
		boolean overflow = false;
		if (sum) {
			overflow = ((a < 0) && (b < 0) && (result > 0)) ||
			           ((a > 0) && (b > 0) && (result < 0));
		} else {
			overflow = ((a <= 0) && (b > 0) && (result > 0)) ||
			           ((a >= 0) && (b < 0) && (result < 0));
		}
		if (overflow) {
			// TODO
		}
		return overflow;
	}

	private void write_reg(int regno, int val) {
		if (regno != GPR_ZR) gpr[regno] = val;
	}

	private void branch(int opcode) {
		next_pc = I_BRANCH(opcode, pc - 4);
		delay_slot = true;
	}

	private void link() {
		link(GPR_RA);
	}

	private void link(int regno) {
		write_reg(regno, pc + 4);
	}

	private void skip_delay_slot() {
		pc += 4;
	}

	private void trap() {
		// TODO
	}
}
