CROSS=mips-unknown-linux-uclibc-

AS      = $(CROSS)gcc
CC      = $(CROSS)gcc
LD      = $(CROSS)ld
OBJCOPY = $(CROSS)objcopy

ASFLAGS = -c -nostdlib -mno-shared -mno-abicalls -fno-pic -g
CFLAGS  = -Wall -nostdlib -mno-shared -mno-abicalls -fno-pic -g -O3
LDFLAGS = -Tlinker.ld

OBJS = boot.o cpu.o init.o exception.o io.o console.o libc.o
TARGET = test-os.elf

all: build

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

%.o: %.S
	$(AS) $(ASFLAGS) $< -o $@

%.elf: $(OBJS)
	$(LD) $(LDFLAGS) $^ -o $@

build: $(TARGET)

.PHONY: clean

clean:
	$(RM) -fr $(TARGET) $(OBJS)
