package jmri.jmrit.operations;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsManagerTest extends OperationsTestCase {

    @Test
    public void testGetInstance() {
        OperationsManager t = InstanceManager.getDefault(OperationsManager.class);
        Assert.assertNotNull("exists", t);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsManagerTest.class);

}
