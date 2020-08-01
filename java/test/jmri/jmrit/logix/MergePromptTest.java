package jmri.jmrit.logix;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.jemmy.operators.JDialogOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
public class MergePromptTest {

    @Test
    @Disabled("unreliable; frequently errors or times out")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator("Merge Prompt CTor Test");
            jdo.close();
        });
        t.setName("MergePrompt Dialog Close Thread");
        t.start();

        MergePrompt m = new MergePrompt("Merge Prompt CTor Test",new HashMap<String,Boolean>(),
                        new HashMap<String, Map<Integer,Boolean>>());
        assertThat(m).withFailMessage("exists").isNotNull();
        m.dispose();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MergePromptTest.class.getName());

}
