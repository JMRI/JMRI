package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_Thread;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 *
 * @author Daniel Bergqvist 2020
 */
public class EditThreadsDialog {
    
    private final ConditionalNG _conditionalNG;
    private JDialog _editThreadsDialog;
    private LogixNG_Thread _newStartupThread;
    private JLabel _startupThreadNameLabel;
    ThreadTableModel _threadTableModel = null;
    
    
    public EditThreadsDialog(ConditionalNG conditionalNG) {
        // Ensure that the predefined threads exist
        LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_THREAD);
        LogixNG_Thread.getThread(LogixNG_Thread.DEFAULT_LOGIXNG_DEBUG_THREAD);
        
        _conditionalNG = conditionalNG;
        _newStartupThread = LogixNG_Thread.getThread(_conditionalNG.getStartupThreadId());
    }
    
    public void showDialog() {
        
        _editThreadsDialog  = new JDialog(
                (JDialog)null,
                Bundle.getMessage("EditThreadsDialog_Title"),
                true);
        
        
        Container contentPane = _editThreadsDialog.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        
        _startupThreadNameLabel = new JLabel(Bundle.getMessage(
                "EditThreadsDialog_StartupThreadName",
                _newStartupThread.getThreadName()));
        contentPane.add(_startupThreadNameLabel);
        
        _threadTableModel = new ThreadTableModel();
        JTable conditionalTable = new JTable(_threadTableModel);
        conditionalTable.setRowSelectionAllowed(false);
        TableColumnModel conditionalColumnModel = conditionalTable
                .getColumnModel();
        TableColumn sNameColumn = conditionalColumnModel
                .getColumn(ThreadTableModel.THREAD_ID_COLUMN);
        sNameColumn.setResizable(true);
        sNameColumn.setMinWidth(100);
        sNameColumn.setPreferredWidth(130);
        TableColumn uNameColumn = conditionalColumnModel
                .getColumn(ThreadTableModel.THREAD_NAME_COLUMN);
        uNameColumn.setResizable(true);
        uNameColumn.setMinWidth(210);
        uNameColumn.setPreferredWidth(260);
        TableColumn buttonColumn = conditionalColumnModel
                .getColumn(ThreadTableModel.BUTTON_SELECT_THREAD_COLUMN);
        TableColumn buttonDeleteColumn = conditionalColumnModel
                .getColumn(ThreadTableModel.BUTTON_DELETE_COLUMN);

        // install button renderer and editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        conditionalTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        conditionalTable.setDefaultEditor(JButton.class, buttonEditor);
        JButton testButton = new JButton("XXXXXX");  // NOI18N
        conditionalTable.setRowHeight(testButton.getPreferredSize().height);
        buttonColumn.setMinWidth(testButton.getPreferredSize().width);
        buttonColumn.setMaxWidth(testButton.getPreferredSize().width);
        buttonColumn.setResizable(false);
        buttonDeleteColumn.setMinWidth(testButton.getPreferredSize().width);
        buttonDeleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        buttonDeleteColumn.setResizable(false);

        JScrollPane conditionalTableScrollPane = new JScrollPane(conditionalTable);
        Dimension dim = conditionalTable.getPreferredSize();
        dim.height = 450;
        conditionalTableScrollPane.getViewport().setPreferredSize(dim);
        contentPane.add(conditionalTableScrollPane);
        
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        
        contentPane.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        
        // Add thread
        JButton addThread = new JButton(Bundle.getMessage("EditThreadsDialog_ButtonAddThread"));    // NOI18N
        panel5.add(addThread);
        addThread.addActionListener((ActionEvent e) -> {
            String newName = "";
            if (! LogixNG_Thread.validateNewThreadName(newName)) {
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("EditThreadsDialog_ErrorThreadNameAlreadyExists", newName),
                        Bundle.getMessage("EditThreadsDialog_ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            LogixNG_Thread thread = LogixNG_Thread.createNewThread(newName);
            int row = _threadTableModel._threads.size();
            _threadTableModel._threads.add(thread);
            _threadTableModel.fireTableRowsInserted(row, row);
        });
//        addThread.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener((ActionEvent e) -> {
            abortPressed();
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        
        // OK
        JButton ok = new JButton(Bundle.getMessage("ButtonOK"));    // NOI18N
        panel5.add(ok);
        ok.addActionListener((ActionEvent e) -> {
            okPressed();
        });
//        ok.setToolTipText(Bundle.getMessage("OKLogixButtonHint"));      // NOI18N
        
        _editThreadsDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                abortPressed();
            }
        });
/*        
        _create = new JButton(Bundle.getMessage("ButtonCreate"));  // NOI18N
        panel5.add(_create);
        _create.addActionListener((ActionEvent e) -> {
            cancelAddPressed(null);
            
            SwingConfiguratorInterface swingConfiguratorInterface =
                    _swingConfiguratorComboBox.getItemAt(_swingConfiguratorComboBox.getSelectedIndex());
//            System.err.format("swingConfiguratorInterface: %s%n", swingConfiguratorInterface.getClass().getName());
            createAddFrame(femaleSocket, path, swingConfiguratorInterface);
        });
*/        
        contentPane.add(panel5);
        
