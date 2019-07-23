package jmri.jmrix.anyma;

import jmri.Light;
import jmri.managers.AbstractLightMgrTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for UsbLightManager class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class UsbLightManagerTest extends AbstractLightMgrTestBase {

    private AnymaDMX_SystemConnectionMemo _memo = null;

    @Test
    public void ConstructorTest() {
        Assert.assertNotNull("ConnectionConfig constructor", l);
    }

    @Override
    public String getSystemName(int i) {
        return "DL" + i;
    }

    @Test
    public void testAsAbstractFactory() {
        String systemName = "DL21";
        String userName = "My Name";
        Light tl = l.newLight(systemName, userName);

        if (log.isDebugEnabled()) {
            log.debug("new light value: " + tl);
        }
        Assert.assertNotNull(tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName(systemName));
        }
        Assert.assertNotNull(l.getBySystemName(systemName));

        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName(userName));
        }
        Assert.assertNotNull(l.getByUserName(userName));
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();

        _memo = new AnymaDMX_SystemConnectionMemo();
        l = _memo.getLightManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private final static Logger log
            = LoggerFactory.getLogger(UsbLightManagerTest.class);
}
