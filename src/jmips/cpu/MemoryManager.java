package jmips.cpu;

import java.util.ArrayList;
import java.util.List;

public final class MemoryManager extends Device {

	private final Ram ram;
	private final List<Device> devices = new ArrayList<Device>();

	public MemoryManager(int ramOffset, int ramSize) {
		super(0);
		this.ram = new Ram(ramOffset, ramSize);
	}

	public boolean registerDevice(Device device) {
		if (findDeviceAtAddress(device.getMappedOffset()) == null) {
			devices.add(device);
			return true;
		}
		return false;
	}

	public boolean unregisterDevice(Device device) {
		return devices.remove(device);
	}

	public Device findDeviceAtAddress(int address) {
		// Assuming that the region is entirely contained in one of the half-ranges, i.e.,
		// all the integers in the interval [offset, offset + size) have the same sign
		// that's why we don't use Utils.compareUnsigned
		if ((address >= ram.getMappedOffset())
		    && (address < ram.getMappedOffset() + ram.getDeviceSize())) {
			return ram;
		}
		for (Device device : devices) {
			int offset = device.getMappedOffset();
			int size = device.getDeviceSize();
			// Same assumption for the RAM memory
			if ((address >= offset) && (address < offset + size)) {
				return device;
			}
		}
		return null;
	}

	public Ram getRam() {
		return ram;
	}

	@Override
	public byte read8(int address) {
		Device device = findDeviceAtAddress(address);
		if (device == null) {
			error = true;
			return 0;
		}

		int offset = device.getMappedOffset();
		byte b = device.read8(address - offset);
		error = device.error();
		return b;
	}

	@Override
	public short read16(int address, boolean bigEndian) {
		Device device = findDeviceAtAddress(address);
		if (device == null) {
			error = true;
			return 0;
		}
		int offset = device.getMappedOffset();
		short s = device.read16(address - offset, bigEndian);
		error = device.error();
		return s;
	}

	@Override
	public int read32(int address, boolean bigEndian) {
		Device device = findDeviceAtAddress(address);
		if (device == null) {
			error = true;
			return 0;
		}
		int offset = device.getMappedOffset();
		int v = device.read32(address - offset, bigEndian);
		error = device.error();
		return v;
	}

	@Override
	public void write8(int address, byte value) {
		Device device = findDeviceAtAddress(address);
		if (device == null) {
			error = true;
			return;
		}
		int offset = device.getMappedOffset();
		device.write8(address - offset, value);
		error = device.error();
	}

	@Override
	public void write16(int address, short value, boolean bigEndian) {
		Device device = findDeviceAtAddress(address);
		if (device == null) {
			error = true;
			return;
		}
		int offset = device.getMappedOffset();
		device.write16(address - offset, value, bigEndian);
		error = device.error();
	}

	@Override
	public void write32(int address, int value, boolean bigEndian) {
		Device device = findDeviceAtAddress(address);
		if (device == null) {
			error = true;
			return;
		}
		int offset = device.getMappedOffset();
		device.write32(address - offset, value, bigEndian);
		error = device.error();
	}

	@Override
	public int getDeviceSize() {
		return -1;
	}

	@Override
	public void reset() {
		ram.reset();
		for (Device device : devices) {
			device.reset();
		}
	}
}
