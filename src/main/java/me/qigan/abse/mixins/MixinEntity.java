package me.qigan.abse.mixins;

import me.qigan.abse.fr.exc.PhantomAim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.vecmath.Vector2f;
import java.util.UUID;

@SideOnly(Side.CLIENT)
@Mixin(Entity.class)
public abstract class MixinEntity {

    @Shadow
    public float rotationYaw;
    @Shadow
    public float rotationPitch;
    @Shadow
    public double motionX;
    @Shadow
    public double motionY;
    @Shadow
    public double motionZ;


    @Shadow public abstract UUID getUniqueID();

    @Shadow public abstract void setSprinting(boolean p_setSprinting_1_);

    @Unique
    private Vector2f absoluteEvil$genVec(float p_moveFlying_1_, float p_moveFlying_2_, float rot) {
        float f1 = MathHelper.sin(rot);
        float f2 = MathHelper.cos(rot);
        return new Vector2f((p_moveFlying_1_ * f2 - p_moveFlying_2_ * f1), (p_moveFlying_2_ * f2 + p_moveFlying_1_ * f1));
    }

    /**
     * @author qigan
     * @reason changing move flying function to fix movement off camera
     */
    @Overwrite
    public void moveFlying(float p_moveFlying_1_, float p_moveFlying_2_, float p_moveFlying_3_) {
        float rot = this.rotationYaw * 3.1415927F / 180.0F;
        float f = p_moveFlying_1_ * p_moveFlying_1_ + p_moveFlying_2_ * p_moveFlying_2_;
        if (f >= 1.0E-4F) {
            f = MathHelper.sqrt_float(f);
            if (f < 1.0F) {
                f = 1.0F;
            }

            f = p_moveFlying_3_ / f;
            p_moveFlying_1_ *= f;
            p_moveFlying_2_ *= f;

            Vector2f targetVec = absoluteEvil$genVec(p_moveFlying_1_, p_moveFlying_2_, rot);
            if (this.getUniqueID().equals(Minecraft.getMinecraft().thePlayer.getUniqueID()) && PhantomAim.enabled) {
                float rotCustom = PhantomAim.currentAngles[0] * 3.1415927F / 180.0F;
                Vector2f vec;
                double vecProd = 0d;
                Vector2f choice = null;
                double res = 0d;

                Vector2f fwdVec = absoluteEvil$genVec(p_moveFlying_1_, p_moveFlying_2_, rotCustom);
                vec = (Vector2f) fwdVec.clone();
                vecProd = vec.dot(targetVec);
                res = vecProd;choice = vec;

                vec = absoluteEvil$genVec(-p_moveFlying_1_, p_moveFlying_2_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}
                vec = absoluteEvil$genVec(p_moveFlying_1_, -p_moveFlying_2_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}
                vec = absoluteEvil$genVec(-p_moveFlying_1_, -p_moveFlying_2_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}

                vec = absoluteEvil$genVec(p_moveFlying_2_, p_moveFlying_1_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}
                vec = absoluteEvil$genVec(-p_moveFlying_2_, p_moveFlying_1_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}
                vec = absoluteEvil$genVec(p_moveFlying_2_, -p_moveFlying_1_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}
                vec = absoluteEvil$genVec(-p_moveFlying_2_, -p_moveFlying_1_, rotCustom);
                vecProd = vec.dot(targetVec);
                if (vecProd > res) {res = vecProd;choice = vec;}

                if (choice.equals(fwdVec) && Minecraft.getMinecraft().gameSettings.keyBindSprint.isKeyDown())
                    this.setSprinting(true);
                else
                    this.setSprinting(false);

                this.motionX += (double)(choice.x);
                this.motionZ += (double)(choice.y);
            } else {
                this.motionX += (double)(targetVec.x);
                this.motionZ += (double)(targetVec.y);
            }
        }

    }
}
