// SpeedTableVarValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

/** 
 * SpeedTableVarValue.java
 *<P>
 *_value is a holdover from the LongAddrVariableValue, which this was copied from; it should
 * be removed. _maxVal, _minVal are also redundant.
 *
 *<P> Color (hence state) of individual sliders (hence CVs) are directly coupled to the
 * state of those CVs. 
 *<P> The state of this variable has to be a composite of all the sliders, hence CVs.
 *The mapping is (in order):
 *<UL>
 *<LI>If any CVs are UNKNOWN, its UNKNOWN..
 *<LI>If not, and any are EDITTED, its EDITTED.
 *<LI>If not, and any are FROMFILE, its FROMFILE.
 *<LI>If not, and any are READ, its READ.
 *<LI>If not, and any are STORED, its STORED.
 *<LI>And if we get to here, something awful has happened.
 *</UL><P>
 *A similar pattern is used for a read or write request.  Write writes them all;
 *Read reads any that aren't READ or WRITTEN.
 *<P>
 * Description:		Extends VariableValue to represent a NMRA long address
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: SpeedTableVarValue.java,v 1.7 2002-02-04 07:32:07 jacobsen Exp $
 *
 */
public class SpeedTableVarValue extends VariableValue implements PropertyChangeListener, ChangeListener {

	final int nValues = 28;
	BoundedRangeModel[] models = new BoundedRangeModel[nValues];
		
	/**
	 * Create the object with a "standard format ctor".  Note that max and min are ignored.
	 */
	public SpeedTableVarValue(String name, String comment, boolean readOnly,
							int cvNum, String mask, int minVal, int maxVal,
							Vector v, JLabel status, String stdname) {
		super(name, comment, readOnly, cvNum, mask, v, status, stdname);
		
		// create the set of models 
		for (int i=0; i<nValues; i++) {
			// create each model
			DefaultBoundedRangeModel j = new DefaultBoundedRangeModel(255*i/nValues, 0, 0, 255);
			models[i] = j;
			// connect each model to CV for notification
			// the connection is to cvNum through cvNum+27 (28 values total)
			// The invoking code (e.g. VariableTableModel) must ensure the CVs exist
			// Note that the default values in the CVs are zero, but are the ramp
			// values here.  We leave that as work item 177, and move on to set the
			// CV states to "FromFile"
			CvValue c = (CvValue)_cvVector.elementAt(getCvNum()+i);
			c.setValue(255*i/nValues);
			c.addPropertyChangeListener(this);
			c.setState(CvValue.FROMFILE);
		}

		_defaultColor = (new JSlider()).getBackground();
	}
	
  	/** 
  	 * Create a null object.  Normally only used for tests and to pre-load classes.
  	 */ 
   	public SpeedTableVarValue() {}
		
	public Object rangeVal() {
		log.warn("rangeVal doesn't make sense for a speed table");
		return new String("Speed table");
	}
		
	public void stateChanged(ChangeEvent e) {
		// called for new values of a slider - set the CV(s) as needed
		// e.getSource() points to the JSlider object - find it in the list
		JSlider j = (JSlider) e.getSource();
		BoundedRangeModel r = j.getModel();
		
		for (int i=0; i<nValues; i++) {
			if (r == models[i]) {
				// found it, and i is useful!
				setModel(i, r.getValue());				
				break; // no need to continue loop
			}
		}

	}
	
	void setModel(int i, int value) {  // value is 0 to 255
		if (models[i].getValue() != value) 
			models[i].setValue(value);
		// update the CV
		((CvValue)_cvVector.elementAt(getCvNum()+i)).setValue(value);
		// check the neighbors, and force them if needed
		if (i>0) {
			// left neighbour
			if (models[i-1].getValue() > value)  setModel(i-1, value);
		}
		if (i<nValues-1) {
			// right neighbour
			if (value > models[i+1].getValue())  setModel(i+1, value);
		}
	}

	public int getState()  {
		int i;
		for (i=0; i<nValues; i++) 
				if (((CvValue)_cvVector.elementAt(getCvNum()+i)).getState() == UNKNOWN ) return UNKNOWN;
		for (i=0; i<nValues; i++) 
				if (((CvValue)_cvVector.elementAt(getCvNum()+i)).getState() == EDITTED ) return EDITTED;
		for (i=0; i<nValues; i++) 
				if (((CvValue)_cvVector.elementAt(getCvNum()+i)).getState() == FROMFILE ) return FROMFILE;
		for (i=0; i<nValues; i++) 
				if (((CvValue)_cvVector.elementAt(getCvNum()+i)).getState() == READ ) return READ;
		for (i=0; i<nValues; i++) 
				if (((CvValue)_cvVector.elementAt(getCvNum()+i)).getState() == STORED ) return STORED;
		log.error("getState did not decode a possible state");
		return UNKNOWN;
	}

