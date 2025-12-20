package jmri.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.Assertions;

/**
 * Log4J2 Appender Plugin that works with JUnit tests to check for expected vs
 * unexpected log messages.
 * Used by tests_lcf.xml
 * Most tests log at WARN or ERROR, the root logging level within tests_lcf.xml
 * Much of the interface is static to avoid lots of instance() calls, but this
 * is not a problem as there should be only one of these while tests are running
 *
 * level.isLessSpecificThan(Level.WARN) true if level DEBUG / INFO / WARN
 * level.isMoreSpecificThan(Level.WARN) true if level WARN / ERROR
 *
 * ( level.compareTo(Level.WARN) &gt; 0 )  true if level DEBUG / INFO
 * ( level.compareTo(Level.WARN) &gt;= 0 ) true if level DEBUG / INFO / WARN
 * ( level.compareTo(Level.WARN) == 0 ) true if level WARN
 * ( level.compareTo(Level.WARN) &lt;= 0 ) true if level WARN / ERROR
 * ( level.compareTo(Level.WARN) &lt; 0 )  true if level ERROR
 *
 * Level.ERROR = 200
 * Level.DEBUG = 500
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

    private static final java.util.ArrayList<LogEvent> list = new java.util.ArrayList<>();

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

    /**
     * Send a Logging Event to the Console.
     * @param ev the LogEvent to send.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
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
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
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

    private static boolean hold = false;

    private static JUnitAppender instance = null;

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
            if (unexpectedFatalContent != null ) { return unexpectedFatalContent; }
            return unexpectedErrorContent;
        }
        if (l == Level.WARN) {
            if (unexpectedFatalContent != null ) { return unexpectedFatalContent; }
            if (unexpectedErrorContent != null ) { return unexpectedErrorContent; }
            return unexpectedWarnContent;
        }
        if (l == Level.INFO) {
            if (unexpectedFatalContent != null ) { return unexpectedFatalContent; }
            if (unexpectedErrorContent != null ) { return unexpectedErrorContent; }
            if (unexpectedWarnContent != null ) { return unexpectedWarnContent; }
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
    private void superappend(LogEvent l) {
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
     * Remove any messages stored up, returning how many there were.
     * This is used to skip over messages that don't matter,
     * e.g. during setting up a test.
     * Please check the return value of this method against the expected number.
     * Please do not use this method to clear the backlog without checking.
     * The majority of tests are set to only log at the Levels WARN / ERROR so
     * INFO / DEBUG levels are not normally included.
     * Removed messages are not sent for further logging.
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
     * severity there are.
     * This is used to skip over messages that don't matter,
     * e.g. during setting up a test.
     * Removed messages are not sent for further logging.
     * Please check the return value of this method against the expected number.
     * Please do not use this method to clear the backlog without checking.
     * @return count of skipped messages of WARN or more specific level
     * @see #clearBacklog(Level)
     */
    @SuppressWarnings("javadoc")
    public static int clearBacklog() {
        return clearBacklog(Level.WARN);
    }

    /**
     * Returns the backlog.
     * @return the backlog, unmodifiable.
     */
    @Nonnull
    public static List<LogEvent> getBacklog() {
        return Collections.unmodifiableList(list);
    }

    /**
     * Verify that no messages were emitted, logging any that were.
     * Does not stop the logging.
     * Clears the accumulated list.
     * The majority of tests are set to only log at the Levels WARN / ERROR.
     * @return true if no messages logged
     */
    public static boolean verifyNoBacklog() {
        if (list.isEmpty()) {
            return true;
        }
        while (!list.isEmpty()) {
            LogEvent evt = list.remove(0);
            instance().superappend(evt);
        }
        return false;
    }

    /**
     * Assert that no Error Messages are present in the logging queue.
     * Messages of Level WARN / INFO / DEBUG are permitted.
     * No messages are removed from the message queue.
     */
    public static void assertNoErrorMessage() {
        assertNoMessagesOf( Level.ERROR );
    }

    /**
     * Assert that no messages are present of a specific severity or higher.
     * Does not remove any messages from the message queue.
     * @param level the level of which to check.
     */
    private static void assertNoMessagesOf( Level level) {
        for (LogEvent event : list) {
            if ( event.getLevel().isMoreSpecificThan(level) ) {
                Assertions.fail("Log Message " + event.getLevel().name() + ": " + event.getMessage().getFormattedMessage());
            }
        }
    }

    /**
     * Check that the next queued message was of Error severity,
     * and has a specific message.
     * White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert exists
     */
    public static void assertErrorMessage(String msg) {
        assertNextMessage(msg, Level.ERROR, Level.INFO);
    }

    private static void assertNextMessage(String msg, Level level, Level exclude) {
        if (list.isEmpty()) {
            Assertions.fail("No " + level.name()+ " message present: \"" + msg + "\"");
            return;
        }

        LogEvent evt = list.remove(0);

        while ((evt.getLevel().isLessSpecificThan(exclude))) {
            if (list.isEmpty()) {
                Assertions.fail("Only " + exclude.name() + " or less severe messages present: Looking for "
                    + level.name()+ " \""+ msg + "\"");
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if ( (evt.getLevel() != level) || !compare(evt, msg)) {
            Assertions.fail("Looking for " + level.name() + " message \"" + msg + "\" got "
                + evt.getLevel().name() + "\"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * Check that the next queued message was of Error severity,
     * and text is at start of this message.
     * White space is ignored.
     * Prior TRACE / DEBUG / INFO level messages are ignored.
     * Prior messages of level WARN or ERROR are NOT ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert exists
     */
    public static void assertErrorMessageStartsWith(String msg) {
        assertNextMessageStartsWith(Level.ERROR, msg);
    }

    /**
     * Check that the next queued message was of Warn severity,
     * and text is at start of this message.
     * White space is ignored.
     * Prior TRACE / DEBUG / INFO level messages are ignored.
     * Prior messages of level WARN or ERROR are NOT ignored.tt
    * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     *
     * @param msg the message to assert starts with
     */
    public static void assertWarnMessageStartsWith(String msg) {
        assertNextMessageStartsWith(Level.WARN, msg);
    }

    /**
     * Assert that a message exists of a given level and message String.
     * Strips INFO / DEBUG / TRACE messages,
     * then checks the next message in the queue.
     * @param level the level to match
     * @param msg the message to match
     */
    private static void assertNextMessageStartsWith(Level level, String msg) {
        if (list.isEmpty()) {
            Assertions.fail("No " + level + " message present: " + msg);
            return;
        }

        LogEvent evt = list.remove(0);

        while (evt.getLevel().isLessSpecificThan(Level.INFO) ) {
            if (list.isEmpty()) {
                Assertions.fail("Only debug/info messages present when looking to Assert "
                    + level.name() + " StartsWith: " + msg);
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if ( (evt.getLevel() != level ) || !compareStartsWith(evt, msg)) {
            Assertions.fail("Looking for " + level.name() + " message starting with \"" + msg + "\" got "
                + evt.getLevel().name() + "\"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * If there's a last matching message of specific severity, just ignore it.
     * Not an error if not present; mismatch is an error.
     * Removes messages of lower or equal severity while looking for the specific one.
     * White space is ignored.
     *
     * @param level the level at which to suppress the message
     * @param msg   the message to suppress
     */
    private static void suppressMessage(Level level, String msg) {
        if (list.isEmpty()) {
            return;
        }

        LogEvent evt = list.remove(0);

        // TODO test while ( level.compareTo(evt.getLevel() ) < 0 ) {
        while ( level.compareTo(evt.getLevel() ) <= 0 ) {
            if (list.isEmpty()) {
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if ( (evt.getLevel() != level) || !compare(evt, msg)) {
            Assertions.fail("Looking to suppress " + level + " message \"" + msg + "\" got "
                + evt.getLevel().name()+ " \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * If there's a next matching message of specific severity, just ignore it.
     * Not an error if not present; mismatch is an error.
     * Removes messages of lower or equal severity while looking for a matching one.
     * White space is ignored.
     *
     * @param level the level at which to suppress the message
     * @param msg   text at start of the message to suppress
     */
    private static void suppressMessageStartsWith(Level level, String msg) {
        if (list.isEmpty()) {
            return;
        }

        LogEvent evt = list.remove(0);

        // TODO test while ( level.compareTo(evt.getLevel() ) < 0 ) {
        while ( level.compareTo(evt.getLevel() ) <= 0 ) {
            if (list.isEmpty()) {
                return;
            }
            evt = list.remove(0);
        }

        // check the remaining message, if any
        if ( (evt.getLevel() != level) || !compareStartsWith(evt, msg)) {
            Assertions.fail("Looking to suppress " + level + " message starting with \"" + msg + "\" got "
                + evt.getLevel() + " \"" + evt.getMessage().getFormattedMessage() + "\"");
        }
    }

    /**
     * If there's a next matching message of Error severity, just ignore it.
     * Not an error if not present; mismatch is an error.
     * White space is ignored.
     *
     * @param msg the message to suppress
     */
    public static void suppressErrorMessage(String msg) {
        suppressMessage(Level.ERROR, msg);
    }

    /**
     * If there's a next matching message of Error severity, just ignore it.
     * Not an error if not present; mismatch is an error.
     * White space is ignored.
     * Removes messages of lower or equal severity while looking for a matching one.
     * @param msg text at start of the message to suppress
     */
    public static void suppressErrorMessageStartsWith(String msg) {
        suppressMessageStartsWith(Level.ERROR, msg);
    }

    /**
     * If there's a next matching message of Warn severity, just ignore it.
     * Not an error if not present; mismatch is an error.
     * White space is ignored.
     *
     * @param msg the message to suppress
     */
    public static void suppressWarnMessage(String msg) {
        suppressMessage(Level.WARN, msg);
    }

    /**
     * If there's a next matching message of Warn severity, just ignore it.
     * Not an error if not present; mismatch is an error.
     * White space is ignored.
     * Removes messages of lower or equal severity while looking for a matching one.
     * @param msg text at start of the message to suppress
     */
    public static void suppressWarnMessageStartsWith(String msg) {
        suppressMessageStartsWith(Level.WARN, msg);
    }

    /**
     * See if a message (completely matching particular text) has been emitted
     * yet.
     * White space is ignored.
     * All messages of any severity sent before the matching String are dropped.
     * If the requested message hasn't been issued, this means that the
     * message queue is cleared.
     *
     * @param msg the message text to check for
     * @return null if not present, else the LoggingEvent for possible further
     *         checks of level, etc
     */
    @CheckForNull
    public static LogEvent checkForMessage(String msg) {
        if (list.isEmpty()) {
            return null;
        }

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
     * White space is ignored.
     * All messages OF ANY SEVERITY before the matching one are dropped.
     * If a matching message hasn't been issued, this means that the message
     * queue is cleared.
     *
     * @param msg the message text to check for
     * @return null if not present, else the LoggingEvent for possible further
     *         checks of level, etc
     */
    @CheckForNull
    public static LogEvent checkForMessageStartingWith(String msg) {
        if (list.isEmpty()) {
            return null;
        }

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
     * specific message.
     * ALL messages of ANY severity sent before the matching one are dropped.
     * White space is ignored.
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
     * ALL messages of ANY severity sent before the matching one are dropped.
     * Invokes a JUnit Assertion Fail if the message doesn't match,
     * or if the Logging Level is different.
     *
     * @param msg the message to assert exists
     * @param level the Logging Level which should match
     */
    public static void assertMessage(String msg, org.slf4j.event.Level level) {
        assertMessage(msg, convertSlf4jLevelToLog4jLevel(level));
    }

    /**
     * Assert Message with matching String and Level.
     * All messages of any severity sent before the matching one are dropped.
     * @param msg the Message String to match.
     * @param level the expected level of the LogEvent.
     */
    private static void assertMessage(String msg, @Nonnull Level level) {
        LogEvent evt = checkForMessage(msg);
        if (evt == null) {
            Assertions.fail("Looking for " + level.name() + " message \"" + msg + "\" and didn't find it");
            return;
        }
        if (level != evt.getLevel() ){
            Assertions.fail("Incorrect logging level for \"" + msg
                + "\" expecting " + level + " was " + evt.getLevel());
        }
    }

    /**
     * Check that the next queued message was of Warn severity, and has a
     * specific message.
     * ALL messages of ANY severity sent before the matching one are dropped.
     * White space is ignored.
     * <p>
     * Invokes a JUnit Assert if the message doesn't match.
     * Does NOT currently ensure correct logging level ( WARN ) is matched.
     * @param msg the message to assert exists
     */
    @jmri.util.junit.annotations.ToDo("Add check for message Level severity")
    public static void assertWarnMessageStartingWith(String msg) {
        if (list.isEmpty()) {
            Assertions.fail("No message present: " + msg);
            return;
        }
        LogEvent evt = checkForMessageStartingWith(msg);

        // TODO - add check the message found is actually a WARN, currently passes ANY level
        if (evt == null) {
            Assertions.fail("Looking for WARN message starting with \"" + msg + "\" and didn't find it");
        }
    }

    /**
     * Assert that a specific message, of any severity, has been logged.
     * White space is ignored.
     * Does not remove any other messages of any severity from the message queue.
     * <p>
     * Invokes a JUnit Assert if no matching message is found, but doesn't
     * require it to be the next message.
     * This allows use e.g. for debug-severity messages.
     *
     * @param msg the message to assert exists
     */
    public static void assertMessage(String msg) {
        Iterator<LogEvent> iterator = list.iterator();
        LogEvent lastEvent = null;
        while (iterator.hasNext()) {
            lastEvent = iterator.next();
            if (compare(lastEvent, msg) ) {
                iterator.remove();
                return;
            }
        }
        Assertions.fail("No message present for ANY logging level: \"" + msg + "\""
            + ( lastEvent==null ? "" : " last was " + lastEvent.getLevel().name()
            + " \"" + lastEvent.getMessage().getFormattedMessage() + "\"" ) );
    }

    /**
     * Compare two message strings, handling nulls and ignoring whitespace.
     *
     * @param e1 the event
     * @param s2 the string to compare e1 to
     * @return true if message in e1 equals s2; false otherwise
     */
    protected static boolean compare(@CheckForNull LogEvent e1, @CheckForNull String s2) {
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
    protected static boolean compareStartsWith(@CheckForNull LogEvent e1, String s2) {
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
