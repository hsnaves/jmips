package jmips.cpu;

public final class Interpreter {

	public static void step(CpuState cpu) {
		int opcode = cpu.read32(cpu.pc);

		cpu.pc = cpu.next_pc;
		cpu.next_pc = cpu.pc + 4;

		switch (Utils.I_OP(opcode)) {
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

	public static void stepSpecial(CpuState cpu, int opcode) {
		switch(Utils.I_FUNCT(opcode)) {
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

	public static void stepSpecial2(CpuState cpu, int opcode) {
		switch(Utils.I_FUNCT(opcode)) {
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

	public static void stepRegImm(CpuState cpu, int opcode) {
		switch(Utils.I_RT(opcode)) {
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

	public static void stepCop0(CpuState cpu, int opcode) {
		switch(Utils.I_RS(opcode)) {
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

	public static void stepCop0Co(CpuState cpu, int opcode) {
		switch(Utils.I_FUNCT(opcode)) {
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

	public static void add(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs + rt;
		if (check_overflow(cpu, rs, rt, result, true)) return;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void addi(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		int result = rs + imm;
		if (check_overflow(cpu, rs, imm, result, true)) return;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void addiu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		int result = rs + imm;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void addu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs + rt;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void and(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs & rt;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void andi(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int immu = Utils.I_IMM16U(opcode);
		int result = rs & immu;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void beq(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs == rt) {
			branch(cpu, opcode);
		}
	}

	public static void beql(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs == rt) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bgez(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs >= 0) {
			branch(cpu, opcode);
		}
	}

	public static void bgezal(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		link(cpu);
		if (rs >= 0) {
			branch(cpu, opcode);
		}
	}

	public static void bgezall(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		link(cpu);
		if (rs >= 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bgezl(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs >= 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bgtz(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs > 0) {
			branch(cpu, opcode);
		}
	}

	public static void bgtzl(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs > 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void blez(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs <= 0) {
			branch(cpu, opcode);
		}
	}

	public static void blezl(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs <= 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bltz(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs < 0) {
			branch(cpu, opcode);
		}
	}

	public static void bltzal(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		link(cpu);
		if (rs < 0) {
			branch(cpu, opcode);
		}
	}

	public static void bltzall(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		link(cpu);
		if (rs < 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bltzl(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		if (rs < 0) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void bne(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs != rt) {
			branch(cpu, opcode);
		}
	}

	public static void bnel(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs != rt) {
			branch(cpu, opcode);
		} else {
			skip_delay_slot(cpu);
		}
	}

	public static void break_(CpuState cpu, int opcode) {
		//TODO
	}

	public static void cache(CpuState cpu, int opcode) {
		//TODO
	}

	public static void clo(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = Utils.count_leading_ones(rs);
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void clz(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = Utils.count_leading_zeros(rs);
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void deret(CpuState cpu, int opcode) {
		//TODO
	}

	public static void div(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RS(opcode)];
		cpu.lo = rs / rt;
		cpu.hi = rs % rt;
	}

	public static void divu(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[Utils.I_RS(opcode)] & 0xFFFFFFFFL;
		cpu.lo = (int) (rs / rt);
		cpu.hi = (int) (rs % rt);
	}

	public static void eret(CpuState cpu, int opcode) {
		//TODO
	}

	public static void j(CpuState cpu, int opcode) {
		cpu.next_pc = Utils.I_JUMP(opcode, cpu.pc - 4);
	}

	public static void jal(CpuState cpu, int opcode) {
		link(cpu);
		j(cpu, opcode);
	}

	public static void jalr(CpuState cpu, int opcode) {
		link(cpu, Utils.I_RD(opcode));
		jr(cpu, opcode);
	}

	public static void jr(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		cpu.next_pc = rs;
	}

	public static void lb(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read8(address);
		if (cpu.memory_ok) {
			write_reg(cpu, Utils.I_RT(opcode), val);
		}
	}

	public static void lbu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read8(address) & 0xFF;
		if (cpu.memory_ok) {
			write_reg(cpu, Utils.I_RT(opcode), val);
		}
	}

	public static void lh(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read16(address);
		if (cpu.memory_ok) {
			write_reg(cpu, Utils.I_RT(opcode), val);
		}
	}

	public static void lhu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read16(address) & 0xFFFF;
		if (cpu.memory_ok) {
			write_reg(cpu, Utils.I_RT(opcode), val);
		}
	}

	public static void ll(CpuState cpu, int opcode) {
		//TODO
	}

	public static void lui(CpuState cpu, int opcode) {
		int imm = Utils.I_IMM16(opcode);
		int result = imm << 16;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void lw(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int val = cpu.read32(address);
		if (cpu.memory_ok) {
			write_reg(cpu, Utils.I_RT(opcode), val);
		}
	}

	public static void lwl(CpuState cpu, int opcode) {
		//TODO
	}

	public static void lwr(CpuState cpu, int opcode) {
		//TODO
	}

	public static void madd(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)];
		long rt = cpu.gpr[Utils.I_RT(opcode)];
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo + rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void maddu(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[Utils.I_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo + rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void mfc0(CpuState cpu, int opcode) {
		//TODO
	}

	public static void mfhi(CpuState cpu, int opcode) {
		write_reg(cpu, Utils.I_RD(opcode), cpu.hi);
	}

	public static void mflo(CpuState cpu, int opcode) {
		write_reg(cpu, Utils.I_RD(opcode), cpu.lo);
	}

	public static void movn(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rt != 0) {
			int rs = cpu.gpr[Utils.I_RS(opcode)];
			write_reg(cpu, Utils.I_RD(opcode), rs);
		}
	}

	public static void movz(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rt == 0) {
			int rs = cpu.gpr[Utils.I_RS(opcode)];
			write_reg(cpu, Utils.I_RD(opcode), rs);
		}
	}

	public static void msub(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)];
		long rt = cpu.gpr[Utils.I_RT(opcode)];
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo - rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void msubu(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[Utils.I_RT(opcode)] & 0xFFFFFFFFL;
		long hilo = (((long) cpu.hi) << 32) | ((long) cpu.lo);
		long result = hilo - rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void mtc0(CpuState cpu, int opcode) {
		//TODO
	}

	public static void mthi(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		cpu.hi = rs;
	}

	public static void mtlo(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		cpu.lo = rs;
	}

	public static void mul(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs * rt;
		write_reg(cpu, Utils.I_RD(opcode), result);
		// cpu.hi = cpu.lo = 0; // Unpredictable
	}

	public static void mult(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)];
		long rt = cpu.gpr[Utils.I_RT(opcode)];
		long result = rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void multu(CpuState cpu, int opcode) {
		long rs = cpu.gpr[Utils.I_RS(opcode)] & 0xFFFFFFFFL;
		long rt = cpu.gpr[Utils.I_RT(opcode)] & 0xFFFFFFFFL;
		long result = rs * rt;
		cpu.lo = (int) result;
		cpu.hi = (int) (result >> 32);
	}

	public static void nor(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = ~(rs | rt);
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void or(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs | rt;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void ori(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int immu = Utils.I_IMM16U(opcode);
		int result = rs | immu;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void pref(CpuState cpu, int opcode) {
		//TODO
	}

	public static void sb(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		cpu.write8(address, (byte) rt);
	}

	public static void sc(CpuState cpu, int opcode) {
		//TODO
	}

	public static void sdbbp(CpuState cpu, int opcode) {
		//TODO
	}

	public static void sh(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		cpu.write16(address, (short) rt);
	}

	public static void sll(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int sa = Utils.I_SA(opcode);
		int result = rt << sa;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void sllv(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = rt << rs;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void slt(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = (rs < rt) ? 1 : 0;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void slti(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		int result = (rs < imm) ? 1 : 0;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void sltiu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		int result = (Utils.compare_unsigned(rs, imm) < 0) ? 1 : 0;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void sltu(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = (Utils.compare_unsigned(rs, rt) < 0) ? 1 : 0;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void sra(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int sa = Utils.I_SA(opcode);
		int result = rt >> sa;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void srav(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = rt >> rs;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void srl(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int sa = Utils.I_SA(opcode);
		int result = rt >>> sa;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void srlv(CpuState cpu, int opcode) {
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int result = rt >>> rs;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void sub(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs - rt;
		if (check_overflow(cpu, rs, rt, result, false)) return;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void subu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs - rt;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void sw(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int offset = Utils.I_IMM16(opcode);
		int address = rs + offset;
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		cpu.write32(address, rt);
	}

	public static void swl(CpuState cpu, int opcode) {
		//TODO
	}

	public static void swr(CpuState cpu, int opcode) {
		//TODO
	}

	public static void sync(CpuState cpu, int opcode) {
		//TODO
	}

	public static void syscall(CpuState cpu, int opcode) {
		//TODO
	}

	public static void teq(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs == rt) {
			trap(cpu);
		}
	}

	public static void teqi(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		if (rs == imm) {
			trap(cpu);
		}
	}

	public static void tge(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs >= rt) {
			trap(cpu);
		}
	}

	public static void tgei(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		if (rs >= imm) {
			trap(cpu);
		}
	}

	public static void tgeiu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		if (Utils.compare_unsigned(rs, imm) >= 0) {
			trap(cpu);
		}
	}

	public static void tgeu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (Utils.compare_unsigned(rs, rt) >= 0) {
			trap(cpu);
		}
	}

	public static void tlbp(CpuState cpu, int opcode) {
		//TODO
	}

	public static void tlbr(CpuState cpu, int opcode) {
		//TODO
	}

	public static void tlbwi(CpuState cpu, int opcode) {
		//TODO
	}

	public static void tlbwr(CpuState cpu, int opcode) {
		//TODO
	}

	public static void tlt(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs < rt) {
			trap(cpu);
		}
	}

	public static void tlti(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		if (rs < imm) {
			trap(cpu);
		}
	}

	public static void tltiu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		if (Utils.compare_unsigned(rs, imm) < 0) {
			trap(cpu);
		}
	}

	public static void tltu(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (Utils.compare_unsigned(rs, rt) < 0) {
			trap(cpu);
		}
	}

	public static void tne(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		if (rs != rt) {
			trap(cpu);
		}
	}

	public static void tnei(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int imm = Utils.I_IMM16(opcode);
		if (rs != imm) {
			trap(cpu);
		}
	}

	public static void wait(CpuState cpu, int opcode) {
		//TODO
	}

	public static void xor(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int rt = cpu.gpr[Utils.I_RT(opcode)];
		int result = rs ^ rt;
		write_reg(cpu, Utils.I_RD(opcode), result);
	}

	public static void xori(CpuState cpu, int opcode) {
		int rs = cpu.gpr[Utils.I_RS(opcode)];
		int immu = Utils.I_IMM16U(opcode);
		int result = rs ^ immu;
		write_reg(cpu, Utils.I_RT(opcode), result);
	}

	public static void invalid(CpuState cpu) {
		//TODO
	}

	public static void reserved(CpuState cpu) {
		//TODO
	}


	private static boolean check_overflow(CpuState cpu, int a, int b, int result, boolean sum) {
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

	private static void write_reg(CpuState cpu, int regno, int val) {
		if (regno != 0) cpu.gpr[regno] = val;
	}

	private static void branch(CpuState cpu, int opcode) {
		cpu.next_pc = Utils.I_BRANCH(opcode, cpu.pc - 4);
	}

	private static void link(CpuState cpu) {
		link(cpu, 31);
	}

	private static void link(CpuState cpu, int regno) {
		write_reg(cpu, regno, cpu.pc + 4);
	}

	private static void skip_delay_slot(CpuState cpu) {
		cpu.next_pc += 4;
	}

	private static void trap(CpuState cpu) {
		// TODO
	}
}
