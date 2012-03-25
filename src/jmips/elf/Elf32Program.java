package jmips.elf;

import java.nio.ByteBuffer;

/**
 * Elf32 Program Class
 * @author Humberto Silva Naves
 *
 */
public class Elf32Program {
	public static final int PT_NULL =                  0;
	public static final int PT_LOAD =                  1;
	public static final int PT_DYNAMIC =               2;
	public static final int PT_INTERP =                3;
	public static final int PT_NOTE =                  4;
	public static final int PT_SHLIB =                 5;
	public static final int PT_PHDR =                  6;
	public static final int PT_MIPS_REGINFO = 0x70000000;


	public static final int PF_X =                   1;
	public static final int PF_W =                   2;
	public static final int PF_R =                   4;

	private int type;
	private int offset;
	private int virtualAddress;
	private int physicalAddress;
	private int fileSize;
	private int memorySize;
	private int flags;
	private int alignment;
	private ByteBuffer data;

	public boolean readElf32Program(ByteBuffer bb) {
		int P_TYPE = bb.getInt();
		int P_OFF = bb.getInt();
		int P_VADDR = bb.getInt();
		int P_PADDR = bb.getInt();
		int P_FILESZ = bb.getInt();
		int P_MEMSZ = bb.getInt();
		int P_FLAGS = bb.getInt();
		int P_ALIGN = bb.getInt();

		if (P_TYPE == PT_LOAD && P_FILESZ > P_MEMSZ) return false;

		ByteBuffer data = null;
		if (P_FILESZ > 0) {
			data = ByteBuffer.allocate(P_FILESZ);
			data.order(bb.order());
			int oldposition = bb.position();
			try {
				bb.position(P_OFF);
				ByteBuffer sliced = bb.slice();
				sliced.limit(P_FILESZ);
				data.put(sliced);
				data.flip();
			} finally {
				bb.position(oldposition);
			}
		}
		type = P_TYPE;
		offset = P_OFF;
		virtualAddress = P_VADDR;
		physicalAddress = P_PADDR;
		fileSize = P_FILESZ;
		memorySize = P_MEMSZ;
		flags = P_FLAGS;
		alignment = P_ALIGN;
		this.data = data;
		return true;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getVirtualAddress() {
		return virtualAddress;
	}

	public void setVirtualAddress(int virtualAddress) {
		this.virtualAddress = virtualAddress;
	}

	public int getPhysicalAddress() {
		return physicalAddress;
	}

	public void setPhysicalAddress(int physicalAddress) {
		this.physicalAddress = physicalAddress;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(int memorySize) {
		this.memorySize = memorySize;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getAlignment() {
		return alignment;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public ByteBuffer getData() {
		return data;
	}

	public void setData(ByteBuffer data) {
		this.data = data;
	}

	private static String getTypeString(int type) {
		switch (type) {
		case PT_NULL: return "NULL";
		case PT_LOAD: return "LOAD";
		case PT_DYNAMIC: return "DYN";
		case PT_INTERP: return "INTER";
		case PT_NOTE: return "NOTE";
		case PT_SHLIB: return "SHLIB";
		case PT_PHDR: return "PHDR";
		case PT_MIPS_REGINFO: return "REGINFO";
		}
		return String.format("0x%X", type);
	}

	private static String getFlagsString(int flags) {
		String f1 = ((flags & PF_X) != 0) ? "X" : " ";
		String f2 = ((flags & PF_R) != 0) ? "R" : " ";
		String f3 = ((flags & PF_W) != 0) ? "W" : " ";
		return String.format("%s%s%s", f1, f2, f3);
	}

	@Override
	public String toString() {
		String stype = getTypeString(type);
		String sflags = getFlagsString(flags);

		return String.format("  %-9s 0x%08X 0x%08X 0x%08X 0x%08X 0x%08X %s 0x%04X\n",
				stype, offset, virtualAddress, physicalAddress, fileSize,
				memorySize, sflags, alignment);
	}
}
