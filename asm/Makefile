CROSS=mips-unknown-linux-uclibc-

AS      = $(CROSS)as
CC      = $(CROSS)gcc
LD      = $(CROSS)ld
OBJCOPY = $(CROSS)objcopy

ASFLAGS = -g
CFLAGS  = -Wall -mno-abicalls -g
LDFLAGS = -Ttext=0x80100000

CRT = crt.o
SERIAL = serial.o
BINS += sha1.bin

all: build

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@
	$(OBJCOPY) -R .reginfo $@ $@

%.o: %.S
	$(AS) $(ASFLAGS) $< -o $@
	$(OBJCOPY) -R .reginfo $@ $@

%.elf: $(CRT) $(SERIAL) %.o
	$(LD) $(LDFLAGS) $^ -o $@

%.bin: %.elf
	cp $^ $@
	#$(OBJCOPY) -R .reginfo $^ $@

build: $(CRT) $(BINS)

.PHONY: clean

clean:
	$(RM) -fr $(BINS) $(CRT)
