package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a table of the available factory resets available for a particular
 * decoder.
 *
 * @author Howard G. Penny Copyright (C) 2005
 */
public class ResetTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {

    private String headers[] = {"Label", "Name",
        "Value",
        "Write", "State"};

    private List<CvValue> rowVector = new ArrayList<>(); // vector of Reset items
    private List<String> labelVector = new ArrayList<>(); // vector of related labels
    private List<List<String>> modeVector = new ArrayList<>(); // vector of related modes

    private List<JButton> _writeButtons = new ArrayList<>();

    private JLabel _status = null;
    private Programmer mProgrammer;

    public ResetTableModel(JLabel status, Programmer pProgrammer) {
        super();

        mProgrammer = pProgrammer;
        // save a place for notification
        _status = status;
    }

    public void setProgrammer(Programmer p) {
        mProgrammer = p;

        // pass on to all contained CVs
        rowVector.forEach((cv) -> {
            cv.setProgrammer(p);
        });
    }

    private boolean hasOpsModeFlag = false;

    protected void flagIfOpsMode(String mode) {
        if (mode.startsWith("OPS")) {
            hasOpsModeFlag = true;
        }
    }

    public boolean hasOpsModeReset() {
        return hasOpsModeFlag;
    }

    @Override
    public int getRowCount() {
        return rowVector.size();
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // log.debug("getValueAt "+row+" "+col);
        // some error checking
        if (row >= rowVector.size()) {
            log.debug("row greater than row vector");
            return "Error";
        }
        CvValue cv = rowVector.get(row);
        if (cv == null) {
            log.debug("cv is null!");
            return "Error CV";
        }
        switch (headers[col]) {
            case "Label":
                return "" + labelVector.get(row);
            case "Name":
                return "" + cv.cvName();
            case "Value":
                return "" + cv.getValue();
            case "Write":
                return _writeButtons.get(row);
            case "State":
                int state = cv.getState();
                switch (state) {
                    case CvValue.UNKNOWN:
                        return "Unknown";
                    case CvValue.READ:
                        return "Read";
                    case CvValue.EDITED:
                        return "Edited";
                    case CvValue.STORED:
                        return "Stored";
                    case CvValue.FROMFILE:
                        return "From file";
                    default:
                        return "inconsistent";
                }
            default:
                return "hmmm ... missed it";
        }
    }

    public void setRow(int row, Element e, Element p, String model) {
        decoderModel = model; // Save for use elsewhere
        String label = LocaleSelector.getAttribute(e, "label"); // Note the name variable is actually the label attribute
        log.debug("Starting to setRow \"{}\"", label);
        String cv = e.getAttribute("CV").getValue();
        int cvVal = Integer.parseInt(e.getAttribute("default").getValue());

        log.debug("            CV \"{}\" value {}", cv, cvVal);

        CvValue resetCV = new CvValue(cv, mProgrammer);
        resetCV.addPropertyChangeListener(this);
        resetCV.setValue(cvVal);
        resetCV.setWriteOnly(true);
        resetCV.setState(VariableValue.STORED);
        rowVector.add(resetCV);
        labelVector.add(label);
        modeVector.add(getResetModeList(e, p));
    }

    protected List<String> getResetModeList(Element e, Element p) {
        List<Element> elementList = new ArrayList<>();
        List<String> modeList = new ArrayList<>();
        List<Element> elementModes;
        String mode;
        boolean resetsModeFound = false;

        elementList.add(p);
        elementList.add(e);

        for (Element ep : elementList) {
            try {
                mode = ep.getAttribute("mode").getValue();
                if (ep.getName().equals("resets")) {
                    resetsModeFound = true;
                } else if (resetsModeFound) {
                    modeList.clear();
                    resetsModeFound = false;
                }
                modeList.add(mode);
                flagIfOpsMode(mode);
            } catch (NullPointerException ex) {
                // ignore as expected result if there is no attribute mode
            }

            try {
                elementModes = ep.getChildren("mode");
                for (Element s : elementModes) {
                    if (ep.getName().equals("resets")) {
                        resetsModeFound = true;
                    } else if (resetsModeFound) {
                        modeList.clear();
                        resetsModeFound = false;
                    }
                    modeList.add(s.getText());
                    flagIfOpsMode(s.getText());
                }
            } catch (NullPointerException ex) {
                // ignore as expected result if there is no attribute mode
            }
        }

        return modeList;
    }

    private ProgrammingMode savedMode;
    private String decoderModel;

