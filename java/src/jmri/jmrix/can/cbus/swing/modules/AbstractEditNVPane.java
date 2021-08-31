package jmri.jmrix.can.cbus.swing.modules;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;

/**
 * Abstract Node Variable edit Frame for a CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
abstract public class AbstractEditNVPane extends jmri.jmrix.can.swing.CanPanel {
    
    protected CbusNodeNVTableDataModel _dataModel;
    protected CbusNode _node;
    
    public AbstractEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super();
        _dataModel = dataModel;
        _node = node;
        setNV(node);
    }
    
    /**
     * The array of Node Variables
     * 0th Index is total NVs
     * so Index 1 is NV1 .. Index 255 is NV255
     */
    protected int [] _nvArray;
    
    /**
     * Make a copy of the initial NVs before editing
     * 
     * @param node to edit
     */
    private void setNV(CbusNode node) {
        _nvArray = node.getNodeNvManager().getNvArray().clone();
    }
    
    /**
     * Build the edit gui for display
     * 
     * @return the JPanel containing the edit gui
     */
    abstract public JPanel getContent();
    
    /**
     * The node table model has changed.
     * 
     * Decode the event to update the edit gui
     * 
     * @param e the change event
     */
    abstract public void tableChanged(TableModelEvent e);
}
