package jmri.jmrit.logix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import org.netbeans.jemmy.operators.JDialogOperator;


/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MergePromptTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator("test");
            jdo.close();
        }).start();

        MergePrompt t = new MergePrompt("test",new HashMap<String,Boolean>(),
                        new HashMap<String, HashMap<Integer,Boolean>>());
        Assert.assertNotNull("exists",t);
        t.dispose();
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

    private final static Logger log = LoggerFactory.getLogger(MergePromptTest.class.getName());

}
