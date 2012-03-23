package jmips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import jmips.cpu.Cpu;
import jmips.cpu.Utils;
import jmips.serial.SwingTTY;

public class Main {
	private static final int BASE_ADDRESS = 0x80100000;
	private static final int INITRD_ADDRESS = 0x803D9000;

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

	public static void main(String[] args) throws FileNotFoundException, IOException {
		final JFrame frame = createConsoleFrame();
		final SwingTTY tty = createSwingTTY(frame);
		final MipsSystem system = new MipsSystem(tty);
		frame.setVisible(true);

		system.reset();
		final Cpu cpu = system.getCpu();
		byte[] data = Utils.readFile(new FileInputStream("vmlinux.bin"));
		for(int i = 0; i < data.length; i++) {
			cpu.store8(BASE_ADDRESS + i, data[i]);
		}

		data = Utils.readFile(new FileInputStream("initrd.gz"));
		for(int i = 0; i < data.length; i++) {
			cpu.write8(INITRD_ADDRESS + i, data[i]);
		}

		cpu.setPc(BASE_ADDRESS);

		//GdbServer server = new GdbServer(cpu, tty);
		//server.startServer(1234);
		//return;
		for(int i = 0; i < 500000000; i++) {
			cpu.step();
		}
	}


}
