package me.falu.exero.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.falu.exero.Main;
import me.falu.exero.core.crawler.ExeroCrawler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Getter
public class ExeroProject {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final String name;
    private final URL domain;
    public boolean downloadedSources;
    public Map<String, String> referenceNames = new HashMap<>();
    private transient final ExeroCrawler crawler;

    public ExeroProject(String name, URL domain) {
        this.name = name;
        this.domain = domain;
        this.crawler = new ExeroCrawler(this);
    }

    public static ExeroProject fromFile(File file) {
        try {
            String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return GSON.fromJson(json, ExeroProject.class);
        } catch (IOException e) {
            LOGGER.error("Error while reading project from file", e);
        }
        return null;
    }

    public void save() {
        try {
            FileUtils.writeStringToFile(this.getProjectPath().resolve("project.json").toFile(), GSON.toJson(this), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Error while saving options to file", e);
        }
    }

    public boolean delete() {
        try {
            FileUtils.deleteDirectory(this.getProjectPath().toFile());
            LOGGER.info("Deleted project folder for '{}'.", this.name);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while deleting project folder", e);
            return false;
        }
    }

    public String getSafeName() {
        return ExeroUtils.cleanFilename(this.name);
    }

    public Path getProjectPath() {
        Path folder = Main.getProjectsFolder().resolve(this.getSafeName());
        if (folder.toFile().mkdirs()) {
            LOGGER.warn("Created project folder for '{}'.", this.name);
        }
        return folder;
    }

    @Override
    public String toString() {
        return this.name + " (" + this.domain.toString() + ")";
    }
}
