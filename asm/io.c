
void putchar (char c)
{
  volatile char *send = (char *) 0xBFD003F8;
  volatile char *lsr = (char *)  0xBFD003FD;
  while (((*lsr) & 0x20) == 0);
  *send = c;
}

