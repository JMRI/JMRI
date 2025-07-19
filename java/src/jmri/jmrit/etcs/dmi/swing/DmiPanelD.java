package jmri.jmrit.etcs.dmi.swing;

import jmri.jmrit.etcs.TrackCondition;
import jmri.jmrit.etcs.ResourceUtil;
import jmri.jmrit.etcs.TrackSection;
import jmri.jmrit.etcs.MovementAuthority;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * Class to demonstrate features of ERTMS DMI Panel D, the Planning Area.
 * @author Steve Young Copyright (C) 2024
 */
public class DmiPanelD extends JPanel {

    private static final Color PASP_DARK = new Color(33,49,74);
    private static final Color PASP_LIGHT = new Color(41,74,107);

    private final DmiPanel dmiPanel;
    private final JPanel trackAheadFreeQuestion;
    private final JPanel planningPanel;

    private final JButton plusButton;
    private final JButton minusButton;

    private static final int[] scaleLineYPx = new int[]{284,283,250,206,182,164,150,149,107,64,21,20};
    private static final boolean[] scaleLineLight = new boolean[]{true, true, false, false, false, false,
        true, true, false, false, true, true};

    private static final int[] scaleDistanceBase = new int[]{0, 125, 250, 500, 1000};
    private static final int[] scaleycords = new int[]{287, 155, 111, 68, 25};
    private static final int[] scales = new int[]{ 1, 2, 4, 8, 16, 32};

    private final List<MovementAuthority> maList = new ArrayList<>();
    private boolean loopGradientLimitReached = false;
    private int nextAdviceChange = -1;
    private int indicationDistance = -1;
    private int indicationSpeedChange = -1;

    private static final BufferedImage speedDownImage = ResourceUtil.getTransparentImage("PL_22.bmp");
    private static final BufferedImage speedDownImageTargetIndication = ResourceUtil.getTransparentImage("PL_23.bmp");
    private static final BufferedImage speedDownImageTargetIndicationAto = ResourceUtil.getTransparentImage("PL_37.bmp");
    private static final BufferedImage speedUpImage = ResourceUtil.getTransparentImage("PL_21.bmp");

    private int currentScale = 0;

    public DmiPanelD(@Nonnull DmiPanel mainPanel){
        super();
        setLayout(null);
        dmiPanel = mainPanel;
        trackAheadFreeQuestion = this.trackAheadFreeQuestionPanel();

        plusButton = new TransparentButton( true );
        minusButton = new TransparentButton( false );

        setButtonsToState(); // set initial state

        planningPanel = getPlanningPanel();
        planningPanel.setLayout(null);

        setBackground(DmiPanel.BACKGROUND_COLOUR);
        setBounds(334, 15, 246, 300);

        add(trackAheadFreeQuestion);
        add(planningPanel);

        DmiPanelD.this.setTrackAheadFreeQuestionVisible(false);
    }

