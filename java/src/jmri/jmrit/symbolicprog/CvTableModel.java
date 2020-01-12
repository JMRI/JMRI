package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.Programmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of CvValues in symbolic programmer.
 * <p>
 * This represents the contents of a single decoder, so the Programmer used to
 * access it is a data member.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2006
 * @author Howard G. Penny Copyright (C) 2005
 */
public class CvTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

    private int _numRows = 0;                // must be zero until Vectors are initialized
    static final int MAXCVNUM = 1024;
    private Vector<CvValue> _cvDisplayVector = new Vector<CvValue>();  // vector of CvValue objects, in display-row order, for doing row mapping

    private HashMap<String, CvValue> _cvAllMap = new HashMap<String, CvValue>();

    public HashMap<String, CvValue> allCvMap() {
        return _cvAllMap;
    }

    private Vector<JButton> _writeButtons = new Vector<JButton>();
    private Vector<JButton> _readButtons = new Vector<JButton>();
    private Vector<JButton> _compareButtons = new Vector<JButton>();
    private Programmer mProgrammer;

    // Defines the columns
    public static final int NUMCOLUMN = 0;
    private static final int VALCOLUMN = 1;
    private static final int STATECOLUMN = 2;
    private static final int READCOLUMN = 3;
    private static final int WRITECOLUMN = 4;
    private static final int COMPARECOLUMN = 5;

    private static final int HIGHESTCOLUMN = COMPARECOLUMN + 1;
    private static final int HIGHESTNOPROG = STATECOLUMN + 1;

    private JLabel _status = null;

    public JLabel getStatusLabel() {
        return _status;
    }

    public CvTableModel(JLabel status, Programmer pProgrammer) {
        super();

        mProgrammer = pProgrammer;
        // save a place for notification
        _status = status;

        // define just address CV at start, pending some variables
        // boudreau: not sure why we need the statement below, 
        // messes up building CV table for CV #1 when in ops mode.
        //addCV("1", false, false, false);
    }

    /**
     * Gives access to the programmer used to reach these CVs, so you can check
     * on mode, capabilities, etc.
     *
     * @return Programmer object for the CVs
     */
    public Programmer getProgrammer() {
        return mProgrammer;
    }

    public void setProgrammer(Programmer p) {
        mProgrammer = p;
        // tell all variables
        for (CvValue cv : allCvMap().values()) {
            if (cv != null) {
                cv.setProgrammer(p);
            }
        }
        for (CvValue cv : _cvDisplayVector) {
            if (cv != null) {
                cv.setProgrammer(p);
            }
        }
    }

    // basic methods for AbstractTableModel implementation
    @Override
    public int getRowCount() {
        return _numRows;
    }

    @Override
    public int getColumnCount() {
        if (getProgrammer() != null) {
            return HIGHESTCOLUMN;
        } else {
            return HIGHESTNOPROG;
        }
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NUMCOLUMN:
                return Bundle.getMessage("ColumnNameNumber");
            case VALCOLUMN:
                return Bundle.getMessage("ColumnNameValue");
            case STATECOLUMN:
                return Bundle.getMessage("ColumnNameState");
            case READCOLUMN:
                return Bundle.getMessage("ColumnNameRead");
            case WRITECOLUMN:
                return Bundle.getMessage("ColumnNameWrite");
            case COMPARECOLUMN:
                return Bundle.getMessage("ColumnNameCompare");
            default:
                return "unknown";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case NUMCOLUMN:
                return Integer.class;
            case VALCOLUMN:
                return JTextField.class;
            case STATECOLUMN:
                return String.class;
            case READCOLUMN:
                return JButton.class;
            case WRITECOLUMN:
                return JButton.class;
            case COMPARECOLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case NUMCOLUMN:
                return false;
            case VALCOLUMN:
                if (_cvDisplayVector.elementAt(row).getReadOnly()
                        || _cvDisplayVector.elementAt(row).getInfoOnly()) {
                    return false;
                } else {
                    return true;
                }
            case STATECOLUMN:
                return false;
            case READCOLUMN:
                return true;
            case WRITECOLUMN:
                return true;
            case COMPARECOLUMN:
                return true;
            default:
                return false;
        }
    }

    public String getName(int row) {  // name is text number
        return "" + _cvDisplayVector.elementAt(row).number();
    }

    public String getValString(int row) {
        return "" + _cvDisplayVector.elementAt(row).getValue();
    }

    public CvValue getCvByRow(int row) {
        return _cvDisplayVector.elementAt(row);
    }

    public CvValue getCvByNumber(String number) {
        return _cvAllMap.get(number);
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case NUMCOLUMN:
                return _cvDisplayVector.elementAt(row).number();
            case VALCOLUMN:
                return _cvDisplayVector.elementAt(row).getTableEntry();
            case STATECOLUMN:
                int state = _cvDisplayVector.elementAt(row).getState();
                switch (state) {
                    case CvValue.UNKNOWN:
                        return Bundle.getMessage("CvStateUnknown");
                    case CvValue.READ:
                        return Bundle.getMessage("CvStateRead");
                    case CvValue.EDITED:
                        return Bundle.getMessage("CvStateEdited");
                    case CvValue.STORED:
                        return Bundle.getMessage("CvStateStored");
                    case CvValue.FROMFILE:
                        return Bundle.getMessage("CvStateFromFile");
                    case CvValue.SAME:
                        return Bundle.getMessage("CvStateSame");
                    case CvValue.DIFF:
                        return Bundle.getMessage("CvStateDiff") + " "
                                + _cvDisplayVector.elementAt(row).getDecoderValue();
                    default:
                        return "inconsistent";
                }
            case READCOLUMN:
                return _readButtons.elementAt(row);
            case WRITECOLUMN:
                return _writeButtons.elementAt(row);
            case COMPARECOLUMN:
                return _compareButtons.elementAt(row);
            default:
                return "unknown";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case VALCOLUMN: // Object is actually an Integer
                if (_cvDisplayVector.elementAt(row).getValue() != ((Integer) value).intValue()) {
                    _cvDisplayVector.elementAt(row).setValue(((Integer) value).intValue());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("action command: " + e.getActionCommand());
        }
        char b = e.getActionCommand().charAt(0);
        int row = Integer.parseInt(e.getActionCommand().substring(1));
        if (log.isDebugEnabled()) {
            log.debug("event on " + b + " row " + row);
        }
        if (b == 'R') {
            // read command
            _cvDisplayVector.elementAt(row).read(_status);
        } else if (b == 'C') {
            // compare command
            _cvDisplayVector.elementAt(row).confirm(_status);
        } else {
            // write command
            _cvDisplayVector.elementAt(row).write(_status);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        // don't need to forward Busy, do need to forward Value
        // not sure about any others
        if (!e.getPropertyName().equals("Busy")) {
            fireTableDataChanged();
        }
    }

    public void addCV(String s, boolean readOnly, boolean infoOnly, boolean writeOnly) {
        if (_cvAllMap.get(s) == null) {
            CvValue cv = new CvValue(s, mProgrammer);
            cv.setReadOnly(readOnly);
            _cvAllMap.put(s, cv);
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
                    bw.setActionCommand("W" + _numRows);
                    bw.addActionListener(this);
                } else {
                    bw.setEnabled(false);
                }
                if (infoOnly) {
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
            } else {
                bw.setEnabled(true);
                bw.setActionCommand("W" + _numRows);
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
        CvValue cv = _cvAllMap.get(s);
        if (readOnly) {
            cv.setReadOnly(readOnly);
        }
        if (infoOnly) {
            cv.setReadOnly(!infoOnly);
            cv.setWriteOnly(!infoOnly);
            cv.setInfoOnly(infoOnly);
        }
        if (writeOnly) {
            cv.setWriteOnly(writeOnly);
        }
    }

    public boolean decoderDirty() {
        int len = _cvDisplayVector.size();
        for (int i = 0; i < len; i++) {
            if (_cvDisplayVector.elementAt(i).getState() == CvValue.EDITED) {
                if (log.isDebugEnabled()) {
                    log.debug("CV decoder dirty due to " + _cvDisplayVector.elementAt(i).number());
                }
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }

        // remove buttons
        for (int i = 0; i < _writeButtons.size(); i++) {
            _writeButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i < _readButtons.size(); i++) {
            _readButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i < _compareButtons.size(); i++) {
            _compareButtons.elementAt(i).removeActionListener(this);
        }

        // remove CV listeners
        for (int i = 0; i < _cvDisplayVector.size(); i++) {
            _cvDisplayVector.elementAt(i).removePropertyChangeListener(this);
        }

        // null references, so that they can be gc'd even if this isn't.
        _cvDisplayVector.removeAllElements();
        _cvDisplayVector = null;

        _writeButtons.removeAllElements();
        _writeButtons = null;

        _readButtons.removeAllElements();
        _readButtons = null;

        _compareButtons.removeAllElements();
        _compareButtons = null;

        _cvAllMap.clear();
        _cvAllMap = null;
        
        _status = null;
        
    }

    private final static Logger log = LoggerFactory.getLogger(CvTableModel.class);
}
