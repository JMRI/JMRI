// ResetTableModel.java
package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.table.AbstractTableModel;
import java.util.Vector;
import java.beans.PropertyChangeEvent;

import javax.swing.JLabel;
import org.jdom.Element;
import jmri.Programmer;
import javax.swing.JButton;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import jmri.util.jdom.LocaleSelector;

/**
 * Creates a table of the available factory resets available for a
 * particular decoder.
 *
 * @author    Howard G. Penny    Copyright (C) 2005
 * @version   $Revision$
 */
public class ResetTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {
    private String headers[] = {"Label", "Name",
                                "PI", "PIvalue",
                                "SI", "SIvalue",
                                "CV", "Value",
                                "Write", "State"};

    private Vector<CvValue> rowVector   = new Vector<CvValue>(); // vector of Reset items
    private Vector<String> labelVector = new Vector<String>(); // vector of related labels

    private Vector<JButton> _writeButtons = new Vector<JButton>();

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
    
    public int getRowCount() {
        return rowVector.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public Object getValueAt(int row, int col) {
        // if (log.isDebugEnabled()) log.debug("getValueAt "+row+" "+col);
       	// some error checking
    	if (row >= rowVector.size()){
    		log.debug("row greater than row vector");
    		return "Error";
    	}
        CvValue cv = rowVector.elementAt(row);
        if (cv == null){
        	log.debug("cv is null!");
        	return "Error CV";
        }
        if (headers[col].equals("Label"))
            return "" + labelVector.elementAt(row);
        else if (headers[col].equals("Name"))
            return "" + cv.cvName();
        else if (headers[col].equals("PI"))
            return "" + cv.piCv();
        else if (headers[col].equals("PIvalue"))
            return "" + cv.piVal();
        else if (headers[col].equals("SI"))
            return "" + cv.siCv();
        else if (headers[col].equals("SIvalue"))
            return "" + cv.siVal();
        else if (headers[col].equals("CV"))
            return "" + cv.iCv();
        else if (headers[col].equals("Value"))
            return "" + cv.getValue();
        else if (headers[col].equals("Write"))
            return _writeButtons.elementAt(row);
        else if (headers[col].equals("State")) {
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
        }
        else
            return "hmmm ... missed it";
    }

    private String _piCv;
    private String _siCv;

    public void setPiCv(String piCv) {
        _piCv = piCv;
    }

    public void setSiCv(String siCv) {
        _siCv = siCv;
    }

    public void setRow(int row, Element e) {
        String label = LocaleSelector.getAttribute(e, "label"); // Note the name variable is actually the label attribute
        if (log.isDebugEnabled()) log.debug("Starting to setRow \"" +
                                            label + "\"");
        String cv = e.getAttribute("CV").getValue();
        int cvVal = Integer.valueOf(e.getAttribute("default").getValue()).intValue();

        if (log.isDebugEnabled()) log.debug("            CV \"" +cv+ "\" value "+cvVal);

        CvValue resetCV = new CvValue(cv, mProgrammer);
        resetCV.setValue(cvVal);
        resetCV.setWriteOnly(true);
        resetCV.setState(VariableValue.STORED);
        rowVector.addElement(resetCV);
        labelVector.addElement(label);
        return;
    }

    public void setIndxRow(int row, Element e) {
        if (_piCv != "" && _siCv != "") {
            // get the values for the VariableValue ctor
            String label = LocaleSelector.getAttribute(e, "label"); // Note the name variable is actually the label attribute
            if (log.isDebugEnabled()) log.debug("Starting to setIndxRow \"" +
                                                label + "\"");
            String cvName = e.getAttributeValue("CVname");
            int piVal = Integer.valueOf(e.getAttribute("PI").getValue()).intValue();
            int siVal = (e.getAttribute("SI") != null ?
                         Integer.valueOf(e.getAttribute("SI").getValue()).
                         intValue() :
                         -1);
            String iCv   = e.getAttribute("CV").getValue();
            int icvVal = Integer.valueOf(e.getAttribute("default").getValue()).intValue();

            CvValue resetCV = new CvValue(""+row, cvName, _piCv, piVal, _siCv, siVal, iCv, mProgrammer);
            resetCV.addPropertyChangeListener(this);

            JButton bw = new JButton("Write");
            _writeButtons.addElement(bw);
            resetCV.setValue(icvVal);
            resetCV.setWriteOnly(true);
            resetCV.setState(VariableValue.STORED);
            rowVector.addElement(resetCV);
            labelVector.addElement(label);
        }
        return;
    }

    protected void performReset(int row) {
        CvValue cv = rowVector.get(row);
        if (log.isDebugEnabled()) log.debug("performReset: "+cv+" with piCv \""+cv.piCv()+"\"");
        if (cv.piCv() != null && cv.piCv() != "" && cv.iCv() != null && cv.iCv() != "") {
            _iCv = cv;
            indexedWrite();
        } else {
            cv.write(_status);
        }
    }

    public void actionPerformed(ActionEvent e) {
         if (log.isDebugEnabled()) log.debug("action command: "+e.getActionCommand());
         char b = e.getActionCommand().charAt(0);
         int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
         if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
         if (b=='W') {
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
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
         // lets skip the SI step if SI is not used
        if (_iCv.siVal() > 0) {
            _progState = WRITING_PI;
        } else {
            _progState = WRITING_SI;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        // to write any indexed CV we must write the PI
        _iCv.writePI(_status);
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
            case WRITING_PI:   // have written the PI, now write SI if needed
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");
                _progState = WRITING_SI;
                _iCv.writeSI(_status);
                return;
            case WRITING_SI:  // have written the SI if needed, now write CV
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");
                _progState = WRITING_CV;
                _iCv.writeIcV(_status);
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
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        // remove buttons
        for (int i = 0; i<_writeButtons.size(); i++) {
            _writeButtons.elementAt(i).removeActionListener(this);
        }

        _writeButtons.removeAllElements();
        _writeButtons = null;

        // remove variables listeners
        for (int i = 0; i<rowVector.size(); i++) {
            CvValue cv = rowVector.elementAt(i);
            cv.dispose();
        }
        rowVector.removeAllElements();
        rowVector = null;

        labelVector.removeAllElements();
        labelVector = null;

        headers = null;

        _status = null;
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(ResetTableModel.class.getName());
}
