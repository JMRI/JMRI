package jmri.jmrix.cmri.serial;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;

import jmri.*;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.managers.AbstractTurnoutManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for CMRI serial systems.
 * <p>
 * System names are "CTnnn", where C is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class SerialTurnoutManager extends AbstractTurnoutManager {

    public SerialTurnoutManager(CMRISystemConnectionMemo memo) {
       super(memo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CMRISystemConnectionMemo getMemo() {
        return (CMRISystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        // validate the system name, and normalize it
        String sName = getMemo().normalizeSystemName(systemName);
        if (sName.isEmpty()) {
            // system name is not valid
            throw new IllegalArgumentException("Cannot create System Name from " + systemName);
        }
        // does this turnout already exist
        Turnout t = getBySystemName(sName);
        if (t != null) {
            return t;
        }
        // check under alternate name
        String altName = getMemo().convertSystemNameToAlternate(sName);
        t = getBySystemName(altName);
        if (t != null) {
            return t;
        }

        // check if the addressed output bit is available
        int nAddress = getMemo().getNodeAddressFromSystemName(sName);
        if (nAddress == -1) {
            throw new IllegalArgumentException("Cannot get Node Address from System Name " + systemName + " " + sName);
        }
        int bitNum = getMemo().getBitFromSystemName(sName);
        if (bitNum == 0) {
            throw new IllegalArgumentException("Cannot get Bit from System Name " + systemName + " " + sName);
        }
        String conflict = getMemo().isOutputBitFree(nAddress, bitNum);
        if ((!conflict.isEmpty()) && (!conflict.equals(sName))) {
            log.error("{} assignment conflict with {}.", sName, conflict);
            throw new IllegalArgumentException(Bundle.getMessage("ErrorAssignDialog", bitNum, conflict));
        }

        // create the turnout
        t = new SerialTurnout(sName, userName, getMemo());

        // does system name correspond to configured hardware
        if (!getMemo().validSystemNameConfig(sName, 'T', getMemo().getTrafficController())) {
            // system name does not correspond to configured hardware
            log.warn("Turnout '{}' refers to an undefined Serial Node.", sName);
        }
        return t;
    }

    /**
     * Public method to notify user of Turnout creation error.
     * @param conflict human readable name of turnout with conflict.
     * @param bitNum conflict bit number.
     * @deprecated  since 4.23.4;
     */
    @Deprecated
    public void notifyTurnoutCreationError(String conflict, int bitNum) {
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorAssignDialog", bitNum, conflict) + "\n" +
                Bundle.getMessage("ErrorAssignLine2T"),
                Bundle.getMessage("ErrorAssignTitle"),
                JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * Get from the user, the number of addressed bits used to control a
     * turnout.
     * <p>
     * Normally this is 1, and the default routine returns 1
     * automatically. Turnout Managers for systems that can handle multiple
     * control bits should override this method with one which asks the user to
     * specify the number of control bits. If the user specifies more than one
     * control bit, this method should check if the additional bits are
     * available (not assigned to another object). If the bits are not
     * available, this method should return 0 for number of control bits, after
     * informing the user of the problem. This function is called whenever a new
     * turnout is defined in the Turnout table. It can also be used to set up
     * other turnout control options, such as pulsed control of turnout
     * machines.
     */
    @Override
    public int askNumControlBits(@Nonnull String systemName) {

        // ask user how many bits should control the turnout - 1 or 2
        int iNum = selectNumberOfControlBits();
        if (iNum == JOptionPane.CLOSED_OPTION) {
            /* user cancelled without selecting an option */
            iNum = 1;
            log.warn("User cancelled without selecting number of output bits. Defaulting to 1.");
        } else {
            iNum = iNum + 1;
        }

        if (iNum == 2) {
            // check if the second output bit is available
            int nAddress = getMemo().getNodeAddressFromSystemName(systemName);
            if (nAddress == -1) {
                return 0;
            }
            int bitNum = getMemo().getBitFromSystemName(systemName);
            if (bitNum == 0) {
                return 0;
            }
            bitNum = bitNum + 1;
            String conflict = getMemo().isOutputBitFree(nAddress, bitNum);
            if (!conflict.equals("")) {
                log.error("Assignment conflict with {}. Turnout not created.", conflict);
                notifySecondBitConflict(conflict, bitNum);
                return 0;
            }
        }

        return (iNum);
    }

    /**
     * Get from the user, the type of output to be used bits to control a
     * turnout.
     * <p>
     * Normally this is 0 for 'steady state' control, and the default
     * routine returns 0 automatically. Turnout Managers for systems that can
     * handle pulsed control as well as steady state control should override
     * this method with one which asks the user to specify the type of control
     * to be used. The routine should return 0 for 'steady state' control, or n
     * for 'pulsed' control, where n specifies the duration of the pulse
     * (normally in seconds).
     */
    @Override
    public int askControlType(@Nonnull String systemName) {
        // ask if user wants 'steady state' output (stall motors, e.g., Tortoises) or 
        // 'pulsed' output (some turnout controllers).
        int iType = selectOutputType();
        if (iType == JOptionPane.CLOSED_OPTION) {
            /* user cancelled without selecting an output type */
            iType = 0;
            log.warn("User cancelled without selecting output type. Defaulting to 'steady state'.");
        }
        // Note: If the user selects 'pulsed', this routine defaults to 1 second.
        return (iType);
    }

    /**
     * Public method to allow user to specify one or two output bits for turnout
     * control.
     *
     * @return 1 or 2 if the user selected, or 0 if the user cancelled without selecting.
     */
    public int selectNumberOfControlBits() {
        return JOptionPane.showOptionDialog(null,
                Bundle.getMessage("QuestionBitsDialog"),
                Bundle.getMessage("CmriTurnoutTitle"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, new String[]{Bundle.getMessage("BitOption1"), Bundle.getMessage("BitOption2")}, Bundle.getMessage("BitOption1"));
    }

    /**
     * Public method to allow user to specify pulsed or steady state for two
     * output bits for turnout control.
     *
     * @return 1 for steady state or 2 for pulsed if the user selected,
     * or 0 if the user cancelled without selecting.
     */
    public int selectOutputType() {
        return JOptionPane.showOptionDialog(null,
                Bundle.getMessage("QuestionPulsedDialog"),
                Bundle.getMessage("CmriBitsTitle"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, new String[]{Bundle.getMessage("PulsedOptionSteady"), Bundle.getMessage("PulsedOptionPulsed")},
                Bundle.getMessage("PulsedOptionSteady"));
    }

    /**
     * Public method to notify user when the second bit of a proposed two output
     * bit turnout has a conflict with another assigned bit.
     * @param conflict human readable name of turnout with conflict.
     * @param bitNum conflict bit number.
     */
    public void notifySecondBitConflict(String conflict, int bitNum) {
        JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorAssign2Dialog", bitNum, conflict) + "\n" +
                Bundle.getMessage("ErrorAssignLine2X", Bundle.getMessage("BeanNameTurnout")),
                Bundle.getMessage("ErrorAssignTitle"),
                JOptionPane.INFORMATION_MESSAGE, null);
    }

    /**
     * Turnout format is more than a simple format.
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isNumControlBitsSupported(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isControlTypeSupported(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        int seperator = 0;
        String tmpSName;

        if (curAddress.contains(":")) {
            // Address format passed is in the form node:address
            seperator = curAddress.indexOf(":");
            nAddress = Integer.parseInt(curAddress.substring(0, seperator));
            // check for non-numerical chars
            try {
                bitNum = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                throw new JmriException("Part 2 of " + curAddress + " is not an integer");
            }
            tmpSName = getMemo().makeSystemName("T", nAddress, bitNum);
        } else if (curAddress.contains("B") || (curAddress.contains("b"))) {
            curAddress = curAddress.toUpperCase();
            try {
                //We do this to simply check that we have numbers in the correct places ish
                Integer.parseInt(curAddress.substring(0, 1));
                int b = (curAddress.toUpperCase()).indexOf("B") + 1;
                Integer.parseInt(curAddress.substring(b));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert " + curAddress + " to a valid Hardware Address");
            }
            tmpSName = prefix + typeLetter() + curAddress;
            bitNum = getMemo().getBitFromSystemName(tmpSName);
            nAddress = getMemo().getNodeAddressFromSystemName(tmpSName);
        } else {
            try {
                // We do this to simply check that the value passed is a number!
                Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                throw new JmriException("Address " + curAddress + " is not an integer");
            }
            tmpSName = prefix + "T" + curAddress;
            bitNum = getMemo().getBitFromSystemName(tmpSName);
            nAddress = getMemo().getNodeAddressFromSystemName(tmpSName);
        }
        return (tmpSName);
    }

    private int bitNum = 0;
    private int nAddress = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {
        String tmpSName = "";
        try {
            tmpSName = validateSystemNameFormat(createSystemName(curAddress, prefix));
        } catch (JmriException | NamedBean.BadNameException ex) {
            throw new JmriException(ex.getMessage());
        }

        //If the hardware address part does not already exist then this can
        //be considered the next valid address.
        Turnout t = getBySystemName(tmpSName);
        if (t == null && !ignoreInitialExisting) {
            /* We look for the last instance of T, as the hardware address side
             of the system name should not contain the letter, however parts of the prefix might */
            int seperator = tmpSName.lastIndexOf("T") + 1;
            return tmpSName.substring(seperator);
        }

        //The Number of Output Bits of the previous turnout will help determine the next
        //valid address.
        int increment = t==null ? 1 : t.getNumberOutputBits();
        for (int x = 0; x < 10; x++) {
            bitNum = bitNum + increment;
            //System.out.println("This should increment " + bitNum);
            tmpSName = getMemo().makeSystemName("T", nAddress, bitNum);
            t = getBySystemName(tmpSName);
            if (t == null) {
                int seperator = tmpSName.lastIndexOf("T") + 1;
                return tmpSName.substring(seperator);
            }
        }
        throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,tmpSName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) throws NamedBean.BadSystemNameException {
        systemName = validateSystemNamePrefix(systemName, locale);
        return getMemo().validateSystemNameFormat(super.validateSystemNameFormat(systemName, locale), typeLetter(), locale);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return getMemo().validSystemNameFormat(systemName, typeLetter());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManager.class);

}
