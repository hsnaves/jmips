package jmips.cpu.dynrec;

import static jmips.cpu.Mips.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jmips.cpu.Cpu;
import jmips.cpu.Mips;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class DynamicRecompiler extends ClassLoader {
	public static final int BLOCK_SIZE = 4096;

	private static final int VAR_CPU = 1;
	private static final int VAR_NUM_CYCLES = 2;
	private static final int VAR_PC = 3;
	private static final int VAR_TEMP_INTEGER1 = 4;
	private static final int VAR_TEMP_INTEGER2 = 5;
	private static final int VAR_TEMP_INTEGER3 = 6;

	private static final AtomicInteger counter = new AtomicInteger();

	private MethodVisitor mv;
	private Printer printer;
	private Label labels[];
	private Label returnLabel, jumpLabel;
	private Cpu cpu;

	
	public RecompiledBlock recompile(Cpu cpu, int blockPhysicalAddress) {
		int id;
		byte[] b;

		if (blockPhysicalAddress >= cpu.getRam().getRamSize())
			return null;

		id = counter.incrementAndGet();
		this.cpu = cpu;
		b = recompileBlock(id, blockPhysicalAddress);

		Class<?> clazz = defineClass("jmips.cpu.dynrec.Block_" + id, b, 0, b.length);

		try {
			return (RecompiledBlock) clazz.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private void createConstructor(ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
				"jmips/cpu/dynrec/RecompiledBlock", "<init>", "()V");
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private byte[] recompileBlock(int id, int blockPhysicalAddress) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(Opcodes.V1_4, Opcodes.ACC_FINAL + Opcodes.ACC_PUBLIC,
				"jmips/cpu/dynrec/Block_" + id, null,
				"jmips/cpu/dynrec/RecompiledBlock", null);
		createConstructor(cw);

		mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute",
				"(Ljmips/cpu/Cpu;I)I", null, null);
		printer = new Textifier();
		mv = new TraceMethodVisitor(mv, printer);
		mv.visitCode();

		returnLabel = new Label();

		labels = new Label[BLOCK_SIZE / 4];
		int[] switchValues = new int[BLOCK_SIZE / 4];
		for(int i = 0; i < BLOCK_SIZE / 4; i++) {
			labels[i] = new Label();
			switchValues[i] = i;
		}

		extractPc();
		mv.visitVarInsn(Opcodes.ISTORE, VAR_PC);

		mv.visitVarInsn(Opcodes.ILOAD, VAR_NUM_CYCLES);
		mv.visitJumpInsn(Opcodes.IFLE, returnLabel);

		mv.visitVarInsn(Opcodes.ILOAD, VAR_PC);
		mv.visitLdcInsn(2);
		mv.visitInsn(Opcodes.ISHR);
		mv.visitLdcInsn(0x03FF);
		mv.visitInsn(Opcodes.IAND);

		mv.visitLookupSwitchInsn(returnLabel, switchValues, labels);
		for(int i = 0; i < BLOCK_SIZE / 4; i++) {
			int physicalAddress = blockPhysicalAddress + 4 * i;
			int opcode = cpu.load32phys(physicalAddress);
			mv.visitLabel(labels[i]);
			appendText("    // 0x%08X: 0x%08X %s\n", physicalAddress, opcode, Mips.disassemble(opcode, physicalAddress));
			recompileInstruction(opcode);
		}

		mv.visitLabel(returnLabel);

		mv.visitVarInsn(Opcodes.ILOAD, VAR_PC);
		updatePc();

		mv.visitVarInsn(Opcodes.ILOAD, VAR_NUM_CYCLES);
		mv.visitInsn(Opcodes.IRETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		System.out.println(sw.toString());
		cw.visitEnd();
		return cw.toByteArray();
	}

	private void appendText(String format, Object... args) {
		@SuppressWarnings("unchecked")
		List<String> text = (List<String>) printer.text;
		text.add(String.format(format, args));
	}

	private void extractPc() {
		mv.visitVarInsn(Opcodes.ALOAD, VAR_CPU);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "jmips/cpu/Cpu", "getPc", "()I");
	}

	private void updatePc() {
		mv.visitVarInsn(Opcodes.ALOAD, VAR_CPU);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "jmips/cpu/Cpu", "setPc", "(I)");
	}

	private void extractGpr(int reg) {
		if (reg == 0) {
			mv.visitInsn(Opcodes.ICONST_0);
		} else {
			mv.visitVarInsn(Opcodes.ALOAD, VAR_CPU);
			mv.visitLdcInsn(reg);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "jmips/cpu/Cpu", "getGpr", "(I)I");
		}
	}

	private void updateGpr(int reg) {
		if (reg == 0) {
		} else {
			mv.visitVarInsn(Opcodes.ALOAD, VAR_CPU);
			mv.visitInsn(Opcodes.SWAP);
			mv.visitLdcInsn(reg);
			mv.visitInsn(Opcodes.SWAP);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "jmips/cpu/Cpu", "setGpr", "(II)V");
		}
	}


	private void callCheckOverflow(boolean sum) {
		mv.visitVarInsn(Opcodes.ALOAD, VAR_CPU);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER1);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER2);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER3);
		mv.visitLdcInsn(sum);
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "jmips/cpu/Cpu", "checkOverflow", "(IIIZ)Z");

		Label notOverflowLabel = new Label();
		mv.visitJumpInsn(Opcodes.IFEQ, notOverflowLabel);

		// TODO: Call overflow

		mv.visitLabel(notOverflowLabel);
	}

	private void setOnCondition(int opcodeCondition) {
		Label trueLabel = new Label();
		Label endLabel = new Label();
		mv.visitJumpInsn(opcodeCondition, trueLabel);
		mv.visitInsn(Opcodes.ICONST_0);
		mv.visitJumpInsn(Opcodes.GOTO, endLabel);
		mv.visitLabel(trueLabel);
		mv.visitInsn(Opcodes.ICONST_1);
		mv.visitLabel(endLabel);
	}

	private void recompileInstruction(int opcode) {
		switch (DECODE_OP(opcode)) {
		case I_SPECIAL: recompileSpecial(opcode); break;
		case I_REGIMM:  recompileRegImm(opcode); break;
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

		case I_COP0:    recompileCop0(opcode); break;
		case I_COP1:    invalid(1); break;
		case I_COP2:    invalid(2); break;
		case I_COP1X:   invalid(1); break;
		case I_BEQL:    beql(opcode); break;
		case I_BNEL:    bnel(opcode); break;
		case I_BLEZL:   blezl(opcode); break;
		case I_BGTZL:   bgtzl(opcode); break;

		case I_SPECIAL2: recompileSpecial2(opcode); break;

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
		case I_CACHE:   cache(opcode); break;

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

	private void recompileSpecial(int opcode) {
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

	private void recompileSpecial2(int opcode) {
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

	private void recompileRegImm(int opcode) {
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

	private void recompileCop0(int opcode) {
		int rs = DECODE_RS(opcode);
		switch(rs) {
		case I_COP0_MFC0: mfc0(opcode); break;
		case I_COP0_MTC0: mtc0(opcode); break;
		default:
			if (rs >= I_COP0_CO_MIN && rs <= I_COP0_CO_MAX)
				recompileCop0Co(opcode);
			else
				reserved();
			break;
		}
	}

	private void recompileCop0Co(int opcode) {
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

	private void add(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER1);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER1);
		extractGpr(DECODE_RT(opcode));
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER2);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER2);
		mv.visitInsn(Opcodes.IADD);
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER3);

		callCheckOverflow(true);

		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER3);
		updateGpr(DECODE_RD(opcode));
	}

	private void addi(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER1);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER1);
		mv.visitLdcInsn(DECODE_IMM16(opcode));
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER2);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER2);
		mv.visitInsn(Opcodes.IADD);
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER3);

		callCheckOverflow(true);

		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER3);
		updateGpr(DECODE_RT(opcode));
	}

	private void addiu(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitLdcInsn(DECODE_IMM16(opcode));
		mv.visitInsn(Opcodes.IADD);
		updateGpr(DECODE_RT(opcode));
	}

	private void addu(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));
		mv.visitInsn(Opcodes.IADD);
		updateGpr(DECODE_RD(opcode));
	}

	private void and(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));
		mv.visitInsn(Opcodes.IAND);
		updateGpr(DECODE_RD(opcode));
	}

	private void andi(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitLdcInsn(DECODE_IMM16U(opcode));
		mv.visitInsn(Opcodes.IAND);
		updateGpr(DECODE_RT(opcode));
	}

	private void beq(int opcode) {
	}

	private void beql(int opcode) {
	}

	private void bgez(int opcode) {
	}

	private void bgezal(int opcode) {
	}

	private void bgezall(int opcode) {
	}

	private void bgezl(int opcode) {
	}

	private void bgtz(int opcode) {
	}

	private void bgtzl(int opcode) {
	}

	private void blez(int opcode) {
	}

	private void blezl(int opcode) {
	}

	private void bltz(int opcode) {
	}

	private void bltzal(int opcode) {
	}

	private void bltzall(int opcode) {
	}

	private void bltzl(int opcode) {
	}

	private void bne(int opcode) {
	}

	private void bnel(int opcode) {
	}

	private void break_(int opcode) {
	}

	private void cache(int opcode) {
	}

	private void clo(int opcode) {
	}

	private void clz(int opcode) {
	}

	private void deret(int opcode) {
	}

	private void div(int opcode) {
	}

	private void divu(int opcode) {
	}

	private void eret(int opcode) {
	}

	private void j(int opcode) {
	}

	private void jal(int opcode) {
	}

	private void jalr(int opcode) {
	}

	private void jr(int opcode) {
	}

	private void lb(int opcode) {
	}

	private void lbu(int opcode) {
	}

	private void lh(int opcode) {
	}

	private void lhu(int opcode) {
	}

	private void ll(int opcode) {
	}

	private void lui(int opcode) {
		int val = DECODE_IMM16(opcode) << 16;
		mv.visitLdcInsn(val);
		updateGpr(DECODE_RT(opcode));
	}

	private void lw(int opcode) {
	}

	private void lwl(int opcode) {
	}

	private void lwr(int opcode) {
	}

	private void madd(int opcode) {
	}

	private void maddu(int opcode) {
	}

	private void mfc0(int opcode) {
	}

	private void mfhi(int opcode) {
	}

	private void mflo(int opcode) {
	}

	private void movn(int opcode) {
	}

	private void movz(int opcode) {
	}

	private void msub(int opcode) {
	}

	private void msubu(int opcode) {
	}

	private void mtc0(int opcode) {
	}

	private void mthi(int opcode) {
	}

	private void mtlo(int opcode) {
	}

	private void mul(int opcode) {
	}

	private void mult(int opcode) {
	}

	private void multu(int opcode) {
	}

	private void nor(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));
		mv.visitInsn(Opcodes.IOR);
		mv.visitInsn(Opcodes.ICONST_M1);
		mv.visitInsn(Opcodes.IXOR);
		updateGpr(DECODE_RD(opcode));
	}

	private void or(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));
		mv.visitInsn(Opcodes.IOR);
		updateGpr(DECODE_RD(opcode));
	}

	private void ori(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitLdcInsn(DECODE_IMM16U(opcode));
		mv.visitInsn(Opcodes.IOR);
		updateGpr(DECODE_RT(opcode));
	}

	private void pref(int opcode) {
	}

	private void sb(int opcode) {
	}

	private void sc(int opcode) {
	}

	private void sdbbp(int opcode) {
	}

	private void sh(int opcode) {
	}

	private void sll(int opcode) {
		int rd = DECODE_RD(opcode);
		if (rd != 0) { // Check for nops
			extractGpr(DECODE_RT(opcode));
			mv.visitLdcInsn(DECODE_SA(opcode));
			mv.visitInsn(Opcodes.ISHL);
			updateGpr(DECODE_RD(opcode));
		} else {
			mv.visitInsn(Opcodes.NOP);
		}
	}

	private void sllv(int opcode) {
		extractGpr(DECODE_RT(opcode));
		extractGpr(DECODE_RS(opcode));
		mv.visitInsn(Opcodes.ISHL);
		updateGpr(DECODE_RD(opcode));
	}

	private void slt(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));

		setOnCondition(Opcodes.IF_ICMPLT);
		updateGpr(DECODE_RD(opcode));
	}

	private void slti(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitLdcInsn(DECODE_IMM16(opcode));

		setOnCondition(Opcodes.IF_ICMPLT);
		updateGpr(DECODE_RT(opcode));
	}

	private void sltiu(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitLdcInsn(DECODE_IMM16(opcode));

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "jmips/cpu/Helper", "compareUnsigned", "(II)I");
		setOnCondition(Opcodes.IFLT);
		updateGpr(DECODE_RT(opcode));
	}

	private void sltu(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));

		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "jmips/cpu/Helper", "compareUnsigned", "(II)I");
		setOnCondition(Opcodes.IFLT);
		updateGpr(DECODE_RD(opcode));
	}

	private void sra(int opcode) {
		extractGpr(DECODE_RT(opcode));
		mv.visitLdcInsn(DECODE_SA(opcode));
		mv.visitInsn(Opcodes.ISHR);
		updateGpr(DECODE_RD(opcode));
	}

	private void srav(int opcode) {
		extractGpr(DECODE_RT(opcode));
		extractGpr(DECODE_RS(opcode));
		mv.visitInsn(Opcodes.ISHR);
		updateGpr(DECODE_RD(opcode));
	}

	private void srl(int opcode) {
		extractGpr(DECODE_RT(opcode));
		mv.visitLdcInsn(DECODE_SA(opcode));
		mv.visitInsn(Opcodes.IUSHR);
		updateGpr(DECODE_RD(opcode));
	}

	private void srlv(int opcode) {
		extractGpr(DECODE_RT(opcode));
		extractGpr(DECODE_RS(opcode));
		mv.visitInsn(Opcodes.IUSHR);
		updateGpr(DECODE_RD(opcode));
	}

	private void sub(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER1);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER1);
		extractGpr(DECODE_RT(opcode));
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER2);
		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER2);

		mv.visitInsn(Opcodes.ISUB);
		mv.visitVarInsn(Opcodes.ISTORE, VAR_TEMP_INTEGER3);

		callCheckOverflow(false);

		mv.visitVarInsn(Opcodes.ILOAD, VAR_TEMP_INTEGER3);
		updateGpr(DECODE_RD(opcode));
	}

	private void subu(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));
		mv.visitInsn(Opcodes.ISUB);
		updateGpr(DECODE_RD(opcode));
	}

	private void sw(int opcode) {
	}

	private void swl(int opcode) {
	}

	private void swr(int opcode) {
	}

	private void sync(int opcode) {
	}

	private void syscall(int opcode) {
	}

	private void teq(int opcode) {
	}

	private void teqi(int opcode) {
	}

	private void tge(int opcode) {
	}

	private void tgei(int opcode) {
	}

	private void tgeiu(int opcode) {
	}

	private void tgeu(int opcode) {
	}

	private void tlbp(int opcode) {
	}

	private void tlbr(int opcode) {
	}

	private void tlbwi(int opcode) {
	}

	private void tlbwr(int opcode) {
	}

	private void tlt(int opcode) {
	}

	private void tlti(int opcode) {
	}

	private void tltiu(int opcode) {
	}

	private void tltu(int opcode) {
	}

	private void tne(int opcode) {
	}

	private void tnei(int opcode) {
	}

	private void wait(int opcode) {
	}

	private void xor(int opcode) {
		extractGpr(DECODE_RS(opcode));
		extractGpr(DECODE_RT(opcode));
		mv.visitInsn(Opcodes.IXOR);
		updateGpr(DECODE_RD(opcode));
	}

	private void xori(int opcode) {
		extractGpr(DECODE_RS(opcode));
		mv.visitLdcInsn(DECODE_IMM16U(opcode));
		mv.visitInsn(Opcodes.IXOR);
		updateGpr(DECODE_RT(opcode));
	}

	private void invalid(int copno) {
	}

	private void reserved() {
	}
}