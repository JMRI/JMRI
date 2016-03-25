package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a table of the available factory resets available for a particular
 * decoder.
 *
 * @author Howard G. Penny Copyright (C) 2005
 */
public class ResetTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = 5802447765323835861L;

    private String headers[] = {"Label", "Name",
        "PI", "PIvalue",
        "SI", "SIvalue",
        "CV", "Value",
        "Write", "State"};

    private Vector<CvValue> rowVector = new Vector<>(); // vector of Reset items
    private Vector<String> labelVector = new Vector<>(); // vector of related labels
    private Vector<List<String>> modeVector = new Vector<>(); // vector of related modes

    private Vector<JButton> _writeButtons = new Vector<>();

    private CvValue _iCv = null;
    private JLabel _status = null;
    private Programmer mProgrammer;

    public ResetTableModel(JLabel status, Programmer pProgrammer) {
        super();

        mProgrammer = pProgrammer;
        // save a place for notification
        _status = status;
    }

    public void setProgrammer(Programmer p) {
        mProgrammer = p;

        // pass on to all contained CVs
        for (CvValue cv : rowVector) {
            cv.setProgrammer(p);
        }
    }

    private boolean hasOpsModeFlag = false;

    protected void flagIfOpsMode(String mode) {
        if (mode.startsWith("OPS")) {
            hasOpsModeFlag = true;
        }
    }

    public boolean hasOpsModeReset() {
        return hasOpsModeFlag;
    }

    public int getRowCount() {
        return rowVector.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public Object getValueAt(int row, int col) {
        // if (log.isDebugEnabled()) log.debug("getValueAt "+row+" "+col);
        // some error checking
        if (row >= rowVector.size()) {
            log.debug("row greater than row vector");
            return "Error";
        }
        CvValue cv = rowVector.elementAt(row);
        if (cv == null) {
            log.debug("cv is null!");
            return "Error CV";
        }
        if (headers[col].equals("Label")) {
            return "" + labelVector.elementAt(row);
        } else if (headers[col].equals("Name")) {
            return "" + cv.cvName();
        } else if (headers[col].equals("PI")) {
            return "" + cv.piCv();
        } else if (headers[col].equals("PIvalue")) {
            return "" + cv.piVal();
        } else if (headers[col].equals("SI")) {
            return "" + cv.siCv();
        } else if (headers[col].equals("SIvalue")) {
            return "" + cv.siVal();
        } else if (headers[col].equals("CV")) {
            return "" + cv.iCv();
        } else if (headers[col].equals("Value")) {
            return "" + cv.getValue();
        } else if (headers[col].equals("Write")) {
            return _writeButtons.elementAt(row);
        } else if (headers[col].equals("State")) {
            int state = cv.getState();
            switch (state) {
                case CvValue.UNKNOWN:
                    return "Unknown";
                case CvValue.READ:
                    return "Read";
                case CvValue.EDITED:
                    return "Edited";
                case CvValue.STORED:
                    return "Stored";
                case CvValue.FROMFILE:
                    return "From file";
                default:
                    return "inconsistent";
            }
        } else {
            return "hmmm ... missed it";
        }
    }

    private String _piCv;
    private String _siCv;

    public void setPiCv(String piCv) {
        _piCv = piCv;
    }

    public void setSiCv(String siCv) {
        _siCv = siCv;
    }

    public void setRow(int row, Element e, Element p, String model) {
        decoderModel = model; // Save for use elsewhere
        String label = LocaleSelector.getAttribute(e, "label"); // Note the name variable is actually the label attribute
        if (log.isDebugEnabled()) {
            log.debug("Starting to setRow \""
                    + label + "\"");
        }
        String cv = e.getAttribute("CV").getValue();
        int cvVal = Integer.valueOf(e.getAttribute("default").getValue()).intValue();

        if (log.isDebugEnabled()) {
            log.debug("            CV \"" + cv + "\" value " + cvVal);
        }

        CvValue resetCV = new CvValue(cv, mProgrammer);
        resetCV.addPropertyChangeListener(this);
        resetCV.setValue(cvVal);
        resetCV.setWriteOnly(true);
        resetCV.setState(VariableValue.STORED);
        rowVector.addElement(resetCV);
        labelVector.addElement(label);
        modeVector.addElement(getResetModeList(e, p));
        return;
    }

    public void setIndxRow(int row, Element e, Element p, String model) {
        decoderModel = model; // Save for use elsewhere
        if (!_piCv.equals("") && !_siCv.equals("")) {
            // get the values for the VariableValue ctor
            String label = LocaleSelector.getAttribute(e, "label"); // Note the name variable is actually the label attribute
            if (log.isDebugEnabled()) {
                log.debug("Starting to setIndxRow \""
                        + label + "\"");
            }
            String cvName = e.getAttributeValue("CVname");
            int piVal = Integer.valueOf(e.getAttribute("PI").getValue()).intValue();
            int siVal = (e.getAttribute("SI") != null
                    ? Integer.valueOf(e.getAttribute("SI").getValue()).
                    intValue()
                    : -1);
            String iCv = e.getAttribute("CV").getValue();
            int icvVal = Integer.valueOf(e.getAttribute("default").getValue()).intValue();

            CvValue resetCV = new CvValue("" + row, cvName, _piCv, piVal, _siCv, siVal, iCv, mProgrammer);
            resetCV.addPropertyChangeListener(this);

            JButton bw = new JButton("Write");
            _writeButtons.addElement(bw);
            resetCV.setValue(icvVal);
            resetCV.setWriteOnly(true);
            resetCV.setState(VariableValue.STORED);
            rowVector.addElement(resetCV);
            labelVector.addElement(label);
            modeVector.addElement(getResetModeList(e, p));
        }
        return;
    }

    protected List<String> getResetModeList(Element e, Element p) {
        List<Element> elementList = new ArrayList<Element>();
        List<String> modeList = new ArrayList<String>();
        List<Element> elementModes;
        String mode;
        boolean resetsModeFound = false;

        elementList.add(p);
        elementList.add(e);
        
        for (Element ep : elementList) {            
            try {
                mode = ep.getAttribute("mode").getValue();
                if (ep.getName().equals("resets")) {
                    resetsModeFound = true;
                } else if (resetsModeFound) {
                    modeList.clear();
                    resetsModeFound = false;
                }
                modeList.add(mode);
                flagIfOpsMode(mode);
            } catch (NullPointerException ex) {
                mode = null;
            }        

           try {
               elementModes = ep.getChildren("mode");
                for (Element s : elementModes) {
                if (ep.getName().equals("resets")) {
                    resetsModeFound = true;
                } else if (resetsModeFound) {
                    modeList.clear();
                    resetsModeFound = false;
                }
                modeList.add(s.getText());
                flagIfOpsMode(s.getText());
               }
            } catch (NullPointerException ex) {
                elementModes = null;
            }        
        }
        
        return modeList;
    }

    private ProgrammingMode savedMode;
    private String decoderModel;

    protected void performReset(int row) {
        savedMode = mProgrammer.getMode(); // In case we need to change modes
        if (modeVector.get(row) != null) {
            List<ProgrammingMode> modes = mProgrammer.getSupportedModes();
            List<String> validModes = modeVector.get(row);

            String programmerModeList = "";
            for (ProgrammingMode m : modes) {
                programmerModeList = programmerModeList + "," + m.toString();
            }
            if (programmerModeList.startsWith(",")) {
                programmerModeList = programmerModeList.substring(1);
            }

            String resetModeList = "";
            for (String mode : validModes) {
                resetModeList = resetModeList + "," + new ProgrammingMode(mode).toString();
            }
            if (resetModeList.startsWith(",")) {
                resetModeList = resetModeList.substring(1);
            }

            
            if (resetModeList.length() > 0) {
                boolean modeFound = false;
                search:
                    for (ProgrammingMode m : modes) {
                        for (String mode : validModes) {
                            if (mode.equals(m.getStandardName())) {
                                mProgrammer.setMode(m);
                                modeFound = true;
                                break search;
                            }
                        }
                    }

                if (mProgrammer.getMode().getStandardName().startsWith("OPS")) {
                    if ( !opsResetOk() ) {
                        return;
                    }
                }
                
                if (!modeFound) {
                    if (!badModeOk((savedMode.toString()), resetModeList, programmerModeList)) {
                        return;
                    }
                    log.warn(labelVector.get(row)+ " for " + decoderModel + " was attempted in "+ savedMode.toString() + " mode.");
                    log.warn("Recommended mode(s) were \"" + resetModeList + "\" but available modes were \"" + programmerModeList + "\"");
                }
            }
        }
        CvValue cv = rowVector.get(row);
        if (log.isDebugEnabled()) {
            log.debug("performReset: " + cv + " with piCv \"" + cv.piCv() + "\"");
        }
        if (cv.piCv() != null && !cv.piCv().equals("") && cv.iCv() != null && !cv.iCv().equals("")) {
            _iCv = cv;
            indexedWrite();
        } else {
            _progState = WRITING_CV;
            cv.write(_status);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("action command: " + e.getActionCommand());
        }
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        if (log.isDebugEnabled()) {
            log.debug("event on " + b + " row " + row);
        }
        if (b == 'W') {
            // write command
            performReset(row);
        }
    }

    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int WRITING_PI = 1;
    private static final int WRITING_SI = 2;
    private static final int WRITING_CV = 3;

    public void indexedWrite() {
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in write()");
        }
        // lets skip the SI step if SI is not used
        if (_iCv.siVal() > 0) {
            _progState = WRITING_PI;
        } else {
            _progState = WRITING_SI;
        }
        if (log.isDebugEnabled()) {
            log.debug("invoke PI write for CV write");
        }
        // to write any indexed CV we must write the PI
        _iCv.writePI(_status);
    }

    public void propertyChange(PropertyChangeEvent e) {

        if (log.isDebugEnabled()) {
            log.debug("Property changed: " + e.getPropertyName());
        }
        // notification from Indexed CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
                case IDLE:  // no, just an Indexed CV update
                    if (log.isDebugEnabled()) {
                        log.error("Busy goes false with state IDLE");
                    }
                    return;
                case WRITING_PI:   // have written the PI, now write SI if needed
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state WRITING_PI");
                    }
                    _progState = WRITING_SI;
                    _iCv.writeSI(_status);
                    return;
                case WRITING_SI:  // have written the SI if needed, now write CV
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state WRITING_SI");
                    }
                    _progState = WRITING_CV;
                    _iCv.writeIcV(_status);
                    return;
                case WRITING_CV:  // now done with the write request
                    if (log.isDebugEnabled()) {
                        log.debug("Finished writing the Indexed CV");
                    }
                    mProgrammer.setMode(savedMode);            
                    _progState = IDLE;
                    return;
                default:  // unexpected!
                    log.error("Unexpected state found: " + _progState);
                    mProgrammer.setMode(savedMode);            
                    _progState = IDLE;
                    return;
            }
        }
    }

        /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean badModeOk(String currentMode, String resetModes, String availableModes) {
        String resetWarning =
                ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn1")
                + "\n\n"
                + java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn2"), resetModes)
                + "\n"
                + java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn3"), availableModes)
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn4")
                + "\n\n"
                + java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn5"), currentMode)
                ;
        return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(null,
                        resetWarning,
                        ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetTitle"),
                        JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE));
    }

        /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean opsResetOk() {
        String resetWarning =
                ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn1")
                + "\n\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn2")
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn3")
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn4")
                + "\n\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn5")
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn6")
                + "\n\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn7")
                ;
        return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(null,
                        resetWarning,
                        ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsTitle"),
                        JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE));
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }

        // remove buttons
        for (int i = 0; i < _writeButtons.size(); i++) {
            _writeButtons.elementAt(i).removeActionListener(this);
        }

        _writeButtons.removeAllElements();
        _writeButtons = null;

        // remove variables listeners
        for (int i = 0; i < rowVector.size(); i++) {
            CvValue cv = rowVector.elementAt(i);
            cv.dispose();
        }
        rowVector.removeAllElements();
        rowVector = null;

        labelVector.removeAllElements();
        labelVector = null;

        modeVector.removeAllElements();
        modeVector = null;

        headers = null;

        _status = null;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ResetTableModel.class.getName());
}
