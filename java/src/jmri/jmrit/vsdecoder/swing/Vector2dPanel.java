package jmri.jmrit.vsdecoder.swing;

import javax.vecmath.Vector2d;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.SpinnerNumberModel;
import javax.swing.BoxLayout;


public class Vector2dPanel extends JPanel {
    
    private Vector2d _value;
    private JSpinner xspin, yspin, zspin;
    private double _minspin, _maxspin, _spininc;
    private Boolean _initialized = false;

    public Vector2dPanel(String l1, String l2, double spinval, double min_spin, double max_spin, double spin_inc) {
	super();
	_minspin = min_spin;
	_maxspin = max_spin;
	_spininc = spin_inc;
	initGui(l1, l2, spinval);
    }

    protected void initGui(String l1, String l2, double spinval) {
	this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
	
	xspin = new JSpinner(new SpinnerNumberModel(spinval, _minspin, _maxspin, _spininc));
	yspin = new JSpinner(new SpinnerNumberModel(spinval, _minspin, _maxspin, _spininc));

	this.add(new JLabel(l1));
	this.add(xspin);
	this.add(new JLabel(l2));
	this.add(yspin);

	_initialized = true;
    }

    public void setMinValue(double min) {
	_minspin = min;
	if (_initialized) {
	    ((SpinnerNumberModel)xspin.getModel()).setMinimum(min);
	    ((SpinnerNumberModel)yspin.getModel()).setMinimum(min);
	}
    }

    public void setMaxValue(double max) {
	_maxspin = max;
	if (_initialized) {
	    ((SpinnerNumberModel)xspin.getModel()).setMaximum(max);
	    ((SpinnerNumberModel)yspin.getModel()).setMaximum(max);
	}
    }

    public void setIncrement(double inc) {
	_spininc = inc;
	if (_initialized) {
	    ((SpinnerNumberModel)xspin.getModel()).setStepSize(inc);
	    ((SpinnerNumberModel)yspin.getModel()).setStepSize(inc);
	}
    }

    public Vector2d getValue() {
	Double x = (Double)((SpinnerNumberModel)xspin.getModel()).getNumber();
	Double y = (Double)((SpinnerNumberModel)yspin.getModel()).getNumber();

	return(new Vector2d(x, y));
    }

    public void setValue(Vector2d v) {
	xspin.setValue(v.x);
	yspin.setValue(v.y);
    }

}