package me.falu.exero.gui;

import me.falu.exero.core.ExeroProject;
import me.falu.exero.core.ScriptFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ScriptsExplorerPanel extends ExplorerPanel {
    public ScriptsExplorerPanel(ExeroProject project, IExplorerOwner parent) {
        super(project, parent);
    }

    @Override
    public List<ExplorerEntry> getValues() {
        List<ExplorerEntry> values = new ArrayList<>();
        File scriptsFolder = this.project.getProjectPath().resolve("js").toFile();
        for (File file : Objects.requireNonNull(scriptsFolder.listFiles())) {
            try {
                values.add(new ExplorerEntry(file.getName(), FileUtils.readFileToString(file, StandardCharsets.UTF_8), 0));
                ScriptFile scriptFile = new ScriptFile(file);
                for (Map.Entry<String, String> entry : scriptFile.getFunctions().entrySet()) {
                    values.add(new ExplorerEntry(entry.getKey(), entry.getValue(), 1));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return values;
    }
}
