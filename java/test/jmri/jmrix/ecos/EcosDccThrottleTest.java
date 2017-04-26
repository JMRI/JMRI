package jmri.jmrix.ecos;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EcosDccThrottleTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc){
           @Override
           public EcosLocoAddressManager getLocoAddressManager(){
              return new EcosLocoAddressManager(this);
           }

           @Override
           public EcosPreferences getPreferenceManager(){ 
              return new EcosPreferences(this){
                  @Override
                  public boolean getPreferencesLoaded(){
                     return true;
                  }
              };
           }
        };
        EcosDccThrottle t = new EcosDccThrottle(new jmri.DccLocoAddress(100,true),memo,true);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHeadless() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc){
           @Override
           public EcosLocoAddressManager getLocoAddressManager(){
              return new EcosLocoAddressManager(this);
           }

           @Override
           public EcosPreferences getPreferenceManager(){ 
              return new EcosPreferences(this){
                  @Override
                  public boolean getPreferencesLoaded(){
                     return true;
                  }
                  // don't ask any questions related to locos.
                  @Override
                  public int getAddLocoToEcos(){
                     return EcosPreferences.NO;
                  }
                  @Override
                  public int getAddLocoToJMRI(){
                     return EcosPreferences.NO;
                  }
                  @Override
                  public int getAdhocLocoFromEcos(){
                     return EcosPreferences.NO;
                  }
                  @Override
                  public int getForceControlFromEcos(){
                     return EcosPreferences.NO;
                  }
                  @Override
                  public int getRemoveLocoFromEcos(){
                     return EcosPreferences.NO;
                  }
                  @Override
                  public int getRemoveLocoFromJMRI(){
                     return EcosPreferences.NO;
                  }
              };
           }
        };
        EcosDccThrottle t = new EcosDccThrottle(new jmri.DccLocoAddress(100,true),memo,true);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EcosDccThrottleTest.class.getName());

}
