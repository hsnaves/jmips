package jmips.dev;

import java.util.LinkedList;

import jmips.cpu.Cpu;
import jmips.cpu.Device;
import jmips.serial.TTY;

public final class Uart16550 extends Device {

	/* Generated serialVersionUID */
	private static final long serialVersionUID = 2487224479818994761L;

	// Interrupt enable register constants

	private static final int UART_IER_THRI = 0x02;   // Enable Transmitter holding register int. 
	private static final int UART_IER_RDI = 0x01;    // Enable receiver data interrupt 

	// Interrupt identification register constants

	private static final int UART_IIR_NO_INT = 0x01; // No interrupts pending 

	private static final int UART_IIR_THRI = 0x02;   // Transmitter holding register empty 
	private static final int UART_IIR_RDI = 0x04;    // Receiver data interrupt 

	// These are the definitions for the Line Control Register

	private static final int UART_LCR_DLAB = 0x80;   // Divisor latch access bit
	// Data word length, stop bits, parity and break signal are simply ignored here


	// These are the definitions for the Line Status Register

	private static final int UART_LSR_TEMT = 0x40;   // Transmitter empty
	private static final int UART_LSR_THRE = 0x20;   // Transmit-hold-register empty
	private static final int UART_LSR_BI = 0x10;     // Break interrupt indicator
	private static final int UART_LSR_DR = 0x01;     // Receiver data ready


	private final TTY tty;
	private final int irq;
	private final Cpu cpu;

	private int divider; // divider
	private int rbr; //receive register
	private int ier; // interrupt enable register
	private int iir; // interrupt identification register
	private int lcr; // line control register
	private int mcr; // modem control register
	private int lsr; // line status register
	private int msr; // modem status register
	private int scr; // scratch register


	private final LinkedList<Byte> recvFifo = new LinkedList<Byte>();

	public Uart16550(int mappedOffset, Cpu cpu, TTY tty, int irq) {
		super(mappedOffset);
		this.cpu = cpu;
		this.tty = tty;
		this.irq = irq;
		reset();
	}

	@Override
	public void reset() {
		divider = 0;
		rbr = 0;
		ier = 0;
		iir = UART_IIR_NO_INT;
		lcr = 0;
		lsr = UART_LSR_TEMT | UART_LSR_THRE;
		msr = 0;
		scr = 0;
		mcr = 0;
		recvFifo.clear();
		tty.reset();
	}

	@Override
	public int read32(int offset, boolean bigEndian) {
		// Invalid read
		error = true;
		return 0;
	}

	@Override
	public void write32(int offset, int value, boolean bigEndian) {
		// Invalid write
		error = true;
	}

	@Override
	public short read16(int offset, boolean bigEndian) {
		// Invalid read
		error = true;
		return 0;
	}

	@Override
	public void write16(int offset, short value, boolean bigEndian) {
		error = true;
	}

	private void updateIrq() {
		if ((lsr & UART_LSR_DR) != 0 && (ier & UART_IER_RDI) != 0) {
			iir = UART_IIR_RDI;
		} else if ((lsr & UART_LSR_THRE) != 0 && (ier & UART_IER_THRI) != 0) {
			iir = UART_IIR_THRI;
		} else {
			iir = UART_IIR_NO_INT;
		}

		if (iir != UART_IIR_NO_INT) {
			cpu.raiseIrq(irq);
		} else {
			cpu.lowerIrq(irq);
		}
	}

	private void sendCharFromFifo() {
		if (recvFifo.size() > 0 && (lsr & UART_LSR_DR) == 0) {
			byte b = recvFifo.remove();
			rbr = b;
			lsr |= UART_LSR_DR;
			updateIrq();
		}
	}

	@Override
	public byte read8(int offset) {
		byte ret;
		
		offset &= 7;
		switch(offset) {
		default:
		case 0:
			if ((lcr & UART_LCR_DLAB) != 0) {
				ret = (byte) divider;
			} else {
				ret = (byte) this.rbr;
				lsr &= ~(UART_LSR_DR | UART_LSR_BI);
				updateIrq();
				sendCharFromFifo();
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
		offset &= 7;
		switch(offset) {
		default:
		case 0:
			if ((lcr & UART_LCR_DLAB) != 0) {
				divider = (divider & 0xFF00) | (((int) value) & 0xFF);
			} else {
				lsr &= ~UART_LSR_THRE;
				updateIrq();
				tty.write((char) value);
				lsr |= UART_LSR_THRE;
				lsr |= UART_LSR_TEMT;
				updateIrq();
			}
			break;
		case 1:
			if ((lcr & UART_LCR_DLAB) != 0) {
				divider = (divider & 0x00FF) | ((((int) value) << 8) & 0xFF00);
			} else {
				ier = value;
				updateIrq();
			}
			break;
		case 2:
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

	public void sendChar(char c) {
		recvFifo.add((byte) c);
		sendCharFromFifo();
	}

	public void sendString(String str) {
		for(int i = 0; i < str.length(); i++)
			recvFifo.add((byte) str.charAt(i));
		sendCharFromFifo();
	}

	@Override
	public String getDeviceName() {
		return "uart16550";
	}

	@Override
	public int getDeviceSize() {
		return 8;
	}
}
