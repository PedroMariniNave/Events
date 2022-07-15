package com.zpedroo.voltzevents.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileUtils {

    private static FileUtils instance;
    public static FileUtils get() { return instance; }

    private final Plugin plugin;
    private final Map<Files, FileManager> files = new HashMap<>(Files.values().length);

    public FileUtils(Plugin plugin) {
        instance = this;
        this.plugin = plugin;

        for (Files files : Files.values()) {
            this.files.put(files, new FileManager(files));
        }
    }

    public String getString(Files files, String path) {
        return getString(files, path, "NULL");
    }

    public String getString(Files files, String path, String defaultValue) {
        return getFile(files).get().getString(path, defaultValue);
    }

    public List<String> getStringList(Files files, String path) {
        return getFiles().get(files).get().getStringList(path);
    }

    public boolean getBoolean(Files files, String path) {
        return getFile(files).get().getBoolean(path);
    }

    public int getInt(Files files, String path) {
        return getInt(files, path, 0);
    }

    public int getInt(Files files, String path, int defaultValue) {
        return getFile(files).get().getInt(path, defaultValue);
    }

    public long getLong(Files files, String path) {
        return getLong(files, path, 0);
    }

    public long getLong(Files files, String path, long defaultValue) {
        return getFile(files).get().getLong(path, defaultValue);
    }

    public double getDouble(Files files, String path) {
        return getDouble(files, path, 0);
    }

    public double getDouble(Files files, String path, double defaultValue) {
        return getFile(files).get().getDouble(path, defaultValue);
    }

    public float getFloat(Files files, String path) {
        return getFloat(files, path, 0);
    }

    public float getFloat(Files files, String path, float defaultValue) {
        return (float) getFile(files).get().getDouble(path, defaultValue);
    }

    public Set<String> getSection(Files files, String path) {
        return getFile(files).get().getConfigurationSection(path).getKeys(false);
    }

    public FileManager getFile(Files files) {
        return getFiles().get(files);
    }

    public Map<Files, FileManager> getFiles() {
        return files;
    }

    private void copy(InputStream is, java.io.File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;

            while ((len=is.read(buf)) > 0) {
                out.write(buf,0,len);
            }

            out.close();
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum Files {
        CONFIG("config", "configuration-files", ""),
        ITEMS("items", "configuration-files", ""),
        LOCATIONS("locations", "configuration-files", ""),
        WINNERS("winners", "configuration-files", ""),
        MAIN("main", "menus", "menus"),
        SELECT_EVENT("select_event", "menus", "menus"),
        HOST_MANAGEMENT("host_management", "menus", "menus"),
        HOST_REWARDS("host_rewards", "menus", "menus"),
        EVENTS("events", "menus", "menus"),
        TOP("top", "menus", "menus"),
        FASTCRAFT("fastcraft", "events", "events"),
        FIGHT("fight", "events", "events"),
        SUMO("sumo", "events", "events"),
        MEGA_SUMO("mega_sumo", "events", "events"),
        KILLER("killer", "events", "events"),
        PARKOUR("parkour", "events", "events"),
        RACE("race", "events", "events"),
        HOT_POTATO("hot_potato", "events", "events"),
        PAINTBALL("paintball", "events", "events"),
        SPLEEF("spleef", "events", "events");

        private final String name;
        private final String resource;
        private final String folder;

        Files(String name, String resource, String folder) {
            this.name = name;
            this.resource = resource;
            this.folder = folder;
        }

        public String getName() {
            return name;
        }

        public String getResource() {
            return resource;
        }

        public String getFolder() {
            return folder;
        }
    }

    public class FileManager {

        private final java.io.File file;
        private FileConfiguration fileConfig;

        public FileManager(Files files) {
            this.file = new java.io.File(plugin.getDataFolder() + (files.getFolder().isEmpty() ? "" : "/" + files.getFolder()), files.getName() + ".yml");

            if (!this.file.exists()) {
                try {
                    this.file.getParentFile().mkdirs();
                    this.file.createNewFile();

                    copy(plugin.getResource((files.getResource().isEmpty() ? "" : files.getResource() + "/") + files.getName() + ".yml"), this.file);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file), StandardCharsets.UTF_8));
                fileConfig = YamlConfiguration.loadConfiguration(reader);
                reader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public FileConfiguration get() {
            return fileConfig;
        }

        public File getFile() {
            return file;
        }

        public void save() {
            try {
                fileConfig.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}