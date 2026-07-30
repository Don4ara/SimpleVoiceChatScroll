package ru.don4ara.voicewheel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

public final class VoiceWheelFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(
                VoiceWheelFabric::renderVolumeIndicators
        );
    }

    public static boolean handleScroll(Vector2i vector) {
        Minecraft client = Minecraft.getInstance();
        double scroll = vector.y == 0 ? -vector.x : vector.y;
        return client.mouseHandler.isRightPressed()
                && scroll != 0.0D
                && VoiceWheelCore.handleScroll(scroll);
    }

    private static void renderVolumeIndicators(LevelRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        AbstractClientPlayer localPlayer = client.player;
        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        if (level == null || localPlayer == null || cameraPos == null) {
            VoiceWheelCore.clearMessages();
            return;
        }

        long now = System.currentTimeMillis();
        VoiceWheelCore.removeExpiredMessages(now);

        float tickDelta = client.getDeltaTracker()
                .getGameTimeDeltaPartialTick(false);
        Vec3 localPosition = localPlayer.getPosition(tickDelta);
        for (AbstractClientPlayer player : level.players()) {
            VoiceWheelCore.VolumeMessage message =
                    VoiceWheelCore.getMessage(player.getUUID());
            if (message == null) {
                continue;
            }

            Vec3 position = player.getPosition(tickDelta);
            if (position.distanceToSqr(localPosition)
                    > VoiceWheelCore.RENDER_DISTANCE_SQUARED) {
                continue;
            }

            VoiceWheelCore.submitText(
                    context.poseStack(),
                    context.submitNodeCollector(),
                    context.levelState().cameraRenderState.orientation,
                    position.x - cameraPos.x,
                    position.y + player.getBbHeight()
                            + VoiceWheelCore.indicatorHeight(
                                    VoiceWheelCore.isNameTagVisible(
                                            player,
                                            localPlayer
                                    )
                            )
                            - cameraPos.y,
                    position.z - cameraPos.z,
                    message.text(),
                    VoiceWheelCore.opacity(message, now)
            );
        }
    }
}
