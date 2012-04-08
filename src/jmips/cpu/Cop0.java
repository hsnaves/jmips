package jmips.cpu;

import jmips.utils.ReadableSeedRandom;
import static jmips.cpu.Mips.*;

public class Cop0 {
	private static final int[] CHANGE_MASKS = new int[] {
		INDEX_MASK,                                     // Index
		0,                                              // Random
		ENTRYLO_GLOBAL | ENTRYLO_VALID | ENTRYLO_DIRTY | ENTRYLO_PFN_MASK | ENTRYLO_COHERENCY_MASK, // EntryLo0
		ENTRYLO_GLOBAL | ENTRYLO_VALID | ENTRYLO_DIRTY | ENTRYLO_PFN_MASK | ENTRYLO_COHERENCY_MASK, // EntryLo1
		CONTEXT_PTE_MASK,                               // Contex
		PAGEMASK_MASK,                                  // PageMask
		WIRED_MASK,                                     // Wired
		-1,                                             // Reserved1
		0,                                              // BadVAddr
		-1,                                             // Count
		ENTRYHI_ASID_MASK | ENTRYHI_VPN2_MASK,          // EntryHi
		-1,                                             // Compare
		STATUS_IE | STATUS_EXL | STATUS_ERL | STATUS_UM | STATUS_INT_MASK | STATUS_NMI | // Status
		STATUS_SR | STATUS_TS | STATUS_BEV | STATUS_RE | STATUS_RP | STATUS_COP_MASK,
		(3 << CAUSE_INTERRUPT_SHIFT) | CAUSE_WP | CAUSE_IV, // Cause
		-1,                                             // EPC
		0,                                              // PRId
		CONFIG_K0_MASK,                                 // Config
		0,                                              // LLAddr
		-1,                                             // WatchLo (we don't care)
		-1,                                             // WatchHi (we don't care)
		-1,                                             // Reserved2
		-1,                                             // Reserved3
		-1,                                             // Reserved4
		-1,                                             // Debug (we don't care)
		-1,                                             // DEPC (we don't care)
		-1,                                             // Reserved5
		-1,                                             // ERRCTRL (we don't care)
		-1,                                             // Reserved6
		-1,                                             // TagLo (we don't care)
		-1,                                             // Reserved7
		-1,                                             // ErrorEPC
		-1,                                             // DESAVE (we don't care)
	};

	private static final int DEFAULT_PRID = 0x00018000; // Revision 1.1
	private static final int DEFAULT_CONFIG = 0x80008082;
	private static final int DEFAULT_CONFIG1 = 0x1E190C80; // cache 2-way, no fpu, no ejtag, no mips16, no watch and no performance counter

	// Coprocessor0 Instance fields
	private int[] regs = new int[32];

	private boolean kernelMode;
	private final TlbEntry[] tlbEntries = new TlbEntry[NUM_TLB_ENTRIES];

	private final ReadableSeedRandom random = new ReadableSeedRandom(0);

	public Cop0() {
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntries[i] = new TlbEntry();
		}
		resetTlb();

