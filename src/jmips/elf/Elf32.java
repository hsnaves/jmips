package jmips.elf;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Elf32 Class
 * @author Humberto Silva Naves
 *
 */
public class Elf32 {
	public static final int EV_CURRENT = 1;
	public static final int ELFDATANONE = 0;
	public static final int ELFDATA2LSB = 1;
	public static final int ELFDATA2MSB = 2;

	public static final int READ_SUCCESS = 0;
	public static final int READ_IOERROR = 1;
	public static final int READ_INVALID = 2;

	private static final byte[] validMagic = {
		0x7F, 'E', 'L', 'F',
	};

	private static final int ELFCLASS32 = 1;
	private static final int EI_PADSIZE = 9;
	private static final int ET_EXEC = 2; 
	private static final int EM_MIPS = 8;
	private static final int EH_SIZE = 52;
	private static final int SH_ENTSIZE = 40;
	private static final int PH_ENTSIZE = 32;

	private int dataOrdering;
	private int entry;
	private int flags;

	private List<Elf32Section> sections;
	private List<Elf32Program> programs;

	
	public int checkValidIdent(ByteBuffer bb) {
		for(int i = 0; i < validMagic.length; i++) {
			byte b = bb.get();
			if (b != validMagic[i]) return ELFDATANONE;
		}
		byte EI_CLASS = bb.get();
		byte EI_DATA = bb.get();
		byte EI_VERSION = bb.get();

		if (EI_CLASS != ELFCLASS32) return ELFDATANONE;
		if (EI_VERSION != EV_CURRENT) return ELFDATANONE;
		if (EI_DATA != ELFDATA2LSB && EI_DATA != ELFDATA2MSB) return ELFDATANONE;
		for(int i = 0; i < EI_PADSIZE; i++) {
			byte b = bb.get();
			if (b != 0) return ELFDATANONE;
		}
		return EI_DATA;
	}

	public int readElf32(ByteBuffer bb) {
		try {
			int EI_DATA = checkValidIdent(bb);
			if (EI_DATA == ELFDATANONE) return READ_INVALID;

			if (EI_DATA == ELFDATA2LSB)
				bb.order(ByteOrder.LITTLE_ENDIAN);
			else
				bb.order(ByteOrder.BIG_ENDIAN);

			int E_TYPE = readUnsigned16(bb);
			if (E_TYPE != ET_EXEC) return READ_INVALID;

			int E_MACHINE = readUnsigned16(bb);
			if (E_MACHINE != EM_MIPS) return READ_INVALID;
			
			int E_VERSION = bb.getInt();
			if (E_VERSION != EV_CURRENT) return READ_INVALID;

			int E_ENTRY = bb.getInt();
			int E_PHOFF = bb.getInt();
			int E_SHOFF = bb.getInt();
			int E_FLAGS = bb.getInt();

			int E_EHSIZE = readUnsigned16(bb);
			if (E_EHSIZE != EH_SIZE) return READ_INVALID;

			int E_PHENTSIZE = readUnsigned16(bb);
			int E_PHNUM = readUnsigned16(bb);
			int E_SHENTSIZE = readUnsigned16(bb);
			int E_SHNUM = readUnsigned16(bb);
			int E_SHSTRNDX = readUnsigned16(bb);

			if (E_SHNUM != 0 && E_SHENTSIZE != SH_ENTSIZE) return READ_INVALID;
			List<Elf32Section> sections = loadSections(bb, E_SHOFF,  E_SHNUM);
			if (sections == null) return READ_INVALID;

			if (!loadSectionNames(sections, E_SHSTRNDX)) return READ_INVALID;

			if (E_PHNUM == 0 || E_PHENTSIZE != PH_ENTSIZE) return READ_INVALID;
			List<Elf32Program> programs = loadPrograms(bb, E_PHOFF,  E_PHNUM);
			if (programs == null) return READ_INVALID;

			dataOrdering = EI_DATA;
			entry = E_ENTRY;
			flags = E_FLAGS;
			this.sections = sections;
			this.programs = programs;
		} catch(RuntimeException ex) {
			return READ_IOERROR;
		}
		return READ_SUCCESS;
	}

	private List<Elf32Section> loadSections(ByteBuffer bb, int E_SHOFF, int E_SHNUM) {
		bb.position(E_SHOFF);
		List<Elf32Section> sections = new ArrayList<Elf32Section>();
		for(int idx = 0; idx < E_SHNUM; idx++) {
			Elf32Section section = new Elf32Section();
			if (!section.readElf32Section(bb))
				return null;
			sections.add(section);
		}
		return sections;
	}

