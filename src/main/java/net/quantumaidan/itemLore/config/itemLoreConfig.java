package net.quantumaidan.itemLore.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class itemLoreConfig extends MidnightConfig {
    @Entry(name = "Toggle")
    public static boolean enabled = true;

    @Entry(name = "Force Lore")
    public static boolean forceLore = false;

    @Comment(name = "Defaults if input is nonsense", centered = true)
    public static String comment1;
    @Comment(name = "Formatting Information on Modrinth", centered = true)
    public static String comment2;

    @Entry(name = "Date Time")
    public static String dateTimeFormatConfig = "MM/dd/yyyy hh:mm a";

    @Entry(name = "Time Zone")
    public static String timeZone = "CST";

    @Entry(name = "Relevant Blocks")
    public static String relevantBlocks = "Copper,Gold,Iron,Coal,Lapis,Redstone,Emerald,Diamond,Quartz,Stone,Deepslate";

    @Entry(name = "Relevant Mobs")
    public static String relevantMobs = "Zombie,Creeper,Skeleton,Spider,Enderman";
}
