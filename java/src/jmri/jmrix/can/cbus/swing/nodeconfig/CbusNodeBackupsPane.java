package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;
import jmri.jmrix.can.cbus.node.*;
import jmri.jmrix.can.cbus.node.CbusNodeConstants.BackupType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupsPane extends JPanel implements TableModelListener {
    public SimpleDateFormat readableDateStyle = new SimpleDateFormat ("HH:mm EEE d MMM"); // NOI18N
    private JScrollPane eventScroll;
    private JPanel infoPane;
    private JPanel backupInfoPane;
    private JSplitPane split;
    private CbusNode nodeOfInterest;
    private ActionListener newBackupListener;
    private ActionListener deleteBackupListener;
    private JButton newBackupButton;
    private JLabel headerText;
    private JPanel evMenuPane;
    private JTable backupTable;
    private JTabbedPane tabbedPane;
    private CbusNodeNVTableDataModel nodeNVModel;
    private CbusNodeNVEditTablePane nodevarPane;
    private CbusNodeEventTablePane nodeEventPane;
    private CbusNodeInfoPane nodeInfoPane;
    private CbusNodeBackupTableModel cbusNodeBackupTableModel = null;
    private final NodeConfigToolPane _mainPane;
    
    // table stuff
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);

    /**
     * Create a new instance of CbusNodeBackupsPane.
     * @param main the master Node Manager Pane
     */
    protected CbusNodeBackupsPane( NodeConfigToolPane main ) {
        super();
        _mainPane = main;
        initComponents();
    }

    /**
     * Create the Pane components with a null Node
     */
    public final void initComponents() {
        
        if (eventScroll != null ){ 
            eventScroll.setVisible(false);
            evMenuPane.setVisible(false);
        }
        eventScroll = null;
        evMenuPane = null;
        
        newBackupButton = new JButton(("Create New Backup"));
        evMenuPane = new JPanel();
        evMenuPane.add(newBackupButton);
        
        cbusNodeBackupTableModel = new CbusNodeBackupTableModel();
        cbusNodeBackupTableModel.addTableModelListener(this);
        
        backupTable = new JTable(cbusNodeBackupTableModel);
        backupTable.setRowHeight(26);
        backupTable.setDefaultRenderer(Date.class, getRenderer());
        backupTable.setDefaultRenderer(String.class, getRenderer());
        backupTable.setDefaultRenderer(Integer.class, getRenderer());
        backupTable.setDefaultRenderer(CbusNodeConstants.BackupType.class, getRenderer());
        
        backupTable.setAutoCreateRowSorter(true);
        backupTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        headerText = new JLabel("");
        evMenuPane.add(headerText);
        updateHeaderText();
        
        JScrollPane backupTableScrollPane = new JScrollPane(backupTable);
        //    textFieldName.setMargin( new java.awt.Insets(10,10,10,10) );
        
        setLayout(new BorderLayout() );
        
        nodeInfoPane = new CbusNodeInfoPane();
        
        nodeNVModel = new CbusNodeNVTableDataModel(null, 5,
            CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
        nodevarPane = new CbusNodeNVEditTablePane(nodeNVModel);
        nodevarPane.setNonEditable();
        
        CbusNodeEventTableDataModel nodeEvModel = new CbusNodeEventTableDataModel( null, null, 10,
            CbusNodeEventTableDataModel.MAX_COLUMN); // controller, row, column
        nodeEventPane = new CbusNodeEventTablePane(nodeEvModel);
        nodeEventPane.setHideEditButton();
        
        backupInfoPane = new JPanel();
        backupInfoPane.setLayout(new BorderLayout() );
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(("Backup Info"), backupInfoPane);
        tabbedPane.addTab(("Node Info"), nodeInfoPane);
        tabbedPane.addTab(("Node Variables"), nodevarPane);
        tabbedPane.addTab(("Node Events"), nodeEventPane);
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            backupTableScrollPane, tabbedPane);
        
        // there is potential for a decent amount of data crunching involved
        // when changing the view
        split.setContinuousLayout(false);
        split.setDividerLocation(100); // px from top of backups table
        
        this.add(evMenuPane, BorderLayout.PAGE_START);
        this.add(split, BorderLayout.CENTER);
        
        validate();
        repaint();
        
        newBackupListener = ae -> {
            saveBackup();
        };
        newBackupButton.addActionListener(newBackupListener);
        
        // add listener for bottom pane
        backupTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if ( !e.getValueIsAdjusting() ) {
                userBackupViewChanged();
            }
        });
    }
    
    /**
     * Set the node and display backup details
     * @param node can be null if no node selected
     */
    public void setNode(CbusNode node){
        
        if (node == nodeOfInterest){
            return;
        }
        if (nodeOfInterest!=null) {
            nodeOfInterest.removeBackupTableModel(cbusNodeBackupTableModel);
        }
        nodeOfInterest = node;
        if (nodeOfInterest==null){
            return;
        }
        if (nodeOfInterest.getNodeBackupFile() == null){
            return;
        }
        nodeOfInterest.addBackupTableModel(cbusNodeBackupTableModel);
        cbusNodeBackupTableModel.fireTableDataChanged();
        userBackupViewChanged(); // set no backup selected message on startup
    }
    
    /**
     * Triggered when either the row selected has changed or tab has changed
     */
    private void userBackupViewChanged(){
        
        int sel = backupTable.getSelectedRow();
        CbusNodeFromBackup backupNode;
        if ( sel > -1 ) {
            backupNode = nodeOfInterest.getNodeBackupFile().getBackups().get(backupTable.convertRowIndexToModel(sel));
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setEnabledAt(2,true);
            tabbedPane.setEnabledAt(3,true);
            
            nodevarPane.setNode( backupNode );
            nodeEventPane.setNode( backupNode );
            nodeInfoPane.initComponents(backupNode);
            
        } else {
            backupNode = null;
            tabbedPane.setEnabledAt(1,false);
            tabbedPane.setEnabledAt(2,false);
            tabbedPane.setEnabledAt(3,false);
        }
        
        // Pane to hold Node
        // evPane.setLayout(new BoxLayout(evPane, BoxLayout.Y_AXIS));
        
        if (infoPane != null ){ 
                infoPane.setVisible(false);
            }
        infoPane = null;
        
        // build backup pane locally
        
        if ( backupNode!=null ) {
            
            // log.info("building pane for backupnode {}",backupNode);
            StringBuilder text = new StringBuilder();
            text.append( "<html><h3>" );
            text.append(readableDateStyle.format(backupNode.getBackupTimeStamp()));
            text.append("</h3>");
            text.append(" <h4>NV's : " );
            text.append( ( Math.max(0,backupNode.getParameter(6) )) );
            text.append( "</h4>");
            text.append( "<h4>Events : " );
            text.append(  ( Math.max(0,backupNode.getTotalNodeEvents()) ) );
            text.append( "</h4>");
            text.append( "<h4>Params : " );
            text.append(  ( Math.max(0,backupNode.getParameter(0) )) );
            text.append( "</h4>");
            text.append("</html>");
            
            JLabel nvstring = new JLabel(text.toString());
            JPanel evPane = new JPanel();
            evPane.setLayout(new BoxLayout(evPane, BoxLayout.X_AXIS));
            evPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // nvstring.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            //  evPane.setLayout(new BorderLayout() );
            evPane.add(nvstring);
            
            JButton restoreBackupButton = new JButton("Restore Backup");
            JButton deleteBackupButton = new JButton("Delete Backup");
            
            restoreBackupButton.setEnabled(false);
            
            if (backupNode.getBackupResult() == CbusNodeConstants.BackupType.COMPLETE ) {
                if ( backupNode.getTotalNVs() == nodeOfInterest.getTotalNVs() ) {
                    if ( backupNode.getParameter(5) == nodeOfInterest.getParameter(5) ) {
                        restoreBackupButton.setToolTipText(null);
                        restoreBackupButton.setEnabled(true);
                    } else {
                        restoreBackupButton.setToolTipText("Event Variable total does not match");
                    }
                } else {
                    restoreBackupButton.setToolTipText("NV total does not match");
                }
            } else {
                restoreBackupButton.setToolTipText("Backup Incomplete");
            }
            
            deleteBackupListener = ae -> {
                deleteBackup(sel);
            };
            deleteBackupButton.addActionListener(deleteBackupListener);
            
            ActionListener restore = ae -> {
                // pre-validation checks, ie same nv's and same ev vars should be by button enabled
                _mainPane.showConfirmThenSave(backupNode,nodeOfInterest,
                    true, true, true, null ); // from, to, nvs, clear events, events, null uses mainpane frame
            };
            restoreBackupButton.addActionListener(restore);
            
            evPane.add(restoreBackupButton);
            evPane.add(deleteBackupButton);
            
            JScrollPane scroll = new JScrollPane(evPane);
            infoPane = new JPanel();
            infoPane.setLayout(new BorderLayout() );
            infoPane.add(scroll);
            
            backupInfoPane.add(infoPane);
            backupInfoPane.revalidate();
            
        } else {
            JLabel nvstring = new JLabel("<html><h3>No Backup Selected</h3></html>");
            infoPane = new JPanel();
            infoPane.add(nvstring);
            backupInfoPane.add(infoPane);
            backupInfoPane.validate();
            backupInfoPane.repaint();
        }
    }
    
    /**
     * Updates the header text
     */
    private void updateHeaderText(){
        if (nodeOfInterest != null ) {
            StringBuilder text = new StringBuilder();
            text.append("<html><h4>");
            if (nodeOfInterest.getNodeBackupFile().getBackups().size() == 1 ){
                text.append(nodeOfInterest.getNodeBackupFile().getBackups().size());
                text.append(" xml entry");
            } else {
                text.append(nodeOfInterest.getNodeBackupFile().getBackups().size());
                text.append(" xml entries");
            }
            text.append("</h4></html>");
            headerText.setText(text.toString());
            evMenuPane.revalidate();
            evMenuPane.repaint();
        }
    }
    
    /**
     * Save a new backup with rotation
     */
    private void saveBackup() {
        if (!nodeOfInterest.getNodeBackupFile().doStore(true, nodeOfInterest.hasLoadErrors())){
            log.error("Issue saving Backup File");
        }
        cbusNodeBackupTableModel.fireTableDataChanged();
    }
    
    /**
     * Delete a backup from the array and re-save the xml
     * @param bup The index in the backup array to delete, 0 is most recent.
     */
    private void deleteBackup(int bup){
        log.debug("Manually deleting {}",nodeOfInterest.getNodeBackupFile().getBackups().get(bup));
        nodeOfInterest.getNodeBackupFile().getBackups().remove(bup);
        if (!nodeOfInterest.getNodeBackupFile().doStore(false, nodeOfInterest.hasLoadErrors())){
            log.error("Issue saving Backup File following remove single entry");
        }
        cbusNodeBackupTableModel.fireTableDataChanged();
    }
    
    /**
     * Cell Renderer for string table columns, highlights any text in filter input
     */    
    private TableCellRenderer getRenderer() {
        return new TableCellRenderer() {
            JTextField f = new JTextField();
            
            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object arg1, boolean isSelected, boolean hasFocus, 
                int row, int col) {
                f.setHorizontalAlignment(JTextField.CENTER);
                f.setBorder( table.getBorder() );
                
                String string;
                if ( row % 2 == 0 ) {
                    f.setBackground( table.getBackground() );
                } else {
                    f.setBackground( WHITE_GREEN );
                }
                if (isSelected) {
                    f.setBackground( table.getSelectionBackground() );
                }
                if(arg1 != null){
                    string = arg1.toString();
                    f.setText(string);
                    
                    if (arg1 instanceof Date) {
                        f.setText(readableDateStyle.format((Date) arg1));
                    }
                    if ( arg1 instanceof BackupType ) {
                        f.setText(CbusNodeConstants.displayPhrase( (BackupType) arg1));
                        f.setBackground( VERY_LIGHT_RED );
                        if ( Objects.equals(arg1 , BackupType.COMPLETE )) {
                            f.setBackground( VERY_LIGHT_GREEN );
                        }
                    }
                } else {
                    f.setText("");
                }
                if (hasFocus) {
                   f.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.blue));
                } else {
                    f.setBorder( table.getBorder() );
                }
                return f;
            }
        };
    }
    /**
     * Update the header text (backup total) when table changes
     * {@inheritDoc} 
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        updateHeaderText();
    }
    
    /**
     * Table model for Backup Files.
     */
    public class CbusNodeBackupTableModel extends javax.swing.table.AbstractTableModel {

        public static final int DATE_COLUMN = 0;
        public static final int STATUS_COLUMN = 1;
        public static final int BYTES_COLUMN = 2;
        public static final int COMMENT_COLUMN = 3;
        public static final int DESCRIPTION_COLUMN = 4;

        public CbusNodeBackupTableModel() {
            super();
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getColumnClass(int c) {
            if (c == DATE_COLUMN) {
                return Date.class;
            }
            if (c == STATUS_COLUMN) {
                return CbusNodeConstants.BackupType.class;
            }
            if (c == BYTES_COLUMN) {
                return Integer.class;
            }
            if (c == COMMENT_COLUMN) {
                return String.class;
            }
            if (c == DESCRIPTION_COLUMN) {
                return String.class;
            }
            return String.class;
        }

        /** {@inheritDoc} */
        @Override
        public int getColumnCount() {
            return DESCRIPTION_COLUMN + 1;
        }

        /** {@inheritDoc} */
        @Override
        public int getRowCount() {
            if ( nodeOfInterest==null ){
                return 0;
            }
            return (nodeOfInterest.getNodeBackupFile().getBackups().size());
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCellEditable(int r, int c) {
            return c == COMMENT_COLUMN;
        }

        /** {@inheritDoc} */
        @Override
        public String getColumnName(int col) {
            if (col == DATE_COLUMN) {
                return ("Date / Time");
            }
            if (col == STATUS_COLUMN) {
                return ("Backup Integrity");
            }
            if (col == BYTES_COLUMN) {
                return("Bytes");
            }
            if (col == COMMENT_COLUMN) {
                return ("User Comment");
            }
            return "";
        }
        
        public int getPreferredWidth(int col) {
            switch (col) {
                case DATE_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case STATUS_COLUMN:
                    return new JTextField(60).getPreferredSize().width;
                case BYTES_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case COMMENT_COLUMN:
                    return new JTextField(80).getPreferredSize().width;
                case DESCRIPTION_COLUMN:
                    return new JTextField(70).getPreferredSize().width;
                default:
                    // fall through
                    break;
            }
            return new JTextField(8).getPreferredSize().width;
        }

        /** {@inheritDoc} */
        @Override
        public Object getValueAt(int r, int c) {
            if (r > nodeOfInterest.getNodeBackupFile().getBackups().size()) {
                return null;
            }
            CbusNodeFromBackup lc = nodeOfInterest.getNodeBackupFile().getBackups().get(r);
            switch (c) {
                case DATE_COLUMN:
                    return ((lc.getBackupTimeStamp()));
                case STATUS_COLUMN:
                    return (lc.getBackupResult());
                case BYTES_COLUMN:
                    return lc.totalNodeFileBytes();
                case COMMENT_COLUMN:
                    return (lc.getBackupComment());
                case DESCRIPTION_COLUMN:
                    if ( r == nodeOfInterest.getNodeBackupFile().getBackups().size()-1 ){
                        return ("First Backup on File");
                    }
                    if (lc.getBackupResult() == BackupType.NOTONNETWORK) {
                        return BackupType.NOTONNETWORK;
                    }
                    return lc.compareWithString(getPreviousBackup(r+1));
                default:
                    return "123";
            }
        }
        
        /** 
         * Get the previous actual backup to this one in array order, else null
         */
        private CbusNodeFromBackup getPreviousBackup(int arrayIndex){
            for (int i = arrayIndex; i < nodeOfInterest.getNodeBackupFile().getBackups().size()-1; i++) {
                if (nodeOfInterest.getNodeBackupFile().getBackups().get(i).getBackupResult() != BackupType.NOTONNETWORK) {
                    return nodeOfInterest.getNodeBackupFile().getBackups().get(i);
                }
            }
            return null;
        }

        /**
         * If Backup Comment changes, update backup and save xml
         * {@inheritDoc}
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == COMMENT_COLUMN) {
                nodeOfInterest.getNodeBackupFile().getBackups().get(row).setBackupComment(String.valueOf(value));
                if(!nodeOfInterest.getNodeBackupFile().doStore(false, nodeOfInterest.hasLoadErrors())){
                    log.error("Unable to save Backup User Comment to File");
                }
            }
        }
    }
    
    public void dispose() {
        // bupFile.getBackupModel().removeTableModelListener(this);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeBackupsPane.class);
    
}
