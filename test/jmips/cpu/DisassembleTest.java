package jmips.cpu;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import org.junit.Test;

public class DisassembleTest {

	private void disassemble(String fileName) throws Exception {
		byte[] data = Utils.readFile(new FileInputStream("asm/" + fileName));
		System.out.println(fileName);
		int pc = 0x08000000;
		for(int i = 0; i < data.length / 4; i++) {
			int opcode = data[4 * i] << 24;
			opcode |= (data[4 * i + 1] & 0xFF) << 16;
			opcode |= (data[4 * i + 2] & 0xFF) << 8;
			opcode |= data[4 * i + 3] & 0xFF;

			String line = Disassemble.disassemble(pc, opcode);
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
