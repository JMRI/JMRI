package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEditNVarPane extends JPanel implements TableModelListener {
    
    private JPanel infoPane;
    private JTabbedPane tabbedPane;
    private CanSystemConnectionMemo _memo;
    private CbusNodeNVTableDataModel nodeNVModel;
    private JButton saveNvButton;
    private JButton resetNvButton;
    private CbusNode nodeOfInterest;
    private final NodeConfigToolPane _mainPane;

    /**
     * Create a new instance of CbusNodeEditNVarPane.
     * @param main the NodeConfigToolPane this is a component of
     */
    protected CbusNodeEditNVarPane( NodeConfigToolPane main ) {
        super();
        nodeOfInterest = null;
        _mainPane = main;
    }

    /**
     * Create a new instance of CbusNodeEditNVarPane.
     * @param memo the System Connection to use
     */
    public void initComponents(CanSystemConnectionMemo memo) {
        
        _memo = memo;
        
        nodeNVModel = new CbusNodeNVTableDataModel(memo, 5,
        CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
        nodeNVModel.addTableModelListener(this);
        
        infoPane = new JPanel();
        setLayout(new BorderLayout() );
        
        saveNvButton = new JButton(("Save"));
        saveNvButton.setToolTipText(("Update Node"));
        
        resetNvButton = new JButton(Bundle.getMessage("Reset"));
        resetNvButton.setToolTipText(("Reset table New NV values"));
        
        ActionListener reset = ae -> {
            resetNVs();
        };
        resetNvButton.addActionListener(reset);
        
        ActionListener save = ae -> {
            _mainPane.showConfirmThenSave(nodeNVModel.getChangedNode(),nodeOfInterest,
                true,false,false, null ); // from, to, nvs, clear events, events, null uses mainpane frame
        };
        saveNvButton.addActionListener(save);
        
    }
    
    /**
     * Set the Node and update panes
     * @param node the CbusNode of Interest, can be null
     */
    public void setNode( CbusNode node ) {
        log.debug("setnode {}",nodeOfInterest);
        if (node == nodeOfInterest) {
            return;
        }
        
        nodeOfInterest = node;
        nodeNVModel.setNode(nodeOfInterest);
        
        if (infoPane != null ){ 
            infoPane.setVisible(false);
        }
        
        infoPane = null;
        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );

        JPanel nvMenuPane = new JPanel();
        JPanel buttonPane = new JPanel();
        setSaveCancelButtonsActive ( false );
        
        buttonPane.add(saveNvButton );
        buttonPane.add(resetNvButton ); 
        
        nvMenuPane.add(buttonPane);
        nvMenuPane.add( new JSeparator(SwingConstants.HORIZONTAL) );
        
        tabbedPane = new JTabbedPane();
        
        JPanel generic = new JPanel();
        JPanel template = new JPanel();
        
        CbusNodeNVEditTablePane genericNVTable = new CbusNodeNVEditTablePane(nodeNVModel);
        genericNVTable.initComponents(_memo);
        genericNVTable.setNode( nodeOfInterest );
        generic.add( genericNVTable );
        
        tabbedPane.addTab(("Generic"), genericNVTable);
        tabbedPane.addTab(("Template"), template);
        
        tabbedPane.setEnabledAt(1,false);
        tabbedPane.setSelectedIndex(0);
        
        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);
        infoPane.add(tabbedPane, BorderLayout.CENTER);
        
        this.add(infoPane);
        
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
     */
    public void resetNVs(){
        nodeNVModel.resetNewNvs();
    }
    
    /**
     * Get the Node being viewed / edited
     * @return the node, may be null
     */
    public CbusNode getNode() {
        return nodeOfInterest;
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
     * {@inheritDoc}
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        setSaveCancelButtonsActive( nodeNVModel.isTableDirty() );
    }
    
    /**
     * Removes the  NV Model listener from the Node
     */
    public void dispose(){
        if ( nodeNVModel !=null ) {
            nodeNVModel.removeTableModelListener(this);
            nodeNVModel.dispose();
        }
        
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusNodeEditNVarPane.class);
    
}
