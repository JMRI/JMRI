package jmri.jmrix.powerline;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Create a "Systems" menu containing the Jmri powerline-specific tools
 *
 * @author Bob Jacobsen Copyright 2003, 2006, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class SystemMenu extends jmri.jmrix.powerline.swing.PowerlineMenu {

    public SystemMenu(SerialSystemConnectionMemo memo) {
        super(memo);
    }
}
