package jmips.dev;

public interface TTY {
	public void write(byte b);
	public boolean available();
	public byte read();
}
