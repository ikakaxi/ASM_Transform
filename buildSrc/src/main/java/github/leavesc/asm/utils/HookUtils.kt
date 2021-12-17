package github.leavesc.asm.utils

import github.leavesc.asm.plugins.double_click.DoubleClickConfig
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * @Author: leavesC
 * @Date: 2021/12/7 11:07
 * @Desc:
 */
val MethodNode.nameWithDesc: String
    get() = name + desc

val MethodNode.isStatic: Boolean
    get() = access and Opcodes.ACC_STATIC != 0

val MethodNode.isInitMethod: Boolean
    get() = name == "<init>"

fun MethodNode.hasAnnotation(annotationDesc: String): Boolean {
    return visibleAnnotations?.find { it.desc == annotationDesc } != null
}

fun ClassNode.isHookPoint(config: DoubleClickConfig, methodNode: MethodNode): Boolean {
    val myInterfaces = interfaces
    if (myInterfaces.isNullOrEmpty()) {
        return false
    }
    val extraHookMethodList = config.hookPointList
    extraHookMethodList.forEach {
        if (myInterfaces.contains(it.interfaceName) && methodNode.nameWithDesc == it.methodSign) {
            return true
        }
    }
    return false
}

fun MethodNode.findHookPointLambda(config: DoubleClickConfig): List<InvokeDynamicInsnNode> {
    val onClickListenerLambda = findLambda {
        val nodeName = it.name
        val nodeDesc = it.desc
        val find = config.hookPointList.find { point ->
            nodeName == point.methodName && nodeDesc.endsWith(point.interfaceSignSuffix)
        }
        return@findLambda find != null
    }
    return onClickListenerLambda
}

private fun MethodNode.findLambda(
    filter: (InvokeDynamicInsnNode) -> Boolean
): List<InvokeDynamicInsnNode> {
    val handleList = mutableListOf<InvokeDynamicInsnNode>()
    val instructions = instructions?.iterator() ?: return handleList
    while (instructions.hasNext()) {
        val nextInstruction = instructions.next()
        if (nextInstruction is InvokeDynamicInsnNode) {
            if (filter(nextInstruction)) {
                handleList.add(nextInstruction)
            }
        }
    }
    return handleList
}