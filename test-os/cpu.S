# -------------------------------------------------------------------------
#  cpu.S
# -------------------------------------------------------------------------

#define Index		$0
#define	Random		$1
#define	EntryLo0	$2
#define	EntryLo1	$3
#define	Context		$4
#define	PageMask	$5
#define	Wired		$6
#define	BadVAddr	$8
#define	Count		$9
#define	EntryHi		$10
#define	Compare		$11
#define Status		$12
#define Cause		$13
#define EPC		$14
#define PRID		$15
#define Config		$16
#define LLAddr		$17
#define	ErrorEPC	$30


# -------------------------------------------------------------------------
# Code segment
# -------------------------------------------------------------------------

	.text
	.set	noreorder
	.set	noat

	.global	kcop0_get_status
	.global	kcop0_set_status
	.global	kcop0_get_cause
	.global	kcop0_set_cause
	.global	kcop0_get_count
	.global	kcop0_set_timer
	.global	kcpu_wait


# -------------------------------------------------------------------------
# Set cpu status register
#  $a0 - and mask
#  $a1 - or mask
# -------------------------------------------------------------------------
kcop0_set_status:
	mfc0	$v0, Status	#loads cp0's status reg
	and	$v0, $v0, $a0
	or	$v0, $v0, $a1
	mtc0	$v0, Status
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Get cpu status register
# -------------------------------------------------------------------------
kcop0_get_status:
	mfc0	$v0, Status	#loads cp0's status reg
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Set cause register
#  $a0 - and mask
#  $a1 - or mask
# -------------------------------------------------------------------------
kcop0_set_cause:
	mfc0	$v0, Cause	#loads cp0's cause reg
	and	$v0, $v0, $a0
	or	$v0, $v0, $a1
	mtc0	$v0, Cause
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Get cause register
# -------------------------------------------------------------------------
kcop0_get_cause:
	mfc0	$v0, Cause	#loads cp0's cause reg
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Get cpu count register
# -------------------------------------------------------------------------
kcop0_get_count:
	mfc0	$v0, Count
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Set the timer
#  $a0 - value to add to count before setting the compare register
# -------------------------------------------------------------------------
kset_timer:
	mfc0	$t0, Count
	addu	$a0, $a0, $t0
	mtc0	$a0, Compare
	jr	$ra
	nop

# -------------------------------------------------------------------------
# Put the cpu to sleep
# -------------------------------------------------------------------------
kcpu_wait:
	.set	mips3
        wait
	j	kcpu_wait
	nop
