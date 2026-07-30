package ru.don4ara.voicewheel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderAvatarEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(VoiceWheelCore.MOD_ID)
public final class VoiceWheelForge {

    public VoiceWheelForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEvents.register();
        }
    }

    private static final class ClientEvents {

        private ClientEvents() {
        }

        static void register() {
            InputEvent.MouseScrollingEvent.BUS.addListener(
                    ClientEvents::onMouseScroll
            );
            RenderAvatarEvent.Post.BUS.addListener(
                    ClientEvents::onRenderAvatar
            );
        }

        private static boolean onMouseScroll(
                InputEvent.MouseScrollingEvent event
        ) {
            double scroll = event.getDeltaY() == 0.0D
                    ? -event.getDeltaX()
                    : event.getDeltaY();
            return event.isRightDown()
                    && scroll != 0.0D
                    && VoiceWheelCore.handleScroll(scroll);
        }

        private static void onRenderAvatar(RenderAvatarEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            AvatarRenderState state = event.getState();
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
                    event.getNodeCollector(),
                    event.getCameraState().orientation,
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
}
