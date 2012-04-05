package jmips.cpu;

public class Mips {
	// General purpose register constants
	public static final int GPR_ZR = 0;  // Constant 0
	public static final int GPR_AT = 1;  // Assembler temporary
	public static final int GPR_V0 = 2;  // First return register
	public static final int GPR_V1 = 3;  // Second return register 
	public static final int GPR_A0 = 4;  // Function arguments
	public static final int GPR_A1 = 5;
	public static final int GPR_A2 = 6;
	public static final int GPR_A3 = 7;
	public static final int GPR_T0 = 8;  // Temporaries
	public static final int GPR_T1 = 9;
	public static final int GPR_T2 = 10;
	public static final int GPR_T3 = 11;
	public static final int GPR_T4 = 12;
	public static final int GPR_T5 = 13;
	public static final int GPR_T6 = 14;
	public static final int GPR_T7 = 15;
	public static final int GPR_S0 = 16; // Saved temporaries
	public static final int GPR_S1 = 17;
	public static final int GPR_S2 = 18;
	public static final int GPR_S3 = 19;
	public static final int GPR_S4 = 20;
	public static final int GPR_S5 = 21;
	public static final int GPR_S6 = 22;
	public static final int GPR_S7 = 23;
	public static final int GPR_T8 = 24; // More temporaries
	public static final int GPR_T9 = 25;
	public static final int GPR_K0 = 26; // Reserved for OS kernel
	public static final int GPR_K1 = 27;
	public static final int GPR_GP = 28; // Global pointer
	public static final int GPR_SP = 29; // Stack pointer
	public static final int GPR_FP = 30; // Frame pointer
	public static final int GPR_RA = 31; // Return address

	// The name of the general purpose registers
	public static final String[] REGISTER_NAMES = {
		"$zr", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
		"$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
		"$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
		"$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
	};

	// Coprocessor0 register constants
	public static final int COP0_INDEX = 0;         // Index into TLB array
	public static final int COP0_RANDOM = 1;        // Random generated index into the TLB array
	public static final int COP0_ENTRYLO0 = 2;      // Low portion of the TLB entry for even virtual pages
	public static final int COP0_ENTRYLO1 = 3;      // Low portion of the TLB entry for odd virtual pages
	public static final int COP0_CONTEXT = 4;       // Pointer to page table entry in memory
	public static final int COP0_PAGEMASK = 5;      // Controls the page sizes in TLB entries
	public static final int COP0_WIRED = 6;         // Controls the number of fixed TLB entries
	public static final int COP0_RESERVED1 = 7;     // Reserved
	public static final int COP0_BADVADDR = 8;      // ASddress for the most recent address-related exception
	public static final int COP0_COUNT = 9;         // Processor cycle count
	public static final int COP0_ENTRYHI = 10;      // High-order portion of the TLB entry
	public static final int COP0_COMPARE = 11;      // Timer interrupt control
	public static final int COP0_STATUS = 12;       // Processor status and control
	public static final int COP0_CAUSE = 13;        // Cause of last exception
	public static final int COP0_EPC = 14;          // Program counter at last exception
	public static final int COP0_PRID = 15;         // Processor identification and revision
	public static final int COP0_CONFIG = 16;       // Configuration registers
	public static final int COP0_LLADDR = 17;       // Load linked address
	public static final int COP0_WATCHLO = 18;      // Watchpoint address(low order)
	public static final int COP0_WATCHHI = 19;      // Watchpoint address(high order) and mask
	public static final int COP0_RESERVED2 = 20;    // Reserved
	public static final int COP0_RESERVED3 = 21;    // Reserved
	public static final int COP0_RESERVED4 = 22;    // Reserved
	public static final int COP0_DEBUG = 23;        // Debug control and exception status
	public static final int COP0_DEPC = 24;         // Program counter at last debug exception
	public static final int COP0_RESERVED5 = 25;    // Reserved
	public static final int COP0_ERRCTRL = 26;      // Control access to data for CACHE instruction
	public static final int COP0_RESERVED6 = 27;    // Reserved
	public static final int COP0_TAGLO = 28;        // Low-order portion of cache tag interface
	public static final int COP0_RESERVED7 = 29;    // Reserved
	public static final int COP0_ERROREPC = 30;     // Program counter at last error
	public static final int COP0_DESAVE = 31;       // Debug handler scratchpad register

