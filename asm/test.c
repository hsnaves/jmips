
#include <stdio.h>
#include "cpu.h"

void _die_callback (int ex)
{
  printf ("Die %d!!!\n", ex);
}

void _exception_callback (int ex, int *regs)
{
  printf ("Exception %d!!!\n", ex);
}

int _main(void)
{
  printf ("Mips test\n");
  printf ("PRId = 0x%08X\n", get_c0_prid ());
  printf ("Config = 0x%08X\n", get_c0_config ());

  callback_die = &_die_callback;
  callback_exception = &_exception_callback;

  set_c0_status (STATUS_INT_MASK);
  cop0_install_handlers ();
  cop0_enable_interrupt (1);
  return 0;
}
