package jmri.jmrix.can.cbus.node;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.TimerTask;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import jmri.InvokeOnAnyThread;
import jmri.InvokeOnGuiThread;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusPreferences;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.util.ThreadingUtil;
import jmri.util.TimerUtil;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.JSpinnerUtil;

/**
 * Listens for CBUS Nodes requesting a Node Number, and asks the user for one.
 * <p>
 * Incoming frames, the node number dialogue and the Set Node Number timeout
 * are all handled on the GUI thread, which owns the allocation state.
 * Once {@link #dispose()} has been called, frames and timeouts that were
 * already queued for the GUI thread are ignored.
 */
public class CbusAllocateNodeNumber implements CanListener {

    private final CbusNodeTableDataModel nodeModel;
    private final CanSystemConnectionMemo _memo;
    private final CbusSend send;

    private JLabel rqNNtext;
    private int baseNodeNum;
    private boolean waitingForParams;
    private volatile boolean dialogueOpen; // read by outgoing message thread
    private boolean waitingForName;
    private int[] _paramsArr;
    private String _tempNodeName;
    private JLabel rqNNspinnerlabel;
    private int _timeout;
    private volatile boolean disposed;

    public CbusAllocateNodeNumber(CanSystemConnectionMemo memo, CbusNodeTableDataModel model) {

        nodeModel = model;
        // connect to the CanInterface
        _memo = memo;
        addTc(memo);
        send = new CbusSend(memo);

        baseNodeNum = 256;
        _paramsArr = null;
        waitingForParams = false;
        dialogueOpen = false;
        waitingForName = false;
        _timeout = CbusNodeTimerManager.SINGLE_MESSAGE_TIMEOUT_TIME;
        _tempNodeName="";
    }


    /**
     *
     * @param nn -1 if already in setup from unknown, 0 if entering from SLiM,
     * else previous node number
     * @param nodeText
     */
    private void startNodeAllocation(int nn, String nodeText) {

        if (dialogueOpen) {
            return;
        }

        dialogueOpen=true;
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

        rqNNpane.setPreferredSize( new java.awt.Dimension(280, 80));

        Toolkit.getDefaultToolkit().beep();

        if ( _paramsArr==null ) {
            waitingForParams=true;
            send.nodeRequestParamSetup();
        }

        int option = JmriJOptionPane.showConfirmDialog(null,
            rqNNpane,
            popuplabel,
            JmriJOptionPane.OK_CANCEL_OPTION);

        // the dialogue is modal, dispose() may have been called while it was open
        if (option == JmriJOptionPane.OK_OPTION && !disposed) {
            int newval = (Integer) rqnnSpinner.getValue();
            baseNodeNum = newval;
            setSendSNNTimeout();
            send.nodeSetNodeNumber(newval);
        } else {
            // cancelled or disposed, discard any parameters / name received from the node in setup
            // so they cannot be applied to a different node later.
            _paramsArr = null;
            _tempNodeName = "";
            waitingForName = false;
        }
        dialogueOpen=false;
        waitingForParams=false;
    }

