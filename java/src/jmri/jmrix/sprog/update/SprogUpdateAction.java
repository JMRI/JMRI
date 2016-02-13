//SprogUpdateAction.java
package jmri.jmrix.sprog.update;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogIIUpdateFrame object
 *
 * @author	Andrew crosland Copyright (C) 2004
 * @version	$Revision$
 */
public class SprogUpdateAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6041262829964811405L;

    public SprogUpdateAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
    }

    private final static Logger log = LoggerFactory.getLogger(SprogUpdateAction.class.getName());

}


/* @(#)SprogUpdateAction.java */
