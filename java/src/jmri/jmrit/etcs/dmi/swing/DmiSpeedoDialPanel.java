package jmri.jmrit.etcs.dmi.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;

/**
 * Creates a JPanel containing an Dial type speedometer display.
 * <p>
 * Based on analogue clock frame by Dennis Miller
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 * @author Steve Young Copyright (C) 2023
 */
public class DmiSpeedoDialPanel extends JPanel {

    private float speed = 0;
    private float targetAdviceSpeed = -1; // unset
    private int maxSpeed = 140;
    private int majorSpeedGap = 20; // every 20 mph from 0

    private Color centreCircleAndDialColor = DmiPanel.GREY;
    private String displaySpeedUnit = "";

    private static final int DIAL_CENTRE_X = 140;
    private static final int DIAL_CENTRE_Y = 150;
    private static final float DEGREES_SPEED0_CLOCKWISE = 126;
    private static final int HOOK_DEPTH = 20;

    private static final int OUTER_CSG_RADIUS = 137;

    private final int[] pointerX = {-20,   -20,   -7,   -7,    7,    7,   20,  20};
    private final int[] pointerY = {-31,  -266, -314, -381, -381, -314, -266, -31};
    private final int[] scaledPointerX = new int[pointerX.length];
    private final int[] scaledPointerY = new int[pointerY.length];
    private final int[] rotatedPointerX = new int[pointerX.length];
    private final int[] rotatedPointerY = new int[pointerY.length];

    private Polygon scaledPointerHand;
    private final int pointerHeight;

    private final CopyOnWriteArrayList<DmiCircularSpeedGuideSection> csgSectionList;
    private final jmri.UserPreferencesManager p;

    public DmiSpeedoDialPanel() {
        super();

        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        setLayout(null);
        setOpaque(false);

        csgSectionList = new CopyOnWriteArrayList<>();

        // Create an unscaled pointer to get the original size (height)to use
        // in the scaling calculations
        Polygon pointerHand = new Polygon(pointerX, pointerY, pointerY.length);
        pointerHeight = pointerHand.getBounds().getSize().height;

        float scaleRatio = 250 / 2.6F / pointerHeight;
        for (int i = 0; i < pointerX.length; i++) {
            scaledPointerX[i] = (int) (pointerX[i] * scaleRatio);
            scaledPointerY[i] = (int) (pointerY[i] * scaleRatio);
        }
        scaledPointerHand = new Polygon(scaledPointerX, scaledPointerY, scaledPointerX.length);
        DmiSpeedoDialPanel.this.setMaxDialSpeed(140);
        DmiSpeedoDialPanel.this.update(0); // default to 0 speed
    }

    @Override
    public void paint(Graphics g) {
        if (!(g instanceof Graphics2D) ) {
              throw new IllegalArgumentException("Graphics object passed is not the correct type");
        }
        Graphics2D g2 = (Graphics2D) g;

        // coordinates of the centre of the dial in the panel.
        g2.translate(DIAL_CENTRE_X, DIAL_CENTRE_Y);

        RenderingHints  hints =new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHints(hints);

        drawTicksAndDigits(g2, 125);
        csgSectionList.forEach((var s) -> drawACsgSection(g2, s )); // after ticks

        drawTargetAdviceSpeed(g2);
        drawPointer(g2);
        drawCentreCircle(g2); // after Pointer so is on top

        drawSpeedUnit(g2);
    }

    private void drawHook(Graphics2D g2, DmiCircularSpeedGuideSection section){

        int hookWidth = 4;
        boolean miniMook = section.type==DmiCircularSpeedGuideSection.CSG_TYPE_SUPERVISION;
        float hookWidthAngle = DEGREES_SPEED0_CLOCKWISE - hookWidth;
        float angle = getSpeedAngle(section.stop);
        int hookOuterRadius = OUTER_CSG_RADIUS - (miniMook ? 6 : 0);
        int hookInnerRadius = OUTER_CSG_RADIUS - HOOK_DEPTH;
    
        g2.setColor(miniMook ? DmiPanel.YELLOW: section.col);
        Polygon polygon2 = new Polygon();
        polygon2.addPoint(dotX(hookOuterRadius, angle+DEGREES_SPEED0_CLOCKWISE), 
            dotY(hookOuterRadius, angle+DEGREES_SPEED0_CLOCKWISE));

        polygon2.addPoint(dotX(hookInnerRadius, angle+DEGREES_SPEED0_CLOCKWISE),
            dotY(hookInnerRadius, angle+DEGREES_SPEED0_CLOCKWISE));
        polygon2.addPoint(dotX(hookInnerRadius, angle+hookWidthAngle),
            dotY(hookInnerRadius, angle+hookWidthAngle));
        polygon2.addPoint(dotX(hookOuterRadius, angle+hookWidthAngle),
            dotY(hookOuterRadius, angle+hookWidthAngle));
        g2.fillPolygon(polygon2);
    }

