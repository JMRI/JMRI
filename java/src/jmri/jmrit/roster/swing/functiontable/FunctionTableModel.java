package jmri.jmrit.roster.swing.functiontable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.throttle.preferences.ThrottlesPreferences;
import jmri.util.swing.EditableResizableImagePanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A TableModel that provides a way for the user to edit details of functions of a decoder in the roster. 
 * It is used in the FunctionLabelPane.
 * 
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Lionel Jeanson 2009-2026
 */

public class FunctionTableModel extends AbstractTableModel implements PropertyChangeListener {
    private RosterEntry rosterEntry;

    // to allow easy GUI update
    public static final int COL_FN = 0; // Function number
    public static final int COL_LK = 1; // Lockable
    public static final int COL_OF = 2; // icon off
    public static final int COL_ON = 3; // icon on
    public static final int COL_VI = 4; // Visibility
    public static final int COL_SH = 5; // isShuntingFunction
    public static final int COL_LA = 6; // label
    public static final int NBCOL = 8;
    public static final int NBVISCOL = 7;   
    public static final int COL_DO = 7; // Display Order, not diplayed, but used to manage the drag'n drop order of the rows

    private static final String[] columnNames = new String[NBCOL];
    private static final Class<?>[] columnClass = new Class<?>[NBCOL];

     // row to fn mapping, to allow easy GUI update when the user reorders the rows in the table
    private int[] rowFn;
    // caches for ImagePanels to avoid creating new ones each time the table is refreshed
    private EditableResizableImagePanel[] imageOnPanels;
    private EditableResizableImagePanel[] imageOffPanels; 
    
    /**
     * Create a new FunctionTableModel for a COPY of the given RosterEntry,
     * 
     * @param re the RosterEntry on which to base this model. A copy of the RosterEntry is made so that changes to the model do not affect the original RosterEntry.
     */
    public FunctionTableModel(RosterEntry re) {
        columnNames[COL_FN]=Bundle.getMessage("FunctionButtonN");
        columnNames[COL_DO]=Bundle.getMessage("FunctionButtonDisplayOrder");
        columnNames[COL_VI]=Bundle.getMessage("FunctionButtonVisible");
        columnNames[COL_LK]=Bundle.getMessage("FunctionButtonLockable");
        columnNames[COL_OF]=Bundle.getMessage("FunctionButtonImageOff");
        columnNames[COL_ON]=Bundle.getMessage("FunctionButtonImageOn");
        columnNames[COL_SH]=Bundle.getMessage("FunctionButtonShunterFn");
        columnNames[COL_LA]=Bundle.getMessage("FunctionButtonLabel");

        columnClass[COL_FN]=Integer.class;
        columnClass[COL_DO]=Integer.class;
        columnClass[COL_VI]=Boolean.class;
        columnClass[COL_LK]=Boolean.class;
        columnClass[COL_OF]=String.class; 
        columnClass[COL_ON]=String.class; 
        columnClass[COL_SH]=Boolean.class;
        columnClass[COL_LA]=String.class;                
    
        rosterEntry = new RosterEntry(re, re.getId()+"FunctionTableModelWIP");
        initRowFn();
        initImagePanels();        
    }

    /**
     * Update the FunctionTableModel for a COPY of the given RosterEntry,
     * 
     * @param re the RosterEntry on which to base this model. A copy of the RosterEntry is made so that changes to the model do not affect the original RosterEntry.
     */
    public void setRosterEntry(RosterEntry re) {
        rosterEntry = new RosterEntry(re, re.getId()+"FunctionTableModelWIP"); // we work on a copy of the RosterEntry to avoid changing the original RosterEntry until the user clicks on "Apply" in the FunctionLabelPane
        initRowFn();
        initImagePanels();
        fireTableDataChanged();
    }

    private void initRowFn() {
        String fnDOAtt = rosterEntry.getAttribute("FnDisplayOrder");
        if (fnDOAtt != null) {
            try {
                rowFn = new ObjectMapper().readValue(fnDOAtt, int[].class );
                return;
            } catch (JsonProcessingException e) {
                log.warn("Couldn't parse FnDisplayOrder attribute ",e);
            } 
        } 
        rowFn = new int[rosterEntry.getMaxFnNumAsInt()];
        for (int i=0;i < rowFn.length; i++) {
            rowFn[i] = i; // initial mapping
        }
    }

    private void initImagePanels() {
        imageOnPanels = new EditableResizableImagePanel[rosterEntry.getMaxFnNumAsInt()];
        imageOffPanels = new EditableResizableImagePanel[rosterEntry.getMaxFnNumAsInt()];
    }

