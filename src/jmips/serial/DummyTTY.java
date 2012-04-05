package jmips.serial;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DummyTTY implements TTY {
	private ConcurrentLinkedQueue<Byte> outputBytes;
	private ConcurrentLinkedQueue<Byte> inputBytes;

	@Override
	public void write(byte b) {
		outputBytes.add(b);
	}

	public Byte[] getOutputBytes() {
		Byte []r = null;
		return outputBytes.toArray(r);
	}

	@Override
	public boolean available() {
		return !inputBytes.isEmpty();
	}

	@Override
	public byte read() {
		return inputBytes.remove();
	}

	public void addInputByte(byte b) {
		inputBytes.add(b);
	}

	@Override
	public void reset() {
		inputBytes.clear();
		outputBytes.clear();
	}
}
