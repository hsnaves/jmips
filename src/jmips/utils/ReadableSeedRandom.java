package jmips.utils;

public class ReadableSeedRandom {
	private static final long multiplier = 0x5DEECE66DL;
	private static final long addend = 0xBL;
	private static final long mask = (1L << 48) - 1;

	private long seed = 0;

	public ReadableSeedRandom(long seed) {
		this.seed = seed;
	}

	private int nextRandom(int bits) {
		seed = (seed * multiplier + addend) & mask;
		return (int)(seed >>> (48 - bits));
	}

	public int nextRandomInt() {
		return nextRandom(32);
	}

	public int nextRandomInt(int n) {
		if (n <= 0)
			throw new IllegalArgumentException("n must be positive");

		if ((n & -n) == n)  // i.e., n is a power of 2
			return (int)((n * (long) nextRandom(31)) >> 31);

		int bits, val;
		do {
			bits = nextRandom(31);
			val = bits % n;
		} while (bits - val + (n-1) < 0);
		return val;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		seed = (seed * multiplier + addend) & mask;
		this.seed = seed;
	}
}

