package jmips.dev;

public interface UartController {
	public void changeIrqStatus(boolean raise);
	public void write(byte b);
}
