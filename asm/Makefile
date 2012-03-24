CROSS=mips-unknown-linux-uclibc-

AS      = $(CROSS)as
CC      = $(CROSS)gcc
LD      = $(CROSS)ld
OBJCOPY = $(CROSS)objcopy

ASFLAGS = -mips32 -EB -march=4kc -mabi=32
CFLAGS  = -mips32 -EB -march=4kc -mabi=32 -mno-abicalls -O2
LDFLAGS = -s -Tlinker.ld

CRT = crt.o
SERIAL = serial.o
BINS += sha1.bin
BINS += primes.bin
BINS += sum.bin

all: build

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

%.o: %.S
	$(AS) $(ASFLAGS) $< -o $@

%.elf: $(CRT) $(SERIAL) %.o
	$(LD) $(LDFLAGS) $^ -o $@

%.bin: %.elf
	$(OBJCOPY) -O binary $^ $@

build: $(CRT) $(BINS)

.PHONY: clean

clean:
	$(RM) -fr $(BINS) $(CRT)
