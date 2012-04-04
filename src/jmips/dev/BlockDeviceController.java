package jmips.dev;

import java.nio.ByteBuffer;

public interface BlockDeviceController {
	public boolean readFromMemory(ByteBuffer sector, int address);
	public boolean writeToMemory(ByteBuffer sector, int address);
}
