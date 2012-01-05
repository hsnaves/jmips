CROSS=mips-linux-gnu-

AS      = $(CROSS)as
LD      = $(CROSS)ld
OBJCOPY = $(CROSS)objcopy

ASFLAGS = -mips32 -EB -march=4kc -mabi=32
LDFLAGS = -s -Tlinker.ld

CRT        = crt.o
BINS += primes.bin

all: build

%.o: %.S
	$(AS) $(ASFLAGS) $< -o $@

%.elf: %.o $(CRT)
	$(LD) $(LDFLAGS) $^ -o $@

%.bin: %.elf
	$(OBJCOPY) -O binary $^ $@

build: $(CRT) $(BINS)

.PHONY: clean

clean:
	$(RM) -fr $(BINS) $(CRT)
