# Reference analysis report

# libunnamed_raw.so analysis

## Sections
-                          off=0x0 size=0x0 flags=0
- .note.android.ident      off=0x318 size=0x98 flags=2
- .note.gnu.build-id       off=0x3b0 size=0x24 flags=2
- .note.gnu.property       off=0x3d8 size=0x20 flags=2
- .dynsym                  off=0x3f8 size=0x1620 flags=2
- .gnu.version             off=0x1a18 size=0x1d8 flags=2
- .gnu.version_r           off=0x1bf0 size=0x60 flags=2
- .gnu.hash                off=0x1c50 size=0x20 flags=2
- .dynstr                  off=0x1c70 size=0xe27 flags=2
- .rela.dyn                off=0x2a98 size=0x108 flags=2
- .relr.dyn                off=0x2ba0 size=0x1f8 flags=2
- .rela.plt                off=0x2d98 size=0x15d8 flags=66
- .rodata                  off=0x4370 size=0xecab flags=2097202
- .gcc_except_table        off=0x1301c size=0x7df4 flags=2
- .eh_frame_hdr            off=0x1ae10 size=0x4efc flags=2
- .eh_frame                off=0x1fd10 size=0x19aa8 flags=2
- .text                    off=0x397c0 size=0x2c6894 flags=6
- __lcxx_override          off=0x300054 size=0x10c flags=6
- .plt                     off=0x300160 size=0x15f8 flags=536870918
- .tbss                    off=0x301758 size=0x81 flags=1027
- .data.rel.ro             off=0x301760 size=0x6800 flags=3
- .fini_array              off=0x307f60 size=0x10 flags=3
- .init_array              off=0x307f70 size=0x48 flags=3
- .dynamic                 off=0x307fb8 size=0x250 flags=3
- .got                     off=0x308208 size=0x408 flags=3
- .got.plt                 off=0x308610 size=0x760 flags=3
- .relro_padding           off=0x308d70 size=0x3290 flags=3
- .data                    off=0x308d70 size=0x868 flags=3
- .bss                     off=0x3095d8 size=0xaeb8 flags=3
- .comment                 off=0x3095d8 size=0x187 flags=48
- .shstrtab                off=0x30975f size=0x146 flags=0

