//TrainByCarTypeAction.java
package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a TrainByCarTypeFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainByCarTypeAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5831631159830861734L;

    public TrainByCarTypeAction(String s, Train train) {
        super(s);
        _train = train;
    }

    Train _train;

    public void actionPerformed(ActionEvent e) {
        // create frame
        TrainByCarTypeFrame f = new TrainByCarTypeFrame();
        f.initComponents(_train);
    }
}

/* @(#)TrainByCarTypeAction.java */
