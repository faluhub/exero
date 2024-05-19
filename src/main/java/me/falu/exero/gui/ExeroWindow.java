package me.falu.exero.gui;

import me.falu.exero.ExeroOptions;
import me.falu.exero.core.ExeroProject;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

public class ExeroWindow extends JFrame implements IExplorerOwner, WindowStateListener {
    private final ExeroProject project;
    private final JLabel textAreaLabel;
    private final RSyntaxTextArea textArea;

    public ExeroWindow(ExeroProject project) {
        this.project = project;
        this.setTitle("Exero - " + this.project.getName());
        this.setSize(1000, 700);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());

        JPanel navbarPanel = new JPanel();
        navbarPanel.setPreferredSize(new Dimension(260, this.getHeight()));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(260, this.getHeight()));
        tabbedPane.setMinimumSize(tabbedPane.getPreferredSize());

        tabbedPane.addTab("Scripts", new ScriptsExplorerPanel(this.project, this));
        tabbedPane.addTab("Styles", new StylesExplorerPanel(this.project, this));

        navbarPanel.add(tabbedPane);
        this.add(navbarPanel, BorderLayout.WEST);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        this.textAreaLabel = new JLabel("No file opened");
        mainPanel.add(this.textAreaLabel, BorderLayout.NORTH);

        this.textArea = new RSyntaxTextArea();
        this.textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        this.textArea.setCodeFoldingEnabled(true);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        this.textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.textArea.setEditable(false);
        tabbedPane.addChangeListener(e -> {
            int i = tabbedPane.getSelectedIndex();
            this.textArea.setSyntaxEditingStyle(i == 0 ? SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT : SyntaxConstants.SYNTAX_STYLE_CSS);
        });

        RTextScrollPane scrollPane = new RTextScrollPane(this.textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public void openFile(String name, String content) {
        this.textAreaLabel.setText(name);
        this.textArea.setText(content);
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == WindowEvent.WINDOW_CLOSING) {
            ExeroOptions.save();
            this.project.save();
        }
    }
}
