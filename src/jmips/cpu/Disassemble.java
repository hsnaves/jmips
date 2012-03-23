package jmips.cpu;

public class Disassemble {

	public static final String[] REGISTER_NAMES = {
		"$zr", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
		"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
		"$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
		"$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
	};
	
	public static final String[] COP0_REGISTER_NAMES = {
		"Index", "Random", "EntryLo0", "EntryLo1", "Context", "PageMask", "Wired", "Resv1",
		"BadVAddr", "Count", "EntryHi", "Compare", "Status", "Cause", "EPC", "PRId",
		"Config", "LLAddr", "WatchLo", "WatchHi", "Resv2", "Resv3", "Resv4", "Debug",
		"DEPC", "Resv5", "ErrCtl", "Resv6", "TagLo", "Resv7", "ErrorEPC", "DESAVE"
	};

	private static void printInsn(StringBuilder sb, String insn) {
		sb.append(insn);
		for (int i = insn.length(); i < 16; i++)
			sb.append(" ");
	}


	private static void disassembleSyscallAndBreak(StringBuilder sb, int opcode) {
		int code = Cpu.I_SYSCALLCODE(opcode);
		if (code != 0) sb.append(String.format("0x%X", code));
	}

	private static void disassembleSync(StringBuilder sb, int opcode) {
		int code = Cpu.I_SA(opcode);
		if (code != 0) sb.append(String.format("0x%X", code));
	}

	private static void disassembleWait(StringBuilder sb, int opcode) {
		int code = Cpu.I_WAITCODE(opcode);
		if (code != 0) sb.append(String.format("0x%X", code));
	}

	private static void disassembleJump(StringBuilder sb, int pc, int opcode) {
		sb.append(String.format("0x%08X", Cpu.I_JUMP(opcode, pc)));
	}

	private static void disassembleLoadStore(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(Cpu.I_IMM16(opcode));
		sb.append("(").append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(")");
	}

	private static void disassembleBranch(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		sb.append(String.format("0x%08X", Cpu.I_BRANCH(opcode, pc)));
	}

	private static void disassembleBranch2(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(String.format("0x%08X", Cpu.I_BRANCH(opcode, pc)));
	}

	private static void disassembleBranch3(StringBuilder sb, int pc, int opcode) {
		sb.append(String.format("0x%08X", Cpu.I_BRANCH(opcode, pc)));
	}

