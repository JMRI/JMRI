package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JPanel;

import jmri.jmrit.display.Editor;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DragJComponentTest {

    @Test
    public void testCTor() throws ClassNotFoundException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DragJComponent t = new DragJComponent(new DataFlavor(Editor.POSITIONABLE_FLAVOR),new JPanel()){
           @Override
           public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
              return null;
           }
        };
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DropJLabelTest.class);

}
