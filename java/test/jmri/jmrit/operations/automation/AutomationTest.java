package jmri.jmrit.operations.automation;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AutomationTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Automation t = new Automation("1","Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(AutomationTest.class);

}
