
int putchar (int c)
{
  volatile char *send = (char *) 0xBFD003F8;
  volatile char *lsr = (char *)  0xBFD003FD;
  while (((*lsr) & 0x20) == 0);
  *send = (char) c;
  return c & 0xFF;
}

int puts (const char *s)
{
  while ((*s) != 0) {
    putchar (*s);
    s++;
  }
  putchar ('\n');
  return 1;
}

