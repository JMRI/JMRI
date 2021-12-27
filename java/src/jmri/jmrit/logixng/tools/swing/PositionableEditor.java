package jmri.jmrit.logixng.tools.swing;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.jmrit.display.Positionable;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

/**
 * Editor of ConditionalNG
 * 
 * @author Daniel Bergqvist 2021
 */
public class PositionableEditor extends TreeEditor {

    protected final Positionable _positionableLabel;
    
    
    /**
     * Maintain a list of listeners -- normally only one.
     */
    private final List<Runnable> listenerList = new ArrayList<>();
    
    /**
     * Construct a ConditionalEditor.
     * <p>
     * This is used by JmriUserPreferencesManager since it tries to create an
     * instance of this class.
     */
    public PositionableEditor() {
        super(InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(null, new FemaleSocketListener(){
            @Override
            public void connected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public void disconnected(FemaleSocket socket) {
                throw new UnsupportedOperationException("Not supported");
            }
        }, "A"),
                EnableClipboard.EnableClipboard,
                EnableRootRemoveCutCopy.EnableRootRemoveCutCopy,
                EnableRootPopup.EnableRootPopup,
                EnableExecuteEvaluate.EnableExecuteEvaluate
        );
        
        _positionableLabel = null;
    }
    
    /**
     * Construct a ConditionalEditor.
     *
     * @param positionableLabel the PositionableLabel to be edited
     */
    public PositionableEditor(@Nonnull Positionable positionableLabel) {
        super(positionableLabel.getConditionalNG().getFemaleSocket(),
                EnableClipboard.EnableClipboard,
                EnableRootRemoveCutCopy.EnableRootRemoveCutCopy,
                EnableRootPopup.EnableRootPopup,
                EnableExecuteEvaluate.EnableExecuteEvaluate
        );
        
        _positionableLabel = positionableLabel;
        
        PositionableEditor.this.setTitle(Bundle.getMessage("TitleEditPositionable"));
    }
    
    /** {@inheritDoc} */
    @Override
    public void windowClosed(WindowEvent e) {
        fireLogixNGEvent();
    }
    
    public void addListener(Runnable listener) {
        listenerList.add(listener);
    }
    
    /**
     * Notify the listeners to check for new data.
     */
    void fireLogixNGEvent() {
        for (Runnable l : listenerList) {
            l.run();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionableEditor.class);

}
