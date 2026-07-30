package ru.don4ara.voicewheel;

import java.util.UUID;

/**
 * Loader-independent integration API for HUD and name-tag add-ons.
 */
public final class VoiceWheel {

    private VoiceWheel() {
    }

    /**
     * @return the active volume message width in text pixels, or {@code 0}
     */
    public static int getActiveIndicatorWidth(UUID playerId) {
        return VoiceWheelCore.getActiveIndicatorWidth(playerId);
    }

    /**
     * @return the active name-tag lift in text pixels, or {@code 0}
     */
    public static float getActiveIndicatorNameTagLift(UUID playerId) {
        return VoiceWheelCore.getActiveIndicatorNameTagLift(playerId);
    }
}
