#include <stdio.h>

int _main(void)
{
  char buf[8];
  sprintf (buf, "%d", 134);
  printf ("Hello, world %40s\n", buf);
  return 0;
}
