package jmri.jmrit.operations;

import jmri.InstanceManager;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OperationsManagerTest extends OperationsTestCase {

    @Test
    public void testGetInstance() {
        OperationsManager t = InstanceManager.getDefault(OperationsManager.class);
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsManagerTest.class);

}
