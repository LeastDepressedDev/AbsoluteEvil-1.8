package me.qigan.abse.mixins;

import me.qigan.abse.events.LivingHurtEvent;
import me.qigan.abse.events.PostMotionEvent;
import me.qigan.abse.events.PreMotionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SideOnly(Side.CLIENT)
@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void evPost(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PostMotionEvent());
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    public void evPre(CallbackInfo ci) {
        EntityPlayerSP playerSP = (EntityPlayerSP) (Object) this;
        MinecraftForge.EVENT_BUS.post(new PreMotionEvent(playerSP.rotationYaw, playerSP.rotationPitch, playerSP.onGround, playerSP.posX, playerSP.posY, playerSP.posZ));
    }
}
