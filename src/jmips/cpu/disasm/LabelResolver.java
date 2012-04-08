package jmips.cpu.disasm;

public interface LabelResolver {
	public int resolveSymbolByName(String name);
	public String resolveSymbolByAddress(int address);
	public int findNearestSymbolAddress(int address);
}