	// The name of the Coprocessor0 registers
	public static final String[] COP0_REGISTER_NAMES = {
		"Index", "Random", "EntryLo0", "EntryLo1", "Context", "PageMask", "Wired", "Resv1",
		"BadVAddr", "Count", "EntryHi", "Compare", "Status", "Cause", "EPC", "PRId",
		"Config", "LLAddr", "WatchLo", "WatchHi", "Resv2", "Resv3", "Resv4", "Debug",
		"DEPC", "Resv5", "ErrCtl", "Resv6", "TagLo", "Resv7", "ErrorEPC", "DeSave"
	};

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

	// The description of the exception codes
	public static final String[] COP0_EXCEPTION_NAMES = {
		"Interrupt exception", "TLB Modification Exception", "TLB exception (load or fetch)", "TLB exception (store)",
		"Address exception (load or fetch)", "Address exception (store)", "Bus Error Exception (instruction fetch)", "Bus Error Exception (load or store)",
		"Syscall exception", "Breakpoint exception", "Reserved Instruction exception", " Coprocessor Unusable exception",
		"Overflow exception", "Trap exception", "(unknown14)", "(unknown exception: 15)",
		"(unknown exception: 16)", "(unknown exception: 17)", "(unknown exception: 18)", "(unknown exception: 19)",
		"(unknown exception: 20)", "(unknown exception: 21)", "(unknown exception: 22)", "Watch exception",
		"Machine Check exception", "(unknown exception: 25)", "(unknown exception: 26)", "(unknown exception: 27)",
		"(unknown exception: 28)", "(unknown exception: 29)", "(unknown exception: 30)", "(unknown exception: 31)"
	};

	// Instructions
	public static final int I_SPECIAL  = 0;
	public static final int I_REGIMM   = 1;
	public static final int I_J        = 2;
	public static final int I_JAL      = 3;
	public static final int I_BEQ      = 4;
	public static final int I_BNE      = 5;
	public static final int I_BLEZ     = 6;
	public static final int I_BGTZ     = 7;
	public static final int I_ADDI     = 8;
	public static final int I_ADDIU    = 9;
	public static final int I_SLTI     = 10;
	public static final int I_SLTIU    = 11;
	public static final int I_ANDI     = 12;
	public static final int I_ORI      = 13;
	public static final int I_XORI     = 14;
	public static final int I_LUI      = 15;
	public static final int I_COP0     = 16;

	public static final int I_BEQL     = 20;
	public static final int I_BNEL     = 21;
	public static final int I_BLEZL    = 22;
	public static final int I_BGTZL    = 23;

	public static final int I_SPECIAL2 = 28;
	
	public static final int I_LB       = 32;
	public static final int I_LH       = 33;
	public static final int I_LWL      = 34;
	public static final int I_LW       = 35;
	public static final int I_LBU      = 36;
	public static final int I_LHU      = 37;
	public static final int I_LWR      = 38;

	public static final int I_SB       = 40;
	public static final int I_SH       = 41;
	public static final int I_SWL      = 42;
	public static final int I_SW       = 43;

	public static final int I_SWR      = 46;
	public static final int I_CACHE    = 47;
	public static final int I_LL       = 48;

	public static final int I_PREF     = 51;

	public static final int I_SC       = 56;

	public static final int I_SPEC_SLL       = 0;

	public static final int I_SPEC_SRL       = 2;
	public static final int I_SPEC_SRA       = 3;
	public static final int I_SPEC_SLLV      = 4;
	public static final int I_SPEC_SRLV      = 6;
	public static final int I_SPEC_SRAV      = 7;

	public static final int I_SPEC_JR        = 8;
	public static final int I_SPEC_JALR      = 9;
	public static final int I_SPEC_MOVZ      = 10;
	public static final int I_SPEC_MOVN      = 11;
	public static final int I_SPEC_SYSCALL   = 12;
	public static final int I_SPEC_BREAK     = 13;
	public static final int I_SPEC_SYNC      = 15;

	public static final int I_SPEC_MFHI      = 16;
	public static final int I_SPEC_MTHI      = 17;
	public static final int I_SPEC_MFLO      = 18;
	public static final int I_SPEC_MTLO      = 19;

	public static final int I_SPEC_MULT      = 24;
	public static final int I_SPEC_MULTU     = 25;
	public static final int I_SPEC_DIV       = 26;
	public static final int I_SPEC_DIVU      = 27;

	public static final int I_SPEC_ADD       = 32;
	public static final int I_SPEC_ADDU      = 33;
	public static final int I_SPEC_SUB       = 34;
	public static final int I_SPEC_SUBU      = 35;
	public static final int I_SPEC_AND       = 36;
	public static final int I_SPEC_OR        = 37;

