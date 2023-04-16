package net.momirealms.customcrops.api.object.pot;

import net.momirealms.customcrops.CustomCrops;
import net.momirealms.customcrops.api.object.Function;
import net.momirealms.customcrops.api.object.fertilizer.FertilizerType;
import net.momirealms.customcrops.api.object.fill.PassiveFillMethod;
import net.momirealms.customcrops.api.util.AdventureUtils;
import net.momirealms.customcrops.api.util.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;

public class PotManager extends Function {

    private final CustomCrops plugin;
    private final HashMap<String, PotConfig> potConfigMap;
    private final HashMap<String, String> blockToPotKey;

    public PotManager(CustomCrops plugin) {
        this.plugin = plugin;
        this.potConfigMap = new HashMap<>();
        this.blockToPotKey = new HashMap<>();
    }

    @Override
    public void load() {
        loadConfig();
    }

    @Override
    public void unload() {
        this.potConfigMap.clear();
        this.blockToPotKey.clear();
    }

    private void loadConfig() {
        File pot_folder = new File(plugin.getDataFolder(), "contents" + File.separator + "pots");
        if (!pot_folder.exists()) {
            if (!pot_folder.mkdirs()) return;
            plugin.saveResource("contents" + File.separator + "pots" + File.separator + "default.yml", false);
        }
        File[] files = pot_folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) continue;
                boolean enableFertilized = section.getBoolean("fertilized-pots.enable", false);
                String base_dry = section.getString("base.dry");
                String base_wet = section.getString("base.wet");
                if (base_wet == null || base_dry == null) {
                    AdventureUtils.consoleMessage("<red>[CustomCrops] base.dry/base.wet is not correctly set for pot: " + key);
                    continue;
                }
                PassiveFillMethod[] methods = ConfigUtils.getPassiveFillMethods(section.getConfigurationSection("fill-method"));
                if (methods == null) {
                    AdventureUtils.consoleMessage("<red>[CustomCrops] fill method is not set for pot: " + key);
                    continue;
                }
                blockToPotKey.put(base_wet, key);
                blockToPotKey.put(base_dry, key);
                PotConfig potConfig = new PotConfig(
                        section.getInt("max-water-storage"),
                        base_dry,
                        base_wet,
                        enableFertilized,
                        methods
                );
                if (enableFertilized) {
                    ConfigurationSection fertilizedSec = section.getConfigurationSection("fertilized-pots");
                    if (fertilizedSec == null) continue;
                    for (String type : fertilizedSec.getKeys(false)) {
                        if (type.equals("enable")) continue;
                        String dry = fertilizedSec.getString(type + ".dry");
                        String wet = fertilizedSec.getString(type + ".wet");
                        blockToPotKey.put(dry, key);
                        blockToPotKey.put(wet, key);
                        switch (type) {
                            case "quality" -> potConfig.registerFertilizedPot(FertilizerType.QUALITY, dry, wet);
                            case "yield-increase" -> potConfig.registerFertilizedPot(FertilizerType.YIELD_INCREASE, dry, wet);
                            case "variation" -> potConfig.registerFertilizedPot(FertilizerType.VARIATION, dry, wet);
                            case "soil-retain" -> potConfig.registerFertilizedPot(FertilizerType.SOIL_RETAIN, dry, wet);
                            case "speed-grow" -> potConfig.registerFertilizedPot(FertilizerType.SPEED_GROW, dry, wet);
                        }
                    }
                }
                potConfigMap.put(key, potConfig);
            }
        }
        AdventureUtils.consoleMessage("[CustomCrops] Loaded <green>" + potConfigMap.size() + " <gray>pot(s)");
    }

    public boolean containsPotBlock(String id) {
        return blockToPotKey.containsKey(id);
    }

    @Nullable
    public PotConfig getPotConfig(String key) {
        return potConfigMap.get(key);
    }

    @Nullable
    public String getPotKeyByBlockID(String id) {
        return blockToPotKey.get(id);
    }

    @Nullable
    public PotConfig getPotConfigByBlockID(String id) {
        String key = blockToPotKey.get(id);
        if (key == null) return null;
        return potConfigMap.get(key);
    }
}