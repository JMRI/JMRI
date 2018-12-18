package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintMoreOptionActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintMoreOptionAction t = new PrintMoreOptionAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintMoreOptionActionTest.class);

}
