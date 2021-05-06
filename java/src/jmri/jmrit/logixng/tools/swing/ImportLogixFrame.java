package jmri.jmrit.logixng.tools.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.tools.ImportLogix;
import jmri.util.JmriJFrame;
import jmri.swing.JTitledSeparator;

/**
 * Imports Logixs to LogixNG
 *
 * @author Daniel Bergqvist 2019
 */
public final class ImportLogixFrame extends JmriJFrame {

//    private static final int panelWidth700 = 700;
//    private static final int panelHeight500 = 500;

    private JRadioButton _whichLogix_All;
    private JRadioButton _whichLogix_AllActive;
    private JRadioButton _whichLogix_Selected;
    private JRadioButton _whatToDo_Nothing;
    private JRadioButton _whatToDo_Disable;
    private JRadioButton _whatToDo_Delete;
    private JCheckBox _includeSystemLogixs;

    private JButton _importLogix;
    private JButton _cancelDone;

    /**
     * Construct a LogixNGEditor.
     */
    public ImportLogixFrame() {
        setTitle(Bundle.getMessage("TitleImportLogix"));
    }

    @Override
    public void initComponents() {
        super.initComponents();

        Container contentPanel = getContentPane();
//        contentPanel.setLayout(new GridLayout( 0, 1));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        add(new JTitledSeparator(Bundle.getMessage("Import_WhichLogix")));
        _whichLogix_All = new JRadioButton(Bundle.getMessage("Import_WhichLogix_All"));
        _whichLogix_AllActive = new JRadioButton(Bundle.getMessage("Import_WhichLogix_AllActive"));
        _whichLogix_AllActive.setEnabled(false);
        _whichLogix_Selected = new JRadioButton(Bundle.getMessage("Import_WhichLogix_Selected"));
        _whichLogix_Selected.setEnabled(false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(_whichLogix_All);
        buttonGroup.add(_whichLogix_AllActive);
        buttonGroup.add(_whichLogix_Selected);
        _whichLogix_All.setSelected(true);
//        r2.setSelected(true);
        contentPanel.add(_whichLogix_All);
        contentPanel.add(_whichLogix_AllActive);
        contentPanel.add(_whichLogix_Selected);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.

        add(new JTitledSeparator(Bundle.getMessage("Import_WhatToDo")));
        _whatToDo_Nothing = new JRadioButton(Bundle.getMessage("Import_WhatToDo_Nothing"));
        _whatToDo_Disable = new JRadioButton(Bundle.getMessage("Import_WhatToDo_Disable"));
        _whatToDo_Disable.setEnabled(false);
        _whatToDo_Delete = new JRadioButton(Bundle.getMessage("Import_WhatToDo_Delete"));
        _whatToDo_Delete.setEnabled(false);
        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(_whatToDo_Nothing);
        buttonGroup2.add(_whatToDo_Disable);
        buttonGroup2.add(_whatToDo_Delete);
        _whatToDo_Nothing.setSelected(true);
//        _whatToDo_Disable.setSelected(true);
        contentPanel.add(_whatToDo_Nothing);
        contentPanel.add(_whatToDo_Disable);
        contentPanel.add(_whatToDo_Delete);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.

        add(new JTitledSeparator(Bundle.getMessage("Import_IncludeSystemLogixs")));
        _includeSystemLogixs = new JCheckBox(Bundle.getMessage("Import_IncludeSystemLogixs"));
//        includeSystemLogixs.addItemListener((ItemEvent e) -> {
//            if (includeSystemLogixs.isSelected()) {
//                _systemName.setEnabled(false);
//                _sysNameLabel.setEnabled(false);
//            } else {
//                _systemName.setEnabled(true);
//                _sysNameLabel.setEnabled(true);
//            }
//        });
        add(_includeSystemLogixs);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.

        add(new JTitledSeparator(Bundle.getMessage("Import_SelectLogix")));

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.

        add(new JTitledSeparator(Bundle.getMessage("Import_WarningMessage")));

        JLabel warning = new JLabel(Bundle.getMessage("Import_WarningMessage_Long"));
        JPanel warningPanel = new JPanel();
        warningPanel.add(warning);
        contentPanel.add(warningPanel);

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.


        // set up import and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());

        // Import
        _importLogix = new JButton(Bundle.getMessage("Import_ButtonImport"));    // NOI18N
        panel5.add(_importLogix);
        _importLogix.addActionListener((ActionEvent e) -> {
            doImport();
            _cancelDone.setText(Bundle.getMessage("ButtonDone"));
//            dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        _importLogix.setToolTipText("ImportLogixButtonHint");      // NOI18N
//        panel5.setAlignmentX(LEFT_ALIGNMENT);
        // Cancel
        _cancelDone = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(_cancelDone);
        _cancelDone.addActionListener((ActionEvent e) -> {
            dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        _cancelDone.setToolTipText("CancelLogixButtonHint");      // NOI18N
//        panel5.setAlignmentX(LEFT_ALIGNMENT);
        contentPanel.add(panel5);

        // Align all components to the left
        for (Component c : contentPanel.getComponents()) {
            ((JComponent)c).setAlignmentX(LEFT_ALIGNMENT);
        }

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

//        initMinimumSize(new Dimension(panelWidth700, panelHeight500));
    }

    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }

    private void doImport() {
//        private JRadioButton _whichLogix_All;
//        private JRadioButton _whichLogix_AllActive;
//        private JRadioButton _whichLogix_Selected;
//        private JRadioButton _whatToDo_Nothing;
//        private JRadioButton _whatToDo_Disable;
//        private JRadioButton _whatToDo_Delete;
//        private JCheckBox _includeSystemLogixs;
        List<Logix> logixs = new ArrayList<>();
        if (_whichLogix_All.isSelected()) {
            for (Logix logix : InstanceManager.getDefault(LogixManager.class).getNamedBeanSet()) {
                boolean isSystemLogix =
                        "SYS".equals(logix.getSystemName())
                        || logix.getSystemName().startsWith(
                                InstanceManager.getDefault(LogixManager.class)
                                        .getSystemNamePrefix() + ":RTX");

                if (!isSystemLogix || _includeSystemLogixs.isSelected()) {
                    logixs.add(logix);
                }
            }
        } else if (_whichLogix_All.isSelected()) {
            throw new RuntimeException("Currently not supported");
        } else if (_whichLogix_All.isSelected()) {
            throw new RuntimeException("Currently not supported");
        } else {
            throw new RuntimeException("No choice selected");
        }

        boolean error = false;
        StringBuilder errorMessage = new StringBuilder("<html><table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
        errorMessage.append("<tr><th>");
        errorMessage.append("System name");
        errorMessage.append("</th><th>");
        errorMessage.append("User name");
        errorMessage.append("</th><th>");
        errorMessage.append("Error");
        errorMessage.append("</th></tr>");

        for (Logix logix : logixs) {
            ImportLogix importLogix = new ImportLogix(logix, _includeSystemLogixs.isSelected(), true);
            try {
                importLogix.doImport();
            } catch (JmriException e) {
                errorMessage.append("<tr><td>");
                errorMessage.append(logix.getSystemName());
                errorMessage.append("</td><td>");
                errorMessage.append(logix.getUserName() != null ? logix.getUserName() : "");
                errorMessage.append("</td><td>");
                errorMessage.append(e.getMessage());
                errorMessage.append("</td></tr>");
                log.error("Error thrown: {}", e, e);
                error = true;
            }
        }

        if (!error) {
            for (Logix logix : logixs) {
                ImportLogix importLogix = new ImportLogix(logix, _includeSystemLogixs.isSelected(), false);
                try {
                    importLogix.doImport();
                } catch (JmriException e) {
                    throw new RuntimeException("Unexpected error: "+e.getMessage(), e);
                }
            }
        } else {
            errorMessage.append("</table></html>");
            JOptionPane.showMessageDialog(this, errorMessage.toString(), "Error during import", JOptionPane.ERROR_MESSAGE);
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportLogixFrame.class);

}
