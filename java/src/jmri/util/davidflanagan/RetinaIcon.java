package jmri.util.davidflanagan;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;

public class RetinaIcon implements Icon {
    private final Image image;
    private final double retinaScale;

    public RetinaIcon(Image image, double retinaScale) {
        this.image = image;
        this.retinaScale = retinaScale;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Draw the larger image into the smaller space
        g.drawImage(image, x, y, (int)(image.getWidth(null) / retinaScale), (int)(image.getHeight(null) / retinaScale), c);
    }

    @Override
    public int getIconWidth() { return (int)(image.getWidth(null) / retinaScale); }

    @Override
    public int getIconHeight() { return (int)(image.getHeight(null) / retinaScale); }
}
