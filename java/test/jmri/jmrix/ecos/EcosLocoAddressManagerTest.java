package jmri.jmrix.ecos;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EcosLocoAddressManagerTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo(tc){
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
        EcosLocoAddressManager t = new EcosLocoAddressManager(memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTorHeadLess() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new jmri.jmrix.ecos.EcosSystemConnectionMemo(tc){
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
        EcosLocoAddressManager t = new EcosLocoAddressManager(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosLocoAddressManagerTest.class);

}
