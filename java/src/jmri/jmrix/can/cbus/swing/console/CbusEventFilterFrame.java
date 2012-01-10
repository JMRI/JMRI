/*
 * CbusEventFilterFrame.java
 *
 */

package jmri.jmrix.can.cbus.swing.console;

import javax.swing.WindowConstants;
import jmri.jmrix.can.cbus.*;

/**
 * Frame to control an instance of CBUS filter to filter events
 *
 * @author			Andrew Crosland   Copyright (C) 2008
 * @version			$Revision: 17977 $
 */
public class CbusEventFilterFrame extends jmri.jmrix.can.cbus.CbusEventFilterFrame {
    
    private CbusConsolePane _console = null;
    
    /** Creates a new instance of CbusFilterFrame */
    public CbusEventFilterFrame(CbusConsolePane console) {
        super();
        log.debug("CbusEventFilterFrame(CbusEventFilter) ctor called");
        for (int i = 0; i < FILTERS; i++) {
            _filter[i] = new CbusEventFilter();
            _filterActive[i] = false;
        }
        _console = console;
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        log.debug("CbusEventFilterFrame(CbusEventFilter) ctor done");
    }
    
    protected String title() { return "CBUS EventFilter"; }
    
    protected void init() {
    }
    
    public void enable(int index, int nn, boolean nnEn, int ev, boolean evEn, int ty) {
        _console.filterOn(index, nn, nnEn, ev, evEn, ty);
        _filter[index].setNn(nn);
        _filter[index].setNnEnable(nnEn);
        _filter[index].setEv(ev);
        _filter[index].setEvEnable(evEn);
        _filter[index].setType(ty);
        _filterActive[index] = true;
    }
    
    public void disable(int index) {
        _filterActive[index] = false;
        _console.filterOff(index);
    }

    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CbusEventFilterFrame.class.getName());
}
