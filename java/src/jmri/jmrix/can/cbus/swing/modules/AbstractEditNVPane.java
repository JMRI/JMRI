package jmri.jmrix.can.cbus.swing.modules;

import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import static jmri.jmrix.can.cbus.node.CbusNodeConstants.*;
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
    protected int _fwMaj = -1;
    protected int _fwMin = -1;
    protected int _fwBuild = -1;

    
    public AbstractEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super();
        _dataModel = dataModel;
        _node = node;
        _fwMaj = node.getNodeParamManager().getParameter(MAJOR_VER_IDX);
        _fwMin = node.getNodeParamManager().getParameter(MINOR_VER_IDX);
        _fwBuild = node.getNodeParamManager().getParameter(BETA_REV_IDX);
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
    protected int getSelectValue8(int row) {
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
     * @return the NV value, or min if NVs not available yet
     */
    protected int getSelectValue8(int row, int min) {
        try {
            int val = (int)_dataModel.getValueAt(row - 1, NV_SELECT_COLUMN);
            if (val < min) {
                return min;
            } else {
                return val;
            }
        } catch (NullPointerException ex) {
            // NVs are not available yet, e.g. during resync
            return min;
        }
    }
    
    /**
     * Get the NV value from NV_SELECT_COLUMN
     * 
     * @param row index of NV
     * @param min minimum value to return
     * @param max maximum value to return
     * 
     * @return the NV value, or min if NVs not available yet
     */
    protected int getSelectValue8(int row, int min, int max) {
        try {
            int val = (int)_dataModel.getValueAt(row - 1, NV_SELECT_COLUMN);
            if (val < min) {
                return min;
            } else if (val > max) {
                return max;
            } else {
                return val;
            }
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
     * @param max maximum value to return
     * 
     * @return the NV value, or min if NVs not available yet
     */
    protected int getSelectValue16(int rowHi, int rowLo, int min, int max) {
        int hi, lo, val;
        try {
            hi = (int)_dataModel.getValueAt(rowHi - 1, NV_SELECT_COLUMN);
            lo = (int)_dataModel.getValueAt(rowLo - 1, NV_SELECT_COLUMN);
            val = hi*256 + lo;
            if (val < min) {
                return min;
            } else if (val > max) {
                return max;
            } else {
                return val;
            }
        } catch (NullPointerException ex) {
            // NVs are not available yet, e.g. during resync
            return min;
        }
    }
    
    /**
     * Get the value of a 4-byte (32-bit) NV from NV_SELECT_COLUMN
     * 
     * Hardware should return a count in range 0 .. max positive integer
     * 
     * @param rowT index of top (MSB) byte
     * 
     * @return the NV value, or 0 if NVs not available yet
     */
    protected int getSelectValue32(int rowT) {
        int t, u, h, l;
        try {
            t = (int)_dataModel.getValueAt(rowT - 1, NV_SELECT_COLUMN);
            u = (int)_dataModel.getValueAt(rowT, NV_SELECT_COLUMN);
            h = (int)_dataModel.getValueAt(rowT + 1, NV_SELECT_COLUMN);
            l = (int)_dataModel.getValueAt(rowT + 2, NV_SELECT_COLUMN);
            int val = ((t*256 + u)*256 + h)*256 + l;
            if (val < 0) {
                return 0;
            } else {
                return val;
            }
        } catch (NullPointerException ex) {
            // NVs are not available yet, e.g. during resync
            return 0;
        }
    }

//    private final static Logger log = LoggerFactory.getLogger(AbstractEditNVPane.class);

}
