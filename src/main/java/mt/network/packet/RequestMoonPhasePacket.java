package mt.network.packet;

import net.minecraft.util.Identifier;

public record RequestMoonPhasePacket() {
    public static final Identifier ID = new Identifier("midnightthoughts", "request_moon_phase");

    public static RequestMoonPhasePacket decode() {
        return new RequestMoonPhasePacket();
    }
}