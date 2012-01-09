package jmips.cpu;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import jmips.serial.ConsoleTTY;
import jmips.dev.Uart16550;

import org.junit.Test;

public class InterpreterTest {

	private void load(CpuState cpu, String fileName) throws Exception {
		int[] opcodes = Utils.readFile(new FileInputStream("asm/" + fileName), true);
		int pc = 0x08000000;
		for(int i = 0; i < opcodes.length; i++) {
			cpu.write32(pc, opcodes[i]);
			pc += 4;
		}
	}

	@Test
	public void test() throws Exception {
		CpuState cpu = new CpuState(0x08000000, 64 * 1024 * 1024);
		Uart16550 uart = new Uart16550(cpu, new ConsoleTTY(), 1);

		cpu.registerDevice(uart, 0x30000000);
		cpu.reset();

		load(cpu, "sum.bin");

		System.out.println("Running sum.bin ...");
		for(int i = 0; i < 41000000; i++) {
			//System.out.println(Disassemble.disassemble(cpu.pc, cpu.read32(cpu.pc)));
			Interpreter.step(cpu);
			//System.out.println(String.format("sp = 0x%08X", cpu.gpr[29]));
		}
	}

}
