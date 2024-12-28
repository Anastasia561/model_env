package model_env;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class SimulationFrame extends JFrame {
    private Controller controller;
    private JList<String> modelList;
    private JList<String> dataList;
    private JPanel rightPanel;
    private JPanel leftPanel;

    public SimulationFrame() {
        setTitle("Modelling environment");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        rightPanel = createRightPanel();
        rightPanel.setVisible(false);
        leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(getWidth() / 3, getHeight()));
        leftPanel.setBackground(Color.LIGHT_GRAY);

        modelList = new JList<>(new String[]{"Model1", "Model2"});
        modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        dataList = new JList<>(new String[]{"data1.txt", "data2.txt"});
        dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton runModelButton = createRunModelButton();

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        listsPanel.setPreferredSize(new Dimension(getWidth() / 3 - 10, getHeight() - 100));
        listsPanel.setBackground(Color.LIGHT_GRAY);
        listsPanel.add(new JScrollPane(modelList));
        listsPanel.add(new JScrollPane(dataList));

        leftPanel.add(new JLabel("Select model and data"), BorderLayout.NORTH);
        leftPanel.add(listsPanel, BorderLayout.CENTER);
        leftPanel.add(runModelButton, BorderLayout.SOUTH);

        return leftPanel;
    }

    private JButton createRunModelButton() {
        JButton runModelButton = new JButton("Run model");
        runModelButton.setFocusable(false);
        runModelButton.addActionListener(e -> {
            String selectedModel = modelList.getSelectedValue();
            String selectedData = dataList.getSelectedValue();
            if (selectedModel != null) {
                controller = new Controller(selectedModel);
                if (selectedData != null) {
                    rightPanel.setVisible(true);
                    controller.readDataFrom(selectedData).runModel();
                } else {
                    JOptionPane.showMessageDialog(this, "Please select data to run.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a model to run.");
            }
        });
        return runModelButton;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel tablePanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setPreferredSize(new Dimension(getWidth() * 2 / 3, 100));

        JButton fileScriptButton = new JButton("Run script from file");
        fileScriptButton.setFocusable(false);
        fileScriptButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (controller != null) {
                    controller.runScriptFromFile(file.getName());
                } else {
                    JOptionPane.showMessageDialog(this, "Please select model first");
                }
            }
        });

        JButton createdScriptButton = new JButton("Create and run ad hoc script");
        createdScriptButton.setFocusable(false);
        createdScriptButton.addActionListener(e -> {
            createScriptArea();
        });

        buttonsPanel.add(fileScriptButton);
        buttonsPanel.add(createdScriptButton);
        //tablePanel.add(createTable());

        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);
        rightPanel.add(tablePanel, BorderLayout.NORTH);

        return rightPanel;
    }


    private JTable createTable() {
        String results = controller.getResultsAsTsv();
        String[] rowLines = results.split("\n");
        String[] columnNames = rowLines[0].split(" ");
        String[][] data = new String[rowLines.length][];
        //popilate table
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);

        return new JTable(tableModel);
    }

    private void createScriptArea() {
        JFrame frame = new JFrame();
        frame.setTitle("Script");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();

        JTextArea scriptTextArea = new JTextArea(10, 25);
        scriptTextArea.setFont(new Font("Arial", Font.ITALIC, 15));
        JScrollPane scrollPane = new JScrollPane(scriptTextArea);

        JButton runButton = new JButton("Ok");
        runButton.setFocusable(false);
        runButton.addActionListener(e -> {
            String script = scriptTextArea.getText();
            if (!script.isEmpty()) {
                if (controller != null) {
                    controller.runScript(script);
                } else {
                    JOptionPane.showMessageDialog(this, "Please select model first");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a script before running.");
            }
            frame.dispose();
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(runButton, BorderLayout.SOUTH);
        frame.add(panel);
    }
}
