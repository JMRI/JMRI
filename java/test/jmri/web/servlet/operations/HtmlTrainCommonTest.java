package jmri.web.servlet.operations;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HtmlTrainCommonTest {

    @Test
    public void testCTor() throws java.io.IOException {
        HtmlTrainCommon t = new HtmlTrainCommon(java.util.Locale.US,
                     (InstanceManager.getDefault(TrainManager.class)).getTrainById("2"));
        Assert.assertNotNull("exists",t);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initIdTagManager();
        jmri.util.JUnitOperationsUtil.setupOperationsTests();
        jmri.util.JUnitOperationsUtil.initOperationsData();     
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HtmlTrainCommonTest.class);

}
