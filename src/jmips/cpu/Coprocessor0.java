package jmips.cpu;

import java.util.Random;

public final class Coprocessor0 {
	public static final int NUM_TLB_ENTRIES = 16;

	public static final int COP0_INDEX = 0;            // Index into TLB array
	public static final int COP0_RANDOM = 1;           // Random generated index into the TLB array
	public static final int COP0_ENTRYLO0 = 2;         // Low order portion of the TLB entry for even-numbered virtual pages
	public static final int COP0_ENTRYLO1 = 3;         // Low order portion of the TLB entry for odd-numbered virtual pages
	public static final int COP0_CONTEXT = 4;          // Pointer to page table entry in memory
	public static final int COP0_PAGEMASK = 5;         // Controls the page sizes in TLB entries
	public static final int COP0_WIRED = 6;            // Controls the number of fixed TLB entries
	public static final int COP0_RESERVED1 = 7;        // Reserved
	public static final int COP0_BADVADDR = 8;         // Reports the address for the most recent address-related exception
	public static final int COP0_COUNT = 9;            // Processor cycle count
	public static final int COP0_ENTRYHI = 10;         // High-order portion of the TLB entry
	public static final int COP0_COMPARE = 11;         // Timer interrupt control
	public static final int COP0_STATUS = 12;          // Processor status and control
	public static final int COP0_CAUSE = 13;           // Cause of last exception
	public static final int COP0_EPC = 14;             // Program counter at last exception
	public static final int COP0_PRID = 15;            // Processor identification and revision
	public static final int COP0_CONFIG = 16;          // Configuration registers
	public static final int COP0_LLADDR = 17;          // Load linked address
	public static final int COP0_WATCHLO = 18;         // Watchpoint address(low order)
	public static final int COP0_WATCHHI = 19;         // Watchpoint address(high order) and mask
	public static final int COP0_RESERVED2 = 20;       // Reserved
	public static final int COP0_RESERVED3 = 21;       // Reserved
	public static final int COP0_RESERVED4 = 22;       // Reserved
	public static final int COP0_DEBUG = 23;           // Debug control and exception status
	public static final int COP0_DEPC = 24;            // Program counter at last debug exception
	public static final int COP0_RESERVED5 = 25;       // Reserved
	public static final int COP0_ERRCTRL = 26;         // Control access to data and SPRAM arrays for CACHE instruction
	public static final int COP0_RESERVED6 = 27;       // Reserved
	public static final int COP0_TAGLO = 28;           // Low-order portion of cache tag interface
	public static final int COP0_RESERVED7 = 29;       // Reserved
	public static final int COP0_ERROREPC = 30;        // Program counter at last error
	public static final int COP0_DESAVE = 31;          // Debug handler scratchpad register

	public static final int EXCP_RESET = 0;            // Reset (Cold_Reset)
	public static final int EXCP_SOFT_RESET = 1;       // Reset (Soft_Reset)
	public static final int EXCP_DSS = 2;              // EJTAG Debug Single Step
	public static final int EXCP_DINT = 3;             // EJTAG Debug Interrupt
	public static final int EXCP_NMI = 4;              // Non-maskable interrupt
	public static final int EXCP_MACHINE_CHECK = 5;    // TLB write conflicts with an existing entry
	public static final int EXCP_INTERRUPT = 6;        // Interrupt (Software of Hardware interrupt)
	public static final int EXCP_DEFERRED_WATCH = 7;   // Deferred Watch
	public static final int EXCP_DIB = 8;              // EJTAG Debug Hardware Instruction break matched
	public static final int EXCP_WATCH_FETCH = 9;      // Reference to an address in one of the watch registers (fetch)
	public static final int EXCP_ADEL_FETCH = 10;      // Fetch address alignment error or User mode fetch reference to kernel address
	public static final int EXCP_TLBL_FETCH = 11;      // Fetch TLB miss or Fetch TLB hit to page with valid=false
	public static final int EXCP_IBE = 12;             // Instruction fetch bus error 
	public static final int EXCP_DBP = 13;             // EJTAG Breakpoint (execution of SDBBP)
	public static final int EXCP_SYS = 14;             // SYSCALL
	public static final int EXCP_BP = 15;              // BREAK
	public static final int EXCP_CPU = 16;             // Coprocessor Unusable
	public static final int EXCP_RI = 17;              // Reserved Instruction
	public static final int EXCP_OV = 18;              // Overflow
	public static final int EXCP_TR = 19;              // Trap
	public static final int EXCP_DDBL_DDBS = 20;       // EJTAG Data address break (address only) or EJTAG Data Value Break on Store(address and value)
	public static final int EXCP_WATCH = 21;           // Reference to an address in one of the watch registers (data)
	public static final int EXCP_ADEL = 22;            // Load address alignment error or User mode load reference to kernel address
	public static final int EXCP_ADES = 23;            // Store address alignment error or User mode store reference to kernel address
	public static final int EXCP_TLBL = 24;            // Load TLB miss or Load TLB hit to page with valid=false
	public static final int EXCP_TLBS = 25;            // Store TLB miss or Store TLB hit to page with valid=false
	public static final int EXCP_TLBMOD = 26;          // Store TLB hit to page with dirty=false
	public static final int EXCP_DBE = 27;             // Load or store bus error.
	public static final int EXCP_DDBL = 28;            // EJTAG data hardware breakpoint matched in load data compare

