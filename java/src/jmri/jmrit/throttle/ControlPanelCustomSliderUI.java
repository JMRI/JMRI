package jmri.jmrit.throttle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;


public class ControlPanelCustomSliderUI extends BasicSliderUI {
    
    // Color are coming from the Tango themes color palette (as well as icons on the Throttle window, for look consistency)
    private final static Color SLIDER_COLOR_BACK = new Color(0x88, 0x8a, 0x85, 0x88);
    private final static Color SLIDER_COLOR_FRONT = new Color(0xf57900);
    private final static Color THUMB_INNER_COLOR_STOP = new Color(0xcc0000);
    private final static Color THUMB_INNER_COLOR_RUN = new Color(0x4e0206);
    private final static Color THUMB_CONTOUR_COLOR = new Color(0xd3d7cf);
    private final static Color ZERO_INNER_COLOR = new Color(0x2e3436);
    private final static Color ZERO_CONTOUR_COLOR = new Color(0x555753);    
    private final static int ZERO_THICKNESS = 4;
    
    public ControlPanelCustomSliderUI(JSlider b) {
        super(b);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g, c);
    }

    @Override
    protected Dimension getThumbSize() {
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            return new Dimension(16, contentRect.height - tickRect.height - labelRect.height -trackBuffer*2);
        } else {
            return new Dimension(contentRect.width  - tickRect.width - labelRect.width - trackBuffer*2, 16);
        }
    }

    @Override
    public void paintTrack(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Paint oldPaint = g2d.getPaint();
        g2d.setPaint(SLIDER_COLOR_BACK);
        g2d.fillRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
        g2d.setPaint(SLIDER_COLOR_FRONT);
        if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
            if (slider.getMinimum()<0 && slider.getMaximum()>0) {
                double doublerel0Pos = Math.abs((double)slider.getMinimum()) / ((double)slider.getMaximum() - (double)slider.getMinimum());
                double x0 = trackRect.x + trackRect.width * doublerel0Pos;
                double widthRect = thumbRect.x + (double)thumbRect.width/2 - x0;
                if (widthRect>0) {
                    g2d.fillRect( (int)Math.round(x0), trackRect.y, (int)Math.round(widthRect), trackRect.height);
                } else {
                    g2d.fillRect( (int)Math.round(x0+widthRect), trackRect.y, (int)Math.round(-widthRect), trackRect.height);
                }
                paintZeroH(g, (int)Math.round(x0), trackRect.y );
            } else {           
                g2d.fillRect(trackRect.x, trackRect.y, thumbRect.x-thumbRect.width/2, trackRect.height);
            }
        } else {
            if (slider.getMinimum()<0 && slider.getMaximum()>0) {
                double doublerel0Pos = Math.abs((double)slider.getMaximum()) / ((double)slider.getMaximum() - (double)slider.getMinimum());
                double y0 = trackRect.y + trackRect.height * doublerel0Pos ;
                double heightRect = thumbRect.y + (double)thumbRect.height/2 - y0;
                if (heightRect>0) {
                    g2d.fillRect( trackRect.x, (int)Math.round(y0), trackRect.width, (int)Math.round(heightRect));
                } else {
                    g2d.fillRect( trackRect.x, (int)Math.round(y0+heightRect), trackRect.width, (int)Math.round(-heightRect));
                }
                paintZeroV(g, trackRect.x, (int)Math.round(y0) );
            } else {
                g2d.fillRect(trackRect.x, thumbRect.y+thumbRect.height/2, trackRect.width, trackRect.height - thumbRect.y+thumbRect.height/2 - trackRect.y  );
            }
        }
        g2d.setPaint(oldPaint);
    }
    
    private void paintZeroH(Graphics g, int x, int y) { 
        Graphics2D g2d = (Graphics2D) g;
        int x1 = x - ZERO_THICKNESS/2 ;
        int x2 = x + ZERO_THICKNESS/2 ;
        GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        shape.moveTo(x1, y+1);
        shape.lineTo(x2, y+1);
        shape.lineTo(x2, y+thumbRect.height-1);
        shape.lineTo(x1, y+thumbRect.height-1);
        shape.closePath();
        g2d.setPaint(ZERO_INNER_COLOR);
        g2d.fill(shape);
        Stroke old = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2f));
        g2d.setPaint(ZERO_CONTOUR_COLOR);
        g2d.draw(shape);
        g2d.setStroke(old);        
    }
    
    private void paintZeroV(Graphics g, int x, int y) { 
        Graphics2D g2d = (Graphics2D) g;
        int y1 = y - ZERO_THICKNESS/2 ;
        int y2 = y + ZERO_THICKNESS/2 ;
        GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        shape.moveTo(x+1, y1);
        shape.lineTo(x+1, y2);
        shape.lineTo(x+thumbRect.width-1, y2);
        shape.lineTo(x+thumbRect.width-1, y1);
        shape.closePath();
        g2d.setPaint(ZERO_INNER_COLOR);
        g2d.fill(shape);
        Stroke old = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2f));
        g2d.setPaint(ZERO_CONTOUR_COLOR);
        g2d.draw(shape);
        g2d.setStroke(old);          
    }

    @Override
    public void paintThumb(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        int x1 = thumbRect.x + 2;
        int x2 = thumbRect.x + thumbRect.width - 2;
        GeneralPath shape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        shape.moveTo(x1, thumbRect.y+1);        
        shape.lineTo(x2, thumbRect.y+1);
        shape.lineTo(x2, thumbRect.y+thumbRect.height-2);
        shape.lineTo(x1, thumbRect.y+thumbRect.height-2);
        shape.closePath();
        if (slider.getValue()==0) {
            g2d.setPaint(THUMB_INNER_COLOR_STOP);
        } else {             
            g2d.setPaint(  new Color(THUMB_INNER_COLOR_RUN.getRed(),THUMB_INNER_COLOR_RUN.getGreen() + Math.abs(slider.getValue()), THUMB_INNER_COLOR_RUN.getBlue()) );
        }
        g2d.fill(shape);
        Stroke old = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2f));
        g2d.setPaint(THUMB_CONTOUR_COLOR);
        g2d.draw(shape);
        g2d.setStroke(old);
    }
}