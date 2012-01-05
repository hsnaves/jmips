package jmips.cpu;

public final class CpuState {
	public final int[] gpr = new int[32];
	public int hi, lo;
	public int pc, next_pc;

	public boolean memory_ok;

	public byte read8(int address) {
		return 0;
	}

	public short read16(int address) {
		return 0;
	}

	public int read32(int address) {
		return 0;
	}

	public void write8(int address, byte value) {
	}

	public void write16(int address, short value) {
	}

	public void write32(int address, int value) {
	}

}
