package net.quantumaidan.itemLore.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.quantumaidan.itemLore.ItemLore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class itemLoreConfig {
    public static final itemLoreConfig DEFAULT = new itemLoreConfig();

    public static final itemLoreConfig loadConfig() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("itemLore.json");
        ItemLore.LOGGER.info("beginning itemLore config read");

        if (!Files.exists(path)) {
            itemLoreConfig temp = initConfig();
            temp.saveConfig();
            return temp;
        }
        BufferedReader br;
        try {
            br = Files.newBufferedReader(path);
            String jsonString = br.readLine();
            JsonObject json = (JsonObject) JsonParser.parseString(jsonString);
            itemLoreConfig config = new itemLoreConfig();

            config.enabled = json.get("enabled").getAsBoolean();
            config.dateTimeFormatConfig = json.get("dateTimeFormatConfig").getAsString();

            ItemLore.LOGGER.info("itemLore config successfully read!");
            return config;
        } catch (IOException e) {
            ItemLore.LOGGER.error("config failed to read");
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
            ItemLore.LOGGER.info("itemLore config read");
        } catch (IOException e) {
            ItemLore.LOGGER.error("config failed to save");
        }
    }

    private boolean enabled;
    private String dateTimeFormatConfig;

    private static itemLoreConfig initConfig(){
        return new itemLoreConfig(true, "MM/dd/yyyy hh:mm a");
    }

    public itemLoreConfig(boolean inp, String dateTimeFormatConfig) {
        this.enabled = inp;
        this.dateTimeFormatConfig = dateTimeFormatConfig;
    }

    public itemLoreConfig(){}

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
