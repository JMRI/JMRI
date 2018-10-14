package jmri.util.com.rbnb;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the UDPOutputStream class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class UDPOutputStreamTest  {

   @Test
   public void testCtor(){
      Assert.assertNotNull(new UDPOutputStream());
   }

}
