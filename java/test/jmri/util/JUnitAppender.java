package jmri.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;

import java.io.Serializable;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.config.Property;

/**
 * Log4J2 Appender Plugin that works with JUnit tests to check for expected vs
 * unexpected log messages.
 * Used by tests_lcf.xml
 *
 * Much of the interface is static to avoid lots of instance() calls, but this
 * is not a problem as there should be only one of these while tests are running
 *
 * @see jmri.util.JUnitUtil
 *
 * @author Bob Jacobsen - Copyright 2007
 */
@Plugin(name="JUnitAppender", category="Core", elementType="appender", printObject=true)
public class JUnitAppender extends AbstractAppender {

    protected JUnitAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout, 
            final boolean ignoreExceptions, final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties );
        activateInstance();
    }

    /**
     * Create an Appender.
     * <p>
     * Log4j will parse the configuration and call this factory method to
     * construct an Appender instance with the configured attributes.
     * <p>
     * @param name Plugin Name
     * @param layout Layout to use, if null uses standard PatternLayout
     * @param filter A Filter in use
     * @return New Appender.
     */
    @PluginFactory
    public static JUnitAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new JUnitAppender(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    static java.util.ArrayList<LogEvent> list = new java.util.ArrayList<>();

    /**
     * Called for each logging event.
     *
     * @param event the event to log
     */
    @Override
    public synchronized void append(LogEvent event) {
        if (hold) {
            list.add(event);
        } else {
            sendToConsole(event);
        }
    }

    private void sendToConsole(LogEvent ev){
        try {
            final byte[] bytes = getLayout().toByteArray(ev);
            System.out.write(bytes);
        } catch (IOException ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        }
    }
    
    /**
     * Called once options are set.
     *
     */
    private void activateInstance() {
        if (JUnitAppender.instance != null) {
            System.err.println("JUnitAppender initialized more than once"); // can't count on logging here
        } else {
            JUnitAppender.instance = this;
        }

    }

    /**
     * Do clean-up at end.
     *
     * Currently just reflects back to super-class.
     */
    @Override
    public synchronized void stop() {
        list.clear();
        super.stop();
    }

    static boolean hold = false;

    static private JUnitAppender instance = null;

    // package-level access for testing
    static volatile boolean unexpectedFatalSeen = false;
    static volatile String  unexpectedFatalContent = null;
    static volatile boolean unexpectedErrorSeen = false;
    static volatile String  unexpectedErrorContent = null;
    static volatile boolean unexpectedWarnSeen = false;
    static volatile String  unexpectedWarnContent = null;
    static volatile boolean unexpectedInfoSeen = false;
    static volatile String  unexpectedInfoContent = null;

    static synchronized void setUnexpectedFatalSeen(boolean seen) {
        unexpectedFatalSeen = seen;
    }

    static synchronized void setUnexpectedErrorSeen(boolean seen) {
        unexpectedErrorSeen = seen;
    }

    static synchronized void setUnexpectedWarnSeen(boolean seen) {
        unexpectedWarnSeen = seen;
    }

    static synchronized void setUnexpectedInfoSeen(boolean seen) {
        unexpectedInfoSeen = seen;
    }

    static synchronized void setUnexpectedFatalContent(String content) {
        unexpectedFatalContent = content;
    }

    static synchronized void setUnexpectedErrorContent(String content) {
        unexpectedErrorContent = content;
    }

    static synchronized void setUnexpectedWarnContent(String content) {
        unexpectedWarnContent = content;
    }

    static synchronized void setUnexpectedInfoContent(String content) {
        unexpectedInfoContent = content;
    }

    public static boolean unexpectedMessageSeen(org.slf4j.event.Level l) {
        return unexpectedMessageSeen(convertSlf4jLevelToLog4jLevel(l));
    }

    private static boolean unexpectedMessageSeen(Level l) {
        if (l == Level.FATAL) {
            return unexpectedFatalSeen;
        }
        if (l == Level.ERROR) {
            return unexpectedFatalSeen || unexpectedErrorSeen;
        }
        if (l == Level.WARN) {
            return unexpectedFatalSeen || unexpectedErrorSeen || unexpectedWarnSeen;
        }
        if (l == Level.INFO) {
            return unexpectedFatalSeen || unexpectedErrorSeen || unexpectedWarnSeen || unexpectedInfoSeen;
        }
        throw new java.lang.IllegalArgumentException("Did not expect " + l);
    }

    public static String unexpectedMessageContent(org.slf4j.event.Level l) {
        return unexpectedMessageContent(convertSlf4jLevelToLog4jLevel(l));
    }

    private static String unexpectedMessageContent(Level l) {
        if (l == Level.FATAL) {
            return unexpectedFatalContent;
        }
        if (l == Level.ERROR) {
            if (unexpectedFatalContent != null ) return unexpectedFatalContent;
            return unexpectedErrorContent;
        }
        if (l == Level.WARN) {
            if (unexpectedFatalContent != null ) return unexpectedFatalContent;
            if (unexpectedErrorContent != null ) return unexpectedErrorContent;
            return unexpectedWarnContent;
        }
        if (l == Level.INFO) {
            if (unexpectedFatalContent != null ) return unexpectedFatalContent;
            if (unexpectedErrorContent != null ) return unexpectedErrorContent;
            if (unexpectedWarnContent != null ) return unexpectedWarnContent;
            return unexpectedInfoContent;
        }
        throw new java.lang.IllegalArgumentException("Did not expect " + l);
    }

    /**
     * Reset the Unexpected Message Flags.
     * e.g. Level.ERROR will reset both setUnexpectedErrorSeen and setUnexpectedFatalSeen
     * @param severity the lowest severity level to reset to.
     */
    public static void resetUnexpectedMessageFlags(org.slf4j.event.Level severity) {
        resetUnexpectedMessageFlags(convertSlf4jLevelToLog4jLevel(severity));
    }

    private static void resetUnexpectedMessageFlags(Level severity) {
        if ( severity.isLessSpecificThan(Level.INFO) ){
            setUnexpectedInfoSeen(false);
            unexpectedInfoContent = null;
        }
        if ( severity.isLessSpecificThan(Level.WARN) ){
            setUnexpectedWarnSeen(false);
            unexpectedWarnContent = null;
        }
        if ( severity.isLessSpecificThan(Level.ERROR) ){
            setUnexpectedErrorSeen(false);
            unexpectedErrorContent = null;
        }
        if ( severity.isLessSpecificThan(Level.FATAL) ){
            setUnexpectedFatalSeen(false);
            unexpectedFatalContent = null;
        }
    }

    /**
     * Tell appender that a JUnit test is starting.
     * <p>
     * This causes log messages to be held for examination.
     */
    public static void startLogging() {
        hold = true;
    }

    /**
     * Tell appender that the JUnit test is ended.
     * <p>
     * Any queued messages at this point will be passed through to the actual
     * log.
     */
    public static void end() {
        hold = false;
        while (!list.isEmpty()) {
            LogEvent evt = list.remove(0);
            instance().superappend(evt);
        }
    }

    /**
     * do common local processing of event, then pass up to super class
     *
     * @param l the event to process
     */
    void superappend(LogEvent l) {
        if (l.getLevel() == Level.FATAL) {
            setUnexpectedFatalSeen(true);
            setUnexpectedFatalContent(l.getMessage().getFormattedMessage());
        }
        if (l.getLevel() == Level.ERROR) {
            if (compare(l, "Uncaught Exception caught by jmri.util.exceptionhandler.UncaughtExceptionHandler")) {
                // still an error, just suppressed
            } else {
                setUnexpectedErrorSeen(true);
                setUnexpectedErrorContent(l.getMessage().getFormattedMessage());
            }
        }
        if (l.getLevel() == Level.WARN) {
            setUnexpectedWarnSeen(true);
            setUnexpectedWarnContent(l.getMessage().getFormattedMessage());
        }
        if (l.getLevel() == Level.INFO) {
            setUnexpectedInfoSeen(true);
            setUnexpectedInfoContent(l.getMessage().getFormattedMessage());
        }

        sendToConsole(l);
    }

    /**
     * Remove any messages stored up, returning how many there were. This is
     * used to skip over messages that don't matter, e.g. during setting up a
     * test. Removed messages are not sent for further logging.
     *
     * @param l lowest level counted in return value, e.g. WARN means WARN
     *                  and higher will be counted
     * @return count of skipped messages
     * @see #clearBacklog()
     */
    public static int clearBacklog(org.slf4j.event.Level l) {
        return clearBacklog(convertSlf4jLevelToLog4jLevel(l));
    }

    private static int clearBacklog(Level level) {
        if (list.isEmpty()) {
            return 0;
        }
        int retval = 0;
        for (LogEvent event : list) {
            if (event != null && event.getLevel() != null && event.getLevel().isMoreSpecificThan(level)) {
                retval++; // higher number -> more severe, specific, limited
            }
        }
        list.clear();
        return retval;
    }

    /**
     * Remove any messages stored up, returning how many of WARN or higher
     * severity there are. This is used to skip over messages that don't matter,
     * e.g. during setting up a test. Removed messages are not sent for further
     * logging.
     *
     * @return count of skipped messages of WARN or more specific level
     * @see #clearBacklog(Level)
     */
    public static int clearBacklog() {
        return clearBacklog(Level.WARN);
    }

    /**
     * Returns the backlog.
     * @return the backlog
     */
    public static List<LogEvent> getBacklog() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Verify that no messages were emitted, logging any that were. Does not
     * stop the logging. Clears the accumulated list.
     *
     * @return true if no messages logged
     */
    public static boolean verifyNoBacklog() {
        if (list.isEmpty()) {
            return true;
        }
        while (!list.isEmpty()) { // should probably add a skip of lower levels?
            LogEvent evt = list.remove(0);
            instance().superappend(evt);
        }
        return false;
    }

    public static void assertNoErrorMessage() {
        assertThat(list).isEmpty();
    }

    /**
     * Check that the next queued message was of Error severity, and has a
     * specific message. White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert exists
     */
    public static void assertErrorMessage(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }

        LogEvent evt = list.remove(0);

        // next piece of code appears three times, should be refactored away during Log4J 2 migration
        while ((evt.getLevel() == Level.INFO) || (evt.getLevel() == Level.DEBUG) || (evt.getLevel() == Level.TRACE)) { // better in Log4J 2
            if (list.isEmpty()) {
                Assert.fail("Only debug/info messages present: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != Level.ERROR) {
            Assert.fail("Level mismatch when looking for ERROR message: \"" +
                    msg +
                    "\" found \"" +
                    evt.getMessage().getFormattedMessage() +
                    "\"");
        }

        if (!compare(evt, msg)) {
            Assert.fail("Looking for ERROR message \"" + msg + "\" got \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * Check that the next queued message was of Error severity, and has a
     * specific message. White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert exists
     */
    public static void assertErrorMessageStartsWith(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }

        LogEvent evt = list.remove(0);

        // next piece of code appears three times, should be refactored away during Log4J 2 migration
        while ((evt.getLevel() == Level.INFO) || (evt.getLevel() == Level.DEBUG) || (evt.getLevel() == Level.TRACE)) { // better in Log4J 2
            if (list.isEmpty()) {
                Assert.fail("Only debug/info messages present: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != Level.ERROR) {
            Assert.fail("Level mismatch when looking for ERROR message: \"" +
                    msg +
                    "\" found \"" +
                    evt.getMessage().getFormattedMessage() +
                    "\"");
        }

        if (!compareStartsWith(evt, msg)) {
            Assert.fail("Looking for ERROR message \"" + msg + "\" got \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * Check that the next queued message was of Warn severity, and text
     * is at start of this message. White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert starts with
     */
    public static void assertWarnMessageStartsWith(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }

        LogEvent evt = list.remove(0);

        // next piece of code appears three times, should be refactored away during Log4J 2 migration
        while ((evt.getLevel() == Level.INFO) || (evt.getLevel() == Level.DEBUG) || (evt.getLevel() == Level.TRACE)) { // better in Log4J 2
            if (list.isEmpty()) {
                Assert.fail("Only debug/info messages present: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != Level.WARN) {
            Assert.fail("Level mismatch when looking for WARN message: \"" +
                    msg +
                    "\" found \"" +
                    evt.getMessage().getFormattedMessage() +
                    "\"");
        }

        if (!compareStartsWith(evt, msg)) {
            Assert.fail("Looking for WARN message \"" + msg + "\" got \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * If there's a next matching message of specific severity, just ignore it.
     * Not an error if not present; mismatch is an error. Skips messages of
     * lower severity while looking for the specific one. White space is
     * ignored.
     *
     * @param level the level at which to suppress the message
     * @param msg   the message to suppress
     */
    private static void suppressMessage(Level level, String msg) {
        if (list.isEmpty()) {
            return;
        }

        LogEvent evt = list.remove(0);

        while (((level.equals(Level.WARN)) &&
                (evt.getLevel() == Level.TRACE ||
                        evt.getLevel() == Level.DEBUG ||
                        evt.getLevel() == Level.INFO ||
                        evt.getLevel() == Level.WARN)) ||
                ((level.equals(Level.ERROR)) &&
                        (evt.getLevel() == Level.TRACE ||
                                evt.getLevel() == Level.DEBUG ||
                                evt.getLevel() == Level.INFO ||
                                evt.getLevel() == Level.WARN ||
                                evt.getLevel() == Level.ERROR))) { // this is much better with Log4J 2's compareTo method
            if (list.isEmpty()) {
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != level) {
            Assert.fail("Level mismatch when looking for " +
                    level +
                    " message: \"" +
                    msg +
                    "\" found \"" +
                    evt.getMessage().getFormattedMessage() +
                    "\"");
        }

        if (!compare(evt, msg)) {
            Assert.fail("Looking for " + level + " message \"" + msg + "\" got \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * If there's a next matching message of specific severity, just ignore it.
     * Not an error if not present; mismatch is an error. Skips messages of
     * lower severity while looking for the specific one. White space is
     * ignored.
     *
     * @param level the level at which to suppress the message
     * @param msg   text at start of the message to suppress
     */
    private static void suppressMessageStartsWith(Level level, String msg) {
        if (list.isEmpty()) {
            return;
        }

        LogEvent evt = list.remove(0);

        while (((level.equals(Level.WARN)) &&
                (evt.getLevel() == Level.TRACE ||
                        evt.getLevel() == Level.DEBUG ||
                        evt.getLevel() == Level.INFO ||
                        evt.getLevel() == Level.WARN)) ||
                ((level.equals(Level.ERROR)) &&
                        (evt.getLevel() == Level.TRACE ||
                                evt.getLevel() == Level.DEBUG ||
                                evt.getLevel() == Level.INFO ||
                                evt.getLevel() == Level.WARN ||
                                evt.getLevel() == Level.ERROR))) { // this is much better with Log4J 2's compareTo method
            if (list.isEmpty()) {
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if (evt.getLevel() != level) {
            Assert.fail("Level mismatch when looking for " +
                    level +
                    " message: \"" +
                    msg +
                    "\" found \"" +
                    evt.getMessage().getFormattedMessage() +
                    "\"");
        }

        if (!compareStartsWith(evt, msg)) {
            Assert.fail("Looking for " + level + " message \"" + msg + "\" got \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * If there's a next matching message of Error severity, just ignore it. Not
     * an error if not present; mismatch is an error. White space is ignored.
     *
     * @param msg the message to suppress
     */
    public static void suppressErrorMessage(String msg) {
        suppressMessage(Level.ERROR, msg);
    }

    /**
     * If there's a next matching message of Error severity, just ignore it. Not
     * an error if not present; mismatch is an error. White space is ignored.
     *
     * @param msg text at start of the message to suppress
     */
    public static void suppressErrorMessageStartsWith(String msg) {
        suppressMessageStartsWith(Level.ERROR, msg);
    }

    /**
     * If there's a next matching message of Warn severity, just ignore it. Not
     * an error if not present; mismatch is an error. White space is ignored.
     *
     * @param msg the message to suppress
     */
    public static void suppressWarnMessage(String msg) {
        suppressMessage(Level.WARN, msg);
    }

    /**
     * If there's a next matching message of Warn severity, just ignore it. Not
     * an error if not present; mismatch is an error. White space is ignored.
     *
     * @param msg text at start of the message to suppress
     */
    public static void suppressWarnMessageStartsWith(String msg) {
        suppressMessageStartsWith(Level.WARN, msg);
    }

    /**
     * See if a message (completely matching particular text) has been emitted
     * yet. White space is ignored. All messages before the requested one are
     * dropped; it the requested message hasn't been issued, this means that the
     * message queue is cleared.
     *
     * @param msg the message text to check for
     * @return null if not present, else the LoggingEvent for possible further
     *         checks of level, etc
     */
    public static LogEvent checkForMessage(String msg) {
        if (list.isEmpty())
            return null;

        LogEvent evt = list.remove(0);
        while (!compare(evt, msg)) {
            if (list.isEmpty()) {
                return null; // normal to not find it
            }
            evt = list.remove(0);
        }
        // fall through with a match

        return evt;
    }

    /**
     * See if a message that starts with particular text has been emitted yet.
     * White space is ignored. All messages before the matching one are dropped;
     * it a matching message hasn't been issued, this means that the message
     * queue is cleared.
     *
     * @param msg the message text to check for
     * @return null if not present, else the LoggingEvent for possible further
     *         checks of level, etc
     */
    public static LogEvent checkForMessageStartingWith(String msg) {
        if (list.isEmpty())
            return null;

        String tmsg = StringUtils.deleteWhitespace(msg);

        LogEvent evt = list.remove(0);
        while (!StringUtils.deleteWhitespace(evt.getMessage().getFormattedMessage()).startsWith(tmsg)) {
            if (list.isEmpty()) {
                return null; // normal to not find it
            }
            evt = list.remove(0);
        }
        // fall through with a match

        return evt;
    }

    /**
     * Check that the next queued message was of Warn severity, and has a
     * specific message. White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert exists
     */
    public static void assertWarnMessage(String msg) {
        assertMessage(msg, Level.WARN);
    }

    /**
     * Check that the next queued message has a specific message
     * and matches the expected severity level.
     * White space is ignored.<p>
     * Invokes a JUnit Assertion Fail if the message doesn't match,
     * or if the Logging Level is different.
     *
     * @param msg the message to assert exists
     * @param level the Logging Level which should match
     */
    public static void assertMessage(String msg, org.slf4j.event.Level level) {
        assertMessage(msg, convertSlf4jLevelToLog4jLevel(level));
    }

    private static void assertMessage(String msg, Level level) {
        LogEvent evt = checkForMessage(msg);
        if (evt == null) {
            Assertions.fail("Looking for message \"" + msg + "\" and didn't find it");
            return;
        }
        if (level != evt.getLevel() ){
            Assertions.fail("Incorrect logging level for \"" + msg
                + "\" expecting " + level + " was " + evt.getLevel());
        }
    }

    /**
     * Check that the next queued message was of Warn severity, and has a
     * specific message. White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert exists
     */
    public static void assertWarnMessageStartingWith(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }
        LogEvent evt = checkForMessageStartingWith(msg);

        if (evt == null) {
            Assert.fail("Looking for message \"" + msg + "\" and didn't find it");
        }
    }

    /**
     * Assert that a specific message, of any severity, has been logged. White
     * space is ignored.
     * <p>
     * Invokes a JUnit Assert if no matching message is found, but doesn't
     * require it to be the next message. This allows use e.g. for
     * debug-severity messages.
     *
     * @param msg the message to assert exists
     */
    public static void assertMessage(String msg) {
        if (list.isEmpty()) {
            Assert.fail("No message present: " + msg);
            return;
        }
        LogEvent evt = list.remove(0);

        while ((evt.getLevel() == Level.INFO) || (evt.getLevel() == Level.DEBUG) || (evt.getLevel() == Level.TRACE)) { // better in Log4J 2
            if (list.isEmpty()) {
                Assert.fail("Message not found: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        if (!compare(evt, msg)) {
            Assert.fail("Looking for message \"" + msg + "\" got \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * Compare two message strings, handling nulls and ignoring whitespace.
     *
     * @param e1 the event
     * @param s2 the string to compare e1 to
     * @return true if message in e1 equals s2; false otherwise
     */
    protected static boolean compare(LogEvent e1, String s2) {
        if (e1 == null) {
            System.err.println("Logging event null when comparing to " + s2);
            return s2 == null;
        } else if (e1.getMessage() == null) {
            System.err.println("Logging event has null message when comparing to " + s2);
            return s2 == null;
        }
        String s1 = e1.getMessage().getFormattedMessage();
        return StringUtils.deleteWhitespace(s1).equals(StringUtils.deleteWhitespace(s2));
    }

    /**
     * Compare two message strings, handling nulls and ignoring whitespace.
     *
     * @param e1 the event
     * @param s2 the string to compare e1 to
     * @return true if message in e1 starts with s2; false otherwise
     */
    protected static boolean compareStartsWith(LogEvent e1, String s2) {
        if (e1 == null) {
            System.err.println("Logging event null when comparing to " + s2);
            return s2 == null;
        } else if (e1.getMessage() == null) {
            System.err.println("Logging event has null message when comparing to " + s2);
            return s2 == null;
        }
        String s1 = e1.getMessage().getFormattedMessage();
        return StringUtils.deleteWhitespace(s1).startsWith(StringUtils.deleteWhitespace(s2));
    }

    private static Level convertSlf4jLevelToLog4jLevel(org.slf4j.event.Level slf4jLevel) {
        switch (slf4jLevel) {
            case TRACE:
                return Level.TRACE;
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
            default:
                return Level.ERROR;
        }
    }

    public static JUnitAppender instance() {
        return JUnitAppender.instance;
    }

}
