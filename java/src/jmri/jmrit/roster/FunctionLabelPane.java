package jmri.jmrit.roster;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.jmrit.roster.swing.functiontable.FunctionTableCellRenderer;
import jmri.jmrit.roster.swing.functiontable.FunctionTableModel;
import jmri.jmrit.roster.swing.functiontable.FunctionTableMouseListener;
import jmri.jmrit.roster.swing.functiontable.FunctionTableRowTransferHandler;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.swing.JmriMouseListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display and edit the function labels in a RosterEntry.
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
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Randall Wood Copyright (C) 2014
 * @author Lionel Jeanson Copyright (C) 2009-2026
 * 
 */
public class FunctionLabelPane extends javax.swing.JPanel {

    private int maxfunction = 28; // default value
    private final FunctionTableModel functionTableModel;
    private JTable functionsTable;


    /**
     * This constructor allows the panel to be used in visual bean editors, but
     * should not be used in code.
     */
    public FunctionLabelPane() {
        super();
        functionTableModel = null;
    }

    public FunctionLabelPane(RosterEntry re) {
        super();
        functionTableModel = new FunctionTableModel(re);
        initGUI();
    }

    private void initGUI() {
        maxfunction = functionTableModel.getRosterEntry().getMaxFnNumAsInt();
        setLayout(new BorderLayout());
        functionsTable = new JTable(functionTableModel);
        // renderer
        FunctionTableCellRenderer renderer = new FunctionTableCellRenderer();
        functionsTable.setDefaultRenderer(Object.class, renderer);
        functionsTable.setDefaultRenderer(Boolean.class, renderer); // force boolean values to use the same renderer as other values
        // allow contextual menu on image cells
        functionsTable.addMouseListener( JmriMouseListener.adapt(new FunctionTableMouseListener(functionsTable)));
        // allow drag'n drop of rows to reorder display order
        functionsTable.setDragEnabled(true);
        functionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        functionsTable.setDropMode(DropMode.INSERT_ROWS);
        functionsTable.getTableHeader().setReorderingAllowed(false); // forbid columns reodering
        functionsTable.setTransferHandler(new FunctionTableRowTransferHandler(functionsTable));
        // column width and tooltips on columns titles
        for (int i = 0; i < functionsTable.getColumnCount(); i++) {
            if (i<functionsTable.getColumnCount()-1) { // let's keep last column large (label)
                functionsTable.getColumnModel().getColumn(i).setMaxWidth(FunctionTableCellRenderer.columnWidths[i]);
            }
            if (FunctionTableCellRenderer.columnTooltips[i] != null) {
                DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
                headerRenderer.setToolTipText(FunctionTableCellRenderer.columnTooltips[i]);
                functionsTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            }
        }
        functionsTable.setRowHeight(FunctionTableCellRenderer.CELL_HEIGHT);
        functionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JScrollPane scrollPane = new JScrollPane(functionsTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public List<String> getLabels() {
        return functionTableModel.getLabels();
    }
    
    public void setLabel(int n, String label) {
        functionTableModel.setLabel(n, label);
    }

    public String getLabel(int n) {
        return functionTableModel.getLabel(n);
    }    

    public void setLockable(int n, boolean lockable) {
        functionTableModel.setLockable(n, lockable);
    }

    public boolean getLockable(int n) {
        return functionTableModel.getLockable(n);
    } 

    /**
     * Check if panel contents differ with a RosterEntry.
     *
     * @param r the roster entry to check
     * @return true if panel contents differ; false otherwise
     */
    public boolean guiChanged(RosterEntry r) {
        RosterEntry re = functionTableModel.getRosterEntry();
        if (r == null) {
            return true;
        }
        // shunting fn
        if (r.getShuntingFunction() == null && re.getShuntingFunction() != null) {
            return true;
        }
        if (r.getShuntingFunction() != null && re.getShuntingFunction() == null) {
            return true;
        }
        if (r.getShuntingFunction() != null && re.getShuntingFunction() != null && !r.getShuntingFunction().equals(re.getShuntingFunction())) {
            return true;
        }
        // number of entries
        if (r.functionEntries.size() != re.functionEntries.size()) {
            return true;
        }
        if (r.functionEntries.size() == 0 && re.functionEntries.size() == 0) {
            return false;
        }
        // compare labels, lockable, visible, image and pressed image 
        for (int i = 0; i < maxfunction; i++) {
            if (r.getFunctionEntry(i) == null && re.getFunctionEntry(i) != null) {
                return true;   
            }
            if (r.getFunctionEntry(i) == null && re.getFunctionEntry(i) == null) {
                return false;   
            }
            if (! r.getFunctionEntry(i).equals(re.getFunctionEntry(i))) {
                return true;
            }
        }
        // compare attributes
        if ( r.getAttributeList().length != re.getAttributeList().length) {
            return true;
        }
        if ( r.getAttributeList().length > 0) {
            for (String s : r.getAttributeList()) {
                if (! r.getAttribute(s).equals(re.getAttribute(s))) {
                    return true;
                }
            }

        }
        return false;
    }

    /**
     * Update contents from a RosterEntry object
     * 
     * @param re the new contents
     */
    public void updateFromEntry(RosterEntry re) {
        functionTableModel.setRosterEntry(re);
    }

    /**
     * Update a RosterEntry object from panel contents.
     *
     * @param r the roster entry to update
     */
    public void update(RosterEntry r) {
        RosterEntry re = functionTableModel.getRosterEntry();
        // function definitions
        for (int i = 0; i < maxfunction; i++) {
            RosterFunctionEntry fn = re.getFunctionEntry(i);
            if (fn != null) {
                r.setFunctionEntry(new RosterFunctionEntry(fn));
            }
        }
        // shunting functions
        r.setShuntingFunction(functionTableModel.getRosterEntry().getShuntingFunction());
        // function display order
        r.deleteAttribute("FnDisplayOrder");
        String dos = functionTableModel.getVisibilityOrderAttributeString();        
        r.putAttribute("FnDisplayOrder", dos);        
    }

    public void dispose() {
        log.debug("dispose");
    }

    public boolean includeInPrint() {
        return print;
    }

    public void includeInPrint(boolean inc) {
        print = inc;
    }
    boolean print = false;

    public void printPane(HardcopyWriter w) {
        // if pane is empty, don't print anything
        // if (varList.size() == 0 && cvList.size() == 0) return;
        // future work needed here to print indexed CVs

        // Define column widths for name and value output.
        // Make col 2 slightly larger than col 1 and reduce both to allow for
        // extra spaces that will be added during concatenation
        int col1Width = w.getCharactersPerLine() / 2 - 3 - 5;
        int col2Width = w.getCharactersPerLine() / 2 - 3 + 5;

        try {
            // Create a string of spaces the width of the first column
            StringBuilder spaces = new StringBuilder();
            for (int i = 0; i < col1Width; i++) {
                spaces.append(" ");
            }
            // start with pane name in bold
            String heading1 = Bundle.getMessage("ColumnHeadingFunction");
            String heading2 = Bundle.getMessage("ColumnHeadingDescription");
            String s;
            int interval = spaces.length() - heading1.length();
            w.setFont(null, Font.BOLD, null);
            // write the section name and dividing line
            s = Bundle.getMessage("HeadingFunctionLabels");
            w.write(s, 0, s.length());
            w.writeBorders();
            //Draw horizontal dividing line for each Pane section
            w.writeLine(w.getCurrentVPos(), 0, w.getCurrentVPos(), w.getPrintablePagesizePoints().width);
            s = "\n";
            w.write(s, 0, s.length());

            w.setFont(null, Font.BOLD + Font.ITALIC, null);
            s = "   " + heading1 + spaces.substring(0, interval) + "   " + heading2;
            w.write(s, 0, s.length());
            w.writeBorders();
            s = "\n";
            w.write(s, 0, s.length());
            w.setFont(null, Font.PLAIN, null);

            // index over variables
            for (int i = 0; i <= maxfunction; i++) {
                String name = "" + i;
                if (functionTableModel.getRosterEntry().getFunctionLockable(i)) {
                    name = name + " (lockable)";
                }
                if (! functionTableModel.getRosterEntry().getFunctionVisible(i)) {
                    name = name + " (not visible)";
                }
                String value = functionTableModel.getRosterEntry().getFunctionLabel(i);
                //Skip Blank functions
                if (value != null) {

                    //define index values for name and value substrings
                    int nameLeftIndex = 0;
                    int nameRightIndex = name.length();
                    int valueLeftIndex = 0;
                    int valueRightIndex = value.length();
                    String trimmedName;
                    String trimmedValue;

                    // Check the name length to see if it is wider than the column.
                    // If so, split it and do the same checks for the Value
                    // Then concatenate the name and value (or the split versions thereof)
                    // before writing - if split, repeat until all pieces have been output
                    while ((valueLeftIndex < value.length()) || (nameLeftIndex < name.length())) {
                        // name split code
                        if (name.substring(nameLeftIndex).length() > col1Width) {
                            for (int j = 0; j < col1Width; j++) {
                                String delimiter = name.substring(nameLeftIndex + col1Width - j - 1,
                                        nameLeftIndex + col1Width - j);
                                if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                                    nameRightIndex = nameLeftIndex + col1Width - j;
                                    break;
                                }
                            }
                            trimmedName = name.substring(nameLeftIndex, nameRightIndex);
                            nameLeftIndex = nameRightIndex;
                            int space = spaces.length() - trimmedName.length();
                            s = "   " + trimmedName + spaces.substring(0, space);
                        } else {
                            trimmedName = name.substring(nameLeftIndex);
                            int space = spaces.length() - trimmedName.length();
                            s = "   " + trimmedName + spaces.substring(0, space);
                            name = "";
                            nameLeftIndex = 0;
                        }
                        // value split code
                        if (value.substring(valueLeftIndex).length() > col2Width) {
                            for (int j = 0; j < col2Width; j++) {
                                String delimiter = value.substring(valueLeftIndex + col2Width - j - 1, valueLeftIndex + col2Width - j);
                                if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                                    valueRightIndex = valueLeftIndex + col2Width - j;
                                    break;
                                }
                            }
                            trimmedValue = value.substring(valueLeftIndex, valueRightIndex);
                            valueLeftIndex = valueRightIndex;
                            s = s + "   " + trimmedValue;
                        } else {
                            trimmedValue = value.substring(valueLeftIndex);
                            s = s + "   " + trimmedValue;
                            valueLeftIndex = 0;
                            value = "";
                        }
                        w.write(s, 0, s.length());
                        w.writeBorders();
                        s = "\n";
                        w.write(s, 0, s.length());
                    }
                    // handle special cases
                }
            }
            s = "\n";
            w.writeBorders();
            w.write(s, 0, s.length());
            w.writeBorders();
            w.write(s, 0, s.length());
        } catch (IOException e) {
            log.warn("error during printing", e);
        }

    }

    private static final Logger log = LoggerFactory.getLogger(FunctionLabelPane.class);
}
