package jmips.dev;

import java.util.LinkedList;
import jmips.cpu.Device;

/**
 * Class implementing a Uart controller.
 * @author Humberto Silva Naves
 *
 */
public final class Uart implements Device {

	// Interrupt enable register constants
	public static final int UART_IER_ERBFI = 0x01;     // Enable received data available interrupt 
	public static final int UART_IER_ETBEI = 0x02;     // Enable transmitter holding register empty interrupt
	public static final int UART_IER_ELSI = 0x04;      // Enable receiver line status interrupt
	public static final int UART_IER_EDSSI = 0x08;     // Enable modem status interrupt
	public static final int UART_IER_MASK = 0x0F;

	// Interrupt identification register constants
	public static final int UART_IIR_NO_INT = 0x01;    // No interrupts pending 
	public static final int UART_IIR_THRI = 0x02;      // Transmitter holding register empty 
	public static final int UART_IIR_RDI = 0x04;       // Receiver data interrupt
	public static final int UART_IIR_CTO = 0x08;       // Character timeout
	public static final int UART_IIR_FE = 0x40;        // FIFO enabled
	public static final int UART_IIR_FIFO = 0x80;      // Has FIFO?

	// Fifo control register constants
	public static final int UART_FCR_FE = 0x01;        // FIFO enabled
	public static final int UART_FCR_CRF = 0x02;       // Clear receive FIFO
	public static final int UART_FCR_CTF = 0x04;       // Clear transmit FIFO
	public static final int UART_FCR_TRIG_MASK = 0xC0; // FIFO interrupt trigger level
	
	// These are the definitions for the Line Control Register
	public static final int UART_LCR_WLEN_MASK = 0x03; // Word length mask
	public static final int UART_LCR_STOP = 0x04;      // Stop bits
	public static final int UART_LCR_PAR_MASK = 0x38;  // Parity mask
	public static final int UART_LCR_BE = 0x40;        // Break signal enabled
	public static final int UART_LCR_DLAB = 0x80;      // Divisor latch access bit

	// These are the definitions for the Line Status Register
	public static final int UART_LSR_DR = 0x01;        // Receiver data available
	public static final int UART_LSR_OE = 0x02;        // Overrun error
	public static final int UART_LSR_PE = 0x04;        // Parity error
	public static final int UART_LSR_FE = 0x08;        // Framing error
	public static final int UART_LSR_BI = 0x10;        // Break signal received
	public static final int UART_LSR_THRE = 0x20;      // Transmit-hold-register empty
	public static final int UART_LSR_TEMT = 0x40;      // Transmitter empty and line is idle
	public static final int UART_LSR_DERR = 0x080;     // Erroneous data in FIFO

	// There are the definition for the Modem Control register
	public static final int UART_MCR_LOOP = 0x10;      // Loopback-mode

	// Parity types
	public static final int UART_NO_PARITY = 0;
	public static final int UART_ODD_PARITY = 1;
	public static final int UART_EVEN_PARITY = 2;
	public static final int UART_HIGH_PARITY = 3;
	public static final int UART_LOW_PARITY = 4;

	// FIFO size
	public static final int UART_FIFO_SIZE = 16;

	private int rbr;     //receive register
	private int ier;     // interrupt enable register
	private int iir;     // interrupt identification register
	private int lcr;     // line control register
	private int mcr;     // modem control register
	private int lsr;     // line status register
	private int msr;     // modem status register
	private int scr;     // scratch register

	private int fifo_threshold;
	private int divider;
	private boolean error;

	private final UartController controller;
	private final LinkedList<Byte> recvFifo = new LinkedList<Byte>(); // There is no transmit fifo

	public Uart(UartController controller) {
		this.controller = controller;
		reset();
	}

	@Override
	public void reset() {
		divider = 12;  // 9600 bps
		rbr = 0;
		ier = 0;
		iir = UART_IIR_NO_INT | UART_IIR_FIFO;
		lcr = 0;
		lsr = UART_LSR_TEMT | UART_LSR_THRE;
		msr = 0;
		scr = 0;
		mcr = 0;
		fifo_threshold = 1;
		recvFifo.clear();
	}

	@Override
	public boolean ioError() {
		return error;
	}

	private void updateIrq() {
		iir &= ~(UART_IIR_NO_INT | UART_IIR_THRI | UART_IIR_RDI | UART_IIR_CTO);
		if ((lsr & UART_LSR_DR) != 0 && (ier & UART_IER_ERBFI) != 0 && (recvFifo.size() + 1 >= fifo_threshold)) {
			iir |= UART_IIR_RDI;
		} else if ((lsr & UART_LSR_THRE) != 0 && (ier & UART_IER_ETBEI) != 0) {
			iir |= UART_IIR_THRI;
		} else {
			iir |= UART_IIR_NO_INT;
		}

		controller.changeIrqStatus((iir & UART_IIR_NO_INT)!= UART_IIR_NO_INT);
	}

