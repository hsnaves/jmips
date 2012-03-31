#ifndef __R4KC_H
#define __R4KC_H

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
void set_c0_prid (int val);
void set_c0_config (int val);
void set_c0_lladdr (int val);
void set_c0_errorepc (int val);


#endif /* __R4KC_H */
