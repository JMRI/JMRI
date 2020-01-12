package jmri.jmrit.operations.routes;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RouteManagerXmlTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RouteManagerXml t = new RouteManagerXml();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(RouteManagerXmlTest.class);

}