    /**
     * Return the EditableResizableImagePanel for the function image (off) for a function number.
     * 
     * @param fn the function number
     * @return the EditableResizableImagePanel
     */
    public EditableResizableImagePanel getFunctionImagePanel(int fn) {
        if (imageOffPanels[fn] == null) {
            imageOffPanels[fn] = new EditableResizableImagePanel(rosterEntry.getFunctionImage(fn), FunctionTableCellRenderer.CELL_HEIGHT, FunctionTableCellRenderer.CELL_HEIGHT);
            imageOffPanels[fn].setDropFolder(Roster.getDefault().getRosterFilesLocation());
            imageOffPanels[fn].setBackground(new java.awt.Color(0, 0, 0, 0));
            imageOffPanels[fn].setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.blue));
            imageOffPanels[fn].addMenuItemBrowseFolder(Bundle.getMessage("MediaRosterOpenSystemFileBrowserOnJMRIfnButtonsRessources"), jmri.util.FileUtil.getExternalFilename("resources/icons/functionicons"));
            imageOffPanels[fn].addPropertyChangeListener(EditableResizableImagePanel.IMAGE_PATH, evt -> {
                    // Update the model when the image path changes
                    setValueAt(evt.getNewValue(), getRowForFn(fn), FunctionTableModel.COL_OF);
                });
        }
        return imageOffPanels[fn];
    }

    /**
     * Return the EditableResizableImagePanel for the function image (on) for a function number.
     * 
     * @param fn the function number
     * @return the EditableResizableImagePanel
     */    
    public EditableResizableImagePanel getFunctionSelectedImagePanel(int fn) {
        if (imageOnPanels[fn] == null) {
            imageOnPanels[fn] = new EditableResizableImagePanel(rosterEntry.getFunctionSelectedImage(fn), FunctionTableCellRenderer.CELL_HEIGHT, FunctionTableCellRenderer.CELL_HEIGHT);
            imageOnPanels[fn].setDropFolder(Roster.getDefault().getRosterFilesLocation());
            imageOnPanels[fn].setBackground(new java.awt.Color(0, 0, 0, 0));
            imageOnPanels[fn].setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.blue));
            imageOnPanels[fn].addMenuItemBrowseFolder(Bundle.getMessage("MediaRosterOpenSystemFileBrowserOnJMRIfnButtonsRessources"), jmri.util.FileUtil.getExternalFilename("resources/icons/functionicons"));
            imageOnPanels[fn].addPropertyChangeListener(EditableResizableImagePanel.IMAGE_PATH, evt -> {
                    // Update the model when the image path changes
                    setValueAt(evt.getNewValue(), getRowForFn(fn), FunctionTableModel.COL_ON);
                });
        }
        return imageOnPanels[fn];
    }

    /**
     *  Return the function number for a row index. 
     * 
     * @param rowIndex the row index
     * @return the function number, -1 if out of bounds
     */
    public int getFnForRow(int rowIndex) {
        if (rowIndex < rowFn.length) {
            return rowFn[rowIndex];
        }
        return -1;
    }

    /**
     *  Return the row index for a function number. 
     * 
     * @param fn the function number
     * @return the row index, -1 if out of bounds
     */
    public int getRowForFn(int fn) {
        int i = 0;
        while (rowFn[i] != fn) {
            i++;
        }
        if (rowFn[i] == fn) {
            return i;
        }
        return -1;
    }

    /** 
     * Move one row from first ndex to second index.
     * Lines between the two indexes are shifted up or down to make room for the moved line.
     * 
     * @param from moving from this index
     * @param to moving to this index
     */

    public void moveRow(int from, int to) {
        log.debug("moveRow() from {} to {}", from, to);
        // reorder table display²
        int fnToMove = rowFn[from];
        if (from > to) {            
            for (int i=from; i>to; i--) {
                rowFn[i] = rowFn[i-1];
            }            
        } else {
            for (int i=from; i<to; i++) {
                rowFn[i] = rowFn[i+1];
            }
        }
        rowFn[to] = fnToMove;
        fireTableDataChanged();
        for (int i=Math.min(to, from);i<=Math.max(to, from);i++) {
            setValueAt(i, i, COL_DO );
        }        
    }

    /**
     *  Return the function visibility order attribute string to be stored in the RosterEntry.
     *  This is a JSON string representing the rowFn array, or null if the order is the default order (i.e. rowFn[i] == i for all i).
     *  At index i is the function number of the function to be shown at that position in the GUI (Throttle for instance)..
     *  The function number is the index in the RosterEntry function list. 
     * 
     * @return the attriute string to be stored in the RosterEntry, or null if not needed
     */
    public String getVisibilityOrderAttributeString() {
        // Really needed?
        boolean needed = false;
        for (int i=0; i<rowFn.length; i++) {
            if (i != rowFn[i]) {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return null;
        }
        try {
            return new ObjectMapper().writeValueAsString(rowFn);
        } catch (JsonProcessingException e) {
            log.debug("Couldn't generate JSON string",e);
        }
        return null;
    }
    
    @Override
    public int getRowCount() {
        return rosterEntry.getMaxFnNumAsInt();
    }

    @Override
    public int getColumnCount() {
        return NBVISCOL;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClass[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int fn = getFnForRow(rowIndex);
        switch (columnIndex) {
            case COL_FN:
                return fn;
            case COL_DO:
                return rowIndex; // because the table is sorted by visibility number
            case COL_VI:
                return rosterEntry.getFunctionVisible(fn);
            case COL_LK:
                return rosterEntry.getFunctionLockable(fn);
            case COL_OF:
                return rosterEntry.getFunctionImage(fn) != null ? rosterEntry.getFunctionImage(fn) : "";
            case COL_ON:
                return rosterEntry.getFunctionSelectedImage(fn) != null ? rosterEntry.getFunctionSelectedImage(fn) : "";
            case COL_SH:
                return ("F" + fn).equals(rosterEntry.getShuntingFunction());
            case COL_LA:
                return rosterEntry.getFunctionLabel(fn);                
            default:
                log.error("getValueAt(): Invalid column index: {}", columnIndex);
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
         // FN Number is not editable, display order managed by drag'n drop, 
         // images cells are not editable because they are handled by EditableResizableImagePanel
        if (columnIndex == COL_FN || columnIndex == COL_DO || columnIndex == COL_ON || columnIndex == COL_OF) {
            return false;
        }
        return true;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        int fn = getFnForRow(rowIndex);
        switch (columnIndex) {
            case COL_FN:
                // FN Number is not editable
                break;
            case COL_DO:
                // Fn Dsplay Order not editable
                break;
            case COL_VI:
                rosterEntry.setFunctionVisible(fn, (Boolean) value);
                break;
            case COL_LK:
                rosterEntry.setFunctionLockable(fn, (Boolean) value);
                break;
            case COL_OF:
                rosterEntry.setFunctionImage(fn, (String) value);
                break;
            case COL_ON:
                rosterEntry.setFunctionSelectedImage(fn, (String) value);
                break;
            case COL_SH:                
                if ((Boolean)value) {
                    rosterEntry.setShuntingFunction("F" + fn);
                } else {
                    if (rosterEntry.getShuntingFunction() != null && rosterEntry.getShuntingFunction().equals("F" + fn)) {
                        rosterEntry.setShuntingFunction(null);
                    }
                }
                fireTableDataChanged(); // force refresh all lines                                
                break;
            case COL_LA:
                rosterEntry.setFunctionLabel(fn, (String) value);
                break;                
            default:
                log.error("setValueAt(): Invalid column index: {}", columnIndex);
        }
        fireTableCellUpdated(rowIndex, columnIndex); // Notify that the cell has changed
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    /**
     *  Return list of all function label ordered by function number 
     * 
     * @return a list of String
     */
    public List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (int fn = 0; fn < getRowCount(); fn++) {
            labels.add(getLabel(fn));
        }
        return labels;
    }

    /**
     *  Return the function label for a function number 
     * 
     * @param fn the function number
     * 
     * @return a list of String
     */    
    public String getLabel(int fn) {
        int idx = getRowForFn(fn);
        if (idx != -1) {
            return rosterEntry.getFunctionLabel(fn);
        }
        log.error("getLabel(): no row index for fn {}", fn);
        return null;
    }

    /**
     *  Set the function label for a function number 
     * 
     * @param fn the function number
     * @param label the label to set
     * 
     */
    public void setLabel(int fn, String label) {
        rosterEntry.setFunctionLabel(fn, label);
        int idx = getRowForFn(fn);
        if (idx != -1) {
            fireTableCellUpdated(idx, COL_LA); // Notify that the label column has changed for row n
            return;
        }
        log.warn("setLabel(): no row index for fn {}", fn);
        fireTableDataChanged();
    }

    /**
     *  Return the function lockable flag for a function number 
     * 
     * @param fn the function number
     * 
     * @return the lockable flag
     */
    public boolean getLockable(int fn) {
        return rosterEntry.getFunctionLockable(fn);
    }

    /**
     *  Set a function lockable flag for a function number 
     * 
     * @param fn the function number
     * @param lockable the lockable flag to set
     * 
     */
    public void setLockable(int fn, boolean lockable) {
        rosterEntry.setFunctionLockable(fn, lockable);
        int idx = getRowForFn(fn);
        if (idx != -1) {
        fireTableCellUpdated(idx, COL_LK); // Notify that the lockable column has changed for row n
            return;
        }
        log.warn("setLockable(): no row index for fn {}", fn);
        fireTableDataChanged();        
    }

    /** 
     * Returns the RosterEntry being edited by that table model
     * 
     * @return the RosterEntry being edited by that table model
     */
    public RosterEntry getRosterEntry() {
        return rosterEntry;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) {
            return;
        }
        log.debug("Property change event received {} / {}", evt.getPropertyName(), evt.getNewValue());        
        if (ThrottlesPreferences.prefPopertyName.equals(evt.getPropertyName())) {
            fireTableDataChanged();
        }   
    }

    private static final Logger log = LoggerFactory.getLogger(FunctionTableModel.class);
}
