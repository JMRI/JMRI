package jmri.jmrit.operations.rollingstock;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class XmlTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Xml t = new Xml();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(XmlTest.class);

}
