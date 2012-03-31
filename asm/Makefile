CROSS=mips-unknown-linux-uclibc-

AS      = $(CROSS)as
CC      = $(CROSS)gcc
LD      = $(CROSS)ld
OBJCOPY = $(CROSS)objcopy

ASFLAGS = -g
CFLAGS  = -Wall -mno-abicalls -g -O3
LDFLAGS = -Ttext=0x80100000

COMMON = crt.o printf.o io.o r4kc.o
BINS += sha1.bin
BINS += test.bin

all: build

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@
	$(OBJCOPY) -R .reginfo $@ $@

%.o: %.S
	$(AS) $(ASFLAGS) $< -o $@
	$(OBJCOPY) -R .reginfo $@ $@

%.elf: $(COMMON) %.o
	$(LD) $(LDFLAGS) $^ -o $@

%.bin: %.elf
	cp $^ $@
	#$(OBJCOPY) -R .reginfo $^ $@

build: $(CRT) $(BINS)

.PHONY: clean

clean:
	$(RM) -fr $(BINS) $(CRT)
