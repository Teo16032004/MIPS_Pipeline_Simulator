import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * MIPS Pipeline Simulator GUI
 * A polished Java Swing interface for visualizing pipeline execution
 */
public class MIPSSimulatorGUI extends JFrame {

    // Colors for the dark theme
    private static final Color BG_DARK = new Color(30, 30, 35);
    private static final Color BG_PANEL = new Color(40, 42, 50);
    private static final Color BG_INPUT = new Color(50, 52, 60);
    private static final Color ACCENT_BLUE = new Color(100, 149, 237);
    private static final Color ACCENT_GREEN = new Color(50, 205, 50);
    private static final Color ACCENT_RED = new Color(255, 99, 71);
    private static final Color ACCENT_ORANGE = new Color(255, 165, 0);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 240);
    private static final Color TEXT_SECONDARY = new Color(180, 180, 180);

    // Pipeline stage colors
    private static final Color STAGE_IF = new Color(70, 130, 180);
    private static final Color STAGE_ID = new Color(60, 179, 113);
    private static final Color STAGE_EX = new Color(255, 140, 0);
    private static final Color STAGE_MEM = new Color(186, 85, 211);
    private static final Color STAGE_WB = new Color(220, 20, 60);

    // Simulator instance
    private MIPSSimulator simulator;
    private String[] currentProgram;

    // UI Components
    private JTextArea programInput;
    private JTextArea logArea;
    private JTable registerTable;
    private JTable memoryTable;
    private JLabel[] pipelineLabels;
    private JLabel cycleLabel;
    private JLabel stallLabel;
    private JLabel statusLabel;
    private JButton loadBtn, stepBtn, runBtn, resetBtn;
    private JComboBox<String> programSelector;

    public MIPSSimulatorGUI() {
        super("MIPS Pipeline Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        // Set dark look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initComponents();
        initSimulator();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center content - split into left and right
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setBackground(BG_DARK);
        centerSplit.setDividerLocation(500);
        centerSplit.setDividerSize(8);

        // Left side - Program input and log
        JPanel leftPanel = createLeftPanel();
        centerSplit.setLeftComponent(leftPanel);

        // Right side - Pipeline, Registers, Memory
        JPanel rightPanel = createRightPanel();
        centerSplit.setRightComponent(rightPanel);

        mainPanel.add(centerSplit, BorderLayout.CENTER);

        // Control buttons at bottom
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel titleLabel = new JLabel("MIPS Pipeline Simulator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Hazard Detection & Forwarding Visualization");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(BG_PANEL);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        // Stats panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setBackground(BG_PANEL);

        cycleLabel = createStatLabel("Cycle: 0", ACCENT_BLUE);
        stallLabel = createStatLabel("Stalls: 0", ACCENT_ORANGE);
        statusLabel = createStatLabel("Ready", ACCENT_GREEN);

        statsPanel.add(cycleLabel);
        statsPanel.add(stallLabel);
        statsPanel.add(statusLabel);

        panel.add(titlePanel, BorderLayout.WEST);
        panel.add(statsPanel, BorderLayout.EAST);

        return panel;
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Consolas", Font.BOLD, 16));
        label.setForeground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        return label;
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 10));
        panel.setBackground(BG_DARK);

        // Program selector panel
        JPanel selectorPanel = new JPanel(new BorderLayout(10, 0));
        selectorPanel.setBackground(BG_PANEL);
        selectorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_BLUE, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        JLabel selectorLabel = new JLabel("Load Example:");
        selectorLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectorLabel.setForeground(TEXT_PRIMARY);

        String[] programs = { "Basic Hazards", "Branch Loop (BEQ)", "Jump Test", "Counter Loop (BGEZ)",
                "Complex Program" };
        programSelector = new JComboBox<>(programs);
        programSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        programSelector.setBackground(BG_INPUT);
        programSelector.setForeground(TEXT_PRIMARY);
        programSelector.addActionListener(e -> loadSelectedProgram());

        selectorPanel.add(selectorLabel, BorderLayout.WEST);
        selectorPanel.add(programSelector, BorderLayout.CENTER);

        // Program input panel
        JPanel inputPanel = createTitledPanel("Assembly Program", ACCENT_BLUE);
        programInput = new JTextArea();
        programInput.setFont(new Font("Consolas", Font.PLAIN, 14));
        programInput.setBackground(BG_INPUT);
        programInput.setForeground(TEXT_PRIMARY);
        programInput.setCaretColor(TEXT_PRIMARY);
        programInput.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        programInput.setText(getProgram("Basic Hazards"));

        JScrollPane inputScroll = new JScrollPane(programInput);
        inputScroll.setBorder(null);
        inputPanel.add(inputScroll, BorderLayout.CENTER);

        // Log panel
        JPanel logPanel = createTitledPanel("Execution Log", ACCENT_GREEN);
        logArea = new JTextArea();
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(BG_INPUT);
        logArea.setForeground(TEXT_SECONDARY);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);
        logPanel.add(logScroll, BorderLayout.CENTER);

        // Top panel with selector + input
        JPanel topPanel = new JPanel(new BorderLayout(0, 5));
        topPanel.setBackground(BG_DARK);
        topPanel.add(selectorPanel, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);

        panel.add(topPanel);
        panel.add(logPanel);

        return panel;
    }

    private void loadSelectedProgram() {
        String selected = (String) programSelector.getSelectedItem();
        programInput.setText(getProgram(selected));
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);

        // Pipeline visualization at top
        JPanel pipelinePanel = createPipelinePanel();
        panel.add(pipelinePanel, BorderLayout.NORTH);

        // Registers and Memory below
        JSplitPane dataSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        dataSplit.setBackground(BG_DARK);
        dataSplit.setDividerLocation(300);
        dataSplit.setDividerSize(8);

        JPanel registerPanel = createRegisterPanel();
        JPanel memoryPanel = createMemoryPanel();

        dataSplit.setTopComponent(registerPanel);
        dataSplit.setBottomComponent(memoryPanel);

        panel.add(dataSplit, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPipelinePanel() {
        JPanel panel = createTitledPanel("Pipeline Stages", ACCENT_ORANGE);
        panel.setPreferredSize(new Dimension(0, 150));

        JPanel stagesPanel = new JPanel(new GridLayout(1, 5, 15, 0));
        stagesPanel.setBackground(BG_PANEL);
        stagesPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] stageNames = { "IF (Fetch)", "ID (Decode)", "EX (Execute)", "MEM (Memory)", "WB (Writeback)" };
        Color[] stageColors = { STAGE_IF, STAGE_ID, STAGE_EX, STAGE_MEM, STAGE_WB };
        pipelineLabels = new JLabel[5];

        for (int i = 0; i < 5; i++) {
            JPanel stageBox = new JPanel(new BorderLayout());
            stageBox.setBackground(stageColors[i].darker().darker());
            stageBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(stageColors[i], 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JLabel nameLabel = new JLabel(stageNames[i], SwingConstants.CENTER);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nameLabel.setForeground(stageColors[i]);

            pipelineLabels[i] = new JLabel("empty", SwingConstants.CENTER);
            pipelineLabels[i].setFont(new Font("Consolas", Font.BOLD, 14));
            pipelineLabels[i].setForeground(TEXT_PRIMARY);

            stageBox.add(nameLabel, BorderLayout.NORTH);
            stageBox.add(pipelineLabels[i], BorderLayout.CENTER);

            stagesPanel.add(stageBox);
        }

        panel.add(stagesPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = createTitledPanel("Registers", ACCENT_BLUE);

        String[] columns = { "Reg", "Value (Dec)", "Value (Hex)" };
        Object[][] data = new Object[32][3];
        for (int i = 0; i < 32; i++) {
            data[i] = new Object[] { "$" + i, "0", "0x00000000" };
        }

        registerTable = new JTable(data, columns);
        styleTable(registerTable);

        JScrollPane scroll = new JScrollPane(registerTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_INPUT);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMemoryPanel() {
        JPanel panel = createTitledPanel("Memory", STAGE_MEM);

        String[] columns = { "Address", "Value (Dec)", "Value (Hex)" };
        Object[][] data = new Object[32][3];
        for (int i = 0; i < 32; i++) {
            data[i] = new Object[] { "0x" + String.format("%08X", i * 4), "0", "0x00000000" };
        }

        memoryTable = new JTable(data, columns);
        styleTable(memoryTable);

        JScrollPane scroll = new JScrollPane(memoryTable);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_INPUT);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Consolas", Font.PLAIN, 12));
        table.setBackground(BG_INPUT);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BG_PANEL);
        table.setRowHeight(25);
        table.setSelectionBackground(ACCENT_BLUE.darker());
        table.setSelectionForeground(TEXT_PRIMARY);

        // Make header more visible
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(60, 65, 80));
        header.setForeground(ACCENT_BLUE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(BG_DARK);

        loadBtn = createStyledButton("Load Program", ACCENT_BLUE);
        stepBtn = createStyledButton("Step", ACCENT_GREEN);
        runBtn = createStyledButton("Run All", ACCENT_ORANGE);
        resetBtn = createStyledButton("Reset", ACCENT_RED);

        loadBtn.addActionListener(e -> loadProgram());
        stepBtn.addActionListener(e -> stepSimulation());
        runBtn.addActionListener(e -> runSimulation());
        resetBtn.addActionListener(e -> resetSimulation());

        stepBtn.setEnabled(false);
        runBtn.setEnabled(false);

        panel.add(loadBtn);
        panel.add(stepBtn);
        panel.add(runBtn);
        panel.add(resetBtn);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color.darker());

        // Make button flat/matte (not shiny)
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 28, 10, 28)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(color);
                }
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(color.darker());
            }
        });

        return button;
    }

    private JPanel createTitledPanel(String title, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(color);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    private void initSimulator() {
        simulator = new MIPSSimulator();
    }

    private void loadProgram() {
        String text = programInput.getText().trim();
        if (text.isEmpty()) {
            logArea.append("Error: No program to load\n");
            return;
        }

        // Parse program
        String[] lines = text.split("\n");
        java.util.List<String> instructions = new java.util.ArrayList<>();
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("//")) {
                instructions.add(line);
            }
        }

        currentProgram = instructions.toArray(new String[0]);
        simulator = new MIPSSimulator();
        simulator.loadProgram(currentProgram);

        logArea.setText("Program loaded: " + currentProgram.length + " instructions\n");
        logArea.append("Ready to execute\n\n");

        stepBtn.setEnabled(true);
        runBtn.setEnabled(true);
        statusLabel.setText("Loaded");
        statusLabel.setForeground(ACCENT_GREEN);

        updateDisplay();
    }

    private void stepSimulation() {
        // Capture output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        // Run one cycle
        simulator.runOneCycle();

        System.out.flush();
        System.setOut(old);

        logArea.append(baos.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());

        updateDisplay();

        if (simulator.isHalted()) {
            stepBtn.setEnabled(false);
            runBtn.setEnabled(false);
            statusLabel.setText("Complete");
            statusLabel.setForeground(ACCENT_GREEN);
        }
    }

    private void runSimulation() {
        stepBtn.setEnabled(false);
        runBtn.setEnabled(false);
        statusLabel.setText("Running...");
        statusLabel.setForeground(ACCENT_ORANGE);

        // Run in background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                while (!simulator.isHalted()) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos);
                    PrintStream old = System.out;
                    System.setOut(ps);

                    simulator.runOneCycle();

                    System.out.flush();
                    System.setOut(old);

                    publish(baos.toString());

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String chunk : chunks) {
                    logArea.append(chunk);
                }
                logArea.setCaretPosition(logArea.getDocument().getLength());
                updateDisplay();
            }

            @Override
            protected void done() {
                statusLabel.setText("Complete");
                statusLabel.setForeground(ACCENT_GREEN);
                updateDisplay();
            }
        };
        worker.execute();
    }

    private void resetSimulation() {
        simulator = new MIPSSimulator();
        if (currentProgram != null) {
            simulator.loadProgram(currentProgram);
        }

        logArea.setText("Simulation reset\n");
        stepBtn.setEnabled(currentProgram != null);
        runBtn.setEnabled(currentProgram != null);
        statusLabel.setText("Ready");
        statusLabel.setForeground(ACCENT_GREEN);

        updateDisplay();
    }

    private void updateDisplay() {
        // Update cycle and stall count
        cycleLabel.setText("Cycle: " + simulator.getCycles());
        stallLabel.setText("Stalls: " + simulator.getStallCount());

        // Update pipeline stages
        String[] stages = simulator.getPipelineState();
        for (int i = 0; i < 5 && i < stages.length; i++) {
            pipelineLabels[i].setText(stages[i] == null ? "empty" : stages[i]);
        }

        // Update registers
        RegisterFile rf = simulator.getRegFile();
        for (int i = 0; i < 32; i++) {
            int val = rf.read(i);
            registerTable.setValueAt("$" + i, i, 0);
            registerTable.setValueAt(String.valueOf(val), i, 1);
            registerTable.setValueAt("0x" + String.format("%08X", val), i, 2);
        }

        // Update memory (show first 32 words)
        Memory mem = simulator.getMemory();
        for (int i = 0; i < 32; i++) {
            int addr = i * 4;
            int val = mem.load(addr);
            memoryTable.setValueAt("0x" + String.format("%08X", addr), i, 0);
            memoryTable.setValueAt(String.valueOf(val), i, 1);
            memoryTable.setValueAt("0x" + String.format("%08X", val), i, 2);
        }
    }

    private String getProgram(String name) {
        switch (name) {
            case "Basic Hazards":
                return "# Basic Hazards Demo\n" +
                        "# Data hazards with forwarding\n" +
                        "ADDI $1, $0, 10\n" +
                        "ADDI $2, $0, 20\n" +
                        "ADD $3, $1, $2\n" +
                        "SUB $4, $3, $1\n" +
                        "# Load-use hazard (stall)\n" +
                        "SW $3, 0($0)\n" +
                        "LW $5, 0($0)\n" +
                        "ADD $6, $5, $1\n";

            case "Branch Loop (BEQ)":
                return "# Branch Loop Demo\n" +
                        "# Counts from 0 to 5\n" +
                        "ADDI $1, $0, 0\n" +
                        "ADDI $2, $0, 5\n" +
                        "# Loop start (index 2)\n" +
                        "ADDI $1, $1, 1\n" +
                        "BEQ $1, $2, 1\n" +
                        "J 2\n" +
                        "# Exit\n" +
                        "ADDI $3, $0, 999\n";

            case "Jump Test":
                return "# Jump Test Demo\n" +
                        "# Tests J instruction\n" +
                        "ADDI $1, $0, 10\n" +
                        "J 3\n" +
                        "ADDI $2, $0, 20\n" +
                        "ADDI $3, $0, 30\n" +
                        "ADDI $4, $0, 40\n";

            case "Counter Loop (BGEZ)":
                return "# Counter Loop Demo\n" +
                        "# Sum 5+4+3+2+1+0 = 15\n" +
                        "ADDI $1, $0, 5\n" +
                        "ADDI $2, $0, 0\n" +
                        "# Loop (index 2)\n" +
                        "ADD $2, $2, $1\n" +
                        "ADDI $1, $1, -1\n" +
                        "BGEZ $1, -3\n" +
                        "# Exit\n" +
                        "ADDI $3, $0, 100\n";

            case "Complex Program":
                return "# Complex Program Demo\n" +
                        "# All instruction types + loops\n" +
                        "ADDI $1, $0, 3\n" +
                        "ADDI $2, $0, 0\n" +
                        "# Sum loop (index 2-4)\n" +
                        "ADD $2, $2, $1\n" +
                        "ADDI $1, $1, -1\n" +
                        "BGEZ $1, -3\n" +
                        "# Store result\n" +
                        "SW $2, 0($0)\n" +
                        "# More operations\n" +
                        "LW $3, 0($0)\n" +
                        "ADD $4, $3, $3\n" +
                        "SLL $5, $4, 2\n" +
                        "AND $6, $5, $4\n" +
                        "OR $7, $6, $3\n";

            default:
                return "# Custom Program\n";
        }
    }

    private String getSampleProgram() {
        return getProgram("Basic Hazards");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MIPSSimulatorGUI gui = new MIPSSimulatorGUI();
            gui.setVisible(true);
        });
    }
}
