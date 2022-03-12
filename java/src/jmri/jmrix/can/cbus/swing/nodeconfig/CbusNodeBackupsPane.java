package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableCellRenderer;

import jmri.jmrix.can.cbus.node.*;

import jmri.jmrix.can.cbus.swing.CbusCommonSwing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for displaying CBUS Node Configuration Backups.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeBackupsPane extends CbusNodeConfigTab implements TableModelListener {
    public SimpleDateFormat readableDateStyle = new SimpleDateFormat ("HH:mm EEE d MMM"); // NOI18N
    private JScrollPane eventScroll;
    private JPanel backupInfoPane;
    private JPanel newInfoPane;
    private JSplitPane split;
    private ActionListener newBackupListener;
    private ActionListener deleteBackupListener;
    private JButton newBackupButton;
    private JLabel headerText;
    private JPanel evMenuPane;
    private JTable backupTable;
    private JTabbedPane tabbedBackupPane;
    private CbusNodeNVTableDataModel nodeNVModel;
    private CbusNodeNVEditTablePane nodevarPane;
    private CbusNodeEventTablePane nodeEventPane;
    private CbusNodeInfoPane nodeInfoPane;
    private CbusNodeBackupTableModel cbusNodeBackupTableModel;
    
    // table stuff
    public static final Color VERY_LIGHT_RED = new Color(255,176,173);
    public static final Color VERY_LIGHT_GREEN = new Color(165,255,164);
    public static final Color WHITE_GREEN = new Color(0xf5,0xf5,0xf5);

    /**
     * Create a new instance of CbusNodeBackupsPane.
     * @param main the master Node Manager Pane
     */
    protected CbusNodeBackupsPane( NodeConfigToolPane main ) {
        super(main);
        initPane();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(){
        return "Node Backups";
    }

    /**
     * Create the Pane components with a null Node.
     */
    public final void initPane() {
        
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
        
        // setLayout(new BorderLayout() );
        
        nodeInfoPane = new CbusNodeInfoPane(null);
        
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
        
        tabbedBackupPane = new JTabbedPane();
        tabbedBackupPane.addTab(("Backup Info"), backupInfoPane);
        tabbedBackupPane.addTab(("Node Info"), nodeInfoPane);
        tabbedBackupPane.addTab(("Node Variables"), nodevarPane);
        tabbedBackupPane.addTab(("Node Events"), nodeEventPane);
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            backupTableScrollPane, tabbedBackupPane);
        
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
        
        tabbedBackupPane.addChangeListener((ChangeEvent e) -> {
            userBackupViewChanged();
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void disposeOfNode(CbusNode node){
        node.removePropertyChangeListener(cbusNodeBackupTableModel);
        super.disposeOfNode(node);
    }
    
    /**
     * Set the node and display backup details.
     * {@inheritDoc}
     */
    @Override
    public void changedNode(CbusNode node){
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
        if ( backupTable.getSelectedRow() > -1 ) {
            backupNode = nodeOfInterest.getNodeBackupManager().getBackups().get(backupTable.convertRowIndexToModel(sel));
            tabbedBackupPane.setEnabled(true);
            
            if (tabbedBackupPane.getSelectedIndex()==1){
                nodeInfoPane.setNode(backupNode);
            }
            if (tabbedBackupPane.getSelectedIndex()==2){
                nodevarPane.setNode( backupNode );
            }
            if (tabbedBackupPane.getSelectedIndex()==3){
                nodeEventPane.setNode( backupNode );
            }
            
            
        } else {
            backupNode = null;
            tabbedBackupPane.setSelectedIndex(0);
            tabbedBackupPane.setEnabled(false);
        }
        
        log.debug("user view changed node {}, index {}",backupNode,tabbedBackupPane.getSelectedIndex());
        
        if (tabbedBackupPane.getSelectedIndex()==0) {
        
            if (newInfoPane != null ){ 
                newInfoPane.setVisible(false);
            }
            newInfoPane = null;
        
            // build backup pane locally
        
            newInfoPane = new JPanel();
            
            if ( backupNode!=null ) {
                JScrollPane scroll = new JScrollPane(getBackupPanel(backupNode));
                newInfoPane.setLayout(new BorderLayout() );
                newInfoPane.add(scroll);

                backupInfoPane.add(newInfoPane);
                backupInfoPane.revalidate();

            } else {
                JLabel nvstring = new JLabel("<html><h3>No Backup Selected</h3></html>");
                newInfoPane.add(nvstring);
                backupInfoPane.add(newInfoPane);
                backupInfoPane.validate();
                backupInfoPane.repaint();
            }
        
        }
        
    }
    
    private JPanel getBackupPanel(CbusNodeFromBackup backupNode){
    
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
                deleteBackup(backupTable.convertRowIndexToModel(backupTable.getSelectedRow()));
            };
            deleteBackupButton.addActionListener(deleteBackupListener);
            
            ActionListener restore = ae -> {
                // pre-validation checks, ie same nv's and same ev vars should be by button enabled
                getMainPane().showConfirmThenSave(backupNode,nodeOfInterest,
                    true, true, true, null ); // from, to, nvs, clear events, events, null uses mainpane frame
            };
            restoreBackupButton.addActionListener(restore);
            
            evPane.add(restoreBackupButton);
            evPane.add(deleteBackupButton);
    
        return evPane;
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
        int rowAfter = Math.max(0,backupTable.getSelectedRow()-1);
        nodeOfInterest.getNodeBackupManager().getBackups().remove(bup);
        if (!nodeOfInterest.getNodeBackupManager().doStore(false, nodeOfInterest.getNodeStats().hasLoadErrors())){
            log.error("Issue saving Backup File following remove single entry");
        }
        cbusNodeBackupTableModel.fireTableDataChanged();
        if (backupTable.getRowCount() > 0 ) {
            backupTable.getSelectionModel().setSelectionInterval(rowAfter,rowAfter);
        }
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
                CbusCommonSwing.setCellBackground(isSelected, f, table, row);
                
                if(arg1 != null){
                    string = arg1.toString();
                    f.setText(string);
                    CbusCommonSwing.setCellFromDate(arg1, f, readableDateStyle);
                    CbusCommonSwing.setCellFromBackupEnum(arg1, f);
                } else {
                    f.setText("");
                }
                CbusCommonSwing.setCellFocus(hasFocus, f, table);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        // bupFile.getBackupModel().removeTableModelListener(this);
        disposeOfNode(nodeOfInterest);
        nodeOfInterest.removePropertyChangeListener(cbusNodeBackupTableModel);
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeBackupsPane.class);
    
}
