package jmips.cpu;

import java.util.Random;

public final class Coprocessor0 {
	public static final int NUM_TLB_ENTRIES = 16;

	public static final int COP0_REG_INDEX = 0;            // Index into TLB array
	public static final int COP0_REG_RANDOM = 1;           // Random generated index into the TLB array
	public static final int COP0_REG_ENTRYLO0 = 2;         // Low order portion of the TLB entry for even-numbered virtual pages
	public static final int COP0_REG_ENTRYLO1 = 3;         // Low order portion of the TLB entry for odd-numbered virtual pages
	public static final int COP0_REG_CONTEXT = 4;          // Pointer to page table entry in memory
	public static final int COP0_REG_PAGEMASK = 5;         // Controls the page sizes in TLB entries
	public static final int COP0_REG_WIRED = 6;            // Controls the number of fixed TLB entries
	public static final int COP0_REG_RESERVED1 = 7;        // Reserved
	public static final int COP0_REG_BADVADDR = 8;         // Reports the address for the most recent address-related exception
	public static final int COP0_REG_COUNT = 9;            // Processor cycle count
	public static final int COP0_REG_ENTRYHI = 10;         // High-order portion of the TLB entry
	public static final int COP0_REG_COMPARE = 11;         // Timer interrupt control
	public static final int COP0_REG_STATUS = 12;          // Processor status and control
	public static final int COP0_REG_CAUSE = 13;           // Cause of last exception
	public static final int COP0_REG_EPC = 14;             // Program counter at last exception
	public static final int COP0_REG_PRID = 15;            // Processor identification and revision
	public static final int COP0_REG_CONFIG = 16;          // Configuration registers
	public static final int COP0_REG_LLADDR = 17;          // Load linked address
	public static final int COP0_REG_WATCHLO = 18;         // Watchpoint address(low order)
	public static final int COP0_REG_WATCHHI = 19;         // Watchpoint address(high order) and mask
	public static final int COP0_REG_RESERVED2 = 20;       // Reserved
	public static final int COP0_REG_RESERVED3 = 21;       // Reserved
	public static final int COP0_REG_RESERVED4 = 22;       // Reserved
	public static final int COP0_REG_DEBUG = 23;           // Debug control and exception status
	public static final int COP0_REG_DEPC = 24;            // Program counter at last debug exception
	public static final int COP0_REG_RESERVED5 = 25;       // Reserved
	public static final int COP0_REG_ERRCTRL = 26;         // Control access to data and SPRAM arrays for CACHE instruction
	public static final int COP0_REG_RESERVED6 = 27;       // Reserved
	public static final int COP0_REG_TAGLO = 28;           // Low-order portion of cache tag interface
	public static final int COP0_REG_RESERVED7 = 29;       // Reserved
	public static final int COP0_REG_ERROREPC = 30;        // Program counter at last error
	public static final int COP0_REG_DESAVE = 31;          // Debug handler scratchpad register

	/* Some mask to bits inside the registers */
	private static final int INDEX_MASK  = 0x0000000F;
	private static final int INDEX_PROBE = 0x80000000;

	private static final int ENTRYLO_GLOBAL    = 0x00000001;
	private static final int ENTRYLO_VALID     = 0x00000002;
	private static final int ENTRYLO_DIRTY     = 0x00000004;
	private static final int ENTRYLO_PFN_MASK  = 0x03FFFFC0;
	private static final int ENTRYLO_COHERENCY_MASK  = 0x00000038;
	private static final int ENTRYLO_COHERENCY_SHIFT = 3;
	private static final int ENTRYLO_WRITE_MASK = ENTRYLO_GLOBAL | ENTRYLO_VALID | ENTRYLO_DIRTY | ENTRYLO_PFN_MASK | ENTRYLO_COHERENCY_MASK;

	private static final int ENTRYHI_ASID_MASK = 0x000000FF;
	private static final int ENTRYHI_VPN2_MASK = 0xFFFFE000;
	private static final int ENTRYHI_WRITE_MASK = ENTRYHI_ASID_MASK | ENTRYHI_VPN2_MASK;

