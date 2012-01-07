package jmips.cpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CpuState {
	public final int[] gpr = new int[32];
	public int hi, lo;
	public int pc, next_pc;

	private final Ram ram;
	private final int ram_base;

	public boolean memory_ok;

	private final List<IODevice> devices = new ArrayList<IODevice>();
	private final List<Integer> deviceOffsets = new ArrayList<Integer>();
	private final List<Integer> deviceSizes = new ArrayList<Integer>();

	public CpuState(int ram_base, int ram_size) {
		this.ram_base = ram_base;
		this.ram = new Ram(ram_size);
	}

	public void registerDevice(IODevice device, int offset) {
		devices.add(device);
		deviceOffsets.add(offset);
		deviceSizes.add(device.size());
	}

	private int findDevice(int address) {
		for(int i = 0; i < deviceOffsets.size(); i++) {
			int offset = deviceOffsets.get(i);
			int size = deviceSizes.get(i);
			if ((address >= offset) && (address < offset + size)) {
				return i;
			}
		}
		return -1;
	}

	private boolean isMainMemory(int address) {
		return ((address >= ram_base) && (address < ram_base + ram.size()));
	}

	public byte read8(int address) {
		if (isMainMemory(address)) {
			memory_ok = true;
			return ram.read8(address - ram_base);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				byte b = dev.read8(address - offset);
				memory_ok = !dev.bus_error();
				return b;
			} else {
				memory_ok = false;
				return 0;
			}
		}
	}

	public short read16(int address) {
		if (isMainMemory(address)) {
			memory_ok = true;
			return ram.read16(address - ram_base, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				short s = dev.read16(address - offset, true);
				memory_ok = !dev.bus_error();
				return s;
			} else {
				memory_ok = false;
				return 0;
			}
		}
	}

	public int read32(int address) {
		if (isMainMemory(address)) {
			memory_ok = true;
			return ram.read32(address - ram_base, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				int v = dev.read32(address - offset, true);
				memory_ok = !dev.bus_error();
				return v;
			} else {
				memory_ok = false;
				return 0;
			}
		}
	}

	public void write8(int address, byte value) {
		if (isMainMemory(address)) {
			memory_ok = true;
			ram.write8(address - ram_base, value);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write8(address - offset, value);
				memory_ok = !dev.bus_error();
			} else {
				memory_ok = false;
			}
		}
	}

	public void write16(int address, short value) {
		if (isMainMemory(address)) {
			memory_ok = true;
			ram.write16(address - ram_base, value, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write16(address - offset, value, true);
				memory_ok = !dev.bus_error();
			} else {
				memory_ok = false;
			}
		}
	}

	public void write32(int address, int value) {
		if (isMainMemory(address)) {
			memory_ok = true;
			ram.write32(address - ram_base, value, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write32(address - offset, value, true);
				memory_ok = !dev.bus_error();
			} else {
				memory_ok = false;
			}
		}
	}

	public void reset() {
		Arrays.fill(gpr, 0);
		hi = lo = 0;
		memory_ok = true;

		pc = 0x08000000;
		next_pc = pc + 4;
	}

	public void raiseIrq(int irqno) {
	}

	public void lowerIrq(int irqno) {
	}
}
