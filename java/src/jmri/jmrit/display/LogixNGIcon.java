package jmri.jmrit.display;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logixng.LogixNG;
import jmri.util.swing.JmriMouseEvent;

/**
 * An icon that executes a LogixNG when clicked on.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNGIcon extends PositionableLabel {

    public static final IdentityManager IDENTITY_MANAGER = new IdentityManager();

    private final int _identity;

    public LogixNGIcon(String s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(this);
    }

    public LogixNGIcon(int identity, String s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(identity, this);
    }

    public LogixNGIcon(@CheckForNull NamedIcon s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(this);
    }

    public LogixNGIcon(int identity, @CheckForNull NamedIcon s, @Nonnull Editor editor) {
        super(s, editor);
        _identity = IDENTITY_MANAGER.getIdentity(identity, this);

        // Please retain the line below. It's used to create the resources/icons/logixng/logixng_icon.gif icon
        // createLogixNGIconImage();
    }

    public int getIdentity() {
        return _identity;
    }

    @Override
    public void doMousePressed(JmriMouseEvent e) {
        log.debug("doMousePressed");
        if (!e.isMetaDown() && !e.isAltDown()) {
            executeLogixNG();
        }
        super.doMousePressed(e);
    }
/*
    @Override
    public void doMouseReleased(JmriMouseEvent e) {
        log.debug("doMouseReleased");
        if (!e.isMetaDown() && !e.isAltDown()) {
            executeLogixNG();
        }
        super.doMouseReleased(e);
    }

    @Override
    public void doMouseClicked(JmriMouseEvent e) {
        if (true && !true) {
            // this button responds to clicks
            if (!e.isMetaDown() && !e.isAltDown()) {
                try {
                    if (getSensor().getKnownState() == jmri.Sensor.INACTIVE) {
                        getSensor().setKnownState(jmri.Sensor.ACTIVE);
                    } else {
                        getSensor().setKnownState(jmri.Sensor.INACTIVE);
                    }
                } catch (jmri.JmriException reason) {
                    log.warn("Exception flipping sensor", reason);
                }
            }
        }
        super.doMouseClicked(e);
    }
*/
    public void executeLogixNG() {
        LogixNG logixNG = getLogixNG();

        if (logixNG != null) {
            for (int i=0; i < logixNG.getNumConditionalNGs(); i++) {
                logixNG.getConditionalNG(i).execute();
            }
        }
    }


/*
    // Please retain this commented method. It's used to create the resources/icons/logixng/logixng_icon.gif icon

    private void createLogixNGIconImage() {

        try {
            int width = 90, height = 39;

            // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
            java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);

            java.awt.Graphics2D ig2 = bi.createGraphics();

            ig2.setColor(java.awt.Color.WHITE);
            ig2.fillRect(0, 0, width-1, height-1);
            ig2.setColor(java.awt.Color.BLACK);
            ig2.drawRect(0, 0, width-1, height-1);

            java.awt.Font font = new java.awt.Font("Verdana", java.awt.Font.BOLD, 15);
            ig2.setFont(font);
            ig2.setPaint(java.awt.Color.black);

            // Draw string twice to get more bold
            ig2.drawString("LogixNG", 11, 24);
            ig2.drawString("LogixNG", 12, 24);

            javax.imageio.ImageIO.write(bi, "gif", new java.io.File(jmri.util.FileUtil.getExternalFilename("resources/icons/logixng/logixng_icon.gif")));
        } catch (java.io.IOException ie) {
            throw new RuntimeException(ie);
        }
    }
*/


    public static class IdentityManager {

        Map<Integer, LogixNGIcon> _identities = new HashMap<>();
        int _lastIdentity = -1;

        private IdentityManager() {
            // Private constructor to keep it as a singleton
        }

        public int getIdentity(LogixNGIcon logixNGIcon) {
            _lastIdentity++;
            _identities.put(_lastIdentity, logixNGIcon);
            return _lastIdentity;
        }

        public int getIdentity(int identity, LogixNGIcon logixNGIcon) {
            if (_identities.containsKey(identity)) {
                log.error(String.format("Identity %d already exists", identity));
                return getIdentity(logixNGIcon);
            }
            _identities.put(identity, logixNGIcon);
            if (identity > _lastIdentity) {
                _lastIdentity = identity;
            }
            return identity;
        }

        public LogixNGIcon getLogixNGIcon(int identity) {
            return _identities.get(identity);
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGIcon.class);
}
