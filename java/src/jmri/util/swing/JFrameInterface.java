package jmri.util.swing;

import java.awt.Frame;
import javax.swing.JFrame;

/**
 * A simple WindowInterface for a JFrame. This really does nothing but wrap the
 * WindowInterface interface around a JFrame, so that menu items that expect the
 * WindowInterface can rely on its presence.
 *
 * @author rhwood
 */
public class JFrameInterface implements WindowInterface {

    protected JFrame frame = null;

    public JFrameInterface(JFrame frame) {
        this.frame = frame;
    }

    @Override
    public void show(JmriPanel child, JmriAbstractAction action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void show(JmriPanel child, JmriAbstractAction action, Hint hint) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean multipleInstances() {
        return false;
    }

    @Override
    public Frame getFrame() {
        return this.frame;
    }

    @Override
    public void dispose() {
    }
}
