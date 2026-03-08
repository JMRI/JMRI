package jmri.jmrit.display.palette;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JPanel;

import jmri.jmrit.display.Editor;
import jmri.util.junit.annotations.DisabledIfHeadless;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DragJComponentTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() throws ClassNotFoundException {
        DragJComponent t = new DragJComponent(new DataFlavor(Editor.POSITIONABLE_FLAVOR),new JPanel()){
           @Override
           public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
              return null;
           }
        };
        Assertions.assertNotNull(t,"exists");
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
