package apps.startup;

import javax.swing.JFileChooser;

import jmri.util.JUnitUtil;
import jmri.util.startup.StartupModel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PerformActionModelXmlTest.java
 * <p>
 * Test for the PerformActionModelXml class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AbstractFileModelFactoryTest {

    @Test
    @SuppressWarnings("deprecation")
    public void testCtor() {
        Assert.assertNotNull("PerformActionModelXml constructor", new AbstractFileModelFactory() {
            @Override
            protected JFileChooser setFileChooser() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Class<? extends StartupModel> getModelClass() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public StartupModel newModel() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
