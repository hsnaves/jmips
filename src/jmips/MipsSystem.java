package jmips;

import jmips.cpu.Cpu;
import jmips.cpu.Device;
import jmips.dev.RealTimeClock;
import jmips.dev.Uart;
import jmips.dev.UartController;
import jmips.serial.TTY;

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

	public static final int UART_IRQ = 1;                   // IRQ number for the UART

	public static final int DEFAULT_RAM_SIZE = 64 * 1024 * 1024; // The default size of the RAM
	public static final int DEFAULT_KERNEL_START = 0x80100000;   // Where to load the kernel

	private final Cpu cpu;
	private final Uart uart;
	private final RealTimeClock rtc;
	private final TTY tty;

	public MipsSystem(TTY tty) {
		this(DEFAULT_RAM_SIZE, tty);
	}

	public MipsSystem(int ramSize, TTY tty) {
		this.cpu = createCpu(ramSize);
		this.uart = createUart();
		this.rtc = createRealTimeClock();
		this.tty = tty;
	}

	private Cpu createCpu(int ramSize) {
		final Device ioController = new Device() {
			private boolean error;
			private int deviceOffset;

			private Device findDevice(int address) {
				if (address >= UART_BASE) {
					if (address < UART_END) return uart;
				} else {
					if (address >= RTC_BASE && address < RTC_END)
						return rtc;
				}
				return null;
			}

			@Override
			public int read32(int offset, boolean bigEndian) {
				Device dev = findDevice(offset);
				int ret;
				if (dev != null) {
					ret = dev.read32(deviceOffset, bigEndian);
					error = dev.ioError();
				} else {
					ret = 0;
					error = true;
				}
				return ret;
			}

			@Override
			public void write32(int offset, int value, boolean bigEndian) {
				Device dev = findDevice(offset);
				if (dev != null) {
					dev.write32(deviceOffset, value, bigEndian);
					error = dev.ioError();
				} else {
					error = true;
				}
			}

			@Override
			public short read16(int offset, boolean bigEndian) {
				Device dev = findDevice(offset);
				short ret;
				if (dev != null) {
					ret = dev.read16(deviceOffset, bigEndian);
					error = dev.ioError();
				} else {
					ret = 0;
					error = true;
				}
				return ret;
			}

			@Override
			public void write16(int offset, short value, boolean bigEndian) {
				Device dev = findDevice(offset);
				if (dev != null) {
					dev.write16(deviceOffset, value, bigEndian);
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
				MipsSystem.this.reset();
			}
			
		};
		Cpu cpu = new Cpu(ramSize, ioController);
		return cpu;
	}

	private Uart createUart() {
		final UartController controller = new UartController() {
			@Override
			public void write(byte b) {
			}
			
			@Override
			public void changeIrqStatus(boolean raise) {
				cpu.lowerIrq(UART_IRQ);
			}
		};
		Uart uart = new Uart(controller);
		return uart;
	}

	private RealTimeClock createRealTimeClock() {
		RealTimeClock rtc = new RealTimeClock();
		return rtc;
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

	public void reset() {
		uart.reset();
		rtc.reset();
		tty.reset();
		cpu.reset();
	}
}
