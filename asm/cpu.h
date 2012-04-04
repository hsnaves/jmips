#ifndef __CPU_H
#define __CPU_H

#define STATUS_IE        0x00000001 /* Interrupt enabled */
#define STATUS_EXL       0x00000002 /* Exception level */
#define STATUS_ERL       0x00000004 /* Error level */
#define STATUS_UM        0x00000010 /* User mode */
#define STATUS_INT_MASK  0x0000FF00
#define STATUS_INT_SHIFT 8
#define STATUS_NMI       0x00080000 /* NMI */
#define STATUS_SR        0x00100000 /* Software reset */
#define STATUS_TS        0x00200000 /* TLB Shutdown */
#define STATUS_BEV       0x00400000 /* Bootstrap exception vector */
#define STATUS_RE        0x02000000 /* Reverse-endian */
#define STATUS_RP        0x08000000 /* Reduced power mode */
#define STATUS_COP_MASK  0xF0000000
#define STATUS_COP_SHIFT 28

int get_c0_index ();
int get_c0_random ();
int get_c0_entrylo0 ();
int get_c0_entrylo1 ();
int get_c0_context ();
int get_c0_pagemask ();
int get_c0_wired ();
int get_c0_badvaddr ();
int get_c0_count ();
int get_c0_entryhi ();
int get_c0_compare ();
int get_c0_status ();
int get_c0_cause ();
int get_c0_epc ();
int get_c0_prid ();
int get_c0_config ();
int get_c0_lladdr ();
int get_c0_errorepc ();

void set_c0_index (int val);
void set_c0_random (int val);
void set_c0_entrylo0 (int val);
void set_c0_entrylo1 (int val);
void set_c0_context (int val);
void set_c0_pagemask (int val);
void set_c0_wired (int val);
void set_c0_badvaddr (int val);
void set_c0_count (int val);
void set_c0_entryhi (int val);
void set_c0_compare (int val);
void set_c0_status (int val);
void set_c0_cause (int val);
void set_c0_epc (int val);
void set_c0_config (int val);
void set_c0_lladdr (int val);
void set_c0_errorepc (int val);

int cop0_enable_interrupt (int enable);
void cop0_install_handlers (void);

extern void (*callback_exception)(int ex, int *regs);
extern void (*callback_die)(int ex);

#endif /* __CPU_H */
