package net.quantumaidan.itemLore.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class itemLoreConfig extends MidnightConfig {
    @Entry(name = "Toggle")
    public static boolean enabled = true;

    @Comment
    public static String comment1 = "DO NOT TOUCH BELOW IF YOU DON'T KNOW WHAT YOU ARE DOING";

    @Entry(name = "DateTimeFormat")
    public static String dateTimeFormatConfig = "MM/dd/yyyy hh:mm a";

    @Entry
    public static String timeZone = "CST";
}
