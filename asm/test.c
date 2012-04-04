
#include <stdio.h>
#include "cpu.h"

void _die_callback (int ex)
{
  printf ("Die %d!!!\n", ex);
}

const char *decode_exception_code (int code)
{
  switch (code) {
  case 0: return "Interrupt";
  case 1: return "TLB Modification";
  case 2: return "TLBL Exception";
  case 3: return "TLBS Exception";
  case 4: return "ADEL Exception";
  case 5: return "ADES Exception";
  case 6: return "IBE Exception";
  case 7: return "DBE Exception";
  case 8: return "Syscall";
  case 9: return "Breakpoint";
  case 10: return "Reserved Instruction";
  case 11: return "Coprocessor unusable";
  case 12: return "Overflow exception";
  case 13: return "Trap Exception";
  case 23: return "Watch Exception";
  case 24: return "MCheck exception";
  }
  return "(unknown)";
}

void _exception_callback (int ex, int *regs)
{
  int epc = get_c0_epc ();
  int cause = get_c0_cause ();
  int status = get_c0_status ();
  int code = (cause >> 2) & 0x1F;
  int delay_slot = (cause & 0x80000000) ? 1 : 0;
  int coprocessor = (cause >> 28) & 0x03;
  int intr = (cause >> 8) & 0xFF;
  int intrmask = (status >> 8) & 0xFF;
  printf ("Exception type: %d code: %s!!!\n", ex, decode_exception_code (code));
  printf ("Delay slot: %d, coprocessor: %d, intr: 0x%02X\n", delay_slot, coprocessor, intr & intrmask);
  set_c0_epc (epc + 4);
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
