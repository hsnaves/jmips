package jmips.tty;

public interface VirtualTerminalHandler {
	public void bell(VirtualTerminal terminal);
	public void changeName(VirtualTerminal terminal, String name, int type);
}
