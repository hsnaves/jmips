package jmips.cpu;

public interface IODevice {
	public int read32(int offset, boolean big_endian);
	public void write32(int offset, int value, boolean big_endian);
	public short read16(int offset, boolean big_endian);
	public void write16(int offset, short value, boolean big_endian);
	public byte read8(int offset);
	public void write8(int offset, byte value);
	public boolean bus_error();
	public int size();
}
