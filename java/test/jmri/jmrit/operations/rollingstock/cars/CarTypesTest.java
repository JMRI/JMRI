package jmri.jmrit.operations.rollingstock.cars;

import java.util.Locale;
import javax.swing.JComboBox;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Operations CarTypes class Last manually cross-checked on
 * 20090131
 * <p>
 * Derived from previous "OperationsCarTest" to include only the tests related
 * to CarTypes.
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class CarTypesTest extends OperationsTestCase {

    private Locale defaultLocale;

    @Test
    public void testDefaultCarTypes() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.getNames(); //Load predefined car types

        Assert.assertTrue("Predefined Car Type 1", ct.containsName(carTypes[1]));
        Assert.assertTrue("Predefined Car Type 2", ct.containsName(carTypes[2]));
    }

    @Test
    public void testAARCarTypes() {
        Setup.setCarTypes(Setup.AAR);
        String carTypes[] = Bundle.getMessage("carTypeARR").split(",");
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.getNames(); //Load predefined car types

        Assert.assertTrue("Predefined Car Type 1", ct.containsName(carTypes[1]));
        Assert.assertTrue("Predefined Car Type 2", ct.containsName(carTypes[2]));
    }

    @Test
    public void testDescriptiveToAARCarTypes() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        String carTypesAAR[] = Bundle.getMessage("carTypeARR").split(",");
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.getNames(); //Load predefined car types

        Assert.assertTrue("Predefined Car Type 1", ct.containsName(carTypes[1]));
        Assert.assertTrue("Predefined Car Type 2", ct.containsName(carTypes[2]));
        
        Assert.assertFalse("Predefined Car Type 1", ct.containsName(carTypesAAR[1]));
        Assert.assertFalse("Predefined Car Type 2", ct.containsName(carTypesAAR[2]));

        ct.changeDefaultNames(Setup.AAR);

        Assert.assertFalse("Predefined Car Type 1", ct.containsName(carTypes[1]));
        Assert.assertFalse("Predefined Car Type 2", ct.containsName(carTypes[2]));
        
        Assert.assertTrue("Predefined Car Type 1", ct.containsName(carTypesAAR[1]));
        Assert.assertTrue("Predefined Car Type 2", ct.containsName(carTypesAAR[2]));
    }
    
    @Test
    public void testAARCarTypesToDescriptive() {
        Setup.setCarTypes(Setup.AAR);
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        String carTypesAAR[] = Bundle.getMessage("carTypeARR").split(",");
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.getNames(); //Load predefined car types

        Assert.assertFalse("Predefined Car Type 1", ct.containsName(carTypes[1]));
        Assert.assertFalse("Predefined Car Type 2", ct.containsName(carTypes[2]));
        
        Assert.assertTrue("Predefined Car Type 1", ct.containsName(carTypesAAR[1]));
        Assert.assertTrue("Predefined Car Type 2", ct.containsName(carTypesAAR[2]));

        ct.changeDefaultNames(Setup.DESCRIPTIVE);

        Assert.assertTrue("Predefined Car Type 1", ct.containsName(carTypes[1]));
        Assert.assertTrue("Predefined Car Type 2", ct.containsName(carTypes[2]));
        
        Assert.assertFalse("Predefined Car Type 1", ct.containsName(carTypesAAR[1]));
        Assert.assertFalse("Predefined Car Type 2", ct.containsName(carTypesAAR[2]));
    }

    @Test
    public void testAddAndDeleteCarTypes() {
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.getNames(); //Load predefined car types

        ct.addName("Type New1");
        Assert.assertTrue("Car Types Add New1", ct.containsName("Type New1"));
        Assert.assertFalse("Car Types Never Added New2", ct.containsName("Type New2"));
        ct.addName("Type New3");
        Assert.assertTrue("Car Types Still Has New1", ct.containsName("Type New1"));
        Assert.assertTrue("Car Types Add New3", ct.containsName("Type New3"));
        ct.replaceName("Type New3", "Type New4");
        Assert.assertFalse("Car Types replace New3", ct.containsName("Type New3"));
        Assert.assertTrue("Car Types replace New3 with New4", ct.containsName("Type New4"));
        String[] types = ct.getNames();
        Assert.assertEquals("First type name", "Type New4", types[0]);
        Assert.assertEquals("2nd type name", "Type New1", types[1]);

        JComboBox<String> box = ct.getComboBox();
        Assert.assertEquals("First comboBox type name", "Type New4", box.getItemAt(0));
        Assert.assertEquals("2nd comboBox type name", "Type New1", box.getItemAt(1));

        ct.deleteName("Type New4");
        Assert.assertFalse("Car Types Delete New4", ct.containsName("Type New4"));
        ct.deleteName("Type New1");
        Assert.assertFalse("Car Types Delete New1", ct.containsName("Type New1"));
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        defaultLocale = Locale.getDefault(); // save the default locale.
    }

    @Override
    @After
    public void tearDown() {
        // reset the default locale
        Locale.setDefault(defaultLocale);
        super.tearDown();
    }
}
