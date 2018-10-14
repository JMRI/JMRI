package jmri.jmrix.sprog.update;

import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;

/**
 * Swing action to create and register a SprogIIUpdateFrame object
 *
 * @author	Andrew crosland Copyright (C) 2004
 */
abstract public class SprogUpdateAction extends AbstractAction {

    protected SprogSystemConnectionMemo _memo = null;

    public SprogUpdateAction(String s,SprogSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

}
