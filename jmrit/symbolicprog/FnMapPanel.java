// FnMapPanel.java

package jmri.jmrit.symbolicprog;

import javax.swing.*;
import java.awt.*;
import jmri.jmrit.symbolicprog.*;
import com.sun.java.util.collections.List;

/** 
 * Provide a graphical representation of the NMRA S&RP mapping between cab functions
 * and physical outputs.
 *<P>
 * This is mapped via various definition variables.  A -1 means don't provide it. The
 * panel then creates a GridBayLayout: <dl>
 *  <DT>Column cvNum  	<DD> CV number (Typically 0)
 *  <DT>Column fnName  	<DD> Function name (Typically 1)
 *
 *  <DT>Row outputLabel	<DD> "output label" (Typically 0)
 *  <DT>Row outputNum	<DD> "output number" (Typically 1)
 *  <DT>Row outputName	<DD> "output name (or color)" (Typically 2)
 *
 *  <DT>Row firstFn     <DD> Row for first function, usually FL0.  Will go up from this,
 *							 with higher numbered functions in higher numbered columns.
 *  <DT>Column firstOut  <DD> Column for leftmost numbered output
 *</dl>
 *<P>
 * Although support for the "CV label column" is still here, its turned off now.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: FnMapPanel.java,v 1.5 2002-01-02 23:48:57 jacobsen Exp $
 */
public class FnMapPanel extends JPanel {
	// columns
	int cvNum = -1;
	int fnName = 0;
	int firstOut = 1;

	// rows
	int outputName = 0;
	int outputNum = 1;
	int outputLabel = 2;
	int firstFn = 3;
	
	// 
	int numFn = 14;  // include FL(f) and FL(r) in the total
	int numOut = 14;
	
	GridBagLayout gl = null;
	GridBagConstraints cs = null;
	VariableTableModel _varModel;
	
