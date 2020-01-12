package jmri.jmrix.bachrus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for graph of loco speed curves
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 */
public class GraphPane extends JPanel implements Printable {

    final int PAD = 40;

    protected String xLabel;
    protected String yLabel;
    // array to hold the speed curves
    protected DccSpeedProfile[] _sp;
    protected String annotate;
    protected Color[] colors = {Color.RED, Color.BLUE, Color.BLACK};

    protected boolean _grid = false;

    // Use a default 28 step profile
    public GraphPane() {
        super();
        _sp = new DccSpeedProfile[1];
        _sp[0] = new DccSpeedProfile(28);
    }

    public GraphPane(DccSpeedProfile sp) {
        super();
        _sp = new DccSpeedProfile[1];
        _sp[0] = sp;
    }

    public GraphPane(DccSpeedProfile sp0, DccSpeedProfile sp1) {
        super();
        _sp = new DccSpeedProfile[2];
        _sp[0] = sp0;
        _sp[1] = sp1;
    }

    public GraphPane(DccSpeedProfile sp0, DccSpeedProfile sp1, DccSpeedProfile ref) {
        super();
        _sp = new DccSpeedProfile[3];
        _sp[0] = sp0;
        _sp[1] = sp1;
        _sp[2] = ref;
    }

    public void setXLabel(String s) {
        xLabel = s;
    }

    public void setYLabel(String s) {
        yLabel = s;
    }

    public void showGrid(boolean b) {
        _grid = b;
    }

    int units = Speed.MPH;
//    String unitString = "Speed (MPH)";

    void setUnitsMph() {
        units = Speed.MPH;
        setYLabel(Bundle.getMessage("SpeedMPH"));
    }

    void setUnitsKph() {
        units = Speed.KPH;
        setYLabel(Bundle.getMessage("SpeedKPH"));
    }

    public int getUnits() {
        return units;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGraph(g);
    }

