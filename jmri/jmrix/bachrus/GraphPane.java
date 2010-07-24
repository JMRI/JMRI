// GraphPane.java

package jmri.jmrix.bachrus;

import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * Frame for graph of loco speed curveSpeed curve
 *
 * @author			Andrew Crosland   Copyright (C) 2010
 * @version			$Revision: 1.1 $
 */
public class GraphPane extends JPanel {
    final int PAD = 40;

    protected String xLabel;
    protected String yLabel;
    protected dccSpeedProfile _sp;

    // Use a default 28 step profile
    public GraphPane() {
        super();
        _sp = new dccSpeedProfile(28);
    }

    public GraphPane(dccSpeedProfile sp) {
        super();
        _sp = sp;
    }

    public void setXLabel (String s) { xLabel = s; }
    public void setYLabel (String s) { yLabel = s; }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        // Draw ordinate.
        g2.draw(new Line2D.Double(PAD, PAD, PAD, h-PAD));
        // Draw abcissa.
        g2.draw(new Line2D.Double(PAD, h-PAD, w-PAD, h-PAD));
        // Draw labels.
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("0", frc);
        float sh = lm.getAscent() + lm.getDescent();
        // Ordinate label.
        float sy = PAD + ((h - 2*PAD) - yLabel.length()*sh)/2 + lm.getAscent();
        g2.setPaint(Color.green.darker());
        for(int i = 0; i < yLabel.length(); i++) {
            String letter = String.valueOf(yLabel.charAt(i));
            float sw = (float)font.getStringBounds(letter, frc).getWidth();
            float sx = (PAD/2 - sw)/2;
            g2.drawString(letter, sx, sy);
            sy += sh;
        }
        // Abcissa label.
        sy = h - PAD/2 + (PAD/2 - sh)/2 + lm.getAscent();
        float sw = (float)font.getStringBounds(xLabel, frc).getWidth();
        float sx = (w - sw)/2;
        g2.setPaint(Color.red);
        g2.drawString(xLabel, sx, sy);
        // The space between values along the abcissa.
        float xInc = (float)(w - 2*PAD)/(_sp.getLength()-1);
        
        // Draw lines.
        float scale = (float)(h - 2*PAD)/_sp.getMax();
        g2.setPaint(Color.green.darker());
        for(int i = 0; i < _sp.getLength()-1; i++) {
            float x1 = PAD + i*xInc;
            float y1 = h - PAD - scale*_sp.getPoint(i);
            float x2 = PAD + (i+1)*xInc;
            float y2 = h - PAD - scale*_sp.getPoint(i+1);
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
            // tick marks along abcissa
            g2.draw(new Line2D.Double(x1, h-7*PAD/8, x1, h-PAD));
        }
        // Mark data points.
        g2.setPaint(Color.red);
        for(int i = 0; i < _sp.getLength(); i++) {
            float x = PAD + i*xInc;
            float y = h - PAD - scale*_sp.getPoint(i);
            g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));
        }
    }
}