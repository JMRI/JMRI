package jmri.plaf.macosx;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Tests for the EawtApplication class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class EawtApplicationTest  {

   @Test
   @Ignore("causes runtime exception")
   public void testCtor(){
      Assert.assertNotNull(new EawtApplication());
   }

}
