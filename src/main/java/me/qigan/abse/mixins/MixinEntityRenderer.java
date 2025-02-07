package me.qigan.abse.mixins;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import me.qigan.abse.Index;
import me.qigan.abse.fr.exc.PhantomAim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;
    @Shadow
    private Entity pointedEntity;

    /**
     * @author qigan
     * @reason implementing custom features
     */
    @Overwrite
    public void getMouseOver(float p_getMouseOver_1_) {
        Entity entity = this.mc.getRenderViewEntity();
        EntityLivingBase livingBase = null;
        if (entity instanceof EntityPlayerSP) {
            livingBase = (EntityPlayerSP) entity;
        }
        if (entity != null && this.mc.theWorld != null) {

            float yaw1 = entity.rotationYaw;
            float yaw2 = entity.prevRotationYaw;
            float yaw3 = livingBase == null ? 0f : livingBase.rotationYawHead;
            float pitch1 = entity.rotationPitch;
            float pitch2 = entity.prevRotationPitch;
            if (PhantomAim.enabled) {
                entity.rotationYaw = entity.prevRotationYaw = PhantomAim.currentAngles[0];
                if (livingBase != null) livingBase.rotationYawHead = PhantomAim.currentAngles[0];
                entity.rotationPitch = entity.prevRotationPitch = PhantomAim.currentAngles[1];
            }
            this.mc.mcProfiler.startSection("pick");
            this.mc.pointedEntity = null;
            double d0 = (double)this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = entity.rayTrace(d0, p_getMouseOver_1_);

            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
            boolean flag = false;
            int i = 1;
            if (this.mc.playerController.extendedReach()) {
                d0 = 6.0;
                d1 = 6.0;
            } else if (d0 > 3.0) {
                flag = true;
            }

            if (this.mc.objectMouseOver != null) {
                d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(p_getMouseOver_1_);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = this.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double)f, (double)f, (double)f), Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>() {
                public boolean apply(Entity p_apply_1_) {
                    return p_apply_1_.canBeCollidedWith();
                }
            }));
            double d2 = d1;

            for(int j = 0; j < list.size(); ++j) {
                Entity entity1 = (Entity)list.get(j);
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d3 < d2 || d2 == 0.0) {
                        if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                            if (d2 == 0.0) {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0) {
                this.pointedEntity = null;
                this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, (EnumFacing)null, new BlockPos(vec33));
            }

            if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            entity.rotationYaw = yaw1;
            entity.prevRotationYaw = yaw2;
            if (livingBase != null) livingBase.rotationYawHead = yaw3;
            entity.rotationPitch = pitch1;
            entity.prevRotationPitch = pitch2;
            this.mc.mcProfiler.endSection();
        }
    }
}