		regs[COP0_PRID] = DEFAULT_PRID;
		regs[COP0_CONFIG] = DEFAULT_CONFIG;
	}

	private int readRegisterRandom() {
		return regs[COP0_WIRED] + random.nextRandomInt(NUM_TLB_ENTRIES - regs[COP0_WIRED]);
	}

	public void setRandomSeed(long seed) {
		random.setSeed(seed);
	}

	public long getRangomSeed() {
		return random.getSeed();
	}

	private void updateModes(Cpu cpu, int status) {
		kernelMode = (status & (STATUS_UM | STATUS_EXL | STATUS_ERL)) != STATUS_UM;
		if (!kernelMode) {
			cpu.setBigEndian((status & STATUS_RE) == 0);
		} else {
			cpu.setBigEndian(true);
		}
	}

	private void changeStatus(Cpu cpu, int value, int mask) {
		regs[COP0_STATUS] = Helper.changeMaskedValue(regs[COP0_STATUS], value, mask);
		updateModes(cpu, value);
	}

	public void setCop0Reg(Cpu cpu, int reg, int sel, int value) {
		int mask = CHANGE_MASKS[reg];
		if (mask != 0 && sel == 0) {
			regs[reg] = Helper.changeMaskedValue(regs[reg], value, mask);
			switch(reg) {
			case COP0_COUNT:
				cpu.setCounter(value);
				break;
			case COP0_COMPARE:
				raiseIrq(TIMER_IRQ, false);
				break;
			case COP0_STATUS:
				updateModes(cpu, value);
				break;
			case COP0_CAUSE:
				checkInterrupts(cpu);
				break;
			}
		}
	}

	public int getCop0Reg(Cpu cpu, int reg, int sel) {
		int retval = 0;
		switch(reg) {
		case COP0_RANDOM:
			retval = readRegisterRandom();
			break;
		case COP0_COUNT:
			retval = cpu.getCounter();
			break;
		case COP0_CONFIG:
			if (sel == 0) retval = regs[reg];
			else retval = DEFAULT_CONFIG1;
			break;
		default:
			retval = regs[reg];
			break;
		}
		return retval;
	}

	public int getCompareRegister() {
		return regs[COP0_COMPARE];
	}

	public void setLoadLinkedRegister(int address) {
		int physicalAddress = translate(address, false);
		regs[COP0_LLADDR] = physicalAddress >>> 4;
	}

	public boolean isCoprocessorAvailable(int copno) {
		if (copno == 0 && kernelMode) return true;
		return (regs[COP0_STATUS] & (1 << (STATUS_COP_SHIFT + copno))) != 0;
	}

	public void exceptionReset(Cpu cpu) {
		final int statusSet = STATUS_BEV | STATUS_ERL;
		final int statusMask = STATUS_RP | STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL;
		changeStatus(cpu, statusSet, statusMask);

		regs[COP0_WIRED] = 0;
		regs[COP0_CONFIG] = DEFAULT_CONFIG;
		regs[COP0_ERROREPC] = cpu.exceptionPc();
		cpu.setPc(0xBFC00000);
	}

	public void exceptionSoftReset(Cpu cpu) {
		final int statusSet = STATUS_SR | STATUS_BEV | STATUS_ERL;
		final int statusMask = STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL;
		changeStatus(cpu, statusSet, statusMask);

		regs[COP0_ERROREPC] = cpu.exceptionPc();
		cpu.setPc(0xBFC00000);
	}

	public void exceptionNMI(Cpu cpu) {
		final int statusSet = STATUS_BEV | STATUS_NMI | STATUS_ERL;
		final int statusMask = STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL;
		changeStatus(cpu, statusSet, statusMask);
		regs[COP0_ERROREPC] = cpu.exceptionPc();
		cpu.setPc(0xBFC00000);
	}

	private void exception(Cpu cpu, int code, int copno, boolean offsetToZero) {
		int vectorOffset;

		regs[COP0_CAUSE] &= ~(CAUSE_CE_MASK | CAUSE_EXCCODE_MASK); 
		if ((regs[COP0_STATUS] & STATUS_EXL) == 0) {
			if (cpu.isBranchDelaySlot()) {
				regs[COP0_EPC] = cpu.exceptionPc();
				regs[COP0_CAUSE] |= CAUSE_BD;
			} else {
				regs[COP0_EPC] = cpu.exceptionPc();
				regs[COP0_CAUSE] &= ~CAUSE_BD;
			}
			if (offsetToZero) {
				vectorOffset = 0;
			} else if (code == EXCEPTION_CODE_INT && ((regs[COP0_CAUSE] & CAUSE_IV) != 0)) {
				vectorOffset = 0x200;
			} else {
				vectorOffset = 0x180;
			}
		} else {
			vectorOffset = 0x180;
		}
		regs[COP0_CAUSE] |= (copno << CAUSE_CE_SHIFT) | (code << CAUSE_EXCCODE_SHIFT);
		changeStatus(cpu, STATUS_EXL, STATUS_EXL);

		if ((regs[COP0_STATUS] & STATUS_BEV) != 0) {
			cpu.setPc(0xBFC00200 + vectorOffset);
		} else {
			cpu.setPc(0x80000000 + vectorOffset);
		}
	}

	private void exception(Cpu cpu, int code, int copno) {
		exception(cpu, code, copno, false);
	}

	private void exception(Cpu cpu, int code) {
		exception(cpu, code, 0);
	}

	public void exceptionMachineCheck(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_MCHECK);
		regs[COP0_STATUS] |= STATUS_TS;
	}

	public void exceptionInterrupt(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_INT);
	}

	public void exceptionAddressError(Cpu cpu, int badVAddr, boolean load) {
		exception(cpu, load ? EXCEPTION_CODE_ADEL : EXCEPTION_CODE_ADES);
		regs[COP0_BADVADDR] = badVAddr;
	}

	private void exceptionTlb(Cpu cpu, int code, int badVAddr, boolean offsetToZero) {
		exception(cpu, code, 0, offsetToZero);
		regs[COP0_BADVADDR] = badVAddr;
		regs[COP0_CONTEXT] = (regs[COP0_CONTEXT] & CONTEXT_PTE_MASK) |
				((badVAddr & ENTRYHI_VPN2_MASK) >>> (ENTRYHI_VPN2_SHIFT - CONTEXT_BADVPN2_SHIFT));
		regs[COP0_ENTRYHI] = (regs[COP0_ENTRYHI] & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	public void exceptionTlbRefill(Cpu cpu, int badVAddr, boolean load) {
		exceptionTlb(cpu, load ? EXCEPTION_CODE_TLBL : EXCEPTION_CODE_TLBS, badVAddr, true);
	}

	public void exceptionTlbInvalid(Cpu cpu, int badVAddr, boolean load) {
		exceptionTlb(cpu, load ? EXCEPTION_CODE_TLBL : EXCEPTION_CODE_TLBS, badVAddr, false);
	}

	public void exceptionTlbModified(Cpu cpu, int badVAddr) {
		exceptionTlb(cpu, EXCEPTION_CODE_MOD, badVAddr, false);
	}

	public void exceptionBusError(Cpu cpu, boolean data) {
		exception(cpu, data ? EXCEPTION_CODE_DBE : EXCEPTION_CODE_IBE);
	}

	public void exceptionSyscall(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_SYS);
	}

	public void exceptionBreak(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_BP);
	}

	public void exceptionReserved(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_RI);
	}

	public void exceptionCoprocessorUnusable(Cpu cpu, int copno) {
		exception(cpu, EXCEPTION_CODE_CPU, copno, false);
	}

	public void exceptionIntegerOverflow(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_OV);
	}

	public void exceptionTrap(Cpu cpu) {
		exception(cpu, EXCEPTION_CODE_TR);
	}

	public void returnFromException(Cpu cpu) {
		int pc;
		if ((regs[COP0_STATUS] & STATUS_ERL) != 0) {
			changeStatus(cpu, 0, STATUS_ERL);
			pc = regs[COP0_ERROREPC];
		} else {
			changeStatus(cpu, 0, STATUS_EXL);
			pc = regs[COP0_EPC];
		}
		cpu.setPc(pc);
		cpu.setLoadLinkedStatus(false);
	}

	public void raiseIrq(int irqno, boolean raise) {
		if (raise)
			regs[COP0_CAUSE] |= 1 << (CAUSE_INTERRUPT_SHIFT + irqno);
		else
			regs[COP0_CAUSE] &= ~(1 << (CAUSE_INTERRUPT_SHIFT + irqno));
	}

	public boolean interruptEnabled() {
		return (regs[COP0_STATUS] & (STATUS_IE | STATUS_EXL | STATUS_ERL)) == STATUS_IE;
	}

	public boolean checkInterrupts(Cpu cpu) {
		if (interruptEnabled()) {
			int mask = (regs[Mips.COP0_STATUS] & STATUS_INT_MASK) >>> STATUS_INT_SHIFT;
			int pending = (regs[COP0_CAUSE] & CAUSE_INTERRUPT_MASK) >>> CAUSE_INTERRUPT_SHIFT;
			if ((mask & pending) != 0) {
				exceptionInterrupt(cpu);
				return true;
			}
		}
		return false;
	}

	public void resetTlb() {
		for(int i = 0; i < NUM_TLB_ENTRIES; i++)
			tlbEntries[i].setInitialized(false);
	}

	public int translate(int address, boolean write) {
		if (kernelMode) {
			if ((address & 0xC0000000) == 0x80000000) { // kseg0 or kseg1 
				return address & 0x1FFFFFFF;
			}
		} else { // User Mode
			if ((address & 0x80000000) != 0) {
				return write ? -MEMORY_ERROR_ADDRESS_ERROR_STORE :
				               -MEMORY_ERROR_ADDRESS_ERROR_LOAD;
			}
		}

		final int ASID = regs[COP0_ENTRYHI] & ENTRYHI_ASID_MASK;

		// Perform the TLB search
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			TlbEntry tlbEntry = tlbEntries[idx];
			TlbEntryPage tlbEntryPage = tlbEntry.match(address, ASID); 
			if (tlbEntryPage != null) {
				return tlbEntry.tlbPageTranslate(tlbEntryPage, address, write);
			}
		}

		if ((regs[Mips.COP0_STATUS] & STATUS_EXL) != 0) {
			return write ? -MEMORY_ERROR_TLB_INVALID_STORE :
			               -MEMORY_ERROR_TLB_INVALID_LOAD;
		} else {
			return write ? -MEMORY_ERROR_TLB_REFILL_STORE :
			               -MEMORY_ERROR_TLB_REFILL_LOAD;
		}
	}


	public void tlbProbe() {
		int ASID = regs[COP0_ENTRYHI] & ENTRYHI_ASID_MASK;
		int VPN2 = regs[COP0_ENTRYHI] & ENTRYHI_VPN2_MASK;
		regs[COP0_INDEX] = INDEX_PROBE;
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			TlbEntry tlbEntry = tlbEntries[idx];
			TlbEntryPage tlbEntryPage = tlbEntry.match(VPN2, ASID);;

			if (tlbEntryPage != null) {
				regs[COP0_INDEX] = idx;
				return;
			}
		}
	}

	public void tlbRead() {
		TlbEntry entry = tlbEntries[regs[COP0_INDEX] & INDEX_MASK];
		regs[COP0_PAGEMASK] = (~entry.getPageMask()) & ENTRYHI_VPN2_MASK;
		regs[COP0_ENTRYHI] = entry.getVPN2() | entry.getASID();
		regs[COP0_ENTRYLO0] = entry.getPage0().convertPageToEntryLo(entry.isGlobal());
		regs[COP0_ENTRYLO1] = entry.getPage1().convertPageToEntryLo(entry.isGlobal());
	}

	public void tlbWrite(Cpu cpu, int index) {
		TlbEntry tlbEntry;
		int mask = (~regs[COP0_PAGEMASK]) & ENTRYHI_VPN2_MASK;
		int VPN2 = regs[COP0_ENTRYHI] & mask;
		int ASID = regs[COP0_ENTRYHI] & ENTRYHI_ASID_MASK;
		boolean global = ((regs[COP0_ENTRYLO0] & ENTRYLO_GLOBAL) != 0) &&
		                 ((regs[COP0_ENTRYLO1] & ENTRYLO_GLOBAL) != 0);

		// Checks for multiple entries
		for (int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			tlbEntry = tlbEntries[idx];
			if (!tlbEntry.isInitialized() || idx == index) continue;
			if ((tlbEntry.getVPN2() & mask) == VPN2) {
				if (global || tlbEntry.isGlobal() || tlbEntry.getASID() == ASID) {
					exceptionMachineCheck(cpu);
					return;
				}
			}
		}

		tlbEntry = tlbEntries[index];
		tlbEntry.setInitialized(true);
		tlbEntry.setPageMask(mask);
		tlbEntry.setVPN2(VPN2);
		tlbEntry.setASID(ASID);
		tlbEntry.setGlobal(global);
		tlbEntry.setSelectionBit(mask ^ (mask >> 1));
		tlbEntry.getPage0().configurePageFromEntryLo(regs[COP0_ENTRYLO0]);
		tlbEntry.getPage1().configurePageFromEntryLo(regs[COP0_ENTRYLO1]);
	}

	public void tlbWriteRandom(Cpu cpu) {
		tlbWrite(cpu, readRegisterRandom());
	}

	public void tlbWriteIndex(Cpu cpu) {
		if ((regs[COP0_INDEX] & INDEX_PROBE) == 0)
			tlbWrite(cpu, regs[COP0_INDEX] & INDEX_MASK);
		else
			tlbWriteRandom(cpu);
	}
}
