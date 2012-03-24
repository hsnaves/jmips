	.section .text
	.align 4
	.set noreorder

	.global	_start
_start:
	lui	$sp, %hi(0x09000000)
	ori	$sp, $sp, %lo(0x09000000)
	jal	_main
	nop

1:
	j	1b
	nop
