package jmips.serial;

import java.util.concurrent.ConcurrentLinkedQueue;

public class DummyTTY implements TTY {
	private final ConcurrentLinkedQueue<Byte> outputBytes = new ConcurrentLinkedQueue<Byte>();
	private final ConcurrentLinkedQueue<Byte> inputBytes = new ConcurrentLinkedQueue<Byte>();

	@Override
	public void write(byte b) {
		outputBytes.add(b);
	}

	public byte[] getOutputBytes() {
		Object[] array = outputBytes.toArray();
		byte [] out = new byte[array.length];
		for(int i = 0; i < array.length; i++) {
			out[i] = (Byte) array[i];
		}
		return out;
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
