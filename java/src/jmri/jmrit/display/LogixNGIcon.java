package jmri.jmrit.display;

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

    public LogixNGIcon(String s, @Nonnull Editor editor) {
        super(s, editor);
    }

    public LogixNGIcon(@CheckForNull NamedIcon s, @Nonnull Editor editor) {
        super(s, editor);
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
    private void executeLogixNG() {
        LogixNG logixNG = getLogixNG();

        if (logixNG != null) {
            for (int i=0; i < logixNG.getNumConditionalNGs(); i++) {
                logixNG.getConditionalNG(i).execute();
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorIcon.class);
}
