package jmips;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import jmips.serial.SwingTTY;

public class Main {
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
		return tty;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
		final JFrame frame = createConsoleFrame();
		final SwingTTY tty = createSwingTTY(frame);
		final MipsSystem system = new MipsSystem(tty);
		frame.setVisible(true);

		String kernelFileName = "asm/sha1.bin";
		String initrdFileName = null; //"initrd.gz";
		system.setEntryPoint(BASE_ADDRESS);

		int initrdAddress = system.loadElf32(new FileInputStream(kernelFileName).getChannel());
		initrdAddress = (initrdAddress + 128 * 4096) & ~4095;
		if (initrdFileName != null) {
			int size = system.load(initrdAddress, new FileInputStream(initrdFileName).getChannel()) - initrdAddress;
			String cmdLine = String.format("rd_start=0x%08X rd_size=%d", initrdAddress, size);
			system.setKernelCommandLine(cmdLine, initrdAddress + size);
		}

		system.reset();
		//GdbServer server = new GdbServer(system);
		//server.startServer(1234);
		//return;

		while(true) {
			system.step(4000);
		}
	}


}