	private static final int STATUS_IE  = 0x00000001;
	private static final int STATUS_EXL = 0x00000002;
	private static final int STATUS_ERL = 0x00000004;
	private static final int STATUS_UM  = 0x00000010;
	private static final int STATUS_INT_MASK  = 0x0000FF00;
	private static final int STATUS_INT_SHIFT = 8;
	private static final int STATUS_NMI = 0x00080000;
	private static final int STATUS_SR  = 0x00100000;
	private static final int STATUS_TS  = 0x00200000;
	private static final int STATUS_BEV = 0x00400000;
	private static final int STATUS_RE  = 0x02000000;
	private static final int STATUS_RP  = 0x08000000;
	private static final int STATUS_COP_MASK  = 0xF0000000;
	private static final int STATUS_COP_SHIFT = 28;
	private static final int STATUS_WRITE_MASK = STATUS_IE | STATUS_EXL | STATUS_ERL | STATUS_UM | STATUS_INT_MASK | STATUS_NMI | STATUS_SR | STATUS_TS | STATUS_BEV | STATUS_RE | STATUS_RP | STATUS_COP_MASK;

	private static final int CAUSE_EXCCODE_MASK  = 0x0000007C;
	private static final int CAUSE_EXCCODE_SHIFT = 2;
	private static final int CAUSE_INTERRUPT_MASK = 0x0000FF00;
	private static final int CAUSE_INTERRUPT_SHIFT = 8;
	private static final int CAUSE_WP = 0x00400000;
	private static final int CAUSE_IV = 0x00800000;
	private static final int CAUSE_CE_MASK = 0x30000000;
	private static final int CAUSE_CE_SHIFT = 28;
	private static final int CAUSE_BD = 0x80000000;
	private static final int CAUSE_WRITE_MASK = (3 << CAUSE_INTERRUPT_SHIFT) | CAUSE_WP | CAUSE_IV;

	private static final int CONTEXT_PTE_MASK = 0xFF800000;
	private static final int CONTEXT_WRITE_MASK = CONTEXT_PTE_MASK;

	public static final int EXCEPTION_CODE_INT = 0;        // Interrupt
	public static final int EXCEPTION_CODE_MOD = 1;        // TLB modification exception
	public static final int EXCEPTION_CODE_TLBL = 2;       // TLB exception (load or instruction fetch)
	public static final int EXCEPTION_CODE_TLBS = 3;       // TLB exception (store)
	public static final int EXCEPTION_CODE_ADEL = 4;       // Address error exception (load or instruction fetch)
	public static final int EXCEPTION_CODE_ADES = 5;       // Address error exception (store)
	public static final int EXCEPTION_CODE_IBE = 6;        // Bus Error exception (instruction fetch)
	public static final int EXCEPTION_CODE_DBE = 7;        // Bus Error exception (data reference: load or store)
	public static final int EXCEPTION_CODE_SYS = 8;        // Syscall exception
	public static final int EXCEPTION_CODE_BP = 9;         // Breakpoint exception
	public static final int EXCEPTION_CODE_RI = 10;        // Reserved Instruction exception
	public static final int EXCEPTION_CODE_CPU = 11;       // Coprocessor Unusable exception
	public static final int EXCEPTION_CODE_OV = 12;        // Overflow exception
	public static final int EXCEPTION_CODE_TR = 13;        // Trap exception
	public static final int EXCEPTION_CODE_WATCH = 23;     // Refrence to WatchHi/WatchLo address
	public static final int EXCEPTION_CODE_MCHECK = 24;    // Machine check exception

	private static final int TIMER_IRQ = 7;

	//private static final boolean[] CACHEABILITY = {
	//	true, true, false, true, true, true, true, false
	//};

	private int Index;
	private int EntryLo0;
	private int EntryLo1;
	private int Context;
	private int PageMask;
	private int Wired;
	private int Reserved1;
	private int BadVAddr;
	private int EntryHi;
	private int Compare;
	private int Status;
	private int Cause;
	private int EPC;
	private final int PRId = 0x0001800B; // Revision 1.1
	private int Config = 0x80008082;
	private final int Config1 = 0x1E000000; // no cache, no fpu, no ejtag, no mips16, no watch and no performance counter
	private int LLAddr;
	private int WatchLo;
	private int WatchHi;
	private int Reserved2;
	private int Reserved3;
	private int Reserved4;
	private int Debug;
	private int DEPC;
	private int Reserved5;
	private int ErrCtrl;
	private int Reserved6;
	private int TagLo, DataLo;
	private int Reserved7;
	private int ErrorEPC;
	private int DESAVE;

