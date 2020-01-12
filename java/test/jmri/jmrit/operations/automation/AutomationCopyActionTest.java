package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AutomationCopyActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        AutomationCopyAction t = new AutomationCopyAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(AutomationCopyActionTest.class);

}
