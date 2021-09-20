package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEditNVarPane extends CbusNodeConfigTab implements TableModelListener {
    
    private JTabbedPane tabbedPane;
    private JPanel infoPane;
    private CbusNodeNVTableDataModel nodeNVModel;
    private JButton saveNvButton;
    private JButton resetNvButton;
    private JPanel buttonPane;
    private CbusNodeNVEditTablePane genericNVTable;
    private CbusNodeNVEditGuiPane editNVGui;
    
    /**
     * Create a new instance of CbusNodeEditNVarPane.
     * @param main the NodeConfigToolPane this is a component of
     */
    protected CbusNodeEditNVarPane( NodeConfigToolPane main ) {
        super(main);
        buildPane();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(){
        return "Node Variables";
    }

    private void buildPane() {
        
        nodeNVModel = new CbusNodeNVTableDataModel(memo, 5,
        CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
        nodeNVModel.addTableModelListener(this);
        
        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );
        
        saveNvButton = new JButton(("Save"));
        saveNvButton.setToolTipText(("Update Node"));
        
        resetNvButton = new JButton(Bundle.getMessage("Reset"));
        resetNvButton.setToolTipText(("Reset table New NV values"));
        
        ActionListener reset = ae -> {
            cancelOption();
        };
        resetNvButton.addActionListener(reset);
        
        ActionListener save = ae -> {
            saveOption();
        };
        saveNvButton.addActionListener(save);
        
        buttonPane = new JPanel();
        buttonPane.add(saveNvButton );
        buttonPane.add(resetNvButton ); 
        
        
        infoPane.setLayout(new BorderLayout() );

        JPanel nvMenuPane = new JPanel();

        nvMenuPane.add(buttonPane);
        nvMenuPane.add( new JSeparator(SwingConstants.HORIZONTAL) );
        
        genericNVTable = new CbusNodeNVEditTablePane(nodeNVModel);
        genericNVTable.initComponents(memo);
        
        editNVGui = new CbusNodeNVEditGuiPane(nodeNVModel);
        editNVGui.initComponents(memo);
        
        tabbedPane = new JTabbedPane();
        
        JPanel template = new JPanel();
        
        tabbedPane.addTab(("Generic"), genericNVTable);
        tabbedPane.addTab(("Edit"), editNVGui);
        tabbedPane.addTab(("Template"), template);
        
        tabbedPane.setEnabledAt(1,false);
        tabbedPane.setEnabledAt(2,false);
        tabbedPane.setSelectedIndex(0);
        
        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);
        infoPane.add(tabbedPane, BorderLayout.CENTER);
        
        this.add(infoPane);
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveOption(){
        getMainPane().showConfirmThenSave(nodeNVModel.getChangedNode(),nodeOfInterest,
    true,false,false, null ); // from, to, nvs, clear events, events, null uses mainpane frame
    }
    
    /**
     * Set the Node and update panes
     * @param node the CbusNode of Interest, can be null
     */
    @Override
    public void changedNode( CbusNode node ) {
        log.debug("setnode {}",nodeOfInterest);
        
        nodeNVModel.setNode(nodeOfInterest);
        setSaveCancelButtonsActive ( false );
        genericNVTable.setNode( nodeOfInterest );
        
        if (editNVGui != null) {
            editNVGui.setNode(nodeOfInterest);
            
            tabbedPane.setEnabledAt(1,true);
            tabbedPane.setSelectedIndex(1);
        } else {
            tabbedPane.setEnabledAt(1,false);
            tabbedPane.setSelectedIndex(0);
        }
        
        validate();
        repaint();
        setVisible(true);
        
    }
    
    /**
     * Get if any NVs are dirty
     * @return true if NVs have been edited, else false
     */
    public boolean areNvsDirty(){
        log.debug("Table Dirty {}",nodeNVModel.isTableDirty());
        return nodeNVModel.isTableDirty();
    }
    
    /**
     * Reset edited NVs to original value ( or reset edited NV values if mid-load )
     * Inform the provider of a the reset
     */
    @Override
    protected void cancelOption(){
        nodeNVModel.resetNewNvs();
        if (editNVGui != null) {
            editNVGui.setNode(nodeOfInterest);
        }
    }
    
    /**
     * Set the Save / Reset NV button status
     * @param newstate true if buttons should be enabled, else false
     */
    public void setSaveCancelButtonsActive ( boolean newstate ) {
        saveNvButton.setEnabled(newstate);
        resetNvButton.setEnabled(newstate);
    }

    /**
     * Sets save / reset buttons active / inactive depending on table status
     * Informs the module provider of a table change
     * {@inheritDoc}
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        setSaveCancelButtonsActive( nodeNVModel.isTableDirty() );
        if (editNVGui != null) {
            editNVGui.tableChanged(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean getVetoBeingChanged(){
        if (areNvsDirty()) {
            return getCancelSaveEditDialog( Bundle.getMessage("NvsEditUnsaved",nodeOfInterest) );
        }
        return false;
    }
    
    /**
     * Removes the  NV Model listener from the Node
     */
    @Override
    public void dispose(){
        if ( nodeNVModel !=null ) {
            nodeNVModel.removeTableModelListener(this);
            nodeNVModel.dispose();
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarPane.class);
    
}