	private static void disassemble3Registers(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]);
	}

	private static void disassemble2Registers(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]);
	}

	private static void disassemble2Registers2(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]);
	}

	private static void disassemble2Registers3(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]);
	}

	private static void disassemble2RegistersImmediate(StringBuilder sb, int opcode, boolean signed) {
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		if (signed) {
			sb.append(Cpu.I_IMM16(opcode));
		} else {
			sb.append(String.format("0x%04X", Cpu.I_IMM16U(opcode)));
		}
	}

	private static void disassembleLi(StringBuilder sb, int opcode, boolean signed) {
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		if (signed) {
			sb.append(Cpu.I_IMM16(opcode));
		} else {
			sb.append(String.format("0x%04X", Cpu.I_IMM16U(opcode)));
		}
	}

	private static void disassembleJumpRegisterAndLink(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]);
	}

	private static void disassembleJumpRegister(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]);
	}

	private static void disassembleShift(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]);
	}

	private static void disassembleShiftImmediate(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(Cpu.I_SA(opcode));
	}

	private static void disassembleMoveFrom(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RD(opcode)]);
	}

	private static void disassembleMoveTo(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]);
	}

	private static void disassembleMoveFromToCop0(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		int sel = Cpu.I_COP0SEL(opcode);
		int rd = Cpu.I_RD(opcode);
		if (sel == 1 && (rd == Coprocessor0.COP0_REG_CONFIG || rd == Coprocessor0.COP0_REG_TAGLO)) {
			if (rd == Coprocessor0.COP0_REG_CONFIG)
				sb.append("Config1");
			else
				sb.append("DataLo");
		} else {
			sb.append(COP0_REGISTER_NAMES[rd]);
		}
	}

	private static void disassembleTrap(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(String.format("0x%4X", Cpu.I_TRAPCODE(opcode)));
	}

	private static void disassembleTrapImmediate(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(", ");
		sb.append(Cpu.I_IMM16(opcode));
	}

	private static void disassembleLui(StringBuilder sb, int opcode) {
		sb.append(REGISTER_NAMES[Cpu.I_RT(opcode)]).append(", ");
		sb.append(String.format("0x%04X", Cpu.I_IMM16U(opcode)));
	}

	private static void disassembleCache(StringBuilder sb, int opcode) {
		// TODO
		sb.append(Cpu.I_RT(opcode)).append(", ");
		sb.append(Cpu.I_IMM16(opcode));
		sb.append("(").append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(")");
	}

	private static void disassemblePref(StringBuilder sb, int opcode) {
		// TODO
		sb.append(Cpu.I_RT(opcode)).append(", ");
		sb.append(Cpu.I_IMM16(opcode));
		sb.append("(").append(REGISTER_NAMES[Cpu.I_RS(opcode)]).append(")");
	}

	private static void disassembleReserved(StringBuilder sb) {
		sb.append("(reserved)");
	}

	private static void disassembleCoprocessorUnusable(StringBuilder sb) {
		sb.append("(coprocessor unusuable)");
	}


	private static void disassembleSpecial(StringBuilder sb, int pc, int opcode) {
		switch(Cpu.I_FUNCT(opcode)) {
		case 0:
			if (opcode == 0) printInsn(sb, "nop");
			else if (opcode == 0x00000040) printInsn(sb, "ssnop");
			else {
				printInsn(sb, "sll");
				disassembleShiftImmediate(sb, opcode);
			}
			break;
		case 1:
			disassembleCoprocessorUnusable(sb);
			break;
		case 2:
			printInsn(sb, "srl");
			disassembleShiftImmediate(sb, opcode);
			break;
		case 3:
			printInsn(sb, "sra");
			disassembleShiftImmediate(sb, opcode);
			break;
		case 4:
			printInsn(sb, "sllv");
			disassembleShift(sb, opcode);
			break;
		case 6:
			printInsn(sb, "srlv");
			disassembleShift(sb, opcode);
			break;
		case 7:
			printInsn(sb, "srav");
			disassembleShift(sb, opcode);
			break;

		case 8:
			printInsn(sb, "jr");
			disassembleJumpRegister(sb, opcode);
			break;
		case 9:
			printInsn(sb, "jalr");
			disassembleJumpRegisterAndLink(sb, opcode);
			break;
		case 10:
			printInsn(sb, "movz");
			disassemble3Registers(sb, opcode);
			break;
		case 11:
			printInsn(sb, "movn");
			disassemble3Registers(sb, opcode);
			break;
		case 12:
			printInsn(sb, "syscall");
			disassembleSyscallAndBreak(sb, opcode);
			break;
		case 13:
			printInsn(sb, "break");
			disassembleSyscallAndBreak(sb, opcode);
			break;
		case 15:
			printInsn(sb, "sync");
			disassembleSync(sb, opcode);
			break;

		case 16:
			printInsn(sb, "mfhi");
			disassembleMoveFrom(sb, opcode);
			break;
		case 17:
			printInsn(sb, "mthi");
			disassembleMoveTo(sb, opcode);
			break;
		case 18:
			printInsn(sb, "mflo");
			disassembleMoveFrom(sb, opcode);
			break;
		case 19:
			printInsn(sb, "mtlo");
			disassembleMoveTo(sb, opcode);
			break;

		case 24:
			printInsn(sb, "mult");
			disassemble2Registers(sb, opcode);
			break;
		case 25:
			printInsn(sb, "multu");
			disassemble2Registers(sb, opcode);
			break;
		case 26:
			printInsn(sb, "div");
			disassemble2Registers(sb, opcode);
			break;
		case 27:
			printInsn(sb, "divu");
			disassemble2Registers(sb, opcode);
			break;

		case 32:
			printInsn(sb, "add");
			disassemble3Registers(sb, opcode);
			break;
		case 33:
			if ((opcode & 0xFC1F07FF) == 0x00000021) {
				printInsn(sb, "move");
				disassemble2Registers2(sb, opcode);
			} else {
				printInsn(sb, "addu");
				disassemble3Registers(sb, opcode);
			}
			break;
		case 34:
			if ((opcode & 0xFFE007FF) == 0x00000022) {
				printInsn(sb, "neg");
				disassemble2Registers3(sb, opcode);
			} else {
				printInsn(sb, "sub");
				disassemble3Registers(sb, opcode);
			}
			break;
		case 35:
			if ((opcode & 0xFFE007FF) == 0x00000023) {
				printInsn(sb, "negu");
				disassemble2Registers3(sb, opcode);
			} else {
				printInsn(sb, "subu");
				disassemble3Registers(sb, opcode);
			}
			break;
		case 36:
			printInsn(sb, "and");
			disassemble3Registers(sb, opcode);
			break;
		case 37:
			if ((opcode & 0xFC1F07FF) == 0x00000025) {
				printInsn(sb, "move");
				disassemble2Registers2(sb, opcode);
			} else {
				printInsn(sb, "or");
				disassemble3Registers(sb, opcode);
			}
			break;
		case 38:
			printInsn(sb, "xor");
			disassemble3Registers(sb, opcode);
			break;
		case 39:
			if ((opcode & 0xFC1F07FF) == 0x00000027) {
				printInsn(sb, "not");
				disassemble2Registers2(sb, opcode);
			} else {
				printInsn(sb, "nor");
				disassemble3Registers(sb, opcode);
			}
			break;

		case 42:
			printInsn(sb, "slt");
			disassemble3Registers(sb, opcode);
			break;
		case 43:
			printInsn(sb, "sltu");
			disassemble3Registers(sb, opcode);
			break;

		case 48:
			printInsn(sb, "tge");
			disassembleTrap(sb, opcode);
			break;
		case 49:
			printInsn(sb, "tgeu");
			disassembleTrap(sb, opcode);
			break;
		case 50:
			printInsn(sb, "tlt");
			disassembleTrap(sb, opcode);
			break;
		case 51:
			printInsn(sb, "tltu");
			disassembleTrap(sb, opcode);
			break;
		case 52:
			printInsn(sb, "teq");
			disassembleTrap(sb, opcode);
			break;
		case 54:
			printInsn(sb, "tne");
			disassembleTrap(sb, opcode);
			break;

		default:
			disassembleReserved(sb);
			break;
		}
	}

	private static void disassembleSpecial2(StringBuilder sb, int pc, int opcode) {
		switch(Cpu.I_FUNCT(opcode)) {
		case 0:
			printInsn(sb, "madd");
			disassemble2Registers(sb, opcode);
			break;
		case 1:
			printInsn(sb, "maddu");
			disassemble2Registers(sb, opcode);
			break;
		case 2:
			printInsn(sb, "mul");
			disassemble3Registers(sb, opcode);
			break;
		case 4:
			printInsn(sb, "msub");
			disassemble2Registers(sb, opcode);
			break;
		case 5:
			printInsn(sb, "msubu");
			disassemble2Registers(sb, opcode);
			break;
		case 32:
			printInsn(sb, "clz");
			disassemble2Registers2(sb, opcode);
			break;
		case 33:
			printInsn(sb, "clo");
			disassemble2Registers2(sb, opcode);
			break;
		case 63:
			printInsn(sb, "sdbbp");
			disassembleSyscallAndBreak(sb, opcode);
			break;
		default:
			disassembleReserved(sb);
			break;
		}
	}

	private static void disassembleRegImm(StringBuilder sb, int pc, int opcode) {
		switch(Cpu.I_RT(opcode)) {
		case 0:
			printInsn(sb, "bltz");
			disassembleBranch(sb, pc, opcode);
			break;
		case 1:
			if ((opcode & 0xFFFF0000) == 0x04010000) {
				printInsn(sb, "b");
				disassembleBranch3(sb, pc, opcode);
			} else {
				printInsn(sb, "bgez");
				disassembleBranch(sb, pc, opcode);
			}
			break;
		case 2:
			printInsn(sb, "bltzl");
			disassembleBranch(sb, pc, opcode);
			break;
		case 3:
			printInsn(sb, "bgezl");
			disassembleBranch(sb, pc, opcode);
			break;
		case 8:
			printInsn(sb, "tgei");
			disassembleTrapImmediate(sb, opcode);
			break;
		case 9:
			printInsn(sb, "tgeiu");
			disassembleTrapImmediate(sb, opcode);
			break;
		case 10:
			printInsn(sb, "tlti");
			disassembleTrapImmediate(sb, opcode);
			break;
		case 11:
			printInsn(sb, "tltiu");
			disassembleTrapImmediate(sb, opcode);
			break;
		case 12:
			printInsn(sb, "teqi");
			disassembleTrapImmediate(sb, opcode);
			break;
		case 14:
			printInsn(sb, "tnei");
			disassembleTrapImmediate(sb, opcode);
			break;
		case 16:
			printInsn(sb, "bltzal");
			disassembleBranch(sb, pc, opcode);
			break;
		case 17:
			if ((opcode & 0xFFFF0000) == 0x04110000) {
				printInsn(sb, "bal");
				disassembleBranch3(sb, pc, opcode);
			} else {
				printInsn(sb, "bgezal");
				disassembleBranch(sb, pc, opcode);
			}
			break;
		case 18:
			printInsn(sb, "bltzall");
			disassembleBranch(sb, pc, opcode);
			break;
		case 19:
			printInsn(sb, "bgezall");
			disassembleBranch(sb, pc, opcode);
			break;
		default:
			disassembleReserved(sb);
			break;
		}
	}

	private static void disassembleCop0Co(StringBuilder sb, int pc, int opcode) {
		switch(Cpu.I_FUNCT(opcode)) {
		case 1:
			printInsn(sb, "tlbr");
			break;
		case 2:
			printInsn(sb, "tlbwi");
			break;
		case 6:
			printInsn(sb, "tlbwr");
			break;
		case 8:
			printInsn(sb, "tlbp");
			break;
		case 24:
			printInsn(sb, "eret");
			break;
		case 31:
			printInsn(sb, "deret");
			break;
		case 32:
			printInsn(sb, "wait");
			disassembleWait(sb, opcode);
			break;
		default:
			disassembleReserved(sb);
			break;
		}
	}

	private static void disassembleCop0(StringBuilder sb, int pc, int opcode) {
		switch(Cpu.I_RS(opcode)) {
		case 0:
			printInsn(sb, "mfc0");
			disassembleMoveFromToCop0(sb, opcode);
			break;
		case 4:
			printInsn(sb, "mtc0");
			disassembleMoveFromToCop0(sb, opcode);
			break;
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
		case 31:
			disassembleCop0Co(sb, pc, opcode);
			break;
		default:
			disassembleReserved(sb);
			break;
		}
	}


	public static String disassemble(int pc, int opcode) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("0x%08X: 0x%08X ", pc, opcode));
		switch (Cpu.I_OP(opcode)) {
		case 0:
			disassembleSpecial(sb, pc, opcode);
			break;
		case 1:
			disassembleRegImm(sb, pc, opcode);
			break;
		case 2:
			printInsn(sb, "j");
			disassembleJump(sb, pc, opcode);
			break;
		case 3:
			printInsn(sb, "jal");
			disassembleJump(sb, pc, opcode);
			break;
		case 4:
			if ((opcode & 0xFFFF0000) == 0x10000000) {
				printInsn(sb, "b");
				disassembleBranch3(sb, pc, opcode);
			} if ((opcode & 0xFC1F0000) == 0x10000000) {
				printInsn(sb, "beqz");
				disassembleBranch(sb, pc, opcode);
			} else {
				printInsn(sb, "beq");
				disassembleBranch2(sb, pc, opcode);
			}
			break;
		case 5:
			if ((opcode & 0xFC1F0000) == 0x14000000) {
				printInsn(sb, "bnez");
				disassembleBranch(sb, pc, opcode);
			} else {
				printInsn(sb, "bne");
				disassembleBranch2(sb, pc, opcode);
			}
			break;
		case 6:
			printInsn(sb, "blez");
			disassembleBranch(sb, pc, opcode);
			break;
		case 7:
			printInsn(sb, "bgtz");
			disassembleBranch(sb, pc, opcode);
			break;

		case 8:
			printInsn(sb, "addi");
			disassemble2RegistersImmediate(sb, opcode, true);
			break;
		case 9:
			if ((opcode & 0xFFE00000) == 0x24000000) {
				printInsn(sb, "li");
				disassembleLi(sb, opcode, true);
			} else {
				printInsn(sb, "addiu");
				disassemble2RegistersImmediate(sb, opcode, true);
			}
			break;
		case 10:
			printInsn(sb, "slti");
			disassemble2RegistersImmediate(sb, opcode, true);
			break;
		case 11:
			printInsn(sb, "sltiu");
			disassemble2RegistersImmediate(sb, opcode, true);
			break;
		case 12:
			printInsn(sb, "andi");
			disassemble2RegistersImmediate(sb, opcode, false);
			break;
		case 13:
			if ((opcode & 0xFFE00000) == 0x34000000) {
				printInsn(sb, "li");
				disassembleLi(sb, opcode, false);
			} else {
				printInsn(sb, "ori");
				disassemble2RegistersImmediate(sb, opcode, false);
			}
			break;
		case 14:
			printInsn(sb, "xori");
			disassemble2RegistersImmediate(sb, opcode, false);
			break;
		case 15:
			printInsn(sb, "lui");
			disassembleLui(sb, opcode);
			break;

		case 16:
			disassembleCop0(sb, pc, opcode);
			break;
		case 17:
			disassembleCoprocessorUnusable(sb);
			break;
		case 18:
			disassembleCoprocessorUnusable(sb);
			break;
		case 19:
			disassembleCoprocessorUnusable(sb);
			break;
		case 20:
			if ((opcode & 0xFC1F0000) == 0x50000000) {
				printInsn(sb, "beqzl");
				disassembleBranch(sb, pc, opcode);
			} else {
				printInsn(sb, "beql");
				disassembleBranch2(sb, pc, opcode);
			}
			break;
		case 21:
			if ((opcode & 0xFC1F0000) == 0x54000000) {
				printInsn(sb, "bnezl");
				disassembleBranch(sb, pc, opcode);
			} else {
				printInsn(sb, "bnel");
				disassembleBranch2(sb, pc, opcode);
			}
			break;
		case 22:
			printInsn(sb, "blezl");
			disassembleBranch(sb, pc, opcode);
			break;
		case 23:
			printInsn(sb, "bgtzl");
			disassembleBranch(sb, pc, opcode);
			break;

		case 24:
			disassembleReserved(sb);
			break;
		case 25:
			disassembleReserved(sb);
			break;
		case 26:
			disassembleReserved(sb);
			break;
		case 27:
			disassembleReserved(sb);
			break;
		case 28:
			disassembleSpecial2(sb, pc, opcode);
			break;
		case 29:
			disassembleReserved(sb);
			break;
		case 30:
			disassembleReserved(sb);
			break;
		case 31:
			disassembleReserved(sb);
			break;

		case 32:
			printInsn(sb, "lb");
			disassembleLoadStore(sb, opcode);
			break;
		case 33:
			printInsn(sb, "lh");
			disassembleLoadStore(sb, opcode);
			break;
		case 34:
			printInsn(sb, "lwl");
			disassembleLoadStore(sb, opcode);
			break;
		case 35:
			printInsn(sb, "lw");
			disassembleLoadStore(sb, opcode);
			break;
		case 36:
			printInsn(sb, "lbu");
			disassembleLoadStore(sb, opcode);
			break;
		case 37:
			printInsn(sb, "lhu");
			disassembleLoadStore(sb, opcode);
			break;
		case 38:
			printInsn(sb, "lwr");
			disassembleLoadStore(sb, opcode);
			break;
		case 39:
			disassembleReserved(sb);
			break;

		case 40:
			printInsn(sb, "sb");
			disassembleLoadStore(sb, opcode);
			break;
		case 41:
			printInsn(sb, "sh");
			disassembleLoadStore(sb, opcode);
			break;
		case 42:
			printInsn(sb, "swl");
			disassembleLoadStore(sb, opcode);
			break;
		case 43:
			printInsn(sb, "sw");
			disassembleLoadStore(sb, opcode);
			break;
		case 44:
			disassembleReserved(sb);
			break;
		case 45:
			disassembleReserved(sb);
			break;
		case 46:
			printInsn(sb, "swr");
			disassembleLoadStore(sb, opcode);
			break;
		case 47:
			printInsn(sb, "cache");
			disassembleCache(sb, opcode);
			break;

		case 48:
			printInsn(sb, "ll");
			disassembleLoadStore(sb, opcode);
			break;
		case 49:
			disassembleCoprocessorUnusable(sb);
			break;
		case 50:
			disassembleCoprocessorUnusable(sb);
			break;
		case 51:
			printInsn(sb, "pref");
			disassemblePref(sb, opcode);
			break;
		case 52:
			disassembleReserved(sb);
			break;
		case 53:
			disassembleCoprocessorUnusable(sb);
			break;
		case 54:
			disassembleCoprocessorUnusable(sb);
			break;
		case 55:
			disassembleReserved(sb);
			break;

		case 56:
			printInsn(sb, "sc");
			disassembleLoadStore(sb, opcode);
			break;
		case 57:
			disassembleCoprocessorUnusable(sb);
			break;
		case 58:
			disassembleCoprocessorUnusable(sb);
			break;
		case 59:
			disassembleReserved(sb);
			break;
		case 60:
			disassembleReserved(sb);
			break;
		case 61:
			disassembleCoprocessorUnusable(sb);
			break;
		case 62:
			disassembleCoprocessorUnusable(sb);
			break;
		case 63:
			disassembleReserved(sb);
			break;
		}
		return sb.toString();
	}
}
