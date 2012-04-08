package jmips.cpu;

import static jmips.cpu.Mips.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class CpuTest {
	private static final int BASE_ADDRESS = 0x80100000;

	private Cpu createCpu() {
		Cpu cpu = new Cpu(4 * 1024 * 1024, null);
		reset(cpu);
		return cpu;
	}

	private void reset(Cpu cpu) {
		cpu.reset();
		cpu.setPc(BASE_ADDRESS, true);
	}

	private int executeADD(Cpu cpu, int a, int b) {
		reset(cpu);
		cpu.setGpr(GPR_A0, a);
		cpu.setGpr(GPR_A1, b);
		cpu.setGpr(GPR_V0, 0);
		cpu.add(ENCODE_ADD(GPR_V0, GPR_A0, GPR_A1));
		return cpu.getGpr(GPR_V0);
	}

	@Test
	public void testADD() {
		Cpu cpu = createCpu();

		int val = executeADD(cpu, 1, 2);
		assertEquals(3, val);
	}

	@Test
	public void testADDI() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x20820003); // addi $v0,$a0,3
		cpu.setGpr(Mips.GPR_A0, 1);
		cpu.setGpr(Mips.GPR_A0, 1);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(4, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.setGpr(Mips.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(0, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x2082FFFE); // addi $v0,$a0,-2
		cpu.setGpr(Mips.GPR_A0, 1);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(-1, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.setGpr(Mips.GPR_A0, 0x80000000);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(0, cpu.getGpr(Mips.GPR_V0));
	}

	@Test
	public void testADDIU() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x24820004); //  addiu $v0,$a0,4
		cpu.setGpr(Mips.GPR_A0, 2);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(6, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.setGpr(Mips.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals((int) 0x80000003, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x2482FFFD); // addiu $v0,$a0,-3
		cpu.setGpr(Mips.GPR_A0, 5);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(2, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.setGpr(Mips.GPR_A0, 0x80000000);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x7FFFFFFD, cpu.getGpr(Mips.GPR_V0));
	}

	@Test
	public void testADDU() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x00851021); // addu $v0,$a0,$a1
		cpu.setGpr(Mips.GPR_A0, 1);
		cpu.setGpr(Mips.GPR_A1, 10);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(11, cpu.getGpr(Mips.GPR_V0));

		reset(cpu);
		cpu.setGpr(Mips.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Mips.GPR_A1, 3);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals((int) 0x80000002, cpu.getGpr(Mips.GPR_V0));
	}

	@Test
	public void testAND() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x00851024); // and $v0,$a0,$a1
		cpu.setGpr(Mips.GPR_A0, 0xFF00FF00);
		cpu.setGpr(Mips.GPR_A1, 0x12345678);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x12005600, cpu.getGpr(Mips.GPR_V0));
	}

	@Test
	public void testANDI() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x30829132); // andi $v0,$a0,0x9132
		cpu.setGpr(Mips.GPR_A0, 0xFFFFFF00);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x00009100, cpu.getGpr(Mips.GPR_V0));
	}

	@Test
	public void testLWR() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x98820003); // lwr $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x11223344);
		cpu.write32(BASE_ADDRESS + 12, 0x55667788);
		cpu.setGpr(Mips.GPR_A0, BASE_ADDRESS + 10);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x00005566, cpu.getGpr(Mips.GPR_V0));

	}

	@Test
	public void testLWL() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x88820000); // lwl $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x11223344);
		cpu.write32(BASE_ADDRESS + 12, 0x55667788);
		cpu.setGpr(Mips.GPR_A0, BASE_ADDRESS + 9);
		cpu.setGpr(Mips.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x22334400, cpu.getGpr(Mips.GPR_V0));

	}

	@Test
	public void testSWL() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0xA8820000); // swl $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x00000000);
		cpu.write32(BASE_ADDRESS + 12, 0x00000000);
		cpu.setGpr(Mips.GPR_A0, BASE_ADDRESS + 11);
		cpu.setGpr(Mips.GPR_V0, 0x11223344);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x00000011, cpu.read32(BASE_ADDRESS + 8));

	}

	@Test
	public void testSWR() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0xB8820000); // swr $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x00000000);
		cpu.write32(BASE_ADDRESS + 12, 0x00000000);
		cpu.setGpr(Mips.GPR_A0, BASE_ADDRESS + 14);
		cpu.setGpr(Mips.GPR_V0, 0x11223344);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x22334400, cpu.read32(BASE_ADDRESS + 12));

	}
}
