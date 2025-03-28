package me.qigan.abse.mixins;

import me.qigan.abse.Index;
import me.qigan.abse.fr.exc.InvWalk;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SideOnly(Side.CLIENT)
@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions {
    
    /**
     * @author qigan
     * @reason Overwriting movement to disable player's one
     */
    @Overwrite
    public void updatePlayerMoveState() {
        MovementInput inpOf = (MovementInput) (Object) this;
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        inpOf.moveStrafe = 0.0F;
        inpOf.moveForward = 0.0F;
        if (Index.PLAYER_CONTROLLER.globalToggle) {
            if (Minecraft.getMinecraft().currentScreen != null) return;
            if (Index.PLAYER_CONTROLLER.goStateOvr[0]) {
                ++inpOf.moveForward;
            }

            if (Index.PLAYER_CONTROLLER.goStateOvr[1]) {
                --inpOf.moveForward;
            }

            if (Index.PLAYER_CONTROLLER.goStateOvr[2]) {
                ++inpOf.moveStrafe;
            }

            if (Index.PLAYER_CONTROLLER.goStateOvr[3]) {
                --inpOf.moveStrafe;
            }

            inpOf.jump = Index.PLAYER_CONTROLLER.jump;
            inpOf.sneak = Index.PLAYER_CONTROLLER.sneak;
        } else {
            if (InvWalk.invWalk) {
                if (Keyboard.isKeyDown(settings.keyBindForward.getKeyCode())) {
                    ++inpOf.moveForward;
                }

                if (Keyboard.isKeyDown(settings.keyBindBack.getKeyCode())) {
                    --inpOf.moveForward;
                }

                if (Keyboard.isKeyDown(settings.keyBindLeft.getKeyCode())) {
                    ++inpOf.moveStrafe;
                }

                if (Keyboard.isKeyDown(settings.keyBindRight.getKeyCode())) {
                    --inpOf.moveStrafe;
                }

                inpOf.jump = Keyboard.isKeyDown(settings.keyBindJump.getKeyCode());
                inpOf.sneak = Keyboard.isKeyDown(settings.keyBindSneak.getKeyCode());
            } else {
                if (settings.keyBindForward.isKeyDown()) {
                    ++inpOf.moveForward;
                }

                if (settings.keyBindBack.isKeyDown()) {
                    --inpOf.moveForward;
                }

                if (settings.keyBindLeft.isKeyDown()) {
                    ++inpOf.moveStrafe;
                }

                if (settings.keyBindRight.isKeyDown()) {
                    --inpOf.moveStrafe;
                }

                inpOf.jump = settings.keyBindJump.isKeyDown();
                inpOf.sneak = settings.keyBindSneak.isKeyDown();
            }
        }


        if (inpOf.sneak) {
            inpOf.moveStrafe = (float)((double)inpOf.moveStrafe * 0.3);
            inpOf.moveForward = (float)((double)inpOf.moveForward * 0.3);
        }

    }
}