	// to complete this class, fill in the routines to handle "Value" parameter
	// and to read/write/hear parameter changes. 
	public String getValueString() {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i< models.length; i++) {
			if (i!=0) buf.append(",");
			buf.append(Integer.toString(models[i].getValue()));
		}
		return new String(buf);
	}
	public void setIntValue(int i) {
		log.warn("setIntValue doesn't make sense for a speed table: "+i);
	}
	
	public Component getValue()  { 
		log.warn("getValue not implemented yet");
	 	return new JLabel("speed table");
	}

	public void setValue(int value) { 
		log.warn("setValue doesn't make sense for a speed table: "+value);
	}

	Color _defaultColor;
	// implement an abstract member to set colors
	void setColor(Color c) {
		prop.firePropertyChange("Value", null, null);
	}
	
	public Component getRep(String format)  { 
		// put together a new panel in scroll pane
		JPanel j = new JPanel();
		j.setLayout(new BoxLayout(j, BoxLayout.X_AXIS));

		for (int i=0; i<nValues; i++) {
			JSlider s = new VarSlider(models[i], ((CvValue)_cvVector.elementAt(getCvNum()+i)));
			s.setOrientation(JSlider.VERTICAL);
			s.addChangeListener(this);
			j.add(s);
		}
		return j;
	}
	
	/**
	 * IDLE if a read/write operation is not in progress.  During an operation, it
	 * indicates the index of the CV to handle when the current programming operation
	 * finishes.
	 */
	private int _progState = IDLE;
	private static final int IDLE = -1;
	boolean isReading;
	
	// 
	public void read() {
		if (log.isDebugEnabled()) log.debug("longAddr read() invoked");
 		setBusy(true);  // will be reset when value changes
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
		isReading = true;
		_progState = -1;
		if (log.isDebugEnabled()) log.debug("start series of read operations");		
		readNext();
	}
	
 	public void write() {
		if (log.isDebugEnabled()) log.debug("write() invoked");
 		if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
 		setBusy(true);  // will be reset when value changes
 		super.setState(STORED);
		if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
		isReading = false;
		_progState = -1;
		if (log.isDebugEnabled()) log.debug("start series of write operations");		
 		writeNext();
 	}

	void readNext() {
		// read operation
		_progState++;  // progState is the index of the CV to handle now, do next
		if (_progState >= nValues) {
			// done, clean up and return to invoker
			_progState = IDLE;
			setBusy(false);
			return;
		}
		// not done, proceed to do the next
		CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()+_progState));
		int state = cv.getState();		
		if (log.isDebugEnabled()) log.debug("invoke CV read index "+_progState+" cv state "+state);
		if (state == UNKNOWN || state == FROMFILE || state == EDITTED) cv.read(_status);
		else readNext(); // repeat until end
	}
	
	void writeNext() {
		// write operation
		_progState++;  // progState is the index of the CV to handle now
		if (_progState >= nValues) {
			_progState = IDLE;
			setBusy(false);
			return;
		}
		CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()+_progState));
		int state = cv.getState();		
		if (log.isDebugEnabled()) log.debug("invoke CV write index "+_progState+" cv state "+state);
		if (state == UNKNOWN || state == FROMFILE || state == EDITTED) cv.write(_status);
		else writeNext();
	}
	
	// handle incoming parameter notification
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (log.isDebugEnabled()) log.debug("property changed event - name: "
										+e.getPropertyName());
		// notification from CV; check for Value being changed
		if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
			// busy transitions drive an ongoing programming operation
			// see if actually done
			
			if (isReading) readNext();
			else writeNext();
		}
		else if (e.getPropertyName().equals("State")) {
			CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
			if (log.isDebugEnabled()) log.debug("CV State changed to "+cv.getState());
			setState(cv.getState());
		}
		else if (e.getPropertyName().equals("Value")) {
			// find the CV that sent this
			CvValue cv = (CvValue)e.getSource();
			int value = cv.getValue();
			// find the index of that CV
			for (int i=0; i<nValues; i++) {
				if ((CvValue)_cvVector.elementAt(getCvNum()+i) == cv) {
					// this is the one, so use this i
					setModel(i, value);
					break;
				}
			}
		}
	}

	/* Internal class extends a JSlider so that its color is consistent with 
	 * an underlying CV; we return one of these in getRep.
	 *<P>
	 * Unlike similar cases elsewhere, this doesn't have to listen to
	 * value changes.  Those are handled automagically since we're sharing the same
	 * model between this object and others.  And this is listening to 
	 * a CV state, not a variable.
	 *
	 * @author			Bob Jacobsen   Copyright (C) 2001
	 * @version			
	 */
	public class VarSlider extends JSlider {

		VarSlider(BoundedRangeModel m, CvValue var) {
			super(m);
			_var = var;
			// get the original color right
			setBackground(_var.getColor());
			// listen for changes to original state
			_var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					originalPropertyChanged(e);
				}
			});		
		}

		CvValue _var;
	
		void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
			if (log.isDebugEnabled()) log.debug("VarSlider saw property change: "+e);
			// update this color from original state
			if (e.getPropertyName().equals("State")) {
				setBackground(_var.getColor());
			}	
		}
	
	}

	// clean up connections when done
	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
		// the connection is to cvNum through cvNum+27 (28 values total)
		for (int i=0; i<nValues; i++) {
			((CvValue)_cvVector.elementAt(getCvNum()+i)).removePropertyChangeListener(this);
		}
		
		// do something about the VarSlider objects
	}

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpeedTableVarValue.class.getName());
		
}
