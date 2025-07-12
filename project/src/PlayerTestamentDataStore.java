package com.fallengod.testament.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Handles saving/loading all player testament progress to a YAML file
 */
public class PlayerTestamentDataStore {
    private final Map<UUID, PlayerTestamentData> playerData = new HashMap<>();
    private final File dataFile;
    private final Yaml yaml;

    public PlayerTestamentDataStore(JavaPlugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "progress.yml");
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
    }

    public PlayerTestamentData get(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerTestamentData());
    }

    public void set(UUID uuid, PlayerTestamentData data) {
        playerData.put(uuid, data);
    }

    public void save() throws IOException {
        // Ensure data folder exists
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
        
        Map<String, Object> saveMap = new HashMap<>();
        for (Map.Entry<UUID, PlayerTestamentData> entry : playerData.entrySet()) {
            Map<String, Object> playerMap = new HashMap<>();
            // Save fragments as godType -> list of fragment numbers
            Map<String, Object> fragmentsOut = new HashMap<>();
            for (Map.Entry<String, Set<Integer>> fragEntry : entry.getValue().getFragmentsMap().entrySet()) {
                fragmentsOut.put(fragEntry.getKey(), new java.util.ArrayList<>(fragEntry.getValue()));
            }
            playerMap.put("fragments", fragmentsOut);
            playerMap.put("completed", new HashSet<>(entry.getValue().getCompletedTestaments()));
            saveMap.put(entry.getKey().toString(), playerMap);
        }
        try (FileWriter writer = new FileWriter(dataFile)) {
            yaml.dump(saveMap, writer);
        }
    }

    @SuppressWarnings("unchecked")
    public void load() throws IOException {
        playerData.clear();
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            Object obj = yaml.load(reader);
            if (!(obj instanceof Map)) return;
            Map<?, ?> saveMap = (Map<?, ?>) obj;
            for (Map.Entry<?, ?> entry : saveMap.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey().toString());
                Map<?, ?> playerMap = (Map<?, ?>) entry.getValue();
                PlayerTestamentData data = new PlayerTestamentData();
                if (playerMap.containsKey("fragments")) {
                    Map<?, ?> fragments = (Map<?, ?>) playerMap.get("fragments");
                    for (Map.Entry<?, ?> frag : fragments.entrySet()) {
                        String godType = frag.getKey().toString();
                        Set<Integer> fragmentNumbers = new HashSet<>();
                        Object value = frag.getValue();
                        if (value instanceof Iterable<?>) {
                            for (Object num : (Iterable<?>) value) {
                                fragmentNumbers.add(Integer.parseInt(num.toString()));
                            }
                        }
                        data.setFragmentsFound(godType, fragmentNumbers);
                    }
                }
                if (playerMap.containsKey("completed")) {
                    Set<?> completed = (Set<?>) playerMap.get("completed");
                    for (Object god : completed) {
                        data.completeTestament(god.toString());
                    }
                }
                playerData.put(uuid, data);
            }
        }
    }
}