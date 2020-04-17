package jmri.jmrix.can.cbus.swing.console;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import jmri.jmrix.AbstractMessage;
import jmri.jmrix.can.CanFrame;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusNameService;
import jmri.jmrix.can.cbus.CbusOpCodes;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Panel for CBUS Console Options.
 * <p>
 * Contains main CBUS CAN Frame String translation options.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleDecodeOptionsPane extends javax.swing.JPanel implements CanListener {
    
    private final CbusConsolePane _mainPane;
    private final CbusNameService nameService;
    
    private JCheckBox timeCheckBox;
    private JCheckBox priCheckBox;
    private JCheckBox canidCheckBox;
    private JCheckBox showarrowsCheckBox;
    private JCheckBox showRtrCheckBox;
    private JCheckBox showOpcNameCheckBox;
    private JCheckBox showOpcCheckBox;
    private JCheckBox showOpcExtraCheckBox;
    private JCheckBox showAddressCheckBox;
    private JCheckBox showCanCheckBox;
    private JCheckBox showEvNdName;
    private JCheckBox showJmriBeans;
    
    private final DateFormat df;
    
    public CbusConsoleDecodeOptionsPane(CbusConsolePane mainPane){
        super();
        _mainPane = mainPane;
        makePane();
        df = new SimpleDateFormat("HH:mm:ss.SSS");
        addTc(_mainPane.tc);
        
        nameService = new CbusNameService(_mainPane.getMemo());
    }

    private void makePane(){
    
        makeCheckBoxes();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
        add(showarrowsCheckBox);
        add(timeCheckBox);
        
        add(showOpcCheckBox);
        add(showEvNdName);
        add(showJmriBeans);
        add(showOpcNameCheckBox);
        add(showOpcExtraCheckBox);
        add(showAddressCheckBox);
        add(priCheckBox);
        add(canidCheckBox);
        add(showCanCheckBox);
        add(showRtrCheckBox);
    }
    
    private void makeCheckBoxes() {
    
        timeCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowTimestamp"));
        priCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowPriorities"));
        canidCheckBox = new JCheckBox(Bundle.getMessage("CanID"));
        showarrowsCheckBox = new JCheckBox(Bundle.getMessage("TrafficDirection"));
        showRtrCheckBox = new JCheckBox(Bundle.getMessage("RtrCheckbox"));
        
        showOpcCheckBox = new JCheckBox(Bundle.getMessage("showOpcCheckbox"));
        showOpcNameCheckBox = new JCheckBox(("OPC Name"));
        showOpcExtraCheckBox = new JCheckBox(Bundle.getMessage("OpcExtraCheckbox"));
        showAddressCheckBox = new JCheckBox(Bundle.getMessage("showAddressCheckBox"));
        showCanCheckBox = new JCheckBox(Bundle.getMessage("showCanCheckBox"));
        showJmriBeans = new JCheckBox("JMRI");
        showEvNdName = new JCheckBox("CBUS");
        
        initButtonTips();
        
    }
    
    private void initButtonTips() {
        
        showarrowsCheckBox.setSelected(true);
        showOpcCheckBox.setSelected(true);
        showJmriBeans.setSelected(true);
        showEvNdName.setSelected(true);
        showOpcNameCheckBox.setSelected(true);
        
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps"));
        priCheckBox.setToolTipText(Bundle.getMessage("TooltipShowPrios"));
        // canidCheckBox.setToolTipText(Bundle.getMessage("CanID"));
        showarrowsCheckBox.setToolTipText(Bundle.getMessage("TrafficDirectionTip"));
        showRtrCheckBox.setToolTipText(Bundle.getMessage("RtrCheckboxTip"));
        // showOpcNameCheckBox.setToolTipText(Bundle.getMessage("CanID"));
        showOpcCheckBox.setToolTipText(Bundle.getMessage("showOpcCheckboxTip"));
        showOpcExtraCheckBox.setToolTipText(Bundle.getMessage("OpcExtraCheckboxTip"));
        showAddressCheckBox.setToolTipText(Bundle.getMessage("showAddressCheckBoxTip"));
        showCanCheckBox.setToolTipText(Bundle.getMessage("showCanCheckBoxTip"));
        showJmriBeans.setToolTipText(("<html>Display names from JMRI Turnout, Sensor and Light Table.<br>CBUS Event Table must be Running</html>"));
        showEvNdName.setToolTipText(("<html>Display names from CBUS Event Table and CBUS Node Manager<br>CBUS Event Table and CBUS Node Manager must be started.</html>"));
        
    }
    
    /**
     * Add standard logging text and send to Console.
     * @param m CanReply or CanMessage
     * @throws IllegalArgumentException if not a CanFrame instance
     */
    public void addBiDirectionalInfo(AbstractMessage m) {
        if (!(m instanceof CanFrame)){
            throw new IllegalArgumentException(m + " is Not a CanFrame");
        }

        CanFrame msg = (CanFrame) m;
        updateMainStats( msg );
        
        StringBuilder output = new StringBuilder();
        
        appendIfChecked(showarrowsCheckBox, output, Bundle.getMessage((m instanceof CanMessage) ? "CBUS_OUT" : "CBUS_IN"));
        appendIfChecked(timeCheckBox, output, df.format(new Date()));
        
        if (!msg.isExtended() ) {
            appendIfChecked(showOpcCheckBox,output,CbusOpCodes.decodeopc(m));
        }
        
        if (msg.isExtended() || showEvNdName.isSelected() ) {
            output.append(CbusOpCodes.decode(m)).append(" ");
        }
        
        appendJmriBeans(output, m);
        
        processBiDiPt2(output, m, msg);
        
    }
    
    private void processBiDiPt2(@Nonnull StringBuilder output, 
        @Nonnull AbstractMessage m, @Nonnull CanFrame msg){
    
    if (CbusOpCodes.isKnownOpc(m) ) {
            appendIfChecked(showOpcNameCheckBox,output,Bundle.getMessage("CBUS_" + CbusOpCodes.decodeopc(m)));
            appendIfChecked(showOpcExtraCheckBox,output,Bundle.getMessage("CTIP_" + CbusOpCodes.decodeopc(m)));
        }
        
        appendIfChecked(showAddressCheckBox,output,Bundle.getMessage("addressGrouping",CbusMessage.toAddress(m))); 

        addPriority(m,msg,output);
        appendIfChecked(canidCheckBox,output,Bundle.getMessage("CanIDLabel",CbusMessage.getId(m)));                
        appendIfChecked(showCanCheckBox,output,msg.toString());
        appendIfChecked(showRtrCheckBox, output, Bundle.getMessage( msg.isRtr() ? "IsRtrFrame" : "IsNotRtrFrame"));

        // complete line and send to console
        output.append("\n");
        _mainPane.nextLine( (
            (m instanceof CanMessage) ? Bundle.getMessage("EventSent"): Bundle.getMessage("EventReceived") )
            + ": " + m.toMonitorString() + "\n", output.toString(),
            (_mainPane.displayPane.highlightFrame != null) ? _mainPane.displayPane.highlightFrame.highlight(m) : -1);
        
    }
    
    private void updateMainStats(CanFrame msg) {
        _mainPane.statsPane.incremenetTotal();
        if (CbusMessage.isEvent((AbstractMessage)msg)) {
            _mainPane.statsPane.incrementEvents();
        }
        if (!msg.isExtended() && CbusOpCodes.isDcc(CbusMessage.getOpcode((AbstractMessage)msg))) {
            _mainPane.statsPane.incrementDcc();
        }
    }
    
    private void addPriority( AbstractMessage m, CanFrame cfm, StringBuilder output ){
        if (priCheckBox.isSelected() && !cfm.isExtended()) {
            output.append(Bundle.getMessage("DynPriTitle")).append(": ")
                .append(CbusMessage.getPri(m) / 4).append(" ")
                .append(Bundle.getMessage("MinPriTitle")).append(": ")
                .append(CbusMessage.getPri(m) & 3).append(" ");
        }
    }
    
    private void appendJmriBeans(StringBuilder output, AbstractMessage m){
    
        if ( showJmriBeans.isSelected() && CbusMessage.isEvent(m)) {
            
            int en = CbusMessage.getEvent(m);
            int nn = CbusMessage.getNodeNumber(m);
            
            if (!showEvNdName.isSelected()) {
                output.append(new CbusEvent(nn,en));
            }
            output.append(nameService.getJmriBeans(nn, en, CbusEvent.getEvState(m))).append(" ");
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
        
        addBiDirectionalInfo(m);
                
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
        
        addBiDirectionalInfo(r);
        
    }
    
    public void dispose(){
        removeTc(_mainPane.tc);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusConsoleDecodeOptionsPane.class);
}
