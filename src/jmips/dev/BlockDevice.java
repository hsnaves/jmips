package jmips.dev;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import jmips.cpu.Device;
import jmips.utils.FileUtils;

/**
 * A simple block device
 * @author Humberto Silva Naves
 *
 */
public class BlockDevice implements Device {
	public static final int REG_STATUS = 0;
	public static final int REG_CONTROL = 4;
	public static final int REG_SECTOR = 8;
	public static final int REG_ADDRESS = 12;
	public static final int REG_END = 16;

	public static final int CMD_RESET = 0;
	public static final int CMD_READ  = 1;
	public static final int CMD_WRITE = 2;
	public static final int CMD_GEO = 3;

	public static final int STATUS_IDLE = 0;
	public static final int STATUS_BUSY = 0x00000001;
	public static final int STATUS_ERROR = 0x80000000;

	public static final int SECTOR_SIZE = 512;

	private final BlockDeviceController controller;
	private RandomAccessFile diskFile;
	private boolean ioError;

	private int status;
	private int sector;
	private int address;

	public BlockDevice(BlockDeviceController controller) {
		this.controller = controller;
	}

	public boolean setDiskFile(String fileName) {
		try {
			diskFile = new RandomAccessFile(fileName, "rws");
		} catch (FileNotFoundException ex) {
			System.err.printf("Backing file `%s' not found\n", fileName);
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public int read32(int offset) {
		if (offset >= REG_END) {
			ioError = true;
			return 0;
		}
		ioError = false;
		int val = 0;
		switch (offset) {
		case REG_STATUS:  val = status; break;
		case REG_CONTROL: break;
		case REG_SECTOR:  val = sector; break;
		case REG_ADDRESS: val = address; break;
		default:
			ioError = true;
		}
		//System.out.printf("R%d = %d\n", offset, val);
		return val;
	}

	@Override
	public void write32(int offset, int value) {
		if (offset >= REG_END) {
			ioError = true;
			return;
		}
		ioError = false;
		//System.out.printf("W%d = %d\n", offset, value);
		switch (offset) {
		case REG_STATUS:  break;
		case REG_CONTROL: writeControlRegister(value); break;
		case REG_SECTOR:  sector = value;  break;
		case REG_ADDRESS: address = value; break;
		default:
			ioError = true;
		}
	}

	private void writeControlRegister(int value) {
		ByteBuffer buf = null;
		switch(value) {
		case CMD_RESET:
			status = STATUS_IDLE;
			break;
		case CMD_READ:
			status = STATUS_BUSY;
			if (diskFile != null) {
				buf = FileUtils.readFileFragment(diskFile.getChannel(), sector * SECTOR_SIZE, SECTOR_SIZE);
				if (buf != null) {
					if (controller.writeToMemory(buf, address)) {
						status = STATUS_IDLE;
					} else {
						status = STATUS_ERROR;
					}
				} else {
					status = STATUS_ERROR;
				}
			} else {
				status = STATUS_ERROR;
			}
			break;
		case CMD_WRITE:
			status = STATUS_BUSY;
			if (diskFile != null) {
				buf = ByteBuffer.allocate(SECTOR_SIZE);
				if (controller.readFromMemory(buf, address)) {
					buf.rewind();
					if (FileUtils.writeFileFragment(diskFile.getChannel(), sector * SECTOR_SIZE, buf)) {
						status = STATUS_IDLE;
					} else {
						status = STATUS_ERROR;
					}
				} else {
					status = STATUS_ERROR;
				}
			} else {
				status = STATUS_ERROR;
			}
			break;
		case CMD_GEO:
			status = STATUS_BUSY;
			try {
				sector = ((int) diskFile.getChannel().size()) / SECTOR_SIZE;
				status = STATUS_IDLE;
			} catch(IOException ex) {
				ex.printStackTrace();
				sector = 0;
				status = STATUS_ERROR;
			}
			break;
		default:
			status = STATUS_ERROR;
			break;
		}
	}

	@Override
	public short read16(int offset) {
		ioError = true;
		return 0;
	}

	@Override
	public void write16(int offset, short value) {
		ioError = true;
	}

	@Override
	public byte read8(int offset) {
		ioError = true;
		return 0;
	}

	@Override
	public void write8(int offset, byte value) {
		ioError = true;
	}

	@Override
	public boolean ioError() {
		return ioError;
	}

	@Override
	public void reset() {
	}
}
