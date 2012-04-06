package jmips.cpu;

import static jmips.cpu.Mips.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class MipsTest {
	private final int pc = 0x80100000;

	@Test
	public void testDisassembleADD() {
		int opcode = ENCODE_ADD(GPR_AT, GPR_V0, GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("add $at, $v0, $v1", disasm);
	}

	@Test
	public void testDisassembleADDI() {
		int opcode = ENCODE_ADDI(GPR_V0, GPR_V1, -7);
		String disasm = disassemble(opcode, pc);
		assertEquals("addi $v0, $v1, -7", disasm);
	}

	@Test
	public void testDisassembleADDIU() {
		int opcode = ENCODE_ADDIU(GPR_V0, GPR_V1, 32);
		String disasm = disassemble(opcode, pc);
		assertEquals("addiu $v0, $v1, 32", disasm);
	}

	@Test
	public void testDisassembleADDU() {
		int opcode = ENCODE_ADDU(GPR_AT, GPR_V0, GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("addu $at, $v0, $v1", disasm);
	}

	@Test
	public void testDisassembleAND() {
		int opcode = ENCODE_AND(GPR_K0, GPR_A0, GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("and $k0, $a0, $v1", disasm);
	}

	@Test
	public void testDisassembleANDI() {
		int opcode = ENCODE_ANDI(GPR_K0, GPR_A0, -2);
		String disasm = disassemble(opcode, pc);
		assertEquals("andi $k0, $a0, 0xFFFE", disasm);
	}

	@Test
	public void testDisassembleBEQ() {
		int opcode = ENCODE_BEQ(GPR_K1, GPR_A0, pc - 32, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("beq $k1, $a0, 0x%08X", pc - 32);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBEQL() {
		int opcode = ENCODE_BEQL(GPR_K1, GPR_A0, pc + 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("beql $k1, $a0, 0x%08X", pc + 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBGEZ() {
		int opcode = ENCODE_BGEZ(GPR_S1, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bgez $s1, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBGEZAL() {
		int opcode = ENCODE_BGEZAL(GPR_S1, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bgezal $s1, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBGEZALL() {
		int opcode = ENCODE_BGEZALL(GPR_S1, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bgezall $s1, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBGEZL() {
		int opcode = ENCODE_BGEZL(GPR_S1, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bgezl $s1, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBGTZ() {
		int opcode = ENCODE_BGTZ(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bgtz $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBGTZL() {
		int opcode = ENCODE_BGTZL(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bgtzl $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBLEZ() {
		int opcode = ENCODE_BLEZ(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("blez $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBLEZL() {
		int opcode = ENCODE_BLEZL(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("blezl $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBLTZ() {
		int opcode = ENCODE_BLTZ(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bltz $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBLTZAL() {
		int opcode = ENCODE_BLTZAL(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bltzal $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBLTZALL() {
		int opcode = ENCODE_BLTZALL(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bltzall $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBLTZL() {
		int opcode = ENCODE_BLTZL(GPR_S2, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bltzl $s2, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBNE() {
		int opcode = ENCODE_BNE(GPR_S2, GPR_T0, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bne $s2, $t0, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBNEL() {
		int opcode = ENCODE_BNEL(GPR_S2, GPR_T0, pc - 64, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("bnel $s2, $t0, 0x%08X", pc - 64);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleBREAK() {
		int opcode = ENCODE_BREAK(13);
		String disasm = disassemble(opcode, pc);
		assertEquals("break (0xD)", disasm);

		opcode = ENCODE_BREAK(0);
		disasm = disassemble(opcode, pc);
		assertEquals("break ", disasm);
	}

	@Test
	public void testDisassembleCACHE() {
		int opcode = ENCODE_CACHE(2, GPR_RA, -6);
		String disasm = disassemble(opcode, pc);
		assertEquals("cache 2, -6($ra)", disasm);
	}

	@Test
	public void testDisassembleCLO() {
		int opcode = ENCODE_CLO(GPR_V1, GPR_SP);
		String disasm = disassemble(opcode, pc);
		assertEquals("clo $v1, $sp", disasm);
	}

	@Test
	public void testDisassembleCLZ() {
		int opcode = ENCODE_CLO(GPR_V1, GPR_SP);
		String disasm = disassemble(opcode, pc);
		assertEquals("clo $v1, $sp", disasm);
	}

	@Test
	public void testDisassembleDERET() {
		int opcode = ENCODE_DERET();
		String disasm = disassemble(opcode, pc);
		assertEquals("deret", disasm);
	}

	@Test
	public void testDisassembleDIV() {
		int opcode = ENCODE_DIV(GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("div $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleDIVU() {
		int opcode = ENCODE_DIVU(GPR_T3, GPR_T2);
		String disasm = disassemble(opcode, pc);
		assertEquals("divu $t3, $t2", disasm);
	}

	@Test
	public void testDisassembleERET() {
		int opcode = ENCODE_ERET();
		String disasm = disassemble(opcode, pc);
		assertEquals("eret", disasm);
	}

	@Test
	public void testDisassembleJ() {
		int opcode = ENCODE_J(pc + 1000, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("j 0x%08X", pc + 1000);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleJAL() {
		int opcode = ENCODE_JAL(pc + 1000, pc);
		String disasm = disassemble(opcode, pc);
		String expected = String.format("jal 0x%08X", pc + 1000);
		assertEquals(expected, disasm);
	}

	@Test
	public void testDisassembleJALR() {
		int opcode = ENCODE_JALR(GPR_FP, GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("jalr $fp, $v1", disasm);
	}

	@Test
	public void testDisassembleJR() {
		int opcode = ENCODE_JR(GPR_S2);
		String disasm = disassemble(opcode, pc);
		assertEquals("jr $s2", disasm);
	}

	@Test
	public void testDisassembleLB() {
		int opcode = ENCODE_LB(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lb $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLBU() {
		int opcode = ENCODE_LBU(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lbu $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLH() {
		int opcode = ENCODE_LH(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lh $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLHU() {
		int opcode = ENCODE_LHU(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lhu $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLL() {
		int opcode = ENCODE_LL(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("ll $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLUI() {
		int opcode = ENCODE_LUI(GPR_V0, 123);
		String disasm = disassemble(opcode, pc);
		assertEquals("lui $v0, 0x007B", disasm);
	}

	@Test
	public void testDisassembleLW() {
		int opcode = ENCODE_LW(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lw $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLWL() {
		int opcode = ENCODE_LWL(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lwl $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleLWR() {
		int opcode = ENCODE_LWR(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("lwr $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleMADD() {
		int opcode = ENCODE_MADD(GPR_V0, GPR_A0);
		String disasm = disassemble(opcode, pc);
		assertEquals("madd $v0, $a0", disasm);
	}

	@Test
	public void testDisassembleMADDU() {
		int opcode = ENCODE_MADDU(GPR_V0, GPR_A0);
		String disasm = disassemble(opcode, pc);
		assertEquals("maddu $v0, $a0", disasm);
	}

	@Test
	public void testDisassembleMFC0() {
		int opcode = ENCODE_MFC0(GPR_V1, COP0_BADVADDR, 0);
		String disasm = disassemble(opcode, pc);
		assertEquals("mfc0 $v1, BadVAddr", disasm);
	}

	@Test
	public void testDisassembleMFHI() {
		int opcode = ENCODE_MFHI(GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("mfhi $v1", disasm);
	}

	@Test
	public void testDisassembleMFLO() {
		int opcode = ENCODE_MFLO(GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("mflo $v1", disasm);
	}

	@Test
	public void testDisassembleMOVN() {
		int opcode = ENCODE_MOVN(GPR_V1, GPR_A0, GPR_A1);
		String disasm = disassemble(opcode, pc);
		assertEquals("movn $v1, $a0, $a1", disasm);
	}

	@Test
	public void testDisassembleMOVZ() {
		int opcode = ENCODE_MOVZ(GPR_V1, GPR_A0, GPR_A1);
		String disasm = disassemble(opcode, pc);
		assertEquals("movz $v1, $a0, $a1", disasm);
	}

	@Test
	public void testDisassembleMSUB() {
		int opcode = ENCODE_MSUB(GPR_V0, GPR_A0);
		String disasm = disassemble(opcode, pc);
		assertEquals("msub $v0, $a0", disasm);
	}

	@Test
	public void testDisassembleMSUBU() {
		int opcode = ENCODE_MSUBU(GPR_V0, GPR_A0);
		String disasm = disassemble(opcode, pc);
		assertEquals("msubu $v0, $a0", disasm);
	}

	@Test
	public void testDisassembleMTC0() {
		int opcode = ENCODE_MTC0(GPR_V1, COP0_CONFIG, 1);
		String disasm = disassemble(opcode, pc);
		assertEquals("mtc0 $v1, Config1", disasm);
	}

	@Test
	public void testDisassembleMTHI() {
		int opcode = ENCODE_MTHI(GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("mthi $v1", disasm);
	}

	@Test
	public void testDisassembleMTLO() {
		int opcode = ENCODE_MTLO(GPR_V1);
		String disasm = disassemble(opcode, pc);
		assertEquals("mtlo $v1", disasm);
	}

	@Test
	public void testDisassembleMUL() {
		int opcode = ENCODE_MUL(GPR_V0, GPR_A0, GPR_A1);
		String disasm = disassemble(opcode, pc);
		assertEquals("mul $v0, $a0, $a1", disasm);
	}

	@Test
	public void testDisassembleMULT() {
		int opcode = ENCODE_MULT(GPR_V0, GPR_A0);
		String disasm = disassemble(opcode, pc);
		assertEquals("mult $v0, $a0", disasm);
	}

	@Test
	public void testDisassembleMULTU() {
		int opcode = ENCODE_MULTU(GPR_V0, GPR_A0);
		String disasm = disassemble(opcode, pc);
		assertEquals("multu $v0, $a0", disasm);
	}

	@Test
	public void testDisassembleNOR() {
		int opcode = ENCODE_NOR(GPR_K0, GPR_A0, GPR_AT);
		String disasm = disassemble(opcode, pc);
		assertEquals("nor $k0, $a0, $at", disasm);
	}

	@Test
	public void testDisassembleOR() {
		int opcode = ENCODE_OR(GPR_K0, GPR_A0, GPR_AT);
		String disasm = disassemble(opcode, pc);
		assertEquals("or $k0, $a0, $at", disasm);
	}

	@Test
	public void testDisassembleORI() {
		int opcode = ENCODE_ORI(GPR_K0, GPR_A0, 0x4321);
		String disasm = disassemble(opcode, pc);
		assertEquals("ori $k0, $a0, 0x4321", disasm);
	}

	@Test
	public void testDisassemblePREF() {
		int opcode = ENCODE_PREF(5, GPR_K0, 9);
		String disasm = disassemble(opcode, pc);
		assertEquals("pref 5, 9($k0)", disasm);
	}

	@Test
	public void testDisassembleSB() {
		int opcode = ENCODE_SB(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("sb $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleSC() {
		int opcode = ENCODE_SC(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("sc $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleSDBPP() {
		int opcode = ENCODE_SDBBP(5);
		String disasm = disassemble(opcode, pc);
		assertEquals("sdbbp (0x5)", disasm);

		opcode = ENCODE_SDBBP(0);
		disasm = disassemble(opcode, pc);
		assertEquals("sdbbp ", disasm);
	}

	@Test
	public void testDisassembleSH() {
		int opcode = ENCODE_SH(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("sh $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleSLL() {
		int opcode = ENCODE_SLL(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("sll $v0, $a0, 12", disasm);
	}

	@Test
	public void testDisassembleSLLV() {
		int opcode = ENCODE_SLLV(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("sllv $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSLT() {
		int opcode = ENCODE_SLT(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("slt $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSLTI() {
		int opcode = ENCODE_SLTI(GPR_V0, GPR_A0, 13);
		String disasm = disassemble(opcode, pc);
		assertEquals("slti $v0, $a0, 13", disasm);
	}

	@Test
	public void testDisassembleSLTIU() {
		int opcode = ENCODE_SLTIU(GPR_V0, GPR_A0, -20);
		String disasm = disassemble(opcode, pc);
		assertEquals("sltiu $v0, $a0, -20", disasm);
	}

	@Test
	public void testDisassembleSLTU() {
		int opcode = ENCODE_SLTU(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("sltu $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSRA() {
		int opcode = ENCODE_SRA(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("sra $v0, $a0, 12", disasm);
	}

	@Test
	public void testDisassembleSRAV() {
		int opcode = ENCODE_SRAV(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("srav $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSRL() {
		int opcode = ENCODE_SRL(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("srl $v0, $a0, 12", disasm);
	}

	@Test
	public void testDisassembleSRLV() {
		int opcode = ENCODE_SRLV(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("srlv $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSUB() {
		int opcode = ENCODE_SUB(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("sub $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSUBU() {
		int opcode = ENCODE_SUBU(GPR_V0, GPR_A0, GPR_A3);
		String disasm = disassemble(opcode, pc);
		assertEquals("subu $v0, $a0, $a3", disasm);
	}

	@Test
	public void testDisassembleSW() {
		int opcode = ENCODE_SW(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("sw $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleSWL() {
		int opcode = ENCODE_SWL(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("swl $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleSWR() {
		int opcode = ENCODE_SWR(GPR_V0, GPR_A0, 12);
		String disasm = disassemble(opcode, pc);
		assertEquals("swr $v0, 12($a0)", disasm);
	}

	@Test
	public void testDisassembleSYNC() {
		int opcode = ENCODE_SYNC(5);
		String disasm = disassemble(opcode, pc);
		assertEquals("sync (0x5)", disasm);

		opcode = ENCODE_SYNC(0);
		disasm = disassemble(opcode, pc);
		assertEquals("sync ", disasm);
	}

	@Test
	public void testDisassembleSYSCALL() {
		int opcode = ENCODE_SYSCALL(5);
		String disasm = disassemble(opcode, pc);
		assertEquals("syscall (0x5)", disasm);

		opcode = ENCODE_SYSCALL(0);
		disasm = disassemble(opcode, pc);
		assertEquals("syscall ", disasm);
	}

	@Test
	public void testDisassembleTEQ() {
		int opcode = ENCODE_TEQ(GPR_A0, GPR_A1, 7);
		String disasm = disassemble(opcode, pc);
		assertEquals("teq $a0, $a1 (0x0007)", disasm);

		opcode = ENCODE_TEQ(GPR_A0, GPR_A1, 0);
		disasm = disassemble(opcode, pc);
		assertEquals("teq $a0, $a1 ", disasm);
	}

	@Test
	public void testDisassembleTEQI() {
		int opcode = ENCODE_TEQI(GPR_A0, -43);
		String disasm = disassemble(opcode, pc);
		assertEquals("teqi $a0, -43", disasm);
	}

	@Test
	public void testDisassembleTGE() {
		int opcode = ENCODE_TGE(GPR_A0, GPR_A1, 7);
		String disasm = disassemble(opcode, pc);
		assertEquals("tge $a0, $a1 (0x0007)", disasm);

		opcode = ENCODE_TGE(GPR_A0, GPR_A1, 0);
		disasm = disassemble(opcode, pc);
		assertEquals("tge $a0, $a1 ", disasm);
	}

	@Test
	public void testDisassembleTGEI() {
		int opcode = ENCODE_TGEI(GPR_A0, -43);
		String disasm = disassemble(opcode, pc);
		assertEquals("tgei $a0, -43", disasm);
	}

	@Test
	public void testDisassembleTGEIU() {
		int opcode = ENCODE_TGEIU(GPR_A0, -43);
		String disasm = disassemble(opcode, pc);
		assertEquals("tgeiu $a0, -43", disasm);
	}

	@Test
	public void testDisassembleTGEU() {
		int opcode = ENCODE_TGEU(GPR_A0, GPR_A1, 7);
		String disasm = disassemble(opcode, pc);
		assertEquals("tgeu $a0, $a1 (0x0007)", disasm);

		opcode = ENCODE_TGEU(GPR_A0, GPR_A1, 0);
		disasm = disassemble(opcode, pc);
		assertEquals("tgeu $a0, $a1 ", disasm);
	}

	@Test
	public void testDisassembleTLBP() {
		int opcode = ENCODE_TLBP();
		String disasm = disassemble(opcode, pc);
		assertEquals("tlbp", disasm);
	}

	@Test
	public void testDisassembleTLBR() {
		int opcode = ENCODE_TLBR();
		String disasm = disassemble(opcode, pc);
		assertEquals("tlbr", disasm);
	}

	@Test
	public void testDisassembleTLBWI() {
		int opcode = ENCODE_TLBWI();
		String disasm = disassemble(opcode, pc);
		assertEquals("tlbwi", disasm);
	}

	@Test
	public void testDisassembleTLBWR() {
		int opcode = ENCODE_TLBWR();
		String disasm = disassemble(opcode, pc);
		assertEquals("tlbwr", disasm);
	}

	@Test
	public void testDisassembleTLT() {
		int opcode = ENCODE_TLT(GPR_A0, GPR_A1, 7);
		String disasm = disassemble(opcode, pc);
		assertEquals("tlt $a0, $a1 (0x0007)", disasm);

		opcode = ENCODE_TLT(GPR_A0, GPR_A1, 0);
		disasm = disassemble(opcode, pc);
		assertEquals("tlt $a0, $a1 ", disasm);
	}

	@Test
	public void testDisassembleTLTI() {
		int opcode = ENCODE_TLTI(GPR_A0, -43);
		String disasm = disassemble(opcode, pc);
		assertEquals("tlti $a0, -43", disasm);
	}

	@Test
	public void testDisassembleTLTIU() {
		int opcode = ENCODE_TLTIU(GPR_A0, -43);
		String disasm = disassemble(opcode, pc);
		assertEquals("tltiu $a0, -43", disasm);
	}

	@Test
	public void testDisassembleTLTU() {
		int opcode = ENCODE_TLTU(GPR_A0, GPR_A1, 7);
		String disasm = disassemble(opcode, pc);
		assertEquals("tltu $a0, $a1 (0x0007)", disasm);

		opcode = ENCODE_TLTU(GPR_A0, GPR_A1, 0);
		disasm = disassemble(opcode, pc);
		assertEquals("tltu $a0, $a1 ", disasm);
	}

	@Test
	public void testDisassembleTNE() {
		int opcode = ENCODE_TNE(GPR_A0, GPR_A1, 7);
		String disasm = disassemble(opcode, pc);
		assertEquals("tne $a0, $a1 (0x0007)", disasm);

		opcode = ENCODE_TNE(GPR_A0, GPR_A1, 0);
		disasm = disassemble(opcode, pc);
		assertEquals("tne $a0, $a1 ", disasm);
	}

	@Test
	public void testDisassembleTNEI() {
		int opcode = ENCODE_TNEI(GPR_A0, -43);
		String disasm = disassemble(opcode, pc);
		assertEquals("tnei $a0, -43", disasm);
	}

	@Test
	public void testDisassembleWAIT() {
		int opcode = ENCODE_WAIT(7);
		String disasm = disassemble(opcode, pc);
		assertEquals("wait (0x7)", disasm);

		opcode = ENCODE_WAIT(0);
		disasm = disassemble(opcode, pc);
		assertEquals("wait ", disasm);
	}

	@Test
	public void testDisassembleXOR() {
		int opcode = ENCODE_XOR(GPR_K0, GPR_A0, GPR_AT);
		String disasm = disassemble(opcode, pc);
		assertEquals("xor $k0, $a0, $at", disasm);
	}

	@Test
	public void testDisassembleXORI() {
		int opcode = ENCODE_XORI(GPR_K0, GPR_A0, 0x4321);
		String disasm = disassemble(opcode, pc);
		assertEquals("xori $k0, $a0, 0x4321", disasm);
	}

}
