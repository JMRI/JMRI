package jmri.jmrit.operations.rollingstock.engines.tools;

import org.junit.Assert;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class NceConsistEnginesTest extends OperationsTestCase {

    @Test
    public void testCTor(){
       // this class currently requires an NCE traffic controller to function.
       jmri.jmrix.nce.NceTrafficController tc = new jmri.jmrix.nce.NceTrafficControlScaffold();
       Assert.assertNotNull("NceConsistEngines exists",new NceConsistEngines(tc));
       
       JUnitOperationsUtil.checkNceShutDownTask();  // TODO need to fix this
       //JUnitUtil.clearShutDownManager();  // TODO for now
    }
}
