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
    int fnName = 0;
    int firstOut = 1;
    
    // rows
    int outputName = 0;
    int outputNum = 1;
    int outputLabel = 2;
    int firstFn = 3;
    
    // these will eventually be passed in from the ctor
    int highestFn = 28;
    int numFn = (highestFn +2) * 3 ;  // include FL and F0, plus all (f) and (r) variants in the total
    int numOut = 20;
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
            JLabel l = new JLabel(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("OutputWireOr"));
            cs.gridy = outputName;
            cs.gridx = 3;
            cs.gridwidth = GridBagConstraints.REMAINDER;
            gl.setConstraints(l, cs);
            add(l);
            cs.gridwidth = 1;
        }
        
        labelAt(0,fnName, ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("Description"), GridBagConstraints.LINE_START);
        
        // label outputs
        for (int iOut=0; iOut<numOut; iOut++) {
            labelAt( outputNum,   firstOut+iOut, outName[iOut]);
            labelAt( outputLabel, firstOut+iOut, outLabel[iOut]);
        }
        
        // Loop through function names and output names looking for variables
        int row = firstFn;
        for (int iFn = -1; iFn <= highestFn; iFn++) {
            if ( (row - firstFn) >= numFn ) break; // for compatibility with legacy defintions
            for (String fnVar : fnVarList ) {
                String fnNameString = "F" + ((iFn == -1) ? "L" : String.valueOf(iFn)) + fnVar;
                boolean rowIsUsed = false;
                for (int iOut = 0; iOut < numOut; iOut++) {
                    // find the variable using the output number or label
                    // include an (alt) variant to enable Tsunami function exchange definitions
                    String nameBase = fnNameString + " controls output ";
                    String[] names;
                    if ( outName[iOut].equals(Integer.toString(iOut+1)) ) {
                        names = new String[] {nameBase+outName[iOut],nameBase+outName[iOut]+"(alt)"};
                    } else {
                        names = new String[] {nameBase+(iOut+1),nameBase+(iOut+1)+"(alt)",
                            nameBase+outName[iOut],nameBase+outName[iOut]+"(alt)"};
                    }
                    for (String name : names) {
//                         log.info(name);
                        int iVar = _varModel.findVarIndex(name);
                        if (iVar>=0) {
                            if (log.isDebugEnabled()) log.debug("Process var: "+name+" as index "+iVar);
                            varsUsed.add(Integer.valueOf(iVar));
                            JComponent j = (JComponent)(_varModel.getRep(iVar, "checkbox"));
                            VariableValue var = _varModel.getVariable(iVar);
                            j.setToolTipText(PaneProgPane.addCvDescription(null, var.getCvDescription(), var.getMask()));
                            int column = firstOut+iOut;
                            saveAt(row, column, j);
                            rowIsUsed = true;                          
                        } else {
                            if (log.isDebugEnabled()) log.debug("Did not find var: "+name);
                        }
                    }
                }
                if ( rowIsUsed ) {
                    if ( fnNameString.equals("FL(f)") ) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionLf");
                    } else if ( fnNameString.equals("FL(r)") ) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionLr");
                    } else if ( fnNameString.endsWith(fnVarList[1] ) ) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionPrefix") + " " +
                            fnNameString.substring(1, fnNameString.length() - fnVarList[1].length()) + 
                            ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionSuffixF");
                    } else if ( fnNameString.endsWith(fnVarList[2] ) ) {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionPrefix") + " " +
                            fnNameString.substring(1, fnNameString.length() - fnVarList[2].length()) + 
                            ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionSuffixR");
                    } else {
                        fnNameString = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FunctionPrefix") + " " +
                            fnNameString.substring(1); 
                    }
                    labelAt(row, fnName, fnNameString, GridBagConstraints.LINE_START);
                    row++;
                }

            }
        }
        if (log.isDebugEnabled()) log.debug("Function map complete");
    }
    
    final String[] fnVarList = new String[] {"", "(f)", "(r)" };
    
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
        this.saveAt(row, column, j, GridBagConstraints.CENTER);
    }
    
    void saveAt(int row, int column, JComponent j, int anchor) {
        if (row<0 || column<0) return;
        cs.gridy = row;
        cs.gridx = column;
        cs.anchor = anchor;
        gl.setConstraints(j, cs);
        add(j);
    }
    
    void labelAt(int row, int column, String name) {
        this.labelAt(row, column, name, GridBagConstraints.CENTER);
    }
    
    void labelAt(int row, int column, String name, int anchor) {
        if (row<0 || column<0) return;
        JLabel t = new JLabel(" "+name+" ");
        saveAt(row, column, t, anchor);
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
