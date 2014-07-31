// FnMapPanel.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;

import java.util.List;
import java.util.ResourceBundle;
import org.jdom.Element;
import org.jdom.Attribute;
import jmri.util.jdom.LocaleSelector;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;

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
 * @version			$Revision$
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
    
    // these will eventually be passed in from the ctor
    int numFn = 14;  // include FL(f) and FL(r) in the total
    int numOut = 20;
    int maxFn = 30;  // include FL(f) and FL(r) in the total; update list of names if you update this
    int maxOut = 40; // update list of names if you update this
    
    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel _varModel;
    
    public FnMapPanel(VariableTableModel v, List<Integer> varsUsed, Element model) {
        if (log.isDebugEnabled()) log.debug("Function map starts");
        _varModel = v;
        
        // configure number of channels, arrays
        configOutputs(model);
        
        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);
        
        {
            JLabel l = new JLabel("Output wire or operation");
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
        
        labelAt( firstFn   , fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function0F"));
        labelAt( firstFn+ 1, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function0R"));
        if (numFn>2) labelAt( firstFn+ 2, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function1"));
        if (numFn>3) labelAt( firstFn+ 3, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function2"));
        if (numFn>4) labelAt( firstFn+ 4, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function3"));
        if (numFn>5) labelAt( firstFn+ 5, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function4"));
        if (numFn>6) labelAt( firstFn+ 6, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function5"));
        if (numFn>7) labelAt( firstFn+ 7, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function6"));
        if (numFn>8) labelAt( firstFn+ 8, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function7"));
        if (numFn>9) labelAt( firstFn+ 9, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function8"));
        if (numFn>10) labelAt( firstFn+10, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function9"));
        if (numFn>11) labelAt( firstFn+11, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function10"));
        if (numFn>12) labelAt( firstFn+12, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function11"));
        if (numFn>13) labelAt( firstFn+13, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function12"));
        if (numFn>14) labelAt( firstFn+14, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function13"));
        if (numFn>15) labelAt( firstFn+15, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function14"));
        if (numFn>16) labelAt( firstFn+16, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function15"));
        if (numFn>17) labelAt( firstFn+17, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function16"));
        if (numFn>18) labelAt( firstFn+18, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function17"));
        if (numFn>19) labelAt( firstFn+19, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function18"));
        if (numFn>20) labelAt( firstFn+20, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function19"));
        if (numFn>21) labelAt( firstFn+21, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function20"));
        if (numFn>22) labelAt( firstFn+22, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function21"));
        if (numFn>23) labelAt( firstFn+23, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function22"));
        if (numFn>24) labelAt( firstFn+24, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function23"));
        if (numFn>25) labelAt( firstFn+25, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function24"));
        if (numFn>26) labelAt( firstFn+26, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function25"));
        if (numFn>27) labelAt( firstFn+27, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function26"));
        if (numFn>28) labelAt( firstFn+28, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function27"));
        if (numFn>29) labelAt( firstFn+29, fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Function28"));
        
        // label outputs
        for (int iOut=0; iOut<numOut; iOut++) {
            labelAt( outputNum,   firstOut+iOut, outName[iOut]);
            labelAt( outputLabel, firstOut+iOut, outLabel[iOut]);
        }
        
        for (int iFn = 0; iFn < numFn; iFn++) {
            for (int iOut = 0; iOut < numOut; iOut++) {
                // find the variable using the output number or label
                // include an (alt) variant to enable Tsunami function exchange definitions
                String nameBase = fnList[iFn]+" controls output ";
                String[] names;
                if ( outName[iOut].equals(Integer.toString(iOut+1)) ) {
                    names = new String[] {nameBase+outName[iOut],nameBase+outName[iOut]+"(alt)"};
                } else {
                    names = new String[] {nameBase+(iOut+1),nameBase+(iOut+1)+"(alt)",
                        nameBase+outName[iOut],nameBase+outName[iOut]+"(alt)"};
                }
                for (String name : names) {
                    int iVar = _varModel.findVarIndex(name);
                    if (iVar>=0) {
                        if (log.isDebugEnabled()) log.debug("Process var: "+name+" as index "+iVar);
                        varsUsed.add(Integer.valueOf(iVar));
                        JComponent j = (JComponent)(_varModel.getRep(iVar, "checkbox"));
                        VariableValue var = _varModel.getVariable(iVar);
                        j.setToolTipText(PaneProgPane.addCvDescription(null, var.getCvDescription(), var.getMask()));
                        int row = firstFn+iFn;
                        int column = firstOut+iOut;
                        saveAt(row, column, j);
                    } else {
                        if (log.isDebugEnabled()) log.debug("Did not find var: "+name);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("Function map complete");
    }
    
    final String[] fnList = new String[] { "FL(f)", "FL(r)", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", 
                                           "F11", "F12", "F13", "F14", "F15", "F16", "F17", "F18", "F19", "F20",
                                           "F21", "F22", "F23", "F24", "F25", "F26", "F27", "F28"
                                            };
    
    final String[] outLabel = new String[] {"White", "Yellow", "Green", "Vlt/Brwn", "", "", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", ""
                                            };
    
    final String[] outName = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                                           "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                           "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                                           "31", "32", "33", "34", "35", "36", "37", "38", "39", "40"
                                            };
    
    void saveAt(int row, int column, JComponent j) {
        if (row<0 || column<0) return;
        cs.gridy = row;
        cs.gridx = column;
        gl.setConstraints(j, cs);
        add(j);
    }
    
    void labelAt(int row, int column, String name) {
        if (row<0 || column<0) return;
        JLabel t = new JLabel(" "+name+" ");
        saveAt(row, column, t);
    }
    
    /**
     * Use the "model" element from the decoder definition file
     * to configure the number of outputs and set up any that
     * are named instead of numbered.
     */
    @SuppressWarnings("unchecked")
	protected void configOutputs(Element model) {
        if (model==null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        // get numOuts, numFns or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try { if (a!=null) numOut = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numOuts value");}
        a = model.getAttribute("numFns");
        try { if (a!=null) numFn = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numFns value");}
        if (log.isDebugEnabled()) log.debug("numFns, numOuts "+numFn+","+numOut);
        // take all "output" children
        List<Element> elemList = model.getChildren("output");
        if (log.isDebugEnabled()) log.debug("output scan starting with "+elemList.size()+" elements");
        for (int i=0; i<elemList.size(); i++) {
            Element e = elemList.get(i);
            String name = e.getAttribute("name").getValue();
            // if this a number, or a character name?
            try {
                int outputNum = Integer.valueOf(name).intValue();
                // yes, since it was converted.  All we do with
                // these are store the label index (if it exists)
                String at = LocaleSelector.getAttribute(e, "label");
                if ( at!=null && outputNum<=numOut)
                    outLabel[outputNum-1] = at;
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (numOut<maxOut) {
                    outName[numOut] = name;
                    String at;
                    if ((at=LocaleSelector.getAttribute(e, "label"))!=null)
                        outLabel[numOut] = at;
                    else
                        outLabel[numOut] ="";
                    numOut++;
                }
            }
        }
    }
    
    /** clean up at end */
    public void dispose() {
        removeAll();
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(FnMapPanel.class.getName());
}
