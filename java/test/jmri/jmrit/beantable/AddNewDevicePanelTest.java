package jmri.jmrit.beantable;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(AddNewDevicePanelTest.class);

}
