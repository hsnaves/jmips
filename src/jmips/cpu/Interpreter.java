package jmips.cpu;

public final class Interpreter {

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

	public static void step(Cpu cpu) {
		int opcode = cpu.read32(cpu.pc);

		cpu.delay_slot = false;
		cpu.pc = cpu.next_pc;
		cpu.next_pc = cpu.pc + 4;

		switch (I_OP(opcode)) {
		case 0: stepSpecial(cpu, opcode); break;
		case 1: stepRegImm(cpu, opcode); break;
		case 2: j(cpu, opcode); break;
		case 3: jal(cpu, opcode); break;
		case 4: beq(cpu, opcode); break;
		case 5: bne(cpu, opcode); break;
		case 6: blez(cpu, opcode); break;
		case 7: bgtz(cpu, opcode); break;

		case 8: addi(cpu, opcode); break;
		case 9: addiu(cpu, opcode); break;
		case 10: slti(cpu, opcode); break;
		case 11: sltiu(cpu, opcode); break;
		case 12: andi(cpu, opcode); break;
		case 13: ori(cpu, opcode); break;
		case 14: xori(cpu, opcode); break;
		case 15: lui(cpu, opcode); break;

		case 16: stepCop0(cpu, opcode); break;
		case 17: invalid(cpu); break;
		case 18: invalid(cpu); break;
		case 19: invalid(cpu); break;
		case 20: beql(cpu, opcode); break;
		case 21: bnel(cpu, opcode); break;
		case 22: blezl(cpu, opcode); break;
		case 23: bgtzl(cpu, opcode); break;

		case 24: reserved(cpu); break;
		case 25: reserved(cpu); break;
		case 26: reserved(cpu); break;
		case 27: reserved(cpu); break;
		case 28: stepSpecial2(cpu, opcode); break;
		case 29: reserved(cpu); break;
		case 30: reserved(cpu); break;
		case 31: reserved(cpu); break;

		case 32: lb(cpu, opcode); break;
		case 33: lh(cpu, opcode); break;
		case 34: lwl(cpu, opcode); break;
		case 35: lw(cpu, opcode); break;
		case 36: lbu(cpu, opcode); break;
		case 37: lhu(cpu, opcode); break;
		case 38: lwr(cpu, opcode); break;
		case 39: reserved(cpu); break;

		case 40: sb(cpu, opcode); break;
		case 41: sh(cpu, opcode); break;
		case 42: swl(cpu, opcode); break;
		case 43: sw(cpu, opcode); break;
		case 44: reserved(cpu); break;
		case 45: reserved(cpu); break;
		case 46: swr(cpu, opcode); break;
		case 47: cache(cpu, opcode); break;

		case 48: ll(cpu, opcode); break;
		case 49: invalid(cpu); break;
		case 50: invalid(cpu); break;
		case 51: pref(cpu, opcode); break;
		case 52: reserved(cpu); break;
		case 53: invalid(cpu); break;
		case 54: invalid(cpu); break;
		case 55: reserved(cpu); break;

		case 56: sc(cpu, opcode); break;
		case 57: invalid(cpu); break;
		case 58: invalid(cpu); break;
		case 59: reserved(cpu); break;
		case 60: reserved(cpu); break;
		case 61: invalid(cpu); break;
		case 62: invalid(cpu); break;
		case 63: reserved(cpu); break;
		}
	}

	public static void stepSpecial(Cpu cpu, int opcode) {
		switch(I_FUNCT(opcode)) {
		case 0: sll(cpu, opcode); break;
		case 1: invalid(cpu); break;
		case 2: srl(cpu, opcode); break;
		case 3: sra(cpu, opcode); break;
		case 4: sllv(cpu, opcode); break;
		case 6: srlv(cpu, opcode); break;
		case 7: srav(cpu, opcode); break;

		case 8: jr(cpu, opcode); break;
		case 9: jalr(cpu, opcode); break;
		case 10: movz(cpu, opcode); break;
		case 11: movn(cpu, opcode); break;
		case 12: syscall(cpu, opcode); break;
		case 13: break_(cpu, opcode); break;
		case 15: sync(cpu, opcode); break;

		case 16: mfhi(cpu, opcode); break;
		case 17: mthi(cpu, opcode); break;
		case 18: mflo(cpu, opcode); break;
		case 19: mtlo(cpu, opcode); break;

		case 24: mult(cpu, opcode); break;
		case 25: multu(cpu, opcode); break;
		case 26: div(cpu, opcode); break;
		case 27: divu(cpu, opcode); break;

		case 32: add(cpu, opcode); break;
		case 33: addu(cpu, opcode); break;
		case 34: sub(cpu, opcode); break;
		case 35: subu(cpu, opcode); break;
		case 36: and(cpu, opcode); break;
		case 37: or(cpu, opcode); break;
		case 38: xor(cpu, opcode); break;
		case 39: nor(cpu, opcode); break;

		case 42: slt(cpu, opcode); break;
		case 43: sltu(cpu, opcode); break;

		case 48: tge(cpu, opcode); break;
		case 49: tgeu(cpu, opcode); break;
		case 50: tlt(cpu, opcode); break;
		case 51: tltu(cpu, opcode); break;
		case 52: teq(cpu, opcode); break;
		case 54: tne(cpu, opcode); break;

		default: reserved(cpu); break;
		}
	}

