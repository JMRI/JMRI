// CvTableModel.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import java.util.Vector;

import jmri.*;

/**
 * Table data model for display of CvValues in symbolic programmer.
 * <P>This represents the contents of a single decoder, so the
 * Programmer used to access it is a data member.
 *
 * @author    Bob Jacobsen   Copyright (C) 2001, 2002, 2006
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision$
 */
public class CvTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {
	
	private int _numRows = 0;                // must be zero until Vectors are initialized
    static final int MAXCVNUM = 1024;
    private Vector<CvValue> _cvDisplayVector = new Vector<CvValue>();  // vector of CvValue objects, in display order
    private Vector<CvValue> _cvAllVector = new Vector<CvValue>(MAXCVNUM+1);  // vector of all possible CV objects
    public Vector<CvValue> allCvVector() { return _cvAllVector; }
    private Vector<JButton> _writeButtons = new Vector<JButton>();
    private Vector<JButton> _readButtons = new Vector<JButton>();
    private Vector<JButton> _compareButtons = new Vector<JButton>();
    private Programmer mProgrammer;

    // Defines the columns
    private static final int NUMCOLUMN   = 0;
    private static final int VALCOLUMN   = 1;
    private static final int STATECOLUMN = 2;
    private static final int READCOLUMN  = 3;
    private static final int WRITECOLUMN = 4;
    private static final int COMPARECOLUMN = 5;
    
    private static final int HIGHESTCOLUMN = COMPARECOLUMN+1;
    private static final int HIGHESTNOPROG = STATECOLUMN+1;

    private JLabel _status = null;

    public JLabel getStatusLabel() { return _status;}

    public CvTableModel(JLabel status, Programmer pProgrammer) {
        super();

        mProgrammer = pProgrammer;
        // save a place for notification
        _status = status;
        // initialize the MAXCVNUM+1 long _cvAllVector;
        for (int i=0; i<=MAXCVNUM; i++) _cvAllVector.addElement(null);

        // define just address CV at start, pending some variables
        // boudreau: not sure why we need the statement below, 
        // messes up building CV table for CV #1 when in ops mode.
        //addCV("1", false, false, false);
    }

    /**
     * Gives access to the programmer used to reach these CVs, so
     * you can check on mode, capabilities, etc.
     * @return Programmer object for the CVs
     */
    public Programmer getProgrammer() {
        return mProgrammer;
    }

    // basic methods for AbstractTableModel implementation
    public int getRowCount() { return _numRows; }

    public int getColumnCount( ){ 
        if (getProgrammer()!=null)
            return HIGHESTCOLUMN;
        else 
            return HIGHESTNOPROG;
    }

    public String getColumnName(int col) {
        switch (col) {
        case NUMCOLUMN: return Bundle.getMessage("ColumnNameNumber");
        case VALCOLUMN: return Bundle.getMessage("ColumnNameValue");
        case STATECOLUMN: return Bundle.getMessage("ColumnNameState");
        case READCOLUMN: return Bundle.getMessage("ColumnNameRead");
        case WRITECOLUMN: return Bundle.getMessage("ColumnNameWrite");
        case COMPARECOLUMN: return Bundle.getMessage("ColumnNameCompare");
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case NUMCOLUMN: return Integer.class;
        case VALCOLUMN: return JTextField.class;
        case STATECOLUMN: return String.class;
        case READCOLUMN: return JButton.class;
        case WRITECOLUMN: return JButton.class;
        case COMPARECOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case NUMCOLUMN: return false;
        case VALCOLUMN:
            if (_cvDisplayVector.elementAt(row).getReadOnly() ||
                 _cvDisplayVector.elementAt(row).getInfoOnly() ) {
                return false;
            } else {
                return true;
            }
        case STATECOLUMN: return false;
        case READCOLUMN: return true;
        case WRITECOLUMN: return true;
        case COMPARECOLUMN: return true;
        default: return false;
        }
    }

    public String getName(int row) {  // name is text number
        return ""+_cvDisplayVector.elementAt(row).number();
    }

    public String getValString(int row) {
        return ""+_cvDisplayVector.elementAt(row).getValue();
    }

    public CvValue getCvByRow(int row) { return _cvDisplayVector.elementAt(row); }
    public CvValue getCvByNumber(int row) { return _cvAllVector.elementAt(row); }

