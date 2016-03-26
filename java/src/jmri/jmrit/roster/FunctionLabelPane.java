// FunctionEntryPane.java
package jmri.jmrit.roster;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.swing.EditableResizableImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display and edit the function labels in a RosterEntry
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Randall Wood Copyright (C) 2014
 */
public class FunctionLabelPane extends javax.swing.JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -4374849205711874964L;

    RosterEntry re;

    JTextField[] labels;
    JCheckBox[] lockable;
    JRadioButton[] shunterMode;
    ButtonGroup shunterModeGroup;
    EditableResizableImagePanel[] _imageFilePath;
    EditableResizableImagePanel[] _imagePressedFilePath;

    // we're doing a manual allocation of position for
    // now, based on 28 labels
    // The references to maxfunction + 1 are due to F0
    private final int maxfunction = 28;

    /**
     * This constructor allows the panel to be used in visual bean editors, but
     * should not be used in code.
     */
    public FunctionLabelPane() {
        super();
    }

    public FunctionLabelPane(RosterEntry r) {
        super();
        re = r;

        GridBagLayout gbLayout = new GridBagLayout();
        GridBagConstraints cL = new GridBagConstraints();
        setLayout(gbLayout);

        labels = new JTextField[maxfunction + 1];
        lockable = new JCheckBox[maxfunction + 1];
        shunterMode = new JRadioButton[maxfunction + 1];
        shunterModeGroup = new ButtonGroup();
        _imageFilePath = new EditableResizableImagePanel[maxfunction + 1];
        _imagePressedFilePath = new EditableResizableImagePanel[maxfunction + 1];

        cL.gridx = 0;
        cL.gridy = 0;
        cL.ipadx = 3;
        cL.anchor = GridBagConstraints.NORTHWEST;
        cL.insets = new Insets(0, 0, 0, 15);
        cL.fill = GridBagConstraints.HORIZONTAL;
        cL.weighty = 1.0;
        int nextx = 0;

        // first column
        add(new JLabel(Bundle.getMessage("FunctionButtonN")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonLabel")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonLockable")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonImageOff")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonImageOn")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonShunterFn")), cL);
        cL.gridx++;
        // second column
        add(new JLabel(Bundle.getMessage("FunctionButtonN")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonLabel")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonLockable")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonImageOff")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonImageOn")), cL);
        cL.gridx++;
        add(new JLabel(Bundle.getMessage("FunctionButtonShunterFn")), cL);
        cL.gridx++;

        cL.gridx = 0;
        cL.gridy = 1;
        for (int i = 0; i <= maxfunction; i++) {
            // label the row
            add(new JLabel("" + i), cL);
            cL.gridx++;

            // add the label
            labels[i] = new JTextField(20);
            if (r.getFunctionLabel(i) != null) {
                labels[i].setText(r.getFunctionLabel(i));
            }
            add(labels[i], cL);
            cL.gridx++;

            // add the checkbox
            lockable[i] = new JCheckBox();
            lockable[i].setSelected(r.getFunctionLockable(i));
            add(lockable[i], cL);
            cL.gridx++;

            // add the function buttons
            _imageFilePath[i] = new EditableResizableImagePanel(r.getFunctionImage(i), 20, 20);
            _imageFilePath[i].setDropFolder(LocoFile.getFileLocation());
            _imageFilePath[i].setBackground(new Color(0, 0, 0, 0));
            _imageFilePath[i].setToolTipText(Bundle.getMessage("FunctionButtonRosterImageToolTip"));
            _imageFilePath[i].setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
            add(_imageFilePath[i], cL);
            cL.gridx++;

            _imagePressedFilePath[i] = new EditableResizableImagePanel(r.getFunctionSelectedImage(i), 20, 20);
            _imagePressedFilePath[i].setDropFolder(LocoFile.getFileLocation());
            _imagePressedFilePath[i].setBackground(new Color(0, 0, 0, 0));
            _imagePressedFilePath[i].setToolTipText(Bundle.getMessage("FunctionButtonPressedRosterImageToolTip"));
            _imagePressedFilePath[i].setBorder(BorderFactory.createLineBorder(java.awt.Color.blue));
            add(_imagePressedFilePath[i], cL);
            cL.gridx++;

            shunterMode[i] = new JRadioButton();
            shunterModeGroup.add(shunterMode[i]);
            if (("F" + i).compareTo(r.getShuntingFunction()) == 0) {
                shunterMode[i].setSelected(true);
            }
            add(shunterMode[i], cL);
            cL.gridx++;

            // advance position
            cL.gridy++;
            if (cL.gridy - 1 == ((maxfunction + 1) / 2) + 1) {
                cL.gridy = 1;  // skip titles
                nextx = nextx + 6;
            }
            cL.gridx = nextx;
        }
    }

    /**
     * Do the GUI contents agree with a RosterEntry?
     *
     * @param r
     * @return true if GUI differs from RosterEntry
     */
    public boolean guiChanged(RosterEntry r) {
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] != null) {
                    if (r.getFunctionLabel(i) == null && !labels[i].getText().equals("")) {
                        return true;
                    }
                    if (r.getFunctionLabel(i) != null && !r.getFunctionLabel(i).equals(labels[i].getText())) {
                        return true;
                    }
                }
            }
        }
        if (lockable != null) {
            for (int i = 0; i < lockable.length; i++) {
                if (lockable[i] != null) {
                    if (r.getFunctionLockable(i) && !lockable[i].isSelected()) {
                        return true;
                    }
                    if (!r.getFunctionLockable(i) && lockable[i].isSelected()) {
                        return true;
                    }
                }
            }
        }
        if (_imageFilePath != null) {
            for (int i = 0; i < _imageFilePath.length; i++) {
                if (_imageFilePath[i] != null) {
                    if (r.getFunctionImage(i) == null && _imageFilePath[i].getImagePath() != null) {
                        return true;
                    }
                    if (r.getFunctionImage(i) != null && !r.getFunctionImage(i).equals(_imageFilePath[i].getImagePath())) {
                        return true;
                    }
                }
            }
        }
        if (_imagePressedFilePath != null) {
            for (int i = 0; i < _imagePressedFilePath.length; i++) {
                if (_imagePressedFilePath[i] != null) {
                    if (r.getFunctionSelectedImage(i) == null && _imagePressedFilePath[i].getImagePath() != null) {
                        return true;
                    }
                    if (r.getFunctionSelectedImage(i) != null && !r.getFunctionSelectedImage(i).equals(_imagePressedFilePath[i].getImagePath())) {
                        return true;
                    }
                }
            }
        }
        if (shunterMode != null) {
            String shunFn = "";
            for (int i = 0; i < shunterMode.length; i++) {
                if ((shunterMode[i] != null) && (shunterMode[i].isSelected())) {
                    shunFn = "F" + i;
                }
            }
            if (shunFn.compareTo(r.getShuntingFunction()) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fill a RosterEntry object from GUI contents
     *
     * @param r
     *
     */
    public void update(RosterEntry r) {
        if (labels != null) {
            String shunFn = "";
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] != null && !labels[i].getText().equals("")) {
                    r.setFunctionLabel(i, labels[i].getText());
                    r.setFunctionLockable(i, lockable[i].isSelected());
                    r.setFunctionImage(i, _imageFilePath[i].getImagePath());
                    r.setFunctionSelectedImage(i, _imagePressedFilePath[i].getImagePath());
                } else if (labels[i] != null && labels[i].getText().equals("")) {
                    if (r.getFunctionLabel(i) != null) {
                        r.setFunctionLabel(i, null);
                        r.setFunctionImage(i, null);
                        r.setFunctionSelectedImage(i, null);
                    }
                }
                if ((shunterMode[i] != null) && (shunterMode[i].isSelected())) {
                    shunFn = "F" + i;
                }
            }
            r.setShuntingFunction(shunFn);
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
    }

    public boolean includeInPrint() {
        return print;
    }

    public void includeInPrint(boolean inc) {
        print = inc;
    }
    boolean print = false;

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION")
    // Only used occasionally, so inefficient String processing not really a problem
    // though it would be good to fix it if you're working in this area
    public void printPane(HardcopyWriter w) {
        // if pane is empty, don't print anything
        //if (varList.size() == 0 && cvList.size() == 0) return;
        // future work needed her to print indexed CVs

        // Define column widths for name and value output.
        // Make col 2 slightly larger than col 1 and reduce both to allow for
        // extra spaces that will be added during concatenation
        int col1Width = w.getCharactersPerLine() / 2 - 3 - 5;
        int col2Width = w.getCharactersPerLine() / 2 - 3 + 5;

        try {
            //Create a string of spaces the width of the first column
            String spaces = "";
            for (int i = 0; i < col1Width; i++) {
                spaces = spaces + " ";
            }
            // start with pane name in bold
            String heading1 = "Function";
            String heading2 = "Description";
            String s;
            int interval = spaces.length() - heading1.length();
            w.setFontStyle(Font.BOLD);
            // write the section name and dividing line
            s = "FUNCTION LABELS";
            w.write(s, 0, s.length());
            w.writeBorders();
            //Draw horizontal dividing line for each Pane section
            w.write(w.getCurrentLineNumber(), 0, w.getCurrentLineNumber(),
                    w.getCharactersPerLine() + 1);
            s = "\n";
            w.write(s, 0, s.length());

            w.setFontStyle(Font.BOLD + Font.ITALIC);
            s = "   " + heading1 + spaces.substring(0, interval) + "   " + heading2;
            w.write(s, 0, s.length());
            w.writeBorders();
            s = "\n";
            w.write(s, 0, s.length());
            w.setFontStyle(Font.PLAIN);

            // index over variables
            for (int i = 0; i <= maxfunction; i++) {
                String name = "" + i;
                if (re.getFunctionLockable(i)) {
                    name = name + " (locked)";
                }
                String value = re.getFunctionLabel(i);
                //Skip Blank functions
                if (value != null) {

                    //define index values for name and value substrings
                    int nameLeftIndex = 0;
                    int nameRightIndex = name.length();
                    int valueLeftIndex = 0;
                    int valueRightIndex = value.length();
                    String trimmedName;
                    String trimmedValue;

                    // Check the name length to see if it is wider than the column.
                    // If so, split it and do the same checks for the Value
                    // Then concatenate the name and value (or the split versions thereof)
                    // before writing - if split, repeat until all pieces have been output
                    while ((valueLeftIndex < value.length()) || (nameLeftIndex < name.length())) {
                        // name split code
                        if (name.substring(nameLeftIndex).length() > col1Width) {
                            for (int j = 0; j < col1Width; j++) {
                                String delimiter = name.substring(nameLeftIndex + col1Width - j - 1,
                                        nameLeftIndex + col1Width - j);
                                if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                                    nameRightIndex = nameLeftIndex + col1Width - j;
                                    break;
                                }
                            }
                            trimmedName = name.substring(nameLeftIndex, nameRightIndex);
                            nameLeftIndex = nameRightIndex;
                            int space = spaces.length() - trimmedName.length();
                            s = "   " + trimmedName + spaces.substring(0, space);
                        } else {
                            trimmedName = name.substring(nameLeftIndex);
                            int space = spaces.length() - trimmedName.length();
                            s = "   " + trimmedName + spaces.substring(0, space);
                            name = "";
                            nameLeftIndex = 0;
                        }
                        // value split code
                        if (value.substring(valueLeftIndex).length() > col2Width) {
                            for (int j = 0; j < col2Width; j++) {
                                String delimiter = value.substring(valueLeftIndex + col2Width - j - 1, valueLeftIndex + col2Width - j);
                                if (delimiter.equals(" ") || delimiter.equals(";") || delimiter.equals(",")) {
                                    valueRightIndex = valueLeftIndex + col2Width - j;
                                    break;
                                }
                            }
                            trimmedValue = value.substring(valueLeftIndex, valueRightIndex);
                            valueLeftIndex = valueRightIndex;
                            s = s + "   " + trimmedValue;
                        } else {
                            trimmedValue = value.substring(valueLeftIndex);
                            s = s + "   " + trimmedValue;
                            valueLeftIndex = 0;
                            value = "";
                        }
                        w.write(s, 0, s.length());
                        w.writeBorders();
                        s = "\n";
                        w.write(s, 0, s.length());
                    }
                    // handle special cases
                }
            }
            s = "\n";
            w.writeBorders();
            w.write(s, 0, s.length());
            w.writeBorders();
            w.write(s, 0, s.length());
        } catch (IOException e) {
            log.warn("error during printing: " + e);
        }

    }

    private final static Logger log = LoggerFactory.getLogger(FunctionLabelPane.class.getName());

}
