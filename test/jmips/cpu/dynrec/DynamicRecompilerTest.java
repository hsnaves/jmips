package jmips.cpu.dynrec;

import static jmips.cpu.Mips.*;
import static org.junit.Assert.*;
import jmips.cpu.Cpu;

import org.junit.Test;

public class DynamicRecompilerTest {
	private static final int BASE_ADDRESS = 0x80100000;

	private Cpu createCpu() {
		Cpu cpu = new Cpu(4 * 1024 * 1024, null);
		reset(cpu);
		return cpu;
	}

	private void reset(Cpu cpu) {
		cpu.reset();
		cpu.setPc(BASE_ADDRESS);
	}

	@Test
	public void testRecompile() {
		Cpu cpu = createCpu();
		DynamicRecompiler dynamicRecompiler = new DynamicRecompiler();
		reset(cpu);

		cpu.store32(BASE_ADDRESS, ENCODE_ADDIU(GPR_A1, GPR_A1, 4));
		RecompiledBlock block = dynamicRecompiler.recompile(cpu, BASE_ADDRESS & 0x1FFFFFFF);
		assertNotNull(block);
		cpu.setGpr(GPR_A1, 4);
		block.execute(cpu, 1);
		assertEquals(8, cpu.getGpr(GPR_A1));
	}

}
