package jmri.jmrit.logix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.junit.Rule;
import org.junit.rules.Timeout;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MergePromptTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator("Merge Prompt CTor Test");
            jdo.close();
        }).start();

        MergePrompt t = new MergePrompt("Merge Prompt CTor Test",new HashMap<String,Boolean>(),
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