    private void drawACsgSection(Graphics2D g, DmiCircularSpeedGuideSection section ){

        float startAngle = getSpeedAngle(section.start)+ DEGREES_SPEED0_CLOCKWISE;
        float endAngle = getSpeedAngle(section.stop) - getSpeedAngle(section.start);

        if ( section.includeNegative ) {
            startAngle -= 5;
            endAngle += 5;
        }

        log.debug("endAngle: {}", endAngle);

        int innerRadius = 9;
        int outerRadius = OUTER_CSG_RADIUS;
        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_SUPERVISION ){
            outerRadius = OUTER_CSG_RADIUS-5;
        }

        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL ){
            innerRadius = 9;
        } else if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_HOOK ){
            innerRadius = HOOK_DEPTH;
        }
        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_RELEASE ){ // outer edge, grey
            innerRadius = 5;
        }
        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_SUPERVISION ){ // inner edge, yellow
            innerRadius = 5;
        }

        // Create a buffered image for rendering
        int width = getWidth();
        int height = getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        RenderingHints hints =new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setRenderingHints(hints);
        g2d.translate(DIAL_CENTRE_X, DIAL_CENTRE_Y);

        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_NORMAL || section.type == DmiCircularSpeedGuideSection.CSG_TYPE_HOOK ) {
            drawAnArc(g2d, false, section.col,
                outerRadius, (int) startAngle, (int) endAngle);
            drawAnArc(g2d, true, section.col,
                outerRadius-innerRadius, (int) startAngle, (int) endAngle);
        }

        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_RELEASE ) {
            drawAnArc(g2d, false, DmiPanel.GREY,
                outerRadius, (int) startAngle, (int) endAngle);
            drawAnArc(g2d, false, DmiPanel.BACKGROUND_COLOUR,
                outerRadius-innerRadius+1, (int) startAngle, (int) endAngle);
            drawAnArc(g2d, true, section.col,
                outerRadius-innerRadius, (int) startAngle, (int) endAngle);
        }

        if ( section.type == DmiCircularSpeedGuideSection.CSG_TYPE_SUPERVISION ) {
            drawAnArc(g2d, false, DmiPanel.BACKGROUND_COLOUR,
                outerRadius, (int) startAngle, (int) endAngle);
            drawAnArc(g2d, false, DmiPanel.YELLOW,
                outerRadius-1, (int) startAngle, (int) endAngle);
            drawAnArc(g2d, true, section.col,
                outerRadius-innerRadius, (int) startAngle, (int) endAngle);
            drawHook(g, section);
        }

        // Draw the modified image onto the JPanel
        g.drawImage(image, -DIAL_CENTRE_X, -DIAL_CENTRE_Y, null);

        // Dispose of graphics objects
        g2d.dispose();

        if ( section.includeHook ) {
            drawHook(g, section);
        }
    }

    private void drawAnArc( Graphics2D g2, boolean transparent, Color color,
        int radius, int startAngle, int endAngle ) {

        if (transparent) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_ATOP, 0f));
        } else {
            g2.setColor(color);
        }

        g2.fillArc(
            -radius, -radius,
            2 * radius , 2 * radius,
            (- startAngle) + (transparent ? 2 : 0),
            (- endAngle)   - (transparent ? 4 : 0));
    }

    private void drawTicksAndDigits(Graphics2D g2, float halfFaceSize) {

        Font sizedFont = new Font(DmiPanel.FONT_NAME, Font.BOLD, 18);
        g2.setFont(sizedFont);
        FontMetrics fontM = g2.getFontMetrics(sizedFont);
        g2.setColor(Color.WHITE);
        for (int j = 0; j <= maxSpeed; j += 10) {
            float i = getSpeedAngle(j)+DEGREES_SPEED0_CLOCKWISE;
            log.debug("angle is {}", i);
            if (j >= 0 && (j % majorSpeedGap != 0)) {
                // minor tick every 10, excluding major ticks
                g2.drawLine(
                dotX(halfFaceSize, i), 
                dotY(halfFaceSize, i),
                dotX(halfFaceSize - 15, i), 
                dotY(halfFaceSize - 15, i));
            } else {
                // major tick with Speed String
                g2.drawLine(
                    dotX(halfFaceSize, i), 
                    dotY(halfFaceSize, i),
                    dotX(halfFaceSize - 25, i), 
                    dotY(halfFaceSize - 25, i));

                // ertms3.6 only draw big speeds over 250 in hundreds.
                // if ( majorSpeedGap > 20 && j > 251 && (j % 100) !=0) {

                // ertms4.0 only draw big speeds over 201 in hundreds.
                if ( majorSpeedGap > 20 && j > 201 && (j % 100) !=0) {
                    continue;
                }

                String dashSpeed = Integer.toString( j);
                int xOffset = fontM.stringWidth(dashSpeed);
                int yOffset = fontM.getHeight();
                g2.drawString(dashSpeed,
                    dotX(halfFaceSize - 40, i ) - xOffset / 2,
                    dotY(halfFaceSize - 40, i ) + yOffset / 4);
            }
        }
    }

    private void drawCentreCircle(Graphics2D g2) {

        // create centre circle
        g2.setColor(centreCircleAndDialColor);
        int dotSize = 20;
        g2.fillOval(-dotSize, -dotSize, dotSize *2, dotSize*2);

        // display the speed value in centre of the centre circle
        String speedString = getSpeedString(speed);
        Font digitsSizedFont = new Font(DmiPanel.FONT_NAME, Font.BOLD, 22);
        g2.setFont(digitsSizedFont);
        g2.setColor( centreCircleAndDialColor==DmiPanel.RED ? Color.WHITE : Color.BLACK);
        FontMetrics digitsFontM = g2.getFontMetrics(digitsSizedFont);
        if ( p.getSimplePreferenceState(DmiPanel.PROPERTY_CENTRE_TEXT) ) {
            g2.drawString(speedString, -digitsFontM.stringWidth(speedString) / 2 , 7);
        } else { // right-align
            g2.drawString(speedString, 18-digitsFontM.stringWidth(speedString) , 7);
        }
    }

    /**
     * Get a String for the Speed value.
     * Speeds are displayed to nearest whole number.
     * If the speed is non-zero, the minimum displayable speed is 1 .
     * @param speed the loco speed.
     * @return formatted String.
     */
    private String getSpeedString(float speed) {
        if (speed > 0f) { // round to nearest whole number
            return String.valueOf(Math.max(1, Math.round(speed)));
        } else {
            return "0";
        }
    }

    private void drawPointer(Graphics2D g2) {
        float speedAngle = getSpeedAngle(speed);
        double speedAngleRadians = Math.toRadians(speedAngle -144);
        for (int i = 0; i < scaledPointerX.length; i++) {
            rotatedPointerX[i] = (int) (scaledPointerX[i] * Math.cos(speedAngleRadians)
                    - scaledPointerY[i] * Math.sin(speedAngleRadians));
            rotatedPointerY[i] = (int) (scaledPointerX[i] * Math.sin(speedAngleRadians)
                    + scaledPointerY[i] * Math.cos(speedAngleRadians));
        }
        scaledPointerHand = new Polygon(rotatedPointerX, rotatedPointerY, rotatedPointerX.length);
        g2.setColor(centreCircleAndDialColor);
        g2.fillPolygon(scaledPointerHand);
    }

    private void drawSpeedUnit(Graphics2D g2){
        if (displaySpeedUnit.isBlank()) {
            return;
        }
        Font unitsSizedFont = new Font(DmiPanel.FONT_NAME, Font.BOLD, 16);
        g2.setFont(unitsSizedFont);
        g2.setColor(DmiPanel.GREY);
        FontMetrics unitsFontM = g2.getFontMetrics(unitsSizedFont);
        g2.drawString(displaySpeedUnit,  - unitsFontM.stringWidth(displaySpeedUnit) / 2, 50);
    }

    private void drawTargetAdviceSpeed(Graphics2D g2){
        log.debug("targetAS {}", targetAdviceSpeed);
        if ( targetAdviceSpeed < 0){
            return;
        }
        g2.setColor(DmiPanel.MEDIUM_GREY);
        float i = getSpeedAngle(targetAdviceSpeed)+DEGREES_SPEED0_CLOCKWISE;
        g2.fillOval(dotX(111, i )-5, dotY(111, i)-5, 10, 10);
    }

    /**
     * Get the Angle from speed 0 at 0 degrees.
     * Maximum speed always at 288 degrees.
     * Maximum speeds above 299 are scaled at 50% from speed 200 upwards.
     * @param speed to locate angle for.
     * @return Angle for the speed
     */
    private float getSpeedAngle( float speed ){
        float angleForSpeed1 = 288f / maxSpeed;
        if ( maxSpeed >299 ){
            angleForSpeed1 *= (maxSpeed/( 200f+((maxSpeed-200f)/2)));
            if ( speed >= 200 ) {
                return angleForSpeed1 * 200f + angleForSpeed1 * (speed-200f) / 2f;
            }
        }
        return angleForSpeed1 * speed;
    }

    // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
    private int dotX(float radius, float angle) {
        return (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
    }

    // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
    private int dotY(float radius, float angle) {
        return (int)Math.round(radius * Math.sin(Math.toRadians(angle)));
    }

    protected void update(float speed) {
        if (speed > maxSpeed) {
            log.debug("add code here to move scale up");
        }
        this.speed = speed;
        repaint();
    }

    protected void setDisplaySpeedUnit(String newVal){
        displaySpeedUnit = newVal;
        repaint();
    }

    protected void setMaxDialSpeed(int newSpd){
        maxSpeed = newSpd;
        majorSpeedGap = ( newSpd > 251 ? 50 : 20 );
        repaint();
    }

    protected void setCentreCircleAndDialColor(Color newColor ) {
        centreCircleAndDialColor = newColor;
    }

    protected void setCsgSections(List<DmiCircularSpeedGuideSection> list){
        csgSectionList.clear();
        csgSectionList.addAll(list);
        repaint();
    }

    protected void setTargetAdviceSpeed(int newVal){
        targetAdviceSpeed = newVal;
        repaint();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiSpeedoDialPanel.class);

}
