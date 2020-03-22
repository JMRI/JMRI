package jmri.jmrix.can.cbus.swing.console;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Frame for CBUS Console
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleDecodeOptionsPane extends javax.swing.JPanel implements CanListener {
    
    private final CbusConsolePane _mainPane;
    
    private JCheckBox timeCheckBox;
    private JCheckBox priCheckBox;
    private JCheckBox canidCheckBox;
    private JCheckBox showarrowsCheckBox;
    private JCheckBox showRtrCheckBox;
    private JCheckBox showOpcCheckBox;
    private JCheckBox showOpcExtraCheckBox;
    private JCheckBox showAddressCheckBox;
    private JCheckBox showCanCheckBox;
    
    private final DateFormat df;
    
    public CbusConsoleDecodeOptionsPane(CbusConsolePane mainPane){
        super();
        _mainPane = mainPane;
        makePane();
        df = new SimpleDateFormat("HH:mm:ss.SSS");
        addTc(_mainPane.tc);
    }

    private void makePane(){
    
        makeCheckBoxes();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
        add(showarrowsCheckBox);
        add(timeCheckBox);
        add(showOpcCheckBox);        
        add(showOpcExtraCheckBox);
        add(showAddressCheckBox);
        add(priCheckBox);
        add(canidCheckBox);
        add(showCanCheckBox);
        add(showRtrCheckBox);
    }
    
    private void makeCheckBoxes() {
    
        timeCheckBox = new JCheckBox();
        priCheckBox = new JCheckBox();
        canidCheckBox = new JCheckBox();
        showarrowsCheckBox = new JCheckBox();
        showRtrCheckBox = new JCheckBox();
        showOpcCheckBox = new JCheckBox();
        showOpcExtraCheckBox = new JCheckBox();
        showAddressCheckBox = new JCheckBox();
        showCanCheckBox = new JCheckBox();
        
        initButtonTips();
        
    }
    
    private void initButtonTips() {
    
        timeCheckBox.setText(Bundle.getMessage("ButtonShowTimestamp"));
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps"));

        priCheckBox.setText(Bundle.getMessage("ButtonShowPriorities"));
        priCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPrios"));

        canidCheckBox.setText(Bundle.getMessage("CanID"));
        canidCheckBox.setToolTipText(Bundle.getMessage("CanID"));

        showarrowsCheckBox.setText(Bundle.getMessage("TrafficDirection"));
        showarrowsCheckBox.setToolTipText(Bundle.getMessage("TrafficDirectionTip"));
        showarrowsCheckBox.setSelected(true);
        
        showRtrCheckBox.setText(Bundle.getMessage("RtrCheckbox"));
        showRtrCheckBox.setToolTipText(Bundle.getMessage("RtrCheckboxTip"));

        showOpcCheckBox.setText(Bundle.getMessage("showOpcCheckbox"));
        showOpcCheckBox.setToolTipText(Bundle.getMessage("showOpcCheckboxTip"));
        showOpcCheckBox.setSelected(true);
        
        showOpcExtraCheckBox.setText(Bundle.getMessage("OpcExtraCheckbox"));
        showOpcExtraCheckBox.setToolTipText(Bundle.getMessage("OpcExtraCheckboxTip"));
        
        showAddressCheckBox.setText(Bundle.getMessage("showAddressCheckBox"));
        showAddressCheckBox.setToolTipText(Bundle.getMessage("showAddressCheckBoxTip"));
        
        showCanCheckBox.setText(Bundle.getMessage("showCanCheckBox"));
        showCanCheckBox.setToolTipText(Bundle.getMessage("showCanCheckBoxTip"));
    
    }
    
    /**
     * Add standard logging text
     * @param m CanReply or CanMessage
     * @param output the StringBuilder to append to
     * @throws IllegalArgumentException if not a CanFrame instance
     */
    public void addBiDirectionalInfo(AbstractMessage m, StringBuilder output) {
        if (!(m instanceof CanFrame)){
            throw new IllegalArgumentException(m + " is Not a CanFrame");
        }
        addTime(output);
        CanFrame msg = (CanFrame) m;
        updateMainStats( msg );
        
        if (!msg.isExtended()) {
            output.append(CbusOpCodes.decodeopc(m)).append(" ");
        }
        
        output.append(CbusOpCodes.decode(m)).append(" ");
        
        if (CbusOpCodes.isKnownOpc(m)) {
        
            appendIfChecked(showOpcCheckBox,output,Bundle.getMessage("CBUS_" + CbusOpCodes.decodeopc(m)));
            appendIfChecked(showOpcExtraCheckBox,output,Bundle.getMessage("CTIP_" + CbusOpCodes.decodeopc(m)));
        
        }

        if (showAddressCheckBox.isSelected()) {
            output.append(" [").append(CbusMessage.toAddress(m)).append("] ");
        }
        if (!msg.isExtended()) {
            addPriority(m,output);
        }
        
        if (canidCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CanID")).append(": ").append(CbusMessage.getId(m)).append(" ");
        }
        
        appendIfChecked(showCanCheckBox,output,msg.toString());
        
        addRtR(msg,output);
        
        output.append("\n");
    
    }
    
    private void updateMainStats(CanFrame msg) {
        _mainPane.statsPane.incremenetTotal();
        if (!msg.isExtended()){
            int opc = CbusMessage.getOpcode((AbstractMessage)msg);
            if (CbusOpCodes.isEvent(opc)) {
                _mainPane.statsPane.incrementEvents();
            }

            if (CbusOpCodes.isDcc(opc)) {
               _mainPane.statsPane.incrementDcc();
            }
        }
    
    }
    
    private void addPriority( AbstractMessage m, StringBuilder output ){
        if (priCheckBox.isSelected()) {
            output.append(Bundle.getMessage("DynPriTitle")).append(": ")
                .append(CbusMessage.getPri(m) / 4).append(" ")
                .append(Bundle.getMessage("MinPriTitle")).append(": ")
                .append(CbusMessage.getPri(m) & 3).append(" ");
        }
    }
    
    private void addRtR( CanFrame msg, StringBuilder output ){
        if (showRtrCheckBox.isSelected()) {
            if (msg.isRtr()) { 
                output.append(Bundle.getMessage("IsRtrFrame"));
            } else { 
                output.append(Bundle.getMessage("IsNotRtrFrame"));
            }
            output.append(" ");
        }
    }
    
    private void addTime(StringBuilder output) {
        if (timeCheckBox.isSelected()) {
           output.append(df.format(new Date())).append(" ");
        }
    }
    
    private void appendIfChecked( JCheckBox box, StringBuilder output, String toAdd){
        if (box.isSelected()) {
           output.append(toAdd).append(" ");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage m) {  // process an outgoing message and log it
        if ( ( _mainPane.displayPane.filterFrame!=null ) && ( _mainPane.displayPane.filterFrame.filter(m)) ) {
            return;
        }
        _mainPane.statsPane.incremenetSent();
        
        StringBuilder output = new StringBuilder();
        if (showarrowsCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CBUS_OUT")).append(" ");
        }
        
        addBiDirectionalInfo(m, output);

        _mainPane.nextLine( Bundle.getMessage("EventSent") + ": " + m.toMonitorString() + "\n",
            output.toString(),
            (_mainPane.displayPane.highlightFrame != null) ? _mainPane.displayPane.highlightFrame.highlight(m) : -1);
                
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply r) {  // receive a reply message and log it
        if ( ( _mainPane.displayPane.filterFrame!=null ) && ( _mainPane.displayPane.filterFrame.filter(r) ) ) {
            return;
        }
        _mainPane.statsPane.incremenetReceived();
        _mainPane.packetPane.setLastReceived(r);
        
        StringBuilder output = new StringBuilder();
        
        if (showarrowsCheckBox.isSelected()) {
            output.append(Bundle.getMessage("CBUS_IN")).append(" ");
        }
        
        addBiDirectionalInfo(r,output);
        
        _mainPane.nextLine( Bundle.getMessage("EventReceived") + ": " + r.toMonitorString() + "\n",
            output.toString(),
            (_mainPane.displayPane.highlightFrame != null) ? _mainPane.displayPane.highlightFrame.highlight(r) : -1);
        
    }
    
    public void dispose(){
        removeTc(_mainPane.tc);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusConsoleDecodeOptionsPane.class);
}
