package apps.gui3.dp3;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DecoderPro3ActionTest {

    @Test
    public void testCTor() {
        DecoderPro3Action t = new DecoderPro3Action("test",true);
        Assert.assertNotNull("exists",t);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMakePanel(){
        DecoderPro3Action t = new DecoderPro3Action("test",true);
        t.makePanel();  
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

    // private final static Logger log = LoggerFactory.getLogger(DecoderPro3ActionTest.class);

}
