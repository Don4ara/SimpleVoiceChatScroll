package ru.don4ara.voicewheel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = VoiceWheelCore.MOD_ID, dist = Dist.CLIENT)
public final class VoiceWheelNeoForge {

    public VoiceWheelNeoForge() {
        NeoForge.EVENT_BUS.addListener(VoiceWheelNeoForge::onMouseScroll);
        NeoForge.EVENT_BUS.addListener(VoiceWheelNeoForge::onRenderPlayer);
    }

    private static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        double scroll = event.getScrollDeltaY() == 0.0D
                ? -event.getScrollDeltaX()
                : event.getScrollDeltaY();
        if (event.isRightDown()
                && scroll != 0.0D
                && VoiceWheelCore.handleScroll(scroll)) {
            event.setCanceled(true);
        }
    }

    private static void onRenderPlayer(RenderPlayerEvent.Post<?> event) {
        Minecraft client = Minecraft.getInstance();
        AvatarRenderState state = event.getRenderState();
        if (client.level == null
                || !(client.level.getEntity(state.id)
                        instanceof AbstractClientPlayer player)
                || state.distanceToCameraSq
                        > VoiceWheelCore.RENDER_DISTANCE_SQUARED) {
            return;
        }

        long now = System.currentTimeMillis();
        VoiceWheelCore.removeExpiredMessages(now);
        VoiceWheelCore.VolumeMessage message =
                VoiceWheelCore.getMessage(player.getUUID());
        if (message == null) {
            return;
        }

        VoiceWheelCore.submitText(
                event.getPoseStack(),
                event.getSubmitNodeCollector(),
                client.gameRenderer.mainCamera().rotation(),
                0.0D,
                state.boundingBoxHeight
                        + VoiceWheelCore.indicatorHeight(
                                state.nameTag != null
                        ),
                0.0D,
                message.text(),
                VoiceWheelCore.opacity(message, now)
        );
    }
}
