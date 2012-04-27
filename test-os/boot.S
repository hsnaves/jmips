# -------------------------------------------------------------------------
#  boot.S
# -------------------------------------------------------------------------

#include "exception.h"
#define Status	$12
#define EPC	$14

# -------------------------------------------------------------------------
# Data segment
# -------------------------------------------------------------------------

#define INIT_STACK_SIZE		0x4000
#define EXCEPTION_STACK_SIZE	0x4000

	.data

	# Align everything on a 2^4 = 16 byte word bondary
	.align	4

exception_regs:
	.word	0

	.space	INIT_STACK_SIZE
init_stack:

	.space	EXCEPTION_STACK_SIZE
exception_stack:

# -------------------------------------------------------------------------
# Code segment
# -------------------------------------------------------------------------

	.text
	.set	noreorder
	.set	noat

	.global	__start
	.global	kset_exception_registers
	.global	kget_exception_registers
	.global	kexception_handler
	.global	kinit
	.global	kcpu_wait


# -------------------------------------------------------------------------
# Set register area used by exception handler
# -------------------------------------------------------------------------
kset_exception_registers:

	lui	$at, %hi(exception_regs)
	ori	$at, $at, %lo(exception_regs)
	sw	$a0, 0($at)
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Get register area used by exception handler
# -------------------------------------------------------------------------
kget_exception_registers:

	lui	$at, %hi(exception_regs)
	ori	$at, $at, %lo(exception_regs)
	lw	$v0, 0($at)
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Startup code
#    This is where the system boots from
#     1. Copy 'exception_stub' to the several MIPS exception vectors
#     2. Load the stackpointer 'sp' with top of initial stack.
#     3. Load 'gp' with 0x80000000 (needed by the compiler).
#     4. Jump to application-specific initialization 'kinit'.
# -------------------------------------------------------------------------
__start:
	mtc0	$0, Status	# Clear the Status register

	lui	$s0, %hi(exception_stub)
	ori	$s0, $s0, %lo(exception_stub)
	lui	$t0, %hi(exception_stub_end)
	ori	$t0, $t0, %lo(exception_stub_end)

	subu	$s1, $t0, $s0	# Compute the length of handler

	lui	$a0, 0xA000	# First handler
	move	$a1, $s0
	move	$a2, $s1
	jal	_memcpy
	nop

	li	$a0, 0xA0000180	# Second handler
	move	$a1, $s0
	move	$a2, $s1
	jal	_memcpy
	nop

	li	$a0, 0xA0000200	# Last handler
	move	$a1, $s0
	move	$a2, $s1
	jal	_memcpy
	nop

	li	$gp, 0x80000000
	lui	$sp, %hi(init_stack - 32)
	ori	$sp, $sp, %lo(init_stack - 32)
	move	$fp, $sp
	jal	kinit
	nop
	j	kcpu_wait
	nop

# -------------------------------------------------------------------------
# Memcpy function to install the exception handlers
#   a0 - destination
#   a1 - source
#   a2 - length (multiple of 4)
# -------------------------------------------------------------------------
_memcpy:
	lw	$t0, 0($a1)	#copy one word at a time
	sw	$t0, 0($a0)
	addu	$a0, 4
	addu	$a1, 4
	subu	$a2, 4
	bgtz	$a2, _memcpy
	nop
	jr	$ra
	nop


# -------------------------------------------------------------------------
# On exception: jump to exception handler.
# This stub is copied to the exception handler vectors
# it calls 'exception_handler' below when an exception occurs.
# -------------------------------------------------------------------------

exception_stub:
	move	$k0, $ra
	bal	1f				# to get current pc
	nop
1:
	addiu	$k1, $ra, -12			# k1 = address of handler
	move	$ra, $k0
	lui	$k0, %hi(exception_handler)	# jumps to handler
	ori	$k0, $k0, %lo(exception_handler)
	jr	$k0
	nop
exception_stub_end:


# -------------------------------------------------------------------------
# The exception handler
#   Calls 'kexception_handler' when registers are saved and
#   stack pointer is loaded.
# -------------------------------------------------------------------------
exception_handler:
	lui	$k0, %hi(exception_regs)
	ori	$k0, $k0, %lo(exception_regs)
	lw	$k0, 0($k0)

	sw	$k1, REG_EADDR($k0)

	sw	$at, REG_AT($k0)
	sw	$v0, REG_V0($k0)
	sw	$v1, REG_V1($k0)
	sw	$a0, REG_A0($k0)
	sw	$a1, REG_A1($k0)
	sw	$a2, REG_A2($k0)
	sw	$a3, REG_A3($k0)
	sw	$t0, REG_T0($k0)
	sw	$t1, REG_T1($k0)
	sw	$t2, REG_T2($k0)
	sw	$t3, REG_T3($k0)
	sw	$t4, REG_T4($k0)
	sw	$t5, REG_T5($k0)
	sw	$t6, REG_T6($k0)
	sw	$t7, REG_T7($k0)
	sw	$s0, REG_S0($k0)
	sw	$s1, REG_S1($k0)
	sw	$s2, REG_S2($k0)
	sw	$s3, REG_S3($k0)
	sw	$s4, REG_S4($k0)
	sw	$s5, REG_S5($k0)
	sw	$s6, REG_S6($k0)
	sw	$s7, REG_S7($k0)
	sw	$t8, REG_T8($k0)
	sw	$t9, REG_T9($k0)
	sw	$sp, REG_SP($k0)
	sw	$fp, REG_FP($k0)
	sw	$gp, REG_GP($k0)
	sw	$ra, REG_RA($k0)

	mfc0	$k1, EPC
	nop
	nop
	nop
	sw	$k1, REG_EPC($k0)

	li	$gp, 0x80000000
	lui	$sp, %hi(exception_stack - 32)
	ori	$sp, $sp, %lo(exception_stack - 32)
	move	$fp, $sp
	jal	kexception_handler
	nop

	lui	$k0, %hi(exception_regs)
	ori	$k0, $k0, %lo(exception_regs)
	lw	$k0, 0($k0)

	lw	$at, REG_AT($k0)
	lw	$v0, REG_V0($k0)
	lw	$v1, REG_V1($k0)
	lw	$a0, REG_A0($k0)
	lw	$a1, REG_A1($k0)
	lw	$a2, REG_A2($k0)
	lw	$a3, REG_A3($k0)
	lw	$t0, REG_T0($k0)
	lw	$t1, REG_T1($k0)
	lw	$t2, REG_T2($k0)
	lw	$t3, REG_T3($k0)
	lw	$t4, REG_T4($k0)
	lw	$t5, REG_T5($k0)
	lw	$t6, REG_T6($k0)
	lw	$t7, REG_T7($k0)
	lw	$s0, REG_S0($k0)
	lw	$s1, REG_S1($k0)
	lw	$s2, REG_S2($k0)
	lw	$s3, REG_S3($k0)
	lw	$s4, REG_S4($k0)
	lw	$s5, REG_S5($k0)
	lw	$s6, REG_S6($k0)
	lw	$s7, REG_S7($k0)
	lw	$t8, REG_T8($k0)
	lw	$t9, REG_T9($k0)
	lw	$sp, REG_SP($k0)
	lw	$fp, REG_FP($k0)
	lw	$gp, REG_GP($k0)
	lw	$ra, REG_RA($k0)
	lw	$k1, REG_EPC($k0)

	mtc0	$k1, EPC
	nop
	nop

	.set	mips3
	nop
	eret
	nop
