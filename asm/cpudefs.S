	.set	cop0_reg_index,		$0
	.set	cop0_reg_random,	$1
	.set	cop0_reg_entrylo0,	$2
	.set	cop0_reg_entrylo1,	$3
	.set	cop0_reg_context,	$4
	.set	cop0_reg_pagemask,	$5
	.set	cop0_reg_wired,		$6
	.set	cop0_reg_badvaddr,	$8
	.set	cop0_reg_count,		$9
	.set	cop0_reg_entryhi,	$10
	.set	cop0_reg_compare,	$11
	.set	cop0_reg_status,	$12
	.set	cop0_reg_cause,		$13
	.set	cop0_reg_epc,		$14
	.set	cop0_reg_prid,		$15
	.set	cop0_reg_config,	$16
	.set	cop0_reg_lladdr,	$17
	.set	cop0_reg_errorepc,	$30

	.macro	ehb
	sll	$0, $0, 3
	.endm

	.macro	ssnop
	sll	$0, $0, 1
	.endm

	.macro	cpu_wait
	.set	push
	.set	mips3
	wait
	.set	pop
	.endm
