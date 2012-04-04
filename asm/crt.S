	.section	.text
	.align		4
	.set		noreorder

	.include	"cpudefs.S"

	.global	__start
	.ent	__start
	.type	__start, @function
__start:
	mtc0	$0, cop0_reg_status

	lui	$t0, %hi(.text)
	ori	$t2, $t0, %lo(.text)
	lui	$t1, 0x0010
	addu	$sp, $t0, $t1

	lui	$gp, %hi(_gp)
	jal	_main
	ori	$gp, %lo(_gp)

	syscall
	b	cpu_die
	nop
	.end	__start
