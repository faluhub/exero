package me.falu.exero;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.falu.exero.core.ExeroProject;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ExeroOptions {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path OPTIONS_PATH = Main.DATA_FOLDER.resolve("options.json");
    private static ExeroOptions INSTANCE;

    public ExeroProject lastOpened;

    private ExeroOptions() {}

    public static ExeroOptions getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("Attempted to get options instance before its initialization was completed.");
        }
        return INSTANCE;
    }

    public static void load() {
        File file = OPTIONS_PATH.toFile();
        if (file.exists()) {
            try {
                String json = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                INSTANCE = GSON.fromJson(json, ExeroOptions.class);
                LOGGER.info("Successfully loaded config.");
                return;
            } catch (IOException e) {
                LOGGER.error("Error while reading options from file", e);
            }
        }
        INSTANCE = new ExeroOptions();
    }

    public static void save() {
        try {
            String json = GSON.toJson(getInstance());
            FileUtils.writeStringToFile(OPTIONS_PATH.toFile(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Error while saving options to file", e);
        }
    }
}
