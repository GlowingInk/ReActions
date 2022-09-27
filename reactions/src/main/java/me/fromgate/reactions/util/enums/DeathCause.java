package me.fromgate.reactions.util.enums;

public enum DeathCause {
    PVP,
    PVE,
    OTHER,
    ANY;

    public static DeathCause getByName(String name) {
        for (DeathCause d : DeathCause.values())
            if (d.name().equalsIgnoreCase(name)) return d;
        return ANY;
    }
}
