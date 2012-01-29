package jmips.cpu;

public final class Ram extends Device {
	/* Generated serialVersionUID */
	private static final long serialVersionUID = -7526192275199175167L;

	private final int[] memory;
	private final int size;

	public Ram(int mappedOffset, int size) {
		super(mappedOffset);
		this.size = size & (~3);
		memory = new int[this.size >> 2];
	}

	@Override
	public String getDeviceName() {
		return "ram";
	}

	@Override
	public int getDeviceSize() {
		return this.size;
	}

	@Override
	public int read32(int offset, boolean bigEndian) {
		if (bigEndian) {
			return memory[offset >> 2];
		} else {
			return Utils.byteSwap(memory[offset >> 2]);
		}
	}

	@Override
	public void write32(int offset, int value, boolean bigEndian) {
		if (bigEndian) {
			memory[offset >> 2] = value;
		} else {
			memory[offset >> 2] = Utils.byteSwap(value);
		}
	}

	@Override
	public short read16(int offset, boolean bigEndian) {
		int block = offset >> 2;
		int portion = 2 - (offset & 2);
		int val = memory[block];
		int v1, v2;

		v1 = (val >> (8 * portion)) & 0xFF;
		v2 = (val >> (8 + 8 * portion)) & 0xFF;

		if (bigEndian) {
			return ((short) (v2 << 8 | v1));
		} else {
			return ((short) (v1 << 8 | v2));
		}
	}

	@Override
	public void write16(int offset, short value, boolean bigEndian) {
		int block = offset >> 2;
		int portion = 2 - (offset & 2);
		int val = memory[block];
		int s = ((int) value) & 0xFFFF;

		if (bigEndian) {
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

	@Override
	public byte read8(int offset) {
		int block = offset >> 2;
		int portion = 3 - (offset & 3);
		int val = memory[block];
		return (byte) ((val >> (8 * portion)) & 0xFF);
	}

	@Override
	public void write8(int offset, byte value) {
		int block = offset >> 2;
		int portion = 3 - (offset & 3);
		int val = memory[block];
		int b = ((int) value) & 0xFF;

		portion *=8;
		val = (val & ~(0xFF << portion)) | (b << portion);
		memory[block] = val;
	}

	@Override
	public void reset() {
	}
}
