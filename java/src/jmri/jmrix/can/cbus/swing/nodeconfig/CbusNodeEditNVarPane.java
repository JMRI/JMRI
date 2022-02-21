package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.CbusConfigPaneProvider;
import jmri.jmrix.can.cbus.swing.modules.UnknownPaneProvider;

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
    private JButton liveUpdateNvButton;
    private JButton resetNvButton;
    private JPanel buttonPane;
    private CbusNodeNVEditTablePane genericNVTable;
    private CbusNodeNVEditGuiPane editNVGui;
    private CbusConfigPaneProvider provider;
    
    private static final int GENERIC = 0;
    private static final int EDIT = 1;
    private static final int TEMPLATE = 2;
    
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
        
        saveNvButton = new JButton(Bundle.getMessage("ButtonSave"));
        saveNvButton.setToolTipText(Bundle.getMessage("SaveNvButtonTt"));
        
        liveUpdateNvButton = new JButton(Bundle.getMessage("LiveUpdateNode"));
        liveUpdateNvButton.setToolTipText(Bundle.getMessage("LiveUpdateNodeTt"));
        
        resetNvButton = new JButton(Bundle.getMessage("Reset"));
        resetNvButton.setToolTipText(Bundle.getMessage("ResetTt"));
        
        ActionListener reset = ae -> {
            cancelOption();
        };
        resetNvButton.addActionListener(reset);
        
        ActionListener save = ae -> {
            saveOption();
        };
        saveNvButton.addActionListener(save);
        
        ActionListener liveUpdate = ae -> {
            liveUpdateOption();
        };
        liveUpdateNvButton.addActionListener(liveUpdate);
        
        buttonPane = new JPanel();
        buttonPane.add(liveUpdateNvButton );
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
        
        tabbedPane.setEnabledAt(EDIT,false);
        tabbedPane.setEnabledAt(TEMPLATE,false);
        tabbedPane.setSelectedIndex(GENERIC);
        
        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);
        infoPane.add(tabbedPane, BorderLayout.CENTER);
        
        this.add(infoPane);
        
    }
    
    /**
     * Put the node into learn mode so that NV writes are performed immediately.
     * e.g., for live update of servo position NVs.
     */
    protected void liveUpdateOption() {
        nodeOfInterest.send.nodeEnterLearnEvMode(nodeOfInterest.getNodeNumber());
        saveNvButton.setEnabled(true);
        nodeOfInterest.setliveUpdate(true);
    }
    
    /**
     * {@inheritDoc}
     * 
     * If node was in learn mode then there's nothing to save but we take it out
     * of learn mode which will trigger the module to flush NVs to non-volatile 
     * storage if necessary.
     */
    @Override
    protected void saveOption(){
        if (nodeOfInterest.getnvWriteInLearnOnly()) {
            nodeOfInterest.send.nodeExitLearnEvMode(nodeOfInterest.getNodeNumber());
            saveNvButton.setEnabled(false);
            nodeOfInterest.setliveUpdate(false);
        } else {
            getMainPane().showConfirmThenSave(nodeNVModel.getChangedNode(),nodeOfInterest,
                    true,false,false, null ); // from, to, nvs, clear events, events, null uses mainpane frame
        }
    }
    
    /**
     * Set the Node and update panes
     * 
     * Show the edit GUI if available.
     * 
     * @param node the CbusNode of Interest, can be null
     */
    @Override
    public void changedNode( CbusNode node ) {
        log.debug("setnode {}",nodeOfInterest);
        
        nodeNVModel.setNode(nodeOfInterest);
        setSaveCancelButtonsActive ( false );
        if (nodeOfInterest.getnvWriteInLearnOnly()) {
            liveUpdateNvButton.setVisible(true);
            liveUpdateNvButton.setEnabled(true);
            saveNvButton.setEnabled(true);
        } else {
            liveUpdateNvButton.setVisible(false);
            liveUpdateNvButton.setEnabled(false);
        }
        genericNVTable.setNode( nodeOfInterest );

        provider = CbusConfigPaneProvider.getProviderByNode(nodeOfInterest);
        editNVGui.setNode(nodeOfInterest, provider);

        if (!(provider instanceof UnknownPaneProvider)) {
            tabbedPane.setEnabledAt(EDIT, true);
            tabbedPane.setSelectedIndex(EDIT);
        } else {
            tabbedPane.setEnabledAt(EDIT, false);
            tabbedPane.setSelectedIndex(GENERIC);
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
        editNVGui.setNode(nodeOfInterest);
    }
    
    /**
     * Set the Save / Reset NV button status
     * 
     * Save button is always enabled when in live update
     * 
     * @param newstate true if buttons should be enabled, else false
     */
    public void setSaveCancelButtonsActive ( boolean newstate ) {
        if (liveUpdateNvButton.isVisible()) {
            saveNvButton.setEnabled(true);
        } else {
            saveNvButton.setEnabled(newstate);
        }
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
        editNVGui.tableChanged(e);
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
     * Removes the NV Model listener from the Node.
     * 
     * Also dispose of the edit gui cleanly to take node out of learn mode
     */
    @Override
    public void dispose(){
        if ( nodeNVModel !=null ) {
            nodeNVModel.removeTableModelListener(this);
            nodeNVModel.dispose();
        }
        
        editNVGui.dispose();
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarPane.class);
    
}
