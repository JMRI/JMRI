package jmri.jmrit.symbolicprog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.util.CvUtil;
import jmri.util.jdom.LocaleSelector;

import org.jdom2.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide a graphical representation of the NMRA Standard mapping between cab
 * functions and physical outputs.
 * <p>
 * Uses data from the "model" element from the decoder definition file to
 * configure the number of rows and columns and set up any custom column
 * names:
 * <dl>
 * <dt>numOuts</dt>
 * <dd>Number of physical outputs.</dd>
 * <dd>&nbsp;</dd>
 * <dt>numFns</dt>
 * <dd>Maximum number of function rows to display.</dd>
 * <dd>&nbsp;</dd>
 * <dt>output</dt>
 * <dd>name="n" label="yyy"</dd>
 * <dd>&nbsp;-&nbsp;Set lower line of heading for column number "n" to
 * "yyy".*</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="n" label="xxx|yyy"</dd>
 * <dd>&nbsp;-&nbsp;Set upper line of heading for column number "n" to "xxx" and
 * lower line to "yyy".*</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="n" label="|"</dd>
 * <dd>&nbsp;-&nbsp;Sets both lines of heading for column number "n" to blank,
 * causing the column to be suppressed from the table.*</dd>
 * <dd>&nbsp;</dd>
 * <dd>&nbsp;*&nbsp;The forms above increase the value of numOuts to n if
 * numOuts &lt; n.</dd>
 * <dd>&nbsp;</dd>
 * <dd>name="text1" label="text2"</dd>
 * <dd>&nbsp;-&nbsp;Set upper line of heading of column numOuts+1 to "xxx" and
 * lower line to "yyy". numOuts is then incremented.</dd>
 * <dd>&nbsp;(This is a legacy form, the other forms are preferred.)</dd>
 * </dl>
 * <dl>
 * <dt>Default column headings:</dt>
 * <dd>First row is the column number.</dd>
 * <dd>Second row is defined in "SymbolicProgBundle.properties".</dd>
 * <dd>Column headings can be overridden by the "output" elements documented
 * above.</dd>
 * <dd>&nbsp;</dd>
 * <dt>Two rows are available for column headings:</dt>
 * <dd>Use the "|" character to designate a row break.</dd>
 * </dl>
 * <dl>
 * <dt>Columns will be suppressed if any of the following are true:</dt>
 * <dd>No variables are found for that column.</dd>
 * <dd>The column output name is of the form name="n" label="|".</dd>
 * <dd>Column number is &gt; maxOut (an internal variable, currently 40).</dd>
 * </dl>
 * <dl>
 * <dt>Searches the decoder file for variable definitions of the form:</dt>
 * <dd>"Fd controls output n" (where d is a function number in the range 0-28
 * and n is an output number in the range 0-maxOut)</dd>
 * <dd>"FL controls output n" (L for light)</dd>
 * <dd>"Sd controls output n" (where s is a sensor number in the range 0-28
 * and n is an output number in the range 0-maxOut)</dd>
 * <dd>"STOP controls output n" (where STOP designates a decoder state)</dd>
 * <dd>"DRIVE controls output n" (where DRIVE designates a decoder state)</dd>
 * <dd>"FWD controls output n" (where FWD designates a decoder state)</dd>
 * <dd>"REV controls output n" (where REV designates a decoder state)</dd>
 * <dd><br>Directional variants of all the above forms:</dd>
 * <dd>"xxx(f) controls output n"</dd>
 * <dd>"xxx(r) controls output n"</dd>
 * <dd><br>Alternate variants of all the above forms:</dd>
 * <dd>"xxx controls output n(alt)" (allows an alternate definition for the same
 * variable, such as used by Tsunami decoders)</dd>
 * <dd>"xxx(f) controls output n(alt)"</dd>
 * <dd>"xxx(r) controls output n(alt)"</dd>
 * <dd><br>
 * The "tooltip" &amp; "label" attributes on a fnmapping variable are ignored.
 * Expanded internationalized tooltips are generated in the code.
 * </dd>
 * </dl>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Dave Heap Copyright (C) 2016
 */
public class FnMapPanel extends JPanel {

    // GridBayLayout column numbers
    int fnNameCol = 0;
    int firstOutCol = 1;

    // GridBayLayout row numbers
    int outputNameRow = 0;
    int outputNumRow = 1;
    int outputLabelRow = 2;
    int firstFnRow = 3;

    // Some limits and defaults
    int highestFn = 28;
    int highestSensor = 28;
    int numFn;  // calculated later
    int numOut = 20; // default number of physical outputs
    int maxOut = 40; // maximum number of output columns

    final String[] outName = new String[maxOut];
    final String[] outLabel = new String[maxOut];
    final boolean[] outIsUsed = new boolean[maxOut];

    final String[] fnExtraList = new String[]{"STOP", "DRIVE", "FWD", "REV", "FL"};
    final String[] fnVariantList = new String[]{"", "(f)", "(r)"};

    List<String> fnList;
    GridBagLayout gl = null;
    GridBagConstraints cs = null;
    VariableTableModel _varModel;

