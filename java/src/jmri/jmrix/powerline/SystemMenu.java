package jmri.jmrix.powerline;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Create a "Systems" menu containing the Jmri powerline-specific tools
 *
 * @author Bob Jacobsen Copyright 2003, 2006, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 */
@Deprecated
@API(status = EXPERIMENTAL)
public class SystemMenu extends jmri.jmrix.powerline.swing.PowerlineMenu {

    public SystemMenu(SerialSystemConnectionMemo memo) {
        super(memo);
    }
}
