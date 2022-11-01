package jmri.jmrit.logixng.tools.swing;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.table.TableColumnModel;

import jmri.util.JmriJFrame;

/**
 * Shows the inline LogixNGs.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class InlineLogixNGsFrame extends JmriJFrame {

    /**
     * Construct a InlineLogixNGsFrame.
     */
    public InlineLogixNGsFrame() {
        setTitle(Bundle.getMessage("TitleInlineLogixNGs"));
    }

    @Override
    public void initComponents() {
        super.initComponents();

        Container contentPanel = getContentPane();
//        contentPanel.setLayout(new GridLayout( 0, 1));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JTable table = new JTable();
        InlineLogixNGsTableModel inlineLogixNGsTableModel =
                new InlineLogixNGsTableModel();
        table.setModel(inlineLogixNGsTableModel);
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
                new InlineLogixNGsTableModel.MenuCellEditor(table, inlineLogixNGsTableModel));
        inlineLogixNGsTableModel.setColumnForMenu(table);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollpane = new JScrollPane(table);
        scrollpane.setPreferredSize(new Dimension(600, 400));
        contentPanel.add(scrollpane);

        pack();
    }

}
