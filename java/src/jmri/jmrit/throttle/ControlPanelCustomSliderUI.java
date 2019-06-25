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
    private final static Color TRACK_COLOR_BACK = new Color(0x88, 0x8a, 0x85, 0x88);
    private final static Color TRACK_COLOR_FRONT = new Color(0xf5, 0x79, 0x00, 0xCC);
//    private final static Color TRACK_COLOR_TICKS = new Color(0x888a85);
    private final static Color THUMB_INNER_COLOR_STOP = new Color(0xcc0000);
    private final static Color THUMB_INNER_COLOR_RUN = new Color(0xd7d27A);
    private final static Color THUMB_CONTOUR_COLOR = new Color(0x555753);
//    private final static Color ZERO_CONTOUR_COLOR = new Color(0x555753);    
//    private final static int ZERO_THICKNESS = 5;
    
    public ControlPanelCustomSliderUI(JSlider b) {
        super(b);
    }
    
    @Override
    public void paint(Graphics g, JComponent c) {
        if (g instanceof Graphics2D) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            super.paint(g, c);
        }
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
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g;
        Paint oldPaint = g2d.getPaint();
        // Track back rectangle
        g2d.setPaint(TRACK_COLOR_BACK);
        g2d.fillRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
/*        // Track ticks 
        g2d.setPaint(TRACK_COLOR_TICKS);
        // only if it fits 
        if (slider.getOrientation() == SwingConstants.VERTICAL && trackRect.height > (slider.getMaximum()-slider.getMinimum())*2) {
            for (int n = 0 ; n<slider.getMaximum()-slider.getMinimum() ; n++) { 
                g2d.drawLine( trackRect.x, 
                        trackRect.y+trackRect.height - n*trackRect.height / (slider.getMaximum()-slider.getMinimum()), 
                        trackRect.x+trackRect.width, 
                        trackRect.y+trackRect.height - n*trackRect.height / (slider.getMaximum()-slider.getMinimum()));
            }
        } else if (slider.getOrientation() == SwingConstants.HORIZONTAL && trackRect.width > (slider.getMaximum()-slider.getMinimum())*2) {
            for (int n = 0 ; n<slider.getMaximum()-slider.getMinimum() ; n++) { 
                g2d.drawLine( trackRect.x + n*trackRect.width / (slider.getMaximum()-slider.getMinimum()), 
                        trackRect.y, 
                        trackRect.x + n*trackRect.width / (slider.getMaximum()-slider.getMinimum()), 
                        trackRect.y+trackRect.height );
            }            
        }*/
        // Track front
        g2d.setPaint(TRACK_COLOR_FRONT);        
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
                // Zero marker
//                g2d.setPaint(ZERO_CONTOUR_COLOR);
//                g2d.fillRect( (int)Math.round(x0- ZERO_THICKNESS/2) , trackRect.y+ZERO_THICKNESS/2, ZERO_THICKNESS, trackRect.height-ZERO_THICKNESS );     
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
                // Zero marker
//                g2d.setPaint(ZERO_CONTOUR_COLOR);
//                g2d.fillRect( trackRect.x+ZERO_THICKNESS/2, (int)Math.round(y0- ZERO_THICKNESS/2), trackRect.width-ZERO_THICKNESS, ZERO_THICKNESS );     
                
            } else {
                g2d.fillRect(trackRect.x, thumbRect.y+thumbRect.height/2, trackRect.width, trackRect.height - thumbRect.y+thumbRect.height/2 - trackRect.y  );
            }
        }
        g2d.setPaint(oldPaint);
    }

    @Override
    public void paintThumb(Graphics g) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
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
            g2d.setPaint( new Color(THUMB_INNER_COLOR_RUN.getRed() - Math.abs(slider.getValue())*100/slider.getMaximum(),THUMB_INNER_COLOR_RUN.getGreen(), THUMB_INNER_COLOR_RUN.getBlue() - Math.abs(slider.getValue()*100/slider.getMaximum()) ));
        }
        g2d.fill(shape);
        Stroke old = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2f));
        g2d.setPaint(THUMB_CONTOUR_COLOR);
        g2d.draw(shape);
        g2d.setStroke(old);
    }      
}