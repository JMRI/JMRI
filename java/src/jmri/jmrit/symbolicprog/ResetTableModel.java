package jmri.jmrit.symbolicprog;

import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import jmri.Programmer;

/**
 * Holds a table of the available factory resets available for a particular
 * decoder.
 *
 * @author Howard G. Penny Copyright (C) 2005
 */
public class ResetTableModel extends ExtraMenuTableModel {

    public ResetTableModel(JLabel status, Programmer pProgrammer) {
        super(status, pProgrammer);
        name = Bundle.getMessage("MenuReset");
    }

    /** {@inheritDoc} */
    @Override
    public String getTopLevelElementName() {
        return "resets";
    }

    /** {@inheritDoc} */
    @Override
    public String getIndividualElementName() {
        return "factReset";
    }

    /**
     * Can provide some mechanism to prompt for user for one last chance to
     * change his/her mind
     * @param currentMode current programming mode
     * @param resetModes representation of reset modes available
     * @param availableModes representation of available modes
     * @return true if user says to continue
     */
    @Override
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
    @Override
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


}
