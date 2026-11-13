/*
 * Copyright (C) 2026 piko <https://github.com/crimera/piko>
 *
 * Adapted from https://github.com/crimera/piko (GPL-3.0).
 * See META-INF/PROTEUS-NOTICE.txt for GPLv3 §7(b) terms.
 */

package app.template.patches.instagram.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INSTAGRAM_COMPATIBILITY
import app.template.patches.shared.getFreeRegisterProvider
import app.template.patches.shared.getReference
import app.template.patches.shared.registersUsed
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.StringReference

private const val EXTENSION_CLASS_DESCRIPTOR =
    "Lapp/template/extension/extension/InstagramLikeAnimation;"

private const val USER_SESSION_CLASS = "Lcom/instagram/common/session/UserSession;"
private const val LIKE_VIEW = "Lcom/instagram/ui/mediaactions/LikeActionView;"
private const val CONTEXT = "Landroid/content/Context;"

private object AnimationClassFingerprint : Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = "animationClass",
)

private object MapAnimationFingerprint : Fingerprint(
    definingClass = EXTENSION_CLASS_DESCRIPTOR,
    name = "mapAnimation",
)

private object XDTUserActivationMetadataImplInitFingerprint : Fingerprint(
    name = "<init>",
    definingClass = "Lcom/instagram/api/schemas/XDTUserActivationMetadataImpl;",
)

private object SetUpCustomLikesAnimationFingerprint : Fingerprint(
    definingClass = LIKE_VIEW,
    name = "setUpCustomLikesAnimation",
    returnType = "V",
    parameters = listOf("L"),
    filters = listOf(
        // Animation drawable factory.
        methodCall(opcode = Opcode.INVOKE_STATIC, parameters = listOf(CONTEXT, "L")),
    ),
)

private object ConfigureLikeAnimationFingerprint : Fingerprint(
    definingClass = LIKE_VIEW,
    returnType = "V",
    parameters = listOf(USER_SESSION_CLASS, "L"),
    filters = listOf(
        methodCall(definingClass = "this", name = "setUpCustomLikesAnimation"),
        // Default heart drawable factory instance.
        fieldAccess(opcode = Opcode.SGET_OBJECT),
        methodCall(opcode = Opcode.INVOKE_VIRTUAL, parameters = listOf(CONTEXT)),
    ),
)

@Suppress("unused")
val instagramChangeLikeAnimationPatch = bytecodePatch(
    name = "Change like animation",
    description = "Lets you choose one of Instagram's built-in double-tap like animations, " +
        "or pick a random one per session, from the Proteus settings dialog. Experimental on " +
        "builds where Instagram renamed the animation classes.",
    default = false,
) {
    compatibleWith(INSTAGRAM_COMPATIBILITY)
    dependsOn(instagramSettingsPatch)

    execute {
        val xdtMethod = XDTUserActivationMetadataImplInitFingerprint.method
        if (xdtMethod.parameterTypes.size < 2 ||
            !xdtMethod.parameterTypes[0].startsWith("L") ||
            xdtMethod.parameterTypes[1] != "Ljava/lang/Boolean;"
        ) {
            throw PatchException("Unexpected user-activation metadata parameters")
        }
        val animationEnumClassType = xdtMethod.parameterTypes[0].toString()

        if (!animationEnumClassType.endsWith(";")) {
            throw PatchException("Unexpected like animation enum type: $animationEnumClassType")
        }

        val renderType = SetUpCustomLikesAnimationFingerprint
            .originalMethod
            .parameterTypes
            .first()
            .toString()

        val mapper = classDefBy(renderType).methods.singleOrNull { method ->
            method.parameterTypes == listOf(animationEnumClassType) &&
                method.returnType == renderType &&
                AccessFlags.PUBLIC.isSet(method.accessFlags) &&
                AccessFlags.STATIC.isSet(method.accessFlags)
        } ?: throw PatchException(
            "Expected one like animation mapper from $animationEnumClassType to $renderType",
        )

        // Point the extension's reflective enum lookup at the real class.
        val enumClassName = animationEnumClassType
            .removePrefix("L")
            .removeSuffix(";")
            .replace('/', '.')
        val stringInstruction = AnimationClassFingerprint.method.instructions.firstOrNull {
            (it.opcode == Opcode.CONST_STRING || it.opcode == Opcode.CONST_STRING_JUMBO) &&
                it.getReference<StringReference>()?.string == "className"
        } ?: throw PatchException("Expected the animation class placeholder string")
        val stringRegister = stringInstruction.registersUsed[0]
        AnimationClassFingerprint.method.replaceInstruction(
            stringInstruction.location.index,
            "const-string v$stringRegister, \"$enumClassName\"",
        )

        // Dispatch selected animation values through Instagram's own mapper.
        MapAnimationFingerprint.method.addInstructions(
            0,
            """
                check-cast p0, $animationEnumClassType
                invoke-static {p0}, $mapper
                move-result-object p0
                return-object p0
            """.trimIndent(),
        )

        // Replace the rendering animation used by the like button.
        ConfigureLikeAnimationFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p2}, $EXTENSION_CLASS_DESCRIPTOR->changeRenderAnimation(Ljava/lang/Object;)Ljava/lang/Object;
                move-result-object p2
                check-cast p2, $renderType
            """.trimIndent(),
        )

        // Select the animation in the user-activation metadata constructor. The
        // extra Boolean marks the value as an explicit choice, matching the
        // native metadata contract.
        val registers = xdtMethod.getFreeRegisterProvider(0, 2)
        val resultRegister = registers.getFreeRegister()
        val flagRegister = registers.getFreeRegister()
        xdtMethod.addInstructions(
            0,
            """
                sget-object v$flagRegister, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                invoke-static {p1}, $EXTENSION_CLASS_DESCRIPTOR->changeLikeAnimation(Ljava/lang/Object;)Ljava/lang/Object;
                move-result-object v$resultRegister
                if-eqz v$resultRegister, :proteus_keep_animation
                sget-object v$flagRegister, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                :proteus_keep_animation
                check-cast v$resultRegister, $animationEnumClassType
                move-object/from16 p1, v$resultRegister
                move-object/from16 p2, v$flagRegister
            """.trimIndent(),
        )
    }
}