	public static final int I_SPEC_XOR       = 38;
	public static final int I_SPEC_NOR       = 39;

	public static final int I_SPEC_SLT       = 42;
	public static final int I_SPEC_SLTU      = 43;

	public static final int I_SPEC_TGE       = 48;
	public static final int I_SPEC_TGEU      = 49;
	public static final int I_SPEC_TLT       = 50;
	public static final int I_SPEC_TLTU      = 51;
	public static final int I_SPEC_TEQ       = 52;

	public static final int I_SPEC_TNE       = 54;

	public static final int I_SPEC2_MADD     = 0;
	public static final int I_SPEC2_MADDU    = 1;
	public static final int I_SPEC2_MUL      = 2;
	public static final int I_SPEC2_MSUB     = 4;
	public static final int I_SPEC2_MSUBU    = 5;
	public static final int I_SPEC2_CLZ      = 32;
	public static final int I_SPEC2_CLO      = 33;
	public static final int I_SPEC2_SDBBP    = 63;

	public static final int I_REGIMM_BLTZ    = 0;
	public static final int I_REGIMM_BGEZ    = 1;
	public static final int I_REGIMM_BLTZL   = 2;
	public static final int I_REGIMM_BGEZL   = 3;
	public static final int I_REGIMM_TGEI    = 8;
	public static final int I_REGIMM_TGEIU   = 9;
	public static final int I_REGIMM_TLTI    = 10;
	public static final int I_REGIMM_TLTIU   = 11;
	public static final int I_REGIMM_TEQI    = 12;
	public static final int I_REGIMM_TNEI    = 14;
	public static final int I_REGIMM_BLTZAL  = 16;
	public static final int I_REGIMM_BGEZAL  = 17;
	public static final int I_REGIMM_BLTZALL = 18;
	public static final int I_REGIMM_BGEZALL = 19;

	public static final int I_COP0_MFC0    = 0;
	public static final int I_COP0_MTC0    = 4;
	public static final int I_COP0_CO_MIN  = 16;
	public static final int I_COP0_CO_MAX  = 31;

	public static final int I_COP0CO_TLBR  = 1;
	public static final int I_COP0CO_TLBWI = 2;
	public static final int I_COP0CO_TLBWR = 6;
	public static final int I_COP0CO_TLBP  = 8;
	public static final int I_COP0CO_ERET  = 24;
	public static final int I_COP0CO_DERET = 31;
	public static final int I_COP0CO_WAIT  = 32;

	// Auxiliary functions to decode the opcode
	public static int DECODE_OP(int opcode) {
		return (opcode >>> 26);
	}

	public static int DECODE_FUNCT(int opcode) {
		return opcode & 0x3F;
	}

	public static int DECODE_RS(int opcode) {
		return ((opcode >> 21) & 0x1F);
	}

	public static int DECODE_RT(int opcode) {
		return ((opcode >> 16) & 0x1F);
	}

	public static int DECODE_RD(int opcode) {
		return ((opcode >> 11) & 0x1F);
	}

	public static int DECODE_SA(int opcode) {
		return ((opcode >> 6) & 0x1F);
	}

	public static int DECODE_IMM16(int opcode) {
		return (int) ((short) opcode);
	}

	public static int DECODE_IMM16U(int opcode) {
		return opcode & 0xFFFF;
	}

	public static int DECODE_BRANCH(int opcode, int pc) {
		return pc + 4 + 4 * DECODE_IMM16(opcode);
	}

	public static int DECODE_JUMP(int opcode, int pc) {
		return ((pc & 0xF0000000) | ((opcode & 0x3FFFFFF) << 2));
	}

	public static int DECODE_SYSCALLCODE(int opcode) {
		return ((opcode >> 6) & 0xFFFFF);
	}

	public static int DECODE_TRAPCODE(int opcode) {
		return ((opcode >> 6) & 0x3FF);
	}

	public static int DECODE_WAITCODE(int opcode) {
		return ((opcode >> 6) & 0x7FFFF);
	}

	public static int DECODE_COP0SEL(int opcode) {
		return (opcode & 0x07);
	}


