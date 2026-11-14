package jmri.util.swing;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new TextAreaFIFO, an extended JTextArea
 * Keeps message log windows to a reasonable length
 * Scrolls down to last line of textarea by default
 * <p>
 * Lines are trimmed from the top in blocks, once there are a tenth more than
 * the maximum, rather than one at a time: removing from the top of a long text
 * costs time in proportion to the whole text, so trimming a line for each line
 * added would make every line cost that.
 * <p>
 * Originally based on https://community.oracle.com/thread/1373400
 * Modified for JMRI by Steve Young (c) 2018
 *
 */
public class TextAreaFIFO extends JTextArea implements DocumentListener {
    private int _maxLines;
    private Boolean _autoScroll;

    /**
     * Add text to the console
     *
     * @param lines number of lines
     */
    public TextAreaFIFO(int lines) {
        _maxLines = lines;
        _autoScroll = true;
        getDocument().addDocumentListener( this );
    }

    private final AtomicBoolean trimPending = new AtomicBoolean(false);

    @Override
    public void insertUpdate(DocumentEvent e) {
        trimLater();
    }
    @Override
    public void removeUpdate(DocumentEvent e ) {
        // removing text never makes it too long
    }
    @Override
    public void changedUpdate(DocumentEvent e) {
        // attribute changes don't add lines
    }

    /**
     * Trim and scroll on the GUI thread, once for all the text added
     * meanwhile.
     */
    private void trimLater() {
        if (trimPending.compareAndSet(false, true)) {
            ThreadingUtil.runOnGUIEventually( ()->{
                trimPending.set(false);
                removeLines();
            });
        }
    }

    /**
     * Set whether the JTextArea should scroll to bottom on update
     *
     * @param newval  autoscrolls if true
     */
    public void setAutoScroll(@Nonnull Boolean newval) {
        _autoScroll = newval;
        if (_autoScroll) {
            trimLater();
        }
    }

    /**
     * Edit maximum lines in JTextArea before trimming from top
     *
     * @param newval  Number of lines
     */
    public void setMaxLines(int newval){
        _maxLines = newval;
    }


    /**
     * Main internal method to trim from top, then if needed, move scroll position
     *
     */
    private void removeLines() {
        Element root = getDocument().getDefaultRootElement();
        // the last element is the line being added to
        int excess = root.getElementCount() - ( _maxLines + 1 );
        if (excess > _maxLines / 10) { // a tenth over the maximum, or one line over a small one
            try {
                getDocument().remove(0, root.getElement(excess - 1).getEndOffset());
            } catch(BadLocationException ble) {
                log.error("bad location",ble);
            }
        }
        if ( _autoScroll ) {
             setCaretPosition( getDocument().getLength() );
        }
    }

    /**
     * Removes document listener
     *
     */
    public void dispose() {
        getDocument().removeDocumentListener( this );
    }
    private static final Logger log = LoggerFactory.getLogger(TextAreaFIFO.class);
}
