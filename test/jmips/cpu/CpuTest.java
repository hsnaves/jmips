package jmips.cpu;

import static org.junit.Assert.*;

import jmips.MipsSystem;
import jmips.serial.DummyTTY;

import org.junit.Test;

public class CpuTest {
	private static final int BASE_ADDRESS = 0x80100000;

	private Cpu createCpu() {
		MipsSystem system = new MipsSystem(new DummyTTY());
		return system.getCpu();
	}

	private void reset(Cpu cpu) {
		cpu.reset();
		cpu.setPc(BASE_ADDRESS);
	}

	@Test
	public void testADD() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x00851020); // add $v0,$a0,$a1
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_A1, 2);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(3, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_A1, 2);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(0, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testADDI() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x20820003); // addi $v0,$a0,3
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(4, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(0, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x2082FFFE); // addi $v0,$a0,-2
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(-1, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.setGpr(Cpu.GPR_A0, 0x80000000);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(0, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testADDIU() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x24820004); //  addiu $v0,$a0,4
		cpu.setGpr(Cpu.GPR_A0, 2);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(6, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals((int) 0x80000003, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x2482FFFD); // addiu $v0,$a0,-3
		cpu.setGpr(Cpu.GPR_A0, 5);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(2, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.setGpr(Cpu.GPR_A0, 0x80000000);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x7FFFFFFD, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testADDU() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x00851021); // addu $v0,$a0,$a1
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_A1, 10);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(11, cpu.getGpr(Cpu.GPR_V0));

		reset(cpu);
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_A1, 3);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals((int) 0x80000002, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testAND() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x00851024); // and $v0,$a0,$a1
		cpu.setGpr(Cpu.GPR_A0, 0xFF00FF00);
		cpu.setGpr(Cpu.GPR_A1, 0x12345678);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x12005600, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testANDI() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x30829132); // andi $v0,$a0,0x9132
		cpu.setGpr(Cpu.GPR_A0, 0xFFFFFF00);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x00009100, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testLWR() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x98820003); // lwr $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x11223344);
		cpu.write32(BASE_ADDRESS + 12, 0x55667788);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 10);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x00005566, cpu.getGpr(Cpu.GPR_V0));

	}

	@Test
	public void testLWL() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0x88820000); // lwl $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x11223344);
		cpu.write32(BASE_ADDRESS + 12, 0x55667788);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 9);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x22334400, cpu.getGpr(Cpu.GPR_V0));

	}

	@Test
	public void testSWL() {
		Cpu cpu = createCpu();

		reset(cpu);
		cpu.write32(BASE_ADDRESS, 0xA8820000); // swl $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x00000000);
		cpu.write32(BASE_ADDRESS + 12, 0x00000000);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 11);
		cpu.setGpr(Cpu.GPR_V0, 0x11223344);
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
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 14);
		cpu.setGpr(Cpu.GPR_V0, 0x11223344);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.getPc());
		assertFalse(cpu.isBranchDelaySlot());
		assertEquals(0x22334400, cpu.read32(BASE_ADDRESS + 12));

	}
}
