package jmri.jmrit.operations.rollingstock.engines.tools;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrix.nce.NceTrafficControlScaffold;
import jmri.jmrix.nce.NceTrafficController;

/**
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class NceConsistEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        // this class currently requires an NCE traffic controller to function.
        NceTrafficController tc = new NceTrafficControlScaffold();
        Assert.assertNotNull("NceConsistEngines exists", new NceConsistEngines(tc));
        // clean up behind the traffic controller
        tc.terminateThreads();
    }
}
