package jmri.plaf.macosx;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EawtApplicationTest  {

   @Test
   public void testCtorMacOSX(){
      Assume.assumeTrue(jmri.util.SystemType.isMacOSX());
      Assert.assertNotNull(new EawtApplication());
   }

   @Test(expected=java.lang.RuntimeException.class)
   public void testCtorNotMacOSX(){
      Assume.assumeFalse(jmri.util.SystemType.isMacOSX());
      Assert.assertNotNull(new EawtApplication());
   }

}
