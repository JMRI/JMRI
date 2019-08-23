package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificMessage class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificMessageTest extends jmri.jmrix.AbstractMessageTestBase {

   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        m = new SpecificMessage(5);
   }

   @After
   public void tearDown(){
	m = null;
        JUnitUtil.tearDown();
   }

}
