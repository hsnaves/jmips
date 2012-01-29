package jmips.cpu;

import static org.junit.Assert.*;

import jmips.serial.ConsoleTTY;
import jmips.dev.Uart16550;

import org.junit.Test;

public class CpuTest {

	private static final int RAM_BASE = 0x00000000;
	private static final int RAM_SIZE = 64 * 1024 * 1024;
	private static final int UART_BASE = 0x30000000;
	private static final int BASE_ADDRESS = 0xA0000000;

	private Cpu createCpu() {
		Cpu cpu = new Cpu(RAM_BASE, RAM_SIZE);
		Uart16550 uart = new Uart16550(UART_BASE, cpu, new ConsoleTTY(), 1);

		cpu.getMemoryManager().registerDevice(uart);
		return cpu;
	}

	@Test
	public void testADD() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x00851020); // add $v0,$a0,$a1
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_A1, 2);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(3, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_A1, 2);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		// TODO
		//assertEquals(BASE_ADDRESS + 4, cpu.pc);
		//assertEquals(BASE_ADDRESS + 8, cpu.next_pc);
		assertEquals(0, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testADDI() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x20820003); // addi $v0,$a0,3
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(4, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		// TODO
		//assertEquals(BASE_ADDRESS + 4, cpu.pc);
		//assertEquals(BASE_ADDRESS + 8, cpu.next_pc);
		assertEquals(0, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x2082FFFE); // addi $v0,$a0,-2
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(-1, cpu.getGpr(Cpu.GPR_V0));

	
		cpu.reset();
		cpu.setGpr(Cpu.GPR_A0, 0x80000000);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		// TODO
		//assertEquals(BASE_ADDRESS + 4, cpu.pc);
		//assertEquals(BASE_ADDRESS + 8, cpu.next_pc);
		assertEquals(0, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testADDIU() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x24820004); //  addiu $v0,$a0,4
		cpu.setGpr(Cpu.GPR_A0, 2);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(6, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals((int) 0x80000003, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x2482FFFD); // addiu $v0,$a0,-3
		cpu.setGpr(Cpu.GPR_A0, 5);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(2, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.setGpr(Cpu.GPR_A0, 0x80000000);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x7FFFFFFD, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testADDU() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x00851021); // addu $v0,$a0,$a1
		cpu.setGpr(Cpu.GPR_A0, 1);
		cpu.setGpr(Cpu.GPR_A1, 10);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(11, cpu.getGpr(Cpu.GPR_V0));

		cpu.reset();
		cpu.setGpr(Cpu.GPR_A0, 0x7FFFFFFF);
		cpu.setGpr(Cpu.GPR_A1, 3);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals((int) 0x80000002, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testAND() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x00851024); // and $v0,$a0,$a1
		cpu.setGpr(Cpu.GPR_A0, 0xFF00FF00);
		cpu.setGpr(Cpu.GPR_A1, 0x12345678);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x12005600, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testANDI() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x30829132); // andi $v0,$a0,0x9132
		cpu.setGpr(Cpu.GPR_A0, 0xFFFFFF00);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x00009100, cpu.getGpr(Cpu.GPR_V0));
	}

	@Test
	public void testLWR() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x98820003); // lwr $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x11223344);
		cpu.write32(BASE_ADDRESS + 12, 0x55667788);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 10);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x00005566, cpu.getGpr(Cpu.GPR_V0));

	}

	@Test
	public void testLWL() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0x88820000); // lwl $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x11223344);
		cpu.write32(BASE_ADDRESS + 12, 0x55667788);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 9);
		cpu.setGpr(Cpu.GPR_V0, 0);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x22334400, cpu.getGpr(Cpu.GPR_V0));

	}

	@Test
	public void testSWL() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0xA8820000); // swl $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x00000000);
		cpu.write32(BASE_ADDRESS + 12, 0x00000000);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 11);
		cpu.setGpr(Cpu.GPR_V0, 0x11223344);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x00000011, cpu.read32(BASE_ADDRESS + 8));

	}

	@Test
	public void testSWR() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(BASE_ADDRESS, 0xB8820000); // swr $v0,0($a0)
		cpu.write32(BASE_ADDRESS + 8, 0x00000000);
		cpu.write32(BASE_ADDRESS + 12, 0x00000000);
		cpu.setGpr(Cpu.GPR_A0, BASE_ADDRESS + 14);
		cpu.setGpr(Cpu.GPR_V0, 0x11223344);
		cpu.step();
		assertEquals(BASE_ADDRESS + 4, cpu.pc);
		assertEquals(BASE_ADDRESS + 8, cpu.nextPc);
		assertEquals(0x22334400, cpu.read32(BASE_ADDRESS + 12));

	}
}