    public Object getValueAt(int row, int col) {
        switch (col) {
        case NUMCOLUMN:
            return (Integer)_cvDisplayVector.elementAt(row).number();
        case VALCOLUMN:
            return _cvDisplayVector.elementAt(row).getTableEntry();
        case STATECOLUMN:
            int state = _cvDisplayVector.elementAt(row).getState();
            switch (state) {
            case CvValue.UNKNOWN:  		return Bundle.getMessage("CvStateUnknown");
            case CvValue.READ:  		return Bundle.getMessage("CvStateRead");
            case CvValue.EDITED:  		return Bundle.getMessage("CvStateEdited");
            case CvValue.STORED:  		return Bundle.getMessage("CvStateStored");
            case CvValue.FROMFILE:  	return Bundle.getMessage("CvStateFromFile");
            case CvValue.SAME:  		return Bundle.getMessage("CvStateSame");
            case CvValue.DIFF:  		return Bundle.getMessage("CvStateDiff")+ " " +
            							_cvDisplayVector.elementAt(row).getDecoderValue();
            default: return "inconsistent";
            }
        case READCOLUMN:
            return _readButtons.elementAt(row);
        case WRITECOLUMN:
            return _writeButtons.elementAt(row);
        case COMPARECOLUMN:
            return _compareButtons.elementAt(row);
        default: return "unknown";
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case VALCOLUMN: // Object is actually an Integer
          if (_cvDisplayVector.elementAt(row).getValue() != ((Integer)value).intValue()) {
              _cvDisplayVector.elementAt(row).setValue(((Integer)value).intValue());
          }
            break;
        default:
            break;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("action command: "+e.getActionCommand());
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
        if (b=='R') {
            // read command
            _cvDisplayVector.elementAt(row).read(_status);
        } else if (b=='C'){
        	// compare command
        	_cvDisplayVector.elementAt(row).confirm(_status);
        } else {
            // write command
            _cvDisplayVector.elementAt(row).write(_status);
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        fireTableDataChanged();
    }

    public void addCV(String s, boolean readOnly, boolean infoOnly, boolean writeOnly) {
        int num = Integer.valueOf(s).intValue();
        if (_cvAllVector.elementAt(num) == null) {
            CvValue cv = new CvValue(num, mProgrammer);
            cv.setReadOnly(readOnly);
            _cvAllVector.setElementAt(cv, num);
            _cvDisplayVector.addElement(cv);
            // connect to this CV to ensure the table display updates
            cv.addPropertyChangeListener(this);
            JButton bw = new JButton(Bundle.getMessage("ButtonWrite"));
            _writeButtons.addElement(bw);
            JButton br = new JButton(Bundle.getMessage("ButtonRead"));
            _readButtons.addElement(br);
            JButton bc = new JButton(Bundle.getMessage("ButtonCompare"));
            _compareButtons.addElement(bc);
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
                    bc.setEnabled(false);
                } else {
                    br.setEnabled(true);
                    br.setActionCommand("R"+_numRows);
                    br.addActionListener(this);
                    bc.setEnabled(true);
                    bc.setActionCommand("C"+_numRows);
                    bc.addActionListener(this);
                }
            } else {
                bw.setEnabled(true);
                bw.setActionCommand("W"+_numRows);
                bw.addActionListener(this);
                if (writeOnly) {
                    br.setEnabled(false);
                    bc.setEnabled(false);
                } else {
                    br.setEnabled(true);
                    br.setActionCommand("R" + _numRows);
                    br.addActionListener(this);
                    bc.setEnabled(true);
                    bc.setActionCommand("C" + _numRows);
                    bc.addActionListener(this);
                }
           }
            _numRows++;
            fireTableDataChanged();
        }
        // make sure readonly set true if required
        CvValue cv = _cvAllVector.elementAt(num);
        if (readOnly) cv.setReadOnly(readOnly);
        if (infoOnly) {
            cv.setReadOnly(infoOnly);
            cv.setInfoOnly(infoOnly);
        }
        if (writeOnly) cv.setWriteOnly(writeOnly);
    }

    public boolean decoderDirty() {
        int len = _cvDisplayVector.size();
        for (int i=0; i< len; i++) {
            if (_cvDisplayVector.elementAt(i).getState() == CvValue.EDITED ) {
                if (log.isDebugEnabled())
                    log.debug("CV decoder dirty due to "+_cvDisplayVector.elementAt(i).number());
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove buttons
        for (int i = 0; i<_writeButtons.size(); i++) {
            _writeButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i<_readButtons.size(); i++) {
            _readButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i<_compareButtons.size(); i++) {
            _compareButtons.elementAt(i).removeActionListener(this);
        }

        // remove CV listeners
        for (int i = 0; i<_cvDisplayVector.size(); i++) {
            _cvDisplayVector.elementAt(i).removePropertyChangeListener(this);
        }

        // null references, so that they can be gc'd even if this isn't.
        _cvDisplayVector.removeAllElements();
        _cvDisplayVector = null;

        _cvAllVector.removeAllElements();
        _cvAllVector = null;

        _writeButtons.removeAllElements();
        _writeButtons = null;

        _readButtons.removeAllElements();
        _readButtons = null;
        
        _compareButtons.removeAllElements();
        _compareButtons = null;

        _status = null;
    }

    static Logger log = LoggerFactory.getLogger(CvTableModel.class.getName());
}

