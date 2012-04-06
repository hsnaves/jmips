package jmips.cpu;

import static org.junit.Assert.*;
import jmips.MipsSystem;
import jmips.serial.DummyTTY;

import org.junit.Test;

public class PerformanceTest {

	@Test
	public void testRunLinux() {
		DummyTTY tty = new DummyTTY();
		MipsSystem system = new MipsSystem(tty);
		int loadAddress = system.loadElf32("vmlinux");
		assertTrue(loadAddress != 0);
		assertTrue(system.setDiskFile("disk.raw"));
		system.setKernelCommandLine("root=/dev/sb1", loadAddress);
		system.reset();
		for(int i = 0; i < 1000000; i++)
			system.step(1);

		long time = System.currentTimeMillis();
		for(int i = 0; i < 40000000; i++)
			system.step(1);
		time = System.currentTimeMillis() - time;

		byte[] out = tty.getOutputBytes();
		System.out.println(new String(out));
		System.out.println(time);
	}
}
