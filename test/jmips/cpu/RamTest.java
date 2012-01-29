package jmips.cpu;

import static org.junit.Assert.*;

import org.junit.Test;

public class RamTest {

	@Test
	public void test1() {
		Ram ram = new Ram(0, 0x1000);
		ram.write8(0, (byte) 0x11);
		ram.write8(1, (byte) 0x22);
		ram.write8(2, (byte) 0x33);
		ram.write8(3, (byte) 0x44);

		assertEquals(0x11223344, ram.read32(0, true));
		assertEquals(0x44332211, ram.read32(0, false));
	}

	@Test
	public void test2() {
		Ram ram = new Ram(0, 0x1000);
		ram.write32(0, 0x11223344, false);
		assertEquals(0x44, ram.read8(0));
		assertEquals(0x33, ram.read8(1));
		assertEquals(0x22, ram.read8(2));
		assertEquals(0x11, ram.read8(3));
	}

	@Test
	public void test3() {
		Ram ram = new Ram(0, 0x1000);
		ram.write16(0, (short) 0x1122, false);
		ram.write16(2, (short) 0x3344, true);

		assertEquals(0x22113344, ram.read32(0, true));
		assertEquals(0x44331122, ram.read32(0, false));
	}

	@Test
	public void test4() {
		Ram ram = new Ram(0, 0x1000);
		ram.write8(0, (byte) 0x11);
		ram.write8(1, (byte) 0x22);

		assertEquals(0x1122, ram.read16(0, true));
		assertEquals(0x2211, ram.read16(0, false));
	}

	@Test
	public void test5() {
		Ram ram = new Ram(0, 0x1000);
		ram.write16(0, (short) 0x1122, true);
		ram.write8(2, (byte) 0x33);
		ram.write8(3, (byte) 0x44);

		assertEquals(0x1122, ram.read16(0, true));
		assertEquals(0x2211, ram.read16(0, false));
		assertEquals(0x11223344, ram.read32(0, true));
		assertEquals(0x44332211, ram.read32(0, false));
	}

}
