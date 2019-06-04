package jmri.util.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.colorchooser.AbstractColorChooserPanel;

/**
 * Create a custom color chooser panel.
 * The panel contains two button grids.
 * The left grid contains the 13 standard Java colors.
 * The right grid contains recently used colors.
 *
 * @author Dave Sand Copyright (C) 2018
 * @since 4.13.1
 */
public class JmriColorChooserPanel extends AbstractColorChooserPanel {

    private Color[] colors = {Color.black, Color.darkGray, Color.gray,
       Color.lightGray, Color.white, Color.red, Color.pink, Color.orange,
       Color.yellow, Color.green, Color.blue, Color.magenta, Color.cyan,
       jmri.util.ColorUtil.BROWN};
    private int numColors = 14; //number of entries in the above array
    private JPanel recentPanel = new JPanel(new GridBagLayout());

    @Override
    public void updateChooser(){
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = java.awt.GridBagConstraints.WEST;
        recentPanel.removeAll();

        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        int cols = Math.max(3, (int) Math.ceil((double)colors.size() / 7));
        int idx = 0;
        for (Color recent : colors) {
            c.gridx = idx % cols;
            c.gridy = idx / cols;
            recentPanel.add(createColorButton(recent, false), c);
            idx++;
        }
    }

    @Override
    protected void buildChooser(){
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JPanel stdColors = new JPanel(new GridBagLayout());
        stdColors.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("StandardColorLabel")));  // NOI18N
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = java.awt.GridBagConstraints.WEST;
        for (int i = 0; i < numColors; i++) {
            c.gridx = i % 2;
            c.gridy = i / 2;
            stdColors.add(createColorButton(colors[i], true), c);
        }
        add(stdColors);
        stdColors.setVisible(true);

        recentPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RecentColorLabel")));  // NOI18N
        ArrayList<Color> colors = JmriColorChooser.getRecentColors();
        int cols = Math.max(3, (int) Math.ceil((double)colors.size() / 7));
        int idx = 0;
        for (Color recent : colors) {
            c.gridx = idx % cols;
            c.gridy = idx / cols;
            recentPanel.add(createColorButton(recent, false), c);
            idx++;
        }
        add(recentPanel);
        recentPanel.setVisible(true);
    }

    /**
     * Create a button that contains a color swatch and
     * the translated color name.  Use the RGB values if a name is not
     * available.
     * @param color The color object
     * @param stdcolor If true, the color name is used otherwise the RGB values.
     * @return a button item with the listener.
     */
    JButton createColorButton(Color color, boolean stdcolor) {
        BufferedImage image = new BufferedImage(40, 15,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        // fill it with its representative color
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
        g.fillRect(0, 0, 40, 15);
        // draw a black border around it
        g.setColor(Color.black);
        g.drawRect(0, 0, 40 - 1, 15 - 1);
        ImageIcon icon = new ImageIcon(image);

        String colorName = "";
        if (stdcolor) {
            colorName = jmri.util.ColorUtil.colorToLocalizedName(color);
        }
        String colorTip = String.format("r=%d, g=%d, b=%d, a=%d",
                color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());

        JButton colorButton = new JButton(colorName, icon);
        colorButton.setToolTipText(colorTip);
        colorButton.addActionListener((ActionEvent e) -> {
            getColorSelectionModel().setSelectedColor(color);
        });
        return colorButton;
    }

    @Override
    public String getDisplayName() {
        return "JMRI";  // NOI18N
    }

    @Override
    public Icon getSmallDisplayIcon(){
       return null;
    }

    @Override
    public Icon getLargeDisplayIcon(){
       return null;
    }
}
