package jmri.jmrit.operations.setup;

import org.junit.Assert;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsSetupPanelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsSetupPanel t = new OperationsSetupPanel();
        Assert.assertNotNull("exists",t);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsSetupPanelTest.class);

}