	public static final int EXCP_CAUSE_INT = 0;        // Interrupt
	public static final int EXCP_CAUSE_MOD = 1;        // TLB modification exception
	public static final int EXCP_CAUSE_TLBL = 2;       // TLB exception (load or instruction fetch)
	public static final int EXCP_CAUSE_TLBS = 3;       // TLB exception (store)
	public static final int EXCP_CAUSE_ADEL = 4;       // Address error exception (load or instruction fetch)
	public static final int EXCP_CAUSE_ADES = 5;       // Address error exception (store)
	public static final int EXCP_CAUSE_IBE = 6;        // Bus Error exception (instruction fetch)
	public static final int EXCP_CAUSE_DBE = 7;        // Bus Error exception (data reference: load or store)
	public static final int EXCP_CAUSE_SYS = 8;        // Syscall exception
	public static final int EXCP_CAUSE_BP = 9;         // Breakpoint exception
	public static final int EXCP_CAUSE_RI = 10;        // Reserved Instruction exception
	public static final int EXCP_CAUSE_CPU = 11;       // Coprocessor Unusable exception
	public static final int EXCP_CAUSE_OV = 12;        // Overflow exception
	public static final int EXCP_CAUSE_TR = 13;        // Trap exception
	public static final int EXCP_CAUSE_WATCH = 23;     // Refrence to WatchHi/WatchLo address
	public static final int EXCP_CAUSE_MCHECK = 24;    // Machine check exception

	private int Index;
	private int EntryLo0;
	private int EntryLo1;
	private int Context;
	private int PageMask;
	private int Wired;
	private int Reserved1;
	private int BadVAddr;
	private int Count;
	private int EntryHi;
	private int Compare;
	private int Status;
	private int Cause;
	private int EPC;
	private int PRId;
	private int Config;
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

	private boolean interruptEnable;
	private boolean cop0Avail;
	private boolean kernelMode= true, debugMode;
	private int ASID;

	private boolean translationError;

	private final Random rand = new Random(System.currentTimeMillis());
	private final TlbEntry [] tlbEntries;
	private TlbEntry lastCodeEntry;
	private TlbEntry lastDataEntry;

	private final Cpu cpu;

