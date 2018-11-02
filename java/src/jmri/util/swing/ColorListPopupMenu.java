package jmri.util.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

/**
 * Popup menu for displaying recently selected colors along with standard
 * java colors.
 *
 * @author Paul Bender Copyright (C) 2018
 * @since 4.13.1
 */
public class ColorListPopupMenu extends JPopupMenu {

    private static final int ICON_DIMENSION = 20;

    // Standard Colors
    private Color[] colorCode = {Color.black, Color.darkGray, Color.gray,
       Color.lightGray, Color.white, Color.red, Color.pink, Color.orange,
       Color.yellow, Color.green, Color.blue, Color.magenta, Color.cyan,
       jmri.util.ColorUtil.BROWN};
    private int numColors = 14; //number of entries in the above arrays
    private ColorSelectionModel model;

    public ColorListPopupMenu(ColorSelectionModel m){
        super();
        model = m;
        addRecentColors();
        addSeparator();
        addStandardColors();
    }

    private void addRecentColors(){
        // build the menu.
        add(new JLabel(Bundle.getMessage("RecentColorLabel")));
        for (Color color : JmriColorChooser.getRecentColors()) {
            add(createMenuItem(color, false));
        }
    }

    private void addStandardColors(){
        // build the menu.
        add(new JLabel(Bundle.getMessage("StandardColorLabel")));
        for (int i = 0; i < numColors; i++) {
            add(createMenuItem(colorCode[i], true));
        }
    }

    private JMenuItem createMenuItem(Color swatchColor, boolean isStdColor){
        // update the Swatch to have the right color showing.
        BufferedImage image = new BufferedImage(ICON_DIMENSION, ICON_DIMENSION,
             BufferedImage.TYPE_INT_ARGB);

        Graphics g = image.getGraphics();
        // fill it with its representative color
        g.setColor(new Color(swatchColor.getRed(), swatchColor.getGreen(), swatchColor.getBlue(), swatchColor.getAlpha()));
        g.fillRect(0, 0, ICON_DIMENSION, ICON_DIMENSION);
        // draw a black border around it
        g.setColor(Color.black);
        g.drawRect(0, 0, ICON_DIMENSION - 1, ICON_DIMENSION - 1);

        ImageIcon icon = new ImageIcon(image);

        g.dispose();

        String colorName = "";
        String colorTip = String.format("r=%d, g=%d, b=%d, a=%d",
                swatchColor.getRed(), swatchColor.getGreen(), swatchColor.getBlue(), swatchColor.getAlpha());
        if (isStdColor) {
            colorName = jmri.util.ColorUtil.colorToLocalizedName(swatchColor);
        } else {
            colorName = colorTip;
        }

        JMenuItem colorMenuItem = new JMenuItem(colorName,icon);
        if (isStdColor) colorMenuItem.setToolTipText(colorTip);
        colorMenuItem.addActionListener((ActionEvent e) -> {
           model.setSelectedColor(swatchColor);
        });
        return colorMenuItem;
    }

}