	private void receiveByteFromFifo() {
		if (recvFifo.size() > 0 && (lsr & UART_LSR_DR) == 0) {
			byte b = recvFifo.remove();
			rbr = b;
			lsr |= UART_LSR_DR;
		}
		updateIrq();
	}

	private void updateFifoControlRegister(int fcr) {
		if ((fcr & UART_FCR_FE) == UART_FCR_FE) {
			iir |= UART_IIR_FE;
		} else {
			iir &= ~(UART_IIR_FE);
		}

		if ((fcr & UART_FCR_CRF) != 0) {
			recvFifo.clear();
		}
		// We don't clear the transmit FIFO because we don't have one
		switch(fcr & UART_FCR_TRIG_MASK) {
		case 0: fifo_threshold = 1; break;
		case 64: fifo_threshold = 4; break;
		case 128: fifo_threshold = 8; break;
		case 256:
		default:
			fifo_threshold = 14;
			break;
		}
	}

	private byte readReceiveRegister() {
		byte ret = (byte) rbr;
		lsr &= ~(UART_LSR_DR | UART_LSR_BI);
		updateIrq();
		receiveByteFromFifo();
		return ret;
	}

	private void writeTransmitRegister(byte value) {
		lsr &= ~(UART_LSR_THRE | UART_LSR_TEMT);
		updateIrq();
		if ((mcr & UART_MCR_LOOP) == 0) { // Loopback mode
			controller.write(value);
		} else {
			receiveByte(value);
		}
		lsr |= UART_LSR_THRE | UART_LSR_TEMT;
		updateIrq();
	}

	private void changeInterruptEnable(byte value) {
		ier = value & UART_IER_MASK;
		updateIrq();
	}

	public void receiveByte(byte b) {
		if (recvFifo.size() < UART_FIFO_SIZE - 1)
			recvFifo.add(b);
		receiveByteFromFifo();
	}

	public void expireReceiveBufferData() {
		if (!recvFifo.isEmpty() || (lsr & UART_LSR_DR) != 0) {
			updateIrq();
			if ((iir & UART_IIR_NO_INT) != 0) {
				iir &= ~UART_IIR_NO_INT;
				iir |= UART_IIR_CTO;
				controller.changeIrqStatus(true);
			}
		}
	}

	@Override
	public byte read8(int offset) {
		byte ret;

		error = false;
		offset &= 7;
		switch(offset) {
		default:
		case 0:
			if ((lcr & UART_LCR_DLAB) != 0) {
				ret = (byte) divider;
			} else {
				ret = readReceiveRegister();
			}
			break;
		case 1:
			if ((lcr & UART_LCR_DLAB) != 0) {
				ret = (byte) (divider >> 8);
			} else {
				ret = (byte) ier;
			}
			break;
		case 2:
			ret = (byte) iir;
			break;
		case 3:
			ret = (byte) lcr;
			break;
		case 4:
			ret = (byte) mcr;
			break;
		case 5:
			ret = (byte) lsr;
			break;
		case 6:
			ret = (byte) msr;
			break;
		case 7:
			ret = (byte) scr;
			break;
		}
		return ret;
	}

	@Override
	public void write8(int offset, byte value) {
		error = false;
		offset &= 7;
		switch(offset) {
		default:
		case 0:
			if ((lcr & UART_LCR_DLAB) != 0) {
				divider = (divider & 0xFF00) | (((int) value) & 0xFF);
			} else {
				writeTransmitRegister(value);
			}
			break;
		case 1:
			if ((lcr & UART_LCR_DLAB) != 0) {
				divider = (divider & 0x00FF) | ((((int) value) << 8) & 0xFF00);
			} else {
				changeInterruptEnable(value);
			}
			break;
		case 2:
			updateFifoControlRegister(value);
			break;
		case 3:
			lcr = value;
			break;
		case 4:
			mcr = value;
			break;
		case 5:
			break;
		case 6:
			msr = value;
			break;
		case 7:
			scr = value;
			break;
		}
	}

	public int getWordLength() {
		switch(lcr & UART_LCR_WLEN_MASK) {
		case 0: return 5;
		case 1: return 6;
		case 2: return 7;
		case 3: return 8;
		}
		return 5;
	}

	public int getStopBits() {
		return (lcr & UART_LCR_STOP) != 0 ? 2 : 1;
	}

	public int getParity() {
		switch(lcr & UART_LCR_PAR_MASK) {
		case 8: return UART_ODD_PARITY;
		case 24: return UART_EVEN_PARITY;
		case 40: return UART_HIGH_PARITY;
		case 56: return UART_LOW_PARITY;
		default:
			return UART_NO_PARITY;
		}
	}

	public int getSpeed() {
		return 115200 / divider;
	}

	// Reads/writes of sizes different than 8 aren't supported
	@Override
	public int read32(int offset) {
		// Invalid read
		error = true;
		return 0;
	}

	@Override
	public void write32(int offset, int value) {
		// Invalid write
		error = true;
	}

	@Override
	public short read16(int offset) {
		// Invalid read
		error = true;
		return 0;
	}

	@Override
	public void write16(int offset, short value) {
		// Invalid write
		error = true;
	}
}
