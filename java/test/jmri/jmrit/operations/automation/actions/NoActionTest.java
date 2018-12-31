package jmri.jmrit.operations.automation.actions;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NoActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        NoAction t = new NoAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(NoActionTest.class);

}
