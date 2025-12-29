package net.quantumaidan.itemLore.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class itemLoreConfig extends MidnightConfig {
    @Entry(name = "Toggle", category = "server")
    public static boolean enabled = true;

    @Entry(name = "Force Lore Mode", category = "server")
    public static ForceLoreMode forceLoreMode = ForceLoreMode.UNSTACKABLE;

    public enum ForceLoreMode {
        ALL, UNSTACKABLE, OFF
    }

    @Entry(name = "Date Time", category = "server")
    public static String dateTimeFormatConfig = "MM/dd/yyyy hh:mm a";

    @Entry(name = "Time Zone", category = "server")
    public static String timeZone = "CST";

    @Comment(name = "Defaults if input is nonsense", centered = true, category = "server")
    public static String comment1;
    @Comment(name = "Formatting Information on Modrinth", centered = true, category = "server")
    public static String comment2;

    @Entry(name = "Relevant Blocks", category = "server")
    public static String relevantBlocks = "Copper,Gold,Iron,Coal,Lapis,Redstone,Emerald,Diamond,Quartz,Stone,Deepslate";

    @Entry(name = "Relevant Mobs", category = "server")
    public static String relevantMobs = "Zombie,Creeper,Skeleton,Spider,Enderman";

    @Comment(name = "There is no settings on the client-side just yet", centered = true, category = "client")
    public static String comment3;
}
