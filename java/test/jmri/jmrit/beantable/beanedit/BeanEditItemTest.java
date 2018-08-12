package jmri.jmrit.beantable.beanedit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BeanEditItemTest {

    @Test
    public void testCTor() {
        BeanItemPanel p = new BeanItemPanel();
        BeanEditItem t = new BeanEditItem(p,"IS1","");
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(BeanEditItemTest.class);

}
