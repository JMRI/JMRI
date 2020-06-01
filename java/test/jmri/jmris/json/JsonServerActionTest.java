package jmri.jmris.json;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class JsonServerActionTest {

    @Test
    public void testCTor() {
        JsonServerAction t = new JsonServerAction();
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonServerActionTest.class);

}
