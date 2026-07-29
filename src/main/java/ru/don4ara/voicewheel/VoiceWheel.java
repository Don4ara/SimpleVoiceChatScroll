package ru.don4ara.voicewheel;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoiceWheel implements ClientModInitializer {

    private static final long MESSAGE_DURATION_MILLIS = 3000L;
    private static final long FADE_DURATION_MILLIS = 500L;
    private static final double RENDER_DISTANCE = 64.0D;
    private static final float TEXT_SCALE = 0.025F;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Map<UUID, VolumeMessage> VOLUME_MESSAGES = new HashMap<>();

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.END_MAIN.register(VoiceWheel::renderVolumeIndicators);
    }

    public static boolean handleScroll(Vector2i vector) {
        Minecraft client = Minecraft.getInstance();
        if (client.mouseHandler.isRightPressed() && client.crosshairPickEntity instanceof AbstractClientPlayer target) {
            PlayerState state = ClientManager.getPlayerStateManager().getState(target.getUUID());
            if (state != null) {
                double prevVolume = VoicechatClient.PLAYER_VOLUME_CONFIG.getVolume(state.getUuid());
                double newVolume = Mth.clamp(prevVolume + (prevVolume >= 1.0 ? 0.1 : 0.05) * (vector.y == 0 ? -vector.x : vector.y), 0.0, 4.0);
                VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(state.getUuid(), newVolume);
                VoicechatClient.PLAYER_VOLUME_CONFIG.save();
                int percent = (int) Math.round(100.0 * (newVolume - 1.0));
                VOLUME_MESSAGES.put(target.getUUID(), new VolumeMessage(
                        Component.literal((percent >= 0 ? "+" + percent : String.valueOf(percent)) + "%"),
                        System.currentTimeMillis()
                ));
                return true;
            }
        }
        return false;
    }

    private static void renderVolumeIndicators(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        AbstractClientPlayer localPlayer = client.player;
        Vec3 cameraPos = context.worldState().cameraRenderState.pos;
        if (level == null || localPlayer == null || cameraPos == null) {
            VOLUME_MESSAGES.clear();
            return;
        }

        long now = System.currentTimeMillis();
        VOLUME_MESSAGES.entrySet().removeIf(entry ->
                now - entry.getValue().timestamp >= MESSAGE_DURATION_MILLIS);
        if (VOLUME_MESSAGES.isEmpty()) {
            return;
        }

        float tickDelta = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        Vec3 localPosition = localPlayer.getPosition(tickDelta);
        double maxDistanceSquared = RENDER_DISTANCE * RENDER_DISTANCE;
        for (AbstractClientPlayer player : level.players()) {
            VolumeMessage message = VOLUME_MESSAGES.get(player.getUUID());
            if (message == null) {
                continue;
            }

            Vec3 position = player.getPosition(tickDelta);
            if (position.distanceToSqr(localPosition) > maxDistanceSquared) {
                continue;
            }

            long elapsed = now - message.timestamp;
            float opacity = elapsed <= MESSAGE_DURATION_MILLIS - FADE_DURATION_MILLIS
                    ? 1.0F
                    : (MESSAGE_DURATION_MILLIS - elapsed) / (float) FADE_DURATION_MILLIS;
            boolean nameTagVisible = isNameTagVisible(player, localPlayer);
            submitText(
                    context,
                    cameraPos,
                    position,
                    player.getBbHeight(),
                    nameTagVisible,
                    message.text,
                    opacity
            );
        }
    }

    private static boolean isNameTagVisible(
            AbstractClientPlayer player,
            AbstractClientPlayer localPlayer
    ) {
        if (player.isInvisibleTo(localPlayer) || player.getDisplayName().getString().isBlank()) {
            return false;
        }

        PlayerTeam playerTeam = player.getTeam();
        if (playerTeam == null) {
            return true;
        }

        PlayerTeam localTeam = localPlayer.getTeam();
        Team.Visibility visibility = playerTeam.getNameTagVisibility();
        return switch (visibility) {
            case ALWAYS -> true;
            case NEVER -> false;
            case HIDE_FOR_OTHER_TEAMS ->
                    localTeam == null || playerTeam.isAlliedTo(localTeam);
            case HIDE_FOR_OWN_TEAM ->
                    localTeam == null || !playerTeam.isAlliedTo(localTeam);
        };
    }

    private static void submitText(
            WorldRenderContext context,
            Vec3 cameraPos,
            Vec3 playerPosition,
            float playerHeight,
            boolean nameTagVisible,
            Component text,
            float opacity
    ) {
        Minecraft client = Minecraft.getInstance();
        PoseStack poses = context.matrices();
        poses.pushPose();
        double heightOffset = nameTagVisible ? 0.85D : 0.5D;
        poses.translate(
                playerPosition.x - cameraPos.x,
                playerPosition.y + playerHeight + heightOffset - cameraPos.y,
                playerPosition.z - cameraPos.z
        );
        poses.mulPose(context.worldState().cameraRenderState.orientation);
        poses.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        int textAlpha = Math.round(255.0F * opacity);
        int backgroundAlpha = Math.round(96.0F * opacity);
        float x = -client.font.width(text) / 2.0F;
        SubmitNodeCollector collector = context.commandQueue();
        collector.submitText(
                poses,
                x,
                0.0F,
                text.getVisualOrderText(),
                false,
                Font.DisplayMode.SEE_THROUGH,
                FULL_BRIGHT,
                textAlpha << 24 | 0xFFFFFF,
                backgroundAlpha << 24,
                0
        );
        poses.popPose();
    }

    private record VolumeMessage(Component text, long timestamp) {
    }
}
