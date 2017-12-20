package jmri.jmrit.operations.rollingstock.cars;

import java.util.Locale;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the Operations CarTypes class Last manually cross-checked on
 * 20090131
 * <p>
 * Derived from previous "OperationsCarTest" to include only the tests related
 * to CarTypes.
 *
 * @author	Bob Coleman Copyright (C) 2008, 2009
 */
public class CarTypesTest {

    private Locale defaultLocale;

    @Test
    public void testDefaultCarTypes() {
        String carTypes[]=Bundle.getMessage("carTypeNames").split(","); 
        CarTypes ct1 = InstanceManager.getDefault(CarTypes.class);
        ct1.getNames();	//Load predefined car types

        Assert.assertTrue("Predefined Car Type 1", ct1.containsName(carTypes[1]));
        Assert.assertTrue("Predefined Car Type 2", ct1.containsName(carTypes[2]));
    }
    
    @Test
    public void testAddAndDeleteCarTypes() {
        CarTypes ct1 = InstanceManager.getDefault(CarTypes.class);
        ct1.getNames();	//Load predefined car types

        ct1.addName("Type New1");
        Assert.assertTrue("Car Types Add New1", ct1.containsName("Type New1"));
        Assert.assertFalse("Car Types Never Added New2", ct1.containsName("Type New2"));
        ct1.addName("Type New3");
        Assert.assertTrue("Car Types Still Has New1", ct1.containsName("Type New1"));
        Assert.assertTrue("Car Types Add New3", ct1.containsName("Type New3"));
        ct1.replaceName("Type New3", "Type New4");
        Assert.assertFalse("Car Types replace New3", ct1.containsName("Type New3"));
        Assert.assertTrue("Car Types replace New3 with New4", ct1.containsName("Type New4"));
        String[] types = ct1.getNames();
        Assert.assertEquals("First type name", "Type New4", types[0]);
        Assert.assertEquals("2nd type name", "Type New1", types[1]);
        JComboBox<?> box = ct1.getComboBox();
        Assert.assertEquals("First comboBox type name", "Type New4", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox type name", "Type New1", box.getItemAt(1));
        ct1.deleteName("Type New4");
        Assert.assertFalse("Car Types Delete New4", ct1.containsName("Type New4"));
        ct1.deleteName("Type New1");
        Assert.assertFalse("Car Types Delete New1", ct1.containsName("Type New1"));
    }

    @Test
    @Ignore("locale set is not having the desired effect")
    public void defaultNameChangetest(){
        Locale.setDefault(Locale.US); // set the locale to US English 
                                           // for this test.
        CarTypes ct1 = InstanceManager.getDefault(CarTypes.class);
        ct1.getNames();	//Load predefined car types
        // change default names produces an error message if the
        // number of items in carTypeNames and carTypeCovert don't match
        // when the local is set to US english, this should not occur.
        ct1.changeDefaultNames(jmri.jmrit.operations.setup.Setup.AAR);
    }

    @Test
    @Ignore("locale set is not having the desired effect")
    public void defaultNameChangetestGB(){
        Locale.setDefault(Locale.UK); // set the locale to UK english 
                                           // for this test.
        CarTypes ct1 = InstanceManager.getDefault(CarTypes.class);
        ct1.getNames();	//Load predefined car types
        // change default names produces an error message if the
        // number of items in carTypeNames and carTypeCovert don't match
        // when the local is set to US english, this should not occur.
        ct1.changeDefaultNames(jmri.jmrit.operations.setup.Setup.AAR);
        jmri.util.JUnitAppender.assertErrorMessage("Properties file doesn't have equal length conversion strings, carTypeNames 10, carTypeConvert 33");
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitOperationsUtil.resetOperationsManager();
        defaultLocale = Locale.getDefault(); // save the default locale.
    }

    @After
    public void tearDown() throws Exception {
        // reset the default locale
        Locale.setDefault(defaultLocale);
        JUnitUtil.tearDown();
    }
}