	private boolean loadSectionNames(List<Elf32Section> sections, int stringSectionIndex) {
		Elf32Section stringSection = sections.get(stringSectionIndex);
		ByteBuffer data = stringSection.getData();
		if (data == null) return false;
		for(int idx = 0; idx < sections.size(); idx++) {
			Elf32Section section = sections.get(idx);
			int pos = section.getIndexName();
			String name = readString(data, pos);
			section.setName(name);
		}
		return true;
	}

	private List<Elf32Program> loadPrograms(ByteBuffer bb, int E_PHOFF, int E_PHNUM) {
		bb.position(E_PHOFF);
		List<Elf32Program> programs = new ArrayList<Elf32Program>();
		for(int idx = 0; idx < E_PHNUM; idx++) {
			Elf32Program program = new Elf32Program();
			if (!program.readElf32Program(bb))
				return null;
			programs.add(program);
		}
		return programs;
	}

	public int getDataOrdering() {
		return dataOrdering;
	}

	public void setDataOrdering(int dataOrdering) {
		this.dataOrdering = dataOrdering;
	}

	public int getEntry() {
		return entry;
	}

	public void setEntry(int entry) {
		this.entry = entry;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public List<Elf32Program> getPrograms() {
		return programs;
	}

	public void setPrograms(List<Elf32Program> programs) {
		this.programs = programs;
	}

	public Elf32Program getProgram(int index) {
		if (programs == null) return null;
		return programs.get(index);
	}

	public int getNumPrograms() {
		if (programs == null) return 0;
		return programs.size();
	}

	public List<Elf32Section> getSections() {
		return sections;
	}

	public void setSections(List<Elf32Section> sections) {
		this.sections = sections;
	}

	public Elf32Section getSection(int index) {
		if (sections == null) return null;
		return sections.get(index);
	}

	public Elf32Section getSectionByName(String sectionName) {
		if (sections == null) return null;
		for(Elf32Section section : sections) {
			if (section != null && sectionName.equals(section.getName()))
				return section;
		}
		return null;
	}

	public int getNumSections() {
		if (sections == null) return 0;
		return sections.size();
	}

	public String readString(int sectionIndex, int offset) {
		if (sections != null) {
			Elf32Section section = sections.get(sectionIndex);
			if (section != null && section.getType() == Elf32Section.SHT_STRTAB) {
				ByteBuffer data = section.getData();
				try {
					return readString(data, offset);
				} catch(RuntimeException ex) {
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ELF Header:\n");
		String ordering = "      None";
		if (dataOrdering == ELFDATA2LSB) ordering = " LittleEnd";
		else ordering = "    BigEnd";
		sb.append(String.format("  Data ordering:              %s\n", ordering));
		sb.append(String.format("  Entry point address:        0x%08X\n", entry));
		sb.append(String.format("  Flags:                      0x%08X\n", flags));
		sb.append(String.format("  Number of programs:           %8d\n", programs.size()));
		sb.append(String.format("  Number of sections:           %8d\n", sections.size()));

		if (programs != null) {
			sb.append("\nProgram Headers:\n");
			sb.append("  Type      Offset     VirtAddr   PhysAddr   FileSiz    MemSiz     Flg Align\n");
			for (Elf32Program program : programs) {
				if (program != null)
					sb.append(program.toString());
			}
		}
		if (sections != null) {
			sb.append("\nSection Headers:\n");
			sb.append("  [Nr]  Name                        Type       Address    Offset     Size       ES Flags Link Info   Alignment\n");
			for (int idx = 0; idx < sections.size(); idx++) {
				Elf32Section section = sections.get(idx);
				if (section != null)
					sb.append(String.format("  [%2d] ", idx)).append(section.toString()).append("\n");
			}
		}
		return sb.toString();
	}

	public static int readUnsigned16(ByteBuffer bb) {
		return ((int) bb.getShort()) & 0xFFFF;
	}
	private static String readString(ByteBuffer bb, int pos) {
		StringBuilder sb = new StringBuilder();
		while(true) {
			byte b = bb.get(pos);
			if (b == 0) break;
			pos++;
			sb.append((char) b);
		}
		return sb.toString();
	}
}
