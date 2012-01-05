.text
.global _start

_start:
    lui    $sp, %hi(_start + 0x04000000 - 4)
    ori    $sp, $sp, %lo(_start + 0x04000000 - 4)
    j      _main
    nop

