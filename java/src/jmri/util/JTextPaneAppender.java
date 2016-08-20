package jmri.util;

import java.awt.Color;
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
 *
 * This code was copied from
 * "jakarta-log4j-1.2.15\apache-log4j-1.2.15\contribs\SvenReimers\gui\TextPaneAppender.java"
 * (which did not work properly, not even compile) and adapted for my needs.
 *
 * @author bender heri See 4/15/2009 Log4J email
 */
public class JTextPaneAppender extends AppenderSkeleton {

    /**
     *
     */
    JTextPane myTextPane;
    /**
     *
     */
    Hashtable<String, MutableAttributeSet> myAttributeSet;

    /**
     * Constructor
     *
     */
    public JTextPaneAppender(Layout aLayout, String aName, Filter[] aFilterArray, JTextPane aTextPane) {
        this();
        this.layout = aLayout;
        this.name = aName;
        myTextPane = aTextPane;

        if (aFilterArray != null) {
            for (int i = 0; i < aFilterArray.length; i++) {
                if (aFilterArray[i] != null) {
                    addFilter(aFilterArray[i]);
                } // if aFilterArray[i] != null]
            } // for i

        } // if aFilterArray != null

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
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    // original source had this marked as an over-ride, but it isn't
    //@Override
    public void close() {
        //
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
     * @see
     * org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public void append(LoggingEvent event) {
        if (myTextPane == null) {
            LogLog.warn("TextPane is not initialized");
            return;
        } // if myTextPane == null

        String text = this.layout.format(event);
        String[] stackTrace = event.getThrowableStrRep();
        if (stackTrace != null) {
            StringBuffer sb = new StringBuffer(text);

            for (int i = 0; i < stackTrace.length; i++) {
                sb.append("    ").append(stackTrace[i]).append("\n");
            } // for i

            text = sb.toString();
        }

        StyledDocument myDoc = myTextPane.getStyledDocument();

        try {
            myDoc.insertString(myDoc.getLength(), text, myAttributeSet.get(event.getLevel().toString()));
        } catch (BadLocationException badex) {
            System.err.println(badex);  // can't log this, as it would be recursive error
        }

        myTextPane.setCaretPosition(myDoc.getLength());
    }

    /**
     * Get current TextPane
     */
    public JTextPane getTextPane() {
        return myTextPane;
    }

    /**
     * Set current TextPane
     *
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
     * setColorEmerg
     *
     */
    public void setColorEmerg(Color color) {
        setColor(Level.FATAL, color);
    }

    /**
     * getColorEmerg
     */
    public Color getColorEmerg() {
        return getColor(Level.FATAL);
    }

    /**
     * setColorError
     *
     */
    public void setColorError(Color color) {
        setColor(Level.ERROR, color);
    }

    /**
     * getColorError
     */
    public Color getColorError() {
        return getColor(Level.ERROR);
    }

    /**
     * setColorWarn
     *
     */
    public void setColorWarn(Color color) {
        setColor(Level.WARN, color);
    }

    /**
     * getColorWarn
     */
    public Color getColorWarn() {
        return getColor(Level.WARN);
    }

    /**
     * setColorInfo
     *
     */
    public void setColorInfo(Color color) {
        setColor(Level.INFO, color);
    }

    /**
     * getColorInfo
     */
    public Color getColorInfo() {
        return getColor(Level.INFO);
    }

    /**
     * setColorDebug
     *
     */
    public void setColorDebug(Color color) {
        setColor(Level.DEBUG, color);
    }

    /**
     * getColorDebug
     */
    public Color getColorDebug() {
        return getColor(Level.DEBUG);
    }

    /**
     * Sets the font size of all Level's
     *
     */
    public void setFontSize(int aSize) {
        Enumeration<MutableAttributeSet> e = myAttributeSet.elements();
        while (e.hasMoreElements()) {
            StyleConstants.setFontSize(e.nextElement(), aSize);
        }
        return;
    }

    /**
     * Sets the font size of a particular Level
     *
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
     */
    public int getFontSize(Level aLevel) {
        AttributeSet attrSet = myAttributeSet.get(aLevel.toString());
        if (attrSet == null) {
            throw new IllegalArgumentException("Unhandled Level: " + aLevel.toString());
        } // if attrSet == null

        return StyleConstants.getFontSize(attrSet);
    }

    /**
     * Sets the font name of all known Level's
     *
     */
    public void setFontName(String aName) {
        Enumeration<MutableAttributeSet> e = myAttributeSet.elements();
        while (e.hasMoreElements()) {
            StyleConstants.setFontFamily(e.nextElement(), aName);
        }
        return;
    }

    /**
     * setFontName
     *
     */
    public void setFontName(String aName, Level aLevel) {
        MutableAttributeSet set = myAttributeSet.get(aLevel.toString());
        if (set != null) {
            StyleConstants.setFontFamily(set, aName);
        }
        return;
    }

    /**
     * Retrieves the font name of a particular Level
     *
     */
    public String getFontName(Level aLevel) {
        AttributeSet attrSet = myAttributeSet.get(aLevel.toString());

        if (attrSet == null) {
            throw new IllegalArgumentException("Unhandled Level: " + aLevel.toString());
        } // if attrSet == null

        return StyleConstants.getFontFamily(attrSet);
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    // original code had this marked as an override, but it isn't,
    // at least for Log4J 1.2.15
    //@Override
    public boolean requiresLayout() {
        return true;
    }
}
