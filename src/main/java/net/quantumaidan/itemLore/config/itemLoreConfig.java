package net.quantumaidan.itemLore.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class itemLoreConfig extends MidnightConfig {
    @Entry(name = "Toggle")
    public static boolean enabled = true;

    @Comment(name = "Defaults if input is nonsense", centered = true) public static String comment1;
    @Comment(name = "Formatting Information on Modrinth", centered = true) public static String comment2;

    @Entry(name = "Date Time")
    public static String dateTimeFormatConfig = "MM/dd/yyyy hh:mm a";

    @Entry(name = "Time Zone")
    public static String timeZone = "CST";
}
