package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintMoreOptionPanelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintMoreOptionPanel t = new PrintMoreOptionPanel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintMoreOptionPanelTest.class);

}
