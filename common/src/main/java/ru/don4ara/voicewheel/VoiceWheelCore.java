package ru.don4ara.voicewheel;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class VoiceWheelCore {

    static final String MOD_ID = "voicewheel";
    static final long MESSAGE_DURATION_MILLIS = 3000L;
    static final double RENDER_DISTANCE_SQUARED = 64.0D * 64.0D;

    private static final long FADE_DURATION_MILLIS = 500L;
    private static final float TEXT_SCALE = 0.025F;
    private static final double HEIGHT_WITH_NAME_TAG = 0.85D;
    private static final double HEIGHT_WITHOUT_NAME_TAG = 0.5D;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Map<UUID, VolumeMessage> VOLUME_MESSAGES = new HashMap<>();

    private VoiceWheelCore() {
    }

    static boolean handleScroll(double scroll) {
        Minecraft client = Minecraft.getInstance();
        if (!(client.crosshairPickEntity instanceof AbstractClientPlayer target)) {
            return false;
        }

        PlayerState state = ClientManager.getPlayerStateManager().getState(target.getUUID());
        if (state == null) {
            return false;
        }

        double previous = VoicechatClient.PLAYER_VOLUME_CONFIG.getVolume(state.getUuid());
        double step = previous >= 1.0D ? 0.1D : 0.05D;
        double volume = Mth.clamp(previous + step * scroll, 0.0D, 4.0D);
        VoicechatClient.PLAYER_VOLUME_CONFIG.setVolume(state.getUuid(), volume);
        VoicechatClient.PLAYER_VOLUME_CONFIG.save();

        int percent = (int) Math.round(100.0D * (volume - 1.0D));
        VOLUME_MESSAGES.put(target.getUUID(), new VolumeMessage(
                Component.literal((percent >= 0 ? "+" + percent : String.valueOf(percent)) + "%"),
                System.currentTimeMillis()
        ));
        return true;
    }

    static int getActiveIndicatorWidth(UUID playerId) {
        VolumeMessage message = getActiveMessage(playerId, System.currentTimeMillis());
        return message == null ? 0 : Minecraft.getInstance().font.width(message.text());
    }

    static float getActiveIndicatorNameTagLift(UUID playerId) {
        if (getActiveMessage(playerId, System.currentTimeMillis()) == null) {
            return 0.0F;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level == null
                || client.player == null
                || !(client.level.getPlayerByUUID(playerId) instanceof AbstractClientPlayer player)
                || !isNameTagVisible(player, client.player)) {
            return 0.0F;
        }
        return (float) ((HEIGHT_WITH_NAME_TAG - HEIGHT_WITHOUT_NAME_TAG) / TEXT_SCALE);
    }

    static void clearMessages() {
        VOLUME_MESSAGES.clear();
    }

    static void removeExpiredMessages(long now) {
        VOLUME_MESSAGES.entrySet().removeIf(entry ->
                now - entry.getValue().timestamp() >= MESSAGE_DURATION_MILLIS);
    }

    static VolumeMessage getMessage(UUID playerId) {
        return VOLUME_MESSAGES.get(playerId);
    }

    static float opacity(VolumeMessage message, long now) {
        long elapsed = now - message.timestamp();
        return elapsed <= MESSAGE_DURATION_MILLIS - FADE_DURATION_MILLIS
                ? 1.0F
                : (MESSAGE_DURATION_MILLIS - elapsed) / (float) FADE_DURATION_MILLIS;
    }

    static double indicatorHeight(boolean nameTagVisible) {
        return nameTagVisible ? HEIGHT_WITH_NAME_TAG : HEIGHT_WITHOUT_NAME_TAG;
    }

    static boolean isNameTagVisible(
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

    static void submitText(
            PoseStack poses,
            SubmitNodeCollector collector,
            Quaternionf cameraOrientation,
            double x,
            double y,
            double z,
            Component text,
            float opacity
    ) {
        Minecraft client = Minecraft.getInstance();
        poses.pushPose();
        poses.translate(x, y, z);
        poses.mulPose(cameraOrientation);
        poses.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        int textAlpha = Math.round(255.0F * opacity);
        int backgroundAlpha = Math.round(96.0F * opacity);
        float textX = -client.font.width(text) / 2.0F;
        collector.submitText(
                poses,
                textX,
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

    private static VolumeMessage getActiveMessage(UUID playerId, long now) {
        VolumeMessage message = VOLUME_MESSAGES.get(playerId);
        return message == null || now - message.timestamp() >= MESSAGE_DURATION_MILLIS
                ? null
                : message;
    }

    record VolumeMessage(Component text, long timestamp) {
    }
}