	public Coprocessor0(Cpu cpu) {
		this.cpu = cpu;
		tlbEntries = new TlbEntry[NUM_TLB_ENTRIES];
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntries[i] = new TlbEntry();
		}
		lastCodeEntry = lastDataEntry = tlbEntries[0];
	}

	private int randomRead() {
		return Wired + rand.nextInt(NUM_TLB_ENTRIES - Wired);
	}

	private static int changeValue(int oldValue, int newValue, int mask) {
		return (oldValue & ~ mask) | (newValue & mask);
	}

	public void moveToCoprocessor(int reg, int sel, int value) {
		switch(reg) {
		case COP0_INDEX: // Index
			Index = changeValue(Index, value, 0x0F);
			break;
		case COP0_RANDOM: // Random
			// Ignore random writes
			break;
		case COP0_ENTRYLO0: // EntryLo0
			EntryLo0 = value & 0x03FFFFFF;
			break;
		case COP0_ENTRYLO1: // EntryLo1
			EntryLo1 = value & 0x03FFFFFF;
			break;
		case COP0_CONTEXT: // Context
			Context = changeValue(Context, value, 0xFF800000);
			break;
		case COP0_PAGEMASK: // PageMask
			PageMask = value & 0x01FFE000;
			break;
		case COP0_WIRED: // Wired
			Wired = value & 0x0F;
			break;
		case COP0_RESERVED1: // Reserved1
			Reserved1 = value;
			break;
		case COP0_BADVADDR: // BadVAddr
			// Ignore BadVAddr writes
			break;
		case COP0_COUNT: // Count
			Count = value;
			break;
		case COP0_ENTRYHI: // EntryHi
			EntryHi = value & 0xFFFFE0FF;
			ASID = value & 0xFF;
			break;
		case COP0_COMPARE: // Compare
			Compare = value;
			break;
		case COP0_STATUS: // Status
			Status = value & 0xFA78FF17;
			kernelMode = ((value & 0x00000016) != 0x00000010);
			interruptEnable = ((value & 0x00000007) == 0x00000001);
			cop0Avail = ((value & 0x10000000) != 0);
			break;
		case COP0_CAUSE: // Cause
			// Cause = value & 0xFFFFFFFF;
			break;
		case COP0_EPC: // EPC
			EPC = value;
			break;
		case COP0_PRID: // PRId
			// Ignore PRId writes
			break;
		case COP0_CONFIG: // Config and Config1
			if (sel == 0) {
				Config = changeValue(Config,  value, 0x07);
			}
			// Ignores Config1 writes
			break;
		case COP0_LLADDR: // LLAddr
			// Ignore LLAddr writes
			break;
		case COP0_WATCHLO: // WatchLo
			WatchLo = value;
			break;
		case COP0_WATCHHI: // WatchHi
			// WatchHi = value & 0xFFFFFFFF;
			break;
		case COP0_RESERVED2: // Reserved2
			Reserved2 = value;
			break;
		case COP0_RESERVED3: // Reserved3
			Reserved3 = value;
			break;
		case COP0_RESERVED4: // Reserved4
			Reserved4 = value;
			break;
		case COP0_DEBUG: // Debug
			// Debug = value & 0xFFFFFFFF;
			debugMode = (value & 0x40000000) != 0;
			break;
		case COP0_DEPC: // DEPC
			DEPC = value;
			break;
		case COP0_RESERVED5: // Reserved5
			Reserved5 = value;
			break;
		case COP0_ERRCTRL: // ErrCtrl
			//ErrCtrl = value & 0xFFFFFFFF;
			break;
		case COP0_RESERVED6: // Reserved6
			Reserved6 = value;
			break;
		case COP0_TAGLO: // TagLo
			//TagLo = value & 0xFFFFFFFF;
			break;
		case COP0_RESERVED7: // Reserved7
			Reserved7 = value;
			break;
		case COP0_ERROREPC: // ErrorEPC
			ErrorEPC = value;
			break;
		case COP0_DESAVE: // DESAVE
			DESAVE = value;
			break;
		}
	}

	public int moveFromCoprocessor(int reg, int sel) {
		int retval = 0;
		switch(reg) {
		case COP0_INDEX: // Index
			//Index = value & 0x0F;
			break;
		case COP0_RANDOM: // Random
			retval = randomRead();
			break;
		case COP0_ENTRYLO0: // EntryLo0
			//EntryLo0 = value & 0x03FFFFFF;
			break;
		case COP0_ENTRYLO1: // EntryLo1
			//EntryLo1 = value & 0x03FFFFFF;
			break;
		case COP0_CONTEXT: // Context
			//Context = value & 0xFF800000;
			break;
		case COP0_PAGEMASK: // PageMask
			retval = PageMask;
			break;
		case COP0_WIRED: // Wired
			retval = Wired;
			break;
		case COP0_RESERVED1: // Reserved1
			retval = Reserved1;
			break;
		case COP0_BADVADDR: // BadVAddr
			retval = BadVAddr;
			break;
		case COP0_COUNT: // Count
			retval = Count;
			break;
		case COP0_ENTRYHI: // EntryHi
			//EntryHi = value & 0xFFFFE0FF;
			break;
		case COP0_COMPARE: // Compare
			retval = Compare;
			break;
		case COP0_STATUS: // Status
			retval = Status;
			break;
		case COP0_CAUSE: // Cause
			// Cause = value & 0xFFFFFFFF;
			break;
		case COP0_EPC: // EPC
			retval = EPC;
			break;
		case COP0_PRID: // PRId
			retval = PRId;
			break;
		case COP0_CONFIG: // Config and Config1
			if (sel == 0) retval = Config;
			else retval = Config1;
			break;
		case COP0_LLADDR: // LLAddr
			// Ignore LLAddr writes
			break;
		case COP0_WATCHLO: // WatchLo
			retval = WatchLo;
			break;
		case COP0_WATCHHI: // WatchHi
			// WatchHi = value & 0xFFFFFFFF;
			break;
		case COP0_RESERVED2: // Reserved2
			retval = Reserved2;
			break;
		case COP0_RESERVED3: // Reserved3
			retval = Reserved3;
			break;
		case COP0_RESERVED4: // Reserved4
			retval = Reserved4;
			break;
		case COP0_DEBUG: // Debug
			// Debug = value & 0xFFFFFFFF;
			break;
		case COP0_DEPC: // DEPC
			retval = DEPC;
			break;
		case COP0_RESERVED5: // Reserved5
			retval = Reserved5;
			break;
		case COP0_ERRCTRL: // ErrCtrl
			//ErrCtrl = value & 0xFFFFFFFF;
			break;
		case COP0_RESERVED6: // Reserved6
			retval = Reserved6;
			break;
		case COP0_TAGLO: // TagLo
			//TagLo = value & 0xFFFFFFFF;
			break;
		case COP0_RESERVED7: // Reserved7
			retval = Reserved7;
			break;
		case COP0_ERROREPC: // ErrorEPC
			retval = ErrorEPC;
			break;
		case COP0_DESAVE: // DESAVE
			retval = DESAVE;
			break;
		}
		return retval;
	}

	public void raiseException() {
		
	}

	public void exception_RESET() {
		Wired = 0;
		Config = 0x80008082;
		Status = changeValue(Status, 0x00400004, 0x08780004);
		kernelMode = true;
		interruptEnable = false;

		ErrorEPC = (cpu.delaySlot) ? cpu.pc - 4 : cpu.pc;
		WatchLo &= ~0x07;

		cpu.halted = false;
		cpu.delaySlot = false;
		cpu.pc = 0xBFC00000;
		cpu.nextPc = cpu.pc + 4;
	}

	public void exception_SOFT_RESET() {
		Status = changeValue(Status, 0x00500004, 0x00780004);
		kernelMode = true;
		interruptEnable = false;

		ErrorEPC = (cpu.delaySlot) ? cpu.pc - 4 : cpu.pc;

		cpu.halted = false;
		cpu.delaySlot = false;
		cpu.pc = 0xBFC00000;
		cpu.nextPc = cpu.pc + 4;
	}

	public void exception_NMI() {
		Status = changeValue(Status, 0x00480004, 0x00780004);
		kernelMode = true;
		interruptEnable = false;

		ErrorEPC = (cpu.delaySlot) ? cpu.pc - 4 : cpu.pc;

		cpu.halted = false;
		cpu.delaySlot = false;
		cpu.pc = 0xBFC00000;
		cpu.nextPc = cpu.pc + 4;
	}

	private void exception_GENERAL(int cause) {
		int vectorOffset;
		if ((Status & 0x00000002) == 0) { // EXL = 0
			if (cpu.delaySlot) {
				EPC = cpu.pc - 4;
				Cause |= 0x80000000;
			} else {
				EPC = cpu.pc;
				Cause &= ~0x80000000;
			}
			if (cause == EXCP_CAUSE_TLBL || cause == EXCP_CAUSE_TLBS) {
				vectorOffset = 0;
			}
		}
	}

	private void resetTLB() {
		for(int i = 0; i < NUM_TLB_ENTRIES; i++)
			tlbEntries[i].initialized = false;
	}

	public int translate(int address, boolean write, boolean data) {
		translationError = false;
		if (debugMode || kernelMode) {
			if ((address & 0xC0000000) == 0x80000000) { // kseg0 or kseg1 
				return address & 0x1FFFFFFF;
			}
			if (debugMode && ((address & 0xFFE00000) == 0xFF200000)) { // dmseg or drseg
				// TODO: Debug Memory segment 
				return 0;
			}
		} else { // User Mode
			// useg
			if ((address & 0x80000000) != 0) { 
				// TODO: Exception 
				translationError = true;
				return 0;
			}
			
		}

		TlbEntry tlbEntry;
		TlbEntryPage tlbEntryPage;
		tlbEntry = (data) ? lastDataEntry : lastCodeEntry;

		if (tlbEntry.initialized) {
			tlbEntryPage = tlbMatch(tlbEntry, address, ASID);
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}

		// Perform the TLB search
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntry = tlbEntries[i];

			if (!tlbEntry.initialized) continue;
			tlbEntryPage = tlbMatch(tlbEntry, address, ASID); 
			if (tlbEntryPage != null) {
				return tlbPageTranslate(tlbEntry, tlbEntryPage, address, write, data);
			}
		}
		// TODO: TLB Miss
		translationError = true;
		return 0;
	}

	public boolean translationError() {
		return translationError;
	}

	private static TlbEntryPage tlbMatch(TlbEntry entry, int address, int ASID) {
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
		if (data) lastDataEntry = tlbEntry;
		else lastCodeEntry = tlbEntry;
		if (!tlbEntryPage.valid) {
			// TODO: TLB Invalid exception
			translationError = true;
			return 0;
		}
		if (write && !tlbEntryPage.dirty) {
			// TODO: TLB Modified exception
			translationError = true;
			return 0;
		}
		address &= ~(tlbEntry.PageMask | tlbEntry.selectionBit);
		address |= tlbEntryPage.PFN;
		return address;
	}

	private static int convertPageToEntryLo(TlbEntryPage page, boolean global) {
		int entry = page.PFN >>> 6;
		entry |= page.cacheability << 3;
		if (page.dirty) entry |= 4;
		if (page.valid) entry |= 2;
		if (global) entry |= 1;
		return entry;
	}

	private static void configurePageFromEntryLo(TlbEntryPage page, int entry) {
		page.PFN = (entry << 6) & 0xFFFFF000;
		page.cacheability = (entry >> 3) & 0x07;
		page.dirty = (entry & 0x04) != 0;
		page.valid = (entry & 0x02) != 0;
	}

	public void doTLBP() {
		int ASID = EntryHi & 0xFF;
		int VPN2 = EntryHi & 0xFFFFE000;
		Index = 0x80000000;
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			TlbEntry tlbEntry = tlbEntries[i];
			TlbEntryPage tlbEntryPage;

			if (!tlbEntry.initialized) continue;
			tlbEntryPage = tlbMatch(tlbEntry, VPN2, ASID); 
			if (tlbEntryPage != null) {
				Index = i;
				return;
			}
		}
	}

	public void doTLBR() {
		TlbEntry entry = tlbEntries[Index];
		PageMask = (~entry.PageMask) & 0xFFFFE000;
		EntryHi = entry.VPN2 | entry.ASID;
		EntryLo0 = convertPageToEntryLo(entry.page0, entry.global);
		EntryLo1 = convertPageToEntryLo(entry.page1, entry.global);
	}

	private void doTLBWrite(int index) {
		TlbEntry tlbEntry;
		int mask = (~PageMask) & 0xFFFFE000;
		int VPN2 = EntryHi & mask;
		int ASID = EntryHi & 0xFF;
		boolean global = ((EntryLo0 & 0x01) != 0) && ((EntryLo1 & 0x01) != 0);

		// Checks for multiple entries
		for(int i = 0; i < NUM_TLB_ENTRIES; i++) {
			tlbEntry = tlbEntries[i];
			if (!tlbEntry.initialized || i == index) continue;
			if ((tlbEntry.VPN2 & mask) == VPN2) {
				if (global || tlbEntry.global || tlbEntry.ASID == ASID) {
					// TODO: TLB Machine Check exception
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

	public void doTLBWR() {
		doTLBWrite(randomRead());
	}

	public void doTLBWI() {
		doTLBWrite(Index & 0x0F);
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
