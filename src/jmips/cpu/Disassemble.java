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


	private static void disassembleSyscallAndBreak(StringBuilder sb, int pc, int opcode) {
		int code = Interpreter.I_SYSCALLCODE(opcode);
		if (code != 0) sb.append(String.format("0x%X", code));
	}

	private static void disassembleSync(StringBuilder sb, int pc, int opcode) {
		int code = Interpreter.I_SA(opcode);
		if (code != 0) sb.append(String.format("0x%X", code));
	}

	private static void disassembleWait(StringBuilder sb, int pc, int opcode) {
		int code = Interpreter.I_WAITCODE(opcode);
		if (code != 0) sb.append(String.format("0x%X", code));
	}

	private static void disassembleJump(StringBuilder sb, int pc, int opcode) {
		sb.append(String.format("0x%08X", Interpreter.I_JUMP(opcode, pc)));
	}

	private static void disassembleLoadStore(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(Interpreter.I_IMM16(opcode));
		sb.append("(").append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(")");
	}

	private static void disassembleBranch(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		sb.append(String.format("0x%08X", Interpreter.I_BRANCH(opcode, pc)));
	}

	private static void disassembleBranch2(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(String.format("0x%08X", Interpreter.I_BRANCH(opcode, pc)));
	}

	private static void disassemble3Registers(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]);
	}

	private static void disassemble2Registers(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]);
	}

	private static void disassemble2Registers2(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]);
	}

	private static void disassemble2Registers3(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]);
	}

	private static void disassemble2RegistersImmediate(StringBuilder sb, int pc, int opcode, boolean signed) {
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		if (signed) {
			sb.append(Interpreter.I_IMM16(opcode));
		} else {
			sb.append(String.format("0x%04X", Interpreter.I_IMM16U(opcode)));
		}
	}

	private static void disassembleLi(StringBuilder sb, int pc, int opcode, boolean signed) {
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		if (signed) {
			sb.append(Interpreter.I_IMM16(opcode));
		} else {
			sb.append(String.format("0x%04X", Interpreter.I_IMM16U(opcode)));
		}
	}

	private static void disassembleJumpRegisterAndLink(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]);
	}

	private static void disassembleJumpRegister(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]);
	}

	private static void disassembleShift(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]);
	}

	private static void disassembleShiftImmediate(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(Interpreter.I_SA(opcode));
	}

	private static void disassembleMoveFrom(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RD(opcode)]);
	}

	private static void disassembleMoveTo(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]);
	}

	private static void disassembleMoveFromToCop0(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(COP0_REGISTER_NAMES[Interpreter.I_RD(opcode)]);
	}

	private static void disassembleTrap(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(String.format("0x%4X", Interpreter.I_TRAPCODE(opcode)));
	}

	private static void disassembleTrapImmediate(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RS(opcode)]).append(", ");
		sb.append(Interpreter.I_IMM16(opcode));
	}

	private static void disassembleLui(StringBuilder sb, int pc, int opcode) {
		sb.append(REGISTER_NAMES[Interpreter.I_RT(opcode)]).append(", ");
		sb.append(String.format("0x%04X", Interpreter.I_IMM16U(opcode)));
	}

	private static void disassembleCache(StringBuilder sb, int pc, int opcode) {
		// TODO
	}

	private static void disassemblePref(StringBuilder sb, int pc, int opcode) {
		// TODO
	}

	private static void disassembleInvalid(StringBuilder sb, int pc, int opcode, boolean alpha) {
		if (alpha) {
			sb.append("(reserved)");
		} else {
			sb.append("(invalid)");
		}
	}

	private static void disassembleSpecial(StringBuilder sb, int pc, int opcode) {
		switch(Interpreter.I_FUNCT(opcode)) {
		case 0:
			if (opcode == 0) printInsn(sb, "nop");
			else {
				printInsn(sb, "sll");
				disassembleShiftImmediate(sb, pc, opcode);
			}
			break;
		case 1:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 2:
			printInsn(sb, "srl");
			disassembleShiftImmediate(sb, pc, opcode);
			break;
		case 3:
			printInsn(sb, "sra");
			disassembleShiftImmediate(sb, pc, opcode);
			break;
		case 4:
			printInsn(sb, "sllv");
			disassembleShift(sb, pc, opcode);
			break;
		case 6:
			printInsn(sb, "srlv");
			disassembleShift(sb, pc, opcode);
			break;
		case 7:
			printInsn(sb, "srav");
			disassembleShift(sb, pc, opcode);
			break;

		case 8:
			printInsn(sb, "jr");
			disassembleJumpRegister(sb, pc, opcode);
			break;
		case 9:
			printInsn(sb, "jalr");
			disassembleJumpRegisterAndLink(sb, pc, opcode);
			break;
		case 10:
			printInsn(sb, "movz");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 11:
			printInsn(sb, "movn");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 12:
			printInsn(sb, "syscall");
			disassembleSyscallAndBreak(sb, pc, opcode);
			break;
		case 13:
			printInsn(sb, "break");
			disassembleSyscallAndBreak(sb, pc, opcode);
			break;
		case 15:
			printInsn(sb, "sync");
			disassembleSync(sb, pc, opcode);
			break;

		case 16:
			printInsn(sb, "mfhi");
			disassembleMoveFrom(sb, pc, opcode);
			break;
		case 17:
			printInsn(sb, "mthi");
			disassembleMoveTo(sb, pc, opcode);
			break;
		case 18:
			printInsn(sb, "mflo");
			disassembleMoveFrom(sb, pc, opcode);
			break;
		case 19:
			printInsn(sb, "mtlo");
			disassembleMoveTo(sb, pc, opcode);
			break;

		case 24:
			printInsn(sb, "mult");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 25:
			printInsn(sb, "multu");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 26:
			printInsn(sb, "div");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 27:
			printInsn(sb, "divu");
			disassemble2Registers(sb, pc, opcode);
			break;

		case 32:
			printInsn(sb, "add");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 33:
			if ((opcode & 0xFC1F07FF) == 0x00000021) {
				printInsn(sb, "move");
				disassemble2Registers2(sb, pc, opcode);
			} else {
				printInsn(sb, "addu");
				disassemble3Registers(sb, pc, opcode);
			}
			break;
		case 34:
			if ((opcode & 0xFFE007FF) == 0x00000022) {
				printInsn(sb, "neg");
				disassemble2Registers3(sb, pc, opcode);
			} else {
				printInsn(sb, "sub");
				disassemble3Registers(sb, pc, opcode);
			}
			break;
		case 35:
			if ((opcode & 0xFFE007FF) == 0x00000023) {
				printInsn(sb, "negu");
				disassemble2Registers3(sb, pc, opcode);
			} else {
				printInsn(sb, "subu");
				disassemble3Registers(sb, pc, opcode);
			}
			break;
		case 36:
			printInsn(sb, "and");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 37:
			if ((opcode & 0xFC1F07FF) == 0x00000025) {
				printInsn(sb, "move");
				disassemble2Registers2(sb, pc, opcode);
			} else {
				printInsn(sb, "or");
				disassemble3Registers(sb, pc, opcode);
			}
			break;
		case 38:
			printInsn(sb, "xor");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 39:
			if ((opcode & 0xFC1F07FF) == 0x00000027) {
				printInsn(sb, "not");
				disassemble2Registers2(sb, pc, opcode);
			} else {
				printInsn(sb, "nor");
				disassemble3Registers(sb, pc, opcode);
			}
			break;

		case 42:
			printInsn(sb, "slt");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 43:
			printInsn(sb, "sltu");
			disassemble3Registers(sb, pc, opcode);
			break;

		case 48:
			printInsn(sb, "tge");
			disassembleTrap(sb, pc, opcode);
			break;
		case 49:
			printInsn(sb, "tgeu");
			disassembleTrap(sb, pc, opcode);
			break;
		case 50:
			printInsn(sb, "tlt");
			disassembleTrap(sb, pc, opcode);
			break;
		case 51:
			printInsn(sb, "tltu");
			disassembleTrap(sb, pc, opcode);
			break;
		case 52:
			printInsn(sb, "teq");
			disassembleTrap(sb, pc, opcode);
			break;
		case 54:
			printInsn(sb, "tne");
			disassembleTrap(sb, pc, opcode);
			break;

		default:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		}
	}

	private static void disassembleSpecial2(StringBuilder sb, int pc, int opcode) {
		switch(Interpreter.I_FUNCT(opcode)) {
		case 0:
			printInsn(sb, "madd");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 1:
			printInsn(sb, "maddu");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 2:
			printInsn(sb, "mul");
			disassemble3Registers(sb, pc, opcode);
			break;
		case 4:
			printInsn(sb, "msub");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 5:
			printInsn(sb, "msubu");
			disassemble2Registers(sb, pc, opcode);
			break;
		case 32:
			printInsn(sb, "clz");
			disassemble2Registers2(sb, pc, opcode);
			break;
		case 33:
			printInsn(sb, "clo");
			disassemble2Registers2(sb, pc, opcode);
			break;
		case 63:
			printInsn(sb, "sdbbp");
			disassembleSyscallAndBreak(sb, pc, opcode);
			break;
		default:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		}
	}

	private static void disassembleRegImm(StringBuilder sb, int pc, int opcode) {
		switch(Interpreter.I_RT(opcode)) {
		case 0:
			printInsn(sb, "bltz");
			disassembleBranch(sb, pc, opcode);
			break;
		case 1:
			printInsn(sb, "bgez");
			disassembleBranch(sb, pc, opcode);
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
			disassembleTrapImmediate(sb, pc, opcode);
			break;
		case 9:
			printInsn(sb, "tgeiu");
			disassembleTrapImmediate(sb, pc, opcode);
			break;
		case 10:
			printInsn(sb, "tlti");
			disassembleTrapImmediate(sb, pc, opcode);
			break;
		case 11:
			printInsn(sb, "tltiu");
			disassembleTrapImmediate(sb, pc, opcode);
			break;
		case 12:
			printInsn(sb, "teqi");
			disassembleTrapImmediate(sb, pc, opcode);
			break;
		case 14:
			printInsn(sb, "tnei");
			disassembleTrapImmediate(sb, pc, opcode);
			break;
		case 16:
			printInsn(sb, "bltzal");
			disassembleBranch(sb, pc, opcode);
			break;
		case 17:
			printInsn(sb, "bgezal");
			disassembleBranch(sb, pc, opcode);
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
			disassembleInvalid(sb, pc, opcode, true);
			break;
		}
	}

	private static void disassembleCop0Co(StringBuilder sb, int pc, int opcode) {
		switch(Interpreter.I_FUNCT(opcode)) {
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
			disassembleWait(sb, pc, opcode);
			break;
		default:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		}
	}

	private static void disassembleCop0(StringBuilder sb, int pc, int opcode) {
		switch(Interpreter.I_RS(opcode)) {
		case 0:
			printInsn(sb, "mfc0");
			disassembleMoveFromToCop0(sb, pc, opcode);
			break;
		case 4:
			printInsn(sb, "mtc0");
			disassembleMoveFromToCop0(sb, pc, opcode);
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
			disassembleInvalid(sb, pc, opcode, true);
			break;
		}
	}


	public static String disassemble(int pc, int opcode) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("0x%08X: ", pc));
		switch (Interpreter.I_OP(opcode)) {
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
			printInsn(sb, "beq");
			disassembleBranch2(sb, pc, opcode);
			break;
		case 5:
			printInsn(sb, "bne");
			disassembleBranch2(sb, pc, opcode);
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
			disassemble2RegistersImmediate(sb, pc, opcode, true);
			break;
		case 9:
			if ((opcode & 0xFFE00000) == 0x24000000) {
				printInsn(sb, "li");
				disassembleLi(sb, pc, opcode, true);
			} else {
				printInsn(sb, "addiu");
				disassemble2RegistersImmediate(sb, pc, opcode, true);
			}
			break;
		case 10:
			printInsn(sb, "slti");
			disassemble2RegistersImmediate(sb, pc, opcode, true);
			break;
		case 11:
			printInsn(sb, "sltiu");
			disassemble2RegistersImmediate(sb, pc, opcode, true);
			break;
		case 12:
			printInsn(sb, "andi");
			disassemble2RegistersImmediate(sb, pc, opcode, false);
			break;
		case 13:
			if ((opcode & 0xFFE00000) == 0x34000000) {
				printInsn(sb, "li");
				disassembleLi(sb, pc, opcode, false);
			} else {
				printInsn(sb, "ori");
				disassemble2RegistersImmediate(sb, pc, opcode, false);
			}
			break;
		case 14:
			printInsn(sb, "xori");
			disassemble2RegistersImmediate(sb, pc, opcode, false);
			break;
		case 15:
			printInsn(sb, "lui");
			disassembleLui(sb, pc, opcode);
			break;

		case 16:
			disassembleCop0(sb, pc, opcode);
			break;
		case 17:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 18:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 19:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 20:
			printInsn(sb, "beql");
			disassembleBranch2(sb, pc, opcode);
			break;
		case 21:
			printInsn(sb, "bnel");
			disassembleBranch2(sb, pc, opcode);
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
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 25:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 26:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 27:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 28:
			disassembleSpecial2(sb, pc, opcode);
			break;
		case 29:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 30:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 31:
			disassembleInvalid(sb, pc, opcode, true);
			break;

		case 32:
			printInsn(sb, "lb");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 33:
			printInsn(sb, "lh");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 34:
			printInsn(sb, "lwl");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 35:
			printInsn(sb, "lw");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 36:
			printInsn(sb, "lbu");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 37:
			printInsn(sb, "lhu");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 38:
			printInsn(sb, "lwr");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 39:
			disassembleInvalid(sb, pc, opcode, true);
			break;

		case 40:
			printInsn(sb, "sb");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 41:
			printInsn(sb, "sh");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 42:
			printInsn(sb, "swl");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 43:
			printInsn(sb, "sw");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 44:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 45:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 46:
			printInsn(sb, "swr");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 47:
			printInsn(sb, "cache");
			disassembleCache(sb, pc, opcode);
			break;

		case 48:
			printInsn(sb, "ll");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 49:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 50:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 51:
			printInsn(sb, "pref");
			disassemblePref(sb, pc, opcode);
			break;
		case 52:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 53:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 54:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 55:
			disassembleInvalid(sb, pc, opcode, true);
			break;

		case 56:
			printInsn(sb, "sc");
			disassembleLoadStore(sb, pc, opcode);
			break;
		case 57:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 58:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 59:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 60:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		case 61:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 62:
			disassembleInvalid(sb, pc, opcode, false);
			break;
		case 63:
			disassembleInvalid(sb, pc, opcode, true);
			break;
		}
		return sb.toString();
	}
}