    public FnMapPanel(VariableTableModel v, List<Integer> varsUsed, Element model) {
        if (log.isDebugEnabled()) {
            log.debug("Function map starts");
        }
        _varModel = v;

        // Set up fnList array
        this.fnList = new ArrayList<>();
        fnList.addAll(Arrays.asList(fnExtraList));
        for (int i = 0; i <= highestFn; i++) {
            fnList.add("F" + i);
        }
        for (int i = 0; i <= highestSensor; i++) {
            fnList.add("S" + i);
        }

        numFn = fnList.size() * fnVariantList.length;

        // set up default names and labels
        for (int iOut = 0; iOut < maxOut; iOut++) {
            outName[iOut] = Integer.toString(iOut + 1);
            outIsUsed[iOut] = false;
            // get default labels, if any
            try {
                outLabel[iOut] = Bundle.getMessage("FnMapOutLabelDefault_" + (iOut + 1));
            } catch (java.util.MissingResourceException e) {
                outLabel[iOut] = "";  // no default label specified
            }
        }

        // configure number of channels, arrays
        configOutputs(model);

        // initialize the layout
        gl = new GridBagLayout();
        cs = new GridBagConstraints();
        setLayout(gl);

        {
            JLabel l = new JLabel(Bundle.getMessage("FnMapOutWireOr"));
            cs.gridy = outputNameRow;
            cs.gridx = firstOutCol;
            cs.gridwidth = GridBagConstraints.REMAINDER;
            gl.setConstraints(l, cs);
            add(l);
            cs.gridwidth = 1;
        }

        labelAt(0, fnNameCol, Bundle.getMessage("FnMapDesc"), GridBagConstraints.LINE_START);

// Loop through function names and output names looking for variables
        int row = firstFnRow;
        for (String fnNameBase : fnList) {
            if ((row - firstFnRow) >= numFn) {
                break; // for compatibility with legacy defintions
            }
            for (String fnDirVariant : fnVariantList) {
                String fnNameString = fnNameBase + fnDirVariant;
//                log.info(fnNameString);
                boolean rowIsUsed = false;
                for (int iOut = 0; iOut < numOut; iOut++) {
                    // if column is not suppressed by blank headers
                    if (!outName[iOut].equals("") || !outLabel[iOut].equals("")) {
                        // find the variable using the output number or label
                        // include an (alt) variant to enable Tsunami function exchange definitions
                        String searchNameBase = fnNameString + " controls output ";
                        List<String> names = new ArrayList<>();
                        if (!outName[iOut].equals(Integer.toString(iOut + 1))) {
                            names.add(searchNameBase + (iOut + 1));
                            names.add(searchNameBase + (iOut + 1) + "(alt)");
                        }
                        names.add(searchNameBase + outName[iOut]);
                        names.add(searchNameBase + outName[iOut] + "(alt)");
                        for (String name : names) {
//                            log.info("Search name='" + name + "'");
                            int iVar = _varModel.findVarIndex(name);
                            if (iVar >= 0) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Process var: " + name + " as index " + iVar);
                                }
                                varsUsed.add(Integer.valueOf(iVar));
                                VariableValue var = _varModel.getVariable(iVar);
                                // Only single-bit (exactly two options) variables should use checkbox
                                // this really would be better fixed in EnumVariableValue
                                // done here to avoid side effects elsewhere
                                String displayFormat = "checkbox";
                                if ((var.getMask() != null) && (((var.getMask().replace("X", "")).length()) != 1)) {
                                    displayFormat = "";
                                }
                                JComponent j = (JComponent) (_varModel.getRep(iVar, displayFormat));
                                j.setToolTipText(CvUtil.addCvDescription((fnNameString + " "
                                        + Bundle.getMessage("FnMapControlsOutput") + " "
                                        + outName[iOut] + " " + outLabel[iOut]), var.getCvDescription(), var.getMask()));
                                int column = firstOutCol + iOut;
                                saveAt(row, column, j);
                                rowIsUsed = true;
                                outIsUsed[iOut] = true;
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Did not find var: " + name);
                                }
                            }
                        }
                    }
                }
                if (rowIsUsed) {
                    if (fnNameBase.matches("F\\d+")) {
                        fnNameString = Bundle.getMessage("FnMap_F") + " " + fnNameBase.substring(1);
                        if (!fnDirVariant.equals("")) {
                            fnNameString = fnNameString + Bundle.getMessage("FnMap_" + fnDirVariant);
                        }
                    } else if (fnNameBase.matches("S\\d+")) {
                        fnNameString = Bundle.getMessage("FnMap_S") + " " + fnNameBase.substring(1);
                        if (!fnDirVariant.equals("")) {
                            fnNameString = fnNameString + Bundle.getMessage("FnMap_" + fnDirVariant);
                        }
                    } else {
                        try {  // See if we have a match for whole fnNameString
                            fnNameString = Bundle.getMessage("FnMap_" + fnNameString);
                        } catch (java.util.MissingResourceException e) {
                            try {  // Else see if we have a match for fnNameBase
                                fnNameString = Bundle.getMessage("FnMap_" + fnNameBase);
                                if (!fnDirVariant.equals("")) { // Add variant
                                    fnNameString = fnNameString + Bundle.getMessage("FnMap_" + fnDirVariant);
                                }
                            } catch (java.util.MissingResourceException e1) {
                                // No matches found
                            }
                        }
                    }
                    labelAt(row, fnNameCol, fnNameString, GridBagConstraints.LINE_START);
                    row++;
                }

            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Function map complete");
        }

        // label used outputs only
        for (int iOut = 0; iOut < numOut; iOut++) {
            if (outIsUsed[iOut]) {
                labelAt(outputNumRow, firstOutCol + iOut, outName[iOut]);
                labelAt(outputLabelRow, firstOutCol + iOut, outLabel[iOut]);
            }
        }

        // padding for the case of few outputs
        cs.gridwidth = GridBagConstraints.REMAINDER;
        labelAt(outputNumRow, firstOutCol + numOut, "");
    }

    void saveAt(int row, int column, JComponent j) {
        this.saveAt(row, column, j, GridBagConstraints.CENTER);
    }

    void saveAt(int row, int column, JComponent j, int anchor) {
        if (row < 0 || column < 0) {
            return;
        }
        cs = new GridBagConstraints();
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
        if (row < 0 || column < 0) {
            return;
        }
        JLabel t = new JLabel(" " + name + " ");
        saveAt(row, column, t, anchor);
    }

    /**
     * Use the "family" and "model" element from the decoder definition file to configure the
     * number of outputs and set up any that are named instead of numbered.
     */
    protected void configOutputs(Element model) {
        if (model == null) {
            log.debug("configOutputs was given a null model");
            return;
        }
        Element family = null;
        Parent parent = model.getParent();
        if (parent != null && parent instanceof Element) {
            family = (Element) parent;
        } else {
            log.debug("configOutputs found an invalid parent family");
            return;
        }
        
        // get numOuts, numFns or leave the defaults
        Attribute a = model.getAttribute("numOuts");
        try {
            if (a != null) {
                numOut = Integer.parseInt(a.getValue());
            }
        } catch (Exception e) {
            log.error("error handling decoder's numOuts value");
        }
        a = model.getAttribute("numFns");
        try {
            if (a != null) {
                numFn = Integer.parseInt(a.getValue());
            }
        } catch (Exception e) {
            log.error("error handling decoder's numFns value");
        }
        if (log.isDebugEnabled()) {
            log.debug("numFns, numOuts " + numFn + "," + numOut);
        }
        
        // take all "output" children
        List<Element> elemList = new ArrayList<>();
        addOutputElements(family.getChildren(), elemList);
        addOutputElements(model.getChildren(), elemList);
                
        log.debug("output scan starting with {} elements", elemList.size());

        for (int i = 0; i < elemList.size(); i++) {
            Element e = elemList.get(i);
            String name = e.getAttribute("name").getValue();
            log.debug("output element name: {} value: {}", e.getAttributeValue("name"), e.getAttributeValue("label"));
            // if this a number, or a character name?
            try {
                int outputNum = Integer.parseInt(name);
                // yes, since it was converted.  All we do with
                // these are store the label index (if it exists)
                String at = LocaleSelector.getAttribute(e, "label");
                if (at != null) {
                    loadSplitLabel(outputNum - 1, at);
                    numOut = Math.max(numOut, outputNum);
                }
            } catch (java.lang.NumberFormatException ex) {
                // not a number, must be a name
                if (numOut < maxOut) {
                    outName[numOut] = name;
                    String at;
                    if ((at = LocaleSelector.getAttribute(e, "label")) != null) {
                        outLabel[numOut] = at;
                    } else {
                        outLabel[numOut] = "";
                    }
                    numOut++;
                }
            }
        }
    }

    void addOutputElements(List<Element> input, List<Element> accumulate) {
      for (Element elem : input) {
        if (elem.getName().equals("outputs")) {
          log.debug(" found outputs element of size {}", elem.getChildren().size());
          addOutputElements(elem.getChildren(), accumulate);
        } else if (elem.getName().equals("output")) {
          log.debug("adding output element name: {} value: {}", elem.getAttributeValue("name"), elem.getAttributeValue("label"));
          accumulate.add(elem);
        }
      }
    }
    
    // split and load two-line labels
    void loadSplitLabel(int iOut, String theLabel) {
        if (iOut < maxOut) {
            String itemList[] = theLabel.split("\\|");
//             log.info("theLabel=\""+theLabel+"\" itemList.length=\""+itemList.length+"\"");
            if (theLabel.equals("|")) {
                outName[iOut] = "";
                outLabel[iOut] = "";
            } else if (itemList.length == 1) {
                outLabel[iOut] = itemList[0];
            } else if (itemList.length > 1) {
                outName[iOut] = itemList[0];
                outLabel[iOut] = itemList[1];
            }
        }
    }

    /**
     * clean up at end
     */
    public void dispose() {
        removeAll();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(FnMapPanel.class);
}
