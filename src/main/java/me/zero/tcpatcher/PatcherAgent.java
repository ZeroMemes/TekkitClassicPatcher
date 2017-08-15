package me.zero.tcpatcher;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author Brady
 * @since 8/15/2017 5:13 PM
 */
public final class PatcherAgent implements ClassFileTransformer {

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new PatcherAgent(), true);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className.equals("codechicken/nei/NEIServerPacketHandler")) {
            try {
                ClassPool pool = ClassPool.getDefault();

                pool.importPackage("cpw.mods.fml.server");
                pool.importPackage("org.bukkit.craftbukkit.entity");

                CtClass ctClass = pool.get("codechicken.nei.NEIServerPacketHandler");
                CtMethod ctMethod = ctClass.getMethod("handlePacket", "(Lcodechicken/core/PacketCustom;Lnet/minecraft/server/NetServerHandler;Lnet/minecraft/server/EntityPlayer;)V");
                ctMethod.insertBefore("{ if (!FMLBukkitHandler.instance().getServer().serverConfigurationManager.isOp(((CraftPlayer) $3.getBukkitEntity()).getName())) return; }");

                byte[] bytecode = ctClass.toBytecode();
                System.out.println("[TekkitClassicPatcher] Successfully transformed target class");
                return bytecode;

            } catch (NotFoundException | CannotCompileException | IOException e) {
                System.out.println("[TekkitClassicPatcher] Unable to transform target class");
                e.printStackTrace();
            }
        }

        return classfileBuffer;
    }
}
