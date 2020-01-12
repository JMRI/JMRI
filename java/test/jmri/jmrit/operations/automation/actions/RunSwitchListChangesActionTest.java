package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RunSwitchListChangesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RunSwitchListChangesAction t = new RunSwitchListChangesAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RunSwitchListChangesActionTest.class);

}
