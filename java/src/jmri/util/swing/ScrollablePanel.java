package jmri.util.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * JPanel extension to handle the Scrollable interface so it
 * can behave well in a vertical JScrollPane
 *
 * @author Bob Jacobsen Copyright 2018
 * @since 4.13.2
 */
public class ScrollablePanel extends JPanel implements Scrollable {
        private final int increment;  
        
        public ScrollablePanel(int increment) { this.increment = increment;}
        public ScrollablePanel() { 
            this.increment = 16; // just a convenient default
        }
        
        /** {@inheritDoc} */
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            //tell the JScrollPane that we want to be our 'preferredSize'
            // but later, we'll say that vertically, it should scroll.
            return super.getPreferredSize(); 
        }

        /** {@inheritDoc} */
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return increment;
        }

        /** {@inheritDoc} */
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return increment;
        }

        /** {@inheritDoc} */
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; //track the width, and re-size as needed.
        }

        /** {@inheritDoc} */
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; //we don't want to track the height, because we want to scroll vertically.
        }

}