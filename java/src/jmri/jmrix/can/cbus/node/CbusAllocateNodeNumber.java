package jmri.jmrix.can.cbus.node;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusSend;
import java.util.TimerTask;
import jmri.util.TimerUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CbusAllocateNodeNumber implements CanListener {
    
    private final CbusNodeTableDataModel nodeModel;    
    private final CanSystemConnectionMemo _memo;
    private final CbusSend send;
    
    private JLabel rqNNtext;
    private int baseNodeNum;
    private boolean WAITINGRESPONSE_RQNN_PARAMS;
    private boolean NODE_NUM_DIALOGUE_OPEN;
    private boolean WAITING_RESPONSE_NAME;
    private int[] _paramsArr;
    private String _tempNodeName;
    private JLabel rqNNspinnerlabel;
    private int _timeout;
    
    public CbusAllocateNodeNumber(CanSystemConnectionMemo memo, CbusNodeTableDataModel model) {
        
        nodeModel = model;
        // connect to the CanInterface
        _memo = memo;
        addTc(memo);
        send = new CbusSend(memo);
        
        baseNodeNum = 256;
        _paramsArr = null;
        WAITINGRESPONSE_RQNN_PARAMS = false;
        NODE_NUM_DIALOGUE_OPEN = false;
        WAITING_RESPONSE_NAME = false;
        _timeout = CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME;
        _tempNodeName="";
    }
    
    
    /**
     * 
     * @param nn -1 if already in setup from unknown, 0 if entering from SLiM,
     * else previous node number
     * @param nodeText 
     */
    private void startnodeallocation(int nn, String nodeText) {
        
        if (NODE_NUM_DIALOGUE_OPEN) {
            return;
        }
        
        NODE_NUM_DIALOGUE_OPEN=true;
        _tempNodeName="";
        
        JPanel rqNNpane = new JPanel();
        JPanel bottomrqNNpane = new JPanel();
        rqNNspinnerlabel = new JLabel(Bundle.getMessage("NdRqNnSelect"));
        
        bottomrqNNpane.setLayout(new GridLayout(2, 1));
        rqNNpane.setLayout(new BorderLayout());
        rqNNtext = new JLabel(Bundle.getMessage("NdRqNdDetails"));
        
        String popuplabel;
        
        baseNodeNum =  nodeModel.getNextAvailableNodeNumber(baseNodeNum);
        
        switch (nn) {
            case 0:
                popuplabel=Bundle.getMessage("NdEntrSlimTitle");
                _paramsArr = null; // reset just in case
                break;
            case -1:
                popuplabel="Node found in Setup Mode";
                // not resetting _paramsArr as may be set from found in setup
                if ( nodeText != null ) {
                    rqNNtext.setText(nodeText);
                }
                break;
            default:
                popuplabel=Bundle.getMessage("NdEntrNumTitle",String.valueOf(nn));
                _paramsArr = null; // reset just in case
                baseNodeNum = nn;
                break;
        }
        
        JSpinner rqnnSpinner = getNewRqnnSpinner();
        rqnnSpinner.firePropertyChange("open", false, true); // reset node text
        rqNNpane.add(rqNNtext, BorderLayout.CENTER);
        bottomrqNNpane.add(rqNNspinnerlabel);
        bottomrqNNpane.add(rqnnSpinner);
        
        rqNNpane.add(bottomrqNNpane, BorderLayout.PAGE_END);
        
        Toolkit.getDefaultToolkit().beep();
        
        if ( _paramsArr==null ) {
            WAITINGRESPONSE_RQNN_PARAMS=true;
            send.nodeRequestParamSetup();
        }
        
        int option = JOptionPane.showOptionDialog(null, 
            rqNNpane, 
            popuplabel, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (option == JOptionPane.OK_OPTION) {
            int newval = (Integer) rqnnSpinner.getValue();
            baseNodeNum = newval;
            setSendSNNTimeout();
            send.nodeSetNodeNumber(newval);            
        }
        NODE_NUM_DIALOGUE_OPEN=false;
        WAITINGRESPONSE_RQNN_PARAMS=false;
    }
    
    private JSpinner getNewRqnnSpinner() {
    
        JSpinner rqnnSpinner = new JSpinner(new SpinnerNumberModel(baseNodeNum, 1, 65535, 1));
        rqnnSpinner.setToolTipText((Bundle.getMessage("ToolTipNodeNumber")));
        
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(rqnnSpinner, "#");
        rqnnSpinner.setEditor(editor);
        
        JFormattedTextField rqfield = (JFormattedTextField) editor.getComponent(0);
        DefaultFormatter rqformatter = (DefaultFormatter) rqfield.getFormatter();
        rqformatter.setCommitsOnValidEdit(true);
        rqfield.setBackground(Color.white);
        rqnnSpinner.addChangeListener((ChangeEvent e) -> {
            int newval = (Integer) rqnnSpinner.getValue();
            
            if (!CbusNodeConstants.getReservedModule(newval).isEmpty()) {
                rqNNspinnerlabel.setText(CbusNodeConstants.getReservedModule(newval));
                rqfield.setBackground(Color.yellow);
            }
            else {
                rqNNspinnerlabel.setText(Bundle.getMessage("NdRqNnSelect"));
                rqfield.setBackground(Color.white);
            }
            if ( !nodeModel.getNodeNumberName(newval).isEmpty() ) {
                rqNNspinnerlabel.setText(Bundle.getMessage("NdNumInUse",nodeModel.getNodeNumberName(newval)));
                rqfield.setBackground(Color.red);
            }
        });
        return rqnnSpinner;
    }
    
    protected TimerTask sendSNNTask;
    
    private void clearSendSNNTimeout(){
        if (sendSNNTask != null ) {
            sendSNNTask.cancel();
            sendSNNTask = null;
        }
    }
    
    private void setSendSNNTimeout() {
        sendSNNTask = new TimerTask() {
            @Override
            public void run() {
                sendSNNTask = null;
                log.error("No confirmation from node when setting node number {}", baseNodeNum );
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NnAllocError",baseNodeNum), Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
                clearSendSNNTimeout();
            }
        };
        TimerUtil.schedule(sendSNNTask, _timeout);
    }
    
    /**
     * Set the SNN timeout, for Testing purposes
     * @param newVal Timeout value in ms
     */
    protected void setTimeout( int newVal){
        _timeout = newVal;
    }
    
    /**
     * If popup not open send a setup param request to try and catch nodes awaiting number allocation
     * when an all node respond message is sent.
     * @param m Outgoing CanMessage
     */
    @Override
    public void message(CanMessage m) { // outgoing cbus message
        if ( m.extendedOrRtr() ) {
            return;
        }
        if (CbusMessage.getOpcode(m) == CbusConstants.CBUS_QNN) {
            if (!NODE_NUM_DIALOGUE_OPEN) {
                send.nodeRequestParamSetup();
            }
        }
    }
    
    /**
     * Capture CBUS_RQNN, CBUS_PARAMS, CBUS_NNACK, CBUS_NAME
     * @param m incoming CanReply
     */
    @Override
    public void reply(CanReply m) {
        if ( m.extendedOrRtr() ) {
            return;
        }
        // run on GUI not Layout thread as pretty much all of this is GUI based.
        // and could be awaiting from response from JDialog.
        jmri.util.ThreadingUtil.runOnGUIEventually( ()->{
            processAllocateFrame(m);
        });
    }
    
    private void processAllocateFrame(CanReply m){
        switch (CbusMessage.getOpcode(m)) {
            case CbusConstants.CBUS_RQNN:
                // node requesting a number, nn is existing number
                startnodeallocation( ( m.getElement(1) * 256 ) + m.getElement(2), null );
                break;
            case CbusConstants.CBUS_PARAMS:
                processNodeParams(m);
                break;
            case CbusConstants.CBUS_NNACK: // node number acknowledge
                clearSendSNNTimeout();
                // if nodes are allowed to be added to node table, add.
                // this is done here so any known parameters can be passed directly rather than re-requested
                if ( jmri.InstanceManager.getDefault(jmri.jmrix.can.cbus.CbusPreferences.class).getAddNodes() ) {
                    int nodeNum = m.getElement(1) * 256 + m.getElement(2);
                    nodeModel.setRequestNodeDisplay(nodeNum);
                    // provide will add to table
                    CbusNode nd = nodeModel.provideNodeByNodeNum( nodeNum );
                    nd.getCanListener().setParamsFromSetup(_paramsArr);
                    nd.setNodeNameFromName(_tempNodeName);
                    nd.resetNodeAll();
                    nodeModel.startUrgentFetch();
                    nodeModel.setRequestNodeDisplay(-1);
                    send.searchForCommandStations();
                }   
                _paramsArr = null;
                break;
            case CbusConstants.CBUS_NAME:
                processNodeName(m);
                break;
            default:
                break;
        }
    }
    
    private void processNodeParams(CanReply m) {
        _paramsArr = new int[] { m.getElement(1),m.getElement(2),
            m.getElement(3),m.getElement(4), m.getElement(5),
            m.getElement(6),m.getElement(7) };
            
        StringBuilder nodepropbuilder = new StringBuilder(40);
        nodepropbuilder.append (CbusNodeConstants.getManu( _paramsArr[0] ));  
        nodepropbuilder.append (" ");
        nodepropbuilder.append( CbusNodeConstants.getModuleType( _paramsArr[0] , _paramsArr[2] ));
            
        if (WAITINGRESPONSE_RQNN_PARAMS) {
            rqNNtext.setText(nodepropbuilder.toString());
            WAITINGRESPONSE_RQNN_PARAMS=false;
        }
        else if (!NODE_NUM_DIALOGUE_OPEN) {
            startnodeallocation( -1, nodepropbuilder.toString() );
        }
            
        if ( CbusNodeConstants.getModuleType( _paramsArr[0] , _paramsArr[2] ).isEmpty() ) {
            WAITING_RESPONSE_NAME = true;
            send.rQmn(); // request node type name if not recognised
        }
    }
    
    private void processNodeName(CanReply m){
        if (WAITING_RESPONSE_NAME) {
            WAITING_RESPONSE_NAME = false;
            StringBuilder rval = new StringBuilder(10);
            rval.append("CAN");
            rval.append(String.format("%c", (char) m.getElement(1) ));
            rval.append(String.format("%c", (char) m.getElement(2) ));
            rval.append(String.format("%c", (char) m.getElement(3) ));
            rval.append(String.format("%c", (char) m.getElement(4) ));
            rval.append(String.format("%c", (char) m.getElement(5) ));
            rval.append(String.format("%c", (char) m.getElement(6) ));
            rval.append(String.format("%c", (char) m.getElement(7) ));
            _tempNodeName = rval.toString().trim();
            
            StringBuilder nodepropbuilder = new StringBuilder(40);
            nodepropbuilder.append (CbusNodeConstants.getManu( _paramsArr[0] ));  
            nodepropbuilder.append (" ");
            nodepropbuilder.append (_tempNodeName);
            
            rqNNtext.setText(nodepropbuilder.toString());
        }
    }
    
    public void dispose(){
        clearSendSNNTimeout();
        removeTc(_memo);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusAllocateNodeNumber.class);

}
