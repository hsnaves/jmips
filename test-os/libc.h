#ifndef __LIBC_H
#define __LIBC_H

#include <stddef.h>
#include <stdarg.h>

typedef void (*putchar_callback) (char c);

int kvsnprintf (char *str, size_t size, const char *format, va_list ap);
int kvsprintf (char *str, const char *format, va_list ap);
int ksprintf (char *str, const char *format, ...);
int kvprintf (const char *format, va_list ap);
int kprintf (const char *format, ...);

putchar_callback kregister_putchar_callback (putchar_callback callback);

#endif /* __LIBC_H */