    private JSpinner getNewRqnnSpinner() {

        JSpinner rqnnSpinner = new JSpinner(new SpinnerNumberModel(baseNodeNum, 1, 65535, 1));
        rqnnSpinner.setToolTipText((Bundle.getMessage("ToolTipNodeNumber")));

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(rqnnSpinner, "#");
        rqnnSpinner.setEditor(editor);
        JSpinnerUtil.setCommitsOnValidEdit(rqnnSpinner, true);

        JFormattedTextField rqfield = (JFormattedTextField) editor.getComponent(0);
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

    private volatile TimerTask sendSNNTask; // cancelled by dispose() on any thread

    private void clearSendSNNTimeout(){
        TimerTask task = sendSNNTask;
        sendSNNTask = null;
        if ( task != null ) {
            task.cancel();
        }
    }

    private void setSendSNNTimeout() {
        clearSendSNNTimeout();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // hand over to the GUI thread, which owns the allocation state, so the
                // shared timer thread is not held while the error dialogue is open
                ThreadingUtil.runOnGUIEventually( () -> sendSNNTimedOut(this) );
            }
        };
        sendSNNTask = task;
        TimerUtil.schedule(task, _timeout);
    }

    /**
     * Report that the Node did not confirm the Set Node Number.
     * @param task the timer that expired. Ignored if it has since been
     * cleared by a Node Number Acknowledge, replaced, or disposed.
     */
    @InvokeOnGuiThread
    private void sendSNNTimedOut(TimerTask task) {
        if ( disposed || task != sendSNNTask ) {
            return;
        }
        sendSNNTask = null;
        log.error("No confirmation from node when setting node number {}", baseNodeNum );
        JmriJOptionPane.showMessageDialog(null,
            Bundle.getMessage("NnAllocError",baseNodeNum), Bundle.getMessage("WarningTitle"),
            JmriJOptionPane.ERROR_MESSAGE);
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
        if ( disposed || m.extendedOrRtr() ) {
            return;
        }
        if (CbusMessage.getOpcode(m) == CbusConstants.CBUS_QNN && !dialogueOpen ) {
            send.nodeRequestParamSetup();
        }
    }

    /**
     * Capture CBUS_RQNN, CBUS_PARAMS, CBUS_NNACK, CBUS_NAME
     * @param m incoming CanReply
     */
    @Override
    public void reply(CanReply m) {
        if ( disposed || m.extendedOrRtr() ) {
            return;
        }
        // run on GUI not Layout thread as pretty much all of this is GUI based.
        // and could be awaiting from response from JDialog.
        ThreadingUtil.runOnGUIEventually( ()->{
            processAllocateFrame(m);
        });
    }

    private void processAllocateFrame(CanReply m){
        if ( disposed ) { // frame was queued before dispose
            return;
        }
        switch (CbusMessage.getOpcode(m)) {
            case CbusConstants.CBUS_RQNN:
                // node requesting a number, nn is existing number
                startNodeAllocation( ( m.getElement(1) * 256 ) + m.getElement(2), null );
                break;
            case CbusConstants.CBUS_PARAMS:
                processNodeParams(m);
                break;
            case CbusConstants.CBUS_NNACK: // node number acknowledge
                processNodeNumberAck(m);
                break;
            case CbusConstants.CBUS_NAME:
                processNodeName(m);
                break;
            default:
                break;
        }
    }

    /**
     * Process a Node Number Acknowledge.
     * <p>
     * A Node sends NNACK in response to a SNN ( Set Node Number ),
     * and also in response to an ENUM ( CAN ID self enumeration ) request.
     * <p>
     * Only when the NNACK is in response to a SNN sent from here, ie. the SNN timer
     * is running and the Node Number matches, are the parameters and name obtained
     * from the Node in setup applied and the node reset for a full re-fetch.
     * <p>
     * An NNACK for a node not in the node table, eg. a Node Number set by other software,
     * adds the node to the table if enabled in preferences.
     * An NNACK from a node already in the table is otherwise ignored here,
     * the node CAN listener handles the ENUM response.
     *
     * @param m the NNACK CanReply
     */
    private void processNodeNumberAck(CanReply m) {
        int nodeNum = m.getElement(1) * 256 + m.getElement(2);
        boolean snnAcknowledged = ( sendSNNTask != null && nodeNum == baseNodeNum );
        if ( snnAcknowledged ) {
            clearSendSNNTimeout();
        }
        // if nodes are allowed to be added to node table, add.
        // this is done here so any known parameters can be passed directly rather than re-requested
        if ( _memo.get(CbusPreferences.class).getAddNodes() ) {
            if ( snnAcknowledged ) {
                nodeModel.setRequestNodeDisplay(nodeNum);
                // provide will add to table
                CbusNode nd = nodeModel.provideNodeByNodeNum( nodeNum );
                nd.getCanListener().setParamsFromSetup(_paramsArr);
                nd.setNodeNameFromName(_tempNodeName);
                nd.resetNodeAll();
                nodeModel.startUrgentFetch();
                nodeModel.setRequestNodeDisplay(-1);
                send.searchForCommandStations();
            } else if ( nodeModel.getNodeByNodeNum( nodeNum ) == null ) {
                // node number not set by us, eg. by other software, add to table.
                // setup parameters are not applied as they may be from a different node.
                log.debug("NNACK from node {} not in table, adding", nodeNum);
                nodeModel.provideNodeByNodeNum( nodeNum );
                nodeModel.startUrgentFetch();
                send.searchForCommandStations();
            } else {
                log.debug("NNACK from existing node {}, not in response to SNN, ignored", nodeNum);
            }
        }
        if ( snnAcknowledged ) {
            _paramsArr = null;
            _tempNodeName = "";
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

        if (waitingForParams) {
            rqNNtext.setText(nodepropbuilder.toString());
            waitingForParams=false;
        }
        else if (!dialogueOpen) {
            startNodeAllocation( -1, nodepropbuilder.toString() );
        }

        if ( CbusNodeConstants.getModuleType( _paramsArr[0] , _paramsArr[2] ).isEmpty() ) {
            waitingForName = true;
            send.rQmn(); // request node type name if not recognised
        }
    }

    private void processNodeName(CanReply m){
        if (waitingForName) {
            waitingForName = false;
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
            if ( _paramsArr != null ) {
                nodepropbuilder.append (CbusNodeConstants.getManu( _paramsArr[0] ));
                nodepropbuilder.append (" ");
            }
            nodepropbuilder.append (_tempNodeName);

            if ( rqNNtext != null ) {
                rqNNtext.setText(nodepropbuilder.toString());
            }
        }
    }

    /**
     * Stop listening and cancel any Set Node Number timeout.
     * Frames and timeouts already queued for the GUI thread are then ignored.
     */
    @InvokeOnAnyThread
    public void dispose(){
        disposed = true;
        removeTc(_memo);
        clearSendSNNTimeout();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusAllocateNodeNumber.class);

}
