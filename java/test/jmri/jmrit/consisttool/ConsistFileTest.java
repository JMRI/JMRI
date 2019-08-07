package jmri.jmrit.consisttool;

import java.io.File;
import jmri.Consist;
import jmri.ConsistManager;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.roster.Roster;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test simple functioning of ConsistFile
 *
 * @author	Paul Bender Copyright (C) 2015
 */
public class ConsistFileTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCtor() {
        ConsistFile file = new ConsistFile();
        Assert.assertNotNull("exists", file);
    }

    @Test
    public void testDefaultFileLocation(){
            String fileName = Roster.getDefault().getRosterLocation() +
		"roster" + File.separator + 
		"consist" + File.separator;
        Assert.assertEquals("Consist File Location",fileName,
			ConsistFile.getFileLocation());
    }

    @Test
    public void testWriteFile() throws java.io.IOException {
        ConsistFile file = new ConsistFile();
        ConsistManager cm = InstanceManager.getDefault(ConsistManager.class);
        DccLocoAddress addr = new DccLocoAddress(5,false);
        Consist c = cm.getConsist(addr);
        c.add(new DccLocoAddress(10,false),true);
        c.add(new DccLocoAddress(1000,true),false);
        String fileName = folder.newFolder().getPath() + File.separator + "consist.xml";
        file.writeFile(cm.getConsistList(),fileName);
        Assert.assertTrue("file created",(new File(fileName)).exists());
    }

    @Test
    public void testWriteDefaultFile() throws java.io.IOException {
        ConsistFile file = new ConsistFile();
        ConsistManager cm = InstanceManager.getDefault(ConsistManager.class);
        DccLocoAddress addr = new DccLocoAddress(5,false);
        Consist c = cm.getConsist(addr);
        c.add(new DccLocoAddress(10,false),true);
        c.add(new DccLocoAddress(1000,true),false);
        file.writeFile(cm.getConsistList());
        String fileName = ConsistFile.defaultConsistFilename();
        Assert.assertTrue("file created",(new File(fileName)).exists());
    }

    @Before
    public void setUp() throws java.io.IOException {
        JUnitUtil.setUp();
        jmri.profile.Profile profile = new jmri.profile.NullProfile(folder.newFolder(jmri.profile.Profile.PROFILE));
        JUnitUtil.resetProfileManager(profile );
        JUnitUtil.initRosterConfigManager();
        Roster.getDefault().setRosterLocation("");
        InstanceManager.setDefault(ConsistPreferencesManager.class,new ConsistPreferencesManager());
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }

    @After
    public void tearDown() {
       JUnitUtil.tearDown();    
    }
}
