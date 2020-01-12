package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EngineManagerXmlTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        EngineManagerXml t = new EngineManagerXml();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(EngineManagerXmlTest.class);

}
