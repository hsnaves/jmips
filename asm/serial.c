

void send_str(char *s)
{
  volatile char *send = (char *) 0x30000000;
  volatile char *lsr = (char *)  0x30000005;
  while (*s) {
    char c = s[0];
    while (((*lsr) & 0x20) == 0);
    *send = c;
    s++;
  }
}
