package jmri.jmrix.loconet.messageinterp;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.messageinterp.Bundle class.
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
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author B. Milhaupt Copyright (C) 2018
 *
 */
public class BundleTest {
    @Test public void testGoodKeyMessage() {
        Assert.assertEquals("Set Global (Track) Power to 'Force Idle, Broadcast Emergency STOP'.\n", Bundle.getMessage("LN_MSG_IDLE"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessage() {
            Bundle.getMessage("FFFFFTTTTTTT");
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("0x255", Bundle.getMessage("LN_MSG_HEXADECIMAL_REPRESENTATION", 255));
        Assert.assertEquals("Request status of switch SystemName (UserName).\n", Bundle.getMessage("LN_MSG_SW_STATE", "SystemName", "UserName"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessageArg() {
            Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});
    }

    @Test public void testLocaleMessage() {
        Assert.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }
    
}
