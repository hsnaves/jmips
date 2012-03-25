	.section .text
	.align 4
	.set noreorder

	.global	__start
__start:
	lui	$sp, %hi(0x80200000)
	ori	$sp, $sp, %lo(0x80200000)
	b	_main
	nop

1:
	b	1b
	nop
