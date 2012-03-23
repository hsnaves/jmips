package jmips.cpu;

public interface Device {

	public int read32(int offset, boolean bigEndian);
	public void write32(int offset, int value, boolean bigEndian);
	public short read16(int offset, boolean bigEndian);
	public void write16(int offset, short value, boolean bigEndian);
	public byte read8(int offset);
	public void write8(int offset, byte value);

	public boolean ioError();
	public void reset();
}
