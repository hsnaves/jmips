package jmips.dev;

import jmips.cpu.Device;

public class RealTimeClock implements Device {
	private boolean error;

	public RealTimeClock() {
	}

	@Override
	public int read32(int offset, boolean bigEndian) {
		error = true;
		return 0;
	}

	@Override
	public void write32(int offset, int value, boolean bigEndian) {
		error = true;
	}

	@Override
	public short read16(int offset, boolean bigEndian) {
		error = true;
		return 0;
	}

	@Override
	public void write16(int offset, short value, boolean bigEndian) {
		error = true;
	}

	@Override
	public byte read8(int offset) {
		error = false;
		return 0;
	}

	@Override
	public void write8(int offset, byte value) {
		error = false;
	}

	@Override
	public void reset() {
	}

	@Override
	public boolean ioError() {
		return error;
	}

}