    private JPanel getPlanningPanel(){
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                if (trackAheadFreeQuestion.isVisible() || maList.isEmpty()  ) {
                    plusButton.setVisible(false);
                    minusButton.setVisible(false);
                    return;
                }
                if (!(g instanceof Graphics2D) ) {
                    throw new IllegalArgumentException("Graphics object passed is not the correct type");
                }
                Graphics2D g2 = (Graphics2D) g;

                RenderingHints  hints =new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHints(hints);
                
                drawBackground(g2);
                drawScale(g2);
                loopGradientLimitReached = false;
                drawGradientBar(g2);
                drawSpeedChanges(g2);
                drawAnnouncementsAndOrders(g2);
                drawNextAdviceChange(g2);
                drawIndicationMarkerLine(g2);
            }
        };
        p.setLayout(null);
        p.setBounds(0, 0, 246, 300); // includes top and bottom margins
        p.setOpaque(false);

        p.add(plusButton);
        p.add(minusButton);

        return p;
    }

    private void drawAnnouncementsAndOrders( Graphics2D g2 ) {
        int metresInPreviousSections = 0;
        int nextColumn = 2;
        Comparator<TrackCondition> lengthComparator = Comparator.comparingInt(TrackCondition::getDistanceFromStart);
        for (MovementAuthority ma : maList ) {
            for ( TrackSection section : ma.getTrackSections() ) {
                List<TrackCondition> l = section.getAnnouncements();
                Collections.sort(l, lengthComparator );
                for ( TrackCondition da : l) {
                    log.debug("found trackCondition {}", da );
                    int distance = metresInPreviousSections+da.getDistanceFromStart();
                    // no need to render all of the icons
                    if ( distance <= ( scaleDistanceBase[4]*scales[currentScale] *1.5 ) ) {
                        nextColumn = ensureSlotNumber(da, nextColumn);
                        int startPx = ( calculatePositionOnScale(distance));
                        log.debug("marker dist:{} px:{}", distance, startPx);
                        log.debug("drawing image {} at x:{} y:{} ",
                            da.getSmlImage(), getAnnouncementColumnPx(da.getColumnNum()),265-startPx);
                        g2.drawImage(da.getSmlImage(), getAnnouncementColumnPx(da.getColumnNum()), 265-startPx, this);
                    }
                } 
                metresInPreviousSections += section.getLength();
            }
        }
    }

    private static int getAnnouncementColumnPx(int column){
        switch (column) {
            case 1:
                return 42;
            case 2:
                return 67;
            case 3:
                return 92;
            default:
                throw new IllegalArgumentException();
        }
    }

    private static int ensureSlotNumber(TrackCondition da, int nextColumn){
        int tempCol = nextColumn;
        if ( da.getColumnNum() == 0 ) {
            da.setColumnNum(tempCol);
        } else {
            tempCol = da.getColumnNum();
        }
        tempCol++;
        if ( tempCol == 4 ) {
            tempCol = 1;
        }
        return tempCol;
    }

    private void drawSpeedChanges( Graphics2D g2 ) {

        List<TrackSection> speedChangeList = MovementAuthority.getTrackSectionList(maList, true);
        speedChangeList.forEach(ts -> log.debug("track section speed {} length {}", ts.getSpeed(), ts.getLength()));

        Font unitsSizedFont = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 14);
        g2.setFont(unitsSizedFont);
        g2.setColor(DmiPanel.GREY);

        int loopMetres = 0;
        int stopPx = 0;
        boolean increaseDisplayed = false;
        int reductionsDisplayed = 0;

        for (int i = 0; i < speedChangeList.size(); i++){
            int startPx = calculatePositionOnScale(loopMetres);
            loopMetres += speedChangeList.get(i).getLength();
            stopPx = calculatePositionOnScale(loopMetres);
            if ( i == 0 ){
                continue;
            }
            int speed = speedChangeList.get(i).getSpeed();
            int change = speed - speedChangeList.get(i-1).getSpeed();

            if ( change < 0 ) {
                g2.setColor(getIndicationColor(i));
                if ( !increaseDisplayed && reductionsDisplayed < 4 ) {
                    g2.drawImage(getIndicationImg(i), 136, 280-startPx, this);
                    g2.drawString(String.valueOf(speed), 155, 295-startPx);
                    reductionsDisplayed++;
                }
            } else {
                g2.setColor(DmiPanel.GREY);
                g2.drawImage(speedUpImage, 136, 266-startPx, this);
                g2.drawString(String.valueOf(speed), 155, 281-startPx);
                // TODO check in v4 - increaseDisplayed rule 8.3.7.9 clarification
                // increaseDisplayed = true;
            }
        }

        g2.setColor(getIndicationColor(0));
        // add 0 stop speed marker
        g2.drawImage(getIndicationImg(0), 136, 281-stopPx, this);
        g2.drawString(String.valueOf("0"), 155, 295-stopPx);
    }

    private BufferedImage getIndicationImg(int order){
        if ( indicationSpeedChange != order ) {
            return speedDownImage;
        }
        if ( dmiPanel.getMode() == DmiPanel.MODE_AUTOMATIC_DRIVING ){
            return DmiPanelD.speedDownImageTargetIndicationAto;
        } else {
            return DmiPanelD.speedDownImageTargetIndication;
        }
    }

    private Color getIndicationColor(int order){
        if ( indicationSpeedChange != order ) {
            return DmiPanel.GREY;
        }
        if ( dmiPanel.getMode() == DmiPanel.MODE_AUTOMATIC_DRIVING ){
            return DmiPanel.WHITE;
        } else {
            return DmiPanel.YELLOW;
        }
    }

    private void drawBackground( Graphics2D g2 ) {
        g2.setColor( PASP_DARK );
        g2.fillRect(147, 0, 99, 270+15);

        List<TrackSection> speedChangeList = MovementAuthority.getTrackSectionList(maList, true);
        speedChangeList.forEach(ts -> log.debug("track section speed {} length {}", ts.getSpeed(), ts.getLength()));

        int loopSpeedPlanningMetresFromStart = 0;
        int loopWidth = 4;

        for (int i = 0; i < speedChangeList.size(); i++){
            int startPx = calculatePositionOnScale(loopSpeedPlanningMetresFromStart);
            loopSpeedPlanningMetresFromStart += speedChangeList.get(i).getLength();
            int stopPx = calculatePositionOnScale(loopSpeedPlanningMetresFromStart);

            if (speedChangeList.get(i) != speedChangeList.get(0)){
                double percentageOfFirstSection = (double)
                    speedChangeList.get(i).getSpeed() / speedChangeList.get(0).getSpeed() * 100;
                if (percentageOfFirstSection < 50 ){
                    loopWidth = Math.min(loopWidth, 1);
                } else if (percentageOfFirstSection < 75 ){
                    loopWidth = Math.min(loopWidth, 2);
                } else if ( percentageOfFirstSection < 100 ){
                    loopWidth = Math.min(loopWidth, 3);
                }
            }

            int w = (94 * loopWidth / 4);
            log.debug("draw rect y:{} width:{} height:{}", 282-stopPx, w, stopPx-startPx );
            g2.setColor( PASP_LIGHT );
            g2.fillRect(147, 282-stopPx, w, stopPx-startPx);
        }
    }

    private void drawScale(Graphics2D g2){
        for ( int i = 0; i< scaleLineYPx.length; i++ ){
            g2.setColor(scaleLineLight[i] ? DmiPanel.MEDIUM_GREY: DmiPanel.DARK_GREY );
            g2.drawLine(40, scaleLineYPx[i], 240, scaleLineYPx[i]);
        }
        Font unitsSizedFont = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 12);
        g2.setFont(unitsSizedFont);
        FontMetrics unitsFontM = g2.getFontMetrics(unitsSizedFont);
        g2.setColor(DmiPanel.MEDIUM_GREY );

        for ( int i = 0; i< scaleDistanceBase.length; i++ ){
            String s = String.valueOf(scaleDistanceBase[i]*(scales[currentScale]));
            int width = 38- unitsFontM.stringWidth(s);
            g2.drawString(s, width, scaleycords[i]);
        }

        plusButton.setVisible(true);
        minusButton.setVisible(true);
    }

    private void drawNextAdviceChange(Graphics2D g2) {
        if ( nextAdviceChange < 0 ) {
            return;
        }
        g2.setColor(DmiPanel.GREY);
        int startPx = 282 - calculatePositionOnScale(nextAdviceChange);

        g2.fillRect(147, startPx, 10, 2);
        g2.fillRect(167, startPx, 10, 2);
        g2.fillRect(187, startPx, 10, 2);
        g2.fillRect(207, startPx, 10, 2);
        g2.fillRect(227, startPx, 10, 2);
    }

    private void drawIndicationMarkerLine(Graphics2D g2) {
        if ( indicationDistance < 0 ) {
            return;
        }
        g2.setColor(dmiPanel.getMode() == DmiPanel.MODE_AUTOMATIC_DRIVING
            ? DmiPanel.WHITE : DmiPanel.YELLOW);
        int startPx = 282 - calculatePositionOnScale(indicationDistance);
        g2.fillRect(147, startPx, 93, 2);
    }

    // Calculate the position on the scale for a given length in meters
    private int calculatePositionOnScale(int lengthInMeters) {

        int linearScaleMaxDistance = (scaleDistanceBase[1]*(scales[currentScale])/5);
        double logScaleMaxDistance = (scaleDistanceBase[4]*(scales[currentScale]));
        final int TOTAL_PIXELS = 262;
        final long FIRST_LINEAR_SCALE_PIXELS = 33;
        long logScaleWidthInPixels = TOTAL_PIXELS-FIRST_LINEAR_SCALE_PIXELS;

        int position;
        
        if ( lengthInMeters <= linearScaleMaxDistance ) {
            position = (int)((lengthInMeters) /  (float)linearScaleMaxDistance * FIRST_LINEAR_SCALE_PIXELS );
        } else {
            // Logarithmic scale for lengths beyond 100 meters
            double logScaleFactor = logScaleWidthInPixels / (Math.log(logScaleMaxDistance)
                - Math.log(linearScaleMaxDistance));
            position = (int)(FIRST_LINEAR_SCALE_PIXELS + (Math.log(lengthInMeters)
                - Math.log(linearScaleMaxDistance)) * logScaleFactor);
        }
        log.debug("at distance {} px: {}",lengthInMeters,position);
        return position;
    }

    protected void extendMovementAuthorities( MovementAuthority a ){
        maList.add(a);
        repaint();
    }

    protected void resetMovementAuthorities( final List<MovementAuthority> a ){
        maList.clear();
        maList.addAll(a);
        repaint();
    }

    /**
     * Get Unmodifiable List of Movement Authorities.
     * @return List of movement Authorities.
     */
    protected List<MovementAuthority> getMovementAuthorities() {
        return Collections.unmodifiableList(maList);
    }

    protected void advance(int distance) {
        MovementAuthority.advanceForward(maList, distance);
        nextAdviceChange -= distance;
        repaint();
    }

    private void drawGradientBar( Graphics2D g2 ){
        List<TrackSection> gradientList = MovementAuthority.getTrackSectionList(maList, false);
        // gradientList.forEach(ts -> log.debug("track section gradient {} length {}", ts.getGradient(), ts.getLength()));

        int drawingMetresFromStart = 0;
        for ( TrackSection gradientTs: gradientList) {

            int startMetres = drawingMetresFromStart;
            int endMetres = drawingMetresFromStart + gradientTs.getLength();

            int startPx = calculatePositionOnScale(startMetres);
            int stopPx = calculatePositionOnScale(endMetres);

            g2.setColor( gradientTs.getGradient() < 0 ? DmiPanel.DARK_GREY : DmiPanel.GREY);
            g2.fillRect(116, 283-stopPx, 18-1, stopPx-startPx-2);

            g2.setColor( gradientTs.getGradient() < 0 ? DmiPanel.GREY : DmiPanel.WHITE);
            g2.drawLine(115+1, 283-stopPx, 115+17-1, 283-stopPx);
            g2.drawLine(115, 283-startPx, 115, 283-stopPx);

            g2.setColor(DmiPanel.BLACK);
            g2.drawLine(115, 283-startPx-1, 115+17, 283-startPx-1);

            drawIconsOnGradient(g2, gradientTs, startPx, stopPx);
            drawingMetresFromStart = endMetres;
        }
    }

    private void drawIconsOnGradient( Graphics2D g2, TrackSection gradientTs, int startPx, int stopPx ){
        if (loopGradientLimitReached) {
            return;
        }

        int stopHeight = 283-stopPx+12;
        if ( stopHeight < 0 ){
            stopHeight = 12;
            loopGradientLimitReached = true;
        }

        int usableHeight = stopPx-startPx-4;
        if ( usableHeight > 14 ) {

            Font unitsSizedFont = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 13);
            g2.setFont(unitsSizedFont);
            FontMetrics unitsFontM = g2.getFontMetrics(unitsSizedFont);

            g2.setColor(gradientTs.getGradient()<0 ? DmiPanel.WHITE : DmiPanel.BLACK );

            String toDraw = gradientTs.getGradient()<0 ? "   -   " : "   +   ";
            int width = 124 - (unitsFontM.stringWidth(toDraw)/2);
            log.debug("stopHeight:{}",stopHeight);
            g2.drawString(toDraw,width, stopHeight);

            if (usableHeight> 20 ) {
                g2.drawString(toDraw,width, 283-startPx-2);
            }

            if (usableHeight> 30 ) {

                unitsSizedFont = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 12);
                g2.setFont(unitsSizedFont);
                unitsFontM = g2.getFontMetrics(unitsSizedFont);

                toDraw = String.valueOf(gradientTs.getGradient()).replace(String.valueOf("-"), "");
                width = 124 - (unitsFontM.stringWidth(toDraw)/2);
                int centre = (startPx + stopPx)/2;
                int drawHeight = 283-centre+4;
                g2.drawString(toDraw,width, drawHeight);
            }
        }
    }

    private void setButtonsToState(){
        plusButton.setEnabled(currentScale != 0);
        minusButton.setEnabled(currentScale != 5);
    }

    /**
     * Set the Scale on the Planning Area.
     * 0 : 0 - 1000
     * 1 : 0 - 2000
     * 2 : 0 - 4000
     * 3 : 0 - 8000
     * 4 : 0 - 16000
     * 5 : 0 - 32000
     * @param scale the scale to use.
     */
    protected void setScale(int scale){
        currentScale = scale;
        setButtonsToState();
        repaint();
    }

    protected void setTrackAheadFreeQuestionVisible(boolean newVal) {
        trackAheadFreeQuestion.setVisible(newVal);
    }

    protected void setNextAdviceChange(int distance) {
        nextAdviceChange = distance;
        repaint();
    }

    // Section 8.3.8
    protected void setIndicationMarkerLine(int distance, int whichSpeedChange ) {
        indicationDistance = distance;
        indicationSpeedChange = whichSpeedChange;
        repaint();
    }

    private JPanel trackAheadFreeQuestionPanel() {
        JPanel p = new JPanel();
        p.setLayout(null);
        p.setBounds(0,50,246,50);
        p.setBackground(DmiPanel.DARK_GREY);
        JLabel trackAheadFreeQuestionLogo = new JLabel(
        ResourceUtil.getImageIcon( "DR_02.bmp")
        );

        trackAheadFreeQuestionLogo.setBounds(0,0,162,50);
        trackAheadFreeQuestionLogo.setBackground(DmiPanel.DARK_GREY);
        trackAheadFreeQuestionLogo.setBorder(javax.swing.BorderFactory.createLineBorder(DmiPanel.MEDIUM_GREY, 1));

        p.add(trackAheadFreeQuestionLogo);

        JButton trackAheadFreeButton = new JButton(Bundle.getMessage("ButtonYes"));
        trackAheadFreeButton.setFocusable(false);
        trackAheadFreeButton.setBounds(162, 0, 246-162, 50);
        trackAheadFreeButton.setBackground(DmiPanel.MEDIUM_GREY);
        trackAheadFreeButton.setForeground(DmiPanel.BLACK);
        trackAheadFreeButton.setActionCommand(DmiPanel.PROP_CHANGE_TRACK_AHEAD_FREE_TRUE);
        trackAheadFreeButton.addActionListener(this::buttonClicked);

        p.add(trackAheadFreeButton);
        return p;
    }

    void buttonClicked(ActionEvent e){
        dmiPanel.firePropertyChange(e.getActionCommand(), false, true);
        setTrackAheadFreeQuestionVisible(false);
    }

    private class TransparentButton extends JButton {

        private final boolean plus;

        private TransparentButton(boolean plusIcon) {
            super();
            setOpaque(false);
            setBorderPainted(false);
            setFocusable(false);
            plus = plusIcon;

            if (plus){
                setIcon(ResourceUtil.getImageIcon( "NA_03.bmp"));
                setDisabledIcon(ResourceUtil.getImageIcon( "NA_05.bmp"));
            } else {
                setIcon(ResourceUtil.getImageIcon( "NA_04.bmp"));
                setDisabledIcon(ResourceUtil.getImageIcon( "NA_06.bmp"));
            }

            setBounds(0, (plus ? 246 : 0 ), 50, 50);
            addActionListener(this::changeScale);
        }

        private void changeScale(ActionEvent e){
            log.debug("scale changed {}", e.paramString());
            currentScale+= ( plus ? -1 : 1 );
            setButtonsToState();
            planningPanel.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Icon i = ( isEnabled() ? getIcon() : getDisabledIcon());
            if (i != null) {
                i.paintIcon(this, g, 15, plus ? 36 : 0);
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanelD.class);

}
