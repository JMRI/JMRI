package jmri.jmrix.ecos;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosLocoAddressManagerTest {

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCTor() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc){
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

        t.dispose();
        tc.terminateThreads();
    }

    @Test
    public void testCTorHeadLess() {
        EcosTrafficController tc = new EcosInterfaceScaffold();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo(tc){
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
        t.terminateThreads();
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initRosterConfigManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosLocoAddressManagerTest.class);

}
