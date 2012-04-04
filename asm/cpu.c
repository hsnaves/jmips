
#include "cpu.h"

int cop0_enable_interrupt (int enable)
{
  int status = get_c0_status ();
  set_c0_status ((status & 0xFFFFFFFE) | ((enable) ? 1 : 0));
  return status & 1;
}
