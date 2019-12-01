package jmri.jmrix.powerline.cp290;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for SpecificReply class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class SpecificReplyTest extends jmri.jmrix.AbstractMessageTestBase {

   private SpecificTrafficController tc = null;

   @Before
   @Override
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tc = new SpecificTrafficController(new SpecificSystemConnectionMemo());
        m = new SpecificReply(tc);

   }

   @After
   public void tearDown(){
        tc = null;
	m = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

   }

}
