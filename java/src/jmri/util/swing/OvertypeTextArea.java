package jmri.util.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A JTextField where the Insert key switches operation to and from
 * overwrite mode.  In overwrite mode, the cursor is a line under the
 * next character that will be replaced by typing.
 * 
 * @see <a href="https:coderanch.com/t/742171/java/Fixing-JTextComponent-modelToView-deprecations">original source</a>
 */ 
public class OvertypeTextArea extends JTextField {

    private static boolean isOvertypeMode;

    private Caret defaultCaret;
    private Caret overtypeCaret;

    public OvertypeTextArea(int length) {
        super(length);
        setCaretColor( Color.red );
        defaultCaret = getCaret();
        overtypeCaret = new OvertypeCaret();
        overtypeCaret.setBlinkRate( defaultCaret.getBlinkRate() );
        setOvertypeMode( false );  // fields start in regular `insert` mode

        addFocusListener(new FocusListener() {
            // Install a listener that will set the visible cursor to the 
            // correct type when entering a field.
            
            // With Java 11 on Mac, the first time there's a change of 
            // focus with isOvertypeMode true can cause an NPE in the L&F at:
            //      com.apple.laf.AquaCaret.focusGained(AquaCaret.java:104)
            // This is not present in Java 17, nor nn other platforms.
            // The exception is benign to the extent that the operations still work.
            @Override
            public void focusGained(FocusEvent e) {
                setOvertypeMode(isOvertypeMode()); // set caret
            }

            @Override public void focusLost(FocusEvent e) {}
        });
    }
    
    /*
     *  Return the overtype/insert mode
     */
    public boolean isOvertypeMode() {
        return OvertypeTextArea.isOvertypeMode;
    }

    /*
     *  Set the caret to use depending on overtype/insert mode
     */
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    public void setOvertypeMode(boolean isOvertypeModeArg) {
        OvertypeTextArea.isOvertypeMode = isOvertypeModeArg;
        int pos = getCaretPosition();

        if ( isOvertypeMode() ) {
            setCaret( overtypeCaret );
        } else {
            setCaret( defaultCaret );
        }

        setCaretPosition( pos );
    }

    /*
     *  Override method from JComponent to do insert vs overwrite
     */
    @Override
    public void replaceSelection(String text) {
        //  Implement overtype mode by selecting the character at the current
        //  caret position

        if ( isOvertypeMode() ) {
            int pos = getCaretPosition();

            if (getSelectedText() == null
                    &&  pos < getDocument().getLength()) {
                moveCaretPosition( pos + 1);
            }
        }

        super.replaceSelection(text);
    }

    /*
     *  Override method from JComponent to check for INSERT key and handle
     */
    @Override
    protected void processKeyEvent(KeyEvent e) {
        super.processKeyEvent(e);

        //  Handle release of Insert key to toggle overtype/insert mode

        // The Mac apparently cannot provide a VK_INSERT, even if 
        // the keyboard has a key labelled `insert`.  There's no
        // consensus on a replacement key or key sequence either.
        // As a result, this probably won't work on macOS.
        if (e.getID() == KeyEvent.KEY_RELEASED
                    &&  e.getKeyCode() == KeyEvent.VK_INSERT) {
            setOvertypeMode( ! isOvertypeMode() );
        }
    }

    /*
     *  Paint a horizontal line the width of a column and 1 pixel high
     */
    private static class OvertypeCaret extends DefaultCaret {
        /*
         *  The overtype caret will simply be a horizontal line one pixel high
         *  (once we determine where to paint it)
         */
        @SuppressWarnings("deprecation") // TextUI#modelToView replaced by modelToView2D
        @Override
        public void paint(Graphics g) {
            if (isVisible()) {
                try {
                    JTextComponent component = getComponent();
                    TextUI mapper = component.getUI();
                    var r = mapper.modelToView(component, getDot());
                    g.setColor(component.getCaretColor());
                    // ((Graphics2D) g).setStroke(new BasicStroke(2));
                    int width = g.getFontMetrics().charWidth( 'w' );
                    int y = r.y + r.height - 2;
                    g.drawLine(r.x, y, r.x + width - 2, y);
                }
                catch (BadLocationException e) {}
            }
        }

        /*
         *  Damage must be overridden whenever the paint method is overridden
         *  (The damaged area is the area the caret is painted in. We must
         *  consider the area for the default caret and this caret)
         */
        @Override
        protected synchronized void damage(Rectangle r) {
            if (r != null) {
                JTextComponent component = getComponent();
                x = r.x;
                y = r.y;
                width = component.getFontMetrics( component.getFont() ).charWidth( 'w' );
                height = r.height;
                repaint();
            }
        }
    }
}
