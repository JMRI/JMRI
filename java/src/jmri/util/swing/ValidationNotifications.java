package jmri.util.swing;

import javax.swing.JPanel;

/**
 * Utilities for displaying Validation Messages.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * 
 */
public class ValidationNotifications {

    /**
     * Parse a string for binary, decimal or hex byte value.
     * Displays error message Dialog if unable to parse.
     * <p>
     * 0b, 0d or 0x prefix will force parsing of binary, decimal or hex,
     * respectively. Entries with no prefix are parsed as decimal if decimal
     * flag is true, otherwise hex.
     *
     * @param s        string to be parsed
     * @param limit    upper bound of value to be parsed
     * @param decimal  flag for decimal or hex default
     * @param comp     Parent component
     * @param errMsg   Message to be displayed if Number FormatException
     *                 encountered
     * @return the byte value, -1 indicates failure
     */
    public final static int parseBinDecHexByte(String s, int limit, boolean decimal, String errMsg,
            JPanel comp) {
        
        int radix = 16;
        if ((s.length() > 2) && s.substring(0, 2).equalsIgnoreCase("0x")) {
            // hex, remove the prefix
            s = s.substring(2);
            radix = 16;
        } else if ((s.length() > 2) && s.substring(0, 2).equalsIgnoreCase("0d")) {
            // decimal, remove the prefix
            s = s.substring(2);
            radix = 10;
        } else if ((s.length() > 2) && s.substring(0, 2).equalsIgnoreCase("0b")) {
            // binary, remove the prefix
            s = s.substring(2);
            radix = 2;
        } else if (decimal) {
            radix = 10;
        }
        String errText="";
        int data = -1;
        try {
            data = Integer.parseInt(s, radix);
            if (data < 0) {
                errText = Bundle.getMessage("ErrorConvertNegative");
            }
        } catch (NumberFormatException ex) {
            errText = Bundle.getMessage("ErrorConvertFormat",s);
        }
        if (data > limit) {
            errText = Bundle.getMessage("ErrorConvertNumberTooBig",data,limit);
        }
        if (!errText.isEmpty()) {
            JmriJOptionPane.showMessageDialog(comp, errMsg + "\n" + errText,
                errMsg, JmriJOptionPane.ERROR_MESSAGE);
            data = -1;
        }
        return data;
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationNotifications.class);
}
