package model_env;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class SimulationFrame extends JFrame {
    private Controller controller;
    private JList<String> modelList;
    private JList<String> dataList;
    private JScrollPane table;
    private DefaultTableModel tableModel;
    private final JPanel rightPanel;

    public SimulationFrame() {
        setTitle("Modelling environment");
        setSize(900, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        rightPanel = createRightPanel();
        rightPanel.setVisible(false);
        JPanel leftPanel = createLeftPanel();
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
                    controller.readDataFrom(selectedData).runModel();

                    if (!rightPanel.isVisible()) {
                        rightPanel.setVisible(true);
                        table = createTable();
                        rightPanel.add(table);
                    } else {
                        updateTable();
                    }
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
        buttonsPanel.setPreferredSize(new Dimension(getWidth() * 2 / 3 - 20, 40));

        JButton fileScriptButton = createFileScriptButton();

        JButton createdScriptButton = new JButton("Create and run ad hoc script");
        createdScriptButton.setFocusable(false);
        createdScriptButton.addActionListener(e -> {
            createScriptArea();
        });

        buttonsPanel.add(fileScriptButton);
        buttonsPanel.add(createdScriptButton);

        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);
        rightPanel.add(tablePanel, BorderLayout.NORTH);

        return rightPanel;
    }

    private JButton createFileScriptButton() {
        JButton fileScriptButton = new JButton("Run script from file");
        fileScriptButton.setFocusable(false);
        fileScriptButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                controller.runScriptFromFile(file.getName());
                updateTable();
            }
        });
        return fileScriptButton;
    }

    private String[] getColumnNames() {
        String results = controller.getResultsAsTsv();
        String[] rowLines = results.split("\n");
        return rowLines[0].split("\t");
    }

    private String[][] getTableData() {
        String results = controller.getResultsAsTsv();
        String[] rowLines = results.split("\n");
        String[] columnNames = getColumnNames();
        String[][] data = new String[rowLines.length - 1][columnNames.length];

        for (int i = 1; i < rowLines.length; i++) {
            String[] values = rowLines[i].split("\t");
            for (int j = 0; j < values.length; j++) {
                if (values[j].matches("-?\\d+(\\.\\d+)?")) {
                    values[j] = String.format("%.1f", Double.parseDouble(values[j]));
                }
            }
            data[i - 1] = values;
        }
        return data;
    }

    private void updateTable() {
        String[] columnNames = getColumnNames();
        String[][] data = getTableData();
        tableModel.setDataVector(data, columnNames);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        ((JTable) (table.getViewport().getView())).getColumnModel().getColumn(0).setCellRenderer(leftRenderer);

        table.revalidate();
        table.repaint();
    }

    private JScrollPane createTable() {
        String[] columnNames = getColumnNames();
        String[][] data = getTableData();

        tableModel = new DefaultTableModel(data, columnNames);

        JTable table = new JTable(tableModel);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        table.setDefaultRenderer(table.getColumnClass(2), rightRenderer);

        return new JScrollPane(table);
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
                controller.runScript(script);
                updateTable();
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a script before running.");
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(runButton, BorderLayout.SOUTH);
        frame.add(panel);
    }
}