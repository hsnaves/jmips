/**
 * Persistent storage for Jmips.
 * Kernel driver.
 * Copyright (C) 2011 Humberto Silva Naves
 *
 */

#include <linux/fs.h>
#include <linux/init.h>
#include <linux/module.h>
#include <linux/blkdev.h>
#include <linux/hdreg.h>
#include <linux/kernel.h>
#include <linux/slab.h>
#include <linux/types.h>
#include <linux/io.h>


#define MAJOR_NODE 250
#define MINORS 16

#define SECTOR_SIZE 512        /* Size of each sector */
#define DEV_NAME "sb"          /* Device Name */

/* Register information */
#define REG_BASE      0x0100 /* Base register */
#define REG_STATUS    0
#define REG_CONTROL   4
#define REG_SECTOR    8
#define REG_ADDRESS  12

#define REG_LEN      16

#define CMD_READ      1
#define CMD_WRITE     2
#define CMD_GEO       3

MODULE_LICENSE("MIT");
MODULE_AUTHOR("Humberto Silva Naves <hsnaves@gmail.com>");
MODULE_DESCRIPTION("Simple Block device driver for Jmips");

/* Driver stuff */
struct sb_dev {
  spinlock_t lock;
  struct gendisk *gd; /* Gendisk struct */
  struct request_queue *queue; /* Request queue */
};

static struct sb_dev *sbd = NULL;

static int sb_num_sectors(void) {
	unsigned int num_sectors;
	outl (CMD_GEO, REG_BASE + REG_CONTROL);
	num_sectors = inl (REG_BASE + REG_SECTOR);
	return num_sectors;
}

/*
 * The HDIO_GETGEO ioctl is handled in blkdev_ioctl(), which
 * calls this. We need to implement getgeo, since we can't
 * use tools such as fdisk to partition the drive otherwise.
 */
int sb_getgeo(struct block_device *bdev, struct hd_geometry *geo) {
	long size = sb_num_sectors();

	/* We have no real geometry, of course, so make something up. */
	geo->cylinders = (size & ~0x3f) >> 6;
	geo->heads = 1;
	geo->sectors = 16;
	geo->start = 0;
	return 0;
}

/* The device operations structure. */
static struct block_device_operations sb_fops = {
	.owner		= THIS_MODULE,
	.getgeo		= sb_getgeo
};

static int sb_write_sector(sector_t sector, char *buffer) {
	unsigned int status;
	outl ((int) buffer, REG_BASE + REG_ADDRESS);
	outl (sector, REG_BASE + REG_SECTOR);
	outl (CMD_WRITE, REG_BASE + REG_CONTROL);
	status = inl (REG_BASE + REG_STATUS);
	return (status & 0x80000000) != 0;
}

static int sb_read_sector(sector_t sector, char *buffer) {
	unsigned int status;
	outl ((int) buffer, REG_BASE + REG_ADDRESS);
	outl (sector, REG_BASE + REG_SECTOR);
	outl (CMD_READ, REG_BASE + REG_CONTROL);
	status = inl (REG_BASE + REG_STATUS);
	return (status & 0x80000000) != 0;
}

static int sb_transfer(struct sb_dev *dev, sector_t sector, unsigned long nsect, char *buffer, int write) {
	unsigned int s;
	int error = 0;

	/* Each transfer happens in a spinlock */
	spin_lock(&dev->lock);

	for (s = 0; s < nsect; s++) {
		if (write) { /* Do work for a write operation... */
			error = sb_write_sector(sector + s, buffer);
			buffer += SECTOR_SIZE;
			if (error) {
				printk(KERN_WARNING "sb: can't write sector %ld\n", sector + s);
				goto release_lock;
			}
		} else { /* Or for a read operation. */
			error = sb_read_sector(sector + s, buffer);
			buffer += SECTOR_SIZE;
			if (error) {
				printk(KERN_WARNING "sb: can't read sector %ld\n", sector + s);
				goto release_lock;
			}
		}
	}

release_lock:
	/* And release the spinlock anyway */
	spin_unlock(&dev->lock);

	return error;
}

static void sb_request(struct request_queue *q) {
	struct request *req;
	int error;

	req = blk_fetch_request(q);
	while (req != NULL) {
		/* Check request type */
		if (req == NULL || (req->cmd_type != REQ_TYPE_FS)) {
			__blk_end_request_all(req, -EIO);
			continue;
		}
		/* Do transfer */
		error = sb_transfer(sbd, blk_rq_pos(req), blk_rq_cur_sectors(req), req->buffer, rq_data_dir(req));
		if (!__blk_end_request_cur(req, error ? -EIO : 0) ) {
			req = blk_fetch_request(q);
		}
	}

	return;
}

/* Initialize the device */
static int __init sb_init(void) {
	int ret;
	int major = MAJOR_NODE;

	printk(KERN_NOTICE "sb: simple block device driver\n");
	if (request_region(REG_BASE, REG_LEN, DEV_NAME) == NULL) {
		printk(KERN_WARNING "sb: unable to register IOPorts %03x:%d\n", REG_BASE, REG_LEN);
		return -EBUSY;
	}
	ret = register_blkdev(MAJOR_NODE, DEV_NAME);

	if ((ret <= 0) && (!MAJOR_NODE)) {
		printk(KERN_WARNING "sb: unable to register dynamic major number\n");
		return -EBUSY;
	}

	if ((ret > 0) && (!MAJOR_NODE)) {
		printk(KERN_NOTICE "sb: received dynamic major %d\n", ret);
		major = ret;
	}

	if ((ret != 0) && (MAJOR_NODE)) {
		printk(KERN_WARNING "sb: unable to register static major number %d\n", MAJOR_NODE);
		return -EBUSY;
	}
	/* Alloc space for struct */
	sbd = kmalloc(sizeof(struct sb_dev), GFP_KERNEL);
	if (sbd == NULL) {
		goto emergency_clean;
	}

	/* Create spinlock */
	spin_lock_init(&sbd->lock);
	/* Init queue */
	sbd->queue = blk_init_queue(sb_request, &sbd->lock);
	if (sbd->queue == NULL) {
		goto emergency_clean;
	}

	/* Init gendisk stuff */
	sbd->gd = alloc_disk(MINORS);
	if (!sbd->gd) {
		goto emergency_clean;
	}
	sbd->gd->major = major;

	/* Set some extra settings */
	sbd->gd->first_minor = 0;
	sbd->gd->queue = sbd->queue;
	sbd->gd->private_data = sbd;

	/* Device ops */
	sbd->gd->fops = &sb_fops;

	strcpy(sbd->gd->disk_name, "sb");
	/* Size: number of sectors * size of each sect */
	set_capacity(sbd->gd, sb_num_sectors ());

	/* Tell the kernel about the disk */
	add_disk(sbd->gd);

	return 0;

emergency_clean:
	printk(KERN_WARNING "sb: unable to allocate memory\n");
	release_region(REG_BASE, REG_LEN);
	unregister_blkdev(MAJOR_NODE, DEV_NAME);
	return -ENOMEM;
}

static void __exit sb_exit(void) {
	/* Free kernel memory */
	put_disk(sbd->gd);
	del_gendisk(sbd->gd);
	kfree(sbd);
	unregister_blkdev(MAJOR_NODE, DEV_NAME);
	release_region(REG_BASE, REG_LEN);
}

/* Entrypoints for the kernel */
module_init(sb_init);
module_exit(sb_exit);
