package jmri.plaf.macosx;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Jdk9Application class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Jdk9ApplicationTest  {

   @Test
   public void testCtor(){
      Assert.assertNotNull(new Jdk9Application());
   }

}
