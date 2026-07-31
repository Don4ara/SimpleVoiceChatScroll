package ru.don4ara.voicewheel;

import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import de.maxhenkel.voicechat.voice.common.PlayerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(VoiceWheelForge.MOD_ID)
public final class VoiceWheelForge {

    public static final String MOD_ID = "voicewheel";
    private static final long MESSAGE_DURATION_MILLIS = 3000L;
    private static final long FADE_DURATION_MILLIS = 500L;
    private static final double RENDER_DISTANCE_SQUARED = 64.0D * 64.0D;
    private static final float TEXT_SCALE = 0.025F;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final Map<UUID, VolumeMessage> VOLUME_MESSAGES = new HashMap<>();

    public VoiceWheelForge(FMLJavaModLoadingContext context) {
        InputEvent.MouseScrollingEvent.BUS.addListener(VoiceWheelForge::onMouseScroll);
        RenderPlayerEvent.Post.BUS.addListener(VoiceWheelForge::onRenderPlayer);
    }

    private static boolean onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!event.isRightDown()) {
            return false;
        }

        double scroll = event.getDeltaY() == 0.0D
                ? -event.getDeltaX()
                : event.getDeltaY();
        return handleScroll(scroll);
    }

    private static boolean handleScroll(double scroll) {
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

    private static void onRenderPlayer(RenderPlayerEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            VOLUME_MESSAGES.clear();
            return;
        }

        long now = System.currentTimeMillis();
        VOLUME_MESSAGES.entrySet().removeIf(entry ->
                now - entry.getValue().timestamp >= MESSAGE_DURATION_MILLIS);

        PlayerRenderState state = event.getState();
        if (!(level.getEntity(state.id) instanceof AbstractClientPlayer player)) {
            return;
        }

        VolumeMessage message = VOLUME_MESSAGES.get(player.getUUID());
        if (message == null || state.distanceToCameraSq > RENDER_DISTANCE_SQUARED) {
            return;
        }

        long elapsed = now - message.timestamp;
        float opacity = elapsed <= MESSAGE_DURATION_MILLIS - FADE_DURATION_MILLIS
                ? 1.0F
                : (MESSAGE_DURATION_MILLIS - elapsed) / (float) FADE_DURATION_MILLIS;

        submitText(
                event.getPoseStack(),
                event.getMultiBufferSource(),
                client.getEntityRenderDispatcher().cameraOrientation(),
                state.boundingBoxHeight,
                message.text,
                opacity
        );
    }

    private static void submitText(
            PoseStack poses,
            MultiBufferSource consumers,
            org.joml.Quaternionf cameraOrientation,
            float playerHeight,
            Component text,
            float opacity
    ) {
        Minecraft client = Minecraft.getInstance();
        poses.pushPose();
        poses.translate(0.0D, playerHeight + 0.5D, 0.0D);
        poses.mulPose(cameraOrientation);
        poses.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);

        int textAlpha = Math.round(255.0F * opacity);
        int backgroundAlpha = Math.round(96.0F * opacity);
        float x = -client.font.width(text) / 2.0F;
        client.font.drawInBatch(
                text,
                x,
                0.0F,
                textAlpha << 24 | 0xFFFFFF,
                false,
                poses.last().pose(),
                consumers,
                Font.DisplayMode.SEE_THROUGH,
                backgroundAlpha << 24,
                FULL_BRIGHT
        );
        poses.popPose();
    }

    private record VolumeMessage(Component text, long timestamp) {
    }
}
