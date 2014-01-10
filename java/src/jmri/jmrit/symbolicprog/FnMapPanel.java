// FnMapPanel.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import javax.swing.UIManager;
import java.awt.*;

import java.util.List;
import org.jdom.Element;
import org.jdom.Attribute;
import jmri.util.jdom.LocaleSelector;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgPane;
import jmri.util.SystemType;
import jmri.Application;

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
    int guiWarning = 0;
    int outputName = 1;
    int outputNum = 2;
    int outputLabel = 3;
    int firstFn = 4;
    
    // these will eventually be passed in from the ctor
    int numFn = 14;  // include FL(f) and FL(r) in the total
    int numOut = 20;
    int maxFn = 30;  // include FL(f) and FL(r) in the total; update list of names if you update this
    int maxOut = 128; // update list of names if you update this

    final String[] outName = new String[maxOut];
    final String[] outLabel = new String[maxOut];
    
    boolean extFnsESU = false;
    
    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel _varModel;
    
    public FnMapPanel(VariableTableModel v, List<Integer> varsUsed, Element model) {
        if (log.isDebugEnabled()) log.debug("Function map starts");
        _varModel = v;
        
        // set up default names and labels
        for (int iOut=0; iOut<maxOut; iOut++) {
            if (iOut < outNameDef.length) {
                outName[iOut] = outNameDef[iOut];
            } else {
                outName[iOut] = Integer.toString(iOut+1);
            }
            if (iOut < outLabelDef.length) {
                outLabel[iOut] = outLabelDef[iOut];
            } else {
                outLabel[iOut] = "";
            }
        }
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
        
        if (extFnsESU) {
            labelAt(outputNum,fnName, "Mapping");
            labelAt(outputLabel,fnName, "Row");
            labelAt(firstFn+numFn,fnName, "Column");
            
            for (int iFn = 0; iFn < numFn; iFn++) {
                labelAt( firstFn+iFn, fnName, Integer.toString(iFn+1));
                labelAt( firstFn+iFn, numOut+1, Integer.toString(iFn+1));
            }
        } else {
            labelAt(outputName,fnName, "Description");
        
            labelAt( firstFn   , fnName, "Forward Headlight F0(F)");
            labelAt( firstFn+ 1, fnName, "Reverse Headlight F0(R)");
            if (numFn>2) labelAt( firstFn+ 2, fnName, "Function 1");
            if (numFn>3) labelAt( firstFn+ 3, fnName, "Function 2");
            if (numFn>4) labelAt( firstFn+ 4, fnName, "Function 3");
            if (numFn>5) labelAt( firstFn+ 5, fnName, "Function 4");
            if (numFn>6) labelAt( firstFn+ 6, fnName, "Function 5");
            if (numFn>7) labelAt( firstFn+ 7, fnName, "Function 6");
            if (numFn>8) labelAt( firstFn+ 8, fnName, "Function 7");
            if (numFn>9) labelAt( firstFn+ 9, fnName, "Function 8");
            if (numFn>10) labelAt( firstFn+10, fnName, "Function 9");
            if (numFn>11) labelAt( firstFn+11, fnName, "Function 10");
            if (numFn>12) labelAt( firstFn+12, fnName, "Function 11");
            if (numFn>13) labelAt( firstFn+13, fnName, "Function 12");
            if (numFn>14) labelAt( firstFn+14, fnName, "Function 13");
            if (numFn>15) labelAt( firstFn+15, fnName, "Function 14");
            if (numFn>16) labelAt( firstFn+16, fnName, "Function 15");
            if (numFn>17) labelAt( firstFn+17, fnName, "Function 16");
            if (numFn>18) labelAt( firstFn+18, fnName, "Function 17");
            if (numFn>19) labelAt( firstFn+19, fnName, "Function 18");
            if (numFn>20) labelAt( firstFn+20, fnName, "Function 19");
            if (numFn>21) labelAt( firstFn+21, fnName, "Function 20");
            if (numFn>22) labelAt( firstFn+22, fnName, "Function 21");
            if (numFn>23) labelAt( firstFn+23, fnName, "Function 22");
            if (numFn>24) labelAt( firstFn+24, fnName, "Function 23");
            if (numFn>25) labelAt( firstFn+25, fnName, "Function 24");
            if (numFn>26) labelAt( firstFn+26, fnName, "Function 25");
            if (numFn>27) labelAt( firstFn+27, fnName, "Function 26");
            if (numFn>28) labelAt( firstFn+28, fnName, "Function 27");
            if (numFn>29) labelAt( firstFn+29, fnName, "Function 28");
        }        
        // label outputs
        for (int iOut=0; iOut<numOut; iOut++) {
            labelAt( outputNum,   firstOut+iOut, outName[iOut]);
            labelAt( outputLabel, firstOut+iOut, outLabel[iOut]);
            if (extFnsESU) {
                labelAt( firstFn+numFn, firstOut+iOut, String.valueOf(iOut+1));
            }
        }
        
        for (int iFn = 0; iFn < numFn; iFn++) {
            for (int iOut = 0; iOut < numOut; iOut++) {
                // find the variable using the output label
                String name = "";

                if (extFnsESU) {
                        name = "F"+Integer.toString(iFn+1)+" controls output "+Integer.toString(iOut+1);
                } else {
                        name = fnList[iFn]+" controls output "+outName[iOut];
                }
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

        // warn if using OS X GUI
        if (extFnsESU && (SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel())) {
            int numWarnings = numOut/26;
            for (int i=0; i<numWarnings; i++) {
                JLabel l = new JLabel("<html><center><strong>You are using the Mac OS X native GUI<br/>"+
                    "and may experience slow scrolling</strong><br/>"+
                    "If affected, change GUI in "+Application.getApplicationName()+" &gt; Preferences &gt; Display  &gt; GUI</center></html>");
                cs.gridy = guiWarning;
                cs.gridx = numOut/(numWarnings*2)+i*numOut/numWarnings;
                cs.weightx = 1;
                cs.gridwidth = GridBagConstraints.RELATIVE;
                cs.fill = GridBagConstraints.HORIZONTAL;
                gl.setConstraints(l, cs);
                add(l);
            }
        }
        if (log.isDebugEnabled()) log.debug("Function map complete");
    }
    
    final String[] fnList = new String[] { "FL(f)", "FL(r)", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", 
                                           "F11", "F12", "F13", "F14", "F15", "F16", "F17", "F18", "F19", "F20",
                                           "F21", "F22", "F23", "F24", "F25", "F26", "F27", "F28"
                                            };
    
    final String[] outLabelDef = new String[] {"White", "Yellow", "Green", "Vlt/Brwn", "", "", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", "",
                                            "", "", "", "", "","", "", "", "", ""
                                            };
    
    final String[] outNameDef = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                                           "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                           "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
                                           "31", "32", "33", "34", "35", "36", "37", "38", "39", "40"
                                            };
    
    final String[] outDescESU = new String[] {"Loco/drive","Loco/stop","Dir/fwd","Dir/rev"
                                           ,"F0/on","F0/off","F1/on","F1/off","F2/on","F2/off","F3/on","F3/off","F4/on","F4/off","F5/on","F5/off","F6/on","F6/off","F7/on","F7/off","F8/on","F8/off","F9/on","F9/off"
                                           ,"F10/on","F10/off","F11/on","F11/off","F12/on","F12/off","F13/on","F13/off","F14/on","F14/off","F15/on","F15/off","F16/on","F16/off","F17/on","F17/off","F18/on","F18/off","F19/on","F19/off"
                                           ,"F20/on","F20/off","F21/on","F21/off","F22/on","F22/off","F23/on","F23/off","F24/on","F24/off","F25/on","F25/off","F26/on","F26/off","F27/on","F27/off","F28/on","F28/off"
                                           ,"sensW/on","sensW/off","Sens1/on","Sens1/off","Sens2/on","Sens2/off","Sens3/on","Sens3/off","Sens4/on","Sens4/off"
                                           ,"Head/light[1]","Rear/light[1]","Aux/1[1]","Aux/2[1]","Aux/3","Aux/4","Aux/5","Aux/6","Aux/7","Aux/8","Aux/9","Aux/10","Head/light[2]","Rear/light[2]","Aux/1[2]","Aux/2[2]"
                                           ,"Mome/off","Shunt/mode","Dynam/brake","Fire/box","Dim/lights","Grade/cross","not/used","not/used","Smoke/gen","Notch/up","Notch/down","Sound/fade","Bk sql/off","Doppler/effect","Volume/& mute","Shift/mode"
                                           ,"SS/1","SS/2","SS/3","SS/4","SS/5","SS/6","SS/7","SS/8","SS/9","SS/10","SS/11","SS/12","SS/13","SS/14","SS/15","SS/16","SS/17","SS/18","SS/19","SS/20","SS/21","SS/22","SS/23","SS/24"
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
        // get numOuts, numFns, extFnsESU or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try { if (a!=null) numOut = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numOuts value");}
        a = model.getAttribute("numFns");
        try { if (a!=null) numFn = Integer.valueOf(a.getValue()).intValue();}
        catch (Exception e) {log.error("error handling decoder's numFns value");}
        a = model.getAttribute("extFnsESU");
        try { if (a!=null) extFnsESU = (a.getValue()).equalsIgnoreCase("yes");}
        catch (Exception e) {log.error("error handling decoder's extFnsESU value");}        
        if (log.isDebugEnabled()) log.debug("numFns, numOuts, extFnsESU "+numFn+","+numOut+","+extFnsESU);
        
        // add ESU default labels before reading custom ones
        for (int iOut=0; iOut<maxOut; iOut++) {
            loadDescESU(iOut, outDescESU[iOut]);
        } 

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
                if ( at!=null && outputNum<=numOut) {
                    if (extFnsESU) {
                        loadDescESU(outputNum-1, at);
                    } else {
                        outLabel[outputNum-1] = at;
                    }
                }
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (extFnsESU) {
                    if (i<maxOut) {
                        outName[i] = name;
                        String at;
                        if ((at=LocaleSelector.getAttribute(e, "label"))!=null)
                            outLabel[i] = at;
                        else
                            outLabel[i] ="";
                    }
                } else {
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
    }
    
    // split and load ESU default labels
    void loadDescESU(int iOut, String theLabel) {
        if (extFnsESU && (iOut < outDescESU.length)) {
            String itemESU[] = theLabel.split("/");
            if (itemESU.length > 1) {
                outName[iOut] = itemESU[0];
                outLabel[iOut] = itemESU[1];
            } else {
                outName[iOut] = itemESU[0];
                outLabel[iOut] = "";
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
