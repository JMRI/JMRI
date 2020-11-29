package jmri.util.swing;

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
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        ThreadingUtil.runOnGUIEventually( ()->{
            removeLines();
        });
    }
    @Override
    public void removeUpdate(DocumentEvent e ) {
        ThreadingUtil.runOnGUIEventually ( ()->{
            removeLines();
        });        
    }
    @Override
    public void changedUpdate(DocumentEvent e) {
        ThreadingUtil.runOnGUIEventually( ()->{
            removeLines();
        });        
    }

    /**
     * Set whether the JTextArea should scroll to bottom on update
     *
     * @param newval  autoscrolls if true
     */
    public void setAutoScroll(@Nonnull Boolean newval) {
        _autoScroll = newval;
        if (_autoScroll) {
            ThreadingUtil.runOnGUIEventually( ()->{
                removeLines();
            });
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
        while (root.getElementCount() > ( _maxLines + 1 ) ) {
            Element firstLine = root.getElement(0);
            try {
                getDocument().remove(0, firstLine.getEndOffset());
            } catch(BadLocationException ble) {
                log.error("bad location {}",ble);
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
    private final static Logger log = LoggerFactory.getLogger(TextAreaFIFO.class);    
}
