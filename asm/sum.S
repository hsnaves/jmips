    .section .text
    .align 4
    .global _main
_main:
    lui     $a1, %hi(10000000)
    ori     $a1, $a1, %lo(10000000)
    move    $a0, $0
_main_loop:
    addu    $a0, $a0, $a1
    addiu   $a1, $a1, -1
    bne     $a1, $0, _main_loop
    nop

    lui     $a0, %hi(message)
    ori     $a0, $a0, %lo(message)
    jal     send_str
    nop
    jr      $ra
    nop

    .section .rodata
message:
    .ascii "Computed!\n\0"
    .ascii "\0\0\0\0"

