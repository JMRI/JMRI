package jmri.jmrit.blockboss;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JFrameOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BlockBossActionTest {

    @Test
    public void testCtor() {
        BlockBossAction t = new BlockBossAction();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @Test
    @DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
    public void testAction() {
        BlockBossAction t = new BlockBossAction();
        t.actionPerformed(new java.awt.event.ActionEvent(this,1,"test action event"));

        JFrameOperator fo = new JFrameOperator(Bundle.getMessage("Simple_Signal_Logic"));
        fo.close();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockBossActionTest.class);

}
