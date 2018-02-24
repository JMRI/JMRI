package jmri.jmrit.beantable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AddNewHardwareDevicePanelTest {

    @Test
    public void testCTor() {
        ActionListener createlistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { }
        };
        ActionListener cancellistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { }
        };
        ActionListener otherlistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { }
        };
        JButton okbutton = new JButton("ButtonOK");
        okbutton.addActionListener(createlistener);
        AddNewHardwareDevicePanel t = new AddNewHardwareDevicePanel(new JTextField(), new JTextField(), new JComboBox<String>(),
                new JSpinner(), new JCheckBox(), okbutton, cancellistener, otherlistener, new JLabel());
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

    // private final static Logger log = LoggerFactory.getLogger(AddNewHardwareDevicePanelTest.class);

}
