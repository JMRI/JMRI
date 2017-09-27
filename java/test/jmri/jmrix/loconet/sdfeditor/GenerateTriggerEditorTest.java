package jmri.jmrix.loconet.sdfeditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class GenerateTriggerEditorTest {

    @Test
    public void testCTor() {
        GenerateTriggerEditor t = new GenerateTriggerEditor(new jmri.jmrix.loconet.sdf.GenerateTrigger(1));
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

    // private final static Logger log = LoggerFactory.getLogger(GenerateTriggerEditorTest.class);

}
