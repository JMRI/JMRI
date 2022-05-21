package jmri.jmrit.logix;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pete Cressman 2022
 */
public class TrainOrderTest {

    @Test
    public void testCTor() {
        TrainOrder t = new TrainOrder("Medium", TrainOrder.Cause.ERROR, 1, 0, "Error");
        assertThat(t).withFailMessage("exists").isNotNull();
        assertThat(t.toString().startsWith("TrainOrder: speedType")).withFailMessage("toString").isTrue();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
