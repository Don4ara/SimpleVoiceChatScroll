package ru.don4ara.voicewheel.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MouseHandler;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.don4ara.voicewheel.VoiceWheelFabric;

@Mixin(MouseHandler.class)
abstract class MouseMixin {

    @Inject(
            method = "onScroll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"
            ),
            cancellable = true
    )
    private void handleMouseScroll(CallbackInfo ci, @Local Vector2i vector) {
        if (VoiceWheelFabric.handleScroll(vector)) {
            ci.cancel();
        }
    }

}
