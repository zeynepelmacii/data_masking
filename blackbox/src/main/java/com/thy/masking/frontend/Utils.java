package com.thy.masking.frontend;

import com.thy.masking.backend.DatReader;
import com.thy.masking.backend.Masker;
import com.thy.masking.backend.Metric;
import com.thy.masking.backend.MetricFactory;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils extends JFrame {

    JTextField searchFieldForPanel1, searchFieldForPanel2;
    JCheckBox selectAllForPanel1, selectAllForPanel2;
    JPanel parametersContainerForPanel1, parametersContainerForPanel2;
    JScrollPane scrollerForPanel1, scrollerForPanel2;
    JButton btnAddParameter, btnRemoveParameter, btnResetApp;
    JLabel labelInfo, selectedFileName;
    List<String> selectedParameters = new ArrayList<>();
    File selectedFile;

    List<String> getFilteredUniqueParameterNames(String keyword) {
        String keywordFinal = keyword == null ? "" : keyword.trim().toUpperCase();
        List<String> uniqueList = MetricFactory.all().stream()
                .filter(p->!p.isKeep()) //bu parametreleri maskelemiyor ve listede de gostermiyoruz
                .map(p -> p.getMetric())
                .filter(p -> p.toUpperCase().contains(keywordFinal))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        uniqueList.removeAll(selectedParameters);
        return uniqueList;
    }

    private void refillParameterCheckboxList(boolean checkUncheckAll) {
        String keyword = searchFieldForPanel1.getText();
        List<String> parameterNames = getFilteredUniqueParameterNames(keyword);
        parametersContainerForPanel1.removeAll();
        for (String metricName : parameterNames) {
            JCheckBox ch = new JCheckBox(metricName);
            if (checkUncheckAll) {
                ch.setSelected(selectAllForPanel1.isSelected());
            }
            parametersContainerForPanel1.add(ch);
        }
        parametersContainerForPanel1.updateUI();
    }

    private void refillSelectedParameterCheckboxList(boolean checkUncheckAll) {
        String keyword = searchFieldForPanel2.getText();
        List<String> finalList = selectedParameters.stream().filter(p->p.toUpperCase().contains(keyword.toUpperCase())).sorted().collect(Collectors.toList());
        parametersContainerForPanel2.removeAll();
        for (String metricName : finalList) {
            JCheckBox ch = new JCheckBox(metricName);

            if (checkUncheckAll) {
                ch.setSelected(selectAllForPanel2.isSelected());
            }
            parametersContainerForPanel2.add(ch);
        }
        parametersContainerForPanel2.updateUI();
    }

    private void refillParametersAll() {
        refillParameterCheckboxList(false);
        refillSelectedParameterCheckboxList(false);
    }

    private void selectDeselectAll() {
        refillParameterCheckboxList(true);
    }

    private void selectDeselectSelectedParametersAll() {
        refillSelectedParameterCheckboxList(true);
    }

    private List<String> getCheckedParameterNames() {
        List<String> lst = new ArrayList<>();
        for (Component ch : parametersContainerForPanel1.getComponents()) {

            if (!(ch instanceof JCheckBox)) {
                continue;
            }
            JCheckBox chBox = ((JCheckBox) ch);
            if (chBox.isSelected()) {
                lst.add(chBox.getText());
            }
        }
        return lst;
    }

    private List<String> getCheckedSelectedParameterNames() {
        List<String> lst = new ArrayList<>();
        for (Component ch : parametersContainerForPanel2.getComponents()) {
            if (!(ch instanceof JCheckBox)) {
                continue;
            }
            JCheckBox chBox = ((JCheckBox) ch);
            if (chBox.isSelected()) {
                lst.add(chBox.getText());
            }
        }
        return lst;
    }

    private void addParameter(String parameter) {
        selectedParameters.add(parameter);

    }

    private void removeParameter(String parameter) {
        selectedParameters.remove(parameter);
    }

    private void handelInfoLabel(String labelText) {
        labelInfo.setText(labelText);
    }

    private void addChangeListener(JTextField tf, Runnable event) {
        tf.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                event.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                event.run();

            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                event.run();
            }
        });
    }

    private boolean isValidFolder() {
        boolean isOk = false;
        File[] files = selectedFile.listFiles();
        for (File file : files) {
            if (file.isDirectory())
                continue;
            String fileName = file.getName();
            System.out.println(fileName);
            if (fileName.equals(DatReader.RAW_DATA_FILE_NAME)) {
                isOk = true;
                break;
            }
        }
        return isOk;
    }

    private String doMask(List<Metric> metrics) {
        String selectedFolderFullPath = selectedFile.getAbsolutePath();
        String maskedFolder = selectedFile.getParentFile().getAbsolutePath()+File.separator+"masked"+File.separator+selectedFile.getName();


        System.out.println("------------------------------------------------------");
        System.out.printf("selectedFolderFullPath       = %s%n", selectedFolderFullPath);
        System.out.printf("maskedFolder                 = %s%n", maskedFolder);
        System.out.println("------------------------------------------------------");


        Masker masker = new Masker(selectedFolderFullPath);

        long startTs = System.currentTimeMillis();
        masker.mask(metrics, maskedFolder);
        System.out.printf("Time elapsed : %d [msecs]%n", (System.currentTimeMillis() - startTs));
        return maskedFolder;
    }

    private List<Metric> getSelectedMetricsToKeep() {
        return MetricFactory.all().stream()
                .filter(p -> p.isKeep() || selectedParameters.contains(p.getMetric()))
                .collect(Collectors.toList());
    }

    public void drawParametersPanel(JPanel panelTop) {
        int width = 400;
        int height = 430;

        JPanel panel, childPanelTop, childPanelTop_top;
        JLabel label;

        panel = new JPanel();
        panel.setBorder(null);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        childPanelTop = new JPanel();
        childPanelTop.setBorder(null);
        childPanelTop.setPreferredSize(new Dimension(390, 50));
        childPanelTop.setLayout(new BoxLayout(childPanelTop, BoxLayout.Y_AXIS));

        childPanelTop_top = new JPanel();
        childPanelTop_top.setBorder(null);
        childPanelTop_top.setLayout(new FlowLayout(FlowLayout.CENTER));

        parametersContainerForPanel1 = new JPanel();
        parametersContainerForPanel1.setLayout(new BoxLayout(parametersContainerForPanel1, BoxLayout.Y_AXIS));

        label = new JLabel("Search for a parameter: ");
        label.setFont(new Font("Tahoma", Font.BOLD, 13));

        searchFieldForPanel1 = new JTextField(15);
        searchFieldForPanel1.setPreferredSize(new Dimension(15, 25));
        searchFieldForPanel1.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        selectAllForPanel1 = new JCheckBox("Select/Deselect All");
        selectAllForPanel1.setFont(new Font("Tahoma", Font.BOLD, 12));
        selectAllForPanel1.addActionListener(e -> selectDeselectAll());

        addChangeListener(searchFieldForPanel1, () -> refillParameterCheckboxList(false));

        scrollerForPanel1 = new JScrollPane(parametersContainerForPanel1);
        scrollerForPanel1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerForPanel1.setPreferredSize(new Dimension(390, 360));

        childPanelTop_top.add(label);
        childPanelTop_top.add(searchFieldForPanel1);
        childPanelTop.add(childPanelTop_top);
        childPanelTop.add(selectAllForPanel1);
        childPanelTop.add(Box.createVerticalGlue());
        panel.add(childPanelTop);
        panel.add(scrollerForPanel1);
        panelTop.add(panel);
        refillParameterCheckboxList(false);
    }

    private boolean isAnyParameterSelected(JPanel panel) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JCheckBox && ((JCheckBox) component).isSelected()) {
                return true;
            }
        }
        return false;
    }

    public void drawAddRemovePanel(JPanel panelTop) {
        int width = 80;
        int height = 430;

        JPanel panel;

        panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        panel.setPreferredSize(new Dimension(width, height));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.decode("#242B38"));

        btnAddParameter = new JButton("      >>      ");
        btnAddParameter.setFont(new java.awt.Font("Tahoma", Font.BOLD, 11));
        btnAddParameter.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAddParameter.setBackground(Color.decode("#C70A0C"));
        btnAddParameter.setForeground(Color.white);
        btnAddParameter.setPreferredSize(new Dimension(0, 25));
        btnAddParameter.setBorder(null);

        btnRemoveParameter = new JButton("      <<      ");
        btnRemoveParameter.setFont(new java.awt.Font("Tahoma", Font.BOLD, 11));
        btnRemoveParameter.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRemoveParameter.setBackground(Color.decode("#C70A0C"));
        btnRemoveParameter.setForeground(Color.white);
        btnRemoveParameter.setPreferredSize(new Dimension(0, 25));
        btnRemoveParameter.setBorder(null);

        btnResetApp = new JButton("     Reset     ");
        btnResetApp.setFont(new java.awt.Font("Tahoma", Font.BOLD, 11));
        btnResetApp.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnResetApp.setBackground(Color.decode("#C70A0C"));
        btnResetApp.setForeground(Color.white);
        btnResetApp.setPreferredSize(new Dimension(0, 25));
        btnResetApp.setBorder(null);

        btnAddParameter.addActionListener(e -> {
            if (parametersContainerForPanel1.getComponentCount() == 0) {
                handelInfoLabel("Parameter(s) list empty");
            } else if (!isAnyParameterSelected(parametersContainerForPanel1)) {
                handelInfoLabel("No selected parameter(s)");
            } else {
                for (String parameterName : getCheckedParameterNames()) {
                    addParameter(parameterName);
                }
                refillParametersAll();
                selectAllForPanel1.setSelected(false);
                selectAllForPanel2.setSelected(false);
                handelInfoLabel("Parameter(s) added");
            }

        });

        btnRemoveParameter.addActionListener(e -> {
            if (parametersContainerForPanel2.getComponentCount() == 0) {
                handelInfoLabel("Parameter(s) list empty");
            } else if (!isAnyParameterSelected(parametersContainerForPanel2)) {
                handelInfoLabel("No selected parameter(s)");
            } else {
                for (String parameterName : getCheckedSelectedParameterNames()) {
                    removeParameter(parameterName);
                }
                refillParametersAll();
                selectAllForPanel2.setSelected(false);
                selectAllForPanel1.setSelected(false);
                handelInfoLabel("Parameter(s) removed");
            }

        });

        btnResetApp.addActionListener(e -> {
          selectedParameters.clear();
            refillParametersAll();

            selectAllForPanel1.setSelected(false);
            selectAllForPanel2.setSelected(false);

            selectedFile = null;
            selectedFileName.setText("No file selected");
            handelInfoLabel("Application restared");

            searchFieldForPanel1.setText("");
            searchFieldForPanel2.setText("");

        });

        panel.add(Box.createVerticalGlue());
        panel.add(btnAddParameter);
        panel.add(new JLabel("            "));
        panel.add(btnRemoveParameter);
        panel.add(Box.createVerticalGlue());
        panel.add(btnResetApp);
        panelTop.add(panel);

    }

    public void drawSelectedParametersPanel(JPanel panelTop) {
        int width = 400;
        int height = 430;

        JPanel panel, childPanelTop, childPanelTop_top;
        JLabel label;

        panel = new JPanel();
        panel.setBorder(null);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        childPanelTop = new JPanel();
        childPanelTop.setBorder(null);
        childPanelTop.setPreferredSize(new Dimension(390, 50));
        childPanelTop.setLayout(new BoxLayout(childPanelTop, BoxLayout.Y_AXIS));

        childPanelTop_top = new JPanel();
        childPanelTop_top.setBorder(null);
        childPanelTop_top.setLayout(new FlowLayout(FlowLayout.CENTER));

        parametersContainerForPanel2 = new JPanel();
        parametersContainerForPanel2.setLayout(new BoxLayout(parametersContainerForPanel2, BoxLayout.Y_AXIS));

        label = new JLabel("Search for a parameter: ");
        label.setFont(new Font("Tahoma", Font.BOLD, 13));

        searchFieldForPanel2 = new JTextField(15);
        searchFieldForPanel2.setPreferredSize(new Dimension(15, 25));
        searchFieldForPanel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        selectAllForPanel2 = new JCheckBox("Select/Deselect All");
        selectAllForPanel2.setFont(new Font("Tahoma", Font.BOLD, 12));
        selectAllForPanel2.addActionListener(e -> selectDeselectSelectedParametersAll());

        addChangeListener(searchFieldForPanel2, () -> refillSelectedParameterCheckboxList(false));

        scrollerForPanel2 = new JScrollPane(parametersContainerForPanel2);
        scrollerForPanel2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollerForPanel2.setPreferredSize(new Dimension(390, 360));

        childPanelTop_top.add(label);
        childPanelTop_top.add(searchFieldForPanel2); // burayı düzelt
        childPanelTop.add(childPanelTop_top);
        childPanelTop.add(selectAllForPanel2);
        childPanelTop.add(Box.createVerticalGlue());
        panel.add(childPanelTop);
        panel.add(scrollerForPanel2);
        panelTop.add(panel);

        refillSelectedParameterCheckboxList(false);
    }

    public void drawInfoPanel(JPanel panelCenter) {
        int width = 880;
        int height = 20;

        JPanel panel;
        JLabel labelInfoTitle;

        panel = new JPanel();
        panel.setBorder(null);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));

        labelInfoTitle = new JLabel("Info: ");
        labelInfoTitle.setFont(new Font("Tahoma", Font.BOLD, 13));

        labelInfo = new JLabel("");
        labelInfo.setFont(new Font("Tahoma", Font.PLAIN, 13));

        panel.add(labelInfoTitle);
        panel.add(labelInfo);
        panelCenter.add(panel);
    }

    public void drawProcessPanel(JPanel panelBottom) {
        int width = 890;
        int height = 130;

        JPanel panel, panelForFileSelection;
        JButton browseFolderButton, btStartFileMasking;
        JLabel labelForSelection;

        panel = new JPanel();
        panel.setBorder(null);
        panel.setPreferredSize(new Dimension(width, height));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panelForFileSelection = new JPanel();
        panelForFileSelection.setBorder(null);
        panelForFileSelection.setPreferredSize(new Dimension(width, 20));
        panelForFileSelection.setLayout(new BoxLayout(panelForFileSelection, BoxLayout.X_AXIS));

        browseFolderButton = new JButton("     Select Folder To Mask     ");
        browseFolderButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
        browseFolderButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseFolderButton.setPreferredSize(new Dimension(0, 25));
        browseFolderButton.setBackground(Color.decode("#C70A0C"));
        browseFolderButton.setForeground(Color.white);

        labelForSelection = new JLabel("Selected File: ");
        labelForSelection.setFont(new Font("Tahoma", Font.BOLD, 12));
        labelForSelection.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectedFileName = new JLabel("No file selected");
        selectedFileName.setFont(new Font("Tahoma", Font.PLAIN, 12));
        selectedFileName.setAlignmentX(Component.CENTER_ALIGNMENT);

        btStartFileMasking = new JButton("     Start Masking     ");
        btStartFileMasking.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btStartFileMasking.setAlignmentX(Component.CENTER_ALIGNMENT);
        browseFolderButton.setPreferredSize(new Dimension(0, 25));
        btStartFileMasking.setBackground(Color.decode("#C70A0C"));
        btStartFileMasking.setForeground(Color.white);

        browseFolderButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnValue = fileChooser.showOpenDialog(null);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                selectedFileName.setText(selectedFile.getAbsolutePath());
                handelInfoLabel("File Selected");
            }
        });

        btStartFileMasking.addActionListener(e -> {
            int sizeOfSelectedParameters = selectedParameters.size();
            if (selectedFile == null) {
                handelInfoLabel("No folder is chosen. Please choose a folder. ");
            } else if (sizeOfSelectedParameters == 0) {
                handelInfoLabel("No parameter is chosen. Please choose at least a parameter. ");
            } else if (!isValidFolder()) {
                handelInfoLabel("This folder does not contain any valid data ");
            } else {
                try {
                    String maskedFile = doMask(getSelectedMetricsToKeep());
                    handelInfoLabel("Masked done");
                    try {
                        //Runtime.getRuntime().exec("explorer.exe /select," + maskedFile);
                        Runtime.getRuntime().exec(new String[] { "open", "-R", maskedFile });
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    handelInfoLabel(" Hata  olustu: " + ex.getMessage());
                }

            }
        });

        panelForFileSelection.add(labelForSelection);
        panelForFileSelection.add(selectedFileName);
        panel.add(new JLabel("                "));
        panel.add(browseFolderButton);
        panel.add(new JLabel("                "));
        panel.add(panelForFileSelection);
        panel.add(new JLabel("                "));
        panel.add(btStartFileMasking);
        panelBottom.add(panel);
    }
}
