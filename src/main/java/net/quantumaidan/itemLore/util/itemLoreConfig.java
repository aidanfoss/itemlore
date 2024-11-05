package net.quantumaidan.itemLore.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class itemLoreConfig {
    public static final itemLoreConfig DEFAULT = new itemLoreConfig();

    public static final itemLoreConfig loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("itemLore.json");

        if (!Files.exists(path)) {
            return new itemLoreConfig();
        }
        BufferedReader br;
        try {
            br = Files.newBufferedReader(path);
            String jsonString = br.readLine();
            JsonObject json = (JsonObject) JsonParser.parseString(jsonString);
            itemLoreConfig config = new itemLoreConfig();

            config.enabled = json.get("enabled").getAsBoolean();
            config.dateTimeFormatConfig = json.get("dateTimeFormatConfig").getAsString();

            return config;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new itemLoreConfig();
    }

    public void saveConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("itemLore.json");

        JsonObject configJson = new JsonObject();

        configJson.addProperty("enabled", this.enabled);
        configJson.addProperty("dateTimeFormatConfig", this.dateTimeFormatConfig);

        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write(configJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean enabled;
    private String dateTimeFormatConfig;

    public itemLoreConfig() {}

    public itemLoreConfig(
            boolean enabled
    )
    {
        this.enabled = true;
    }

    public boolean getEnabled(){
        return enabled;
    }
    public void setEnabled(boolean input){
        this.enabled=input;
    }

    public String getDateTimeFormatConfig(){
        return dateTimeFormatConfig;
    }

    public void setDateTimeFormatConfig(String input){
        this.dateTimeFormatConfig = input;
    }
}
