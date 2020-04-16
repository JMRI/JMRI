package jmri.jmrix.can.cbus.eventtable;

import java.util.HashSet;
import java.util.Set;
import jmri.NamedBean;

/**
 * Class to provide access to the EventTableData.xml file.
 * @author Steve Young Copyright (C) 2020
 */
public class CbusEventBeanData  {

    private final Set<NamedBean> _nbActiveA;
    private final Set<NamedBean> _nbActiveB;

    public CbusEventBeanData( Set<NamedBean> nbActiveA, Set<NamedBean> nbActiveB) {
        _nbActiveA = nbActiveA;
        _nbActiveB = nbActiveB;
    }
    
    public CbusEventBeanData( Set<NamedBean> nbActiveA, Set<NamedBean> nbActiveB,
        Set<NamedBean> nbInActiveA, Set<NamedBean> nbInActiveB, CbusTableEvent.EvState state ) {
        _nbActiveA = ( state== CbusTableEvent.EvState.ON ? nbActiveA : nbInActiveA);
        _nbActiveB = ( state== CbusTableEvent.EvState.ON ? nbActiveB : nbInActiveB);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        _nbActiveA.forEach((n) -> appendFromNb( n, sb, true) );
        _nbActiveB.forEach((n) -> appendFromNb( n, sb, false) );
        return sb.toString().trim();
    }
    
    private void appendFromNb( NamedBean bean, StringBuilder sb, boolean beanOn) {
        sb.append(bean.getBeanType()).append(" ");
        sb.append( bean.describeState(beanOn? jmri.DigitalIO.ON : jmri.DigitalIO.OFF)).append(": ");
        sb.append(bean.getDisplayName()).append(" ");
    }

    public Set<NamedBean> getActionA(){
        return new HashSet<> ( _nbActiveA);
    }

    public Set<NamedBean> getActionB(){
        return new HashSet<> ( _nbActiveB);
    }

}
