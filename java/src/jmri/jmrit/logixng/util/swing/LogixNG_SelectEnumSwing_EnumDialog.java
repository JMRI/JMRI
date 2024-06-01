package jmri.jmrit.logixng.util.swing;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import jmri.util.JmriJFrame;

/**
 * A dialog that shows the available enums in LogixNG_SelectEnum.
 *
 * @param <E> the type of enum
 *
 * @author Daniel Bergqvist (C) 2024
 */
public class LogixNG_SelectEnumSwing_EnumDialog<E extends Enum<?>> {

    private final JmriJFrame _frame;
    private JTable _table;
    private TableModel<E> _tableModel;

    public LogixNG_SelectEnumSwing_EnumDialog(E[] enumArray, Runnable windowIsClosed) {
        _frame = new JmriJFrame(Bundle.getMessage("LogixNG_SelectEnumSwing_ButtonEnumDialog"));
//        _frame.addHelpMenu(
//                "package.jmri.jmrit.beantable.LogixNGTable", true);     // NOI18N
        _frame.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - _frame.getWidth() / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - _frame.getHeight() / 2);
        Container contentPane = _frame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        _table = new JTable();
        _tableModel = new TableModel<E>(enumArray);
        _table.setModel(_tableModel);
        JScrollPane scrollPane = new JScrollPane(_table);
        _frame.add(scrollPane);
        
        JPanel panel5 = new JPanel();
        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
        panel5.add(okButton);
        okButton.addActionListener((ActionEvent e) -> {
            windowIsClosed.run();
            dispose();
        });
        _frame.add(panel5);
        
        _frame.pack();
        _frame.setVisible(true);
    }
    
    public void setVisible(boolean value) {
        _frame.setVisible(value);
    }
    
    public void dispose() {
        _frame.setVisible(false);
        _frame.dispose();
    }



    public static class TableModel<E extends Enum<?>> extends AbstractTableModel {

        public static final int COLUMN_NAME = 0;
        public static final int COLUMN_DESCRIPTION = COLUMN_NAME + 1;
        
        private final E[] _enumArray;
        
        public TableModel(E[] enumArray) {
            _enumArray = enumArray;
        }
        
        /** {@inheritDoc} */
        @Override
        public int getRowCount() {
            return _enumArray.length;
        }

        /** {@inheritDoc} */
        @Override
        public int getColumnCount() {
            return 2;
        }

        /** {@inheritDoc} */
        @Override
        public String getColumnName(int col) {
            switch (col) {
                case COLUMN_NAME:
                    return Bundle.getMessage("LogixNG_SelectEnumSwing_EnumDialog_ColumnName");
                case COLUMN_DESCRIPTION:
                    return Bundle.getMessage("LogixNG_SelectEnumSwing_EnumDialog_ColumnDescription");
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= _enumArray.length) throw new IllegalArgumentException("Invalid row");

            switch (columnIndex) {
                case COLUMN_NAME:
                    return _enumArray[rowIndex].name();
                case COLUMN_DESCRIPTION:
                    return _enumArray[rowIndex].toString();
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

    }
    
}
