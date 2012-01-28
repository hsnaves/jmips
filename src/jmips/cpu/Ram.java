package jmips.cpu;

public final class Ram {
	private final int size;
	private final int[] memory;

	public Ram(int size) {
		this.size = size & (~3); // Always aligned by 4
		memory = new int[this.size >> 2];
	}

	public int read32(int offset, boolean big_endian) {
		if (big_endian) {
			return memory[offset >> 2];
		} else {
			return Utils.byteSwap(memory[offset >> 2]);
		}
	}

	public void write32(int offset, int value, boolean big_endian) {
		if (big_endian) {
			memory[offset >> 2] = value;
		} else {
			memory[offset >> 2] = Utils.byteSwap(value);
		}
	}

	public short read16(int offset, boolean big_endian) {
		int block = offset >> 2;
		int portion = 2 - (offset & 2);
		int val = memory[block];
		int v1, v2;

		v1 = (val >> (8 * portion)) & 0xFF;
		v2 = (val >> (8 + 8 * portion)) & 0xFF;

		if (big_endian) {
			return ((short) (v2 << 8 | v1));
		} else {
			return ((short) (v1 << 8 | v2));
		}
	}

	public void write16(int offset, short value, boolean big_endian) {
		int block = offset >> 2;
		int portion = 2 - (offset & 2);
		int val = memory[block];
		int s = ((int) value) & 0xFFFF;

		if (big_endian) {
			s = ((int) value) & 0xFFFF;
		} else {
			s = (value >> 8) & 0xFF;
			s |= (value << 8) & 0xFF00;
		}

		if (portion == 0) {
			val = (val & 0xFFFF0000) | s;
		} else {
			val = (val & 0x0000FFFF) | (s << 16);
		}
		memory[block] = val;
	}

	public byte read8(int offset) {
		int block = offset >> 2;
		int portion = 3 - (offset & 3);
		int val = memory[block];
		return (byte) ((val >> (8 * portion)) & 0xFF);
	}

	public void write8(int offset, byte value) {
		int block = offset >> 2;
		int portion = 3 - (offset & 3);
		int val = memory[block];
		int b = ((int) value) & 0xFF;

		switch (portion) {
		case 0:
			val = (val & 0xFFFFFF00) | b;
			break;
		case 1:
			val = (val & 0xFFFF00FF) | (b << 8);
			break;
		case 2:
			val = (val & 0xFF00FFFF) | (b << 16);
			break;
		case 3:
			val = (val & 0x00FFFFFF) | (b << 24);
			break;
		}
		memory[block] = val;
	}

	public int size() {
		return size;
	}
}
