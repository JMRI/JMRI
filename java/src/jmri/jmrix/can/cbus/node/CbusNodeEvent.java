package jmri.jmrix.can.cbus.node;

import javax.annotation.Nonnull;
import jmri.jmrix.can.cbus.CbusEvent;

/**
 * Class to represent an event stored on a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEvent extends CbusEvent {
    private int _thisnode;
    private int _index;
    private int[] _evVarArr;
    private int _nodeConfigPanelID;
    
    public CbusNodeEvent( int nn, int en, int thisnode, int index, int maxEvVar){
        super(nn,en);
        _thisnode = thisnode;
        _index = index;
        _nodeConfigPanelID = -1;
        _evVarArr = new int[maxEvVar];
        java.util.Arrays.fill(_evVarArr,-1);
    }
 
    // copy one
    public CbusNodeEvent(@Nonnull CbusNodeEvent m) {
        super(m.getNn(),m.getEn());
        _thisnode = m._thisnode;
        _index = m._index;
        _nodeConfigPanelID = m._nodeConfigPanelID;
        _evVarArr = m._evVarArr;
    }
 
    public void setEvVar(int index, int value) {
        _evVarArr[(index-1)]=value;
    }
    
    public int getEvVar(int index) {
        return _evVarArr[(index-1)];
    }
    
    public int getParentNn(){
        return _thisnode;
    }

    public void setIndex(int index){
        _index = index;
    }
    
    public void setNodeConfigPanelID( int index){
        _nodeConfigPanelID = index;
    }
    
    public int getNodeConfigPanelID() {
        return _nodeConfigPanelID;
    }

    public int getIndex(){
        return _index;
    }
    
    public int getNumEvVars() {
        return _evVarArr.length;
    }

}
