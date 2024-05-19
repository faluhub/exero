package me.falu.exero.gui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.falu.exero.core.ExeroProject;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class ExplorerPanel extends JPanel {
    protected final ExeroProject project;

    public ExplorerPanel(ExeroProject project, IExplorerOwner parent) {
        this.project = project;
        this.setLayout(new BorderLayout());
        DefaultListModel<ExplorerEntry> filesModel = new DefaultListModel<>();
        JList<ExplorerEntry> files = new JList<>(filesModel);
        List<ExplorerEntry> values = this.getValues();
        values.forEach(filesModel::addElement);
        this.add(new JScrollPane(files), BorderLayout.CENTER);
        files.addListSelectionListener(event -> {
            ExplorerEntry entry = files.getSelectedValue();
            parent.openFile(entry.getName(), entry.getContent());
        });
    }

    public abstract List<ExplorerPanel.ExplorerEntry> getValues();

    @Getter
    @RequiredArgsConstructor
    public static class ExplorerEntry {
        private final String name;
        private final String content;
        private final int indent;

        @Override
        public String toString() {
            return "    ".repeat(Math.max(0, this.indent)) + this.name;
        }
    }
}
