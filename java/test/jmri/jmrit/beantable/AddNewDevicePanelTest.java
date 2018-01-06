package jmri.jmrit.beantable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AddNewDevicePanelTest {

    @Test
    public void testCTor() {
        ActionListener oklistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { }
        };
        ActionListener cancellistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { }
        };
        AddNewDevicePanel t = new AddNewDevicePanel(new JTextField(), new JTextField(), "ButtonOK", oklistener, cancellistener);    
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddNewDevicePanelTest.class);

}
