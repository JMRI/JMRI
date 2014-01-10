// IndexedCvTableModel.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.event.*;
import java.beans.*;

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
 * @author 		Daniel Boudreau Copyright (C) 2007
 * @version   $Revision$
 */
public class IndexedCvTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

    private int _numRows = 0;                // must be zero until Vectors are initialized
    static final int MAXCVNUM = 1200;
    private Vector<CvValue> _indxCvDisplayVector = new Vector<CvValue>();  // vector of CvValue objects, in display order
    private Vector<CvValue> _indxCvAllVector = new Vector<CvValue>(MAXCVNUM + 1);  // vector of all possible indexed CV objects
    public  Vector<CvValue> allIndxCvVector() { return _indxCvAllVector; }
    private Vector<JButton> _indxWriteButtons = new Vector<JButton>();
    private Vector<JButton> _indxReadButtons = new Vector<JButton>();
    private Vector<JButton> _indxCompareButtons = new Vector<JButton>();
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
    private static final int COMPARECOLUMN  = 8;
    private static final int HIGHESTCOLUMN = COMPARECOLUMN + 1;

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
     * Find the existing IndexedCV
     * that matches a particular name
     */
    public CvValue getMatchingIndexedCV(String name) {
        for (int i = 0; i<_numRows; i++) {
            CvValue cv = _indxCvAllVector.get(i);
            if (cv == null) {
                // no longer should run off end
                log.error("cv == null in getMatchingIndexedCV");
                break;
            }            
            if (cv.cvName().equals(name)) {
                return cv;
            }
        }
        return null;
    }
    
    /**
     * Gives access to the programmer used to reach these Indexed CVs,
     * so you can check on mode, capabilities, etc.
     * @return Programmer object for the Indexed CVs
     */
    public Programmer getProgrammer() {
        return mProgrammer;
    }
    
    public void setProgrammer(Programmer p) { 
        mProgrammer = p;

        // tell all existing
        for (CvValue cv : _indxCvDisplayVector) {
            if (cv!=null) cv.setProgrammer(p);
        }
        for (CvValue cv : _indxCvAllVector) {
            if (cv!=null) cv.setProgrammer(p);
        }
        
        log.debug("Set programmer in "+_indxCvAllVector.size()+"CVs");
    }


    // basic methods for AbstractTableModel implementation
    public int getRowCount() { return _numRows; }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case NAMECOLUMN:  return Bundle.getMessage("ColumnNameNumber");
        case PICOLUMN:    return "PI Val";
        case SICOLUMN:    return "SI Val";
        case CVCOLUMN:    return "CV Num";
        case VALCOLUMN:   return Bundle.getMessage("ColumnNameValue");
        case STATECOLUMN: return Bundle.getMessage("ColumnNameState");
        case READCOLUMN:  return Bundle.getMessage("ColumnNameRead");
        case WRITECOLUMN: return Bundle.getMessage("ColumnNameWrite");
        case COMPARECOLUMN: return Bundle.getMessage("ColumnNameCompare");
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case NAMECOLUMN:  return String.class;
        case PICOLUMN:    return String.class;
        case SICOLUMN:    return String.class;
        case CVCOLUMN:    return String.class;
        case VALCOLUMN:   return JTextField.class;
        case STATECOLUMN: return String.class;
        case READCOLUMN:  return JButton.class;
        case WRITECOLUMN: return JButton.class;
        case COMPARECOLUMN: return JButton.class;
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
            if (_indxCvDisplayVector.elementAt(row).getReadOnly() ||
                 _indxCvDisplayVector.elementAt(row).getInfoOnly() ) {
                return false;
            } else {
                return true;
            }
        case STATECOLUMN: return false;
        case READCOLUMN:  return true;
        case WRITECOLUMN: return true;
        case COMPARECOLUMN: return true;
        default: return false;
        }
    }

    public String getName(int row) {  // name is text number
        return ""+_indxCvDisplayVector.elementAt(row).cvName();
    }

    public String getValString(int row) {
        return ""+_indxCvDisplayVector.elementAt(row).getValue();
    }

    public int getCvByName(String name) {
        int row = 0;
        while(row < _numRows) {
            if(_indxCvDisplayVector.elementAt(row).cvName().compareTo(name) == 0) {
                return row;
            }
            row++;
        }
        return -1;
    }

    public CvValue getCvByRow(int row) { return _indxCvDisplayVector.elementAt(row); }
    public CvValue getCvByNumber(int row) { return _indxCvAllVector.elementAt(row); }

    public Object getValueAt(int row, int col) {
    	// some error checking
    	if (row >= _indxCvDisplayVector.size()){
    		log.debug("row greater than cv index");
    		return "Error";
    	}
        switch (col) {
            case NAMECOLUMN:
                return ""+(_indxCvDisplayVector.elementAt(row)).cvName();
            case PICOLUMN:
                return ""+(_indxCvDisplayVector.elementAt(row)).piVal();
            case SICOLUMN:
                return ""+(_indxCvDisplayVector.elementAt(row)).siVal();
            case CVCOLUMN:
                return ""+(_indxCvDisplayVector.elementAt(row)).iCv();
            case VALCOLUMN:
                return (_indxCvDisplayVector.elementAt(row)).getTableEntry();
            case STATECOLUMN:
            	int state = (_indxCvDisplayVector.elementAt(row)).getState();
            	switch (state) {
            		case CvValue.UNKNOWN:  		return Bundle.getMessage("CvStateUnknown");
            		case CvValue.READ:  		return Bundle.getMessage("CvStateRead");
            		case CvValue.EDITED:  		return Bundle.getMessage("CvStateEdited");
            		case CvValue.STORED:  		return Bundle.getMessage("CvStateStored");
            		case CvValue.FROMFILE:  	return Bundle.getMessage("CvStateFromFile");
            		case CvValue.SAME:  		return Bundle.getMessage("CvStateSame");
            		case CvValue.DIFF:  		return Bundle.getMessage("CvStateDiff")+ " " +
            									(_indxCvDisplayVector.elementAt(row)).getDecoderValue();
            		default: return "inconsistent";
            	}
            case READCOLUMN:
                return _indxReadButtons.elementAt(row);
            case WRITECOLUMN:
                return _indxWriteButtons.elementAt(row);
            case COMPARECOLUMN:
                return _indxCompareButtons.elementAt(row);
            default: return "unknown";
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case VALCOLUMN: // Object is actually an Integer
                if ((_indxCvDisplayVector.elementAt(row)).getValue() != ((Integer)value).intValue()) {
                    ( _indxCvDisplayVector.elementAt(row)).setValue(((Integer)value).intValue());
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
        } else if (b=='C'){
        	// compare command
        	indexedCompare();
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
    private static final int WRITING_PI4C = 7;
    private static final int WRITING_SI4C = 8;
    private static final int COMPARE_CV = 9;
    

    /**
     * Count number of retries done
     */
    private int retries = 0;
    
    /**
     * Define maximum number of retries of read/write operations before moving on
     */
    private static final int RETRY_MAX = 2;

    public void indexedRead() {
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
        // lets skip the SI step if SI is not used
        if ((_indxCvDisplayVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        } else {
            _progState = WRITING_SI4R;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV read");
        // to read any indexed CV we must write the PI
        (_indxCvDisplayVector.elementAt(_row)).writePI(_status);
    }

    public void indexedWrite() {
        if ((_indxCvDisplayVector.elementAt(_row)).getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
        // lets skip the SI step if SI is not used
        if ((_indxCvDisplayVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        // to write any indexed CV we must write the PI
        (_indxCvDisplayVector.elementAt(_row)).writePI(_status);
    }
    
    public void indexedCompare() {
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
        // lets skip the SI step if SI is not used
        if ((_indxCvDisplayVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4C;
        } else {
            _progState = WRITING_SI4C;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV compare");
        // to read any indexed CV we must write the PI
        (_indxCvDisplayVector.elementAt(_row)).writePI(_status);
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
            case WRITING_PI4C:
            case WRITING_PI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");

                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_indxCvDisplayVector.elementAt(_row)).getState() != CvValue.STORED) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_indxCvDisplayVector.elementAt(_row)).writePI(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

                if (_progState == WRITING_PI4R )
                	_progState = WRITING_SI4R;
                else if (_progState == WRITING_PI4C )
                	_progState = WRITING_SI4C;
                else
                	_progState = WRITING_SI4W;
                (_indxCvDisplayVector.elementAt(_row)).writeSI(_status);
                return;
            case WRITING_SI4R:  // have written the SI if needed, now read or write CV
            case WRITING_SI4C:
            case WRITING_SI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");

                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_indxCvDisplayVector.elementAt(_row)).getState() != CvValue.STORED) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_indxCvDisplayVector.elementAt(_row)).writeSI(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

                if (_progState == WRITING_SI4R ) {
                    _progState = READING_CV;
                    (_indxCvDisplayVector.elementAt(_row)).readIcV(_status);
                } else if (_progState == WRITING_SI4C ) {
                    _progState = COMPARE_CV;
                    (_indxCvDisplayVector.elementAt(_row)).confirmIcV(_status);
                 } else {
                    _progState = WRITING_CV;
                    (_indxCvDisplayVector.elementAt(_row)).writeIcV(_status);
                }
                return;
            case READING_CV:  // now done with the read request
                if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV");

                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_indxCvDisplayVector.elementAt(_row)).getState() != CvValue.READ) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_indxCvDisplayVector.elementAt(_row)).readIcV(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

                _progState = IDLE;
                return;
            case COMPARE_CV:  // now done with the read request
                if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV for compare");

                // check for success SAME or DIFF?
                if ((retries < RETRY_MAX)
						&& (( _indxCvDisplayVector.elementAt(_row))
								.getState() != CvValue.SAME)
						&& (( _indxCvDisplayVector.elementAt(_row))
								.getState() != CvValue.DIFF)) {
					// need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_indxCvDisplayVector.elementAt(_row)).confirmIcV(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

                _progState = IDLE;
                return;
            case WRITING_CV:  // now done with the write request
                if (log.isDebugEnabled()) log.debug("Finished writing the Indexed CV");

                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_indxCvDisplayVector.elementAt(_row)).getState() != CvValue.STORED) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_indxCvDisplayVector.elementAt(_row)).writeIcV(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

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
        if (existingRow == -1) {
            // we'll be adding a new entry or replacing an existing one; where?
            row = _numRows++;
            
            // create new entry
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
            JButton bc = new JButton("Compare");
            _indxCompareButtons.addElement(bc);
            
            if (infoOnly || readOnly) {
                if (writeOnly) {
                    bw.setEnabled(true);
                    bw.setActionCommand("W"+row);
                    bw.addActionListener(this);
                } else {
                    bw.setEnabled(false);
                }
                if (infoOnly) {
                    br.setEnabled(false);
                    bc.setEnabled(false);
                } else {
                    br.setEnabled(true);
                    br.setActionCommand("R"+row);
                    br.addActionListener(this);
                    bc.setEnabled(true);
                    bc.setActionCommand("C"+row);
                    bc.addActionListener(this);
                }
            } else {
                bw.setEnabled(true);
                bw.setActionCommand("W"+row);
                bw.addActionListener(this);
                if (writeOnly) {
                    br.setEnabled(false);
                    bc.setEnabled(false);
                } else {
                    br.setEnabled(true);
                    br.setActionCommand("R" + row);
                    br.addActionListener(this);
                    bc.setEnabled(true);
                    bc.setActionCommand("C" + row);
                    bc.addActionListener(this);
                }
           }
           if (log.isDebugEnabled()) log.debug("addIndxCV adds row at "+row);
            fireTableDataChanged();
        } else { // this one already exists
           if (log.isDebugEnabled()) 
                log.debug("addIndxCV finds existing row of "+existingRow+" with numRows "+_numRows);
            row = existingRow;
        }
        // make sure readonly set true if required
        if (row > -1 && row < _indxCvAllVector.size() )
        {
            CvValue indxcv =  _indxCvAllVector.elementAt(row);
            if (readOnly) indxcv.setReadOnly(readOnly);
            if (infoOnly) {
                indxcv.setReadOnly(infoOnly);
                indxcv.setInfoOnly(infoOnly);
            }
            if (writeOnly) indxcv.setWriteOnly(writeOnly);
        }
        return row;
    }

    public boolean decoderDirty() {
        int len = _indxCvDisplayVector.size();
        for (int i=0; i< len; i++) {
            if (((_indxCvDisplayVector.elementAt(i))).getState() == CvValue.EDITED ) {
                if (log.isDebugEnabled())
                    log.debug("CV decoder dirty due to "+((_indxCvDisplayVector.elementAt(i))).number());
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove buttons
        for (int i = 0; i<_indxWriteButtons.size(); i++) {
            _indxWriteButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i<_indxReadButtons.size(); i++) {
            _indxReadButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i<_indxCompareButtons.size(); i++) {
            _indxCompareButtons.elementAt(i).removeActionListener(this);
        }

        // remove CV listeners
        for (int i = 0; i<_indxCvDisplayVector.size(); i++) {
            (_indxCvDisplayVector.elementAt(i)).removePropertyChangeListener(this);
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
        
        _indxCompareButtons.removeAllElements();
        _indxCompareButtons = null;

        _status = null;
    }

    static Logger log = LoggerFactory.getLogger(IndexedCvTableModel.class.getName());
}
