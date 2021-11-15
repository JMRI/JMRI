package jmri.jmrix.can.cbus.swing.modules;

import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import static jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel.NV_SELECT_COLUMN;

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
    }
    
    /**
     * Build the edit gui for display
     * 
     * @return the JPanel containing the edit gui
     */
    abstract public AbstractEditNVPane getContent();
    
    /**
     * The node table model has changed.
     * 
     * Decode the event to update the edit gui
     * 
     * @param e the change event
     */
    abstract public void tableChanged(TableModelEvent e);
    
    /**
     * Get the NV value from NV_SELECT_COLUMN
     * 
     * @param row index of NV
     * 
     * @return the NV value, 0 if NV not available yet
     */
    protected int getSelectValue(int row) {
        try {
            return (int)_dataModel.getValueAt(row - 1, NV_SELECT_COLUMN);
        } catch (NullPointerException ex) {
            // NVs are not available yet, e.g. during resync
            return 0;
        }
    }
    
    /**
     * Get the NV value from NV_SELECT_COLUMN
     * 
     * @param row index of NV
     * @param min minimum value to return
     * 
     * @return the NV value, or min if NVs not avaliable yet
     */
    protected int getSelectValue(int row, int min) {
        try {
            return (int)_dataModel.getValueAt(row - 1, NV_SELECT_COLUMN);
        } catch (NullPointerException ex) {
            // NVs are not available yet, e.g. during resync
            return min;
        }
    }
    
    /**
     * Get the value of an NV pair from NV_SELECT_COLUMN
     * 
     * @param rowHi index of hi byte NV
     * @param rowLo index of lo byte NV
     * @param min minimum value to return
     * 
     * @return the NV value, or min if NVs not avaliable yet
     */
    protected int getSelectValue(int rowHi, int rowLo, int min) {
        int hi, lo;
        try {
            hi = (int)_dataModel.getValueAt(rowHi - 1, NV_SELECT_COLUMN);
            lo = (int)_dataModel.getValueAt(rowLo - 1, NV_SELECT_COLUMN);
            return hi*256 + lo;
        } catch (NullPointerException ex) {
            // NVs are not available yet, e.g. during resync
            return min;
        }
    }
}
