package jmri.jmrit.beantable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AddNewBeanPanelTest {

    @Test
    public void testCTor() {
        ActionListener oklistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        ActionListener cancellistener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        };
        AddNewBeanPanel t = new AddNewBeanPanel(new JTextField(), new JTextField(), new JSpinner(), new JCheckBox(), new JCheckBox(), "ButtonOK", oklistener, cancellistener, new JLabel());
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddNewBeanPanelTest.class);

}
