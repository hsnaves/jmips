.text
.global _start

_start:
    lui    $sp, %hi(0x09000000)
    ori    $sp, $sp, %lo(0x09000000)
    jal    _main
    nop

_loop:
    j      _loop
    nop
