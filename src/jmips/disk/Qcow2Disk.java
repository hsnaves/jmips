package jmips.disk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Qcow2Disk {
	private static final int QCOW_MAGIC = 0x514649FB;
	private static final int QCOW_VERSION2 = 2;
	private static final int QCOW2_CRYPT_NONE = 0;
	private static final int QCOW2_HEADER_SIZE = 72;

	private RandomAccessFile diskFile;
	private String backingFileName;
	private RandomAccessFile backingFile;

	private int clusterBits, clusterSize;
	private long size;
	private int l1NumEnries;
	private long l1Offset;
	private long refCountTableOffset;
	private int refCountableClusters;

	private List<QCOW2Snapshot> snapshots;

	public int getClusterSize() {
		return clusterSize;
	}

	public void setClusterSize(int clusterSize) {
		this.clusterSize = clusterSize;
	}

	public int getClusterBits() {
		return clusterBits;
	}

	public void setClusterBits(int clusterBits) {
		this.clusterBits = clusterBits;
	}

	public String getBackingFileName() {
		return backingFileName;
	}

	public void setBackingFileName(String backingFileName) {
		this.backingFileName = backingFileName;
	}

	public RandomAccessFile getBackingFile() {
		return backingFile;
	}

	public void setBackingFile(RandomAccessFile backingFile) {
		this.backingFile = backingFile;
	}

	public RandomAccessFile getDiskFile() {
		return diskFile;
	}

	public void setDiskFile(RandomAccessFile diskFile) {
		this.diskFile = diskFile;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getL1Offset() {
		return l1Offset;
	}

	public void setL1Offset(long l1Offset) {
		this.l1Offset = l1Offset;
	}

	public int getL1NumEnries() {
		return l1NumEnries;
	}

	public void setL1NumEnries(int l1NumEnries) {
		this.l1NumEnries = l1NumEnries;
	}

	public long getRefCountTableOffset() {
		return refCountTableOffset;
	}

	public void setRefCountTableOffset(long refCountTableOffset) {
		this.refCountTableOffset = refCountTableOffset;
	}

	public int getRefCountableClusters() {
		return refCountableClusters;
	}

	public void setRefCountableClusters(int refCountableClusters) {
		this.refCountableClusters = refCountableClusters;
	}

	public List<QCOW2Snapshot> getSnapshots() {
		return snapshots;
	}

	public void setSnapshots(List<QCOW2Snapshot> snapshots) {
		this.snapshots = snapshots;
	}

	public boolean readDisk(RandomAccessFile raf) {
		ByteBuffer bb = readByteBuffer(raf, 0, QCOW2_HEADER_SIZE);

		String backingFileName = null;
		int magic = bb.getInt();
		if (magic != QCOW_MAGIC) return false;

		int version = bb.getInt();
		if (version != QCOW_VERSION2) return false;

		long backingFileOffset = bb.getLong();
		int backingFileSize = bb.getInt();

		if (backingFileSize != 0) {
			backingFileName = readString(raf, backingFileOffset, backingFileSize);
			if (backingFileName == null) return false;
		}

		int clusterBits = bb.getInt();
		long size = bb.getLong();
		int cryptMethod = bb.getInt();
		if (cryptMethod != QCOW2_CRYPT_NONE) return false; // No encryption supported

		int l1NumEntries = bb.getInt();
		long l1Offset = bb.getLong();
		System.out.printf("l1Offset = 0x%08X\n", l1Offset);

		int clusterSize = 1 << clusterBits;
		long refCountTableOffset = bb.getLong();
		int refCountTableClusters = bb.getInt();
		System.out.printf("refCountTableOffset = 0x%08X, refCountTableClusters=%d\n", refCountTableOffset, refCountTableClusters);

		int numSnapshots = bb.getInt();
		long snapshotsOffset = bb.getLong();
		List<QCOW2Snapshot> snapshots = readSnapshots(raf, snapshotsOffset, numSnapshots);
		if (snapshots == null) return false;

		setDiskFile(raf);
		setClusterBits(clusterBits);
		setClusterSize(clusterSize);
		setSize(size);
		setL1NumEnries(l1NumEntries);
		setL1Offset(l1Offset);
		setRefCountableClusters(refCountTableClusters);
		setRefCountTableOffset(refCountTableOffset);
		setSnapshots(snapshots);

		return true;
	}

	private List<QCOW2Snapshot> readSnapshots(RandomAccessFile raf, long offset, int numSnapshots) {
		List<QCOW2Snapshot> snapshots = new ArrayList<QCOW2Snapshot>(numSnapshots);
		for(int i = 0; i < numSnapshots; i++) {
			QCOW2Snapshot snapshot = new QCOW2Snapshot();
			long newOffset = snapshot.readSnapshot(raf, offset); 
			if (offset == newOffset) return null;
			offset = newOffset;
			snapshots.add(snapshot);
		}
		return snapshots;
	}

	private ByteBuffer readByteBuffer(RandomAccessFile raf, long offset, int size) {
		ByteBuffer bb = null;
		try {
			FileChannel fc = raf.getChannel();
			fc.position(offset);
			bb = ByteBuffer.allocate(size);
			if (fc.read(bb) != size)
				return null;
			bb.rewind();
			bb.order(ByteOrder.BIG_ENDIAN);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return bb;
	}

	private String readString(RandomAccessFile raf, long offset, int size) {
		byte []data = null;
		try {
			raf.seek(offset);
			data = new byte[size];
			raf.read(data);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new String(data);
	}

	public static class QCOW2Snapshot {
		private RandomAccessFile diskFile;
		private long snapshotOffset;
		private long l1Offset;
		private int l1Size;
		private String id, name;
		private int dateSec, dateNSec;
		private long clockNSec;
		private int vmStateSize;
		private int extraDataSize;

		public long getSnapshotOffset() {
			return snapshotOffset;
		}
		public void setSnapshotOffset(long snapshotOffset) {
			this.snapshotOffset = snapshotOffset;
		}

		public long getL1Offset() {
			return l1Offset;
		}
		public void setL1Offset(long l1Offset) {
			this.l1Offset = l1Offset;
		}

		public int getL1Size() {
			return l1Size;
		}
		public void setL1Size(int l1Size) {
			this.l1Size = l1Size;
		}

		public int getDateNSec() {
			return dateNSec;
		}
		public void setDateNSec(int dateNSec) {
			this.dateNSec = dateNSec;
		}

		public long getClockNSec() {
			return clockNSec;
		}
		public void setClockNSec(long clockNSec) {
			this.clockNSec = clockNSec;
		}

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		public int getDateSec() {
			return dateSec;
		}
		public void setDateSec(int dateSec) {
			this.dateSec = dateSec;
		}

		public int getExtraDataSize() {
			return extraDataSize;
		}
		public void setExtraDataSize(int extraDataSize) {
			this.extraDataSize = extraDataSize;
		}

		public int getVmStateSize() {
			return vmStateSize;
		}
		public void setVmStateSize(int vmStateSize) {
			this.vmStateSize = vmStateSize;
		}

		public RandomAccessFile getDiskFile() {
			return diskFile;
		}
		public void setDiskFile(RandomAccessFile diskFile) {
			this.diskFile = diskFile;
		}

		public long readSnapshot(RandomAccessFile diskFile, long offset) {
			return offset;
		}
	}

	public static void main(String[] args) {
		Qcow2Disk disk = new Qcow2Disk();
		String fileName = "test.qcow2";
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(fileName, "r");
			disk.readDisk(raf);
		} catch (FileNotFoundException ex) {
			System.err.printf("File `%s' not found\n", fileName);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}