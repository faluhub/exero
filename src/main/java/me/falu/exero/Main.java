package me.falu.exero;

import com.formdev.flatlaf.FlatDarkLaf;
import me.falu.exero.gui.ProjectManagerWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.nio.file.Path;

public class Main {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final boolean IS_DEV = Main.class.getPackage().getImplementationVersion() == null;
    public static final String VERSION = IS_DEV ? "Development" : Main.class.getPackage().getImplementationVersion();
    public static final Path DATA_FOLDER = Path.of(System.getenv("APPDATA")).resolve("Exero");

    public static void main(String[] args) {
        LOGGER.info("Launching Exero {}", VERSION);
        if (DATA_FOLDER.toFile().mkdirs()) {
            LOGGER.info("Created data directory.");
        }
        ExeroOptions.load();
        UIManager.put("Component.focusWidth", 2);
        FlatDarkLaf.setup();
        ProjectManagerWindow app = new ProjectManagerWindow();
        app.setVisible(true);
    }

    public static Path getProjectsFolder() {
        Path folder = DATA_FOLDER.resolve("projects");
        if (folder.toFile().mkdirs()) {
            LOGGER.info("Created projects folder.");
        }
        return folder;
    }
}
