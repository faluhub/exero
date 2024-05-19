package me.falu.exero.gui;

import me.falu.exero.ExeroOptions;
import me.falu.exero.Main;
import me.falu.exero.core.ExeroProject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

public class ProjectManagerWindow extends JFrame implements WindowStateListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final JList<ExeroProject> projectList;
    private final DefaultListModel<ExeroProject> projectListModel;

    public ProjectManagerWindow() {
        this.setTitle("Exero Project Manager");
        this.setSize(650, 400);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - this.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - this.getHeight()) / 2);
        this.setLocation(x, y);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.projectListModel = new DefaultListModel<>();
        this.projectList = new JList<>(this.projectListModel);
        JButton newProjectButton = new JButton("New Project");
        JButton openProjectButton = new JButton("Open Project");
        JButton deleteProjectButton = new JButton("Delete Project");
        this.loadProjects();

        this.projectList.addListSelectionListener(e -> {
            boolean v = this.projectList.getSelectedIndex() >= 0;
            openProjectButton.setEnabled(v);
            deleteProjectButton.setEnabled(v);
        });

        openProjectButton.setEnabled(false);
        deleteProjectButton.setEnabled(false);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(new JScrollPane(this.projectList), BorderLayout.CENTER);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 5;
        gbc.ipady = 5;
        gbc.gridx = 0;
        rightPanel.add(newProjectButton, gbc);
        rightPanel.add(openProjectButton, gbc);
        rightPanel.add(deleteProjectButton, gbc);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.add(leftPanel, new GridBagConstraints(0, 0, 1, 1, 1, 1, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
        mainPanel.add(rightPanel, new GridBagConstraints(1, 0, 1, 2, 0, 0, 11, 0, new Insets(0, 0, 0, 0), 0, 0));

        this.getContentPane().add(mainPanel, BorderLayout.CENTER);

        newProjectButton.addActionListener(e -> this.showNewProjectDialog());
        openProjectButton.addActionListener(e -> this.openSelectedProject());
        deleteProjectButton.addActionListener(e -> this.deleteSelectedProject());
    }

    @SuppressWarnings("deprecation")
    private void showNewProjectDialog() {
        JTextField projectNameField = new JTextField(20);
        projectNameField.addAncestorListener(new RequestFocusListener());
        JTextField domainNameField = new JTextField(20);

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Project Name:"));
        panel.add(projectNameField);
        panel.add(new JLabel("Domain Name: (https://example.com)"));
        panel.add(domainNameField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Create New Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String projectName = projectNameField.getText().trim();
            String domainName = domainNameField.getText().trim();
            String domainRegex = "^(https://)([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
            boolean isValidDomain = Pattern.matches(domainRegex, domainName);

            if (!projectName.isEmpty() && isValidDomain) {
                try {
                    ExeroProject project = new ExeroProject(projectName, new URL(domainName));
                    project.save();
                    this.projectListModel.addElement(project);
                    this.projectList.setSelectedValue(project, true);
                    this.openSelectedProject();
                    return;
                } catch (MalformedURLException ignored) {
                }
            }
            JOptionPane.showMessageDialog(this, "Invalid project details. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSelectedProject() {
        ExeroProject project = this.projectList.getSelectedValue();
        if (project != null) {
            this.setVisible(false);
            this.dispose();
            ExeroOptions.getInstance().lastOpened = project;
            if (!project.downloadedSources) {
                LoadingScreenWindow loadingScreen = new LoadingScreenWindow("Downloading sources, this may take a moment...");
                new Thread(() -> {
                    project.getCrawler().downloadSources(loadingScreen);
                    project.downloadedSources = true;
                    project.save();
                    loadingScreen.setVisible(false);
                    loadingScreen.dispose();
                    SwingUtilities.invokeLater(() -> {
                        ExeroWindow window = new ExeroWindow(project);
                        window.setVisible(true);
                    });
                }).start();
            } else {
                ExeroWindow window = new ExeroWindow(project);
                window.setVisible(true);
            }
        }
    }

    private void deleteSelectedProject() {
        ExeroProject project = this.projectList.getSelectedValue();
        if (project != null) {
            String message = "Are you sure you want to delete '" + project.getName() + "'?";
            int option = JOptionPane.showConfirmDialog(this, message, "Delete Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                if (project.delete()) {
                    this.projectListModel.remove(this.projectList.getSelectedIndex());
                    this.projectList.setSelectedIndex(-1);
                    LOGGER.info("Deleted project '{}'.", project.getName());
                } else {
                    JOptionPane.showMessageDialog(this, "Couldn't delete project. Try again later.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }
            LOGGER.info("Cancelled deleting project '{}'.", project.getName());
        }
    }

    private void loadProjects() {
        if (Main.getProjectsFolder().toFile().mkdirs()) {
            LOGGER.info("Created projects folder.");
            return;
        }
        for (File file : Objects.requireNonNull(Main.getProjectsFolder().toFile().listFiles())) {
            if (file.isDirectory()) {
                File projectFile = file.toPath().resolve("project.json").toFile();
                ExeroProject project = ExeroProject.fromFile(projectFile);
                if (project != null && this.projectListModel != null) {
                    LOGGER.info("Loaded project '{}'.", project.getName());
                    this.projectListModel.addElement(project);
                }
            }
        }
    }

    @Override
    public void windowStateChanged(WindowEvent e) {
        if (e.getNewState() == WindowEvent.WINDOW_CLOSING) {
            ExeroOptions.save();
        }
    }
}
