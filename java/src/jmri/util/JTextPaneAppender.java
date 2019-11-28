package jmri.util;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Implements a log4j appender which writes to a swing JTextPane
 * <p>
 * This code was copied from
 * "jakarta-log4j-1.2.15\apache-log4j-1.2.15\contribs\SvenReimers\gui\TextPaneAppender.java"
 * (which did not work properly, not even compile) and adapted for my needs.
 *
 * @author bender heri See 4/15/2009 Log4J email
 */
public class JTextPaneAppender extends AppenderSkeleton {

    JTextPane myTextPane;
    Hashtable<String, MutableAttributeSet> myAttributeSet;

    /**
     * Constructor
     *
     * @param aLayout      the panel layout
     * @param aName        the panel name
     * @param aFilterArray a list of filters
     * @param aTextPane    the text pane to display
     */
    public JTextPaneAppender(Layout aLayout, String aName, Filter[] aFilterArray, JTextPane aTextPane) {
        this();
        this.layout = aLayout;
        this.name = aName;
        myTextPane = aTextPane;

        if (aFilterArray != null) {
            for (Filter aFilterArray1 : aFilterArray) {
                if (aFilterArray1 != null) {
                    addFilter(aFilterArray1);
                }
            }
        }
        createAttributes();
    }

    /**
     * Constructor
     *
     */
    public JTextPaneAppender() {
        super();
        createAttributes();
    }

    /**
     * {@inheritDoc}
     * @see org.apache.log4j.Appender#close()
     */
    @Override
    public void close() {
    }

    private void createAttributes() {
        String prio[] = new String[6];
        prio[0] = Level.FATAL.toString();
        prio[1] = Level.ERROR.toString();
        prio[2] = Level.WARN.toString();
        prio[3] = Level.INFO.toString();
        prio[4] = Level.DEBUG.toString();
        prio[5] = Level.TRACE.toString();

        myAttributeSet = new Hashtable<String, MutableAttributeSet>();

        for (int i = 0; i < prio.length; i++) {
            MutableAttributeSet att = new SimpleAttributeSet();
            myAttributeSet.put(prio[i], att);
            StyleConstants.setFontSize(att, 14);
        }

        StyleConstants.setForeground(myAttributeSet.get(Level.FATAL.toString()), Color.red);
        StyleConstants.setForeground(myAttributeSet.get(Level.ERROR.toString()), Color.red);
        StyleConstants.setForeground(myAttributeSet.get(Level.WARN.toString()), Color.orange);
        StyleConstants.setForeground(myAttributeSet.get(Level.INFO.toString()), Color.black);
        StyleConstants.setForeground(myAttributeSet.get(Level.DEBUG.toString()), Color.black);
        StyleConstants.setForeground(myAttributeSet.get(Level.TRACE.toString()), Color.black);
    }

    /**
     * {@inheritDoc}
     * @see
     * org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public void append(final LoggingEvent event) {
        if (myTextPane == null) {
            LogLog.warn("TextPane is not initialized");
            return;
        } // if myTextPane == null

        String temp = this.layout.format(event);
        String[] stackTrace = event.getThrowableStrRep();
        if (stackTrace != null) {
            StringBuffer sb = new StringBuffer(temp);

            for (int i = 0; i < stackTrace.length; i++) {
                sb.append("    ").append(stackTrace[i]).append("\n");
            } // for i

            temp = sb.toString();
        }
        final String text = temp;

        // The following can't be jmri.util.ThreadingUtil.runOnGUI(..) because
        // that's a recursive logging loop
        try {
            if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                logIt(text, event);
            } else {
                javax.swing.SwingUtilities.invokeAndWait(() -> {
                    logIt(text, event);
                }); 
            }
        } catch (InterruptedException e) {
            System.err.println("JTextPaneAppender interrupted while doing logging on GUI thread"); // can't log this, as it would be recursive error
            Thread.currentThread().interrupt();
        } catch (InvocationTargetException e) {
            System.err.println("JTextPaneAppender error while logging on GUI thread: "+e.getCause()); // can't log this, as it would be recursive error
        }
    }

    private void logIt(String text, final LoggingEvent event) {
        try {
            StyledDocument myDoc = myTextPane.getStyledDocument();
            myDoc.insertString(myDoc.getLength(), text, myAttributeSet.get(event.getLevel().toString()));
            myTextPane.setCaretPosition(myDoc.getLength());
        } catch (BadLocationException badex) {
            System.err.println(badex);  // can't log this, as it would be recursive error
        }
    }

    /**
     * Get current TextPane.
     *
     * @return the current text pane
     */
    public JTextPane getTextPane() {
        return myTextPane;
    }

