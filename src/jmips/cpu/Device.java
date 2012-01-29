package jmips.cpu;

import java.io.Serializable;

public abstract class Device implements Serializable {
	/* Generated serialVersionUID */
	private static final long serialVersionUID = 4857690609302741932L;

	private int mappedOffset;
	protected boolean error;

	public Device(int mappedOffset) {
		setMappedOffset(mappedOffset);
		this.error = false;
	}

	public final int getMappedOffset() {
		return mappedOffset;
	}

	public final void setMappedOffset(int offset) {
		this.mappedOffset = offset & (~3);
	}

	public final boolean error() {
		return error;
	}

	public abstract String getDeviceName();
	public abstract int getDeviceSize();
	public abstract int read32(int offset, boolean bigEndian);
	public abstract void write32(int offset, int value, boolean bigEndian);
	public abstract short read16(int offset, boolean bigEndian);
	public abstract void write16(int offset, short value, boolean bigEndian);
	public abstract byte read8(int offset);
	public abstract void write8(int offset, byte value);
	public abstract void reset();

	@Override
	public String toString() {
		return getDeviceName();
	}
}
