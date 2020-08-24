package jmri.implementation;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class VirtualSignalMastTest {

    @Test
    public void testCTor() {
        VirtualSignalMast t = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testHeldPermissiveStateStructure() {
    
        VirtualSignalMast t;
        
        // original form of Held
        t = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)");
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
        
        t.setHeld(true);
        // waitFor event with Held true
        Assert.assertTrue(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
       
        t.setHeld(false);
        // waitFor event with Held false
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
       
       
        // normal manipulation of HeldPermissive within Held
        t = new VirtualSignalMast("IF$vsm:basic:one-searchlight($2)");
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
        
        t.setHeld(true);
        // waitFor event with Held true
        Assert.assertTrue(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
 
        t.setHeldPermissive(true);
        // waitFor event with HeldPermissive true
        Assert.assertTrue(t.getHeld());
        Assert.assertTrue(t.getHeldPermissive());
      
         t.setHeldPermissive(false);
        // waitFor event with HeldPermissive false
        Assert.assertTrue(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
      
        t.setHeld(false);
        // waitFor event with Held false
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
        
        
        // direct setting of HeldPermissive
        // transition to HeldPermissive is by way of Held
        t = new VirtualSignalMast("IF$vsm:basic:one-searchlight($3)");
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
        
        t.setHeldPermissive(true);      // Held not set before this
        // waitFor event with Held true
        // waitFor event with HeldPermissive true
        Assert.assertTrue(t.getHeld());
        Assert.assertTrue(t.getHeldPermissive());
      
        t.setHeldPermissive(false);
        // waitFor event with HeldPermissive false
        Assert.assertTrue(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
      
        t.setHeld(false);
        // waitFor event with Held false
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
    
    
        // direct resetting of Held while HeldPermissive
        // leaving Held requires not in HeldPermissive
        t = new VirtualSignalMast("IF$vsm:basic:one-searchlight($4)");
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
        
        t.setHeldPermissive(true);      // Held not set before this
        // waitFor event with Held true
        // waitFor event with HeldPermissive true
        Assert.assertTrue(t.getHeld());
        Assert.assertTrue(t.getHeldPermissive());
      
         t.setHeld(false);
        // waitFor event with HeldPermissive false
        // waitFor event with Held false
        Assert.assertFalse(t.getHeld());
        Assert.assertFalse(t.getHeldPermissive());
    
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(VirtualSignalMastTest.class);

}
