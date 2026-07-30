package ru.don4ara.voicewheel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.don4ara.voicewheel.VoiceWheelFabric;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {

    @Inject(method = "submitEntities", at = @At("TAIL"))
    private void renderVolumeIndicators(
            PoseStack poses,
            LevelRenderState levelRenderState,
            SubmitNodeCollector collector,
            CallbackInfo ci
    ) {
        VoiceWheelFabric.renderVolumeIndicators(poses, levelRenderState, collector);
    }
}
