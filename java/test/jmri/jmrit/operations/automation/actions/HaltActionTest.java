package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class HaltActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        HaltAction t = new HaltAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(HaltActionTest.class);

}
