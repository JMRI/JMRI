// PaneProgPane.java

package jmri.jmrit.symbolicprog.tabbedframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.table.*;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.*;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.jdom.LocaleSelector;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * Provides the individual panes for the TabbedPaneProgrammer.
 * Note that this is not only the panes carrying variables, but also the
 * special purpose panes for the CV table, etc.
 *<P>
 * This class implements PropertyChangeListener so that it can be notified
 * when a variable changes its busy status at the end of a programming read/write operation
 *
 * There are four read and write operation types, all of which have to be handled carefully:
 * <DL>
 * <DT>Write Changes<DD>This must write changes that occur after the operation
 *                      starts, because the act of writing a variable/CV may
 *                      change another.  For example, writing CV 1 will mark CV 29 as changed.
 *           <P>The definition of "changed" is operationally in the
 *              {@link jmri.jmrit.symbolicprog.VariableValue#isChanged} member function.
 *
 * <DT>Write All<DD>Like write changes, this might have to go back and re-write a variable
 *                  depending on what has previously happened.  It should write every
 *              variable (at least) once.
 * <DT>Read All<DD>This should read every variable once.
 * <DT>Read Changes<DD>This should read every variable that's marked as changed.
 *          Currently, we use a common definition of changed with the write operations,
 *      and that someday might have to change.
 *
 * </DL>
 *
 * @author    Bob Jacobsen   Copyright (C) 2001, 2003, 2004, 2005, 2006
 * @author    D Miller Copyright 2003
 * @author    Howard G. Penny   Copyright (C) 2005
 * @author    Dave Heap   Copyright (C) 2014
 * @version   $Revision$
 * @see       jmri.jmrit.symbolicprog.VariableValue#isChanged
 *
 */
public class PaneProgPane extends javax.swing.JPanel
    implements java.beans.PropertyChangeListener  {

    protected CvTableModel _cvModel;
    IndexedCvTableModel _indexedCvModel;
    protected VariableTableModel _varModel;
    protected PaneContainer container;
    protected RosterEntry rosterEntry;
        
    boolean _cvTable;
    
    protected JPanel bottom;

    transient ItemListener l1;
    protected transient ItemListener l2;
    transient ItemListener l3;
    protected transient ItemListener l4;
    transient ItemListener l5;
    transient ItemListener l6;
    
    boolean isCvTablePane = false;

    String mName = "";
    
    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public PaneProgPane() {}

    /**
     * Construct the Pane from the XML definition element.
     *
     * @param name  Name to appear on tab of pane
     * @param pane  The JDOM Element for the pane definition
     * @param cvModel Already existing TableModel containing the CV definitions
     * @param icvModel Already existing TableModel containing the Indexed CV definitions
     * @param varModel Already existing TableModel containing the variable definitions
     * @param modelElem "model" element from the Decoder Index, used to check what decoder options are present.
     * @param pRosterEntry The current roster entry, used to get sound labels.
     */
    @SuppressWarnings("unchecked")
    public PaneProgPane(PaneContainer parent, String name, Element pane, CvTableModel cvModel, IndexedCvTableModel icvModel, VariableTableModel varModel, Element modelElem, RosterEntry pRosterEntry) {

        container = parent;
        mName = name;
        _cvModel = cvModel;
        _indexedCvModel = icvModel;
        _varModel = varModel;
        rosterEntry = pRosterEntry;
        
        // when true a cv table with compare was loaded into pane
        _cvTable = false;

        // This is a JPanel containing a JScrollPane, containing a
        // laid-out JPanel
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

        // Add tooltip if present
        setToolTipText(jmri.util.jdom.LocaleSelector.getAttribute(pane, "tooltip"));
        
        // find out whether to display "label" (false) or "item" (true)
        boolean showItem = false;
        Attribute nameFmt = pane.getAttribute("nameFmt");
        if (nameFmt!= null && nameFmt.getValue().equals("item")) {
            log.debug("Pane "+name+" will show items, not labels, from decoder file");
            showItem = true;
        }
        // put the columns left to right in a panel
        JPanel p = new JPanel();
        panelList.add(p);
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));

        // handle the xml definition
        // for all "column" elements ...
        List<Element> colList = pane.getChildren("column");
        for (int i=0; i<colList.size(); i++) {
            // load each column
            p.add(newColumn( ((colList.get(i))), showItem, modelElem));
        }
        // for all "row" elements ...
        List<Element> rowList = pane.getChildren("row");
        for (int i=0; i<rowList.size(); i++) {
            // load each row
            p.add(newRow( ((rowList.get(i))), showItem, modelElem));
        }
        // for all "grid" elements ...
        List<Element> gridList = pane.getChildren("grid");
        for (int i=0; i<gridList.size(); i++) {
            // load each grid
            p.add(newGrid( ((gridList.get(i))), showItem, modelElem));
        }
        // for all "group" elements ...
        List<Element> groupList = pane.getChildren("group");
        for (int i=0; i<groupList.size(); i++) {
            // load each group
            p.add(newGroup( ((groupList.get(i))), showItem, modelElem));
        }

        // add glue to the right to allow resize - but this isn't working as expected? Alignment?
        add(Box.createHorizontalGlue());

        add(new JScrollPane(p));

        // add buttons in a new panel
        bottom = new JPanel();
        panelList.add(p);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));

        // enable read buttons, if possible, and
        // set their tool tips
        enableReadButtons();

        // add read button listeners
        readChangesButton.addItemListener(l1 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    readChangesButton.setText(SymbolicProgBundle.getMessage("ButtonStopReadChangesSheet"));
                    if (container.isBusy() == false) {
                        prepReadPane(true);
                        prepGlassPane(readChangesButton);
                        container.getBusyGlassPane().setVisible(true);
                        readPaneChanges();
                    }
                } else {
                    stopProgramming();
                    readChangesButton.setText(SymbolicProgBundle.getMessage("ButtonReadChangesSheet"));
                    if (container.isBusy()) {
                        readChangesButton.setEnabled(false);
                    }
                }
            }
        });
        readAllButton.addItemListener(l2 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    readAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopReadSheet"));
                    if (container.isBusy() == false) {
                        prepReadPane(false);
                        prepGlassPane(readAllButton);
                        container.getBusyGlassPane().setVisible(true);
                        readPaneAll();
                    }
                } else {
                    stopProgramming();
                    readAllButton.setText(SymbolicProgBundle.getMessage("ButtonReadFullSheet"));
                    if (container.isBusy()) {
                        readAllButton.setEnabled(false);
                    }
                }
            }
        });

        writeChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipWriteHighlightedSheet"));
        writeChangesButton.addItemListener(l3 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    writeChangesButton.setText(SymbolicProgBundle.getMessage("ButtonStopWriteChangesSheet"));
                    if (container.isBusy() == false) {
                        prepWritePane(true);
                        prepGlassPane(writeChangesButton);
                        container.getBusyGlassPane().setVisible(true);
                        writePaneChanges();
                    }
                } else {
                    stopProgramming();
                    writeChangesButton.setText(SymbolicProgBundle.getMessage("ButtonWriteChangesSheet"));
                    if (container.isBusy()) {
                        writeChangesButton.setEnabled(false);
                    }
                }
            }
        });

        writeAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipWriteAllSheet"));
        writeAllButton.addItemListener(l4 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopWriteSheet"));
                    if (container.isBusy() == false) {
                        prepWritePane(false);
                        prepGlassPane(writeAllButton);
                        container.getBusyGlassPane().setVisible(true);
                        writePaneAll();
                    }
                } else {
                    stopProgramming();
                    writeAllButton.setText(SymbolicProgBundle.getMessage("ButtonWriteFullSheet"));
                    if (container.isBusy()) {
                        writeAllButton.setEnabled(false);
                    }
                }
            }
        });
  
        // enable confirm buttons, if possible, and
        // set their tool tips
        enableConfirmButtons();
        
        // add confirm button listeners
        confirmChangesButton.addItemListener(l5 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    confirmChangesButton.setText(SymbolicProgBundle.getMessage("ButtonStopConfirmChangesSheet"));
                    if (container.isBusy() == false) {
                        prepConfirmPane(true);
                        prepGlassPane(confirmChangesButton);
                        container.getBusyGlassPane().setVisible(true);
                        confirmPaneChanges();
                    }
                } else {
                    stopProgramming();
                    confirmChangesButton.setText(SymbolicProgBundle.getMessage("ButtonConfirmChangesSheet"));
                    if (container.isBusy()) {
                        confirmChangesButton.setEnabled(false);
                    }
                }
            }
        });
        confirmAllButton.addItemListener(l6 = new ItemListener() {
            public void itemStateChanged (ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    confirmAllButton.setText(SymbolicProgBundle.getMessage("ButtonStopConfirmSheet"));
                    if (container.isBusy() == false) {
                        prepConfirmPane(false);
                        prepGlassPane(confirmAllButton);
                        container.getBusyGlassPane().setVisible(true);
                        confirmPaneAll();
                    }
                } else {
                    stopProgramming();
                    confirmAllButton.setText(SymbolicProgBundle.getMessage("ButtonConfirmFullSheet"));
                    if (container.isBusy()) {
                        confirmAllButton.setEnabled(false);
                    }
                }
            }
        });

