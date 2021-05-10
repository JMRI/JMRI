package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_InitializationManager;
import jmri.jmrit.logixng.LogixNG_Manager;

import jmri.util.JmriJFrame;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table for LogixNG initialization.
 *
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class LogixNGInitializationTable extends JmriJFrame {
    
    private static final int panelWidth700 = 700;
    private static final int panelHeight500 = 500;
    
    private final LogixNG_InitializationManager _initManager =
            InstanceManager.getDefault(LogixNG_InitializationManager.class);
    
    private InitializationTableModel _initTableModel = null;
    
    
    @Override
    public void initComponents() {
        super.initComponents();
        // build menu
//        JMenuBar menuBar = new JMenuBar();
//        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
//        toolMenu.add(new CreateNewLogixNGAction("Create a LogixNG"));
/*        
        toolMenu.add(new CreateNewLogixNGAction(Bundle.getMessage("TitleOptions")));
        toolMenu.add(new PrintOptionAction());
        toolMenu.add(new BuildReportOptionAction());
        toolMenu.add(new BackupFilesAction(Bundle.getMessage("Backup")));
        toolMenu.add(new RestoreFilesAction(Bundle.getMessage("Restore")));
        toolMenu.add(new LoadDemoAction(Bundle.getMessage("LoadDemo")));
        toolMenu.add(new ResetAction(Bundle.getMessage("ResetOperations")));
        toolMenu.add(new ManageBackupsAction(Bundle.getMessage("ManageAutoBackups")));
*/      
//        menuBar.add(toolMenu);
//        menuBar.add(new jmri.jmrit.operations.OperationsMenu());
        
//        setJMenuBar(menuBar);
//        addHelpMenu("package.jmri.jmrit.operations.Operations_Settings", true); // NOI18N
        
        
        
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        
        _initTableModel = new InitializationTableModel();
        JTable conditionalTable = new JTable(_initTableModel);
        conditionalTable.setRowSelectionAllowed(false);
        TableColumnModel conditionalColumnModel = conditionalTable
                .getColumnModel();
        TableColumn sNameColumn = conditionalColumnModel
                .getColumn(InitializationTableModel.SYSTEM_NAME_COLUMN);
        sNameColumn.setResizable(true);
        sNameColumn.setMinWidth(100);
        sNameColumn.setPreferredWidth(130);
        TableColumn uNameColumn = conditionalColumnModel
                .getColumn(InitializationTableModel.USER_NAME_COLUMN);
        uNameColumn.setResizable(true);
        uNameColumn.setMinWidth(210);
        uNameColumn.setPreferredWidth(260);
        TableColumn buttonDeleteColumn = conditionalColumnModel
                .getColumn(InitializationTableModel.BUTTON_DELETE_COLUMN);
        TableColumn buttonMoveUpColumn = conditionalColumnModel
                .getColumn(InitializationTableModel.BUTTON_MOVE_UP_COLUMN);
        TableColumn buttonMoveDownColumn = conditionalColumnModel
                .getColumn(InitializationTableModel.BUTTON_MOVE_DOWN_COLUMN);

        // install button renderer and editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        conditionalTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        conditionalTable.setDefaultEditor(JButton.class, buttonEditor);
        JButton testButton = new JButton("XXXXXXXXXXXXX");  // NOI18N
        conditionalTable.setRowHeight(testButton.getPreferredSize().height);
        buttonDeleteColumn.setMinWidth(testButton.getPreferredSize().width);
        buttonDeleteColumn.setMaxWidth(testButton.getPreferredSize().width);
        buttonDeleteColumn.setResizable(false);
        buttonMoveUpColumn.setMinWidth(testButton.getPreferredSize().width);
        buttonMoveUpColumn.setMaxWidth(testButton.getPreferredSize().width);
        buttonMoveUpColumn.setResizable(false);
        buttonMoveDownColumn.setMinWidth(testButton.getPreferredSize().width);
        buttonMoveDownColumn.setMaxWidth(testButton.getPreferredSize().width);
        buttonMoveDownColumn.setResizable(false);

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
        
        // Add LogixNG
        BeanSelectPanel<LogixNG> logixNG_SelectPanel =
                new BeanSelectPanel<>(InstanceManager.getDefault(LogixNG_Manager.class), null);
        
        panel5.add(logixNG_SelectPanel);
        JButton addLogixNG = new JButton(Bundle.getMessage("EditThreadsDialog_ButtonAddThread"));    // NOI18N
        panel5.add(addLogixNG);
        addLogixNG.addActionListener((ActionEvent e) -> {
            LogixNG logixNG = logixNG_SelectPanel.getNamedBean();
            if (logixNG == null) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("LogixNG_Initialization_ErrorNoLogixNG_Selected"),
                        Bundle.getMessage("LogixNG_Initialization_ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (LogixNG l : _initManager.getList()) {
                if (logixNG == l) {
                    JOptionPane.showMessageDialog(this,
                            Bundle.getMessage("LogixNG_Initialization_ErrorLogixNG_Exists"),
                            Bundle.getMessage("LogixNG_Initialization_ErrorTitle"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            _initManager.add(logixNG);
            _initTableModel.fireTableStructureChanged();
        });
        
        contentPane.add(panel5);
        
        pack();
        
        initMinimumSize(new Dimension(panelWidth700, panelHeight500));
    }
    
    public void initMinimumSize(Dimension dimension) {
        setMinimumSize(dimension);
        pack();
        setVisible(true);
    }
    
    
    
    
    
    
    
    
    
    // ------------ Table Models ------------

    /**
     * Table model for ConditionalNGs in the Edit LogixNG pane.
     */
    private final class InitializationTableModel extends AbstractTableModel {

        public static final int SYSTEM_NAME_COLUMN = 0;
        public static final int USER_NAME_COLUMN = SYSTEM_NAME_COLUMN + 1;
        public static final int BUTTON_DELETE_COLUMN = USER_NAME_COLUMN + 1;
        public static final int BUTTON_MOVE_UP_COLUMN = BUTTON_DELETE_COLUMN + 1;
        public static final int BUTTON_MOVE_DOWN_COLUMN = BUTTON_MOVE_UP_COLUMN + 1;
        public static final int NUM_COLUMNS = BUTTON_MOVE_DOWN_COLUMN + 1;
        
        private final List<LogixNG> _logixNGs;
        
        
        public InitializationTableModel() {
            super();
            _logixNGs = _initManager.getList();
        }
        
        @Override
        public Class<?> getColumnClass(int c) {
            if ((c == BUTTON_DELETE_COLUMN) || (c == BUTTON_MOVE_UP_COLUMN) || (c == BUTTON_MOVE_DOWN_COLUMN)) {
                return JButton.class;
            }
            return String.class;
        }
        
        @Override
        public int getColumnCount() {
            return NUM_COLUMNS;
        }
        
        @Override
        public int getRowCount() {
            return _logixNGs.size();
        }
        
        @Override
        public boolean isCellEditable(int r, int c) {
            return ((c == BUTTON_DELETE_COLUMN) || (c == BUTTON_MOVE_UP_COLUMN) || (c == BUTTON_MOVE_DOWN_COLUMN));
        }
        
        @Override
        public String getColumnName(int col) {
            switch (col) {
                case SYSTEM_NAME_COLUMN:
                    return Bundle.getMessage("ColumnSystemName");  // NOI18N
                case USER_NAME_COLUMN:
                    return Bundle.getMessage("ColumnUserName");  // NOI18N
                case BUTTON_DELETE_COLUMN:
                case BUTTON_MOVE_UP_COLUMN:
                case BUTTON_MOVE_DOWN_COLUMN:
                    return ""; // no label
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }
        
        @Override
        public Object getValueAt(int row, int col) {
            if (row > _logixNGs.size()) {
                return null;
            }
            switch (col) {
                case SYSTEM_NAME_COLUMN:
                    return _logixNGs.get(row).getSystemName();
                case USER_NAME_COLUMN:
                    return _logixNGs.get(row).getUserName();
                case BUTTON_DELETE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");  // NOI18N
                case BUTTON_MOVE_UP_COLUMN:
                    return Bundle.getMessage("ButtonMoveUp");  // NOI18N
                case BUTTON_MOVE_DOWN_COLUMN:
                    return Bundle.getMessage("ButtonMoveDown");  // NOI18N
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }
        
        private void deleteLogixNG(int row) {
            _initManager.delete(row);
            fireTableRowsDeleted(row, row);
        }
        
        private void moveUp(int row) {
            _initManager.moveUp(row);
            fireTableRowsDeleted(row, row);
        }
        
        private void moveDown(int row) {
            _initManager.moveDown(row);
            fireTableRowsDeleted(row, row);
        }
        
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (row > _logixNGs.size()) {
                return;
            }
            switch (col) {
                case SYSTEM_NAME_COLUMN:
                    throw new IllegalArgumentException("System name cannot be changed");
                case USER_NAME_COLUMN:
                    throw new IllegalArgumentException("User name cannot be changed");
                case BUTTON_DELETE_COLUMN:
                    deleteLogixNG(row);
                    break;
                case BUTTON_MOVE_UP_COLUMN:
                    moveUp(row);
                    break;
                case BUTTON_MOVE_DOWN_COLUMN:
                    moveDown(row);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown column");
            }
        }
    }
    
    
    
}