	// Disassemble functions
	private static void disassembleInstruction(StringBuilder sb, String format, int opcode, int pc) {
		for(int i = 0; i < format.length(); i++) {
			char c = format.charAt(i);
			if (c == '%') {
				if (++i == format.length()) {
					sb.append('%');
					break;
				}
				c = format.charAt(i);
				switch(c) {
				case 's': // Rs
					sb.append(REGISTER_NAMES[DECODE_RS(opcode)]);
					break;
				case 't': // Rt
					sb.append(REGISTER_NAMES[DECODE_RT(opcode)]);
					break;
				case 'd': // Rd
					sb.append(REGISTER_NAMES[DECODE_RD(opcode)]);
					break;
				case 'b': // Branch target
					sb.append(String.format("0x%08X", DECODE_BRANCH(opcode, pc)));
					break;
				case 'j': // Jump target
					sb.append(String.format("0x%08X", DECODE_JUMP(opcode, pc)));
					break;
				case 'i': // Signed immediate
					sb.append(DECODE_IMM16(opcode));
					break;
				case 'I': // Unsigned immediate
					sb.append(String.format("0x%04X", DECODE_IMM16U(opcode)));
					break;
				case 'a': // SA (for shift operations and others)
					sb.append(DECODE_SA(opcode));
					break;
				case 'x': // For cache operations
					sb.append(DECODE_RT(opcode));
					break;
				case 'C': // Cop0 register
					int sel = DECODE_COP0SEL(opcode);
					int reg = DECODE_RD(opcode);
					if (sel == 1 && (reg == COP0_CONFIG || reg == COP0_TAGLO)) {
						if (reg == COP0_CONFIG)
							sb.append("Config1");
						else
							sb.append("DataLo");
					} else {
						sb.append(COP0_REGISTER_NAMES[reg]);
					}
					break;
				case 'T': // Trap code
					int trapCode = DECODE_TRAPCODE(opcode);
					if (trapCode != 0) sb.append(String.format("(0x%04X)", trapCode));
					break;
				case 'S': // Syscall or Break code
					int syscallCode = DECODE_SYSCALLCODE(opcode);
					if (syscallCode != 0) sb.append(String.format("(0x%X)", syscallCode));
					break;
				case 'W': // Wait code
					int waitCode = DECODE_WAITCODE(opcode);
					if (waitCode != 0) sb.append(String.format("(0x%X)", waitCode));
					break;
				case 'A': // Sync code
					int syncCode = DECODE_SA(opcode);
					if (syncCode != 0) sb.append(String.format("0x%X", syncCode));
					break;
				default:
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
		}
	}

	private static void disassembleReserved(StringBuilder sb, int opcode, int pc) {
		sb.append("(reserved)");
	}

	private static void disassembleCoprocessorUnusable(StringBuilder sb, int opcode, int pc) {
		sb.append("(coprocessor unusuable)");
	}

	private static void disassembleSpecial(StringBuilder sb, int opcode, int pc) {
		switch(DECODE_FUNCT(opcode)) {
		case I_SPEC_SLL:
			if (opcode == 0) disassembleInstruction(sb, "nop", opcode, pc);
			else if (opcode == 0x40) disassembleInstruction(sb, "ssnop", opcode, pc);
			else {
				disassembleInstruction(sb, "sll %d, %t, %a", opcode, pc);
			}
			break;
		case 1:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case I_SPEC_SRL:
			disassembleInstruction(sb, "srl %d, %t, %a", opcode, pc);
			break;
		case I_SPEC_SRA:
			disassembleInstruction(sb, "sra %d, %t, %a", opcode, pc);
			break;
		case I_SPEC_SLLV:
			disassembleInstruction(sb, "sllv %d, %t, %s", opcode, pc);
			break;
		case I_SPEC_SRLV:
			disassembleInstruction(sb, "srlv %d, %t, %s", opcode, pc);
			break;
		case I_SPEC_SRAV:
			disassembleInstruction(sb, "srav %d, %t, %s", opcode, pc);
			break;

		case I_SPEC_JR:
			disassembleInstruction(sb, "jr %s", opcode, pc);
			break;
		case I_SPEC_JALR:
			disassembleInstruction(sb, "jalr %d, %s", opcode, pc);
			break;
		case I_SPEC_MOVZ:
			disassembleInstruction(sb, "movz %d, %s, %t", opcode, pc);
			break;
		case I_SPEC_MOVN:
			disassembleInstruction(sb, "movn %d, %s, %t", opcode, pc);
			break;
		case I_SPEC_SYSCALL:
			disassembleInstruction(sb, "syscall %S", opcode, pc);
			break;
		case I_SPEC_BREAK:
			disassembleInstruction(sb, "break %S", opcode, pc);
			break;
		case I_SPEC_SYNC:
			disassembleInstruction(sb, "sync %A", opcode, pc);
			break;

		case I_SPEC_MFHI:
			disassembleInstruction(sb, "mfhi %d", opcode, pc);
			break;
		case I_SPEC_MTHI:
			disassembleInstruction(sb, "mthi %s", opcode, pc);
			break;
		case I_SPEC_MFLO:
			disassembleInstruction(sb, "mflo %d", opcode, pc);
			break;
		case I_SPEC_MTLO:
			disassembleInstruction(sb, "mtlo %s", opcode, pc);
			break;

		case I_SPEC_MULT:
			disassembleInstruction(sb, "mult %s, %t", opcode, pc);
			break;
		case I_SPEC_MULTU:
			disassembleInstruction(sb, "multu %s, %t", opcode, pc);
			break;
		case I_SPEC_DIV:
			disassembleInstruction(sb, "div %s, %t", opcode, pc);
			break;
		case I_SPEC_DIVU:
			disassembleInstruction(sb, "divu %s, %t", opcode, pc);
			break;

		case I_SPEC_ADD:
			disassembleInstruction(sb, "add %d, %s, %t", opcode, pc);
			break;
		case I_SPEC_ADDU:
			if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "move %d, %s", opcode, pc);
			} else {
				disassembleInstruction(sb, "addu %d, %s, %t", opcode, pc);
			}
			break;
		case I_SPEC_SUB:
			if (DECODE_RS(opcode) == 0) {
				disassembleInstruction(sb, "neg %d, %t", opcode, pc);
			} else {
				disassembleInstruction(sb, "sub %d, %s, %t", opcode, pc);
			}
			break;
		case I_SPEC_SUBU:
			if (DECODE_RS(opcode) == 0) {
				disassembleInstruction(sb, "negu %d, %t", opcode, pc);
			} else {
				disassembleInstruction(sb, "subu %d, %s, %t", opcode, pc);
			}
			break;
		case I_SPEC_AND:
			disassembleInstruction(sb, "and %d, %s, %t", opcode, pc);
			break;
		case I_SPEC_OR:
			if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "move %d, %s", opcode, pc);
			} else {
				disassembleInstruction(sb, "or %d, %s, %t", opcode, pc);
			}
			break;
		case I_SPEC_XOR:
			disassembleInstruction(sb, "xor %d, %s, %t", opcode, pc);
			break;
		case I_SPEC_NOR:
			if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "not %d, %s", opcode, pc);
			} else {
				disassembleInstruction(sb, "nor %d, %s, %t", opcode, pc);
			}
			break;

		case I_SPEC_SLT:
			disassembleInstruction(sb, "slt %d, %s, %t", opcode, pc);
			break;
		case I_SPEC_SLTU:
			disassembleInstruction(sb, "sltu %d, %s, %t", opcode, pc);
			break;

		case I_SPEC_TGE:
			disassembleInstruction(sb, "tge %s, %t %T", opcode, pc);
			break;
		case I_SPEC_TGEU:
			disassembleInstruction(sb, "tgeu %s, %t %T", opcode, pc);
			break;
		case I_SPEC_TLT:
			disassembleInstruction(sb, "tlt %s, %t %T", opcode, pc);
			break;
		case I_SPEC_TLTU:
			disassembleInstruction(sb, "tltu %s, %t %T", opcode, pc);
			break;
		case I_SPEC_TEQ:
			disassembleInstruction(sb, "teq %s, %t %T", opcode, pc);
			break;
		case I_SPEC_TNE:
			disassembleInstruction(sb, "tne %s, %t %T", opcode, pc);
			break;

		default:
			disassembleReserved(sb, opcode, pc);
			break;
		}
	}

	private static void disassembleSpecial2(StringBuilder sb, int opcode, int pc) {
		switch(DECODE_FUNCT(opcode)) {
		case I_SPEC2_MADD:
			disassembleInstruction(sb, "madd %s, %t", opcode, pc);
			break;
		case I_SPEC2_MADDU:
			disassembleInstruction(sb, "maddu %s, %t", opcode, pc);
			break;
		case I_SPEC2_MUL:
			disassembleInstruction(sb, "mul %d, %s, %t", opcode, pc);
			break;
		case I_SPEC2_MSUB:
			disassembleInstruction(sb, "msub %d, %s, %t", opcode, pc);
			break;
		case I_SPEC2_MSUBU:
			disassembleInstruction(sb, "msubu %d, %s, %t", opcode, pc);
			break;
		case I_SPEC2_CLZ:
			disassembleInstruction(sb, "clz %d, %s", opcode, pc);
			break;
		case I_SPEC2_CLO:
			disassembleInstruction(sb, "clo %d, %s", opcode, pc);
			break;
		case I_SPEC2_SDBBP:
			disassembleInstruction(sb, "sdbbp %S", opcode, pc);
			break;
		default:
			disassembleReserved(sb, opcode, pc);
			break;
		}
	}

	private static void disassembleRegImm(StringBuilder sb, int opcode, int pc) {
		switch(DECODE_RT(opcode)) {
		case I_REGIMM_BLTZ:
			disassembleInstruction(sb, "bltz %s, %b", opcode, pc);
			break;
		case I_REGIMM_BGEZ:
			if (DECODE_RS(opcode) == 0) {
				disassembleInstruction(sb, "b %b", opcode, pc);
			} else {
				disassembleInstruction(sb, "bgez %s, %b", opcode, pc);
			}
			break;
		case I_REGIMM_BLTZL:
			disassembleInstruction(sb, "bltzl %s, %b", opcode, pc);
			break;
		case I_REGIMM_BGEZL:
			disassembleInstruction(sb, "bgezl %s, %b", opcode, pc);
			break;
		case I_REGIMM_TGEI:
			disassembleInstruction(sb, "tgei %s, %i", opcode, pc);
			break;
		case I_REGIMM_TGEIU:
			disassembleInstruction(sb, "tgeiu %s, %i", opcode, pc);
			break;
		case I_REGIMM_TLTI:
			disassembleInstruction(sb, "tlti %s, %i", opcode, pc);
			break;
		case I_REGIMM_TLTIU:
			disassembleInstruction(sb, "tltiu %s, %i", opcode, pc);
			break;
		case I_REGIMM_TEQI:
			disassembleInstruction(sb, "teqi %s, %i", opcode, pc);
			break;
		case I_REGIMM_TNEI:
			disassembleInstruction(sb, "tnei %s, %i", opcode, pc);
			break;
		case I_REGIMM_BLTZAL:
			disassembleInstruction(sb, "bltzal %s, %b", opcode, pc);
			break;
		case I_REGIMM_BGEZAL:
			if (DECODE_RS(opcode) == 0) {
				disassembleInstruction(sb, "bal %b", opcode, pc);
			} else {
				disassembleInstruction(sb, "bgezal %s, %b", opcode, pc);
			}
			break;
		case I_REGIMM_BLTZALL:
			disassembleInstruction(sb, "bltzall %s, %b", opcode, pc);
			break;
		case I_REGIMM_BGEZALL:
			disassembleInstruction(sb, "bgezall %s, %b", opcode, pc);
			break;
		default:
			disassembleReserved(sb, opcode, pc);
			break;
		}
	}

	private static void disassembleCop0Co(StringBuilder sb, int opcode, int pc) {
		switch(DECODE_FUNCT(opcode)) {
		case I_COP0CO_TLBR:
			disassembleInstruction(sb, "tlbr", opcode, pc);
			break;
		case I_COP0CO_TLBWI:
			disassembleInstruction(sb, "tlbwi", opcode, pc);
			break;
		case I_COP0CO_TLBWR:
			disassembleInstruction(sb, "tlbwr", opcode, pc);
			break;
		case I_COP0CO_TLBP:
			disassembleInstruction(sb, "tlbp", opcode, pc);
			break;
		case I_COP0CO_ERET:
			disassembleInstruction(sb, "eret", opcode, pc);
			break;
		case I_COP0CO_DERET:
			disassembleInstruction(sb, "deret", opcode, pc);
			break;
		case I_COP0CO_WAIT:
			disassembleInstruction(sb, "wait %W", opcode, pc);
			break;
		default:
			disassembleReserved(sb, opcode, pc);
			break;
		}
	}

	private static void disassembleCop0(StringBuilder sb, int opcode, int pc) {
		int rs = DECODE_RS(opcode);
		switch(rs) {
		case I_COP0_MFC0:
			disassembleInstruction(sb, "mfc0 %t, %C", opcode, pc);
			break;
		case I_COP0_MTC0:
			disassembleInstruction(sb, "mtc0 %t, %C", opcode, pc);
			break;
		default:
			if (rs >= I_COP0_CO_MIN && rs <= I_COP0_CO_MAX)
				disassembleCop0Co(sb, opcode, pc);
			else
				disassembleReserved(sb, opcode, pc);
			break;
		}
	}

	private static void disassembleMips(StringBuilder sb, int opcode, int pc) {
		switch (DECODE_OP(opcode)) {
		case I_SPECIAL:
			disassembleSpecial(sb, opcode, pc);
			break;
		case I_REGIMM:
			disassembleRegImm(sb, opcode, pc);
			break;
		case I_J:
			disassembleInstruction(sb, "j %j", opcode, pc);
			break;
		case I_JAL:
			disassembleInstruction(sb, "jal %j", opcode, pc);
			break;
		case I_BEQ:
			if (DECODE_RS(opcode) == 0 && DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "b %b", opcode, pc);
			} else if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "beqz %s, %b", opcode, pc);
			} else {
				disassembleInstruction(sb, "beq %s, %t, %b", opcode, pc);
			}
			break;
		case I_BNE:
			if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "bnez %s, %b", opcode, pc);
			} else {
				disassembleInstruction(sb, "bne %s, %t, %b", opcode, pc);
			}
			break;
		case I_BLEZ:
			disassembleInstruction(sb, "blez %s, %b", opcode, pc);
			break;
		case I_BGTZ:
			disassembleInstruction(sb, "bgtz %s, %b", opcode, pc);
			break;

		case I_ADDI:
			disassembleInstruction(sb, "addi %t, %s, %i", opcode, pc);
			break;
		case I_ADDIU:
			if (DECODE_RS(opcode) == 0) {
				disassembleInstruction(sb, "li %t, %i", opcode, pc);
			} else {
				disassembleInstruction(sb, "addiu %t, %s, %i", opcode, pc);
			}
			break;
		case I_SLTI:
			disassembleInstruction(sb, "slti %t, %s, %i", opcode, pc);
			break;
		case I_SLTIU:
			disassembleInstruction(sb, "sltiu %t, %s, %i", opcode, pc);
			break;
		case I_ANDI:
			disassembleInstruction(sb, "andi %t, %s, %I", opcode, pc);
			break;
		case I_ORI:
			if (DECODE_RS(opcode) == 0) {
				disassembleInstruction(sb, "li %t, %I", opcode, pc);
			} else {
				disassembleInstruction(sb, "ori %t, %s, %I", opcode, pc);
			}
			break;
		case I_XORI:
			disassembleInstruction(sb, "xori %t, %s, %I", opcode, pc);
			break;
		case I_LUI:
			disassembleInstruction(sb, "lui %t, %I", opcode, pc);
			break;

		case I_COP0:
			disassembleCop0(sb, opcode, pc);
			break;
		case 17:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 18:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 19:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case I_BEQL:
			if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "beqzl %s, %b", opcode, pc);
			} else {
				disassembleInstruction(sb, "beql %s, %t, %b", opcode, pc);
			}
			break;
		case I_BNEL:
			if (DECODE_RT(opcode) == 0) {
				disassembleInstruction(sb, "bnezl %s, %b", opcode, pc);
			} else {
				disassembleInstruction(sb, "bnel %s, %t, %b", opcode, pc);
			}
			break;
		case I_BLEZL:
			disassembleInstruction(sb, "blezl %s, %b", opcode, pc);
			break;
		case I_BGTZL:
			disassembleInstruction(sb, "bgtzl %s, %b", opcode, pc);
			break;

		case 24:
			disassembleReserved(sb, opcode, pc);
			break;
		case 25:
			disassembleReserved(sb, opcode, pc);
			break;
		case 26:
			disassembleReserved(sb, opcode, pc);
			break;
		case 27:
			disassembleReserved(sb, opcode, pc);
			break;
		case 28:
			disassembleSpecial2(sb, opcode, pc);
			break;
		case 29:
			disassembleReserved(sb, opcode, pc);
			break;
		case 30:
			disassembleReserved(sb, opcode, pc);
			break;
		case 31:
			disassembleReserved(sb, opcode, pc);
			break;

		case I_LB:
			disassembleInstruction(sb, "lb %t, %i(%s)", opcode, pc);
			break;
		case I_LH:
			disassembleInstruction(sb, "lh %t, %i(%s)", opcode, pc);
			break;
		case I_LWL:
			disassembleInstruction(sb, "lwl %t, %i(%s)", opcode, pc);
			break;
		case I_LW:
			disassembleInstruction(sb, "lw %t, %i(%s)", opcode, pc);
			break;
		case I_LBU:
			disassembleInstruction(sb, "lbu %t, %i(%s)", opcode, pc);
			break;
		case I_LHU:
			disassembleInstruction(sb, "lhu %t, %i(%s)", opcode, pc);
			break;
		case I_LWR:
			disassembleInstruction(sb, "lwr %t, %i(%s)", opcode, pc);
			break;
		case 39:
			disassembleReserved(sb, opcode, pc);
			break;

		case I_SB:
			disassembleInstruction(sb, "sb %t, %i(%s)", opcode, pc);
			break;
		case I_SH:
			disassembleInstruction(sb, "sh %t, %i(%s)", opcode, pc);
			break;
		case I_SWL:
			disassembleInstruction(sb, "swl %t, %i(%s)", opcode, pc);
			break;
		case I_SW:
			disassembleInstruction(sb, "sw %t, %i(%s)", opcode, pc);
			break;
		case 44:
			disassembleReserved(sb, opcode, pc);
			break;
		case 45:
			disassembleReserved(sb, opcode, pc);
			break;
		case I_SWR:
			disassembleInstruction(sb, "swr %t, %i(%s)", opcode, pc);
			break;
		case I_CACHE:
			disassembleInstruction(sb, "cache %x, %i(%s)", opcode, pc);
			break;

		case I_LL:
			disassembleInstruction(sb, "ll %t, %i(%s)", opcode, pc);
			break;
		case 49:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 50:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case I_PREF:
			disassembleInstruction(sb, "pref %x, %i(%s)", opcode, pc);
			break;
		case 52:
			disassembleReserved(sb, opcode, pc);
			break;
		case 53:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 54:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 55:
			disassembleReserved(sb, opcode, pc);
			break;

		case I_SC:
			disassembleInstruction(sb, "sc %t, %i(%s)", opcode, pc);
			break;
		case 57:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 58:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 59:
			disassembleReserved(sb, opcode, pc);
			break;
		case 60:
			disassembleReserved(sb, opcode, pc);
			break;
		case 61:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 62:
			disassembleCoprocessorUnusable(sb, opcode, pc);
			break;
		case 63:
			disassembleReserved(sb, opcode, pc);
			break;
		}
	}


	public static String disassemble(int opcode, int pc) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("0x%08X: 0x%08X ", pc, opcode));
		disassembleMips(sb, opcode, pc);
		return sb.toString();
	}

	// Auxiliary functions to encode the opcode
	public static int ENCODE_OP(int op) {
		return (op << 26);
	}

	public static int ENCODE_FUNCT(int funct) {
		return funct;
	}

	public static int ENCODE_RS(int rs) {
		return (rs << 21);
	}

	public static int ENCODE_RT(int rt) {
		return (rt << 16);
	}

	public static int ENCODE_RD(int rd) {
		return (rd << 11);
	}

	public static int ENCODE_SA(int sa) {
		return (sa << 6);
	}

	public static int ENCODE_IMM16(int imm) {
		return (imm & 0xFFFF);
	}

	public static int ENCODE_IMM16U(int immu) {
		return (immu & 0xFFFF);
	}

	public static int ENCODE_BRANCH(int target, int pc) {
		int diff = (target - pc - 4) >> 2;
		return ENCODE_IMM16(diff);
	}

	public static int ENCODE_JUMP(int target, int pc) {
		return ((target >> 2) & 0x3FFFFFF);
	}

	public static int ENCODE_SYSCALLCODE(int code) {
		return (code << 6);
	}

	public static int ENCODE_TRAPCODE(int code) {
		return (code << 6);
	}

	public static int ENCODE_WAITCODE(int code) {
		return (code << 6);
	}

	public static int ENCODE_COP0SEL(int sel) {
		return (sel);
	}

	// Function to generate opcodes for Mips
	public static int I_ADD(int rd, int rs, int rt) {
		int opcode = ENCODE_OP(0) | ENCODE_FUNCT(32);
		opcode |= ENCODE_RD(rd);
		opcode |= ENCODE_RS(rs);
		opcode |= ENCODE_RT(rt);
		return opcode;
	}

	// Function to generate opcodes for Mips
	public static int I_ADDI(int rt, int rs, int imm) {
		int opcode = ENCODE_OP(8);
		opcode |= ENCODE_RT(rt);
		opcode |= ENCODE_RS(rs);
		opcode |= ENCODE_IMM16(imm);
		return opcode;
	}

}
