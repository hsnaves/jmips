package jmips.dev;

import jmips.cpu.Device;

public class RealTimeClock extends Device {

	public RealTimeClock(int mappedOffset) {
		super(mappedOffset);
	}

	@Override
	public int getDeviceSize() {
		return 2;
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
		return 0;
	}

	@Override
	public void write8(int offset, byte value) {
	}

	@Override
	public void reset() {
	}

}
