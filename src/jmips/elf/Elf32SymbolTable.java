package jmips.elf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Elf32 Symbol Table Class
 * @author Humberto Silva Naves
 *
 */
public class Elf32SymbolTable {

	public static final int ST_ENTSIZE = 16;

	private List<SymbolEntry> entries;

	public List<SymbolEntry> getEntries() {
		return entries;
	}

	public void setEntries(List<SymbolEntry> entries) {
		this.entries = entries;
	}

	public boolean readElf32SymbolTable(Elf32 elf, int sectionIndex) {
		Elf32Section section = elf.getSection(sectionIndex);
		if (section == null) return false;
		if (section.getEntitySize() != ST_ENTSIZE) return false;
		if ((section.getSize() % ST_ENTSIZE) != 0) return false;

		List<SymbolEntry> entries = new ArrayList<SymbolEntry>();
		try {
			ByteBuffer data = section.getData();
			data = data.duplicate();
			int numElements = section.getSize() / ST_ENTSIZE;
			for(int i = 0; i < numElements; i++) {
				int ST_NAME = data.getInt();
				int ST_VALUE = data.getInt();
				int ST_SIZE = data.getInt();
				int ST_INFO = data.get();
				int ST_OTHER = data.get();
				int ST_SHNDX = Elf32.readUnsigned16(data);
				String name = elf.readString(section.getLink(), ST_NAME);
				if (name == null) return false;
				SymbolEntry entry = new SymbolEntry();
				entry.name = name;
				entry.value = ST_VALUE;
				entry.size = ST_SIZE;
				entry.bind = (ST_INFO >> 4);
				entry.type = (ST_INFO & 0x0F);
				entry.other = ST_OTHER;
				entry.section = ST_SHNDX;
				entries.add(entry);
			}
		} catch(RuntimeException ex) {
			return false;
		}
		this.entries = entries;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Symbol Table:\n");
		if (entries != null) {
			sb.append("Name                                  Value        Size   Type       Bind    Other  Section\n");
			for(SymbolEntry entry : entries) {
				sb.append(entry);
				sb.append("\n");
			}
		}
		return sb.toString();
	}


	public static class SymbolEntry {
		public static final int STT_NOTYPE =       0;
		public static final int STT_OBJECT =       1;
		public static final int STT_FUNC =         2;
		public static final int STT_SECTION =      3;
		public static final int STT_FILE =         4;

		public static final int STB_LOCAL =        0;
		public static final int STB_GLOBAL =       1;
		public static final int STB_WEAK =         2;

		public static final int SHN_ABS =    0xFFF1;
		public static final int SHN_COMMON = 0xFFF2;

		private String name;
		private int value;
		private int size;
		private int type;
		private int bind;
		private int other;
		private int section;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}

		public int getSize() {
			return size;
		}
		public void setSize(int size) {
			this.size = size;
		}

		public int getType() {
			return type;
		}
		public void setType(int type) {
			this.type = type;
		}

		public int getBind() {
			return bind;
		}
		public void setBind(int bind) {
			this.bind = bind;
		}

		public int getOther() {
			return other;
		}
		public void setOther(int other) {
			this.other = other;
		}

		public int getSection() {
			return section;
		}
		public void setSection(int section) {
			this.section = section;
		}

		private static String getTypeString(int type) {
			switch(type) {
			case STT_NOTYPE:  return "NOTYPE";
			case STT_OBJECT:  return "OBJECT";
			case STT_FUNC:    return "FUNCTION";
			case STT_SECTION: return "SECTION";
			case STT_FILE:    return "FILE";
			}
			return Integer.toString(type);
		}

		private static String getBindString(int bind) {
			switch(bind) {
			case STB_LOCAL:   return "LOCAL";
			case STB_GLOBAL:  return "GLOBAL";
			case STB_WEAK:    return "WEAK";
			}
			return Integer.toString(bind);
		}

		private static String getSectionString(int section) {
			if (section == SHN_ABS) return "SHN_ABS";
			if (section == SHN_COMMON) return "SHN_COMMON";
			return String.format("%-5d", section);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			String stype = getTypeString(type);
			String sbind = getBindString(bind);
			String ssection = getSectionString(section);
			sb.append(String.format("%-36s  0x%08X   %-4d   %-8s   %-6s  %-2d     %s",
					name, value, size, stype, sbind, other, ssection));
			return sb.toString();
		}
	}
}
