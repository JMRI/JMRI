package jmri.jmrix.mrc.swing;

import jmri.jmrix.mrc.MrcSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation
 * of window title and help reference for Mrc panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen  Copyright 2010
 * Copied from nce.swing
 * @author Ken Cameron 2014
 * @author Kevin Dickerson 2014
 * @version $Revision: 22942 $
 */

abstract public class MrcPanel extends jmri.util.swing.JmriPanel implements MrcPanelInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8704964386237089071L;
	
	/**
     * make "memo" object available as convenience
     */
    protected MrcSystemConnectionMemo memo;
    
    public void initComponents(MrcSystemConnectionMemo memo) throws Exception {
        this.memo = memo;
    }
    
    public void initContext(Object context) throws Exception {
        if (context instanceof MrcSystemConnectionMemo ) {
            try {
				initComponents((MrcSystemConnectionMemo) context);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
}