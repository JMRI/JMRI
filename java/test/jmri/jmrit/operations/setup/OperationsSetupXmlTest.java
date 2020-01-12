package jmri.jmrit.operations.setup;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class OperationsSetupXmlTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        OperationsSetupXml t = new OperationsSetupXml();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(OperationsSetupXmlTest.class);

}
