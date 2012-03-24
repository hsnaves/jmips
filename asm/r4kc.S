
	.section .text
	.align 4
	.set noreorder

#define COP0_REG_INDEX		$0
#define COP0_REG_RANDOM		$1
#define COP0_REG_ENTRYLO0	$2
#define COP0_REG_ENTRYLO1	$3
#define COP0_REG_CONTEXT	$4
#define COP0_REG_PAGEMASK	$5
#define COP0_REG_WIRED		$6
#define COP0_REG_BADVADDR	$8
#define COP0_REG_COUNT		$9
#define COP0_REG_ENTRYHI	$10
#define COP0_REG_COMPARE	$11
#define COP0_REG_STATUS		$12
#define COP0_REG_CAUSE		$13
#define COP0_REG_EPC		$14
#define COP0_REG_PRID		$15
#define COP0_REG_CONFIG		$16
#define COP0_REG_LLADDR		$17
#define COP0_REG_ERROREPC	$30


	.global	get_c0_index
get_c0_index:
	jr	$ra
	mfc0	$v0, COP0_REG_INDEX

	.global	set_c0_index
set_c0_index:
	jr	$ra
	mtc0	$a0, COP0_REG_INDEX


	.global	get_c0_random
get_c0_random:
	jr	$ra
	mfc0	$v0, COP0_REG_RANDOM

	.global	set_c0_random
set_c0_random:
	jr	$ra
	mtc0	$a0, COP0_REG_RANDOM

	.global	get_c0_entrylo0
get_c0_entrylo0:
	jr	$ra
	mfc0	$v0, COP0_REG_ENTRYLO0

	.global	set_c0_entrylo0
set_c0_entrylo0:
	jr	$ra
	mtc0	$a0, COP0_REG_ENTRYLO0

	.global	get_c0_entrylo1
get_c0_entrylo1:
	jr	$ra
	mfc0	$v0, COP0_REG_ENTRYLO1

	.global	set_c0_entrylo1
set_c0_entrylo1:
	jr	$ra
	mtc0	$a0, COP0_REG_ENTRYLO1

	.global	get_c0_context
get_c0_context:
	jr	$ra
	mfc0	$v0, COP0_REG_CONTEXT

	.global	set_c0_context
set_c0_context:
	jr	$ra
	mtc0	$a0, COP0_REG_CONTEXT

	.global	get_c0_pagemask
get_c0_pagemask:
	jr	$ra
	mfc0	$v0, COP0_REG_PAGEMASK

	.global	set_c0_pagemask
set_c0_pagemask:
	jr	$ra
	mtc0	$a0, COP0_REG_PAGEMASK

	.global	get_c0_wired
get_c0_wired:
	jr	$ra
	mfc0	$v0, COP0_REG_WIRED

	.global	set_c0_wired
set_c0_wired:
	jr	$ra
	mtc0	$a0, COP0_REG_WIRED

	.global	get_c0_status
get_c0_status:
	jr	$ra
	mfc0	$v0, COP0_REG_STATUS

	.global	set_c0_status
set_c0_status:
	jr	$ra
	mtc0	$a0, COP0_REG_STATUS

