package me.qigan.abse.mixins;

import me.qigan.abse.fr.exc.InvWalk;
import me.qigan.abse.fr.exc.TimeoutTasks;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    private int leftClickCounter;

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    public void runGameBefore(CallbackInfo ci) {
        TimeoutTasks.runGameRef();
    }

    @Inject(method = "runGameLoop", at = @At("TAIL"))
    public void runGameTasksAfter(CallbackInfo ci) {

    }

    @Inject(method = "setIngameNotInFocus", at = @At("TAIL"))
    public void setIngameNotInFocus(CallbackInfo ci) {
        if (Minecraft.getMinecraft().theWorld == null || !InvWalk.shadowRotation) return;
        if (Minecraft.getMinecraft().currentScreen != null) Minecraft.getMinecraft().currentScreen.allowUserInput = true;
        Minecraft mc = Minecraft.getMinecraft();
        mc.mouseHelper.grabMouseCursor();
        mc.inGameHasFocus = true;
    }

//    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/FMLCommonHandler;fireKeyInput()V"))
//    public void pp(CallbackInfo ci) {
//        System.out.println("Handling");
//    }

//    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;handleKeyboardInput()V"))
//    public void runTick(CallbackInfo ci) {
//
//    }
}
