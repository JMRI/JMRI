package jmri.jmrix;

import jmri.util.JUnitUtil;
import jmri.DccLocoAddress;
import jmri.NamedBean;

import java.util.Comparator;

import org.junit.After;
import org.junit.Before;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class AbstractThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    AbstractThrottleManager t = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tm = t = new AbstractThrottleManager(new SystemConnectionMemo("T", "Test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
                return null;
            }
        }) {
            @Override
            public void requestThrottleSetup(jmri.LocoAddress a, boolean control) {
                notifyThrottleKnown(new jmri.jmrix.debugthrottle.DebugThrottle((DccLocoAddress) a, adapterMemo), a);
            }

            @Override
            public boolean addressTypeUnique() {
                return false;
            }

            @Override
            public boolean canBeShortAddress(int address) {
                return true;
            }

            @Override
            public boolean canBeLongAddress(int address) {
                return true;
            }
        };
    }

    @After
    public void tearDown() {
        tm = t = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(AbstractThrottleManagerTest.class);

}
