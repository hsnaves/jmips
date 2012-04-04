package jmips.cpu;

public interface Device {

	public int read32(int offset);
	public void write32(int offset, int value);
	public short read16(int offset);
	public void write16(int offset, short value);
	public byte read8(int offset);
	public void write8(int offset, byte value);

	public boolean ioError();
	public void reset();
}
