package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AutomationsTableFrameActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        AutomationsTableFrameAction t = new AutomationsTableFrameAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(AutomationsTableFrameActionTest.class);

}
