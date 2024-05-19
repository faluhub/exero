package me.falu.exero.gui;

import me.falu.exero.core.ExeroProject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StylesExplorerPanel extends ExplorerPanel {
    public StylesExplorerPanel(ExeroProject project, IExplorerOwner parent) {
        super(project, parent);
    }

    @Override
    public List<ExplorerEntry> getValues() {
        List<ExplorerEntry> values = new ArrayList<>();
        File stylesFolder = this.project.getProjectPath().resolve("css").toFile();
        for (File file : Objects.requireNonNull(stylesFolder.listFiles())) {
            try {
                values.add(new ExplorerEntry(file.getName(), FileUtils.readFileToString(file, StandardCharsets.UTF_8), 0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }
}
