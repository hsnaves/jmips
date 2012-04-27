
#ifndef __IO_H
#define __IO_H

void  kio_set_base (char *io_base);
char  kio_inb  (int port);
void  kio_outb (int port, char val);
short kio_ins  (int port);
void  kio_outs (int port, short s);
int   kio_inl  (int port);
void  kio_outl (int port, int i);

#endif /* __IO_H */

