
#include "console.h"
#include "libc.h"
#include "io.h"

void kinit (void)
{
  kconsole_init ();
  kprintf ("Hello, world!\n");
  kprintf ("%d\n", 1 + 4);
}
