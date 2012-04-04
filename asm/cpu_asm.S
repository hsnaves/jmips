
	.section	.text
	.align		4
	.set		noreorder
	.set		noat

	.include	"cpudefs.S"

	.macro	get_c0_reg register
	.global	get_c0_\register
	.ent	get_c0_\register
	.type	get_c0_\register, @function
get_c0_\register:
	jr		$ra
	mfc0	$v0, cop0_reg_\register
	.end	get_c0_\register
	.endm

	.macro	set_c0_reg register
	.global	set_c0_\register
	.ent	set_c0_\register
	.type	set_c0_\register, @function
set_c0_\register:
	jr		$ra
	mtc0	$a0, cop0_reg_\register
	.end	set_c0_\register
	.endm

	.macro	get_set_c0_reg register
	get_c0_reg \register
	set_c0_reg \register
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
	get_c0_reg	prid
	get_set_c0_reg	config
	get_set_c0_reg	lladdr
	get_set_c0_reg	errorepc

	.global	cpu_die
	.ent	cpu_die
	.type	cpu_die, @function
cpu_die:
	cpu_wait
	b	cpu_die
	nop
	.end	cpu_die

__call_die:
	la	$t0, callback_die
	lw	$t1, 0($t0)
	jalr	$t1
	move	$a0, $1
	b	cpu_die
	nop

__save_context:
	sw	$2, 8($k0)
	sw	$3, 12($k0)
	sw	$4, 16($k0)
	sw	$5, 20($k0)
	sw	$6, 24($k0)
	sw	$7, 28($k0)
	sw	$8, 32($k0)
	sw	$9, 36($k0)
	sw	$10, 40($k0)
	sw	$11, 44($k0)
	sw	$12, 48($k0)
	sw	$13, 52($k0)
	sw	$14, 56($k0)
	sw	$15, 60($k0)
	sw	$16, 64($k0)
	sw	$17, 68($k0)
	sw	$18, 72($k0)
	sw	$19, 76($k0)
	sw	$20, 80($k0)
	sw	$21, 84($k0)
	sw	$22, 88($k0)
	sw	$23, 92($k0)
	sw	$24, 96($k0)
	sw	$25, 100($k0)

	sw	$27, 108($k0)
	sw	$28, 112($k0)
	sw	$29, 116($k0)
	sw	$30, 120($k0)

	lw	$t0, 0($k0)
	beqz	$t0, __call_die
	sw	$0, 0($k0)

	jr	$ra
	nop


__restore_context:
	li	$t0, 1
	sw	$t0, 0($k0)

	lw	$1, 4($k0)
	lw	$2, 8($k0)
	lw	$3, 12($k0)
	lw	$4, 16($k0)
	lw	$5, 20($k0)
	lw	$6, 24($k0)
	lw	$7, 28($k0)
	lw	$8, 32($k0)
	lw	$9, 36($k0)
	lw	$10, 40($k0)
	lw	$11, 44($k0)
	lw	$12, 48($k0)
	lw	$13, 52($k0)
	lw	$14, 56($k0)
	lw	$15, 60($k0)
	lw	$16, 64($k0)
	lw	$17, 68($k0)
	lw	$18, 72($k0)
	lw	$19, 76($k0)
	lw	$20, 80($k0)
	lw	$21, 84($k0)
	lw	$22, 88($k0)
	lw	$23, 92($k0)
	lw	$24, 96($k0)
	lw	$25, 100($k0)

	lw	$27, 108($k0)
	lw	$28, 112($k0)
	lw	$29, 116($k0)
	lw	$30, 120($k0)
	lw	$31, 124($k0)

	.set	push
	.set	mips3
	eret
	nop
	.set	pop

	.macro	handle_exception etype val
	.set	push
handle_cop0_\etype:
	la	$k0, saved_regs_\etype
	sw	$1, 4($k0)
	sw	$ra, 124($k0)
	jal	__save_context
	ori	$1, $0, \val

	la	$sp, protected_stack_end
	la	$t0, callback_exception
	lw	$t1, 0($t0)
	la	$a1, saved_regs_\etype
	jalr	$t1
	ori	$a0, $0, \val

	b	__restore_context
	nop

	.section	.data
	.align		4

saved_regs_\etype:
	.word	1
	.skip	124, 0

	.set	pop
	.endm

	handle_exception exception     1
	handle_exception exception200  2
	handle_exception tlbrefill     3

	.section	.text

__install_handler:
	srl	$a0, $a0, 2
	li	$t0, 0x3FFFFFF
	li	$t1, 0x08000000
	and	$a0, $a0, $t0
	or	$a0, $a0, $t1

	sw	$a0, 0($a1)
	sw	$0, 4($a1)
	jr	$ra
	nop
	# Flush cache here!

	.global	cop0_install_handlers
	.ent	cop0_install_handlers
	.type	cop0_install_handlers, @function
cop0_install_handlers:
	move	$t5, $ra
	la	$a0, handle_cop0_exception
	li	$a1, 0x80000180
	jal	__install_handler
	nop

	la	$a0, handle_cop0_exception200
	li	$a1, 0x80000200
	jal	__install_handler
	nop

	la	$a0, handle_cop0_tlbrefill
	li	$a1, 0x80000000
	jal	__install_handler
	nop

	move	$ra, $t5
	jr	$ra
	nop

	.end	cop0_install_handlers

	.section	.data
	.align		4

	.global	callback_exception
	.type	callback_exception, @object
	.size	callback_exception, 4
callback_exception:
	.word	0

	.global	callback_die
	.type	callback_die, @object
	.size	callback_die, 4
callback_die:
	.word	0

	.section	.bss
	.align		4

protected_stack:
	.skip	8192,0
protected_stack_end:

