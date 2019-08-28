package jmri.jmrit.logixng.tools.swing;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TimeDiagram object.
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class TimeDiagramAction extends AbstractAction {

    public TimeDiagramAction(String s) {
        super(s);
    }

    public TimeDiagramAction() {
        this(Bundle.getMessage("MenuTimeDiagram")); // NOI18N
    }

    static TimeDiagram timeDiagramFrame = null;

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Only one TimeDiagramFrame")
    public void actionPerformed(ActionEvent e) {
        // create a settings frame
        if (timeDiagramFrame == null || !timeDiagramFrame.isVisible()) {
            timeDiagramFrame = new TimeDiagram();
            timeDiagramFrame.initComponents();
        }
        timeDiagramFrame.setExtendedState(Frame.NORMAL);
        timeDiagramFrame.setVisible(true); // this also brings the frame into focus
    }
    
}
