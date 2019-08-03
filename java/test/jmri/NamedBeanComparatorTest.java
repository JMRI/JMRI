package jmri;

import jmri.JmriException;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Test NamedBeanComparator
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class NamedBeanComparatorTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testNamedBeanComparator() {
        jmri.NamedBean a = new MyBean("IQDE:000014");
        jmri.NamedBean b = new MyBean("IQDE:00014");
        jmri.util.NamedBeanComparator<jmri.NamedBean> c = new jmri.util.NamedBeanComparator<>();
        System.out.format("Result: %s, %s, %d%n", a.getSystemName(), b.getSystemName(), c.compare(a, b));
        Assert.assertNotEquals("The named beans have different names", 0, c.compare(a, b));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }



    private class MyBean extends jmri.implementation.AbstractNamedBean {

        MyBean(String sysName) {
            super(sysName);
        }

        @Override
        public void setState(int s) throws JmriException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public int getState() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String getBeanType() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}