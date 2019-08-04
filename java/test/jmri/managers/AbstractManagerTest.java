package jmri.managers;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Test NamedBeanComparator
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class AbstractManagerTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testRegister() {
        MyBean a1 = new MyBean("IT1");
        MyBean b1 = new MyBean("IT01");
        MyBean a2 = new MyBean("IT02");
        MyBean b2 = new MyBean("IT2");
        
        MyManager m = new MyManager();
        
        boolean exceptionThrown = false;
        m.register(a1);
        try {
            m.register(b1);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("exception thrown", exceptionThrown);
        JUnitAppender.assertErrorMessage("systemName is already registered. Current system name: IT1. New system name: IT01");
        
        exceptionThrown = false;
        m.register(a2);
        try {
            m.register(b2);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("exception thrown", exceptionThrown);
        JUnitAppender.assertErrorMessage("systemName is already registered. Current system name: IT02. New system name: IT2");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }


    private class MyManager extends AbstractManager<MyBean> {

        MyManager() {
            super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }

        @Override
        public int getXMLOrder() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getBeanTypeHandled(boolean plural) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public char typeLetter() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
    private class MyBean extends jmri.implementation.AbstractNamedBean {

        MyBean(String sysName) {
            super(sysName);
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

}