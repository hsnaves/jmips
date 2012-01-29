package jmips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import jmips.cpu.Cpu;
import jmips.cpu.Disassemble;
import jmips.cpu.Utils;
import jmips.dev.RealTimeClock;
import jmips.dev.Uart16550;
import jmips.serial.ConsoleTTY;
import jmips.serial.SwingTTY;

public class Main {
	private static final int RAM_BASE = 0x00000000;
	private static final int RAM_SIZE = 64 * 1024 * 1024;
	private static final int UART_BASE = 0x1FD003F8;
	private static final int RTC_BASE = 0x1FD00070;
	private static final int BASE_ADDRESS = 0x80100000;

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
		cpu.setProgramCounter(BASE_ADDRESS);
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
				System.out.println("Error");
				return;
			}
		}
		for(int j = 0; j < 10; j++) {
			for(int i = 0; i < 400000000; i++) {
				if (cpu.isHalted()) break;
				//System.out.println(Disassemble.disassemble(pc, opcode));
				cpu.step();
				while(tty.available())
					uart.sendChar(tty.read());
			}
			if (cpu.isHalted()) break;
		}
		System.out.println(cpu.getCounter());
		System.out.println(cpu.getCompare());
	}
}
