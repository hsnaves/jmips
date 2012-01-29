package jmips.serial;

public interface TTY {
	public void write(char c);
	public boolean available();
	public char read();
	public void reset();
}