	private boolean kernelMode= true;
	private boolean bigEndian = true;
	private int ASID;

	private boolean translationError;
	private boolean loadLinkedStatus = false;

	private final Random rand = new Random(System.currentTimeMillis());
	private final TlbEntry[] tlbEntries;
	private TlbEntry lastTlbEntryCode;
	private TlbEntry lastTlbEntryData;

	private final Cpu cpu;

	public Coprocessor0(Cpu cpu) {
		this.cpu = cpu;
		tlbEntries = new TlbEntry[NUM_TLB_ENTRIES];
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntries[i] = new TlbEntry();
		}
		lastTlbEntryCode = lastTlbEntryData = tlbEntries[0];
		hardReset();
	}

	public void hardReset() {
		for(int i = 0; i < NUM_TLB_ENTRIES; i++)
			tlbEntries[i].initialized = false;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public boolean isCoprocessorAvailable(int copno) {
		if (copno == 0 && kernelMode) return true;
		return (Status & (1 << (STATUS_COP_SHIFT + copno))) != 0;
	}

	private int readRegisterRandom() {
		return Wired + rand.nextInt(NUM_TLB_ENTRIES - Wired);
	}

	private static int changeValue(int oldValue, int newValue, int mask) {
		return (oldValue & ~ mask) | (newValue & mask);
	}

	private void writeStatus(int value) {
		Status = value & STATUS_WRITE_MASK;
		kernelMode = (value & (STATUS_UM | STATUS_EXL | STATUS_ERL)) != STATUS_UM;
		if (!kernelMode) {
			bigEndian = (value & STATUS_RE) == 0; 
		} else {
			bigEndian = true;
		}
	}

	public void moveToCoprocessor(int reg, int sel, int value) {
		switch(reg) {
		case COP0_REG_INDEX: // Index
			Index = changeValue(Index, value, INDEX_MASK);
			break;
		case COP0_REG_RANDOM: // Random
			// Ignore random writes
			break;
		case COP0_REG_ENTRYLO0: // EntryLo0
			EntryLo0 = value & ENTRYLO_WRITE_MASK;
			break;
		case COP0_REG_ENTRYLO1: // EntryLo1
			EntryLo1 = value & ENTRYLO_WRITE_MASK;
			break;
		case COP0_REG_CONTEXT: // Context
			Context = changeValue(Context, value, CONTEXT_WRITE_MASK);
			break;
		case COP0_REG_PAGEMASK: // PageMask
			PageMask = value & 0x01FFE000;
			break;
		case COP0_REG_WIRED: // Wired
			Wired = value & INDEX_MASK;
			break;
		case COP0_REG_RESERVED1: // Reserved1
			Reserved1 = value;
			break;
		case COP0_REG_BADVADDR: // BadVAddr
			// Ignore BadVAddr writes
			break;
		case COP0_REG_COUNT: // Count
			cpu.setCounter(value);
			break;
		case COP0_REG_ENTRYHI: // EntryHi
			EntryHi = value & ENTRYHI_WRITE_MASK;
			ASID = value & ENTRYHI_ASID_MASK;
			break;
		case COP0_REG_COMPARE: // Compare
			Compare = value;
			lowerIrq(TIMER_IRQ);
			break;
		case COP0_REG_STATUS: // Status
			writeStatus(value);
			break;
		case COP0_REG_CAUSE: // Cause
			Cause = changeValue(Cause, value, CAUSE_WRITE_MASK);
			break;
		case COP0_REG_EPC: // EPC
			EPC = value;
			break;
		case COP0_REG_PRID: // PRId
			// Ignore PRId writes
			break;
		case COP0_REG_CONFIG: // Config and Config1
			if (sel == 0) {
				Config = changeValue(Config,  value, 0x07);
			}
			// Ignores Config1 writes
			break;
		case COP0_REG_LLADDR: // LLAddr
			// Ignore LLAddr writes
			break;
		case COP0_REG_WATCHLO: // WatchLo
			WatchLo = value;
			break;
		case COP0_REG_WATCHHI: // WatchHi
			WatchHi = value;
			break;
		case COP0_REG_RESERVED2: // Reserved2
			Reserved2 = value;
			break;
		case COP0_REG_RESERVED3: // Reserved3
			Reserved3 = value;
			break;
		case COP0_REG_RESERVED4: // Reserved4
			Reserved4 = value;
			break;
		case COP0_REG_DEBUG: // Debug
			Debug = value;
			break;
		case COP0_REG_DEPC: // DEPC
			DEPC = value;
			break;
		case COP0_REG_RESERVED5: // Reserved5
			Reserved5 = value;
			break;
		case COP0_REG_ERRCTRL: // ErrCtrl
			ErrCtrl = value;
			break;
		case COP0_REG_RESERVED6: // Reserved6
			Reserved6 = value;
			break;
		case COP0_REG_TAGLO: // TagLo
			if (sel == 0) TagLo = value;
			else DataLo = value;
			break;
		case COP0_REG_RESERVED7: // Reserved7
			Reserved7 = value;
			break;
		case COP0_REG_ERROREPC: // ErrorEPC
			ErrorEPC = value;
			break;
		case COP0_REG_DESAVE: // DESAVE
			DESAVE = value;
			break;
		}
	}

	public int moveFromCoprocessor(int reg, int sel) {
		int retval = 0;
		switch(reg) {
		case COP0_REG_INDEX: // Index
			retval = Index;
			break;
		case COP0_REG_RANDOM: // Random
			retval = readRegisterRandom();
			break;
		case COP0_REG_ENTRYLO0: // EntryLo0
			retval = EntryLo0;
			break;
		case COP0_REG_ENTRYLO1: // EntryLo1
			retval = EntryLo1;
			break;
		case COP0_REG_CONTEXT: // Context
			retval = Context;
			break;
		case COP0_REG_PAGEMASK: // PageMask
			retval = PageMask;
			break;
		case COP0_REG_WIRED: // Wired
			retval = Wired;
			break;
		case COP0_REG_RESERVED1: // Reserved1
			retval = Reserved1;
			break;
		case COP0_REG_BADVADDR: // BadVAddr
			retval = BadVAddr;
			break;
		case COP0_REG_COUNT: // Count
			retval = cpu.getCounter();
			break;
		case COP0_REG_ENTRYHI: // EntryHi
			retval = EntryHi;
			break;
		case COP0_REG_COMPARE: // Compare
			retval = Compare;
			break;
		case COP0_REG_STATUS: // Status
			retval = Status;
			break;
		case COP0_REG_CAUSE: // Cause
			retval = Cause;
			break;
		case COP0_REG_EPC: // EPC
			retval = EPC;
			break;
		case COP0_REG_PRID: // PRId
			retval = PRId;
			break;
		case COP0_REG_CONFIG: // Config and Config1
			if (sel == 0) retval = Config;
			else retval = Config1;
			break;
		case COP0_REG_LLADDR: // LLAddr
			retval = LLAddr;
			break;
		case COP0_REG_WATCHLO: // WatchLo
			retval = WatchLo;
			break;
		case COP0_REG_WATCHHI: // WatchHi
			retval = WatchHi;
			break;
		case COP0_REG_RESERVED2: // Reserved2
			retval = Reserved2;
			break;
		case COP0_REG_RESERVED3: // Reserved3
			retval = Reserved3;
			break;
		case COP0_REG_RESERVED4: // Reserved4
			retval = Reserved4;
			break;
		case COP0_REG_DEBUG: // Debug
			retval = Debug;
			break;
		case COP0_REG_DEPC: // DEPC
			retval = DEPC;
			break;
		case COP0_REG_RESERVED5: // Reserved5
			retval = Reserved5;
			break;
		case COP0_REG_ERRCTRL: // ErrCtrl
			retval = ErrCtrl;
			break;
		case COP0_REG_RESERVED6: // Reserved6
			retval = Reserved6;
			break;
		case COP0_REG_TAGLO: // TagLo
			if (sel == 0) retval = TagLo;
			else retval = DataLo;
			break;
		case COP0_REG_RESERVED7: // Reserved7
			retval = Reserved7;
			break;
		case COP0_REG_ERROREPC: // ErrorEPC
			retval = ErrorEPC;
			break;
		case COP0_REG_DESAVE: // DESAVE
			retval = DESAVE;
			break;
		}
		return retval;
	}

	public void exception_RESET() {
		Wired = 0;
		Config = changeValue(Config, 2, 0x07);
		writeStatus(changeValue(Status, STATUS_BEV | STATUS_ERL, STATUS_RP | STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL));
		ErrorEPC = (cpu.isBranchDelaySlot()) ? cpu.getProgramCounter() - 4 : cpu.getProgramCounter();

		cpu.setProgramCounter(0xBFC00000);
	}

	public void exception_SOFT_RESET() {
		writeStatus(changeValue(Status, STATUS_SR | STATUS_BEV | STATUS_ERL, STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL));
		ErrorEPC = (cpu.isBranchDelaySlot()) ? cpu.getProgramCounter() - 4 : cpu.getProgramCounter();

		cpu.setProgramCounter(0xBFC00000);
	}

	public void exception_NMI() {
		writeStatus(changeValue(Status, STATUS_BEV | STATUS_NMI | STATUS_ERL, STATUS_BEV | STATUS_TS | STATUS_SR | STATUS_NMI | STATUS_ERL));
		ErrorEPC = (cpu.isBranchDelaySlot()) ? cpu.getProgramCounter() - 4 : cpu.getProgramCounter();

		cpu.setProgramCounter(0xBFC00000);
	}

	private void exception_GENERAL(int code, int copno) {
		int vectorOffset;

		if ((Status & STATUS_EXL) == 0) {
			if (cpu.isBranchDelaySlot()) {
				EPC = cpu.getProgramCounter() - 4;
				Cause |= CAUSE_BD;
			} else {
				EPC = cpu.getProgramCounter();
				Cause &= ~CAUSE_BD;
			}
			if (code == EXCEPTION_CODE_TLBL || code == EXCEPTION_CODE_TLBS) {
				vectorOffset = 0;
			} else if (code == EXCEPTION_CODE_INT && ((Cause & CAUSE_IV) != 0)) {
				vectorOffset = 0x200;
			} else {
				vectorOffset = 0x180;
			}
		} else {
			vectorOffset = 0x180;
		}
		Cause = (Cause & ~CAUSE_CE_MASK) | (copno << CAUSE_CE_SHIFT);
		Cause = (Cause & ~CAUSE_EXCCODE_MASK) | (code << CAUSE_EXCCODE_SHIFT);
		writeStatus(Status | STATUS_EXL);
		if ((Status & STATUS_BEV) != 0) {
			cpu.setProgramCounter(0xBFC00200 + vectorOffset);
		} else {
			cpu.setProgramCounter(0x80000000 + vectorOffset);
		}
	}

	public void exception_MCHECK() {
		exception_GENERAL(EXCEPTION_CODE_MCHECK, 0);
		Status |= STATUS_TS;
	}

	public void exception_INTERRUPT() {
		exception_GENERAL(EXCEPTION_CODE_INT, 0);
	}

	public void exception_ADDRESS_ERROR(int badVAddr, boolean load) {
		exception_GENERAL(load ? EXCEPTION_CODE_ADEL : EXCEPTION_CODE_ADES, 0);
		BadVAddr = badVAddr;
	}

	public void exception_TLB_REFILL(int badVAddr, boolean load) {
		exception_GENERAL(load ? EXCEPTION_CODE_TLBL : EXCEPTION_CODE_TLBS, 0);
		BadVAddr = badVAddr;
		Context = (Context & CONTEXT_PTE_MASK) | ((badVAddr & ENTRYHI_VPN2_MASK) >>> 9);
		EntryHi = (EntryHi & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	public void exception_TLB_INVALID(int badVAddr, boolean load) {
		exception_GENERAL(load ? EXCEPTION_CODE_TLBL : EXCEPTION_CODE_TLBS, 0);
		BadVAddr = badVAddr;
		Context = (Context & CONTEXT_PTE_MASK) | ((badVAddr & ENTRYHI_VPN2_MASK) >>> 9);
		EntryHi = (EntryHi & ENTRYHI_ASID_MASK) | (badVAddr & ENTRYHI_VPN2_MASK);
	}

	public void exception_BUS_ERROR(boolean data) {
		exception_GENERAL(data ? EXCEPTION_CODE_DBE : EXCEPTION_CODE_IBE, 0);
	}

	public void exception_SYSCALL() {
		exception_GENERAL(EXCEPTION_CODE_SYS, 0);
	}

	public void exception_BREAK() {
		exception_GENERAL(EXCEPTION_CODE_BP, 0);
	}

	public void exception_RESERVED() {
		exception_GENERAL(EXCEPTION_CODE_RI, 0);
	}

	public void exception_COPROCESS_UNUSABLE(int copno) {
		exception_GENERAL(EXCEPTION_CODE_CPU, copno);
	}

	public void exception_INTEGER_OVERFLOW() {
		exception_GENERAL(EXCEPTION_CODE_OV, 0);
	}

	public void exception_TRAP() {
		exception_GENERAL(EXCEPTION_CODE_TR, 0);
	}

	public void exception_TLB_MOD(int badVAddr) {
		exception_GENERAL(EXCEPTION_CODE_MOD, 0);
		BadVAddr = badVAddr;
		Context = badVAddr;
	}

	public void returnFromException() {
		int newPc;
		loadLinkedStatus = false;
		if ((Status & STATUS_ERL) != 0) {
			writeStatus(Status & (~STATUS_ERL));
			newPc = ErrorEPC;
		} else {
			writeStatus(Status & (~STATUS_EXL));
			newPc = EPC;
		}
		cpu.setProgramCounter(newPc);
	}

	public void raiseIrq(int irqno) {
		Cause |= 1 << (CAUSE_INTERRUPT_SHIFT + irqno);
	}

	public void lowerIrq(int irqno) {
		Cause &= ~(1 << (CAUSE_INTERRUPT_SHIFT + irqno));
	}

	public boolean interruptEnabled() {
		return (Status & (STATUS_IE | STATUS_EXL | STATUS_ERL)) == STATUS_IE;
	}

	public boolean checkInterrupts() {
		if (interruptEnabled()) {
			int mask = (Status & STATUS_INT_MASK) >>> STATUS_INT_SHIFT;
			int pending = (Cause & CAUSE_INTERRUPT_MASK) >>> CAUSE_INTERRUPT_SHIFT;
			if ((mask & pending) != 0) {
				exception_INTERRUPT();
				return true;
			}
		}
		return false;
	}

	public boolean checkTimerInterrupt(int before, int after) {
		int cmp1 = Utils.compareUnsigned(before, Compare); 
		if (cmp1 > 0) return false;
		if (cmp1 < 0) {
			if ((after - before) < (Compare - before)) return false;
		}

		raiseIrq(TIMER_IRQ);
		checkInterrupts();
		return true;
	}

	public void loadLinked(int physicalAddress) {
		loadLinkedStatus = true;
		LLAddr = physicalAddress >>> 4;
	}

	public boolean canStoreConditional() {
		return loadLinkedStatus;
	}

	public int translate(int address, boolean write, boolean data) {
		translationError = false;
		if (kernelMode) {
			if ((address & 0xC0000000) == 0x80000000) { // kseg0 or kseg1 
				return address & 0x1FFFFFFF;
			}
		} else { // User Mode
			// useg
			if ((address & 0x80000000) != 0) { 
				exception_ADDRESS_ERROR(address, !write);
				translationError = true;
				return 0;
			}
			
		}

		TlbEntry tlbEntry;
		TlbEntryPage tlbEntryPage;
		tlbEntry = (data) ? lastTlbEntryData : lastTlbEntryCode;

		if (tlbEntry.initialized) {
			tlbEntryPage = tlbMatchEntry(tlbEntry, address, ASID);
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}

		// Perform the TLB search
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntry = tlbEntries[i];

			if (!tlbEntry.initialized) continue;
			tlbEntryPage = tlbMatchEntry(tlbEntry, address, ASID); 
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}
		if ((Status & STATUS_EXL) != 0) {
			exception_TLB_INVALID(address, !write);
		} else {
			exception_TLB_REFILL(address, !write);
		}
		translationError = true;
		return 0;
	}

	public boolean translationError() {
		return translationError;
	}

	private static TlbEntryPage tlbMatchEntry(TlbEntry entry, int address, int ASID) {
		if ((entry.PageMask & address) == entry.VPN2) {
			if (entry.global || entry.ASID == ASID) {
				if ((address & entry.selectionBit) == 0) {
					return entry.page0;
				} else {
					return entry.page1;
				}
			}
		}
		return null;
	}

	private int tlbPageTranslate(TlbEntry tlbEntry, TlbEntryPage tlbEntryPage, int address, boolean write, boolean data) {
		if (data) lastTlbEntryData = tlbEntry;
		else lastTlbEntryCode = tlbEntry;
		if (!tlbEntryPage.valid) {
			exception_TLB_INVALID(address, !write);
			translationError = true;
			return 0;
		}
		if (write && !tlbEntryPage.dirty) {
			exception_TLB_MOD(address);
			translationError = true;
			return 0;
		}
		address &= ~(tlbEntry.PageMask | tlbEntry.selectionBit);
		address |= tlbEntryPage.PFN;
		return address;
	}

	private static int convertPageToEntryLo(TlbEntryPage page, boolean global) {
		int entry = page.PFN >>> 6;
		entry |= page.cacheability << ENTRYLO_COHERENCY_SHIFT;
		if (page.dirty) entry |= ENTRYLO_DIRTY;
		if (page.valid) entry |= ENTRYLO_VALID;
		if (global) entry |= ENTRYLO_GLOBAL;
		return entry;
	}

	private static void configurePageFromEntryLo(TlbEntryPage page, int entry) {
		page.PFN = (entry << 6) & 0xFFFFF000;
		page.cacheability = (entry & ENTRYLO_COHERENCY_MASK) >> ENTRYLO_COHERENCY_SHIFT;
		page.dirty = (entry & ENTRYLO_DIRTY) != 0;
		page.valid = (entry & ENTRYLO_VALID) != 0;
	}

	public void tlbProbe() {
		int ASID = EntryHi & ENTRYHI_ASID_MASK;
		int VPN2 = EntryHi & ENTRYHI_VPN2_MASK;
		Index = INDEX_PROBE;
		for(int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			TlbEntry tlbEntry = tlbEntries[idx];
			TlbEntryPage tlbEntryPage;

			if (!tlbEntry.initialized) continue;
			tlbEntryPage = tlbMatchEntry(tlbEntry, VPN2, ASID); 
			if (tlbEntryPage != null) {
				Index = idx;
				return;
			}
		}
	}

	public void tlbRead() {
		TlbEntry entry = tlbEntries[Index & INDEX_MASK];
		PageMask = (~entry.PageMask) & ENTRYHI_VPN2_MASK;
		EntryHi = entry.VPN2 | entry.ASID;
		EntryLo0 = convertPageToEntryLo(entry.page0, entry.global);
		EntryLo1 = convertPageToEntryLo(entry.page1, entry.global);
	}

	private void tlbWrite(int index) {
		TlbEntry tlbEntry;
		int mask = (~PageMask) & ENTRYHI_VPN2_MASK;
		int VPN2 = EntryHi & mask;
		int ASID = EntryHi & ENTRYHI_ASID_MASK;
		boolean global = ((EntryLo0 & ENTRYLO_GLOBAL) != 0) && ((EntryLo1 & ENTRYLO_GLOBAL) != 0);

		// Checks for multiple entries
		for (int idx = 0; idx < NUM_TLB_ENTRIES; idx++) {
			tlbEntry = tlbEntries[idx];
			if (!tlbEntry.initialized || idx == index) continue;
			if ((tlbEntry.VPN2 & mask) == VPN2) {
				if (global || tlbEntry.global || tlbEntry.ASID == ASID) {
					exception_MCHECK();
					return;
				}
			}
		}
		tlbEntry = tlbEntries[index];
		tlbEntry.initialized = true;
		tlbEntry.PageMask = mask;
		tlbEntry.VPN2 = VPN2;
		tlbEntry.ASID = ASID;
		tlbEntry.global = global;
		configurePageFromEntryLo(tlbEntry.page0, EntryLo0);
		configurePageFromEntryLo(tlbEntry.page1, EntryLo1);
	}

	public void tlbWriteRandom() {
		tlbWrite(readRegisterRandom());
	}

	public void tlbWriteIndex() {
		tlbWrite(Index & INDEX_MASK);
	}

	private static final class TlbEntry {
		private int PageMask; // Negation of original PageMask and'ed with 0xFFFFE000
		private int VPN2;     // Masked VPN2
		private int selectionBit;
		private boolean global;
		private int ASID;
		private TlbEntryPage page0;
		private TlbEntryPage page1;
		private boolean initialized;
	}

	private static final class TlbEntryPage {
		private int PFN;
		private int cacheability;
		private boolean dirty;
		private boolean valid;
	}
}
