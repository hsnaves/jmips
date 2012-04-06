package jmips.cpu;

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

	@Override
	public String toString() {
		return String.format("PFN: %08X Cacheability: %d %s %s", PFN,
		                     cacheability, dirty ? "DT" : "ND", valid ? "VL" : "NV");
	}

}
