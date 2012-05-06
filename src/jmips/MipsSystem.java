package jmips;

import java.nio.ByteBuffer;

import jmips.cpu.Cpu;
import jmips.cpu.Device;
import jmips.cpu.Mips;
import jmips.dev.BlockDevice;
import jmips.dev.BlockDeviceController;
import jmips.dev.RealTimeClock;
import jmips.dev.Uart;
import jmips.dev.UartController;
import jmips.elf.Elf32;
import jmips.elf.Elf32Program;
import jmips.tty.TTY;
import jmips.utils.FileUtils;
import jmips.utils.Elf32LabelResolver;

/**
 * This class implements a Mips Computer System
 * @author Humberto Silva Naves
 */
public final class MipsSystem {
	public static final int IO_START = 0x1FD00000;          // Physical address of the I/O
	public static final int UART_BASE = IO_START + 0x03F8;  // Base physical address of the UART
	public static final int UART_END = UART_BASE + 32;      // End of the UART when mapped into memory
	public static final int RTC_BASE = IO_START + 0x0070;   // Base physical address of the RTC
	public static final int RTC_END = RTC_BASE + 64;        // End of the RTC
	public static final int BLOCK_BASE = IO_START + 0x0100; // Base physical address of the Block device
	public static final int BLOCK_END = BLOCK_BASE + 16;    // End of the block device

	public static final int UART_IRQ = 1;                   // IRQ number for the UART

	public static final int DEFAULT_RAM_SIZE = 64 * 1024 * 1024; // The default size of the RAM

	private final Cpu cpu;
	private final Uart uart;
	private final RealTimeClock rtc;
	private final BlockDevice block;
	private final TTY tty;
	private final Elf32LabelResolver labelResolver;
	private int entryPoint;

	public MipsSystem(TTY tty) {
		this(DEFAULT_RAM_SIZE, tty);
	}

	public MipsSystem(int ramSize, TTY tty) {
		this.cpu = createCpu(ramSize);
		this.uart = createUart();
		this.rtc = createRealTimeClock();
		this.block = createBlockDevice();
		this.tty = tty;
		this.labelResolver = new Elf32LabelResolver();
		this.cpu.setLabelResolver(this.labelResolver);
	}

	private Cpu createCpu(int ramSize) {
		final Device ioController = new Device() {
			private boolean error;
			private int deviceOffset;

			private Device findDevice(int address) {
				if (address >= BLOCK_BASE) {
					if (address < BLOCK_END) {
						deviceOffset = address - BLOCK_BASE;
						return block;
					}

					if (address >= UART_BASE && address < UART_END) {
						deviceOffset = address - UART_BASE;
						return uart;
					}
				} else {
					if (address >= RTC_BASE && address < RTC_END) {
						deviceOffset = address - RTC_BASE;
						return rtc;
					}
				}
				return null;
			}

			@Override
			public int read32(int offset) {
				Device dev = findDevice(offset);
				int ret;
				if (dev != null) {
					ret = dev.read32(deviceOffset);
					error = dev.ioError();
				} else {
					ret = 0;
					error = true;
				}
				return ret;
			}

			@Override
			public void write32(int offset, int value) {
				Device dev = findDevice(offset);
				if (dev != null) {
					dev.write32(deviceOffset, value);
					error = dev.ioError();
				} else {
					error = true;
				}
			}

			@Override
			public short read16(int offset) {
				Device dev = findDevice(offset);
				short ret;
				if (dev != null) {
					ret = dev.read16(deviceOffset);
					error = dev.ioError();
				} else {
					ret = 0;
					error = true;
				}
				return ret;
			}

			@Override
			public void write16(int offset, short value) {
				Device dev = findDevice(offset);
				if (dev != null) {
					dev.write16(deviceOffset, value);
					error = dev.ioError();
				} else {
					error = true;
				}
			}

			@Override
			public byte read8(int offset) {
				Device dev = findDevice(offset);
				byte ret;
				if (dev != null) {
					ret = dev.read8(deviceOffset);
					error = dev.ioError();
				} else {
					ret = 0;
					error = true;
				}
				return ret;
			}

			@Override
			public void write8(int offset, byte value) {
				Device dev = findDevice(offset);
				if (dev != null) {
					dev.write8(deviceOffset, value);
					error = dev.ioError();
				} else {
					error = true;
				}
			}

			@Override
			public boolean ioError() {
				return error;
			}

			@Override
			public void reset() {
			}
			
		};
		Cpu cpu = new Cpu(ramSize, ioController);
		return cpu;
	}

