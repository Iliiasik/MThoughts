package mt.network.packet;

public record RequestMoonPhasePacket() {

    public static void encode() {}

    public static RequestMoonPhasePacket decode() {
        return new RequestMoonPhasePacket();
    }
}