    protected void drawGraph(Graphics g) {
        if (!(g instanceof Graphics2D) ) {
              throw new IllegalArgumentException("Graphics object passed is not the correct type");
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();

        // Draw ordinate (y-axis).
        g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));
        // Draw abcissa (x-axis).
        g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));

        // Draw labels.
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("0", frc);

        float dash1[] = {1.0f};
        BasicStroke dashed = new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                10.0f, dash1, 0.0f);
        BasicStroke plain = new BasicStroke(1.0f);

        float sh = lm.getAscent() + lm.getDescent();
        // Ordinate (y-axis) label.
        float sy = PAD + ((h - 2 * PAD) - yLabel.length() * sh) / 2 + lm.getAscent();
        g2.setPaint(Color.green.darker());
        for (int i = 0; i < yLabel.length(); i++) {
            String letter = String.valueOf(yLabel.charAt(i));
            float sw = (float) font.getStringBounds(letter, frc).getWidth();
            float sx = (PAD / 2 - sw) / 2;
            g2.drawString(letter, sx, sy);
            sy += sh;
        }
        // Abcissa (x-axis) label.
        sy = h - PAD / 2 + (PAD / 2 - sh) / 2 + lm.getAscent();
        float sw = (float) font.getStringBounds(xLabel, frc).getWidth();
        float sx = (w - sw) / 2;
        g2.drawString(xLabel, sx, sy);

        // find the maximum of all profiles
        float maxSpeed = 0;
        for (int i = 0; i < _sp.length; i++) {
            maxSpeed = Math.max(_sp[i].getMax(), maxSpeed);
        }

        // Used to scale values into drawing area
        float scale = (h - 2 * PAD) / maxSpeed;
        // space between values along the ordinate (y-axis)
        // start with an increment of 1
        // Plot a grid line every two
        // Plot a label every ten
        float yInc = scale;
        int yMod = 10;
        int gridMod = 2;
        if (units == Speed.MPH) {
            // need inverse transform here
            yInc = Speed.mphToKph(yInc);
        }
        if ((units == Speed.KPH) && (maxSpeed > 100)
                || (units == Speed.MPH) && (maxSpeed > 160)) {
            log.debug("Adjusting Y axis spacing for max speed");
            yMod *= 2;
            gridMod *= 2;
        }
        String ordString;
        // Draw lines
        for (int i = 0; i <= (h - 2 * PAD) / yInc; i++) {
            g2.setPaint(Color.green.darker());
            g2.setStroke(plain);
            float y1 = h - PAD - i * yInc;
            if ((i % yMod) == 0) {
                g2.draw(new Line2D.Double(7 * PAD / 8, y1, PAD, y1));
                ordString = Integer.toString(i);
                sw = (float) font.getStringBounds(ordString, frc).getWidth();
                sx = 7 * PAD / 8 - sw;
                sy = y1 + lm.getAscent() / 2;
                g2.drawString(ordString, sx, sy);
            }
            if (_grid && (i > 0) && ((i % gridMod) == 0)) {
                // Horizontal grid lines
                g2.setPaint(Color.LIGHT_GRAY);
                if ((i % yMod) != 0) {
                    g2.setStroke(dashed);
                }
                g2.draw(new Line2D.Double(PAD, y1, w - PAD, y1));
            }
        }
        if (_grid) {
            // Close the top
            g2.setPaint(Color.LIGHT_GRAY);
            g2.setStroke(dashed);
            g2.draw(new Line2D.Double(PAD, PAD, w - PAD, PAD));
        }

        // The space between values along the abcissa (x-axis).
        float xInc = (float) (w - 2 * PAD) / (_sp[0].getLength() - 1);
        String abString;
        // Draw lines between data points.
        // for each point in a profile
        for (int i = 0; i < _sp[0].getLength(); i++) {
            g2.setPaint(Color.green.darker());
            g2.setStroke(plain);
            float x1 = 0.0F;
            // for each profile in the array
            for (int j = 0; j < _sp.length; j++) {
                x1 = PAD + i * xInc;
                float y1 = h - PAD - scale * _sp[j].getPoint(i);
                float x2 = PAD + (i + 1) * xInc;
                float y2 = h - PAD - scale * _sp[j].getPoint(i + 1);
                // if it's a valid data point
                if (i <= _sp[j].getLast() - 1) {
                    g2.draw(new Line2D.Double(x1, y1, x2, y2));
                }
            }
            // tick marks along abcissa
            g2.draw(new Line2D.Double(x1, h - 7 * PAD / 8, x1, h - PAD));
            if (((i % 5) == 0) || (i == _sp[0].getLength() - 1)) {
                // abcissa labels every 5 ticks
                abString = Integer.toString(i);
                sw = (float) font.getStringBounds(abString, frc).getWidth();
                sx = x1 - sw / 2;
                sy = h - PAD + (PAD / 2 - sh) / 2 + lm.getAscent();
                g2.drawString(abString, sx, sy);
            }
            if (_grid && (i > 0)) {
                // Verical grid line
                g2.setPaint(Color.LIGHT_GRAY);
                if ((i % 5) != 0) {
                    g2.setStroke(dashed);
                }
                g2.draw(new Line2D.Double(x1, PAD, x1, h - PAD));
            }
        }
        g2.setStroke(plain);

        // Mark data points.
        // for each point in a profile
        for (int i = 0; i <= _sp[0].getLength(); i++) {
            // for each profile in the array
            for (int j = 0; j < _sp.length; j++) {
                g2.setPaint(colors[j]);
                float x = PAD + i * xInc;
                float y = h - PAD - scale * _sp[j].getPoint(i);
                // if it's a valid data point
                if (i <= _sp[j].getLast()) {
                    g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
                }
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws
            PrinterException {

        if (page > 0) { /* We have only one page, and 'page' is zero-based */

            return Printable.NO_SUCH_PAGE;
        }

        if (!(g instanceof Graphics2D) ) {
              throw new IllegalArgumentException("Graphics object passed is not the correct type");
        }
           
        Graphics2D g2 = (Graphics2D) g;
        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping.
         */
        g2.translate(pf.getImageableX(), pf.getImageableY());

        // Scale to fit the width and height if neccessary
        double scale = 1.0;
        if (this.getWidth() > pf.getImageableWidth()) {
            scale *= pf.getImageableWidth() / this.getWidth();
        }
        if (this.getHeight() > pf.getImageableHeight()) {
            scale *= pf.getImageableHeight() / this.getHeight();
        }
        g2.scale(scale, scale);

        // Draw the graph
        drawGraph(g);

        // Add annotation
        g2.setPaint(Color.BLACK);
        g2.drawString(annotate, 0, Math.round(this.getHeight() + 2 * PAD * scale));

        /* tell the caller that this page is part of the printed document */
        return Printable.PAGE_EXISTS;
    }

    public void printProfile(String s) {
        annotate = s;
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                log.error("Exception whilst printing profile " + ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(GraphPane.class);
}
