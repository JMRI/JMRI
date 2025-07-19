package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.util.*;
import java.util.function.Predicate;

import javax.swing.*;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.logixng.LogixNG;
import jmri.util.JmriJFrame;

/**
 * Shows the inline LogixNGs.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class InlineLogixNGsFrame extends JmriJFrame {

    private final JComboBox<String> _filterOnPanel = new JComboBox<>();
    private final JComboBox<String> _filterOnIcon = new JComboBox<>();
    private final InlineLogixNGsTableModel _inlineLogixNGsTableModel =
            new InlineLogixNGsTableModel();

    /**
     * Construct a InlineLogixNGsFrame.
     */
    public InlineLogixNGsFrame() {
        setTitle(Bundle.getMessage("TitleInlineLogixNGs"));
        addHelpMenu("package.jmri.jmrit.logixng.InlineLogixNG", true);  // NOI18N
    }

    @Override
    public void initComponents() {
        super.initComponents();

        Container contentPanel = getContentPane();
//        contentPanel.setLayout(new GridLayout( 0, 1));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel panel = new JPanel();
        JPanel filterPanel = new JPanel();
        filterPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("InlineLogixNGsFrame_Filter")));
        filterPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JLabel filterOnPanelLabel = new JLabel(Bundle.getMessage("InlineLogixNGsFrame_Filter_Panel"));
        filterPanel.add(filterOnPanelLabel, c);
        filterOnPanelLabel.setLabelFor(_filterOnPanel);
        c.gridy = 2;
        JLabel filterOnIconLabel = new JLabel(Bundle.getMessage("InlineLogixNGsFrame_Filter_IconType"));
        filterPanel.add(filterOnIconLabel, c);
        filterOnIconLabel.setLabelFor(_filterOnIcon);
        c.gridx = 1;
        filterPanel.add(Box.createHorizontalStrut(3), c);
        c.gridx = 2;
        c.gridy = 1;
        filterPanel.add(Box.createVerticalStrut(3), c);
        c.gridx = 2;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        filterPanel.add(_filterOnPanel, c);
        c.gridy = 2;
        filterPanel.add(_filterOnIcon, c);
        panel.add(filterPanel);
        contentPanel.add(panel);


        JTable table = new JTable();
        _inlineLogixNGsTableModel.init();
        table.setModel(_inlineLogixNGsTableModel);
        TableColumnModel columnModel = table.getColumnModel();
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JLabel positionColumn = new JLabel("8888888");
        int positionColumnWidth = positionColumn.getPreferredSize().width;
        columnModel.getColumn(InlineLogixNGsTableModel.COLUMN_POS_X)
                .setMaxWidth(positionColumnWidth);
        columnModel.getColumn(InlineLogixNGsTableModel.COLUMN_POS_Y)
                .setMaxWidth(positionColumnWidth);
        table.setDefaultRenderer(InlineLogixNGsTableModel.Menu.class,
                new InlineLogixNGsTableModel.MenuCellRenderer());
        table.setDefaultEditor(InlineLogixNGsTableModel.Menu.class,
                new InlineLogixNGsTableModel.MenuCellEditor(table, _inlineLogixNGsTableModel));
        _inlineLogixNGsTableModel.setColumnForMenu(table);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(600, 400));
        contentPanel.add(scrollpane);

        Set<String> panels = new HashSet<>();
        Set<String> icons = new HashSet<>();
        for (LogixNG logixNG : _inlineLogixNGsTableModel.getLogixNGList()) {
            String editorName = InlineLogixNGsTableModel.getEditorName(logixNG.getInlineLogixNG());
            String typeName = InlineLogixNGsTableModel.getTypeName(logixNG.getInlineLogixNG());
            if (editorName != null && !editorName.isBlank()) panels.add(editorName);
            if (typeName != null && !typeName.isBlank()) icons.add(typeName);
        }

        _filterOnPanel.addItem("");
        for (String s : panels) _filterOnPanel.addItem(s);
        _filterOnPanel.addActionListener((evt)->{updateFilters();});

        _filterOnIcon.addItem("");
        for (String s : icons) _filterOnIcon.addItem(s);
        _filterOnIcon.addActionListener((evt)->{updateFilters();});

        pack();
    }

    private void updateFilters() {
        String filterOnPanelValue = _filterOnPanel.getItemAt(_filterOnPanel.getSelectedIndex());
        String filterOnIconValue = _filterOnIcon.getItemAt(_filterOnIcon.getSelectedIndex());

        Predicate<LogixNG> filter = (LogixNG logixNG) -> {
            String editorName = logixNG.getInlineLogixNG() != null
                    ? logixNG.getInlineLogixNG().getEditorName() : "";
            String typeName = logixNG.getInlineLogixNG() != null
                    ? logixNG.getInlineLogixNG().getTypeName() : "";
            if (!filterOnPanelValue.equals("")) {
                if (editorName == null || !filterOnPanelValue.equals(editorName)) {
                    return false;
                }
            }
            if (!filterOnIconValue.equals("")) {
                if (typeName == null || !filterOnIconValue.equals(typeName)) {
                    return false;
                }
            }
            return true;
        };

        _inlineLogixNGsTableModel.setFilter(filter);
    }

}
