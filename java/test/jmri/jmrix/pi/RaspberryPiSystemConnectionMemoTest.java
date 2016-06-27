package jmri.jmrix.pi;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * <P>
 * Tests for RaspberryPiSystemConnectionMemo
 * </P>
 * @author Paul Bender Copyright (C) 2016
 */
public class RaspberryPiSystemConnectionMemoTest {

   @Test
   public void ConstructorTest(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       Assert.assertNotNull(m);
   }

   @Test
   public void checkProvidesSensorManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       Assert.assertTrue(m.provides(jmri.SensorManager.class));
   }

   @Test
   public void checkProvidesWhenDisabled(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.setDisabled(true);
       Assert.assertFalse(m.provides(jmri.SensorManager.class));
   }

   @Test
   public void checkProvidesTurnoutManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       Assert.assertTrue(m.provides(jmri.TurnoutManager.class));
   }

   @Test
   public void checkProvidesLightManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       Assert.assertFalse(m.provides(jmri.LightManager.class)); //false until implemented.
   }

   @Test
   public void checkProvidesOtherManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       Assert.assertFalse(m.provides(jmri.ProgrammerManager.class));
   }

   @Test
   public void setAndGetSensorManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       RaspberryPiSensorManager sm = new RaspberryPiSensorManager(m.getSystemPrefix());
       m.setSensorManager(sm);
       Assert.assertSame("Sensor Manager",sm,m.getSensorManager()); 
   }

   @Test
   public void setAndGetTurnoutManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       RaspberryPiTurnoutManager sm = new RaspberryPiTurnoutManager(m.getSystemPrefix());
       m.setTurnoutManager(sm);
       Assert.assertSame("Turnout Manager",sm,m.getTurnoutManager()); 
   }

   @Test
   public void setAndGetLightManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       //RaspberryPiLightManager sm = new RaspberryPiLightManager(m.getSystemPrefix());
       //m.setTurnoutManager(sm);
       //Assert.assertSame("Light Manager",sm,m.getLightManager()); 
       Assert.assertNull("Light Manager",m.getLightManager()); 
   }

   @Test
   public void checkConfigureManagers(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.configureManagers();
       Assert.assertNotNull("Sensor Manager after configureManagers",m.getSensorManager()); 
       Assert.assertNotNull("Turnout Manager after configureManagers",m.getTurnoutManager()); 
       Assert.assertNull("Light Manager after configureManagers",m.getLightManager()); 
   }

   @Test
   public void checkGetSensorManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.configureManagers();
       Assert.assertNotNull(m.get(jmri.SensorManager.class));
   }

   @Test
   public void checkGetSensorManagerWhenDisabled(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.configureManagers();
       m.setDisabled(true);
       Assert.assertNull(m.get(jmri.SensorManager.class));
   }

   @Test
   public void checkGetTurnoutManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.configureManagers();
       Assert.assertNotNull(m.get(jmri.TurnoutManager.class));
   }

   @Test
   public void checkGetLightManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.configureManagers();
       Assert.assertNull(m.get(jmri.LightManager.class)); // null until implemented.
   }

   @Test
   public void checkGetOtherManager(){
       RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
       m.configureManagers();
       Assert.assertNull(m.get(jmri.ProgrammerManager.class));
   }

   @Test
   public void checkDispose(){
      RaspberryPiSystemConnectionMemo m = new RaspberryPiSystemConnectionMemo();
      // verify the connection is registered
      Assert.assertNotNull(jmri.InstanceManager.getDefault(RaspberryPiSystemConnectionMemo.class)); 
      m.dispose();
      // after dispose, should be deregistered.
      Assert.assertNull(jmri.InstanceManager.getDefault(RaspberryPiSystemConnectionMemo.class)); 
   }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }
}
