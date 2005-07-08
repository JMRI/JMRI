// IndexedCvTableModel.java

package jmri.jmrit.symbolicprog;

import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;

import java.util.Vector;

import jmri.*;

/**
 * Table data model for display of IndexedCvValues in symbolic programmer.
 *
 * This represents the contents of a single decoder, so the
 * Programmer used to access it is a data member.
 *
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision: 1.3 $
 */
public class IndexedCvTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

    private int _numRows = 0;                // must be zero until Vectors are initialized
    static final int MAXCVNUM = 256;
    private Vector _indxCvDisplayVector = new Vector();  // vector of CvValue objects, in display order
    private Vector _indxCvAllVector = new Vector(MAXCVNUM + 1);  // vector of all possible indexed CV objects
    public  Vector allIndxCvVector() { return _indxCvAllVector; }
    private Vector _indxWriteButtons = new Vector();
    private Vector _indxReadButtons = new Vector();
//    private Vector _indxToolTipText = new Vector();
    private Programmer mProgrammer;

    // Defines the columns
    private static final int NAMECOLUMN   = 0;
    private static final int PICOLUMN     = 1;
    private static final int SICOLUMN     = 2;
    private static final int CVCOLUMN     = 3;
    private static final int VALCOLUMN    = 4;
    private static final int STATECOLUMN  = 5;
    private static final int READCOLUMN   = 6;
    private static final int WRITECOLUMN  = 7;
    private static final int HIGHESTCOLUMN = WRITECOLUMN + 1;

    private JLabel _status = null;

    public JLabel getStatusLabel() { return _status;}

    public IndexedCvTableModel(JLabel status, Programmer pProgrammer) {
        super();

        mProgrammer = pProgrammer;
        // save a place for notification
        _status = status;
        // initialize the MAXCVNUM+1 long _cvAllVector;
        for (int i=0; i<=MAXCVNUM; i++) _indxCvAllVector.addElement(null);
    }

    /**
     * Gives access to the programmer used to reach these Indexed CVs,
     * so you can check on mode, capabilities, etc.
     * @return Programmer object for the Indexed CVs
     */
    public Programmer getProgrammer() {
        return mProgrammer;
    }

    // basic methods for AbstractTableModel implementation
    public int getRowCount() { return _numRows; }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case NAMECOLUMN:  return "Name";
        case PICOLUMN:    return "PI Val";
        case SICOLUMN:    return "SI Val";
        case CVCOLUMN:    return "CV Num";
        case VALCOLUMN:   return "Value";
        case STATECOLUMN: return "State";
        case READCOLUMN:  return "Read";
        case WRITECOLUMN: return "Write";
        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case NAMECOLUMN:  return String.class;
        case PICOLUMN:    return String.class;
        case SICOLUMN:    return String.class;
        case CVCOLUMN:    return String.class;
        case VALCOLUMN:   return JTextField.class;
        case STATECOLUMN: return String.class;
        case READCOLUMN:  return JButton.class;
        case WRITECOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case NAMECOLUMN:  return false;
        case PICOLUMN:    return false;
        case SICOLUMN:    return false;
        case CVCOLUMN:    return false;
        case VALCOLUMN:
            if ( ((CvValue)_indxCvDisplayVector.elementAt(row)).getReadOnly() ||
                 ((CvValue)_indxCvDisplayVector.elementAt(row)).getInfoOnly() ) {
                return false;
            } else {
                return true;
            }
        case STATECOLUMN: return false;
        case READCOLUMN:  return true;
        case WRITECOLUMN: return true;
        default: return false;
        }
    }

    public String getName(int row) {  // name is text number
        return ""+((CvValue)_indxCvDisplayVector.elementAt(row)).cvName();
    }

    public String getValString(int row) {
        return ""+((CvValue)_indxCvDisplayVector.elementAt(row)).getValue();
    }

    public int getCvByName(String name) {
        int row = 0;
        while(row < _numRows) {
            if(((CvValue)_indxCvDisplayVector.elementAt(row)).cvName().compareTo(name) == 0) {
                return row;
            }
            row++;
        }
        return -1;
    }

    public CvValue getCvByRow(int row) { return ((CvValue)_indxCvDisplayVector.elementAt(row)); }
    public CvValue getCvByNumber(int row) { return ((CvValue)_indxCvAllVector.elementAt(row)); }

    public Object getValueAt(int row, int col) {
        switch (col) {
            case NAMECOLUMN:
                return ""+((CvValue)_indxCvDisplayVector.elementAt(row)).cvName();
            case PICOLUMN:
                return ""+((CvValue)_indxCvDisplayVector.elementAt(row)).piVal();
            case SICOLUMN:
                return ""+((CvValue)_indxCvDisplayVector.elementAt(row)).siVal();
            case CVCOLUMN:
                return ""+((CvValue)_indxCvDisplayVector.elementAt(row)).iCv();
            case VALCOLUMN:
                return ((CvValue)_indxCvDisplayVector.elementAt(row)).getTableEntry();
            case STATECOLUMN:
                int state = ((CvValue)_indxCvDisplayVector.elementAt(row)).getState();
                switch (state) {
                    case CvValue.UNKNOWN:  return "Unknown";
                    case CvValue.READ:     return "Read";
                    case CvValue.EDITED:   return "Edited";
                    case CvValue.STORED:   return "Stored";
                    case CvValue.FROMFILE: return "From file";
                    default: return "inconsistent";
                }
            case READCOLUMN:
                return _indxReadButtons.elementAt(row);
            case WRITECOLUMN:
                return _indxWriteButtons.elementAt(row);
            default: return "unknown";
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case VALCOLUMN: // Object is actually an Integer
                if (((CvValue)_indxCvDisplayVector.elementAt(row)).getValue() != ((Integer)value).intValue()) {
                    ((CvValue) _indxCvDisplayVector.elementAt(row)).setValue(((Integer)value).intValue());
                }
                break;
            default:
                break;
        }
    }

    private int _row;

    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("action command: "+e.getActionCommand());
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        _row = row;
        if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
        if (b=='R') {
            // read command
            indexedRead();
        } else {
            // write command
            indexedWrite();
        }
    }

    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int WRITING_PI4R = 1;
    private static final int WRITING_PI4W = 2;
    private static final int WRITING_SI4R = 3;
    private static final int WRITING_SI4W = 4;
    private static final int READING_CV = 5;
    private static final int WRITING_CV = 6;

    public void indexedRead() {
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
        // lets skip the SI step if SI is not used
        if (((CvValue)_indxCvDisplayVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        } else {
            _progState = WRITING_SI4R;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV read");
        // to read any indexed CV we must write the PI
        ((CvValue)_indxCvDisplayVector.elementAt(_row)).writePI(_status);
    }

    public void indexedWrite() {
        if (((CvValue)_indxCvDisplayVector.elementAt(_row)).getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
        // lets skip the SI step if SI is not used
        if (((CvValue)_indxCvDisplayVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        // to write any indexed CV we must write the PI
        ((CvValue)_indxCvDisplayVector.elementAt(_row)).writePI(_status);
    }

    public void propertyChange(PropertyChangeEvent e) {

        if (log.isDebugEnabled()) log.debug("Property changed: "+e.getPropertyName());
        // notification from Indexed CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
            case IDLE:  // no, just an Indexed CV update
                if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
                return;
            case WRITING_PI4R:   // have written the PI, now write SI if needed
            case WRITING_PI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");
                _progState = (_progState == WRITING_PI4R ? WRITING_SI4R : WRITING_SI4W);
                ((CvValue)_indxCvDisplayVector.elementAt(_row)).writeSI(_status);
                return;
            case WRITING_SI4R:  // have written the SI if needed, now read or write CV
            case WRITING_SI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");
                if (_progState == WRITING_SI4R ) {
                    _progState = READING_CV;
                    ((CvValue)_indxCvDisplayVector.elementAt(_row)).readIcV(_status);
                } else {
                    _progState = WRITING_CV;
                    ((CvValue)_indxCvDisplayVector.elementAt(_row)).writeIcV(_status);
                }
                return;
            case READING_CV:  // now done with the read request
                if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV");
                _progState = IDLE;
                return;
            case WRITING_CV:  // now done with the write request
                if (log.isDebugEnabled()) log.debug("Finished writing the Indexed CV");
                _progState = IDLE;
                return;
            default:  // unexpected!
                log.error("Unexpected state found: "+_progState);
                _progState = IDLE;
                return;
            }
        }
        fireTableDataChanged();
    }

    /**
     * return is the current row or the row of an existing Indexed CV
     */
    public int addIndxCV(int row, String cvName,
                         int piCv, int piVal,
                         int siCv, int siVal,
                         int iCv,
                         boolean readOnly, boolean infoOnly, boolean writeOnly) {
        int existingRow = getCvByName(cvName);
        if (existingRow == -1 && _indxCvAllVector.elementAt(row) == null) {
            CvValue indxCv = new CvValue(row, cvName, piCv, piVal, siCv, siVal, iCv, mProgrammer);
            indxCv.setReadOnly(readOnly);
            indxCv.setInfoOnly(infoOnly);
            _indxCvAllVector.setElementAt(indxCv, row);
            _indxCvDisplayVector.addElement(indxCv);
            // connect to this Indexed CV to ensure the table display updates
            indxCv.addPropertyChangeListener(this);

            JButton bw = new JButton("Write");
            _indxWriteButtons.addElement(bw);
            JButton br = new JButton("Read");
            _indxReadButtons.addElement(br);
            if (infoOnly || readOnly) {
                if (writeOnly) {
                    bw.setEnabled(true);
                    bw.setActionCommand("W"+_numRows);
                    bw.addActionListener(this);
                } else {
                    bw.setEnabled(false);
                }
                if (infoOnly) {
                    br.setEnabled(false);
                } else {
                    br.setEnabled(true);
                    br.setActionCommand("R"+_numRows);
                    br.addActionListener(this);
                }
            } else {
                bw.setEnabled(true);
                bw.setActionCommand("W"+_numRows);
                bw.addActionListener(this);
                if (writeOnly) {
                    br.setEnabled(false);
                } else {
                    br.setEnabled(true);
                    br.setActionCommand("R" + _numRows);
                    br.addActionListener(this);
                }
           }
            _numRows++;
            fireTableDataChanged();
        } else { // this one already exists
            row = existingRow;
        }
        // make sure readonly set true if required
        CvValue indxcv = (CvValue) _indxCvAllVector.elementAt(row);
        if (readOnly) indxcv.setReadOnly(readOnly);
        if (infoOnly) {
            indxcv.setReadOnly(infoOnly);
            indxcv.setInfoOnly(infoOnly);
        }
        if (writeOnly) indxcv.setWriteOnly(writeOnly);
        return row;
    }

    public boolean decoderDirty() {
        int len = _indxCvDisplayVector.size();
        for (int i=0; i< len; i++) {
            if (((CvValue)(_indxCvDisplayVector.elementAt(i))).getState() == CvValue.EDITED ) {
                if (log.isDebugEnabled())
                    log.debug("CV decoder dirty due to "+((CvValue)(_indxCvDisplayVector.elementAt(i))).number());
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove buttons
        for (int i = 0; i<_indxWriteButtons.size(); i++) {
            ((JButton)_indxWriteButtons.elementAt(i)).removeActionListener(this);
        }
        for (int i = 0; i<_indxReadButtons.size(); i++) {
            ((JButton)_indxReadButtons.elementAt(i)).removeActionListener(this);
        }

        // remove CV listeners
        for (int i = 0; i<_indxCvDisplayVector.size(); i++) {
            ((CvValue)_indxCvDisplayVector.elementAt(i)).removePropertyChangeListener(this);
        }

        // null references, so that they can be gc'd even if this isn't.
        _indxCvDisplayVector.removeAllElements();
        _indxCvDisplayVector = null;

        _indxCvAllVector.removeAllElements();
        _indxCvAllVector = null;

        _indxWriteButtons.removeAllElements();
        _indxWriteButtons = null;

        _indxReadButtons.removeAllElements();
        _indxReadButtons = null;

        _status = null;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IndexedCvTableModel.class.getName());
}
