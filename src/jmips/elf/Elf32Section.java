package jmips.elf;

import java.nio.ByteBuffer;

/**
 * Elf32 Section Class
 * @author Humberto Silva Naves
 *
 */
public class Elf32Section {
	public static final int SHT_NULL =              0;
	public static final int SHT_PROGBITS =          1;
	public static final int SHT_SYMTAB =            2;
	public static final int SHT_STRTAB =            3;
	public static final int SHT_RELA =              4;
	public static final int SHT_HASH =              5;
	public static final int SHT_DYNAMIC =           6;
	public static final int SHT_NOTE =              7;
	public static final int SHT_NOBITS =            8;
	public static final int SHT_REL =               9;
	public static final int SHT_SHLIB =            10;
	public static final int SHT_DYNSYM =           11;
	public static final int SHT_REGINFO =  0x70000006;

	public static final int SHF_WRITE =              1;
	public static final int SHF_ALLOC =              2;
	public static final int SHF_EXECINSTR =          4;

	private String name;
	private int indexName;
	private int type;
	private int flags;
	private int address;
	private int offset;
	private int size;
	private int link;
	private int info;
	private int addressAlignment;
	private int entitySize;
	private ByteBuffer data;

	public boolean readElf32Section(ByteBuffer bb) {
		int SH_NAME = bb.getInt();
		int SH_TYPE = bb.getInt();
		int SH_FLAGS = bb.getInt();
		int SH_ADDR = bb.getInt();
		int SH_OFFSET = bb.getInt();
		int SH_SIZE = bb.getInt();
		int SH_LINK = bb.getInt();
		int SH_INFO = bb.getInt();
		int SH_ADDRALIGN = bb.getInt();
		int SH_ENTSIZE = bb.getInt();

		ByteBuffer data = null;
		if (SH_TYPE != SHT_NOBITS && SH_SIZE > 0) {
			data = ByteBuffer.allocate(SH_SIZE);
			data.order(bb.order());
			int oldposition = bb.position();
			try {
				bb.position(SH_OFFSET);
				ByteBuffer sliced = bb.slice();
				sliced.limit(SH_SIZE);
				data.put(sliced);
				data.flip();
			} finally {
				bb.position(oldposition);
			}
		}

		name = null;
		indexName = SH_NAME;
		type = SH_TYPE;
		flags = SH_FLAGS;
		address = SH_ADDR;
		offset = SH_OFFSET;
		size = SH_SIZE;
		link = SH_LINK;
		info = SH_INFO;
		addressAlignment = SH_ADDRALIGN;
		entitySize = SH_ENTSIZE;
		this.data = data;
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndexName() {
		return indexName;
	}

	public void setIndexName(int indexName) {
		this.indexName = indexName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLink() {
		return link;
	}

	public void setLink(int link) {
		this.link = link;
	}

	public int getInfo() {
		return info;
	}

	public void setInfo(int info) {
		this.info = info;
	}

	public int getAddressAlignment() {
		return addressAlignment;
	}

	public void setAddressAlignment(int addressAlignment) {
		this.addressAlignment = addressAlignment;
	}

	public int getEntitySize() {
		return entitySize;
	}

	public void setEntitySize(int entitySize) {
		this.entitySize = entitySize;
	}

	public ByteBuffer getData() {
		return data;
	}

	public void setData(ByteBuffer data) {
		this.data = data;
	}

	private static String getTypeString(int type) {
		switch (type) {
		case SHT_NULL: return "NULL";
		case SHT_PROGBITS: return "PROGBITS";
		case SHT_SYMTAB: return "SYMTAB";
		case SHT_STRTAB: return "STRTAB";
		case SHT_RELA: return "RELA";
		case SHT_HASH: return "HASH";
		case SHT_DYNAMIC: return "DYNAMIC";
		case SHT_NOTE: return "NOTE";
		case SHT_NOBITS: return "NOBITS";
		case SHT_REL: return "REL";
		case SHT_SHLIB: return "SHLIB";
		case SHT_DYNSYM: return "DYNSYM";
		case SHT_REGINFO: return "REGINFO";
		}
		return String.format("0x%X", type);
	}
	private static String getFlagsString(int flags) {
		String f1 = ((flags & SHF_ALLOC) != 0) ? "A" : " ";
		String f2 = ((flags & SHF_WRITE) != 0) ? "W" : " ";
		String f3 = ((flags & SHF_EXECINSTR) != 0) ? "X" : " ";
		return String.format("%s%s%s", f1, f2, f3);
	}

	@Override
	public String toString() {
		String stype = getTypeString(type);
		String sflags = getFlagsString(flags);
		return String.format(
				"%-28s %-10s 0x%08X 0x%08X 0x%08X %02d %s   %-4d %-5d  %-4d", name,
				stype, address, offset, size, entitySize,
				sflags , link, info, addressAlignment);
	}

}
