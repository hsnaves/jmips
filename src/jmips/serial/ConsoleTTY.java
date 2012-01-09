package jmips.serial;

import java.io.IOException;


public class ConsoleTTY implements TTY {


	@Override
	public void write(byte b) {
		System.out.print((char) b);
	}

	@Override
	public boolean available() {
		try {
			return System.in.available() > 0;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public byte read() {
		try {
			return (byte) System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}

}
