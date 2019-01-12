package jmri.jmrix.can.cbus.node;

import jmri.jmrix.can.cbus.CbusEvent;

/**
 * Class to represent an event stored on a node.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeEvent extends CbusEvent {
    int _thisnode;
    int _index;
    int[] _evVarArr;
    int _nodeConfigPanelID;
    
    public CbusNodeEvent( int _nn, int _en, int thisnode, int index, int maxEvVar){
        super(_nn,_en);
        _thisnode = thisnode;
        _index = index;
        _nodeConfigPanelID = -1;
        _evVarArr = new int[maxEvVar];
        java.util.Arrays.fill(_evVarArr,-1);
    }
    
    public void setEvVar(int index, int value) {
        _evVarArr[(index-1)]=value;
    }
    
    public int getEvVar(int index) {
        return _evVarArr[(index-1)];
    }
    
    public Boolean matches(int nn, int en) {
        if ( (nn == _nn) && (en == _en) ) {
            return true;
        }
        return false;
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
    
    public int getNn(){
        return _nn;
    }
    
    public int getEn() {
        return _en;
    }
    
    public int getNumEvVars() {
        return _evVarArr.length;
    }

}
