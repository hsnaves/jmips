package jmips.cpu;

public final class Ram implements Device {
	private final int[] memory;
	private final int size;

	public Ram(int size) {
		this.size = size & (~3);
		memory = new int[this.size >> 2];
	}

	public int getRamSize() {
		return size;
	}

	@Override
	public int read32(int offset) {
		return memory[offset >> 2];
	}

	@Override
	public void write32(int offset, int value) {
		memory[offset >> 2] = value;
	}

	@Override
	public short read16(int offset) {
		int block = offset >> 2;
		int portion = 2 - (offset & 2);
		int val = memory[block];
		return (short) (val >>> (8 * portion));
	}

	@Override
	public void write16(int offset, short value) {
		int block = offset >> 2;
		int val = memory[block];
		int s = ((int) value) & 0xFFFF;

		if ((offset & 2) == 2) {
			val = (val & 0xFFFF0000) | s;
		} else {
			val = (val & 0x0000FFFF) | (s << 16);
		}
		memory[block] = val;
	}

	@Override
	public byte read8(int offset) {
		int block = offset >> 2;
		int portion = 3 - (offset & 3);
		int val = memory[block];
		return (byte) (val >>> (8 * portion));
	}

	@Override
	public void write8(int offset, byte value) {
		int block = offset >> 2;
		int portion = 3 - (offset & 3);
		int val = memory[block];
		int b = ((int) value) & 0xFF;

		portion *= 8;
		val = (val & ~(0xFF << portion)) | (b << portion);
		memory[block] = val;
	}

	@Override
	public boolean ioError() {
		return false;
	}

	@Override
	public void reset() {
	}
}
