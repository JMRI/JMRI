package jmri.jmrix;

import org.assertj.swing.edt.GuiActionRunner;
import org.junit.jupiter.api.*;

import javax.swing.JPanel;

/**
 * Base tests for SerialConnectionConfig objects.
 *
 * @author Paul Bender Copyright (C) 2018
 */
abstract public class AbstractSerialConnectionConfigTestBase extends jmri.jmrix.AbstractConnectionConfigTestBase {

    @Test
    @Override
    public void testLoadDetails(){
        GuiActionRunner.execute(() -> {
            // verify no exceptions thrown
            cc.loadDetails(new JPanel());
            // load details MAY produce an error message if no ports are found.
            jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
        });
    }
}
