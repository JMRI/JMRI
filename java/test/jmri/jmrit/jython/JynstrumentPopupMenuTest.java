package jmri.jmrit.jython;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class JynstrumentPopupMenuTest {

    @Test
    public void testCTor() {
        Jynstrument j = new Jynstrument(){
           @Override
           public String getExpectedContextClassName(){
              return "Test Jynstrument";
           }

           @Override
           public void init(){
           }

           @Override
           protected void quit(){
           }
        };
        JynstrumentPopupMenu t = new JynstrumentPopupMenu(j);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JynstrumentPopupMenuTest.class);

}
