/*
 * This file is part of JourneyMapFix, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.github.barteks2x.journeymapfix.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

public class JMPatchTransformer implements IClassTransformer {

    @Override public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if ("journeymap.client.world.ChunkMonitor".equals(transformedName)) {
            return transformJM(basicClass);
        }
        return basicClass;
    }

    private byte[] transformJM(byte[] bytes) {
        final String isRempte = Mappings.getNameFromSrg("field_72995_K");
        ClassReader cr = new ClassReader(bytes);

        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (!name.equals("onChunkLoad")) {
                    return super.visitMethod(access, name, desc, signature, exceptions);
                }
                return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions)) {
                    @Override public void visitCode() {
                        System.out.println("Transforming JourneyMap code: journeymap.client.world.ChunkMonito.onChunkLoad()");
                        super.visitCode();
                        super.visitVarInsn(Opcodes.ALOAD, 1);
                        super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "net/minecraftforge/event/world/WorldEvent",
                                "getWorld", "()Lnet/minecraft/world/World;", false);
                        super.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/world/World", isRempte, "Z");
                        Label cont = new Label();
                        super.visitJumpInsn(Opcodes.IFNE, cont);
                        super.visitInsn(Opcodes.RETURN);
                        super.visitLabel(cont);
                    }
                };
            }
        };

        cr.accept(cv, 0);

        return cw.toByteArray();
    }
}
