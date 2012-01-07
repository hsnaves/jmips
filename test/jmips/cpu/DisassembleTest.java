package jmips.cpu;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import org.junit.Test;

public class DisassembleTest {

	private void disassemble(String fileName) throws Exception {
		int[] opcodes = Utils.readFile(new FileInputStream("asm/ " + fileName), true);
		System.out.println(fileName);
		int pc = 0x08000000;
		for(int i = 0; i < opcodes.length; i++) {
			String line = Disassemble.disassemble(pc, opcodes[i]);
			assertNotNull(line);
			System.out.println(line);
			pc += 4;
		}
		System.out.println();
	}

	@Test
	public void testDisassemblePrimes() throws Exception {
		disassemble("primes.bin");
	}

	@Test
	public void testDisassembleSha1() throws Exception {
		disassemble("sha1.bin");
	}

}