//        addLogixNGFrame.setLocationRelativeTo(component);
        _editThreadsDialog.setLocationRelativeTo(null);
        _editThreadsDialog.pack();
        _editThreadsDialog.setVisible(true);
    }
    
    private void abortPressed() {
        _editThreadsDialog.setVisible(false);
        _editThreadsDialog.dispose();
        _editThreadsDialog = null;
    }
    
    private void okPressed() {
        _editThreadsDialog.setVisible(false);
        _editThreadsDialog.dispose();
        _editThreadsDialog = null;
        _conditionalNG.setStartupThreadId(_newStartupThread.getThreadId());
    }
    
    
    
    
    
    
    
    
    
    // ------------ Table Models ------------

    /**
     * Table model for ConditionalNGs in the Edit LogixNG pane.
     */
    private final class ThreadTableModel extends AbstractTableModel {

        public static final int THREAD_ID_COLUMN = 0;
        public static final int THREAD_NAME_COLUMN = THREAD_ID_COLUMN + 1;
        public static final int BUTTON_SELECT_THREAD_COLUMN = THREAD_NAME_COLUMN + 1;
        public static final int BUTTON_DELETE_COLUMN = BUTTON_SELECT_THREAD_COLUMN + 1;
        public static final int NUM_COLUMNS = BUTTON_DELETE_COLUMN + 1;
        
        private final java.util.List<LogixNG_Thread> _threads;
        
        
        public ThreadTableModel() {
            super();
            _threads = new ArrayList<>(LogixNG_Thread.getThreads());
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if ((c == BUTTON_SELECT_THREAD_COLUMN) || (c == BUTTON_DELETE_COLUMN)) {
                return JButton.class;
            } else if (c == THREAD_ID_COLUMN) {
                return Integer.class;
            }
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return NUM_COLUMNS;
        }

        @Override
        public int getRowCount() {
            return _threads.size();
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if ((c == THREAD_NAME_COLUMN) || (c == BUTTON_SELECT_THREAD_COLUMN)) {
                return true;
            }
            return ((c == BUTTON_DELETE_COLUMN) && (! _threads.get(r).getThreadInUse()));
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case THREAD_ID_COLUMN:
                    return Bundle.getMessage("ColumnSystemName");  // NOI18N
                case THREAD_NAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");  // NOI18N
                case BUTTON_SELECT_THREAD_COLUMN:
                    return ""; // no label
                case BUTTON_DELETE_COLUMN:
                    return ""; // no label
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                justification = "better to keep cases in column order rather than to combine")
        public int getPreferredWidth(int col) {
            switch (col) {
                case THREAD_ID_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case THREAD_NAME_COLUMN:
                    return new JTextField(17).getPreferredSize().width;
                case BUTTON_SELECT_THREAD_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                case BUTTON_DELETE_COLUMN:
                    return new JTextField(6).getPreferredSize().width;
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row > _threads.size()) {
                return null;
            }
            switch (col) {
                case BUTTON_SELECT_THREAD_COLUMN:
                    return Bundle.getMessage("EditThreadsDialog_ButtonSelectThread");  // NOI18N
                case BUTTON_DELETE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");  // NOI18N
                case THREAD_ID_COLUMN:
                    return _threads.get(row).getThreadId();
                case THREAD_NAME_COLUMN: {
                    return _threads.get(row).getThreadName();
                }
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }

        private void deleteThread(int row) {
            LogixNG_Thread thread = _threads.get(row);
            LogixNG_Thread.deleteThread(thread);
            fireTableRowsDeleted(row, row);
        }
        
        private void changeThreadName(Object value, int row) {
            String name = (String) value;
            LogixNG_Thread thread = _threads.get(row);
            if (!thread.getThreadName().equals(name)) {
                if (! LogixNG_Thread.validateNewThreadName(name)) {
                    JOptionPane.showMessageDialog(null,
                            Bundle.getMessage("EditThreadsDialog_ErrorThreadNameAlreadyExists", name),
                            Bundle.getMessage("EditThreadsDialog_ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                thread.setThreadName(name);
                fireTableRowsUpdated(row, row);
            }
        }
        
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row > _threads.size()) {
                return;
            }
            switch (col) {
                case BUTTON_SELECT_THREAD_COLUMN:
                    _newStartupThread = _threads.get(row);
                    _startupThreadNameLabel.setText(Bundle.getMessage(
                            "EditThreadsDialog_StartupThreadName",
                            _newStartupThread.getThreadName()));
                    break;
                case BUTTON_DELETE_COLUMN:
                    deleteThread(row);
                    break;
                case THREAD_ID_COLUMN:
                    throw new IllegalArgumentException("Thread ID cannot be changed");
                case THREAD_NAME_COLUMN: {
                    changeThreadName(value, row);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }
    }
    
    
    
}
