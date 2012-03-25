

void send_str(char *s)
{
  volatile char *send = (char *) 0xBFD003F8;
  volatile char *lsr = (char *)  0xBFD003FD;
  while (*s) {
    char c = s[0];
    while (((*lsr) & 0x20) == 0);
    *send = c;
    s++;
  }
}
