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
 * Pane for displaying CBUS Node Configuration Backups.
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
    private CbusNodeBackupTableModel cbusNodeBackupTableModel;
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
     * Create the Pane components with a null Node.
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
        
        cbusNodeBackupTableModel = new CbusNodeBackupTableModel(null);
        cbusNodeBackupTableModel.addTableModelListener(this);
        
        backupTable = new JTable(cbusNodeBackupTableModel);
        backupTable.setRowHeight(26);
        backupTable.setDefaultRenderer(Date.class, getRenderer());
        backupTable.setDefaultRenderer(String.class, getRenderer());
        backupTable.setDefaultRenderer(Integer.class, getRenderer());
        backupTable.setDefaultRenderer(CbusNodeConstants.BackupType.class, getRenderer());
        backupTable.setAutoCreateRowSorter(true);
        backupTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < backupTable.getColumnCount(); i++) {
            backupTable.getColumnModel().getColumn(i).setPreferredWidth(CbusNodeBackupTableModel.getPreferredWidth(i));
        }
        
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
     * Set the node and display backup details.
     * @param node can be null if no node selected
     */
    public void setNode(CbusNode node){
        
        if (node == nodeOfInterest){
            return;
        }
        if (nodeOfInterest!=null) {
            nodeOfInterest.removePropertyChangeListener(cbusNodeBackupTableModel);
        }
        nodeOfInterest = node;
        if (nodeOfInterest==null){
            return;
        }
        
        cbusNodeBackupTableModel.setNode(nodeOfInterest);
        nodeOfInterest.addPropertyChangeListener(cbusNodeBackupTableModel);
        userBackupViewChanged(); // set no backup selected message on startup
    }
    
    /**
     * Triggered when either the row selected has changed or tab has changed.
     */
    private void userBackupViewChanged(){
        
        int sel = backupTable.getSelectedRow();
        CbusNodeFromBackup backupNode;
        if ( sel > -1 ) {
            backupNode = nodeOfInterest.getNodeBackupManager().getBackups().get(backupTable.convertRowIndexToModel(sel));
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
            text.append( "<html><h3>" )
            .append(readableDateStyle.format(backupNode.getBackupTimeStamp()))
            .append("</h3>")
            .append(" <h4>NV's : " )
            .append( ( Math.max(0,backupNode.getNodeParamManager().getParameter(6) )) )
            .append( "</h4>")
            .append( "<h4>Events : " )
            .append(  ( Math.max(0,backupNode.getNodeEventManager().getTotalNodeEvents()) ) )
            .append( "</h4>")
            .append( "<h4>Params : " )
            .append(  ( Math.max(0,backupNode.getNodeParamManager().getParameter(0) )) )
            .append( "</h4>")
            .append("</html>");
            
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
                if ( backupNode.getNodeNvManager().getTotalNVs() == nodeOfInterest.getNodeNvManager().getTotalNVs() ) {
                    if ( backupNode.getNodeParamManager().getParameter(5) == nodeOfInterest.getNodeParamManager().getParameter(5) ) {
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
     * Updates the header text.
     */
    private void updateHeaderText(){
        if (nodeOfInterest != null ) {
            StringBuilder text = new StringBuilder();
            text.append("<html><h4>");
            if (nodeOfInterest.getNodeBackupManager().getBackups().size() == 1 ){
                text.append(nodeOfInterest.getNodeBackupManager().getBackups().size())
                .append(" xml entry");
            } else {
                text.append(nodeOfInterest.getNodeBackupManager().getBackups().size())
                .append(" xml entries");
            }
            text.append("</h4></html>");
            headerText.setText(text.toString());
            evMenuPane.revalidate();
            evMenuPane.repaint();
        }
    }
    
    /**
     * Save a new backup with rotation.
     */
    private void saveBackup() {
        if (!nodeOfInterest.getNodeBackupManager().doStore(true, nodeOfInterest.getNodeStats().hasLoadErrors())){
            log.error("Issue saving Backup File");
        }
        cbusNodeBackupTableModel.fireTableDataChanged();
    }
    
    /**
     * Delete a backup from the array and re-save the XML.
     * @param bup The index in the backup array to delete, 0 is most recent.
     */
    private void deleteBackup(int bup){
        log.debug("Manually deleting {}",nodeOfInterest.getNodeBackupManager().getBackups().get(bup));
        nodeOfInterest.getNodeBackupManager().getBackups().remove(bup);
        if (!nodeOfInterest.getNodeBackupManager().doStore(false, nodeOfInterest.getNodeStats().hasLoadErrors())){
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
     * Update the header text (backup total) when table changes.
     * {@inheritDoc} 
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        updateHeaderText();
    }

    public void dispose() {
        // bupFile.getBackupModel().removeTableModelListener(this);
        nodeOfInterest.removePropertyChangeListener(cbusNodeBackupTableModel);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeBackupsPane.class);
    
}