//      Only add change buttons to CV tables
        bottom.add(readChangesButton);
        bottom.add(writeChangesButton);
        if (_cvTable)
        	bottom.add(confirmChangesButton);
        bottom.add(readAllButton);
        bottom.add(writeAllButton);
        if (_cvTable)
        	bottom.add(confirmAllButton);

        // don't show buttons if no programmer at all
        if (_cvModel.getProgrammer()!= null) add(bottom);
    }

    public String getName() { return mName; }
    public String toString() { return getName(); }
    
    /**
     * Enable the read all and read changes button if possible.
     * This checks to make sure this is appropriate, given
     * the attached programmer's capability.
     */
    void enableReadButtons() {
        readChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipReadChangesSheet"));
        readAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipReadAllSheet"));
        if (_cvModel.getProgrammer()!= null
            && !_cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the buttons
            readChangesButton.setEnabled(false);
            readAllButton.setEnabled(false);
            // set tooltip to explain why
            readChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipNoRead"));
            readAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipNoRead"));
        } else {
            readChangesButton.setEnabled(true);
            readAllButton.setEnabled(true);
        }
    }

    /**
     * Enable the compare all and compare changes button if possible.
     * This checks to make sure this is appropriate, given
     * the attached programmer's capability.
     */
    void enableConfirmButtons() {
        confirmChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipConfirmChangesSheet"));
        confirmAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipConfirmAllSheet"));
        if (_cvModel.getProgrammer()!= null
            && !_cvModel.getProgrammer().getCanRead()) {
            // can't read, disable the buttons
            confirmChangesButton.setEnabled(false);
            confirmAllButton.setEnabled(false);
            // set tooltip to explain why
            confirmChangesButton.setToolTipText(SymbolicProgBundle.getMessage("TipNoRead"));
            confirmAllButton.setToolTipText(SymbolicProgBundle.getMessage("TipNoRead"));
        } else {
            confirmChangesButton.setEnabled(true);
            confirmAllButton.setEnabled(true);
        }
    }

    /**
     * This remembers the variables on this pane for the Read/Write sheet
     * operation.  They are stored as a list of Integer objects, each of which
     * is the index of the Variable in the VariableTable.
     */
    List<Integer> varList = new ArrayList<Integer>();
    int varListIndex;
    /**
     * This remembers the CVs on this pane for the Read/Write sheet
     * operation.  They are stored as a set of Integer objects, each of which
     * is the index of the CV in the CVTable. Note that variables are handled
     * separately, and the CVs that are represented by variables are not
     * entered here.  So far (sic), the only use of this is for the cvtable rep.
     */
    protected TreeSet<Integer> cvList = new TreeSet<Integer>(); //  TreeSet is iterated in order
    protected Iterator<Integer> cvListIterator;
    /**
     * This remembers the indexed CVs on this pane for the Read/Write sheet
     * operation.  They are stored as a list of Integer objects, each of which
     * is the index of the indexed CV in the VariableTable. This is done so that
     * we can read/write them as a variable.  So far (sic), the only use of this is
     * for the IndexedCvTable rep.
     */
    List<Integer> indexedCvList = new ArrayList<Integer>();
    int indexedCvListIndex;

    protected JToggleButton readChangesButton  = new JToggleButton(SymbolicProgBundle.getMessage("ButtonReadChangesSheet"));
    protected JToggleButton readAllButton      = new JToggleButton(SymbolicProgBundle.getMessage("ButtonReadFullSheet"));
    protected JToggleButton writeChangesButton = new JToggleButton(SymbolicProgBundle.getMessage("ButtonWriteChangesSheet"));
    protected JToggleButton writeAllButton     = new JToggleButton(SymbolicProgBundle.getMessage("ButtonWriteFullSheet"));
    JToggleButton confirmChangesButton = new JToggleButton(SymbolicProgBundle.getMessage("ButtonConfirmChangesSheet"));
    JToggleButton confirmAllButton     = new JToggleButton(SymbolicProgBundle.getMessage("ButtonConfirmFullSheet"));

    /**
     * Estimate the number of CVs that will be accessed when
     * reading or writing the contents of this pane.
     *
     * @param read true if counting for read, false for write
     * @param changes true if counting for a *Changes operation;
     *          false, if counting for a *All operation
     * @return the total number of CV reads/writes needed for this pane
     */

    public int countOpsNeeded(boolean read, boolean changes) {
        Set<Integer> set = new HashSet<Integer>(cvList.size()+varList.size()+50);
        return makeOpsNeededSet(read, changes, set).size();
    }

    /**
     * Produce a set of CVs that will be accessed when
     * reading or writing the contents of this pane.
     *
     * @param read true if counting for read, false for write
     * @param changes true if counting for a *Changes operation;
     *          false, if counting for a *All operation
     * @param set The set to fill.  Any CVs already in here will
     *      not be duplicated, which provides a way to aggregate
     *      a set of CVs across multiple panes.
     * @return the same set as the parameter, for convenient
     *      chaining of operations.
     */
    public Set<Integer> makeOpsNeededSet(boolean read, boolean changes, Set<Integer> set) {

        // scan the variable list
        for (int i =0; i<varList.size(); i++) {

            int varNum = varList.get(i).intValue();
            VariableValue var = _varModel.getVariable( varNum );

            // must decide whether this one should be counted

            if ( !changes ||
                    ( changes && var.isChanged()) ) {

                CvValue[] cvs = var.usesCVs();
                for (int j = 0; j<cvs.length; j++) {
                    // always of interest
                    CvValue cv = cvs[j];
                    if (!changes || VariableValue.considerChanged(cv))
                        set.add( Integer.valueOf(cv.number()));
                }
            }
        }

        return set;
    }

    private void prepGlassPane(AbstractButton activeButton) {
        container.prepGlassPane(activeButton);
    }

    void enableButtons(boolean stat) {
        if (stat) {
            enableReadButtons();
            enableConfirmButtons();
        } else {
            readChangesButton.setEnabled(stat);
            readAllButton.setEnabled(stat);
            confirmChangesButton.setEnabled(stat);
            confirmAllButton.setEnabled(stat);
        }
        writeChangesButton.setEnabled(stat);
        writeAllButton.setEnabled(stat);
    }

    boolean justChanges;

    /**
     * Invoked by "Read changes on sheet" button, this sets in motion a
     * continuing sequence of "read" operations on the
     * variables & CVs in the Pane.  Only variables in states
     * marked as "changed" will be read.
     *
     * @return true is a read has been started, false if the pane is complete.
     */
    public boolean readPaneChanges() {
        if (log.isDebugEnabled()) log.debug("readPane starts with "
                                            +varList.size()+" vars, "
                                            +cvList.size()+" cvs "
                                            +indexedCvList.size()+" indexed cvs" );
        prepReadPane(true);
        return nextRead();
    }

    /**
     * Prepare this pane for a read operation.
     * <P>The read mechanism only reads
     * variables in certain states (and needs to do that to handle error
     * processing right now), so this is implemented by first
     * setting all variables and CVs on this pane to TOREAD via this method
     *
     */
    public void prepReadPane(boolean onlyChanges) {
        if (log.isDebugEnabled()) log.debug("start prepReadPane with onlyChanges="+onlyChanges);
        justChanges = onlyChanges;
        
        if (isCvTablePane) setCvListFromTable();  // make sure list of CVs up to date if table
        
        enableButtons(false);
        if (justChanges == true) {
            readChangesButton.setEnabled(true);
            readChangesButton.setSelected(true);
        } else {
            readAllButton.setSelected(true);
            readAllButton.setEnabled(true);
        }
        if (container.isBusy() == false) {
            container.enableButtons(false);
        }
        setToRead(justChanges, true);
        varListIndex = 0;
        cvListIterator = cvList.iterator();
        indexedCvListIndex = 0;
    }

    /**
     * Invoked by "Read Full Sheet" button, this sets in motion a
     * continuing sequence of "read" operations on the
     * variables & CVs in the Pane.  The read mechanism only reads
     * variables in certain states (and needs to do that to handle error
     * processing right now), so this is implemented by first
     * setting all variables and CVs on this pane to TOREAD
     * in prepReadPaneAll, then starting the execution.
     *
     * @return true is a read has been started, false if the pane is complete.
     */
    public boolean readPaneAll() {
        if (log.isDebugEnabled()) log.debug("readAllPane starts with "
                                            +varList.size()+" vars, "
                                            +cvList.size()+" cvs "
                                            +indexedCvList.size()+" indexed cvs" );
        prepReadPane(false);
        // start operation
        return nextRead();
    }

    /**
     * Set the "ToRead" parameter in all variables and CVs on this pane
     * @param justChanges true if this is read changes, false if read all
     * @param startProcess true if this is the start of processing, false if cleaning up at end
     */
    void setToRead(boolean justChanges, boolean startProcess) {
        if (!container.isBusy() ||  // the frame has already setToRead
           (!startProcess)) {  // we want to setToRead false if the pane's process is being stopped
            for (int i = 0; i < varList.size(); i++) {
                int varNum = varList.get(i).intValue();
                VariableValue var = _varModel.getVariable(varNum);
                if (justChanges) {
                    if (var.isChanged()) {
                        var.setToRead(startProcess);
                    } else {
                        var.setToRead(false);
                    }
                } else {
                    var.setToRead(startProcess);
                }
            }

            if (isCvTablePane) setCvListFromTable();  // make sure list of CVs up to date if table
            for (int cvNum : cvList) {
                CvValue cv = _cvModel.getCvByRow(cvNum);
                if (justChanges) {
                    if (VariableValue.considerChanged(cv)) {
                        cv.setToRead(startProcess);
                    } else {
                        cv.setToRead(false);
                    }
                } else {
                    cv.setToRead(startProcess);
                }
            }

            for (int i = 0; i < indexedCvList.size(); i++) {
                CvValue icv = _indexedCvModel.getCvByRow(i);
                if (justChanges) {
                    if (VariableValue.considerChanged(icv)) {
                        icv.setToRead(startProcess);
                    } else {
                        icv.setToRead(false);
                    }
                } else {
                    icv.setToRead(startProcess);
                }
            }
        }
    }

    /**
     * Set the "ToWrite" parameter in all variables and CVs on this pane
     * @param justChanges true if this is read changes, false if read all
     * @param startProcess true if this is the start of processing, false if cleaning up at end
     */
    void setToWrite(boolean justChanges, boolean startProcess) {
        if (log.isDebugEnabled()) log.debug("start setToWrite method with "+justChanges+","+startProcess);
        if (!container.isBusy() ||  // the frame has already setToWrite
           (!startProcess)) {  // we want to setToWrite false if the pane's process is being stopped
           log.debug("about to start setToWrite of varList");
           for (int i = 0; i < varList.size(); i++) {
               int varNum = varList.get(i).intValue();
               VariableValue var = _varModel.getVariable(varNum);
               if (justChanges) {
                   if (var.isChanged()) {
                       var.setToWrite(startProcess);
                   } else {
                       var.setToWrite(false);
                   }
               } else {
                   var.setToWrite(startProcess);
               }
           }
           
           log.debug("about to start setToWrite of cvList");
           if (isCvTablePane) setCvListFromTable();  // make sure list of CVs up to date if table
           for (int cvNum : cvList) {
               CvValue cv = _cvModel.getCvByRow(cvNum);
               if (justChanges) {
                   if (VariableValue.considerChanged(cv)) {
                       cv.setToWrite(startProcess);
                   } else {
                       cv.setToWrite(false);
                   }
               } else {
                   cv.setToWrite(startProcess);
               }
           }
           
           log.debug("about to start setToWrite of indexedCvList");
           for (int i = 0; i < indexedCvList.size(); i++) {
               CvValue icv = _indexedCvModel.getCvByRow(i);
               if (justChanges) {
                   if (VariableValue.considerChanged(icv)) {
                       icv.setToWrite(startProcess);
                   } else {
                       icv.setToWrite(false);
                   }
               } else {
                   icv.setToWrite(startProcess);
               }
           }
       }
       log.debug("end setToWrite method");
    }

    void executeRead(VariableValue var) {
        setBusy(true);
        // var.setToRead(false);  // variables set this themselves
        if (_programmingVar != null) log.error("listener already set at read start");
        _programmingVar = var;
        _read = true;
        // get notified when that state changes so can repeat
        _programmingVar.addPropertyChangeListener(this);
        // and make the read request
        if (justChanges) {
            _programmingVar.readChanges();
        } else {
            _programmingVar.readAll();
        }
    }

    void executeWrite(VariableValue var) {
        setBusy(true);
        // var.setToWrite(false);   // variables reset themselves when done
        if (_programmingVar != null) log.error("listener already set at write start");
        _programmingVar = var;
        _read = false;
        // get notified when that state changes so can repeat
        _programmingVar.addPropertyChangeListener(this);
        // and make the write request
        if (justChanges) {
            _programmingVar.writeChanges();
        } else {
            _programmingVar.writeAll();
        }
    }

    /**
     * If there are any more read operations to be done on this pane,
     * do the next one.
     * <P>
     * Each invocation of this method reads one variable or CV; completion
     * of that request will cause it to happen again, reading the next one, until
     * there's nothing left to read.
     * <P>
     * @return true is a read has been started, false if the pane is complete.
     */
    boolean nextRead() {
        // look for possible variables
        if (log.isDebugEnabled()) log.debug("nextRead scans "+varList.size()+" variables");
        while ((varList.size() >= 0) && (varListIndex < varList.size())){
            int varNum = varList.get(varListIndex).intValue();
            int vState = _varModel.getState( varNum );
            VariableValue var = _varModel.getVariable(varNum);
            if (log.isDebugEnabled()) log.debug("nextRead var index "+varNum+" state "+VariableValue.stateNameFromValue(vState)+" isToRead: "+var.isToRead()+" label: "+var.label());
            varListIndex++;
            if (var.isToRead()) {
                if (log.isDebugEnabled()) log.debug("start read of variable "+_varModel.getLabel(varNum));
                executeRead(var);

                if (log.isDebugEnabled()) log.debug("return from starting var read");
                // the request may have instantaneously been satisfied...
                return true;  // only make one request at a time!
            }
        }
        // found no variables needing read, try CVs
        if (log.isDebugEnabled()) log.debug("nextRead scans "+cvList.size()+" CVs");
        while (cvListIterator!= null && cvListIterator.hasNext()) {
            int cvNum = cvListIterator.next();
            CvValue cv = _cvModel.getCvByRow(cvNum);
            if (log.isDebugEnabled()) log.debug("nextRead cv index "+cvNum+" state "+cv.getState());

            if (cv.isToRead()) {  // always read UNKNOWN state
               if (log.isDebugEnabled()) log.debug("start read of cv "+cvNum);
                setBusy(true);
                if (_programmingCV != null) log.error("listener already set at read start");
                _programmingCV = _cvModel.getCvByRow(cvNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingCV.addPropertyChangeListener(this);
                // and make the read request
                // _programmingCV.setToRead(false);  // CVs set this themselves
                _programmingCV.read(_cvModel.getStatusLabel());
                if (log.isDebugEnabled()) log.debug("return from starting CV read");
                // the request may have instantateously been satisfied...
                return true;  // only make one request at a time!
            }
        }
        // found no CVs needing read, try indexed CVs
        if (log.isDebugEnabled()) log.debug("nextRead scans "+indexedCvList.size()+" indexed CVs");
        while ((indexedCvList.size() >= 0) && (indexedCvListIndex < indexedCvList.size())) {
            int indxVarNum = indexedCvList.get(indexedCvListIndex).intValue();
            int indxState = _varModel.getState(indxVarNum);
            if (log.isDebugEnabled()) log.debug(
                "nextRead indexed cv @ row index " + indexedCvListIndex + " state " + indxState);
            VariableValue iCv = _varModel.getVariable(indxVarNum);
            indexedCvListIndex++;
            if (iCv.isToRead()) {
                String sz = "start read of indexed cv " +
                    (_indexedCvModel.getCvByRow(indexedCvListIndex-1)).cvName();
                if (log.isDebugEnabled()) log.debug(sz);
                setBusy(true);
                if (_programmingIndexedCV != null) log.error(
                    "listener already set at read start");
                _programmingIndexedCV = _varModel.getVariable(indxVarNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingIndexedCV.addPropertyChangeListener(this);
                // and make the read request
                // _programmingIndexedCV.setToRead(false);  // CVs set this themselves
                _programmingIndexedCV.readAll();
                if (log.isDebugEnabled()) log.debug(
                    "return from starting indexed CV read");
                // the request may have instantateously been satisfied...
                return true; // only make one request at a time!
            }
        }
        // nothing to program, end politely
        if (log.isDebugEnabled()) log.debug("nextRead found nothing to do");
        readChangesButton.setSelected(false);
        readAllButton.setSelected(false);  // reset both, as that's final state we want
        setBusy(false);
        container.paneFinished();
        return false;
    }
    
    /**
     * If there are any more compare operations to be done on this pane,
     * do the next one.
     * <P>
     * Each invocation of this method compare one CV; completion
     * of that request will cause it to happen again, reading the next one, until
     * there's nothing left to read.
     * <P>
     * @return true is a compare has been started, false if the pane is complete.
     */
    boolean nextConfirm() {
        // look for possible CVs
        while (cvListIterator!= null && cvListIterator.hasNext()) {
            int cvNum = cvListIterator.next();
            CvValue cv = _cvModel.getCvByRow(cvNum);
            if (log.isDebugEnabled()) log.debug("nextConfirm cv index "+cvNum+" state "+cv.getState());

            if (cv.isToRead()) {
               if (log.isDebugEnabled()) log.debug("start confirm of cv "+cvNum);
                setBusy(true);
                if (_programmingCV != null) log.error("listener already set at confirm start");
                _programmingCV = _cvModel.getCvByRow(cvNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingCV.addPropertyChangeListener(this);
                // and make the compare request
                _programmingCV.confirm(_cvModel.getStatusLabel());
                if (log.isDebugEnabled()) log.debug("return from starting CV confirm");
                // the request may have instantateously been satisfied...
                return true;  // only make one request at a time!
            }
        }
        // found no CVs needing read, try indexed CVs
        while ((indexedCvList.size() >= 0) && (indexedCvListIndex < indexedCvList.size())) {
            int indxVarNum = indexedCvList.get(indexedCvListIndex).intValue();
            int indxState = _varModel.getState(indxVarNum);
            if (log.isDebugEnabled()) log.debug(
                "nextRead indexed cv @ row index " + indexedCvListIndex + " state " + indxState);
            VariableValue iCv = _varModel.getVariable(indxVarNum);
            indexedCvListIndex++;
            if (iCv.isToRead()) {
                String sz = "start confirm of indexed cv " +
                    (_indexedCvModel.getCvByRow(indexedCvListIndex-1)).cvName();
                if (log.isDebugEnabled()) log.debug(sz);
                setBusy(true);
                if (_programmingIndexedCV != null) log.error(
                    "listener already set at confirm start");
                _programmingIndexedCV = _varModel.getVariable(indxVarNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingIndexedCV.addPropertyChangeListener(this);
                // and make the compare request
                _programmingIndexedCV.confirmAll();
                if (log.isDebugEnabled()) log.debug(
                    "return from starting indexed CV confirm");
                // the request may have instantateously been satisfied...
                return true; // only make one request at a time!
            }
        }
        // nothing to program, end politely
        if (log.isDebugEnabled()) log.debug("nextConfirm found nothing to do");
        confirmChangesButton.setSelected(false);
        confirmAllButton.setSelected(false);  // reset both, as that's final state we want
        setBusy(false);
        container.paneFinished();
        return false;
    }

    /**
     * Invoked by "Write changes on sheet" button, this sets in motion a
     * continuing sequence of "write" operations on the
     * variables in the Pane.  Only variables in isChanged states
     * are written; other states don't
     * need to be.
     * <P>
     * Returns true if a write has been started, false if the pane is complete.
     */
    public boolean writePaneChanges() {
        if (log.isDebugEnabled()) log.debug("writePaneChanges starts");
        prepWritePane(true);
        boolean val = nextWrite();
        if (log.isDebugEnabled()) log.debug("writePaneChanges returns "+val);
        return val;
    }

    /**
     * Invoked by "Write full sheet" button to write all CVs.
     * <P>
     * Returns true if a write has been started, false if the pane is complete.
     */
    public boolean writePaneAll() {
        prepWritePane(false);
        return nextWrite();
    }

    /**
     * Prepare a "write full sheet" operation.
     */
    public void prepWritePane(boolean onlyChanges) {
        if (log.isDebugEnabled()) log.debug("start prepWritePane with "+onlyChanges);
        justChanges = onlyChanges;
        enableButtons(false);

        if (isCvTablePane) setCvListFromTable();  // make sure list of CVs up to date if table

        if (justChanges == true) {
            writeChangesButton.setEnabled(true);
            writeChangesButton.setSelected(true);
        } else {
            writeAllButton.setSelected(true);
            writeAllButton.setEnabled(true);
        }
        if (container.isBusy() == false) {
            container.enableButtons(false);
        }
        setToWrite(justChanges, true);
        varListIndex = 0;
        
        cvListIterator = cvList.iterator();

        indexedCvListIndex = 0;
        log.debug("end prepWritePane");
    }

    boolean nextWrite() {
        log.debug("start nextWrite");
        // look for possible variables
        while ((varList.size() >= 0) && (varListIndex < varList.size())){
            int varNum = varList.get(varListIndex).intValue();
            int vState = _varModel.getState( varNum );
            VariableValue var = _varModel.getVariable(varNum);
            if (log.isDebugEnabled()) log.debug("nextWrite var index "+varNum+" state "+VariableValue.stateNameFromValue(vState)
                                                +" isToWrite: "+var.isToWrite()+" label:"+var.label());
            varListIndex++;
            if (var.isToWrite()) {
                log.debug("start write of variable "+_varModel.getLabel(varNum));

                executeWrite(var);

                if (log.isDebugEnabled()) log.debug("return from starting var write");
                return true;  // only make one request at a time!
            }
        }
        // check for CVs to handle (e.g. for CV table)
        while (cvListIterator!= null && cvListIterator.hasNext()) {
            int cvNum = cvListIterator.next();
            CvValue cv = _cvModel.getCvByRow( cvNum );
            if (log.isDebugEnabled()) log.debug("nextWrite cv index "+cvNum+" state "+cv.getState());

            if (cv.isToWrite()) {
                if (log.isDebugEnabled()) log.debug("start write of cv index "+cvNum);
                setBusy(true);
                if (_programmingCV != null) log.error("listener already set at write start");
                _programmingCV = _cvModel.getCvByRow(cvNum);
                _read = false;
                // get notified when that state changes so can repeat
                _programmingCV.addPropertyChangeListener(this);
                // and make the write request
                // _programmingCV.setToWrite(false);  // CVs set this themselves
                _programmingCV.write(_cvModel.getStatusLabel());
                if (log.isDebugEnabled()) log.debug("return from starting cv write");
                return true;  // only make one request at a time!
            }
        }
        // check for Indexed CVs to handle (e.g. for Indexed CV table)
        while ((indexedCvList.size() >= 0) && (indexedCvListIndex < indexedCvList.size())) {
            int indxVarNum = indexedCvList.get(indexedCvListIndex).intValue();
            int indxState = _varModel.getState(indxVarNum);
            if (log.isDebugEnabled()) log.debug(
                "nextWrite indexed cv @ row index " + indexedCvListIndex + " state " + indxState);
            VariableValue iCv = _varModel.getVariable(indxVarNum);
            indexedCvListIndex++;
            if (iCv.isToWrite()) {
                String sz = "start write of indexed cv " +
                    (  _indexedCvModel.getCvByRow(indexedCvListIndex-1)).cvName();
                if (log.isDebugEnabled()) log.debug(sz);

                setBusy(true);
                if (_programmingIndexedCV != null) log.error(
                    "listener already set at read start");
                _programmingIndexedCV = _varModel.getVariable(indxVarNum);
                _read = true;
                // get notified when that state changes so can repeat
                _programmingIndexedCV.addPropertyChangeListener(this);
                // _programmingIndexedCV.setToWrite(false);  // CVs set this themselves
                // and make the write request
                _programmingIndexedCV.writeAll();
                if (log.isDebugEnabled()) log.debug("return from starting indexed CV read");
                // the request may have instantateously been satisfied...
                return true; // only make one request at a time!
            }
        }
        // nothing to program, end politely
        if (log.isDebugEnabled()) log.debug("nextWrite found nothing to do");
        writeChangesButton.setSelected(false);
        writeAllButton.setSelected(false);
        setBusy(false);
        container.paneFinished();
        log.debug("return from nextWrite with nothing to do");
        return false;
    }
    
    /**
     * Prepare this pane for a compare operation.
     * <P>The read mechanism only reads
     * variables in certain states (and needs to do that to handle error
     * processing right now), so this is implemented by first
     * setting all variables and CVs on this pane to TOREAD via this method
     *
     */
    public void prepConfirmPane(boolean onlyChanges) {
        if (log.isDebugEnabled()) log.debug("start prepReadPane with onlyChanges="+onlyChanges);
        justChanges = onlyChanges;
        enableButtons(false);

        if (isCvTablePane) setCvListFromTable();  // make sure list of CVs up to date if table

        if (justChanges) {
            confirmChangesButton.setEnabled(true);
            confirmChangesButton.setSelected(true);
        } else {
            confirmAllButton.setSelected(true);
            confirmAllButton.setEnabled(true);
        }
        if (container.isBusy() == false) {
            container.enableButtons(false);
        }
        // we can use the read prep since confirm has to read first
        setToRead(justChanges, true);
        varListIndex = 0;
        
        cvListIterator = cvList.iterator();
        
        indexedCvListIndex = 0;
    }

    
    /**
     * Invoked by "Compare changes on sheet" button, this sets in motion a
     * continuing sequence of "confirm" operations on the
     * variables & CVs in the Pane.  Only variables in states
     * marked as "changed" will be checked.
     *
     * @return true is a confirm has been started, false if the pane is complete.
     */
    public boolean confirmPaneChanges() {
        if (log.isDebugEnabled()) log.debug("confirmPane starts with "
                                            +varList.size()+" vars, "
                                            +cvList.size()+" cvs "
                                            +indexedCvList.size()+" indexed cvs" );
        prepConfirmPane(true);
        return nextConfirm();
    }

    /**
     * Invoked by "Compare Full Sheet" button, this sets in motion a
     * continuing sequence of "confirm" operations on the
     * variables & CVs in the Pane.  The read mechanism only reads
     * variables in certain states (and needs to do that to handle error
     * processing right now), so this is implemented by first
     * setting all variables and CVs on this pane to TOREAD
     * in prepReadPaneAll, then starting the execution.
     *
     * @return true is a confirm has been started, false if the pane is complete.
     */
    public boolean confirmPaneAll() {
        if (log.isDebugEnabled()) log.debug("confirmAllPane starts with "
                                            +varList.size()+" vars, "
                                            +cvList.size()+" cvs "
                                            +indexedCvList.size()+" indexed cvs" );
        prepConfirmPane(false);
        // start operation
        return nextConfirm();
    }

    // reference to variable being programmed (or null if none)
    VariableValue _programmingVar = null;
    CvValue _programmingCV  = null;
    VariableValue _programmingIndexedCV = null;
    boolean _read = true;

    // busy during read, write operations
    private boolean _busy = false;
    public boolean isBusy() { return _busy; }

    protected void setBusy(boolean busy) {
        boolean oldBusy = _busy;
        _busy = busy;
        if (!busy && !container.isBusy()) {
            enableButtons(true);
        }
        if (oldBusy != busy) firePropertyChange("Busy", Boolean.valueOf(oldBusy), Boolean.valueOf(busy));
    }

    private int retry = 0;

    /**
     * Get notification of a variable property change, specifically "busy" going to
     * false at the end of a programming operation. If we're in a programming
     * operation, we then continue it by reinvoking the nextRead/writePane operation.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // check for the right event & condition
        if (_programmingVar == null && _programmingCV == null&& _programmingIndexedCV == null) {
            log.warn("unexpected propertChange: "+e);
            return;
        } else if (log.isDebugEnabled()) log.debug("property changed: "+e.getPropertyName()
                                                   +" new value: "+e.getNewValue());

        // find the right way to handle this
        if (e.getSource() == _programmingVar &&
            e.getPropertyName().equals("Busy") &&
            ((Boolean)e.getNewValue()).equals(Boolean.FALSE) ) {
            if (_programmingVar.getState() == VariableValue.UNKNOWN) {
                if (retry == 0) {
                    varListIndex--;
                    retry++;
                } else {
                    retry = 0;
                }
            }
            replyWhileProgrammingVar();
            return;
        } else if (e.getSource() == _programmingCV &&
                   e.getPropertyName().equals("Busy") &&
                   ((Boolean)e.getNewValue()).equals(Boolean.FALSE) ) {
                   
            // there's no -- operator on the HashSet Iterator we're
            // using for the CV list, so we don't do individual retries
            // now.
            //if (_programmingCV.getState() == CvValue.UNKNOWN) {
            //    if (retry == 0) {
            //        cvListIndex--;
            //        retry++;
            //    } else {
            //        retry = 0;
            //    }
            //}
            
            replyWhileProgrammingCV();
            return;
        } else if (e.getSource() == _programmingIndexedCV &&
                   e.getPropertyName().equals("Busy") &&
                   ((Boolean)e.getNewValue()).equals(Boolean.FALSE) ) {
            if (_programmingIndexedCV.getState() == VariableValue.UNKNOWN) {
                if (retry == 0) {
                    indexedCvListIndex--;
                    retry++;
                } else {
                    retry = 0;
                }
            }
            replyWhileProgrammingIndxCV();
            return;
        } else {
            if (log.isDebugEnabled() && e.getPropertyName().equals("Busy"))
                log.debug("ignoring change of Busy "+e.getNewValue()
                          +" "+( ((Boolean)e.getNewValue()).equals(Boolean.FALSE)));
            return;
        }
    }

    public void replyWhileProgrammingVar() {
        if (log.isDebugEnabled()) log.debug("correct event for programming variable, restart operation");
        // remove existing listener
        _programmingVar.removePropertyChangeListener(this);
        _programmingVar = null;
        // restart the operation
        restartProgramming();
    }

    public void replyWhileProgrammingCV() {
        if (log.isDebugEnabled()) log.debug("correct event for programming CV, restart operation");
        // remove existing listener
        _programmingCV.removePropertyChangeListener(this);
        _programmingCV = null;
        // restart the operation
        restartProgramming();
    }

    public void replyWhileProgrammingIndxCV() {
        if (log.isDebugEnabled()) log.debug("correct event for programming Indexed CV, restart operation");
        // remove existing listener
        _programmingIndexedCV.removePropertyChangeListener(this);
        _programmingIndexedCV = null;
        // restart the operation
        restartProgramming();
    }

    void restartProgramming() {
        log.debug("start restartProgramming");
        if (_read && readChangesButton.isSelected()) nextRead();
        else if (_read && readAllButton.isSelected()) nextRead();
        else if (_read && confirmChangesButton.isSelected()) nextConfirm();
        else if (_read && confirmAllButton.isSelected()) nextConfirm();
        else if (writeChangesButton.isSelected()) nextWrite();   // was writePaneChanges
        else if (writeAllButton.isSelected()) nextWrite();
        else {
            if (log.isDebugEnabled()) log.debug("No operation to restart");
            if (isBusy()) {
                container.paneFinished();
                setBusy(false);
            }
        }
        log.debug("end restartProgramming");
    }

    protected void stopProgramming() {
        log.debug("start stopProgramming");
        setToRead(false, false);
        setToWrite(false, false);
        varListIndex = varList.size();
        
        cvListIterator = null;

        indexedCvListIndex = indexedCvList.size();
        log.debug("end stopProgramming");
    }

    /**
     * Create a new group from the JDOM group Element
     */
    @SuppressWarnings("unchecked")
	protected JPanel newGroup(Element element, boolean showStdName, Element modelElem) {

        // create a panel to add as a new column or row
        final JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        c.setLayout(g);

        // handle the xml definition
        // for all elements in the column or row
        List<Element> elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newColumn starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            Element e = (elemList.get(i));
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newGroup processing "+name+" element");
            // decode the type
            if (name.equals("display")) { // its a variable
                                // load the variable
                newVariable( e, c, g, cs, showStdName);
            } else if (name.equals("separator")) { // its a separator
                JSeparator j = new JSeparator(javax.swing.SwingConstants.HORIZONTAL);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(j, cs);
                c.add(j);
                cs.gridwidth = 1;
            } else if (name.equals("label")) {
                cs.gridwidth = GridBagConstraints.REMAINDER;
                makeLabel(e, c, g, cs);
            } else if (name.equals("soundlabel")) {
                cs.gridwidth = GridBagConstraints.REMAINDER;
                makeSoundLabel(e, c, g, cs);
            } else if (name.equals("cvtable")) {
                makeCvTable(cs, g, c);
            } else if (name.equals("indxcvtable")) {
                log.debug("starting to build IndexedCvTable pane");
                JTable indxcvTable = new JTable(_indexedCvModel);
                JScrollPane cvScroll = new JScrollPane(indxcvTable);
                indxcvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
                indxcvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
                indxcvTable.setDefaultEditor(JTextField.class, new ValueEditor());
                indxcvTable.setDefaultEditor(JButton.class, new ValueEditor());
                indxcvTable.setRowHeight(new JButton("X").getPreferredSize().height);
                indxcvTable.setPreferredScrollableViewportSize(new Dimension(700, indxcvTable.getRowHeight()*14));
                cvScroll.setColumnHeaderView(indxcvTable.getTableHeader());
                // don't want a horizontal scroll bar
                // Need to see the whole row at one time
//                indxcvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(cvScroll, cs);
                c.add(cvScroll);
                cs.gridwidth = 1;

                // remember which indexed CVs to read/write
                for (int j=0; j<_indexedCvModel.getRowCount(); j++) {
                    String sz = "CV" +_indexedCvModel.getName(j);
                    int in = _varModel.findVarIndex(sz);
                    indexedCvList.add(Integer.valueOf(in));
                }

                _cvTable = true;
                log.debug("end of building IndexedCvTable pane");
            } else if (name.equals("fnmapping")) {
                pickFnMapPanel(c, g, cs, modelElem);
            } else if (name.equals("dccaddress")) {
                JPanel l = addDccAddressPanel(e);
                if (l.getComponentCount() > 0) {
                    cs.gridwidth = GridBagConstraints.REMAINDER;
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("column")) {
                // nested "column" elements ...
                cs.gridheight = GridBagConstraints.REMAINDER;
                JPanel l = newColumn(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridheight = 1;
                }
            } else if (name.equals("row")) {
                                // nested "row" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newRow(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("grid")) {
                                // nested "grid" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newGrid(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("group")) {
                                // nested "group" elements ...
                JPanel l = newGroup(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
            }
            } else if (!name.equals("qualifier")) { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newColumn");
            }
        }
        // add glue to the bottom to allow resize
        if (c.getComponentCount() > 0) {
            c.add(Box.createVerticalGlue());
        }

        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(c, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                c.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(element, _varModel);
        return c;
    }

    /**
     * Create a new grid group from the JDOM group Element
     */
    @SuppressWarnings("unchecked")
	protected void newGridGroup(Element element, final JPanel c, GridBagLayout g, GridGlobals globs, boolean showStdName, Element modelElem) {

        // handle the xml definition
        // for all elements in the column or row
        List<Element> elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newColumn starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            Element e = (elemList.get(i));
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newGroup processing "+name+" element");
            // decode the type
            if (name.equals("griditem")) {                    
                final JPanel l = newGridItem(e, showStdName, modelElem, globs);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, globs.gridConstraints);
                    c.add(l);
//                     globs.gridConstraints.gridwidth = 1;
					// handle qualification if any
					QualifierAdder qa = new QualifierAdder() {
						protected Qualifier createQualifier(VariableValue var, String relation, String value) {
							return new JComponentQualifier(l, var, Integer.parseInt(value), relation);
						}
						protected void addListener(java.beans.PropertyChangeListener qc) {
							l.addPropertyChangeListener(qc);
						}
					};
		
					qa.processModifierElements(e, _varModel);
                }
            } else if (name.equals("group")) {
                                // nested "group" elements ...
                newGridGroup(e, c, g, globs, showStdName, modelElem);
            } else if (!name.equals("qualifier")) { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newColumn");
            }
        }
        // add glue to the bottom to allow resize
//         if (c.getComponentCount() > 0) {
//             c.add(Box.createVerticalGlue());
//         }

        
    }

    /**
     * Create a single column from the JDOM column Element
     */
    @SuppressWarnings("unchecked")
	public JPanel newColumn(Element element, boolean showStdName, Element modelElem) {

        // create a panel to add as a new column or row
        final JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        c.setLayout(g);

        // handle the xml definition
        // for all elements in the column or row
        List<Element> elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newColumn starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            // update the grid position
            cs.gridy++;
            cs.gridx = 0;

            Element e = (elemList.get(i));
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newColumn processing "+name+" element");
            // decode the type
            if (name.equals("display")) { // its a variable
                                // load the variable
                newVariable( e, c, g, cs, showStdName);
            } else if (name.equals("separator")) { // its a separator
                JSeparator j = new JSeparator(javax.swing.SwingConstants.HORIZONTAL);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(j, cs);
                c.add(j);
                cs.gridwidth = 1;
            } else if (name.equals("label")) {
                cs.gridwidth = GridBagConstraints.REMAINDER;
                makeLabel(e, c, g, cs);
            } else if (name.equals("soundlabel")) {
                cs.gridwidth = GridBagConstraints.REMAINDER;
                makeSoundLabel(e, c, g, cs);
            } else if (name.equals("cvtable")) {
                makeCvTable(cs, g, c);
            } else if (name.equals("indxcvtable")) {
                log.debug("starting to build IndexedCvTable pane");
                JTable indxcvTable = new JTable(_indexedCvModel);
                JScrollPane cvScroll = new JScrollPane(indxcvTable);
                indxcvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
                indxcvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
                indxcvTable.setDefaultEditor(JTextField.class, new ValueEditor());
                indxcvTable.setDefaultEditor(JButton.class, new ValueEditor());
                indxcvTable.setRowHeight(new JButton("X").getPreferredSize().height);
                indxcvTable.setPreferredScrollableViewportSize(new Dimension(700, indxcvTable.getRowHeight()*14));
                cvScroll.setColumnHeaderView(indxcvTable.getTableHeader());
                // don't want a horizontal scroll bar
                // Need to see the whole row at one time
//                indxcvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(cvScroll, cs);
                c.add(cvScroll);
                cs.gridwidth = 1;

                // remember which indexed CVs to read/write
                for (int j=0; j<_indexedCvModel.getRowCount(); j++) {
                    String sz = "CV" +_indexedCvModel.getName(j);
                    int in = _varModel.findVarIndex(sz);
                    indexedCvList.add(Integer.valueOf(in));
                }

                _cvTable = true;
                log.debug("end of building IndexedCvTable pane");
            } else if (name.equals("fnmapping")) {
                pickFnMapPanel(c, g, cs, modelElem);
            } else if (name.equals("dccaddress")) {
                JPanel l = addDccAddressPanel(e);
                if (l.getComponentCount() > 0) {
                    cs.gridwidth = GridBagConstraints.REMAINDER;
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("column")) {
                // nested "column" elements ...
                cs.gridheight = GridBagConstraints.REMAINDER;
                JPanel l = newColumn(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridheight = 1;
                }
            } else if (name.equals("row")) {
                                // nested "row" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newRow(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("grid")) {
                                // nested "grid" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newGrid(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("group")) {
                                // nested "group" elements ...
                JPanel l = newGroup(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
            }
            } else if (!name.equals("qualifier")) { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newColumn");
            }
        }
        // add glue to the bottom to allow resize
        if (c.getComponentCount() > 0) {
            c.add(Box.createVerticalGlue());
        }

        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(c, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                c.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(element, _varModel);
        return c;
    }

    /**
     * Create a single row from the JDOM column Element
     */
    @SuppressWarnings("unchecked")
	public JPanel newRow(Element element, boolean showStdName, Element modelElem) {

        // create a panel to add as a new column or row
        final JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        c.setLayout(g);

        // handle the xml definition
        // for all elements in the column or row
        List<Element> elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newRow starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            // update the grid position
            cs.gridy = 0;
            cs.gridx++;

            Element e = elemList.get(i);
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newRow processing "+name+" element");
            // decode the type
            if (name.equals("display")) { // its a variable
                // load the variable
                newVariable( e, c, g, cs, showStdName);
            } else if (name.equals("separator")) { // its a separator
                JSeparator j = new JSeparator(javax.swing.SwingConstants.VERTICAL);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(j, cs);
                c.add(j);
                cs.fill = GridBagConstraints.NONE;
                cs.gridheight = 1;
            } else if (name.equals("label")) {
                cs.gridheight = GridBagConstraints.REMAINDER;
                makeLabel(e, c, g, cs);
            } else if (name.equals("soundlabel")) {
                cs.gridheight = GridBagConstraints.REMAINDER;
                makeSoundLabel(e, c, g, cs);
            } else if (name.equals("cvtable")) {
                makeCvTable(cs, g, c);
            } else if (name.equals("indxcvtable")) {
                log.debug("starting to build IndexedCvTable pane");
                JTable	indxcvTable	= new JTable(_indexedCvModel);
                JScrollPane cvScroll = new JScrollPane(indxcvTable);
                indxcvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
                indxcvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
                indxcvTable.setDefaultEditor(JTextField.class, new ValueEditor());
                indxcvTable.setDefaultEditor(JButton.class, new ValueEditor());
                indxcvTable.setRowHeight(new JButton("X").getPreferredSize().height);
                indxcvTable.setPreferredScrollableViewportSize(new Dimension(700, indxcvTable.getRowHeight()*14));
                cvScroll.setColumnHeaderView(indxcvTable.getTableHeader());
                // don't want a horizontal scroll bar
                // Need to see the whole row at one time
//                indxcvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(cvScroll, cs);
                c.add(cvScroll);
                cs.gridwidth = 1;

                // remember which indexed CVs to read/write
                for (int j=0; j<_indexedCvModel.getRowCount(); j++) {
                    String sz = "CV" +_indexedCvModel.getName(j);
                    int in = _varModel.findVarIndex(sz);
                    indexedCvList.add(Integer.valueOf(in));
                }

                _cvTable = true;
                log.debug("end of building IndexedCvTable pane");
            } else if (name.equals("fnmapping")) {
                pickFnMapPanel(c, g, cs, modelElem);
            } else if (name.equals("dccaddress")) {
                JPanel l = addDccAddressPanel(e);
                if (l.getComponentCount() > 0) {
                    cs.gridheight = GridBagConstraints.REMAINDER;
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridheight = 1;
                }
            } else if (name.equals("column")) {
                // nested "column" elements ...
                cs.gridheight = GridBagConstraints.REMAINDER;
                JPanel l = newColumn(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridheight = 1;
                }
            } else if (name.equals("row")) {
                                // nested "row" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newRow(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("grid")) {
                                // nested "grid" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newGrid(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("group")) {
                                // nested "group" elements ...
                JPanel l = newGroup(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
            }
            } else if (!name.equals("qualifier")) { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newRow");
            }
        }
        // add glue to the bottom to allow resize
        if (c.getComponentCount() > 0) {
            c.add(Box.createVerticalGlue());
        }
        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(c, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                c.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(element, _varModel);
        return c;
    }

    /**
     * Create a grid from the JDOM  Element
     */
    @SuppressWarnings("unchecked")
    public JPanel newGrid(Element element, boolean showStdName, Element modelElem) {

        // create a panel to add as a new grid
        final JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        c.setLayout(g);
        
        GridGlobals globs = new GridGlobals();        

        // handle the xml definition
        // for all elements in the grid
        List<Element> elemList = element.getChildren();
        globs.gridAttList = element.getAttributes(); // get grid-level attributes
        if (log.isDebugEnabled()) log.debug("newGrid starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {
            globs.gridConstraints = new GridBagConstraints();
            Element e = elemList.get(i);
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newGrid processing "+name+" element");
            // decode the type
            if (name.equals("griditem")) {                    
                JPanel l = newGridItem(e, showStdName, modelElem, globs);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, globs.gridConstraints);
                    c.add(l);
//                     globs.gridConstraints.gridwidth = 1;
                }
            } else if (name.equals("group")) {
                                // nested "group" elements ...
                newGridGroup(e, c, g, globs, showStdName, modelElem);
            } else if (!name.equals("qualifier")) { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newGrid");
            }
        }
        
        // add glue to the bottom to allow resize
        if (c.getComponentCount() > 0) {
            c.add(Box.createVerticalGlue());
        }
        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(c, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                c.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(element, _varModel);
        return c;
    }

    class GridGlobals {
    public int gridxCurrent = -1;
    public int gridyCurrent = -1;
    public List<Attribute> gridAttList;
    public GridBagConstraints gridConstraints;
    }

    /**
     * Create a griditem from the JDOM  Element
     */
    @SuppressWarnings("unchecked")
	public JPanel newGridItem(Element element, boolean showStdName, Element modelElem, GridGlobals globs) {

                List<Attribute> itemAttList = element.getAttributes(); // get item-level attributes
                List<Attribute> attList = new ArrayList<Attribute>(globs.gridAttList);
                attList.addAll(itemAttList); // merge grid and item-level attributes
//                log.info("Attribute list:"+attList);
//                log.info("Previous gridxCurrent="+globs.gridxCurrent+";gridyCurrent="+globs.gridyCurrent);
                for (int j = 0; j < attList.size(); j++) {
                    Attribute attrib = attList.get(j);
                    String attribName = attrib.getName();
                    String attribRawValue = attrib.getValue();
                    Field constraint = null;
                    String constraintType = null;
                    if ( ( attribName.equals("gridx") || attribName.equals("gridy") ) && attribRawValue.equals("RELATIVE") ) {
                        attribRawValue = "NEXT"; // NEXT is a synonym for Relative
                    }
                    if ( attribName.equals("gridx")  && attribRawValue.equals("CURRENT") ) {
                        attribRawValue = String.valueOf(Math.max(0,globs.gridxCurrent));
                    }
                    if ( attribName.equals("gridy")  && attribRawValue.equals("CURRENT") ) {
                        attribRawValue = String.valueOf(Math.max(0,globs.gridyCurrent));
                    }
                    if ( attribName.equals("gridx")  && attribRawValue.equals("NEXT") ) {
                        attribRawValue = String.valueOf(++globs.gridxCurrent);
                    }
                    if ( attribName.equals("gridy")  && attribRawValue.equals("NEXT") ) {
                        attribRawValue = String.valueOf(++globs.gridyCurrent);
                    }
                    try {
                        constraint = globs.gridConstraints.getClass().getDeclaredField(attribName);
                        constraintType = constraint.getType().toString();
                        constraint.setAccessible(true);
                        } catch (NoSuchFieldException ex) {
                        log.error("Unrecognised attribute \""+attribName+"\", skipping");
                        continue;
                    }
                    if ( constraintType.equals("int") ) {
                        int attribValue;
                        try {
                            attribValue = Integer.valueOf(attribRawValue);
                            constraint.set(globs.gridConstraints,attribValue);
                       } catch (IllegalAccessException ey) {
                            log.error("Unable to set constraint \""+attribName+". IllegalAccessException error thrown.");
                        } catch (NumberFormatException ex) {
                            try {
                                Field constant = globs.gridConstraints.getClass().getDeclaredField(attribRawValue);
                                constant.setAccessible(true);
                                attribValue = (Integer) GridBagConstraints.class.getField(attribRawValue).get(constant);
                                constraint.set(globs.gridConstraints,attribValue);
                            } catch (NoSuchFieldException ey) {
                        log.error("Invalid value \""+attribRawValue+"\" for attribute \""+attribName+"\"");
                            } catch (IllegalAccessException ey) {
                            log.error("Unable to set constraint \""+attribName+". IllegalAccessException error thrown.");
                            }
                        }
                    } else if ( constraintType.equals("double") ) {
                        double attribValue;
                        try {
                            attribValue = Double.valueOf(attribRawValue);
                            constraint.set(globs.gridConstraints,attribValue);
                       } catch (IllegalAccessException ey) {
                            log.error("Unable to set constraint \""+attribName+". IllegalAccessException error thrown.");
                        } catch (NumberFormatException ex) {
                            log.error("Invalid value \""+attribRawValue+"\" for attribute \""+attribName+"\"");
                        }
                    } else if ( constraintType.equals("class java.awt.Insets") ) {
                        try {
                            String[] insetStrings = attribRawValue.split(",");
                            if ( insetStrings.length == 4) {
                                Insets attribValue = new Insets(Integer.valueOf(insetStrings[0]),Integer.valueOf(insetStrings[1]),Integer.valueOf(insetStrings[2]),Integer.valueOf(insetStrings[3]));
                                constraint.set(globs.gridConstraints,attribValue);
                            } else {
                                log.error("Invalid value \""+attribRawValue+"\" for attribute \""+attribName+"\"");
                                log.error("Value should be four integers of the form \"top,left,bottom,right\"");
                            }
                       } catch (IllegalAccessException ey) {
                            log.error("Unable to set constraint \""+attribName+". IllegalAccessException error thrown.");
                        } catch (NumberFormatException ex) {
                            log.error("Invalid value \""+attribRawValue+"\" for attribute \""+attribName+"\"");
                            log.error("Value should be four integers of the form \"top,left,bottom,right\"");
                        }
                    } else {
                        log.error("Required \""+constraintType+"\" handler for attribute \""+attribName+"\" not defined in JMRI code");
                        log.error("Please file a JMRI bug report at https://sourceforge.net/p/jmri/bugs/new/");
                    }
                }
//                log.info("Updated globs.GridBagConstraints.gridx="+globs.gridConstraints.gridx+";globs.GridBagConstraints.gridy="+globs.gridConstraints.gridy);


        // create a panel to add as a new grid item
        final JPanel c = new JPanel();
        panelList.add(c);
        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        c.setLayout(g);

        // handle the xml definition
        // for all elements in the grid item
        List<Element> elemList = element.getChildren();
        if (log.isDebugEnabled()) log.debug("newGridItem starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {

            // update the grid position
            cs.gridy = 0;
            cs.gridx++;

            Element e = elemList.get(i);
            String name = e.getName();
            if (log.isDebugEnabled()) log.debug("newGridItem processing "+name+" element");
            // decode the type
            if (name.equals("display")) { // its a variable
                // load the variable
                newVariable( e, c, g, cs, showStdName);
            } else if (name.equals("separator")) { // its a separator
                JSeparator j = new JSeparator(javax.swing.SwingConstants.VERTICAL);
                cs.fill = GridBagConstraints.BOTH;
                cs.gridheight = GridBagConstraints.REMAINDER;
                g.setConstraints(j, cs);
                c.add(j);
                cs.fill = GridBagConstraints.NONE;
                cs.gridheight = 1;
            } else if (name.equals("label")) {
                cs.gridheight = GridBagConstraints.REMAINDER;
                makeLabel(e, c, g, cs);
            } else if (name.equals("soundlabel")) {
                cs.gridheight = GridBagConstraints.REMAINDER;
                makeSoundLabel(e, c, g, cs);
            } else if (name.equals("cvtable")) {
                makeCvTable(cs, g, c);
            } else if (name.equals("indxcvtable")) {
                log.debug("starting to build IndexedCvTable pane");
                JTable	indxcvTable	= new JTable(_indexedCvModel);
                JScrollPane cvScroll = new JScrollPane(indxcvTable);
                indxcvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
                indxcvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
                indxcvTable.setDefaultEditor(JTextField.class, new ValueEditor());
                indxcvTable.setDefaultEditor(JButton.class, new ValueEditor());
                indxcvTable.setRowHeight(new JButton("X").getPreferredSize().height);
                indxcvTable.setPreferredScrollableViewportSize(new Dimension(700, indxcvTable.getRowHeight()*14));
                cvScroll.setColumnHeaderView(indxcvTable.getTableHeader());
                // don't want a horizontal scroll bar
                // Need to see the whole row at one time
//                indxcvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                cs.gridwidth = GridBagConstraints.REMAINDER;
                g.setConstraints(cvScroll, cs);
                c.add(cvScroll);
                cs.gridwidth = 1;

                // remember which indexed CVs to read/write
                for (int j=0; j<_indexedCvModel.getRowCount(); j++) {
                    String sz = "CV" +_indexedCvModel.getName(j);
                    int in = _varModel.findVarIndex(sz);
                    indexedCvList.add(Integer.valueOf(in));
                }

                _cvTable = true;
                log.debug("end of building IndexedCvTable pane");
            } else if (name.equals("fnmapping")) {
                pickFnMapPanel(c, g, cs, modelElem);
            } else if (name.equals("dccaddress")) {
                JPanel l = addDccAddressPanel(e);
                if (l.getComponentCount() > 0) {
                    cs.gridheight = GridBagConstraints.REMAINDER;
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridheight = 1;
                }
            } else if (name.equals("column")) {
                // nested "column" elements ...
                cs.gridheight = GridBagConstraints.REMAINDER;
                JPanel l = newColumn(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridheight = 1;
                }
            } else if (name.equals("row")) {
                                // nested "row" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newRow(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("grid")) {
                                // nested "grid" elements ...
                cs.gridwidth = GridBagConstraints.REMAINDER;
                JPanel l = newGrid(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
                    cs.gridwidth = 1;
                }
            } else if (name.equals("group")) {
                                // nested "group" elements ...
                JPanel l = newGroup(e, showStdName, modelElem);
                if (l.getComponentCount() > 0) {
                    panelList.add(l);
                    g.setConstraints(l, cs);
                    c.add(l);
            }
            } else if (!name.equals("qualifier")) { // its a mistake
                log.error("No code to handle element of type "+e.getName()+" in newGridItem");
            }
        }

			globs.gridxCurrent = globs.gridConstraints.gridx;
			globs.gridyCurrent = globs.gridConstraints.gridy;
//                log.info("Updated gridxCurrent="+globs.gridxCurrent+";gridyCurrent="+globs.gridyCurrent);

        // add glue to the bottom to allow resize
        if (c.getComponentCount() > 0) {
            c.add(Box.createVerticalGlue());
        }
        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(c, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                c.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(element, _varModel);
        return c;
    }

    /**
     * Create label from Element
     */
    protected void makeLabel(Element e, JPanel c, GridBagLayout g, GridBagConstraints cs) {
        String text = LocaleSelector.getAttribute(e,"text");
        if (text==null || text.equals("")) text = LocaleSelector.getAttribute(e,"label"); // label subelement deprecated 3.7.5
        final JLabel l = new JLabel(text);
        l.setAlignmentX(1.0f);
        cs.fill = GridBagConstraints.BOTH;
        if (log.isDebugEnabled()) {
            log.debug("Add label: "+l.getText()+" cs: "
                      +cs.gridwidth+" "+cs.fill+" "
                      +cs.gridx+" "+cs.gridy);
        }
        g.setConstraints(l, cs);
        c.add(l);
        cs.fill = GridBagConstraints.NONE;
        cs.gridwidth = 1;
        cs.gridheight = 1;
        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(l, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                l.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(e, _varModel);
    }

    /**
     * Create sound label from Element
     */
    protected void makeSoundLabel(Element e, JPanel c, GridBagLayout g, GridBagConstraints cs) {
        String labelText = rosterEntry.getSoundLabel(Integer.valueOf(LocaleSelector.getAttribute(e,"num")));
        final JLabel l = new JLabel(labelText);
        l.setAlignmentX(1.0f);
        cs.fill = GridBagConstraints.BOTH;
        if (log.isDebugEnabled()) {
            log.debug("Add soundlabel: "+l.getText()+" cs: "
                      +cs.gridwidth+" "+cs.fill+" "
                      +cs.gridx+" "+cs.gridy);
        }
        g.setConstraints(l, cs);
        c.add(l);
        cs.fill = GridBagConstraints.NONE;
        cs.gridwidth = 1;
        cs.gridheight = 1;
        
        // handle qualification if any
        QualifierAdder qa = new QualifierAdder() {
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new JComponentQualifier(l, var, Integer.parseInt(value), relation);
            }
            protected void addListener(java.beans.PropertyChangeListener qc) {
                l.addPropertyChangeListener(qc);
            }
        };
        
        qa.processModifierElements(e, _varModel);
    }

    void makeCvTable(GridBagConstraints cs, GridBagLayout g, JPanel c) {
        log.debug("starting to build CvTable pane");
        
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(_cvModel);

        JTable cvTable = new JTable(_cvModel);

        sorter.setComparator(CvTableModel.NUMCOLUMN, new jmri.util.PreferNumericComparator());

        List <RowSorter.SortKey> sortKeys 
            = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys); 

        cvTable.setRowSorter(sorter);
        
        cvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
        cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
        cvTable.setDefaultEditor(JTextField.class, new ValueEditor());
        cvTable.setDefaultEditor(JButton.class, new ValueEditor());
        cvTable.setRowHeight(new JButton("X").getPreferredSize().height);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        // instead of forcing the columns to fill the frame (and only fill)
        cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane 	cvScroll	= new JScrollPane(cvTable);
        cvScroll.setColumnHeaderView(cvTable.getTableHeader());

        cs.gridheight = GridBagConstraints.REMAINDER;
        g.setConstraints(cvScroll, cs);
        c.add(cvScroll);
        cs.gridheight = 1;

        // remember which CVs to read/write
        isCvTablePane = true;
        setCvListFromTable();
        
        _cvTable = true;
        log.debug("end of building CvTable pane");
    }
    
    void setCvListFromTable() {
        // remember which CVs to read/write
        for (int j=0; j<_cvModel.getRowCount(); j++) {
            cvList.add(Integer.valueOf(j));
        }
    }
    
    /**
     * Pick an appropriate function map panel depending on model attribute.
     *  <dl>
     *  <dt>If attribute extFnsESU="yes":</dt>
     *   <dd>Invoke FnMapPanelESU(VariableTableModel v, List<Integer> varsUsed, Element model)</dd>
     *  <dt>Otherwise:</dt>
     *   <dd>Invoke FnMapPanel(VariableTableModel v, List<Integer> varsUsed, Element model)</dd>
     * </dl>
     */
    
    void pickFnMapPanel(JPanel c, GridBagLayout g, GridBagConstraints cs, Element modelElem) {
        boolean extFnsESU = false;
        Attribute a = modelElem.getAttribute("extFnsESU");
        try { if (a!=null) extFnsESU = (a.getValue()).equalsIgnoreCase("yes");}
        catch (Exception ex) {log.error("error handling decoder's extFnsESU value");}        
        if (extFnsESU) {
            FnMapPanelESU l = new FnMapPanelESU(_varModel, varList, modelElem, rosterEntry, _cvModel);
            fnMapListESU.add(l); // remember for deletion
            cs.gridwidth = GridBagConstraints.REMAINDER;
            g.setConstraints(l, cs);
            c.add(l);
            cs.gridwidth = 1;
        } else {
            FnMapPanel l = new FnMapPanel(_varModel, varList, modelElem);
            fnMapList.add(l); // remember for deletion
            cs.gridwidth = GridBagConstraints.REMAINDER;
            g.setConstraints(l, cs);
            c.add(l);
            cs.gridwidth = 1;
        }
}

    /**
     * Add the representation of a single variable.  The
     * variable is defined by a JDOM variable Element from the XML file.
     */
    public void newVariable( Element var, JComponent col,
                             GridBagLayout g, GridBagConstraints cs, boolean showStdName) {

        // get the name
        String name = var.getAttribute("item").getValue();

        // if it doesn't exist, do nothing
        int i = _varModel.findVarIndex(name);
        if (i<0) {
            if (log.isDebugEnabled()) log.debug("Variable \""+name+"\" not found, omitted");
            return;
        }
//        log.info("Display item="+name);

        // check label orientation
        Attribute attr;
        String layout ="left";  // this default is also set in the DTD
        if ( (attr = var.getAttribute("layout")) != null && attr.getValue() != null)
            layout = attr.getValue();

        // load label if specified, else use name
        String label = name;
        if (!showStdName) {
            // get name attribute from variable, as that's the mfg name
            label = _varModel.getLabel(i);
        }
        String temp = LocaleSelector.getAttribute(var, "label");
        if (temp != null) label = temp;

        // get representation; store into the list to be programmed
        JComponent rep = getRepresentation(name, var);
        if (i>=0) varList.add(Integer.valueOf(i));

        // create the paired label
        JLabel l = new WatchingLabel(" "+label+" ", rep);
        
        // now handle the four orientations
        // assemble v from label, rep

        if (layout.equals("left")) {
            cs.anchor= GridBagConstraints.EAST;
            g.setConstraints(l, cs);
            col.add(l);

            cs.gridx++;
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(rep, cs);
            col.add(rep);

        } else if (layout.equals("right")) {
            cs.anchor= GridBagConstraints.EAST;
            g.setConstraints(rep, cs);
            col.add(rep);

            cs.gridx++;
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(l, cs);
            col.add(l);

        } else if (layout.equals("below")) {
            // variable in center of upper line
            cs.anchor=GridBagConstraints.CENTER;
            g.setConstraints(rep, cs);
            col.add(rep);

            // label aligned like others
            cs.gridy++;
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(l, cs);
            col.add(l);

        } else if (layout.equals("above")) {
            // label aligned like others
            cs.anchor= GridBagConstraints.WEST;
            g.setConstraints(l, cs);
            col.add(l);

            // variable in center of lower line
            cs.gridy++;
            cs.anchor=GridBagConstraints.CENTER;
            g.setConstraints(rep, cs);
            col.add(rep);

        } else {
            log.error("layout internally inconsistent: "+layout);
            return;
        }
    }

    /**
     * Get a GUI representation of a particular variable for display.
     * @param name Name used to look up the Variable object
     * @param var XML Element which might contain a "format" attribute to be used in the {@link VariableValue#getNewRep} call
     * from the Variable object; "tooltip" elements are also processed here.
     * @return JComponent representing this variable
     */
    public JComponent getRepresentation(String name, Element var) {
        int i = _varModel.findVarIndex(name);
        VariableValue variable = _varModel.getVariable(i);
        JComponent rep = null;
        String format = "default";
        Attribute attr;
        if ( (attr = var.getAttribute("format")) != null && attr.getValue() != null) format = attr.getValue();

        if (i>= 0) {
            rep = getRep(i, format);
            rep.setMaximumSize(rep.getPreferredSize());
            // set tooltip if specified here & not overridden by defn in Variable
            String tip = LocaleSelector.getAttribute(var, "tooltip");
            if ( rep.getToolTipText()!=null )
                tip =  rep.getToolTipText();
            rep.setToolTipText(modifyToolTipText(tip, variable));
        }
        return rep;
    }

    /** 
     * Takes default tooltip text, e.g. from the 
     * decoder element, and modifies it as needed.
     *
     * Intended to handle e.g. adding CV numbers to variables.
     */
    String modifyToolTipText(String start, VariableValue variable) {
        if (log.isDebugEnabled()) log.debug("modifyToolTipText: "+variable.label()+" "+_cvModel.getProgrammer());
        // this is the place to invoke VariableValue methods to (conditionally)
        // add information about CVs, etc in the ToolTip text
        
        // Optionally add CV numbers based on Roster Preferences setting
        start = addCvDescription(start, variable.getCvDescription(), variable.getMask());
        
        // Indicate what the command station can do
        // need to update this with e.g. the specific CV numbers
        if (_cvModel.getProgrammer()!= null
            && !_cvModel.getProgrammer().getCanRead()) {
            start = start+" (Hardware cannot read)";
        }
        if (_cvModel.getProgrammer()!= null
            && !_cvModel.getProgrammer().getCanWrite()) {
            start = start+" (Hardware cannot write)";
        }
        
        // indicate other reasons for read/write constraints
        if (variable.getReadOnly()) start = start+" (Defined to be read only)";
        if (variable.getWriteOnly()) start = start+" (Defined to be read only)";
        
        return start;
    }
    
    /** 
     * Optionally add CV numbers and bit numbers to tool tip text based on Roster Preferences setting.
     *
     * Needs to be independent of VariableValue methods to allow use by  non-standard elements
     * such as SpeedTableVarValue, DccAddressPanel, FnMapPanel.
     */
    public static String addCvDescription(String toolTip, String cvDescription, String mask) {
        // start with CV description
        String descString = cvDescription;
        
        // generate bit numbers from bitmask if applicable
        if ( (mask != null) && (mask.contains("X"))) {
            int lastBit = mask.length()-1;
            int lastV = -2;
            if ( mask.contains("V")) {
                if ( mask.indexOf('V') == mask.lastIndexOf('V') ) {
                    descString = descString+" bit "+(lastBit-mask.indexOf('V'));
                } else {
                    descString = descString+" bits ";
                    for (int i = 0; i <= lastBit; i++) {
                        char descStringLastChar = descString.charAt(descString.length()-1);
                        if ( mask.charAt(lastBit-i) == 'V' ) {
                            if (descStringLastChar == ' ') {
                                descString = descString+i;
                            } else if (lastV == (i-1)) {
                                if (descStringLastChar != '-') descString = descString+"-";
                            } else {
                                descString = descString+","+i;
                            }
                            lastV = i;
                        }
                        descStringLastChar = descString.charAt(descString.length()-1);
                        if ((descStringLastChar == '-') && ((mask.charAt(lastBit-i) != 'V') || (i == lastBit))) {
                            descString = descString+lastV;
                        }
                    }
                }
            } else {
                descString = descString+" no bits";
            }
            log.debug(descString+" Mask:"+mask);
        }
        
        // add to tool tip if Show CV Numbers enabled
        // parenthesise if adding to existing tool tip
        if (PaneProgFrame.getShowCvNumbers() && (descString != null)) {
            if (toolTip == null) {
                toolTip = descString;
            } else {
                toolTip = toolTip+" ("+descString+")";
            }
        } else {
            if (toolTip == null)
                toolTip = "";
        }
        
        return toolTip;
    }


    JComponent getRep(int i, String format) {
        return (JComponent)(_varModel.getRep(i, format));
    }

    /** list of fnMapping objects to dispose */
    ArrayList<FnMapPanel> fnMapList = new ArrayList<FnMapPanel>();
    ArrayList<FnMapPanelESU> fnMapListESU = new ArrayList<FnMapPanelESU>();
    /** list of JPanel objects to removeAll */
    ArrayList<JPanel> panelList = new ArrayList<JPanel>();

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove components
        removeAll();

        readChangesButton.removeItemListener(l1);
        readAllButton.removeItemListener(l2);
        writeChangesButton.removeItemListener(l3);
        writeAllButton.removeItemListener(l4);
        confirmChangesButton.removeItemListener(l5);
        confirmAllButton.removeItemListener(l6);
        l1 = l2 = l3 = l4 = l5 = l6 = null;

        if (_programmingVar != null) _programmingVar.removePropertyChangeListener(this);
        if (_programmingCV != null) _programmingCV.removePropertyChangeListener(this);
        if (_programmingIndexedCV != null) _programmingIndexedCV.removePropertyChangeListener(this);

        _programmingVar = null;
        _programmingCV  = null;
        _programmingIndexedCV = null;

        varList.clear();
        varList = null;
        cvList.clear();
        cvList = null;
        indexedCvList.clear();
        indexedCvList = null;

        // dispose of any panels
        for (int i=0; i<panelList.size(); i++) {
            panelList.get(i).removeAll();
        }
        panelList.clear();
        panelList = null;

        // dispose of any fnMaps
        for (int i=0; i<fnMapList.size(); i++) {
            fnMapList.get(i).dispose();
        }
        fnMapList.clear();
        fnMapList = null;

        readChangesButton = null;
        writeChangesButton = null;

        // these are disposed elsewhere
        _cvModel = null;
        _indexedCvModel = null;
        _varModel = null;
    }

    public boolean includeInPrint() { return print; }
    public void includeInPrint(boolean inc) { print = inc; }
    boolean print = false;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SBSC_USE_STRINGBUFFER_CONCATENATION") 
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public void printPane(HardcopyWriter w) {
        // if pane is empty, don't print anything
        if (varList.size() == 0 && cvList.size() == 0) return;
        // future work needed her to print indexed CVs

        // Define column widths for name and value output.
        // Make col 2 slightly larger than col 1 and reduce both to allow for
        // extra spaces that will be added during concatenation
        int col1Width = w.getCharactersPerLine()/2 -3 - 5;
        int col2Width = w.getCharactersPerLine()/2 -3 + 5;

        try {

            //Create a string of spaces the width of the first column
            String spaces = "";
            for (int i=0; i < col1Width; i++) {
              spaces = spaces + " ";
            }
            // start with pane name in bold
            String heading1 = SymbolicProgBundle.getMessage("PrintHeadingField");
            String heading2 = SymbolicProgBundle.getMessage("PrintHeadingSetting");
            String s;
            int interval = spaces.length()- heading1.length();
            w.setFontStyle(Font.BOLD);
            // write the section name and dividing line
            s = mName.toUpperCase();
            w.write(s, 0, s.length());
            w.writeBorders();
            //Draw horizontal dividing line for each Pane section
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                  w.getCharactersPerLine()+1);
            s = "\n";
            w.write(s,0,s.length());
            // if this isn't the raw CV section, write the column headings
            if (cvList.size()== 0){
            w.setFontStyle(Font.BOLD + Font.ITALIC);
            s = "   " + heading1 + spaces.substring(0,interval) + "   " + heading2;
            w.write(s, 0, s.length());
            w.writeBorders();
            s = "\n";
            w.write(s,0,s.length());
            }
            w.setFontStyle(Font.PLAIN);
            // Define a vector to store the names of variables that have been printed
            // already.  If they have been printed, they will be skipped.
            // Using a vector here since we don't know how many variables will
            // be printed and it allows expansion as necessary
            Vector<String> printedVariables = new Vector<String>(10,5);
            // index over variables
            for (int i=0; i<varList.size(); i++) {
                int varNum = varList.get(i).intValue();
                VariableValue var = _varModel.getVariable(varNum);
                String name = var.label();
                if (name == null) name = var.item();
                // Check if variable has been printed.  If not store it and print
                boolean alreadyPrinted = false;
                for (int j = 0; j < printedVariables.size(); j++) {
                  if (printedVariables.elementAt(j).toString() == name) alreadyPrinted = true;
                }
                //If already printed, skip it.  If not, store it and print
                if (alreadyPrinted == true) continue;
                printedVariables.addElement(name);

                String value = var.getTextValue();
                String originalName = name;
                String originalValue = value;
                name = name +" (CV" +var.getCvNum() + ")";

                //define index values for name and value substrings
                int nameLeftIndex = 0;
                int nameRightIndex = name.length();
                int valueLeftIndex = 0;
                int valueRightIndex =value.length();
                String trimmedName;
                String trimmedValue;

                // Check the name length to see if it is wider than the column.
                // If so, split it and do the same checks for the Value
                // Then concatenate the name and value (or the split versions thereof)
                // before writing - if split, repeat until all pieces have been output
                while ((valueLeftIndex < value.length()) || (nameLeftIndex < name.length())){
                  // name split code
                  if (name.substring(nameLeftIndex).length() > col1Width){
                    for (int j = 0; j < col1Width; j++) {
                      String delimiter = name.substring(nameLeftIndex + col1Width - j - 1,
                                                       nameLeftIndex + col1Width - j);
                      if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                        nameRightIndex = nameLeftIndex + col1Width - j;
                        break;
                      }
                    }
                    trimmedName = name.substring(nameLeftIndex,nameRightIndex);
                    nameLeftIndex = nameRightIndex;
                    int space = spaces.length()- trimmedName.length();
                    s = "   " + trimmedName + spaces.substring(0,space);
                  } else {
                    trimmedName = name.substring(nameLeftIndex);
                    int space = spaces.length() - trimmedName.length();
                    s = "   " + trimmedName + spaces.substring(0,space);
                    name = "";
                    nameLeftIndex = 0;
                  }
                  // value split code
                  if (value.substring(valueLeftIndex).length() > col2Width){
                    for (int j = 0; j < col2Width; j++){
                      String delimiter = value.substring(valueLeftIndex + col2Width - j - 1, valueLeftIndex + col2Width - j);
                      if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                        valueRightIndex = valueLeftIndex + col2Width - j;
                        break;
                      }
                    }
                    trimmedValue = value.substring(valueLeftIndex,valueRightIndex);
                    valueLeftIndex = valueRightIndex;
                    s= s + "   " + trimmedValue;
                  } else {
                    trimmedValue = value.substring(valueLeftIndex);
                    s = s + "   " + trimmedValue;
                    valueLeftIndex = 0;
                    value = "";
                  }
                  w.write(s,0,s.length());
                  w.writeBorders();
                  s = "\n";
                  w.write(s,0,s.length());
                }
                // Check for a Speed Table output and create a graphic display.
                // Java 1.5 has a known bug, #6328248, that prevents printing of progress
                //  bars using old style printing classes.  It results in blank bars on Windows,
                //  but hangs Macs. The version check is a workaround.
                float v = Float.valueOf(java.lang.System.getProperty("java.version").substring(0, 3)).floatValue();
                if (originalName.equals("Speed Table") && v < 1.5) {
                 // set the height of the speed table graph in lines
                 int speedFrameLineHeight = 11;
                 s = "\n";

                 // check that there is enough room on the page; if not,
                 // space down the rest of the page.
                 // don't use page break because we want the table borders to be written
                 // to the bottom of the page
                 int pageSize = w.getLinesPerPage();
                 int here = w.getCurrentLineNumber();
                 if (pageSize - here < speedFrameLineHeight) {
                   for (int j = 0; j < (pageSize - here); j++) {
                     w.writeBorders();
                     w.write(s,0,s.length());
                   }
                 }

                 // Now that there is page space, create the window to hold the graphic speed table
                 JWindow speedWindow = new JWindow();
                 // Window size as wide as possible to allow for largest type size
                 speedWindow.setSize(512,165);
                 speedWindow.getContentPane().setBackground(Color.white);
                 speedWindow.getContentPane().setLayout(null);
                 // in preparation for display, extract the speed table values into an array
                 StringTokenizer valueTokens = new StringTokenizer(originalValue,",",false);
                 int speedVals[] = new int[28];
                 int k = 0;
                 while (valueTokens.hasMoreTokens()) {
                     speedVals[k] = Integer.parseInt(valueTokens.nextToken());
                     k++;
                 }

                 // Now create a set of vertical progress bar whose length is based
                 // on the speed table value (half height) and add them to the window
                 for (int j = 0; j < 28; j++){
                   JProgressBar printerBar = new JProgressBar(JProgressBar.VERTICAL,0,127);
                   printerBar.setBounds(52+j*15, 19, 10, 127);
                   printerBar.setValue(speedVals[j]/2);
                   printerBar.setBackground(Color.white);
                   printerBar.setForeground(Color.darkGray);
                   printerBar.setBorder(BorderFactory.createLineBorder(Color.black));
                   speedWindow.getContentPane().add(printerBar);
                   // create a set of value labels at the top containing the speed table values
                   JLabel barValLabel = new JLabel(Integer.toString(speedVals[j]), SwingConstants.CENTER);
                   barValLabel.setBounds(50+j*15, 4, 15, 15);
                   barValLabel.setFont(new java.awt.Font("Monospaced", 0,7));
                   speedWindow.getContentPane().add(barValLabel);
                   //Create a set of labels at the bottom with the CV numbers in them
                   JLabel barCvLabel = new JLabel(Integer.toString(67+j),SwingConstants.CENTER);
                   barCvLabel.setBounds(50+j*15, 150, 15, 15);
                   barCvLabel.setFont(new java.awt.Font("Monospaced", 0,7));
                   speedWindow.getContentPane().add(barCvLabel);
                 }
                 JLabel cvLabel = new JLabel("Value");
                 cvLabel.setFont(new java.awt.Font("Monospaced", 0, 7));
                 cvLabel.setBounds(25,4,26,15);
                 speedWindow.getContentPane().add(cvLabel);
                 JLabel valueLabel = new JLabel("CV");
                 valueLabel.setFont(new java.awt.Font("Monospaced", 0, 7));
                 valueLabel.setBounds(37,150,13,15);
                 speedWindow.getContentPane().add(valueLabel);
                 // pass the complete window to the printing class
                 w.write(speedWindow);
                 // Now need to write the borders on sides of table
                 for (int j = 0; j < speedFrameLineHeight; j++) {
                   w.writeBorders();
                   w.write(s,0,s.length());
                 }
               }
           }

            final int TABLE_COLS = 3; 

            // index over CVs
            if (cvList.size() > 0){
//            Check how many Cvs there are to print
              int cvCount = cvList.size();
              w.setFontStyle(Font.BOLD); //set font to Bold
              // print a simple heading
              s = "                 Value                       Value                       Value";
              w.write(s, 0, s.length());
              w.writeBorders();
              s = "\n";
              w.write(s,0,s.length());
            s = "            CV  Dec Hex                 CV  Dec Hex                 CV  Dec Hex";
              w.write(s, 0, s.length());
              w.writeBorders();
              s = "\n";
              w.write(s,0,s.length());
              w.setFontStyle(0); //set font back to Normal
              //           }
              /*create an array to hold CV/Value strings to allow reformatting and sorting
                Same size as the table drawn above (TABLE_COLS columns*tableHeight; heading rows
                not included). Use the count of how many CVs there are to determine the number
                of table rows required.  Add one more row if the divison into TABLE_COLS columns
                isn't even.
               */
              int tableHeight = cvCount/TABLE_COLS;
              if (cvCount%TABLE_COLS > 0) tableHeight++;
              String[] cvStrings = new String[TABLE_COLS * tableHeight];

              //blank the array
              for (int j = 0; j < cvStrings.length; j++)
                cvStrings[j] = "";

                // get each CV and value
                int i = 0;
                for (int cvNum : cvList) {
                    CvValue cv = _cvModel.getCvByRow(cvNum);

                    int value = cv.getValue();

                    //convert and pad numbers as needed
                    String numString = String.format("%12s",cv.number());
                    String valueString = Integer.toString(value);
                    String valueStringHex = Integer.toHexString(value).toUpperCase();
                    if (value < 16)
                      valueStringHex = "0" + valueStringHex;
                    for (int j = 1; j < 3; j++) {
                      if (valueString.length() < 3)
                        valueString = " " + valueString;
                    }
                    //Create composite string of CV and its decimal and hex values
                    s = "  " + numString + "  " + valueString + "  " + valueStringHex +
                        " ";

                    //populate printing array - still treated as a single column
                    cvStrings[i] = s;
                    i++;
                }

                //sort the array in CV order (just the members with values)
                String temp;
                boolean swap = false;
                do {
                    swap = false;
                    for (i = 0; i < _cvModel.getRowCount() - 1; i++) {
                        if ( PrintCvAction.cvSortOrderVal(cvStrings[i + 1].substring(0,15).trim()) < PrintCvAction.cvSortOrderVal(cvStrings[i].substring(0,15).trim()) ) {
                            temp = cvStrings[i + 1];
                            cvStrings[i + 1] = cvStrings[i];
                            cvStrings[i] = temp;
                            swap = true;
                        }
                    }
                } while (swap == true);

                //Print the array in four columns
                for (i = 0; i < tableHeight; i++) {
                  s = cvStrings[i] + "    " + cvStrings[i + tableHeight] + "    " + cvStrings[i +
                      tableHeight * 2];
                  w.write(s, 0, s.length());
                 w.writeBorders();
                 s = "\n";
                  w.write(s,0,s.length());
                }
            }
              s = "\n";
              w.writeBorders();
              w.write(s, 0, s.length());
              w.writeBorders();
              w.write(s, 0, s.length());

            // handle special cases

        } catch (IOException e) { log.warn("error during printing: "+e);
        }

    }

    private JPanel addDccAddressPanel(Element e) {
        JPanel l = new DccAddressPanel(_varModel);
        panelList.add(l);
        // make sure this will get read/written, even if real vars not on pane
        int iVar;

        // note we want Short Address first, as it might change others
        iVar = _varModel.findVarIndex("Short Address");
        if (iVar>=0) varList.add(Integer.valueOf(iVar));
        else log.debug("addDccAddressPanel did not find Short Address");

        iVar = _varModel.findVarIndex("Address Format");
        if (iVar>=0) varList.add(Integer.valueOf(iVar));
        else log.debug("addDccAddressPanel did not find Address Format");

        iVar = _varModel.findVarIndex("Long Address");
        if (iVar>=0) varList.add(Integer.valueOf(iVar));
        else log.debug("addDccAddressPanel did not find Long Address");

        // included here because CV1 can modify it, even if it doesn't show on pane;
        iVar = _varModel.findVarIndex("Consist Address");
        if (iVar>=0) varList.add(Integer.valueOf(iVar));
        else log.debug("addDccAddressPanel did not find CV19 Consist Address");

        return l;
    }

    static Logger log = LoggerFactory.getLogger(PaneProgPane.class.getName());

}
