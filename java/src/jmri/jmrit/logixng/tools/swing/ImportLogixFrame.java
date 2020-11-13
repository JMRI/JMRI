package jmri.jmrit.logixng.tools.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
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
        JRadioButton r1 = new JRadioButton("Import all Logixs");
        JRadioButton r2 = new JRadioButton("Import all active Logixs");
        JRadioButton r3 = new JRadioButton("Import selected Logixs");
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(r1);
        buttonGroup.add(r2);
        buttonGroup.add(r3);
        r2.setSelected(true);
        contentPanel.add(r1);
        contentPanel.add(r2);
        contentPanel.add(r3);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.
        
        add(new JTitledSeparator(Bundle.getMessage("Import_WhatToDo")));
        JRadioButton r4 = new JRadioButton("Nothing");
        JRadioButton r5 = new JRadioButton("Disable the Logixs");
        JRadioButton r6 = new JRadioButton("Delete the Logixs - Warning!");
        ButtonGroup buttonGroup2 = new ButtonGroup();
        buttonGroup2.add(r4);
        buttonGroup2.add(r5);
        buttonGroup2.add(r6);
        r5.setSelected(true);
        contentPanel.add(r4);
        contentPanel.add(r5);
        contentPanel.add(r6);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.
        
        add(new JTitledSeparator(Bundle.getMessage("Import_IncludeSystemLogixs")));
        JCheckBox includeSystemLogixs = new JCheckBox(Bundle.getMessage("Import_IncludeSystemLogixs"));
//        includeSystemLogixs.addItemListener((ItemEvent e) -> {
//            if (includeSystemLogixs.isSelected()) {
//                _systemName.setEnabled(false);
//                _sysNameLabel.setEnabled(false);
//            } else {
//                _systemName.setEnabled(true);
//                _sysNameLabel.setEnabled(true);
//            }
//        });
        add(includeSystemLogixs);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.
        
        add(new JTitledSeparator(Bundle.getMessage("Import_SelectLogix")));
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.
        
        add(new JTitledSeparator(Bundle.getMessage("Import_WarningMessage")));
        String warningMessage
                = "Warning\n"
                + "The import tool will do its best to import the requested\n"
                + "Logixs to LogixNG. But LogixNG works in a different way\n"
                + "than Logix and therefore there may be subtle differences\n"
                + "between the original Logix and the imported LogixNG.\n"
                + "\n"
                + "Also, there may be special Logixs not known to the import\n"
                + "tool that should not be imported to LogixNG, for example\n"
                + "the Logix that handles sensor groups. The import tool\n"
                + "knows about some of these Logix (SYS and RTX), but there\n"
                + "may be others not known to the import tool.";
        JTextArea warning = new JTextArea(warningMessage);
        warning.setEditable(false);
//        warning.setAlignmentX(LEFT_ALIGNMENT);
        warning.setColumns(60);
        contentPanel.add(warning);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));    // vertical space.
        
        
        // set up import and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        
        // Import
        JButton importLogix = new JButton(Bundle.getMessage("Import_ButtonImport"));    // NOI18N
        panel5.add(importLogix);
        importLogix.addActionListener((ActionEvent e) -> {
//            dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        importLogix.setToolTipText("ImportLogixButtonHint");      // NOI18N
//        panel5.setAlignmentX(LEFT_ALIGNMENT);
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            dispose();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        cancel.setToolTipText("CancelLogixButtonHint");      // NOI18N
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
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImportLogixFrame.class);

}