	public static void stepSpecial2(Cpu cpu, int opcode) {
		switch(I_FUNCT(opcode)) {
		case 0: madd(cpu, opcode); break;
		case 1: maddu(cpu, opcode); break;
		case 2: mul(cpu, opcode); break;
		case 4: msub(cpu, opcode); break;
		case 5: msubu(cpu, opcode); break;
		case 32: clz(cpu, opcode); break;
		case 33: clo(cpu, opcode); break;
		case 63: sdbbp(cpu, opcode); break;
		default: reserved(cpu); break;
		}
	}

	public static void stepRegImm(Cpu cpu, int opcode) {
		switch(I_RT(opcode)) {
		case 0: bltz(cpu, opcode); break;
		case 1: bgez(cpu, opcode); break;
		case 2: bltzl(cpu, opcode); break;
		case 3: bgezl(cpu, opcode); break;
		case 8: tgei(cpu, opcode); break;
		case 9: tgeiu(cpu, opcode); break;
		case 10: tlti(cpu, opcode); break;
		case 11: tltiu(cpu, opcode); break;
		case 12: teqi(cpu, opcode); break;
		case 14: tnei(cpu, opcode); break;
		case 16: bltzal(cpu, opcode); break;
		case 17: bgezal(cpu, opcode); break;
		case 18: bltzall(cpu, opcode); break;
		case 19: bgezall(cpu, opcode); break;
		default: reserved(cpu); break;
		}
	}

	public static void stepCop0(Cpu cpu, int opcode) {
		switch(I_RS(opcode)) {
		case 0: mfc0(cpu, opcode); break;
		case 4: mtc0(cpu, opcode); break;
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
		case 31: stepCop0Co(cpu, opcode); break;
		default: reserved(cpu); break;
		}
	}

	public static void stepCop0Co(Cpu cpu, int opcode) {
		switch(I_FUNCT(opcode)) {
		case 1: tlbr(cpu, opcode); break;
		case 2: tlbwi(cpu, opcode); break;
		case 6: tlbwr(cpu, opcode); break;
		case 8: tlbp(cpu, opcode); break;
		case 24: eret(cpu, opcode); break;
		case 31: deret(cpu, opcode); break;
		case 32: wait(cpu, opcode); break;
		default: reserved(cpu); break;
		}
	}

