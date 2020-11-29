package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintLocationsByCarTypesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintLocationsByCarTypesAction t = new PrintLocationsByCarTypesAction(true);
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(PrintLocationsByCarTypesActionTest.class);

}
