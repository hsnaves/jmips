package jmips.cpu;

import static org.junit.Assert.*;

import jmips.serial.ConsoleTTY;
import jmips.dev.Uart16550;

import org.junit.Test;

public class CpuTest {

	private static final int RAM_BASE = 0x08000000;
	private static final int RAM_SIZE = 64 * 1024 * 1024;
	private static final int UART_BASE = 0x30000000;

	private Cpu createCpu() {
		Cpu cpu = new Cpu(RAM_BASE, RAM_SIZE);
		Uart16550 uart = new Uart16550(cpu, new ConsoleTTY(), 1);

		cpu.registerDevice(uart, UART_BASE);
		return cpu;
	}

	@Test
	public void testADD() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(RAM_BASE, 0x00851020); // add $v0,$a0,$a1
		cpu.gpr[Cpu.GPR_A0] = 1;
		cpu.gpr[Cpu.GPR_A1] = 2;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(3, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.gpr[Cpu.GPR_A0] = 0x7FFFFFFF;
		cpu.gpr[Cpu.GPR_A1] = 2;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		// TODO
		//assertEquals(RAM_BASE + 4, cpu.pc);
		//assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(0, cpu.gpr[Cpu.GPR_V0]);
	}

	@Test
	public void testADDI() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(RAM_BASE, 0x20820003); // addi $v0,$a0,3
		cpu.gpr[Cpu.GPR_A0] = 1;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(4, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.gpr[Cpu.GPR_A0] = 0x7FFFFFFF;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		// TODO
		//assertEquals(RAM_BASE + 4, cpu.pc);
		//assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(0, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.write32(RAM_BASE, 0x2082FFFE); // addi $v0,$a0,-2
		cpu.gpr[Cpu.GPR_A0] = 1;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(-1, cpu.gpr[Cpu.GPR_V0]);

	
		cpu.reset();
		cpu.gpr[Cpu.GPR_A0] = 0x80000000;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		// TODO
		//assertEquals(RAM_BASE + 4, cpu.pc);
		//assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(0, cpu.gpr[Cpu.GPR_V0]);
	}

	@Test
	public void testADDIU() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(RAM_BASE, 0x24820004); //  addiu $v0,$a0,4
		cpu.gpr[Cpu.GPR_A0] = 2;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(6, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.gpr[Cpu.GPR_A0] = 0x7FFFFFFF;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals((int) 0x80000003, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.write32(RAM_BASE, 0x2482FFFD); // addiu $v0,$a0,-3
		cpu.gpr[Cpu.GPR_A0] = 5;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(2, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.gpr[Cpu.GPR_A0] = 0x80000000;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(0x7FFFFFFD, cpu.gpr[Cpu.GPR_V0]);
	}

	@Test
	public void testADDU() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(RAM_BASE, 0x00851021); // addu $v0,$a0,$a1
		cpu.gpr[Cpu.GPR_A0] = 1;
		cpu.gpr[Cpu.GPR_A1] = 10;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(11, cpu.gpr[Cpu.GPR_V0]);

		cpu.reset();
		cpu.gpr[Cpu.GPR_A0] = 0x7FFFFFFF;
		cpu.gpr[Cpu.GPR_A1] = 3;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals((int) 0x80000002, cpu.gpr[Cpu.GPR_V0]);
	}

	@Test
	public void testAND() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(RAM_BASE, 0x00851024); // and $v0,$a0,$a1
		cpu.gpr[Cpu.GPR_A0] = 0xFF00FF00;
		cpu.gpr[Cpu.GPR_A1] = 0x12345678;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(0x12005600, cpu.gpr[Cpu.GPR_V0]);
	}

	@Test
	public void testANDI() {
		Cpu cpu = createCpu();

		cpu.reset();
		cpu.write32(RAM_BASE, 0x30829132); // andi $v0,$a0,0x9132
		cpu.gpr[Cpu.GPR_A0] = 0xFFFFFF00;
		cpu.gpr[Cpu.GPR_V0] = 0;
		cpu.step();
		assertEquals(RAM_BASE + 4, cpu.pc);
		assertEquals(RAM_BASE + 8, cpu.next_pc);
		assertEquals(0x00009100, cpu.gpr[Cpu.GPR_V0]);
	}

}