	public FnMapPanel(VariableTableModel v, List varsUsed) {
		if (log.isDebugEnabled()) log.debug("Function map starts");
		_varModel = v;
		// initialize the layout
		gl = new GridBagLayout();
		cs = new GridBagConstraints();
		setLayout(gl);
		
		{
			JLabel l = new JLabel("Output wire");
			cs.gridy = outputName;
			cs.gridx = 3;
			cs.gridwidth = GridBagConstraints.REMAINDER;
			gl.setConstraints(l, cs);
			add(l);
			cs.gridwidth = 1;
		}
		// dummy structure until we figure out how to convey CV numbers programmatically
		if (cvNum>=0) {
			labelAt( 0, 0, "CV");
			labelAt( firstFn   , cvNum, "33");
			labelAt( firstFn+ 1, cvNum, "34");
			labelAt( firstFn+ 2, cvNum, "35");
			labelAt( firstFn+ 3, cvNum, "36");
			labelAt( firstFn+ 4, cvNum, "37");
			labelAt( firstFn+ 5, cvNum, "38");
			labelAt( firstFn+ 6, cvNum, "39");
			labelAt( firstFn+ 7, cvNum, "40");
			labelAt( firstFn+ 8, cvNum, "41");
			labelAt( firstFn+ 9, cvNum, "42");
			labelAt( firstFn+10, cvNum, "43");
			labelAt( firstFn+11, cvNum, "44");
			labelAt( firstFn+12, cvNum, "45");
			labelAt( firstFn+13, cvNum, "46");
		}
				
		labelAt(0,fnName, "Description");

		labelAt( firstFn   , fnName, "Forward Headlight FL(f)");
		labelAt( firstFn+ 1, fnName, "Reverse Headlight FL(r)");
		labelAt( firstFn+ 2, fnName, "Function 1");
		labelAt( firstFn+ 3, fnName, "Function 2");
		labelAt( firstFn+ 4, fnName, "Function 3");
		labelAt( firstFn+ 5, fnName, "Function 4");
		labelAt( firstFn+ 6, fnName, "Function 5");
		labelAt( firstFn+ 7, fnName, "Function 6");
		labelAt( firstFn+ 8, fnName, "Function 7");
		labelAt( firstFn+ 9, fnName, "Function 8");
		labelAt( firstFn+10, fnName, "Function 9");
		labelAt( firstFn+11, fnName, "Function 10");
		labelAt( firstFn+12, fnName, "Function 11");
		labelAt( firstFn+13, fnName, "Function 12");
		
		// label outputs
		labelAt( outputNum, firstOut   , outList[0]);
		labelAt( outputNum, firstOut+ 1, outList[1]);
		labelAt( outputNum, firstOut+ 2, outList[2]);
		labelAt( outputNum, firstOut+ 3, outList[3]);
		labelAt( outputNum, firstOut+ 4, outList[4]);
		labelAt( outputNum, firstOut+ 5, outList[5]);
		labelAt( outputNum, firstOut+ 6, outList[6]);
		labelAt( outputNum, firstOut+ 7, outList[7]);
		labelAt( outputNum, firstOut+ 8, outList[8]);
		labelAt( outputNum, firstOut+ 9, outList[9]);
		labelAt( outputNum, firstOut+10, outList[10]);
		labelAt( outputNum, firstOut+11, outList[11]);
		labelAt( outputNum, firstOut+12, outList[12]);
		labelAt( outputNum, firstOut+13, outList[13]);
		
		labelAt( outputLabel, firstOut   , outLabel[0]);
		labelAt( outputLabel, firstOut+ 1, outLabel[1]);
		labelAt( outputLabel, firstOut+ 2, outLabel[2]);
		labelAt( outputLabel, firstOut+ 3, outLabel[3]);
		labelAt( outputLabel, firstOut+ 4, outLabel[4]);
		labelAt( outputLabel, firstOut+ 5, outLabel[5]);
		labelAt( outputLabel, firstOut+ 6, outLabel[6]);
		labelAt( outputLabel, firstOut+ 7, outLabel[7]);
		labelAt( outputLabel, firstOut+ 8, outLabel[8]);
		labelAt( outputLabel, firstOut+ 9, outLabel[9]);
		labelAt( outputLabel, firstOut+10, outLabel[10]);
		labelAt( outputLabel, firstOut+11, outLabel[11]);
		labelAt( outputLabel, firstOut+12, outLabel[12]);
		labelAt( outputLabel, firstOut+13, outLabel[13]);
		
		for (int iFn = 0; iFn < numFn; iFn++) {
			for (int iOut = 0; iOut < numOut; iOut++) {
				// find the variable
				String name = fnList[iFn]+" controls output "+outList[iOut];
				int iVar = _varModel.findVarIndex(name);
				if (iVar>=0) {
					if (log.isDebugEnabled()) log.debug("Process var: "+name+" as index "+iVar);
					varsUsed.add(new Integer(iVar));
					JComponent j = (JComponent)(_varModel.getRep(iVar, "checkbox"));
					int row = firstFn+iFn;
					int column = firstOut+iOut;
					saveAt(row, column, j);
				} else {
					if (log.isDebugEnabled()) log.debug("Did not find var: "+name);
				}
			}
		}
		if (log.isDebugEnabled()) log.debug("Function map complete");
	}
	
	final String[] fnList = new String[] { "FL(f)", "FL(r)", "F1", "F2", "F3", "F4", "F5", "F6", "F7", 
									"F8", "F9", "F10", "F11", "F12" };
	final String[] outList = new String[] {"14", "13", "12", "11", "10", "9", "8", "7", "6",
									"5", "4", "3", "2", "1" };

	final String[] outLabel = new String[] {"", "", "", "", "", "", "", "", "",
									"", "Violet", "Green", "Yellow", "White" };
									
	void saveAt(int row, int column, JComponent j) {
		if (row<0 || column<0) return;
		cs.gridy = row;
		cs.gridx = column;
		gl.setConstraints(j, cs);
		add(j);
	}
		
	void labelAt(int row, int column, String name) {
		if (row<0 || column<0) return;
		saveAt(row, column, new JLabel(" "+name+" "));
	}
			
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(FnMapPanel.class.getName());
		
}
