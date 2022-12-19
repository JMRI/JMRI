package jmri.jmrit.logixng.util.parser;

import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Test TokenType.
 *
 * @author Daniel Bergqvist 2022
 */
public class TokenTypeTest {

    @Test
    public void testTokenType() throws InvalidSyntaxException {
        for (TokenType tokenType : TokenType.values()) {
            if (tokenType.getString() != null) {
                List<Token> tokens = Tokenizer.getTokens(tokenType.getString());
                Assert.assertEquals(1, tokens.size());
                Assert.assertEquals(tokenType, tokens.get(0).getTokenType());
            }
        }
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