## Known gate prologues (validation)
- gate 0x23f3d8: first 32 bytes = 3f2303d5ff0303d1fd7b06a9fc6f07a9fa6708a9f85f09a9f6570aa9f44f0ba9
  gate 0x23f3d8 (VA 0x23f3d8, file 0x23b3d8):
    0x23f3d8: paciasp  
    0x23f3dc: sub      sp, sp, #0xc0
    0x23f3e0: stp      x29, x30, [sp, #0x60]
    0x23f3e4: stp      x28, x27, [sp, #0x70]
    0x23f3e8: stp      x26, x25, [sp, #0x80]
    0x23f3ec: stp      x24, x23, [sp, #0x90]

- gate 0x2a5ecc: first 32 bytes = 3f2303d5ff4303d1fd7b07a9fc6f08a9fa6709a9f85f0aa9f6570ba9f44f0ca9
  gate 0x2a5ecc (VA 0x2a5ecc, file 0x2a1ecc):
    0x2a5ecc: paciasp  
    0x2a5ed0: sub      sp, sp, #0xd0
    0x2a5ed4: stp      x29, x30, [sp, #0x70]
    0x2a5ed8: stp      x28, x27, [sp, #0x80]
    0x2a5edc: stp      x26, x25, [sp, #0x90]
    0x2a5ee0: stp      x24, x23, [sp, #0xa0]

- gate 0x2bf304: first 32 bytes = 3f2303d5ff4304d1fd7b0ba9fc6f0ca9fa670da9f85f0ea9f6570fa9f44f10a9
  gate 0x2bf304 (VA 0x2bf304, file 0x2bb304):
    0x2bf304: paciasp  
    0x2bf308: sub      sp, sp, #0x110
    0x2bf30c: stp      x29, x30, [sp, #0xb0]
    0x2bf310: stp      x28, x27, [sp, #0xc0]
    0x2bf314: stp      x26, x25, [sp, #0xd0]
    0x2bf318: stp      x24, x23, [sp, #0xe0]

- gate 0x2e5fb0: first 32 bytes = 3f2303d5ff4303d1fd7b07a9fc6f08a9fa6709a9f85f0aa9f6570ba9f44f0ca9
  gate 0x2e5fb0 (VA 0x2e5fb0, file 0x2e1fb0):
    0x2e5fb0: paciasp  
    0x2e5fb4: sub      sp, sp, #0xd0
    0x2e5fb8: stp      x29, x30, [sp, #0x70]
    0x2e5fbc: stp      x28, x27, [sp, #0x80]
    0x2e5fc0: stp      x26, x25, [sp, #0x90]
    0x2e5fc4: stp      x24, x23, [sp, #0xa0]

- gate 0x2e1b54: first 32 bytes = 3f2303d5ff4305d1fd7b0fa9fc6f10a9fa6711a9f85f12a9f65713a9f44f14a9
  gate 0x2e1b54 (VA 0x2e1b54, file 0x2ddb54):
    0x2e1b54: paciasp  
    0x2e1b58: sub      sp, sp, #0x150
    0x2e1b5c: stp      x29, x30, [sp, #0xf0]
    0x2e1b60: stp      x28, x27, [sp, #0x100]
    0x2e1b64: stp      x26, x25, [sp, #0x110]
    0x2e1b68: stp      x24, x23, [sp, #0x120]


## Known Pro-capture natives (validation)
- 0x242048: 3f2303d5ff0303d1fd7b06a9fc6f07a9fa6708a9f85f09a9f6570aa9f44f0ba9
- 0x244680: 3f2303d5ff0303d1fd7b06a9fc6f07a9fa6708a9f85f09a9f6570aa9f44f0ba9

## String table hits

## RegisterNatives-style relocation triples
(0 natives)


# DEX analysis

## classes.dex

## pinned bridge methods
### Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->d(Z Z Z Z F Z)[J  (registers=13)
    sget-object            v0, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->a Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;
    move                   v1, v7
    move                   v2, v8
    move                   v3, v9
    move                   v4, v10
    move                   v5, v11
    move                   v6, v12
    invoke-direct/range    v0 ... v6, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->nd2cccb916f63efbcf8b5b19b(Z Z Z Z F Z)[J
    move-result-object     v7
    array-length           v8, v7
    const/4                v9, 2
    if-ne                  v8, v9, +012h
    const/4                v8, 1
    aget-wide              v8, v7, v8
    const-wide/16          v10, 0
    cmp-long               v10, v10, v8
    if-gtz                 v10, +009h
    const-wide/16          v10, 4
    cmp-long               v8, v8, v10
    if-gez                 v8, +003h
    return-object          v7
    const-string           v7, "VU5TVFJHMDIktLbTdppJXboe6N5GcSz6l/tiS5ZZpmrtX6JC+BQnauUd1AS1NMizxyzuwS+JBDUcCPZ1BWHGCPoxocg+eoU/H+6D3A6nfiqS"
    invoke-static          v7, Lapp/unnamed/rawrecording/b/B4ff45f4f1db7dd4649c2a084;->a(Ljava/lang/String;)Ljava/lang/String;
    move-result-object     v7
    new-instance           v8, Ljava/lang/IllegalStateException;
    invoke-virtual         v7, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object     v7
    invoke-direct          v8, v7, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw                  v8

### Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->n72c9e116bb85d91fbf8a15b2(J J J I I I I Z Z D D)[J  (registers=?)

### Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->nd2cccb916f63efbcf8b5b19b(Z Z Z Z F Z)[J  (registers=?)

### Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->p(J J J I I I I Z Z)[J  (registers=29)
    sget-object            v0, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->a Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;
    const-wide/16          v13, 0
    const-wide/16          v15, 0
    move-wide/from16       v1, v17
    move-wide/from16       v3, v19
    move-wide/from16       v5, v21
    move/from16            v7, v23
    move/from16            v8, v24
    move/from16            v9, v25
    move/from16            v10, v26
    move/from16            v11, v27
    move/from16            v12, v28
    invoke-direct/range    v0 ... v16, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->n72c9e116bb85d91fbf8a15b2(J J J I I I I Z Z D D)[J
    move-result-object     v0
    array-length           v1, v0
    const/4                v2, 3
    if-ne                  v1, v2, +003h
    return-object          v0
    const-string           v0, "VU5TVFJHMDIktLbTdppJXboe6N5GcSz6l/tiS5ZZpmrtX6JC+BQnauUd1AS1NMizxyzuwS+JBDUcCPZ1BWHGCPoxocg+eoU/H+6D3A6nfiqS"
    invoke-static          v0, Lapp/unnamed/rawrecording/b/B4ff45f4f1db7dd4649c2a084;->a(Ljava/lang/String;)Ljava/lang/String;
    move-result-object     v0
    new-instance           v1, Ljava/lang/IllegalStateException;
    invoke-virtual         v0, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object     v0
    invoke-direct          v1, v0, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw                  v1

## callers of p
### LJm;->Y0(Landroid/hardware/camera2/CameraCharacteristics; Landroid/hardware/camera2/TotalCaptureResult; Z I LBB; F Ljava/lang/Long; Ljava/lang/Long; D Z Z J I)LIr;  (registers=42)
    move-object/from16     v0, v27
    move-object/from16     v1, v28
    move/from16            v2, v41
    sget-object            v3, LSr;->a LSr;
    and-int/lit8           v3, v2, 4
    const/4                v4, 0
    if-eqz                 v3, +004h
    move                   v3, v4
    goto                   +3h
    move/from16            v3, v29
    and-int/lit8           v5, v2, 16
    if-eqz                 v5, +005h
    sget-object            v5, LBB;->c LBB;
    goto                   +3h
    move-object/from16     v5, v31
    and-int/lit8           v6, v2, 64
    const/4                v7, 0
    if-eqz                 v6, +004h
    move-object            v6, v7
    goto                   +3h
    move-object/from16     v6, v33
    and-int/lit16          v8, v2, 128
    if-eqz                 v8, +004h
    move-object            v8, v7
    goto                   +3h
    move-object/from16     v8, v34
    and-int/lit16          v9, v2, 256
    if-eqz                 v9, +005h
    const-wide/high16      v9, 4607182418800017408
    goto                   +3h
    move-wide/from16       v9, v35
    and-int/lit16          v11, v2, 512
    if-eqz                 v11, +004h
    move                   v11, v4
    goto                   +3h
    move/from16            v11, v37
    and-int/lit16          v12, v2, 1024
    if-eqz                 v12, +004h
    move                   v12, v4
    goto                   +3h
    move/from16            v12, v38
    and-int/lit16          v2, v2, 2048
    if-eqz                 v2, +008h
    const-wide             v13, 9223372036854775807
    goto                   +3h
    move-wide/from16       v13, v39
    const-string           v2, "VU5TVFJHMDIktLbTdppJXboe6N5GcSz6l/tiS5ZZpmrtX6JC+BQnasxT6vWGSx7CU3Cv95s5rexCPFlrKv+SWnP3yOT7VGEwVYRDJDXn+r2rEhU="
    invoke-static          v2, Lapp/unnamed/rawrecording/b/B4ff45f4f1db7dd4649c2a084;->a(Ljava/lang/String;)Ljava/lang/String;
    move-result-object     v2
    invoke-static          v0, v2, Lgi;->j(Ljava/lang/Object; Ljava/lang/String;)V
    const-string           v2, "VU5TVFJHMDIktLbTdppJXboe6N5GcSz6l/tiS5ZZpmrtX6JC+BQnavhNTuTgy/wHPxiLlijibisiraUGf/582WAxrhwVKOsS"
    invoke-static          v2, Lapp/unnamed/rawrecording/b/B4ff45f4f1db7dd4649c2a084;->a(Ljava/lang/String;)Ljava/lang/String;
    move-result-object     v2
    invoke-static          v5, v2, Lgi;->j(Ljava/lang/Object; Ljava/lang/String;)V
    invoke-static          v0, v1, v4, v12, LJm;->t0(Landroid/hardware/camera2/CameraCharacteristics; Landroid/hardware/camera2/TotalCaptureResult; Z Z)LIr;
    move-result-object     v2
    if-nez                 v2, +004h
    goto/16                +126h
    sget-object            v7, LDD;->a Lvy;
    if-eqz                 v3, +005h
    sget-object            v3, LX3;->c LX3;
    goto                   +3h
    sget-object            v3, LX3;->b LX3;
    invoke-virtual         v7, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sput-object            v0, Lfi;->g Landroid/hardware/camera2/CameraCharacteristics;
    sget-object            v7, LX3;->d LX3;
    const/4                v12, 1
    if-eq                  v3, v7, +009h
    sget-object            v7, LX3;->e LX3;
    if-ne                  v3, v7, +003h
    goto                   +3h
    move                   v7, v4
    goto                   +2h
    move                   v7, v12
    invoke-static          v4, LZm;->M(Z)J
    move-result-wide       v17
    invoke-static          v12, LZm;->M(Z)J
    move-result-wide       v19
    invoke-static          v4, LZm;->T(Z)I
    move-result            v22
    invoke-static          v12, LZm;->T(Z)I
    move-result            v23
    sget-object            v15, Landroid/hardware/camera2/CameraCharacteristics;->SENSOR_MAX_ANALOG_SENSITIVITY Landroid/hardware/camera2/CameraCharacteristics$Key;
    sget-object            v12, Lfi;->g Landroid/hardware/camera2/CameraCharacteristics;
    invoke-virtual         v12, v15, Landroid/hardware/camera2/CameraCharacteristics;->get(Landroid/hardware/camera2/CameraCharacteristics$Key;)Ljava/lang/Object;
    move-result-object     v12
    if-nez                 v12, +007h
    const/16               v12, 100
    move/from16            v24, v12
    goto                   +8h
    check-cast             v12, Ljava/lang/Integer;
    invoke-virtual         v12, Ljava/lang/Integer;->intValue()I
    move-result            v12
    goto                   -9h
    sget-object            v12, LX3;->c LX3;
    if-ne                  v3, v12, +006h
    move-object            v12, v5
    const/16               v25, 1
    goto                   +4h
    move/from16            v25, v4
    move-object            v12, v5
    iget-wide              v4, v2, LIr;->b J
    iget                   v15, v2, LIr;->a I
    move/from16            v21, v15
    move-wide              v15, v4
    move/from16            v26, v7
    invoke-static/range    v15 ... v26, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->p(J J J I I I I Z Z)[J
    move-result-object     v4
    const/4                v3, 0
    aget-wide              v15, v4, v3
    const/4                v3, 1
    aget-wide              v4, v4, v3
    long-to-int            v3, v4
    if-nez                 v7, +004h
    sget-object            v4, Lni;->b [Lni;
    const/16               v4, 1000
    int-to-long            v4, v4
    move-wide/from16       v17, v9
    iget-wide              v9, v2, LIr;->b J
    div-long/2addr         v9, v4
    div-long               v9, v15, v4
    new-instance           v7, LIr;
    const/4                v9, 0
    iget-object            v2, v2, LIr;->d Ljava/lang/Integer;
    move-object/from16     v33, v7
    move/from16            v34, v3
    move-wide/from16       v35, v15
    move-object/from16     v37, v9
    move-object/from16     v38, v2
    invoke-direct/range    v33 ... v38, LIr;-><init>(I J Ljava/lang/Long; Ljava/lang/Integer;)V
    if-eqz                 v1, +00ah
    sget-object            v2, Landroid/hardware/camera2/CaptureResult;->STATISTICS_FACES Landroid/hardware/camera2/CaptureResult$Key;
    invoke-virtual         v1, v2, Landroid/hardware/camera2/CaptureResult;->get(Landroid/hardware/camera2/CaptureResult$Key;)Ljava/lang/Object;
    move-result-object     v2
    check-cast             v2, [Landroid/hardware/camera2/params/Face;
    move/from16            v2, v32
    invoke-static          v0, v1, v2, LJm;->A(Landroid/hardware/camera2/CameraCharacteristics; Landroid/hardware/camera2/TotalCaptureResult; F)F
    move-result            v9
    if-eqz                 v6, +009h
    invoke-virtual         v6, Ljava/lang/Long;->longValue()J
    move-result-wide       v19
    move-wide/from16       v1, v19
    goto                   +15h
    const/4                v6, 1
    move-object/from16     v33, v27
    move-object/from16     v34, v28
    move/from16            v35, v6
    move/from16            v36, v30
    move-object/from16     v37, v12
    move/from16            v38, v32
    move/from16            v39, v11
    invoke-static/range    v33 ... v39, LJm;->l(Landroid/hardware/camera2/CameraCharacteristics; Landroid/hardware/camera2/TotalCaptureResult; Z I LBB; F Z)J
    move-result-wide       v19
    goto                   -16h
    if-eqz                 v8, +007h
    invoke-virtual         v8, Ljava/lang/Long;->longValue()J
    move-result-wide       v10
    goto                   +14h
    const/4                v6, 0
    move-object/from16     v33, v27
    move-object/from16     v34, v28
    move/from16            v35, v6
    move/from16            v36, v30
    move-object/from16     v37, v12
    move/from16            v38, v32
    move/from16            v39, v11
    invoke-static/range    v33 ... v39, LJm;->l(Landroid/hardware/camera2/CameraCharacteristics; Landroid/hardware/camera2/TotalCaptureResult; Z I LBB; F Z)J
    move-result-wide       v10
    const-string           v6, "VU5TVFJHMDIktLbTdppJXboe6N5GcSz6l/tiS5ZZpmrtX6JC+BQnanQSuel6mbSY15jvgowT0zw/zkOH0qqJAJBGPpHN1Av5"
    invoke-static          v6, Lapp/unnamed/rawrecording/b/B4ff45f4f1db7dd4649c2a084;->a(Ljava/lang/String;)Ljava/lang/String;
    move-result-object     v6
    sget-object            v8, Ljava/util/Locale;->US Ljava/util/Locale;
    invoke-static          v9, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;
    move-result-object     v9
    filled-new-array       v9, [Ljava/lang/Object;
    move-result-object     v9
    const/4                v0, 1
    invoke-static          v9, v0, Ljava/util/Arrays;->copyOf([Ljava/lang/Object; I)[Ljava/lang/Object;
    move-result-object     v0
    invoke-static          v8, v6, v0, Ljava/lang/String;->format(Ljava/util/Locale; Ljava/lang/String; [Ljava/lang/Object;)Ljava/lang/String;
    const                  v0, 1000000
    int-to-long            v8, v0
    div-long               v19, v1, v8
    div-long               v8, v10, v8
    invoke-virtual         v12, Ljava/lang/Object;->toString()Ljava/lang/String;
    invoke-static          v1, v2, v13, v14, Ljava/lang/Math;->min(J J)J
    move-result-wide       v0
    invoke-static          v10, v11, v13, v14, Ljava/lang/Math;->min(J J)J
    move-result-wide       v8
    const/16               v2, 80
    move-object/from16     v28, v27
    move-object/from16     v29, v7
    move-wide/from16       v30, v0
    move-wide/from16       v32, v8
    move-wide/from16       v34, v17
    move/from16            v36, v2
    invoke-static/range    v28 ... v36, LJm;->C(Landroid/hardware/camera2/CameraCharacteristics; LIr; J J D I)LIr;
    move-result-object     v0
    iget-wide              v1, v0, LIr;->b J
    cmp-long               v6, v1, v15
    if-nez                 v6, +00ah
    iget                   v6, v0, LIr;->a I
    if-eq                  v6, v3, +003h
    goto                   +4h
    move-object/from16     v1, v27
    goto                   +3h
    div-long/2addr         v1, v4
    goto                   -4h
    invoke-static          v1, v0, LJm;->Z(Landroid/hardware/camera2/CameraCharacteristics; LIr;)J
    move-result-wide       v1
    invoke-static          v1, v2, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object     v1
    const-wide/16          v2, 0
    const/16               v4, 11
    invoke-static          v0, v2, v3, v1, v4, LIr;->a(LIr; J Ljava/lang/Long; I)LIr;
    move-result-object     v7
    return-object          v7

### Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->p(J J J I I I I Z Z)[J  (registers=29)
    sget-object            v0, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->a Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;
    const-wide/16          v13, 0
    const-wide/16          v15, 0
    move-wide/from16       v1, v17
    move-wide/from16       v3, v19
    move-wide/from16       v5, v21
    move/from16            v7, v23
    move/from16            v8, v24
    move/from16            v9, v25
    move/from16            v10, v26
    move/from16            v11, v27
    move/from16            v12, v28
    invoke-direct/range    v0 ... v16, Lapp/unnamed/rawrecording/b/B1e6ad8077f6880e309d8da4b;->n72c9e116bb85d91fbf8a15b2(J J J I I I I Z Z D D)[J
    move-result-object     v0
    array-length           v1, v0
    const/4                v2, 3
    if-ne                  v1, v2, +003h
    return-object          v0
    const-string           v0, "VU5TVFJHMDIktLbTdppJXboe6N5GcSz6l/tiS5ZZpmrtX6JC+BQnauUd1AS1NMizxyzuwS+JBDUcCPZ1BWHGCPoxocg+eoU/H+6D3A6nfiqS"
    invoke-static          v0, Lapp/unnamed/rawrecording/b/B4ff45f4f1db7dd4649c2a084;->a(Ljava/lang/String;)Ljava/lang/String;
    move-result-object     v0
    new-instance           v1, Ljava/lang/IllegalStateException;
    invoke-virtual         v0, Ljava/lang/Object;->toString()Ljava/lang/String;
    move-result-object     v0
    invoke-direct          v1, v0, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V
    throw                  v1

