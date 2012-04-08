package jmips.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jmips.cpu.disasm.LabelResolver;
import jmips.elf.Elf32;
import jmips.elf.Elf32Section;
import jmips.elf.Elf32SymbolTable;
import jmips.elf.Elf32SymbolTable.SymbolEntry;

public class Elf32LabelResolver implements LabelResolver {
	private Map<Integer, String> symbolsByAddress;
	private Map<String, Integer> symbolsByName;
	private int[] sortedAddresses;

	private void loadElf32SymbolTable(Elf32SymbolTable table) {
		if (symbolsByAddress == null)
			symbolsByAddress = new HashMap<Integer, String>();
		if (symbolsByName == null)
			symbolsByName = new HashMap<String, Integer>();
		sortedAddresses = null;
		for(SymbolEntry entry : table.getEntries()) {
			if (entry.getType() == SymbolEntry.STT_FUNC) {
				int address = entry.getValue();
				String name = entry.getName();
				symbolsByAddress.put(address, name);
				symbolsByName.put(name, address);
			}
		}
	}

	public void loadElf32SymbolTable(Elf32 elf) {
		if (elf == null) return;
		for(int i = 0; i < elf.getNumSections(); i++) {
			Elf32Section section = elf.getSection(i);
			if (section != null && section.getType() == Elf32Section.SHT_SYMTAB) {
				Elf32SymbolTable symbolTable = new Elf32SymbolTable();
				if (symbolTable.readElf32SymbolTable(elf, i)) {
					loadElf32SymbolTable(symbolTable);
				}
			}
		}
	}

	public boolean makeIndex() {
		int i, size;
		if (symbolsByAddress != null) { 
			size = symbolsByAddress.size();
			sortedAddresses = new int[size];
			i = 0;
			for(int address : symbolsByAddress.keySet()) {
				sortedAddresses[i++] = address;
			}
			Arrays.sort(sortedAddresses);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int resolveSymbolByName(String name) {
		if (symbolsByName != null)
			if (symbolsByName.containsKey(name))
				return symbolsByName.get(name);
		return 0;
	}

	@Override
	public String resolveSymbolByAddress(int address) {
		if (symbolsByAddress != null)
			if (symbolsByAddress.containsKey(address))
				return symbolsByAddress.get(address);
		return null;
	}

	@Override
	public int findNearestSymbolAddress(int address) {
		if (sortedAddresses == null)
			if (!makeIndex()) return 0;

		int index = Arrays.binarySearch(sortedAddresses, address);
		if (index >= 0) return sortedAddresses[index];
		int insertionPoint = -(index + 1);
		if (insertionPoint == 0) return 0;
		int nearestAddress = sortedAddresses[insertionPoint - 1];
		if (address - nearestAddress < 0)
			return 0;
		return nearestAddress;
	}

	public void clearSymbols() {
		symbolsByAddress = null;
		symbolsByName = null;
		sortedAddresses = null;
	}
}
