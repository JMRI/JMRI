package jmri.jmrit.vsdecoder.swing;

import javax.vecmath.Vector3d;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;


public class Vector3dPanel extends JPanel {
    
    private Vector3d _value;
    private JSpinner xspin, yspin, zspin;
    private double _minspin, _maxspin, _spininc;
    private Boolean _initialized = false;

    public Vector3dPanel(String l1, String l2, String l3, double spinval, double min_spin, double max_spin, double spin_inc) {
	super();
	_minspin = min_spin;
	_maxspin = max_spin;
	_spininc = spin_inc;
	initGui(l1, l2, l3, spinval);
    }

    protected void initGui(String l1, String l2, String l3, double spinval) {
	this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	
	xspin = new JSpinner(new SpinnerNumberModel(spinval, _minspin, _maxspin, _spininc));
	yspin = new JSpinner(new SpinnerNumberModel(spinval, _minspin, _maxspin, _spininc));
	zspin = new JSpinner(new SpinnerNumberModel(spinval, _minspin, _maxspin, _spininc));

	this.add(new JLabel(l1));
	this.add(xspin);
	this.add(new JLabel(l2));
	this.add(yspin);
	this.add(new JLabel(l3));
	this.add(zspin);

	_initialized = true;
    }

    public void setMinValue(double min) {
	_minspin = min;
	if (_initialized) {
	    ((SpinnerNumberModel)xspin.getModel()).setMinimum(min);
	    ((SpinnerNumberModel)yspin.getModel()).setMinimum(min);
	    ((SpinnerNumberModel)zspin.getModel()).setMinimum(min);
	}
    }

    public void setMaxValue(double max) {
	_maxspin = max;
	if (_initialized) {
	    ((SpinnerNumberModel)xspin.getModel()).setMaximum(max);
	    ((SpinnerNumberModel)yspin.getModel()).setMaximum(max);
	    ((SpinnerNumberModel)zspin.getModel()).setMaximum(max);
	}
    }

    public void setIncrement(double inc) {
	_spininc = inc;
	if (_initialized) {
	    ((SpinnerNumberModel)xspin.getModel()).setStepSize(inc);
	    ((SpinnerNumberModel)yspin.getModel()).setStepSize(inc);
	    ((SpinnerNumberModel)zspin.getModel()).setStepSize(inc);
	}
    }

    public Vector3d getValue() {
	Double x = (Double)((SpinnerNumberModel)xspin.getModel()).getNumber();
	Double y = (Double)((SpinnerNumberModel)yspin.getModel()).getNumber();
	Double z = (Double)((SpinnerNumberModel)zspin.getModel()).getNumber();

	return(new Vector3d(x, y, z));
    }

    public void setValue(Vector3d v) {
	xspin.setValue(v.x);
	yspin.setValue(v.y);
	zspin.setValue(v.z);
    }
    
}