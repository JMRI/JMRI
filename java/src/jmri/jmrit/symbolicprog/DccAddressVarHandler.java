package jmri.jmrit.symbolicprog;

/**
 * Encapulates DCC address handling logic in one place
 *
 * <p>
 * Expects one or more of the variables called:
 * <ul>
 * <li>primaryAddr - Short Address
 * <li>extendAddr - Long Address
 * <li>addMode - Address Format (an Enum variable to select)
 * </ul>
 * and handles the cases where:
 * <ul>
 * <li>All three are present - the normal advanced decoder case
 * <li>Short Address is present and Long Address is not
 * <li>Long Address is present and Short Address is not
 * </ul>
 * At least one of Short Address and Long Address must be present!
 *
 * @author Bob Jacobsen Copyright (C) 2013
 */
public class DccAddressVarHandler {

    public DccAddressVarHandler(VariableValue primaryAddr, VariableValue extendAddr, EnumVariableValue addMode) {
        // check if only primary, or primary selected
        if (extendAddr == null || (addMode != null && !addMode.getValueString().equals("1"))) {
            if (primaryAddr != null) {
                doPrimary();
            }
        } else {
            doExtended();
        }
    }

    /**
     * Handle case of primary address valid
     */
    protected void doPrimary() {
    }

    /**
     * Handle case of extended address valid
     */
    protected void doExtended() {
    }
}
