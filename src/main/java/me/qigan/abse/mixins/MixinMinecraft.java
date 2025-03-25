package me.qigan.abse.mixins;

import me.qigan.abse.fr.exc.TimeoutTasks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    public void runGameBefore(CallbackInfo ci) {
        TimeoutTasks.runGameRef();
    }

    @Inject(method = "runGameLoop", at = @At("TAIL"))
    public void runGameTasksAfter(CallbackInfo ci) {

    }
}
