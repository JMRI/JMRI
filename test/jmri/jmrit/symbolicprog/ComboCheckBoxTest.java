/** 
 * ComboCheckBoxTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.symbolicprog;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

import jmri.*;
import jmri.progdebugger.*;

public class ComboCheckBoxTest extends TestCase {

	public void testToOriginal() {
		// create an enum variable pointed at CV 81 and connect
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		if (log.isInfoEnabled()) log.info("Enum variable created, loaded");
		
		EnumVariableValue var = new EnumVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		addTestItems(var);
		if (log.isInfoEnabled()) log.info("Enum variable created");

		JComboBox combo = (JComboBox)(var.getValue());
		
		// create object under test
		ComboCheckBox b = new ComboCheckBox(combo,var);
		if (log.isInfoEnabled()) log.info("ComboCheckBox created");
		
		// set it to "checked" & test state
		b.doClick();
		Assert.assertEquals("1 click checkbox state ", true, b.isSelected());
		Assert.assertEquals("1 click original state ", 1, combo.getSelectedIndex());
		
		// set it to unchecked & test state
		b.doClick();
		Assert.assertEquals("2 click checkbox state ", false, b.isSelected());
		Assert.assertEquals("2 click original state ", 0, combo.getSelectedIndex());

		// set it to "checked" again & test state
		b.doClick();
		Assert.assertEquals("3 click checkbox state ", true, b.isSelected());
		Assert.assertEquals("3 click original state ", 1, combo.getSelectedIndex());
		
	}
	
	public void testFromOriginal() {
		// create an enum variable pointed at CV 81 and connect
		Vector v = createCvVector();
		CvValue cv = new CvValue(81);
		cv.setValue(3);
		v.setElementAt(cv, 81);
		EnumVariableValue var = new EnumVariableValue("name", "comment", false, 81, "XXVVVVXX", 0, 255, v, null, null);
		addTestItems(var);
		JComboBox combo = (JComboBox)(var.getValue());
		
		// create object under test
		ComboCheckBox b = new ComboCheckBox(combo,var);
		
		// set combo box to 1 and check state
		combo.setSelectedIndex(1);
		Assert.assertEquals("index 1 checkbox state ", true, b.isSelected());
		Assert.assertEquals("index 1 original state ", 1, combo.getSelectedIndex());
		
		// set it to unchecked & test state
		combo.setSelectedIndex(0);
		Assert.assertEquals("index 0 checkbox state ", false, b.isSelected());
		Assert.assertEquals("index 0 original state ", 0, combo.getSelectedIndex());

		// set it to "checked" again & test state
		combo.setSelectedIndex(1);
		Assert.assertEquals("2nd index 1 checkbox state ", true, b.isSelected());
		Assert.assertEquals("2nd index 1 original state ", 1, combo.getSelectedIndex());
		
	}
	

	protected void addTestItems(EnumVariableValue var) {
		var.nItems(2);
		var.addItem("Value0");
		var.addItem("Value1");
		var.lastItem();
	}
	
	protected Vector createCvVector() {
		Vector v = new Vector(512);
		for (int i=0; i < 512; i++) v.addElement(null);
		return v;
	}

	// from here down is testing infrastructure
	
	public ComboCheckBoxTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {ComboCheckBoxTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(ComboCheckBoxTest.class);
		return suite;
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ComboCheckBoxTest.class.getName());

}
