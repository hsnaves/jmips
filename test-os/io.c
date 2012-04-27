
#include "io.h"

static volatile char *ioport_base = (char *) 0xBFD00000;

void kio_set_base (char *io_base)
{
  ioport_base = io_base;
}

char kio_inb (int port)
{
  return ioport_base[port];
}

void kio_outb (int port, char val)
{
  ioport_base[port] = val;
}

short kio_ins (int port)
{
  volatile short *p = (short *) ioport_base;
  return p[port >> 1];
}

void kio_outs (int port, short s)
{
  volatile short *p = (short *) ioport_base;
  p[port >> 1] = s;
}

int kio_inl (int port)
{
  volatile int *p = (int *) ioport_base;
  return p[port >> 2];
}

void kio_outl (int port, int i)
{
  volatile int *p = (int *) ioport_base;
  p[port >> 2] = i;
}
