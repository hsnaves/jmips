package jmips.cpu;

import java.util.ArrayList;
import java.util.List;

public final class MemoryManager {
	private final Ram ram;
	private final int ramOffset;

	private boolean success;

	private final List<IODevice> devices = new ArrayList<IODevice>();
	private final List<Integer> deviceOffsets = new ArrayList<Integer>();
	private final List<Integer> deviceSizes = new ArrayList<Integer>();

	public MemoryManager(int ramOffset, int ramSize) {
		ramOffset &= ~3; // Align by 4
		ramSize &= ~3;

		this.ram = new Ram(ramSize);
		this.ramOffset = ramOffset;
		this.success = true;
	}

	public boolean registerDevice(IODevice device, int offset) {
		offset &= ~3; // Align by 4
		int dev = findDevice(offset);
		if (dev == -1) {
			devices.add(device);
			deviceOffsets.add(offset);
			deviceSizes.add(device.size());
			return true;
		}
		return false;
	}

	public IODevice findDeviceAtAddress(int address) {
		int dev = findDevice(address);
		if (dev != -1) return devices.get(dev);
		return null;
	}

	private int findDevice(int address) {
		for(int dev = 0; dev < deviceOffsets.size(); dev++) {
			int offset = deviceOffsets.get(dev);
			int size = deviceSizes.get(dev);
			// Assuming that the region is entirely contained in one of the half-ranges, i.e.,
			// all the integers in the interval [offset, offset + size) have the same sign
			// that's why we don't use Utils.compareUnsigned
			if ((address >= offset) && (address < offset + size)) {
				return dev;
			}
		}
		return -1;
	}

	public boolean isMainMemory(int address) {
		return ((address >= ramOffset) && (address < ramOffset + ram.size()));
	}

	public byte read8(int address) {
		if (isMainMemory(address)) {
			success = true;
			return ram.read8(address - ramOffset);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				byte b = dev.read8(address - offset);
				success = !dev.bus_error();
				return b;
			} else {
				success = false;
				return 0;
			}
		}
	}

	public short read16(int address) {
		if (isMainMemory(address)) {
			success = true;
			return ram.read16(address - ramOffset, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				short s = dev.read16(address - offset, true);
				success = !dev.bus_error();
				return s;
			} else {
				success = false;
				return 0;
			}
		}
	}

	public int read32(int address) {
		if (isMainMemory(address)) {
			success = true;
			return ram.read32(address - ramOffset, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				int v = dev.read32(address - offset, true);
				success = !dev.bus_error();
				return v;
			} else {
				success = false;
				return 0;
			}
		}
	}

	public void write8(int address, byte value) {
		if (isMainMemory(address)) {
			success = true;
			ram.write8(address - ramOffset, value);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write8(address - offset, value);
				success = !dev.bus_error();
			} else {
				success = false;
			}
		}
	}

	public void write16(int address, short value) {
		if (isMainMemory(address)) {
			success = true;
			ram.write16(address - ramOffset, value, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write16(address - offset, value, true);
				success = !dev.bus_error();
			} else {
				success = false;
			}
		}
	}

	public void write32(int address, int value) {
		if (isMainMemory(address)) {
			success = true;
			ram.write32(address - ramOffset, value, true);
		} else {
			int devno = findDevice(address);
			if (devno != -1) {
				int offset = deviceOffsets.get(devno);
				IODevice dev = devices.get(devno);
				dev.write32(address - offset, value, true);
				success = !dev.bus_error();
			} else {
				success = false;
			}
		}
	}

	public boolean success() {
		return success;
	}
}
