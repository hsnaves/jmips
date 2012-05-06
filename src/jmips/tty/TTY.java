package jmips.tty;

public interface TTY {
	public void write(byte b);
	public boolean available(); // Should be thread-safe
	public byte read(); // Should be thread-safe
	public void reset();
}
