
#include "console.h"
#include "libc.h"
#include "io.h"

#define UART_BASE   0x03F8
#define UART_SEND   UART_BASE
#define UART_LSR    (UART_BASE + 5)

void kconsole_init (void)
{
  kregister_putchar_callback (&kconsole_putc);
}

void kconsole_putc (char c)
{
  while (1) {
    if (kio_inb (UART_LSR) & 0x20) break;
  }
  kio_outb (UART_SEND, c);
}

void kconsole_puts (const char *str)
{
  while ((*str) != 0) {
    kconsole_putc (*str);
    str++;
  }
  kconsole_putc ('\n');
}
