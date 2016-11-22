package jmri.util.com.rbnb;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Tests for the UDPInputStream class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class UDPInputStreamTest  {

   @Test
   public void testCtor(){
      Assert.assertNotNull(new UDPInputStream());
   }

}
