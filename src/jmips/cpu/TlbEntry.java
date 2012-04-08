package jmips.cpu;

import static jmips.cpu.Mips.*;

public final class TlbEntry {
	private int pageMask; // Negation of original PageMask and'ed with ENTRYHI_VPN2_MASK
	private int VPN2;     // Masked VPN2
	private int selectionBit;
	private boolean global;
	private int ASID;
	private final TlbEntryPage page0 = new TlbEntryPage();
	private final TlbEntryPage page1 = new TlbEntryPage();
	private boolean initialized;

	public int getPageMask() {
		return pageMask;
	}

	public void setPageMask(int pageMask) {
		this.pageMask = pageMask;
	}

	public int getVPN2() {
		return VPN2;
	}

	public void setVPN2(int vPN2) {
		VPN2 = vPN2;
	}

	public int getSelectionBit() {
		return selectionBit;
	}

	public void setSelectionBit(int selectionBit) {
		this.selectionBit = selectionBit;
	}

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public int getASID() {
		return ASID;
	}

	public void setASID(int aSID) {
		ASID = aSID;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public TlbEntryPage getPage0() {
		return page0;
	}

	public TlbEntryPage getPage1() {
		return page1;
	}

	public TlbEntryPage match(int address, int ASID) {
		if ((getPageMask() & address) == getVPN2()) {
			if (isGlobal() || getASID() == ASID) {
				if (isInitialized()) {
					if ((address & getSelectionBit()) == 0) {
						return getPage0();
					} else {
						return getPage1();
					}
				}
			}
		}
		return null;
	}

	public int tlbPageTranslate(TlbEntryPage tlbEntryPage, int address, boolean write) {
		if (!tlbEntryPage.isValid()) {
			return write ? -MEMORY_ERROR_TLB_INVALID_STORE
					: -MEMORY_ERROR_TLB_INVALID_LOAD;
		}
		if (write && !tlbEntryPage.isDirty()) {
			return -MEMORY_ERROR_TLB_MOD;
		}
		address &= ~(getPageMask() | getSelectionBit());
		address |= tlbEntryPage.getPFN();
		return address;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("PageMask: 0x%08X\n", pageMask));
		sb.append(String.format("VPN2: 0x%08X\n", VPN2));
		sb.append(String.format("Selection Bit: 0x%08X\n", selectionBit));
		sb.append(String.format("ASID: 0x%08X\n", ASID));
		sb.append("Global: ").append(global).append("\n");
		sb.append("Initialized: ").append(initialized).append("\n");
		sb.append("Page 0: ").append(page0.toString()).append("\n");
		sb.append("Page 1: ").append(page1.toString()).append("\n");
		return sb.toString();
	}

}
