
#include <stddef.h>
#include <stdarg.h>

#include "libc.h"


static putchar_callback _putchar = NULL;
static char *std_out = NULL;

static
void print_char (char **out, size_t *size, char c)
{
  if (out == &std_out) {
    if (_putchar != NULL)
      (*_putchar) (c);
  } else if (out && (*size) > 1) {
    (*out)[0] = c;
    ++(*out);
    (*size)--;
  }
}

static
int print_string (char **out, size_t *size, const char *str,
                  int width, int align_right, char padding_char)
{
  int count = 0;

  if (width > 0) {
    if (align_right) {
      const char *ptr;
      int len = 0, padding = 0;

      for (ptr = str; *ptr; ptr++) len++;
      if (len >= width) padding = 0;
      else padding = width - len;

      for (; padding > 0; padding--) {
        print_char (out, size, padding_char);
        count++; width--;
      }
    }
  }
  for (; (*str) != 0; str++) {
    print_char (out, size, *str);
    count++; width--;
  }
  while (width > 0) {
    print_char (out, size, padding_char);
    count++; width--;
  }

  return count;
}

static
int print_int (char **out, size_t *size, int num, int radix, int sign,
               int width, int align_right, char letter_base,
               char padding_char, int plus)
{
  char buf[64], *s;
  int neg = 0, count = 0;
  unsigned int unum = num;

  if (num == 0) {
    buf[0] = '0';
    buf[1] = '\0';
    return print_string (out, size, buf, width, align_right, padding_char);
  }

  if (sign && radix == 10 && num < 0) {
    neg = 1;
    unum = -num;
  }

  s = &buf[sizeof(buf) - 1];
  *s = '\0';

  while (unum > 0) {
    char digit  = unum % radix;
    if (digit >= 10)
       digit += letter_base - 10;
    else
       digit += '0';
    *(--s) = digit;
    unum /= radix;
  }

  if (padding_char == '0') {
    if (neg) {
      print_char (out, size, '-');
      count++; width--;
    } else if (plus) {
      print_char (out, size, '+');
      count++; width--;
    }
  } else {
    if (neg) {
      *(--s) = '-';
    } else if (plus) {
      *(--s) = '+';
    }
  }
  return count + print_string (out, size, s, width, align_right, padding_char);
}

static
int print (char **out, size_t size, const char *format, va_list args)
{
  int align_right, width, count, plus;
  char scr[2], padding_char;

  count = 0;
  for (; *format != 0; ++format) {
    if (*format == '%') {
      format++;
      width = 0;
      align_right = 1;
      padding_char = ' ';
      plus = 0;

      if (*format == '\0') break;
      if (*format == '%') goto emit_char;
      while (1) {
        if (*format == '-') {
          align_right = 0;
        } else if (*format == '+') {
          plus = 1;
        } else if (*format == '0') {
          padding_char = '0';
        } else if (*format == ' ') {
          padding_char = ' ';
        } else break;
        format++;
      }

      for (; *format >= '0' && *format <= '9'; format++) {
        width *= 10;
        width += (*format) - '0';
      }

      if (*format == 's') {
        char *str = va_arg (args, char *);
        count += print_string (out, &size, str ? str : "(null)", width,
                               align_right, ' ');
        continue;
      }
      if (*format == 'd' || *format == 'i') {
        count += print_int (out, &size, va_arg (args, int), 10, 1, width,
                            align_right, 'a', padding_char, plus);
        continue;
      }
      if (*format == 'x') {
        count += print_int (out, &size, va_arg (args, int), 16, 0, width,
                            align_right, 'a', padding_char, 0);
        continue;
      }
      if (*format == 'X') {
        count += print_int (out, &size, va_arg (args, int), 16, 0, width,
                            align_right, 'A', padding_char, 0);
        continue;
      }
      if (*format == 'u') {
        count += print_int (out, &size, va_arg (args, int), 10, 0, width,
                            align_right, 'a', padding_char, 0);
        continue;
      }
      if (*format == 'o') {
        count += print_int (out, &size, va_arg (args, int), 8, 0, width,
                            align_right, 'a', padding_char, 0);
        continue;
      }
      if (*format == 'n') {
        int *ptr = va_arg (args, int *);
        *ptr = count;
        continue;
      }
      if (*format == 'c') {
        /* chars are converted to int then pushed on the stack */
        scr[0] = (char) va_arg (args, int);
        scr[1] = '\0';
        count += print_string (out, &size, scr, width, align_right, ' ');
        continue;
      }
    }
  emit_char:
    print_char (out, &size, *format);
    count++;
  }
  if (size > 0 && *out != NULL) {
    (*out)[0] = '\0';
  }
  return count;
}

int kvsnprintf (char *str, size_t size, const char *format, va_list ap)
{
  return print (&str, size, format, ap);
}

int kvsprintf (char *str, const char *format, va_list ap)
{
  return kvsnprintf (str, -1, format, ap);
}

int ksprintf (char *str, const char *format, ...)
{
  va_list args;
  int ret;

  va_start (args, format);
  ret = kvsprintf (str, format, args);
  va_end (args);
  return ret;
}

int kvprintf (const char *format, va_list ap)
{
  return print (&std_out, 0, format, ap);
}

int kprintf (const char *format, ...)
{
  va_list args;
  int ret;

  va_start (args, format);
  ret = kvprintf (format, args);
  va_end (args);
  return ret;
}

putchar_callback kregister_putchar_callback (putchar_callback callback)
{
  putchar_callback old = _putchar;
  _putchar = callback;
  return old;
}
