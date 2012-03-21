package jmips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import jmips.cpu.Cpu;
import jmips.cpu.Utils;
import jmips.dev.RealTimeClock;
import jmips.dev.Uart16550;
import jmips.serial.SwingTTY;

public class Main {
	private static final int RAM_BASE = 0x00000000;
	private static final int RAM_SIZE = 64 * 1024 * 1024;
	private static final int UART_BASE = 0x1FD003F8;
	private static final int RTC_BASE = 0x1FD00070;
	private static final int BASE_ADDRESS = 0x80100000;
	private static final int START_ADDRESS = 0x802D2A80;
	private static final int INITRD_ADDRESS = 0x8040D000;

	private static JFrame createConsoleFrame() {
		JFrame frame = new JFrame("JMIPS");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		return frame;
	}

	private static SwingTTY createSwingTTY(JFrame frame) {
		SwingTTY tty = new SwingTTY();
		frame.getContentPane().add(tty);

		frame.pack();
		tty.open();
		tty.setEchoEnabled(true);
		return tty;
	}

	private static Cpu createCpu() {
		Cpu cpu = new Cpu(RAM_BASE, RAM_SIZE);
		return cpu;
	}

	private static void reset(Cpu cpu) {
		cpu.reset();
		cpu.setPc(START_ADDRESS);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		JFrame frame = createConsoleFrame();
		SwingTTY tty = createSwingTTY(frame);
		Cpu cpu = createCpu();
		Uart16550 uart = new Uart16550(UART_BASE, cpu, tty, 1);
		RealTimeClock rtc = new RealTimeClock(RTC_BASE);
		cpu.getMemoryManager().registerDevice(rtc);
		cpu.getMemoryManager().registerDevice(uart);
		reset(cpu);
		frame.setVisible(true);

		byte[] data = Utils.readFile(new FileInputStream("vmlinux.bin"));
		for(int i = 0; i < data.length; i++) {
			cpu.write8(BASE_ADDRESS + i, data[i]);
			if (!cpu.success()) {
				System.out.println("Error loading linux");
				return;
			}
		}

		data = Utils.readFile(new FileInputStream("initrd.gz"));
		for(int i = 0; i < data.length; i++) {
			cpu.write8(INITRD_ADDRESS + i, data[i]);
			if (!cpu.success()) {
				System.out.println("Error loading inird");
				return;
			}
		}

		GdbServer server = new GdbServer(cpu, tty);
		server.startServer(1234);
		return;
		/*
		for(int i = 0; i < 500000000; i++) {
			if (cpu.isHalted()) break;
			cpu.step();
			while(tty.available())
				uart.sendChar(tty.read());
		}*/
	}


}
