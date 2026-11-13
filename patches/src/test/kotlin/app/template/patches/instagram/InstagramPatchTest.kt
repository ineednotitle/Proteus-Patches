package app.template.patches.instagram

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.instagram.misc.rewriteInstagramAuthority
import app.template.patches.instagram.misc.rewriteInstagramPackagePrefix
import app.template.patches.instagram.shared.replaceJsonFieldWithBogus
import app.template.patches.shared.cloneMutable
import app.template.patches.shared.getReference
import app.template.patches.shared.numberOfParameterRegisters
import app.template.patches.shared.returnBoxedBooleanEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10x
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.ImmutableClassDef
import com.android.tools.smali.dexlib2.immutable.ImmutableDexFile
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import com.android.tools.smali.dexlib2.writer.io.MemoryDataStore
import com.android.tools.smali.dexlib2.writer.pool.DexPool
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream

/** Run by the testInstagram Gradle task; verifies DEX round-trips and clone helpers without an APK. */
object InstagramPatchTest {
    @JvmStatic
    fun main(args: Array<String>) {
        testReplaceJsonFieldWithBogus()
        testReturnBoxedBooleanEarly()
        testCloneParametersPreservesLongParam()
        testCloneRewritingHelpers()
        println("Instagram patch DEX round trips and helper tests passed")
    }

    private fun testReplaceJsonFieldWithBogus() {
        val body = MutableMethodImplementation(2).apply {
            addInstruction(BuilderInstruction21c(Opcode.CONST_STRING, 0, ImmutableStringReference("feed_items")))
            addInstruction(BuilderInstruction21c(Opcode.CONST_STRING, 1, ImmutableStringReference("auto_load_more_enabled")))
            addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
        }
        val method = MutableMethod(
            ImmutableMethod(
                "LInstagramTest;",
                "unsafeParseFromJson",
                emptyList(),
                "V",
                AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
                null,
                null,
                body,
            ),
        )
        method.replaceJsonFieldWithBogus("feed_items")
        val roundTripped = roundTrip(method)
        val strings = roundTripped.implementation!!.instructions.mapNotNull {
            it.getReference<StringReference>()?.string
        }
        check(strings == listOf("BOGUS", "auto_load_more_enabled")) {
            "Expected BOGUS replacement, got $strings"
        }
    }

    private fun testReturnBoxedBooleanEarly() {
        val body = MutableMethodImplementation(1).apply {
            addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
        }
        val method = MutableMethod(
            ImmutableMethod(
                "LInstagramTest;",
                "enableNotes",
                emptyList(),
                "Ljava/lang/Boolean;",
                AccessFlags.PUBLIC.value,
                null,
                null,
                body,
            ),
        )
        method.returnBoxedBooleanEarly(false)
        val insns = roundTrip(method).implementation!!.instructions.toList()
        check(insns[0].opcode == Opcode.SGET_OBJECT && insns[1].opcode == Opcode.RETURN_OBJECT) {
            "Expected boxed Boolean early return, got $insns"
        }
    }

    private fun testCloneParametersPreservesLongParam() {
        val params = listOf(
            ImmutableMethodParameter("Ljava/lang/Object;", null, null),
            ImmutableMethodParameter("J", null, null),
            ImmutableMethodParameter("Z", null, null),
        )
        val body = MutableMethodImplementation(5).apply {
            addInstruction(BuilderInstruction10x(Opcode.RETURN_VOID))
        }
        val original = ImmutableMethod(
            "LInstagramTest;",
            "getBoolFlag",
            params,
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.FINAL.value,
            null,
            null,
            body,
        )
        val cloned = original.cloneMutable(additionalRegisters = original.numberOfParameterRegisters)
        val insns = roundTrip(cloned).implementation!!.instructions.toList()
        check(cloned.implementation!!.registerCount == 10) {
            "Expected 5 original + 5 parameter registers = 10, got ${cloned.implementation!!.registerCount}"
        }
        check(insns.first().opcode == Opcode.MOVE_OBJECT_FROM16) {
            "Expected move-object/from16 prologue for `this`, got ${insns.first().opcode}"
        }
        check(insns[2].opcode == Opcode.MOVE_WIDE_FROM16) {
            "Expected move-wide/from16 prologue for long param, got ${insns[2].opcode}"
        }
    }

    private fun testCloneRewritingHelpers() {
        check(
            rewriteInstagramPackagePrefix("com.instagram.android.permission.RECEIVE", "com.instagram.android.proteus") ==
                "com.instagram.android.proteus.permission.RECEIVE",
        )
        check(
            rewriteInstagramPackagePrefix("com.instagram.android.proteus.permission.RECEIVE", "com.instagram.android.proteus") ==
                "com.instagram.android.proteus.permission.RECEIVE",
        )
        check(
            rewriteInstagramAuthority("com.instagram.android.fileprovider", "com.instagram.android.proteus") ==
                "com.instagram.android.proteus.fileprovider",
        )
        check(
            rewriteInstagramAuthority("com.facebook.appmanager.provider", "com.instagram.android.proteus") ==
                "com.instagram.android.proteus_com.facebook.appmanager.provider",
        )
    }

    private fun roundTrip(method: Method): Method {
        val cls = ImmutableClassDef(
            "LInstagramTest;",
            AccessFlags.PUBLIC.value,
            "Ljava/lang/Object;",
            emptyList(),
            null,
            emptyList(),
            emptyList(),
            listOf(method),
        )
        val store = MemoryDataStore()
        val opcodes = Opcodes.getDefault()
        DexPool.writeTo(store, ImmutableDexFile(opcodes, listOf(cls)))
        return DexBackedDexFile.fromInputStream(opcodes, BufferedInputStream(ByteArrayInputStream(store.data)))
            .classes.single().methods.single()
    }
}
