package jmri.jmrix;

import java.util.Comparator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.NamedBean;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbPortAdapterTest {

    @Test
    public void testCTor() {
        UsbPortAdapter t = new UsbPortAdapter(new SystemConnectionMemo("I", "test") {
            @Override
            protected java.util.ResourceBundle getActionModelResourceBundle() {
                return null;
            }

            @Override
            public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
                return null;
            }
        });
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
