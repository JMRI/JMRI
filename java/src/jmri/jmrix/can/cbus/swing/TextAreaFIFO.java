package jmri.jmrix.can.cbus.swing;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a new TextAreaFIFO.
 * Keeps message log windows to a reasonable length
 * https://community.oracle.com/thread/1373400
 */
public class TextAreaFIFO extends JTextArea implements DocumentListener {
    private int maxLines;
    public TextAreaFIFO(int lines) {
        maxLines = lines;
        getDocument().addDocumentListener( this );
    }
    public void insertUpdate(DocumentEvent e) {
        ThreadingUtil.runOnGUIEventually( ()->{
            removeLines();
        });
    }
    public void removeUpdate(DocumentEvent e) {}
    public void changedUpdate(DocumentEvent e) {}
    public void removeLines() {
        Element root = getDocument().getDefaultRootElement();
        while (root.getElementCount() > maxLines) {
            Element firstLine = root.getElement(0);
            try {
                getDocument().remove(0, firstLine.getEndOffset());
            } catch(BadLocationException ble) {
                log.error("bad location {}",ble);
            }
        }
        setCaretPosition( getDocument().getLength() );
    }
    public void dispose() {
        getDocument().removeDocumentListener( this );
    }
    private final static Logger log = LoggerFactory.getLogger(TextAreaFIFO.class);    
}