    protected void performReset(int row) {
        savedMode = mProgrammer.getMode(); // In case we need to change modes
        if (modeVector.get(row) != null) {
            List<ProgrammingMode> modes = mProgrammer.getSupportedModes();
            List<String> validModes = modeVector.get(row);

            StringBuilder programmerModeListBuffer = new StringBuilder("");
            modes.forEach((m) -> {
                programmerModeListBuffer.append(",").append(m.toString());
            });
            String programmerModeList = programmerModeListBuffer.toString();
            if (programmerModeList.length() <= 1) {
                programmerModeList = ""; // NOI18N
            } else if (programmerModeList.startsWith(",")) {
                programmerModeList = programmerModeList.substring(1);
            }

            StringBuilder resetModeBuilder = new StringBuilder("");
            validModes.forEach((mode) -> {
                resetModeBuilder.append(",").append(new ProgrammingMode(mode).toString());
            });
            String resetModeList = resetModeBuilder.toString();
            if (resetModeList.length() <= 1) {
                resetModeList = ""; // NOI18N
            } else if (resetModeList.startsWith(",")) {
                resetModeList = resetModeList.substring(1);
            }

            if (resetModeList.length() > 0) {
                boolean modeFound = false;
                search:
                for (ProgrammingMode m : modes) {
                    for (String mode : validModes) {
                        if (mode.equals(m.getStandardName())) {
                            mProgrammer.setMode(m);
                            modeFound = true;
                            break search;
                        }
                    }
                }

                if (mProgrammer.getMode().getStandardName().startsWith("OPS")) {
                    if (!opsResetOk()) {
                        return;
                    }
                }

                if (!modeFound) {
                    if (!badModeOk((savedMode.toString()), resetModeList, programmerModeList)) {
                        return;
                    }
                    log.warn("{} for {} was attempted in {} mode.", labelVector.get(row), decoderModel, savedMode);
                    log.warn("Recommended mode(s) were \"{}\" but available modes were \"{}\"", resetModeList, programmerModeList);
                }
            }
        }
        CvValue cv = rowVector.get(row);
        log.debug("performReset: {}", cv);
        _progState = WRITING_CV;
        cv.write(_status);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("action command: {}", e.getActionCommand());
        char b = e.getActionCommand().charAt(0);
        int row = Integer.parseInt(e.getActionCommand().substring(1));
        log.debug("event on {} row {}", b, row);
        if (b == 'W') {
            // write command
            performReset(row);
        }
    }

    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int WRITING_CV = 3;

    @Override
    public void propertyChange(PropertyChangeEvent e) {

        log.debug("Property changed: {}", e.getPropertyName());
        // notification from Indexed CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
                case IDLE:  // no, just an Indexed CV update
                    log.debug("Busy goes false with state IDLE");
                    return;
                case WRITING_CV:  // now done with the write request
                    log.debug("Finished writing the CV");
                    mProgrammer.setMode(savedMode);
                    _progState = IDLE;
                    return;
                default:  // unexpected!
                    log.error("Unexpected state found: {}", _progState);
                    mProgrammer.setMode(savedMode);
                    _progState = IDLE;
            }
        }
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean badModeOk(String currentMode, String resetModes, String availableModes) {
        String resetWarning
                = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn1")
                + "\n\n"
                + java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn2"), resetModes)
                + "\n"
                + java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn3"), availableModes)
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn4")
                + "\n\n"
                + java.text.MessageFormat.format(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetModeWarn5"), currentMode);
        return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(null,
                        resetWarning,
                        ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     *
     * @return true if user says to continue
     */
    boolean opsResetOk() {
        String resetWarning
                = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn1")
                + "\n\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn2")
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn3")
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn4")
                + "\n\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn5")
                + "\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn6")
                + "\n\n"
                + ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsWarn7");
        return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(null,
                        resetWarning,
                        ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("FactoryResetOpsTitle"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
    }

    public void dispose() {
        log.debug("dispose");

        // remove buttons
        for (int i = 0; i < _writeButtons.size(); i++) {
            _writeButtons.get(i).removeActionListener(this);
        }

        _writeButtons.clear();
        _writeButtons = null;

        // remove variables listeners
        for (int i = 0; i < rowVector.size(); i++) {
            CvValue cv = rowVector.get(i);
            cv.dispose();
        }
        rowVector.clear();
        rowVector = null;

        labelVector.clear();
        labelVector = null;

        modeVector.clear();
        modeVector = null;

        headers = null;

        _status = null;
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ResetTableModel.class);
}
