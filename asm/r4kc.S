
	.section .text
	.align 4
	.set noreorder

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

	.macro	get_set_c0_reg register
	.global	get_c0_\register
get_c0_\register:
	jr		$ra
	mfc0	$v0, cop0_reg_\register

	.global	set_c0_\register
set_c0_\register:
	jr		$ra
	mtc0	$a0, cop0_reg_\register
	.endm

	get_set_c0_reg	index
	get_set_c0_reg	random
	get_set_c0_reg	entrylo0
	get_set_c0_reg	entrylo1
	get_set_c0_reg	context
	get_set_c0_reg	pagemask
	get_set_c0_reg	wired
	get_set_c0_reg	badvaddr
	get_set_c0_reg	count
	get_set_c0_reg	entryhi
	get_set_c0_reg	compare
	get_set_c0_reg	status
	get_set_c0_reg	cause
	get_set_c0_reg	epc
	get_set_c0_reg	prid
	get_set_c0_reg	config
	get_set_c0_reg	lladdr
	get_set_c0_reg	errorepc