	public static void add(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs + rt;
		if (check_overflow(cpu, rs, rt, result, true)) return;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void addi(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = rs + imm;
		if (check_overflow(cpu, rs, imm, result, true)) return;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void addiu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = rs + imm;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void addu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs + rt;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void and(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs & rt;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void andi(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs & immu;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void beq(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs == rt) {
			branch(cpu, opcode);
		}
	}

	public static void beql(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs == rt) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bgez(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs >= 0) {
			branch(cpu, opcode);
		}
	}

	public static void bgezal(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		link(cpu);
		if (rs >= 0) {
			branch(cpu, opcode);
		}
	}

	public static void bgezall(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		link(cpu);
		if (rs >= 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bgezl(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs >= 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bgtz(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs > 0) {
			branch(cpu, opcode);
		}
	}

	public static void bgtzl(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs > 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void blez(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs <= 0) {
			branch(cpu, opcode);
		}
	}

	public static void blezl(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs <= 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bltz(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs < 0) {
			branch(cpu, opcode);
		}
	}

	public static void bltzal(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		link(cpu);
		if (rs < 0) {
			branch(cpu, opcode);
		}
	}

	public static void bltzall(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		link(cpu);
		if (rs < 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bltzl(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		if (rs < 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bne(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs != rt) {
			branch(cpu, opcode);
		}
	}

	public static void bnel(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs != rt) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void break_(Cpu cpu, int opcode) {
		//TODO
	}

	public static void cache(Cpu cpu, int opcode) {
		// No cache emulation is done
	}

	public static void clo(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int result = Utils.countLeadingOnes(rs);
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void clz(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int result = Utils.countLeadingZeros(rs);
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void deret(Cpu cpu, int opcode) {
		//TODO
	}

	public static void div(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RS(opcode)];
		cpu.lo = rs / rt;
		cpu.hi = rs % rt;
	}

	public static void divu(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		cpu.lo = (int) (rs / rt);
		cpu.hi = (int) (rs % rt);
	}

	public static void eret(Cpu cpu, int opcode) {
		//TODO
	}

	public static void j(Cpu cpu, int opcode) {
		cpu.next_pc = I_JUMP(opcode, cpu.pc - 4);
		cpu.delay_slot = true;
	}

	public static void jal(Cpu cpu, int opcode) {
		link(cpu);
		j(cpu, opcode);
	}

	public static void jalr(Cpu cpu, int opcode) {
		link(cpu, I_RD(opcode));
		jr(cpu, opcode);
	}

	public static void jr(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		cpu.next_pc = rs;
		cpu.delay_slot = true;
	}

	public static void lb(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read8(address);
		if (cpu.memory_ok) {
			write_reg(cpu, I_RT(opcode), val);
		}
	}

	public static void lbu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read8(address) & 0xFF;
		if (cpu.memory_ok) {
			write_reg(cpu, I_RT(opcode), val);
		}
	}

	public static void lh(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read16(address);
		if (cpu.memory_ok) {
			write_reg(cpu, I_RT(opcode), val);
		}
	}

	public static void lhu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read16(address) & 0xFFFF;
		if (cpu.memory_ok) {
			write_reg(cpu, I_RT(opcode), val);
		}
	}

	public static void ll(Cpu cpu, int opcode) {
		//TODO
	}

	public static void lui(Cpu cpu, int opcode) {
		int imm = I_IMM16(opcode);
		int result = imm << 16;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void lw(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read32(address);
		if (cpu.memory_ok) {
			write_reg(cpu, I_RT(opcode), val);
		}
	}

	public static void lwl(Cpu cpu, int opcode) {
		//TODO
	}

	public static void lwr(Cpu cpu, int opcode) {
		//TODO
	}

	public static void madd(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)];
		long rt = cpu.gpr[I_RT(opcode)];
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo + rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void maddu(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo + rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void mfc0(Cpu cpu, int opcode) {
		//TODO
	}

	public static void mfhi(Cpu cpu, int opcode) {
		write_reg(cpu, I_RD(opcode), cpu.hi);
	}

	public static void mflo(Cpu cpu, int opcode) {
		write_reg(cpu, I_RD(opcode), cpu.lo);
	}

	public static void movn(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		if (rt != 0) {
			int rs = cpu.gpr[I_RS(opcode)];
			write_reg(cpu, I_RD(opcode), rs);
		}
	}

	public static void movz(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		if (rt == 0) {
			int rs = cpu.gpr[I_RS(opcode)];
			write_reg(cpu, I_RD(opcode), rs);
		}
	}

	public static void msub(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)];
		long rt = cpu.gpr[I_RT(opcode)];
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo - rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void msubu(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo - rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void mtc0(Cpu cpu, int opcode) {
		//TODO
	}

	public static void mthi(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		cpu.hi = rs;
	}

	public static void mtlo(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		cpu.lo = rs;
	}

	public static void mul(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs * rt;
		write_reg(cpu, I_RD(opcode), result);
		// cpu.hi = cpu.lo = 0; // Unpredictable
	}

	public static void mult(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)];
		long rt = cpu.gpr[I_RT(opcode)];
		long result = rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void multu(Cpu cpu, int opcode) {
		long rs = cpu.gpr[I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[I_RT(opcode)] & 0xFFFFFFFFL;
		long result = rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void nor(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = ~(rs | rt);
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void or(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs | rt;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void ori(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs | immu;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void pref(Cpu cpu, int opcode) {
		//TODO
	}

	public static void sb(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = cpu.gpr[I_RT(opcode)];
		cpu.write8(address, (byte) rt);
	}

	public static void sc(Cpu cpu, int opcode) {
		//TODO
	}

	public static void sdbbp(Cpu cpu, int opcode) {
		//TODO
	}

	public static void sh(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = cpu.gpr[I_RT(opcode)];
		cpu.write16(address, (short) rt);
	}

	public static void sll(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt << sa;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void sllv(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int rs = cpu.gpr[I_RS(opcode)];
		int result = rt << rs;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void slt(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int rs = cpu.gpr[I_RS(opcode)];
		int result = (rs < rt) ? 1 : 0;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void slti(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = (rs < imm) ? 1 : 0;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void sltiu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		int result = (Utils.compareUnsigned(rs, imm) < 0) ? 1 : 0;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void sltu(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int rs = cpu.gpr[I_RS(opcode)];
		int result = (Utils.compareUnsigned(rs, rt) < 0) ? 1 : 0;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void sra(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt >> sa;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void srav(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int rs = cpu.gpr[I_RS(opcode)];
		int result = rt >> rs;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void srl(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int sa = I_SA(opcode);
		int result = rt >>> sa;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void srlv(Cpu cpu, int opcode) {
		int rt = cpu.gpr[I_RT(opcode)];
		int rs = cpu.gpr[I_RS(opcode)];
		int result = rt >>> rs;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void sub(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs - rt;
		if (check_overflow(cpu, rs, rt, result, false)) return;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void subu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs - rt;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void sw(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int offset = I_IMM16(opcode);
		int address = rs + offset;
		int rt = cpu.gpr[I_RT(opcode)];
		cpu.write32(address, rt);
	}

	public static void swl(Cpu cpu, int opcode) {
		//TODO
	}

	public static void swr(Cpu cpu, int opcode) {
		//TODO
	}

	public static void sync(Cpu cpu, int opcode) {
		// No cache emulation is done
	}

	public static void syscall(Cpu cpu, int opcode) {
		//TODO
	}

	public static void teq(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs == rt) {
			trap(cpu);
		}
	}

	public static void teqi(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs == imm) {
			trap(cpu);
		}
	}

	public static void tge(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs >= rt) {
			trap(cpu);
		}
	}

	public static void tgei(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs >= imm) {
			trap(cpu);
		}
	}

	public static void tgeiu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (Utils.compareUnsigned(rs, imm) >= 0) {
			trap(cpu);
		}
	}

	public static void tgeu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (Utils.compareUnsigned(rs, rt) >= 0) {
			trap(cpu);
		}
	}

	public static void tlbp(Cpu cpu, int opcode) {
		//TODO
	}

	public static void tlbr(Cpu cpu, int opcode) {
		//TODO
	}

	public static void tlbwi(Cpu cpu, int opcode) {
		//TODO
	}

	public static void tlbwr(Cpu cpu, int opcode) {
		//TODO
	}

	public static void tlt(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs < rt) {
			trap(cpu);
		}
	}

	public static void tlti(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs < imm) {
			trap(cpu);
		}
	}

	public static void tltiu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (Utils.compareUnsigned(rs, imm) < 0) {
			trap(cpu);
		}
	}

	public static void tltu(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (Utils.compareUnsigned(rs, rt) < 0) {
			trap(cpu);
		}
	}

	public static void tne(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		if (rs != rt) {
			trap(cpu);
		}
	}

	public static void tnei(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int imm = I_IMM16(opcode);
		if (rs != imm) {
			trap(cpu);
		}
	}

	public static void wait(Cpu cpu, int opcode) {
		cpu.halted = true;
	}

	public static void xor(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int rt = cpu.gpr[I_RT(opcode)];
		int result = rs ^ rt;
		write_reg(cpu, I_RD(opcode), result);
	}

	public static void xori(Cpu cpu, int opcode) {
		int rs = cpu.gpr[I_RS(opcode)];
		int immu = I_IMM16U(opcode);
		int result = rs ^ immu;
		write_reg(cpu, I_RT(opcode), result);
	}

	public static void invalid(Cpu cpu) {
		//TODO
	}

	public static void reserved(Cpu cpu) {
		//TODO
	}


	private static boolean check_overflow(Cpu cpu, int a, int b, int result, boolean sum) {
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

	private static void write_reg(Cpu cpu, int regno, int val) {
		if (regno != 0) cpu.gpr[regno] = val;
	}

	private static void branch(Cpu cpu, int opcode) {
		cpu.next_pc = I_BRANCH(opcode, cpu.pc - 4);
		cpu.delay_slot = true;
	}

	private static void link(Cpu cpu) {
		link(cpu, 31);
	}

	private static void link(Cpu cpu, int regno) {
		write_reg(cpu, regno, cpu.pc + 4);
	}

	private static void skip_delay_slot(Cpu cpu) {
		cpu.pc += 4;
	}

	private static void trap(Cpu cpu) {
		// TODO
	}
}
