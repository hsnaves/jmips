package jmips.cpu.dynrec;

import jmips.cpu.Cpu;

public abstract class RecompiledBlock {
	public abstract int execute(Cpu cpu, int numCycles);
}
