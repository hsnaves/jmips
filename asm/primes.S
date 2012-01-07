    .section .text
    .align 4
    .global _main
_main:
    lui     $a0, %hi(message)
    ori     $a0, $a0, %lo(message)
    jal     send_str
    nop
    jr      $ra
    nop

    .section .rodata
message:
    .ascii "Hello, world!\n\0"
    .ascii "\0\0\0\0"
