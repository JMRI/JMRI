// SplitVariableValue.java

package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Document;

/**
 * Extends VariableValue to represent a variable
 * split across two CVs. The original use is for addresses of stationary (accessory)
 * decoders
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.2 $
 *
 */
public class SplitVariableValue extends VariableValue
                          implements ActionListener, PropertyChangeListener, FocusListener {

	public SplitVariableValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdname,
                                                        int pSecondCV,
                                                        int pFactor, int pOffset) {
		super(name, comment, readOnly, cvNum, mask, v, status, stdname);
		_maxVal = maxVal;
		_minVal = minVal;
		_value = new JTextField("0", 5);
		_defaultColor = _value.getBackground();
		_value.setBackground(COLOR_UNKNOWN);
                mFactor = pFactor;
                mOffset = pOffset;
		// connect to the JTextField value, cv
		_value.addActionListener(this);
		_value.addFocusListener(this);
                mSecondCV = pSecondCV;
                mShift = 0;
                String t = mask;
                while (t.endsWith("V")) {
                  mShift++;
                  t = t.substring(0,t.length()-1);
                }
                if (mShift<=0) log.warn("split variable "+name+" mask "+mask+" must have V in the right most position");
		// connect for notification
		CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()));
		cv.addPropertyChangeListener(this);
		cv.setState(CvValue.FROMFILE);
		CvValue cv1 = ((CvValue)_cvVector.elementAt(getSecondCvNum()));
		cv1.addPropertyChangeListener(this);
		cv1.setState(CvValue.FROMFILE);
	}

        int mSecondCV;
        int mFactor;
        int mOffset;

        public int getSecondCvNum() { return mSecondCV;}
        int mShift;

	// the connection is to cvNum and cvNum+1

	int _maxVal;
	int _minVal;

	public Object rangeVal() {
		return new String("Long address");
	}

	String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }

    void exitField() {
        if (!oldContents.equals(_value.getText())) {
            int newVal = ((Integer.valueOf(_value.getText()).intValue())-mOffset)/mFactor;
            int oldVal = ((Integer.valueOf(oldContents).intValue())-mOffset)/mFactor;
            updatedTextField();
            prop.firePropertyChange("Value", new Integer(oldVal), new Integer(newVal));
        }
    }

	void updatedTextField() {
		if (log.isDebugEnabled()) log.debug("actionPerformed");
		// called for new values - set the CV as needed
		CvValue cv1 = (CvValue)_cvVector.elementAt(getCvNum());
		CvValue cv2 = (CvValue)_cvVector.elementAt(getSecondCvNum());
		// no masking involved for long address
		int newEntry;  // entered value
		try { newEntry = Integer.valueOf(_value.getText()).intValue(); }
			catch (java.lang.NumberFormatException ex) { newEntry = 0; }

                // calculate resulting number
                int newVal = (newEntry-mOffset)/mFactor;

		// no masked combining of old value required, as this fills the two CVs
		int newCv1 = newVal& ((1<<mShift)-1);
		int newCv2 = newVal >>mShift;
		cv1.setValue(newCv1);
		cv2.setValue(newCv2);
		if (log.isDebugEnabled()) log.debug("new value "+newVal+" gives first="+newCv1+" second="+newCv2);
	}

    /** ActionListener implementations */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        int newVal = ((Integer.valueOf(_value.getText()).intValue())-mOffset)/mFactor;
        updatedTextField();
        prop.firePropertyChange("Value", null, new Integer(newVal));
    }

	/** FocusListener implementations */
	public void focusGained(FocusEvent e) {
		if (log.isDebugEnabled()) log.debug("focusGained");
		enterField();
	}

	public void focusLost(FocusEvent e) {
		if (log.isDebugEnabled()) log.debug("focusLost");
		exitField();
	}

	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes.
	public String getValueString() {
                int newVal = ((Integer.valueOf(_value.getText()).intValue())-mOffset)/mFactor;
		return String.valueOf(newVal);
	}
	public void setIntValue(int i) {
		setValue((i-mOffset)/mFactor);
	}

	public Component getValue()  {
	 	if (getReadOnly())  //
	 		return new JLabel(_value.getText());
	 	else
	 		return _value;
	}

    public void setValue(int value) {
        int oldVal;
        try {
            oldVal = (Integer.valueOf(_value.getText()).intValue()-mOffset)/mFactor;
        } catch (java.lang.NumberFormatException ex) { oldVal = -999; }
        if (log.isDebugEnabled()) log.debug("setValue with new value "+value+" old value "+oldVal);
        _value.setText(String.valueOf( value*mFactor + mOffset));
        if (oldVal != value || getState() == VariableValue.UNKNOWN)
            actionPerformed(null);
        prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value*mFactor + mOffset));
    }

    Color _defaultColor;

    // implement an abstract member to set colors
	void setColor(Color c) {
		if (c != null) _value.setBackground(c);
		else _value.setBackground(_defaultColor);
		prop.firePropertyChange("Value", null, null);
	}

	public Component getRep(String format)  {
		return new VarTextField(_value.getDocument(),_value.getText(), 5, this);
	}
	private int _progState = 0;
	private static final int IDLE = 0;
	private static final int READING_FIRST = 1;
	private static final int READING_SECOND = 2;
	private static final int WRITING_FIRST = 3;
	private static final int WRITING_SECOND = 4;

	//
	public void read() {
		if (log.isDebugEnabled()) log.debug("longAddr read() invoked");
 		setBusy(true);  // will be reset when value changes
		//super.setState(READ);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
		_progState = READING_FIRST;
		if (log.isDebugEnabled()) log.debug("invoke CV read");
		((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
	}

 	public void write() {
		if (log.isDebugEnabled()) log.debug("write() invoked");
 		if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
 		setBusy(true);  // will be reset when value changes
 		//super.setState(STORED);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
		_progState = WRITING_FIRST;
		if (log.isDebugEnabled()) log.debug("invoke CV write");
 		((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
 	}

	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("property changed event - name: "
										+e.getPropertyName());
		// notification from CV; check for Value being changed
		if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
			// busy transitions drive the state
			switch (_progState) {
				case IDLE:  // no, just a CV update
						if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
						return;
				case READING_FIRST:   // read first CV, now read second
						if (log.isDebugEnabled()) log.debug("Busy goes false with state READING_FIRST");
						_progState = READING_SECOND;
						((CvValue)_cvVector.elementAt(getSecondCvNum())).read(_status);
						return;
				case READING_SECOND:  // finally done, set not busy
						if (log.isDebugEnabled()) log.debug("Busy goes false with state READING_SECOND");
						_progState = IDLE;
						((CvValue)_cvVector.elementAt(getCvNum())).setState(READ);
						((CvValue)_cvVector.elementAt(getSecondCvNum())).setState(READ);
						//super.setState(READ);
						setBusy(false);
						return;
				case WRITING_FIRST:  // no, just a CV update
						if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_FIRST");
						_progState = WRITING_SECOND;
 						((CvValue)_cvVector.elementAt(getSecondCvNum())).write(_status);
						return;
				case WRITING_SECOND:  // now done with complete request
						if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SECOND");
						_progState = IDLE;
 						super.setState(STORED);
						setBusy(false);
						return;
				default:  // unexpected!
						log.error("Unexpected state found: "+_progState);
						_progState = IDLE;
						return;
			}
		}
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			if (log.isDebugEnabled()) log.debug("CV State changed to "+cv.getState());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
			// update value of Variable
			CvValue cv0 = (CvValue)_cvVector.elementAt(getCvNum());
			CvValue cv1 = (CvValue)_cvVector.elementAt(getSecondCvNum());
			int newVal = (cv0.getValue()&((1<<mShift)-1))
                                    + (cv1.getValue()<<mShift);
			setValue(newVal);  // check for duplicate done inside setVal
			// state change due to CV state change, so propagate that
			setState(cv0.getState());
			// see if this was a read or write operation
			switch (_progState) {
				case IDLE:  // no, just a CV update
						if (log.isDebugEnabled()) log.debug("Value changed with state IDLE");
						return;
				case READING_FIRST:  // yes, now read second
						if (log.isDebugEnabled()) log.debug("Value changed with state READING_FIRST");
						return;
				case READING_SECOND:  // now done with complete request
						if (log.isDebugEnabled()) log.debug("Value changed with state READING_SECOND");
						return;
				default:  // unexpected!
						log.error("Unexpected state found: "+_progState);
						_progState = IDLE;
						return;
			}
		}
	}

	// stored value
	JTextField _value = null;

	/* Internal class extends a JTextField so that its color is consistent with
	 * an underlying variable
	 *
	 * @author	Bob Jacobsen   Copyright (C) 2001
	 * @version     $Revision: 1.2 $
	 */
	public class VarTextField extends JTextField {

		VarTextField(Document doc, String text, int col, SplitVariableValue var) {
			super(doc, text, col);
			_var = var;
			// get the original color right
			setBackground(_var._value.getBackground());
			// listen for changes to ourself
			addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					thisActionPerformed(e);
				}
			});
			addFocusListener(new java.awt.event.FocusListener() {
				public void focusGained(FocusEvent e) {
					if (log.isDebugEnabled()) log.debug("focusGained");
					enterField();
				}

				public void focusLost(FocusEvent e) {
					if (log.isDebugEnabled()) log.debug("focusLost");
					exitField();
				}
			});
			// listen for changes to original state
			_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					originalPropertyChanged(e);
				}
			});
		}

		SplitVariableValue _var;

		void thisActionPerformed(java.awt.event.ActionEvent e) {
			// tell original
			_var.actionPerformed(e);
		}

		void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
			// update this color from original state
			if (e.getPropertyName().equals("State")) {
				setBackground(_var._value.getBackground());
			}
		}

	}

	// clean up connections when done
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
		if (_value != null) _value.removeActionListener(this);
		((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);
		((CvValue)_cvVector.elementAt(getCvNum()+1)).removePropertyChangeListener(this);

		_value = null;
		// do something about the VarTextField
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SplitVariableValue.class.getName());

}
