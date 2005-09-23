/**
 * 
 */
package jmri.util;

import java.awt.*;
import javax.swing.*;

/**
 * JmriSpinner - use when you want a spinner but aren't sure the JRE has them.
 * Displays a JSpinner if available, else a TextField.
 * @author John Harper
 *
 */
public class JmriSpinner extends JPanel {
	
	int value;				// current value
	int minimum;
	int maximum;
	int step;				// increment if there's a spinner
	int defaultValue;
	
	JSpinner spinner = null;
	TextField textfield = null;
	
	public JmriSpinner() {
		try {
//			if (1!=2) throw new Exception(); un-comment to test exception handling
			spinner = new JSpinner();
		} catch (Exception ex) { };
		setLayout(new GridLayout(1,1,5,5));
		if (spinner==null) {
			textfield = new TextField();
			textfield.setEditable(true);
			add(textfield);
		} else {
			add(spinner);
		}
	}
	
	/**
	 * Create JmriSpinner with all values.
	 * @param v	initial value
	 * @param min	minimum value
	 * @param max	maximum value
	 * @param s	step size
	 * @param def	default value
	 */
	public JmriSpinner(int v, int min, int max, int s, int def) {
		this();
		setValues(v, min, max, s, def); 
	}
	
	/**
	 * Set new values.
	 * @param v	initial value
	 * @param min	minimum value
	 * @param max	maximum value
	 * @param s	step size
	 * @param def	default value
	 */
	public void setValues(int v, int min, int max, int s, int def) {
		value = v;
		minimum = min;
		maximum = max;
		step = s;
		defaultValue = def;
		if (spinner!=null) {
			spinner.setModel(new SpinnerNumberModel(value, minimum, maximum, step));
		} else {
			textfield.setText(String.valueOf(value));
		}
	}
	
	/**
	 * Get present value
	 * @return	present value
	 */
	public int getValue() {
		if (spinner==null) {
			try {
				value = Integer.parseInt(textfield.getText());
			} catch (Exception ex) {
				value = defaultValue;
			}
			if (value<minimum) { value = minimum; }
			else if (value>maximum) { value = maximum; }
		} else {
			value = ((Integer)spinner.getValue()).intValue();
		}
		return value;
	}
	
	
}
