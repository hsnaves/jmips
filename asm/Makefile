CROSS=mips-unknown-linux-uclibc-

AS      = $(CROSS)as
CC      = $(CROSS)gcc
LD      = $(CROSS)ld
OBJCOPY = $(CROSS)objcopy

ASFLAGS = -g
CFLAGS  = -Wall -mno-abicalls -g -O3
LDFLAGS = -Tlinker.ld

COMMON = crt.o printf.o io.o r4kc.o
TARGETS += test.elf

all: build

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

%.o: %.S
	$(AS) $(ASFLAGS) $< -o $@

%.elf: $(COMMON) %.o
	$(LD) $(LDFLAGS) $^ -o $@

build: $(COMMON) $(TARGETS)

.PHONY: clean

clean:
	$(RM) -fr $(TARGETS) $(COMMON)
