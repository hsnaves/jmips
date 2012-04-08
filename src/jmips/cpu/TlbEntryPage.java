package jmips.cpu;

import static jmips.cpu.Mips.*;

public final class TlbEntryPage {
	private int PFN;
	private int cacheability;
	private boolean dirty;
	private boolean valid;

	public int getPFN() {
		return PFN;
	}

	public void setPFN(int pFN) {
		PFN = pFN;
	}

	public int getCacheability() {
		return cacheability;
	}

	public void setCacheability(int cacheability) {
		this.cacheability = cacheability;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public int convertPageToEntryLo(boolean global) {
		int entryLo = getPFN() >>> 6;
		entryLo |= getCacheability() << ENTRYLO_COHERENCY_SHIFT;
		if (isDirty()) entryLo |= ENTRYLO_DIRTY;
		if (isValid()) entryLo |= ENTRYLO_VALID;
		if (global) entryLo |= ENTRYLO_GLOBAL;
		return entryLo;
	}

	public void configurePageFromEntryLo(int entryLo) {
		setPFN((entryLo << 6) & 0xFFFFF000);
		setCacheability((entryLo & ENTRYLO_COHERENCY_MASK) >> ENTRYLO_COHERENCY_SHIFT);
		setDirty((entryLo & ENTRYLO_DIRTY) != 0);
		setValid((entryLo & ENTRYLO_VALID) != 0);
	}


	@Override
	public String toString() {
		return String.format("PFN: %08X Cacheability: %d %s %s", PFN,
		                     cacheability, dirty ? "DT" : "ND", valid ? "VL" : "NV");
	}

}
