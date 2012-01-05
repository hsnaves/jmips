package jmips.cpu;

import static org.junit.Assert.*;

import java.io.FileInputStream;

import org.junit.Test;

public class DisassembleTest {

	private int[] readFile(String fileName, boolean bigEndian) throws Exception {
		FileInputStream fis = new FileInputStream("asm/" + fileName);
		int[] opcodes = new int[fis.available() / 4];
		int i = 0;
		while(fis.available() >= 4) {
			int op;
			if (bigEndian) {
				op = (fis.read() & 0xFF) << 24;
				op |= (fis.read() & 0xFF) << 16;
				op |= (fis.read() & 0xFF) << 8;
				op |= fis.read() & 0xFF;
			} else {
				op = fis.read() & 0xFF;
				op |= (fis.read() & 0xFF) << 8;
				op |= (fis.read() & 0xFF) << 16;
				op |= (fis.read() & 0xFF) << 24;
			}
			opcodes[i++] = op;
		}
		fis.close();
		return opcodes;
	}

	@Test
	public void testDisassemble() throws Exception {
		int[] opcodes = readFile("primes.bin", true);
		int pc = 0x08000000;
		for(int i = 0; i < opcodes.length; i++) {
			String line = Disassemble.disassemble(pc, opcodes[i]);
			assertNotNull(line);
			System.out.println(line);
			pc += 4;
		}
	}

}
