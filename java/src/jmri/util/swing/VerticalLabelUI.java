package jmri.util.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

/**
 * Allows a JLabel to be displayed vertically, with a defined orientation. Usage
 * (for a vertical label with anti-clockwise orientation):
 * <code>
 * <br>JLabel label = new JLabel("Vertical Label");
 * <br>label.setUI(new VerticalLabelUI(VerticalLabelUI.ANTICLOCKWISE));
 * </code>
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2010
 */
// commented out in AudioBufferFrame and used in scripts, so do not deprecate
// without checking those sources first even though IDE find usages tools show
// no use outside unit tests
public class VerticalLabelUI extends BasicLabelUI {

    /**
     * Define Clockwise rotation (+90 degrees from horizontal)
     */
    public static final int CLOCKWISE = 1;

    /**
     * Define Anti-Clockwise rotation (-90 degrees from horizontal)
     */
    public static final int ANTICLOCKWISE = 2;

    /**
     * Variable to determine rotation direction
     */
    private int rotation;

    /**
     * Static variables used to compute bounding rectangles for each constituent
     * part of the VerticalLabel
     */
    private final Rectangle iconRectangle = new Rectangle();
    private final Rectangle textRectangle = new Rectangle();
    private final Rectangle viewRectangle = new Rectangle();
    private Insets viewInsets = new Insets(0, 0, 0, 0);

    /**
     * Default constructor which provides a vertical label with anti-clockwise
     * orientation
     */
    public VerticalLabelUI() {
        this(ANTICLOCKWISE);
    }

    /**
     * Constructor used to provide a vertical label of the specified orientation
     *
     * @param rotation defines the rotation:
     * <br>{@link #CLOCKWISE} or
     * <br>{@link #ANTICLOCKWISE}
     */
    public VerticalLabelUI(int rotation) {
        super();
        this.rotation = rotation;
    }

    @Override
    public Dimension getPreferredSize(JComponent component) {
        Dimension dimension = super.getPreferredSize(component);
        return new Dimension(dimension.height, dimension.width);
    }

    @Override
    public void paint(Graphics graphics, JComponent component) {

        // Retrieve the text and icon of the label being rotated
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            String text = label.getText();
            Icon icon = (label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();

            // If both the icon and text are empty, nothing to be done
            if ((icon == null) && (text == null)) {
                return;
            }

            // Retrieve the font redering informaton
            FontMetrics fontMetrics = graphics.getFontMetrics();

            // Determine required insets for the view
            viewInsets = component.getInsets(viewInsets);

            // Initialise view origin
            viewRectangle.x = viewInsets.left;
            viewRectangle.y = viewInsets.top;

            // Determine view height and width
            // (inverting width and height as rotation not yet performed)
            viewRectangle.height = component.getWidth() - (viewInsets.left + viewInsets.right);
            viewRectangle.width = component.getHeight() - (viewInsets.top + viewInsets.bottom);

            // Initialise the icon and text bounding boxes
            iconRectangle.x = 0;
            iconRectangle.y = 0;
            iconRectangle.width = 0;
            iconRectangle.height = 0;
            textRectangle.x = 0;
            textRectangle.y = 0;
            textRectangle.width = 0;
            textRectangle.height = 0;

            // Grab the string to display
            String clippedText
                    = layoutCL(label, fontMetrics, text, icon, viewRectangle, iconRectangle, textRectangle);

            // Store the current transform prior to rotation
            AffineTransform transform = null;
            if (graphics instanceof Graphics2D) {
                transform = ((Graphics2D) graphics).getTransform();

                // Perform the rotation
                ((Graphics2D) graphics).rotate((this.rotation == CLOCKWISE ? 1 : -1) * (Math.PI / 2));
                ((Graphics2D) graphics).translate(
                        this.rotation == CLOCKWISE ? 0 : -component.getHeight(),
                        this.rotation == CLOCKWISE ? -component.getWidth() : 0);
            }

            // If necessary, paint the icon
            if (icon != null) {
                icon.paintIcon(component, graphics, iconRectangle.x, iconRectangle.y);
            }

            // If necessary, paint the text
            if (text != null) {
                int textX = textRectangle.x;
                int textY = textRectangle.y + fontMetrics.getAscent();

                if (label.isEnabled()) {
                    paintEnabledText(label, graphics, clippedText, textX, textY);
                } else {
                    paintDisabledText(label, graphics, clippedText, textX, textY);
                }
            }

            if (graphics instanceof Graphics2D && transform != null) {
                // Finally, restore the original transform
                ((Graphics2D) graphics).setTransform(transform);
            }
        }
    }

    //private static final Logger log = LoggerFactory.getLogger(VerticalLabelUI.class);
}