    /**
     * Set current TextPane
     *
     * @param aTextpane the text pane to make current
     */
    public void setTextPane(JTextPane aTextpane) {
        myTextPane = aTextpane;
    }

    private void setColor(Level p, Color v) {
        StyleConstants.setForeground(myAttributeSet.get(p.toString()), v);
    }

    private Color getColor(Level p) {
        Color c = StyleConstants.getForeground(myAttributeSet.get(p.toString()));
        return c == null ? null : c;
    }

    // ///////////////////////////////////////////////////////////////////
    // option setters and getters
    /**
     * Set the emergency color.
     *
     * @param color the color for {@link Level#FATAL} messages
     */
    public void setColorEmerg(Color color) {
        setColor(Level.FATAL, color);
    }

    /**
     * Get the emergency color.
     *
     * @return the color for {@link Level#FATAL} messages
     */
    public Color getColorEmerg() {
        return getColor(Level.FATAL);
    }

    /**
     * Set the error color.
     *
     * @param color the color for {@link Level#ERROR} messages
     */
    public void setColorError(Color color) {
        setColor(Level.ERROR, color);
    }

    /**
     * Get the error color.
     *
     * @return the color for {@link Level#ERROR} messages
     */
    public Color getColorError() {
        return getColor(Level.ERROR);
    }

    /**
     * Set the warning color.
     *
     * @param color the color for {@link Level#WARN} messages
     */
    public void setColorWarn(Color color) {
        setColor(Level.WARN, color);
    }

    /**
     * Get the warning color.
     *
     * @return the color for {@link Level#WARN} messages
     */
    public Color getColorWarn() {
        return getColor(Level.WARN);
    }

    /**
     * Set the information color.
     *
     * @param color the color for {@link Level#INFO} messages
     */
    public void setColorInfo(Color color) {
        setColor(Level.INFO, color);
    }

    /**
     * Get the information color.
     *
     * @return the color for {@link Level#INFO} messages
     */
    public Color getColorInfo() {
        return getColor(Level.INFO);
    }

    /**
     * Set the debugging color.
     *
     * @param color the color for {@link Level#DEBUG} messages
     */
    public void setColorDebug(Color color) {
        setColor(Level.DEBUG, color);
    }

    /**
     * Get the debugging color.
     *
     * @return the color for {@link Level#DEBUG} messages
     */
    public Color getColorDebug() {
        return getColor(Level.DEBUG);
    }

    /**
     * Set the font size of all levels.
     *
     * @param aSize the font size
     */
    public void setFontSize(int aSize) {
        Enumeration<MutableAttributeSet> e = myAttributeSet.elements();
        while (e.hasMoreElements()) {
            StyleConstants.setFontSize(e.nextElement(), aSize);
        }
    }

    /**
     * Set the font size of a particular level.
     *
     * @param aSize  the font size
     * @param aLevel the level
     */
    public void setFontSize(int aSize, Level aLevel) {
        MutableAttributeSet set = myAttributeSet.get(aLevel.toString());
        if (set != null) {
            StyleConstants.setFontSize(set, aSize);
        } // if set != null
    }

    /**
     * Get the font size for a particular logging level
     *
     * @param aLevel the level
     * @return the font size
     */
    public int getFontSize(Level aLevel) {
        AttributeSet attrSet = myAttributeSet.get(aLevel.toString());
        if (attrSet == null) {
            throw new IllegalArgumentException("Unhandled Level: " + aLevel.toString());
        } // if attrSet == null

        return StyleConstants.getFontSize(attrSet);
    }

    /**
     * Set the font name of all known levels.
     *
     * @param aName the font name
     */
    public void setFontName(String aName) {
        Enumeration<MutableAttributeSet> e = myAttributeSet.elements();
        while (e.hasMoreElements()) {
            StyleConstants.setFontFamily(e.nextElement(), aName);
        }
    }

    /**
     * Set the font name for the given level.
     *
     * @param aName  the font name
     * @param aLevel the log level
     */
    public void setFontName(String aName, Level aLevel) {
        MutableAttributeSet set = myAttributeSet.get(aLevel.toString());
        if (set != null) {
            StyleConstants.setFontFamily(set, aName);
        }
    }

    /**
     * Get the font name of a particular Level.
     *
     * @param aLevel the level
     * @return the font name
     */
    public String getFontName(Level aLevel) {
        AttributeSet attrSet = myAttributeSet.get(aLevel.toString());

        if (attrSet == null) {
            throw new IllegalArgumentException("Unhandled Level: " + aLevel.toString());
        } // if attrSet == null

        return StyleConstants.getFontFamily(attrSet);
    }

    /**
     * {@inheritDoc}
     * @see org.apache.log4j.Appender#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }
}
