import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

 class TimetableGenerator {
    private JFrame frame;
    private JTable timetableTable;
    private DefaultTableModel tableModel;

    private JTextField subjectField;
    private JButton generateButton;

    private static final String[] DAYS_OF_WEEK = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN (Holiday)"};
    private static final int START_HOUR = 9;
    private static final int END_HOUR = 17;

    public TimetableGenerator() {
        frame = new JFrame("Timetable Generator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(1, 2));

        subjectField = new JTextField(20);

        inputPanel.add(new JLabel("Enter the number of subjects: "));
        inputPanel.add(subjectField);

        generateButton = new JButton("Generate Timetable");
        generateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generateTimetable();
            }
        });

        tableModel = new DefaultTableModel();
        timetableTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(timetableTable);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(generateButton, BorderLayout.SOUTH);
        frame.add(tableScrollPane, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public static Map<String, Map<String, String>> generateTimetable(int numSubjects, String[] subjectNames) {
        if (numSubjects == 0) {
            JOptionPane.showMessageDialog(null, "Number of subjects must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Random random = new Random();
        Map<String, Map<String, String>> timetable = new HashMap<>();

        for (String dayOfWeek : DAYS_OF_WEEK) {
            if (!dayOfWeek.endsWith("(Holiday)")) {
                Map<String, String> classTimetable = new HashMap<>();
                List<String> availableSubjects = new ArrayList<>(Arrays.asList(subjectNames));

                int currentHour = START_HOUR;
                for (int i = 1; i <= numSubjects; i++) {
                    String timeslot = String.format("%02d:00 AM - %02d:00 AM", currentHour, currentHour + 1);
                    int subjectIndex = random.nextInt(availableSubjects.size());
                    String subject = availableSubjects.remove(subjectIndex);
                    classTimetable.put(timeslot, subject);
                    currentHour++;
                }

                timetable.put(dayOfWeek, classTimetable);
            } else {
                timetable.put(dayOfWeek, Collections.singletonMap("Holiday", "Holiday"));
            }
        }

        return timetable;
    }

    public void generateTimetable() {
        tableModel.setColumnCount(0);

        int numSubjects = Integer.parseInt(subjectField.getText());

        String[] subjectNames = new String[numSubjects];

        for (int i = 0; i < numSubjects; i++) {
            subjectNames[i] = JOptionPane.showInputDialog("Enter the name of subject " + (i + 1) + ": ");
        }

        tableModel.addColumn("Timeslots");  // Add the "Timeslots" heading
        for (String dayOfWeek : DAYS_OF_WEEK) {
            tableModel.addColumn(dayOfWeek);
        }

        Map<String, Map<String, String>> timetable = generateTimetable(numSubjects, subjectNames);

        if (timetable == null) {
            return; // If an error occurred, exit the method
        }

        int currentHour = START_HOUR;
        for (int i = 1; i <= numSubjects; i++) {
            String timeslot = String.format("%02d:00 AM - %02d:00 AM", currentHour, currentHour + 1);
            Vector<String> rowData = new Vector<>();
            rowData.add(timeslot);
            for (String dayOfWeek : DAYS_OF_WEEK) {
                rowData.add("");
            }
            tableModel.addRow(rowData);
            currentHour++;
        }

        for (String timeslot : timetable.get(DAYS_OF_WEEK[0]).keySet()) {
            int rowIndex = getRowIndexForTimeslot(timeslot);
            for (String dayOfWeek : DAYS_OF_WEEK) {
                if (!dayOfWeek.endsWith("(Holiday)")) {
                    int columnIndex = getColumnIndexForDayOfWeek(dayOfWeek);
                    String subject = timetable.get(dayOfWeek).get(timeslot);
                    tableModel.setValueAt(subject, rowIndex, columnIndex);
                }
            }
        }

        // Automatically save the timetable to a file
        saveTimetableToFile(subjectNames, timetable.get(DAYS_OF_WEEK[0]).keySet().toArray(new String[0]), DAYS_OF_WEEK);
    }

    public int getRowIndexForTimeslot(String timeslot) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(timeslot)) {
                return i;
            }
        }
        return -1;
    }

    public int getColumnIndexForDayOfWeek(String dayOfWeek) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (tableModel.getColumnName(i).equals(dayOfWeek)) {
                return i;
            }
        }
        return -1;
    }

    public void saveTimetableToFile(String[] subjectNames, String[] timeslots, String[] daysOfWeek) {
        try {
            String filePath = "timetable.txt";  // Specify the file path where you want to save the timetable
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

            // Write the column headings
            writer.write("Timeslots\t");
            for (String dayOfWeek : daysOfWeek) {
                writer.write(dayOfWeek + "\t");
            }
            writer.write("\n");

            // Write the timetable data
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                writer.write(timeslots[row] + "\t");
                for (int col = 1; col < tableModel.getColumnCount(); col++) {
                    writer.write(tableModel.getValueAt(row, col) + "\t");
                }
                writer.write("\n");
            }

            writer.close();
            System.out.println("Timetable saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error saving the timetable.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TimetableGenerator();
            }
        });
    }
}