	private Uart createUart() {
		final UartController controller = new UartController() {
			@Override
			public void write(byte b) {
				tty.write(b);
			}
			
			@Override
			public void changeIrqStatus(boolean raise) {
				cpu.raiseIrq(UART_IRQ, raise);
			}
		};
		Uart uart = new Uart(controller);
		return uart;
	}

	private RealTimeClock createRealTimeClock() {
		RealTimeClock rtc = new RealTimeClock();
		return rtc;
	}

	public BlockDevice createBlockDevice() {
		final BlockDeviceController controller = new BlockDeviceController() {
			@Override
			public boolean readFromMemory(ByteBuffer sector, int address) {
				while(sector.hasRemaining()) {
					byte b = cpu.load8(address);
					if (cpu.getLastMemoryError() != Mips.MEMORY_ERROR_NOERROR)
						return false;
					sector.put(b);
					address++;
				}
				return true;
			}

			@Override
			public boolean writeToMemory(ByteBuffer sector, int address) {
				while(sector.hasRemaining()) {
					byte b = sector.get();
					cpu.store8(address, b);
					if (cpu.getLastMemoryError() != Mips.MEMORY_ERROR_NOERROR)
						return false;
					address++;
				}
				return true;
			}
		};
		BlockDevice block = new BlockDevice(controller);
		return block;
	}

	public Cpu getCpu() {
		return cpu;
	}

	public Uart getUart() {
		return uart;
	}

	public RealTimeClock getRealTimeClock() {
		return rtc;
	}

	public TTY getTTY() {
		return tty;
	}

	public int getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(int entryPoint) {
		this.entryPoint = entryPoint;
	}

	public void reset() {
		uart.reset();
		rtc.reset();
		tty.reset();
		cpu.reset();
		cpu.setPc(entryPoint);
	}

	public void setKernelCommandLine(String cmdLine, int address) {
		cpu.setGpr(Mips.GPR_A0, address);
		for (int i = 0; i < cmdLine.length(); i++) {
			byte b = (byte) cmdLine.charAt(i);
			cpu.store8(address, b);
			address++;
		}
		cpu.store8(address, (byte) 0);
	}

	public boolean setDiskFile(String fileName) {
		return block.setDiskFile(fileName);
	}

	public int load(int address, ByteBuffer bb) {
		while(bb.hasRemaining()) {
			byte data = bb.get();
			cpu.store8(address, data);
			address++;
		}
		return address;
	}

	public int load(int address, String fileName) {
		ByteBuffer bb = FileUtils.readFile(fileName);
		if (bb == null) return 0;
		return load(address, bb);
	}

	public int loadElf32(ByteBuffer bb) {
		Elf32 elf = new Elf32();
		int lastAddress = 0;

		if (elf.readElf32(bb) == Elf32.READ_SUCCESS) {
			setEntryPoint(elf.getEntry());
			for(int i = 0; i < elf.getNumPrograms(); i++) {
				Elf32Program program = elf.getProgram(i);
				if (program != null && program.getType() == Elf32Program.PT_LOAD &&
						program.getFileSize() != 0) {
					ByteBuffer data = program.getData();
					int ret = load(program.getVirtualAddress(), data);
					if (lastAddress < ret || lastAddress == 0)
						lastAddress = ret;
				}
			}
			labelResolver.loadElf32SymbolTable(elf);
		}
		return lastAddress;
	}

	public int loadElf32(String fileName) {
		ByteBuffer bb = FileUtils.readFile(fileName);
		if (bb == null) return 0;
		return loadElf32(bb);
	}

	public void step(int num) {
		cpu.step(num);
		while (tty.available()) {
			uart.receiveByte(tty.read());
		}
		uart.expireReceiveBufferData();
	}
}
