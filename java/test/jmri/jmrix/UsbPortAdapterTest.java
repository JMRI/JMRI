package jmri.jmrix;

import java.util.Comparator;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.NamedBean;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbPortAdapterTest {

    @Test
    public void testCTor() {
        UsbPortAdapter t = new UsbPortAdapter(new DefaultSystemConnectionMemo("I", "test") {
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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
