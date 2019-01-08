package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatter;
import javax.swing.Timer;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusSend;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.swing.nodeconfig.NodeEditEventFrame;
import jmri.jmrix.can.TrafficController;
import jmri.util.swing.TextAreaFIFO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for setting node configuration.
 * <p>
 * Listens to requests for node numbers from modules, popup allocation.
 * Methods are certainly subject to change and should not be relied on at present.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018 2019
 * @since 2.3.1
 */
public class NodeConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    JButton selectNodeButton;
    JButton searchForNodesButton;
    private JPanel cards;
    protected JComboBox<String> nodeSelBox = new JComboBox<>();
    protected TextAreaFIFO tablefeedback = new TextAreaFIFO(1000);
    private JScrollPane scrolltablefeedback = new JScrollPane (tablefeedback);
    private JSplitPane split;
    private double _splitratio = 0.9;
    private JPanel p1 = new JPanel();
    protected static String ls = System.getProperty("line.separator");
    private JPanel selectNodePane = new JPanel();
    private JPanel propertiespane = new JPanel();
    private JPanel nvpane = new JPanel();
    private JPanel evpane = new JPanel();
    private JPanel nvListpane = new JPanel();
    private JPanel evListpane = new JPanel();
    private JScrollPane nvListpanescroll = new JScrollPane(nvListpane);
    private JScrollPane evListpanescroll = new JScrollPane(evListpane);
    
    private JPanel nvToppane = new JPanel();
    private JPanel evToppane = new JPanel();
    private JPanel nvbuildlist = new JPanel();
    private JPanel evbuildlist = new JPanel();
    private JPanel rqNNpane;
    private JLabel rqNNtext;
    
    private JToggleButton nvPaneSelectButton;
    private JToggleButton evPaneSelectButton;

    private JPanel nvorevpane = new JPanel();

    private JPanel nvorevbuttonpane = new JPanel();    
    private ButtonGroup group = new ButtonGroup();
    private JButton nodesupportlinkbutton = new JButton();
    private JButton writebutton;

    private int searchForNodesDelay=2000;
    private boolean WAITINGRESPONSE_STARTUPISANODEINSETUP=true;
    private boolean WAITINGRESPONSE_GETNODEPARAM=false;
    private boolean WAITINGRESPONSE_GETNV=false;
    private boolean WAITINGRESPONSE_SETNV=false;
    private boolean WAITINGRESPONSE_GET_NUM_EV=false;
    private boolean WAITINGRESPONSE_GET_EV_BY_INDEX=false;
    private boolean WAITINGRESPONSE_GET_EV_VAL=false;
    private boolean WAITINGRESPONSE_SET_EV_VAL=false;
    private boolean WAITINGRESPONSE_UNLEARN_EV=false;
    protected boolean RELEARN_WHEN_DELETED=false;
    private boolean WAITINGRESPONSE_RQNN_PARAMS=false;
    private boolean WAITINGRESPONSE_SNN=false;
    private boolean NODE_NUM_DIALOGUE_OPEN=false;
    
    protected int _nodeinsetup=0;
    private int _nodeparameters=0;
    private int _nextnodeparam=0;
    private int _nextnodenv=0;
    private int _numevents=0;
    private int _nodecanid;
    protected int _nextsetevvar;
    private int _evVarsReceived;
    
    private JLabel propertieslabel = new JLabel();
    private List<JSpinner> nvFields;
    private List<JLabel> nvToHex;
    private List<JButton> evEditButList = new ArrayList<JButton>();
    
    private URI supportlink;
    private ActionListener nodeParTotFListener;
    private Timer nodeparamtimer = null;
    private Timer setNVTimer;
    private Timer getEVNumandNodeTimer;
    private Timer setEvVarTimer;
    private Timer unlearnEvTimer;
    private Timer sNnTimer;
    private Timer onStartup; // not just startup, for a few other things as well atm, all way after startup though.

    
    private Timer getnumEvTimer;
    private Timer getEvVarTimer;
    
    private NodeEditEventFrame editevframe;

    private ArrayList<String> nodearr;
    private ArrayList<ArrayList<JLabel>> eventListRow;
    private ArrayList<JLabel> eventListCols = new ArrayList<JLabel>();
    private List<Integer> paramlist;
    private List<Integer> nvlist;
    protected ArrayList<CbusNodeEvent> _ndEvArr;
    private int _numEventResponsesOutstanding;

    private TrafficController tc;
    protected CbusSend send;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
        send = new CbusSend(memo,tablefeedback);
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " " + Bundle.getMessage("NodeConfigTitle"));
        }
        return Bundle.getMessage("NodeConfigTitle");
    }

    public NodeConfigToolPane() {
        super();
        
        nodearr = new ArrayList<String>(20);
        _ndEvArr = new ArrayList<CbusNodeEvent>();
        this.setLayout(new BorderLayout());

        p1.setBorder(BorderFactory.createTitledBorder((Bundle.getMessage("ChooseNode"))));
        
        searchForNodesButton = new JButton(Bundle.getMessage("Refresh"));
        searchForNodesButton.setToolTipText(Bundle.getMessage("RefreshNodes"));
        selectNodePane.add(searchForNodesButton);
        nodeSelBox.setEditable(false);
        nodeSelBox.setVisible(true);
        
        selectNodeButton = new JButton(Bundle.getMessage("ChooseNode"));
        selectNodeButton.setVisible(true);
        
        nvPaneSelectButton = new JToggleButton(Bundle.getMessage("NodeVariables"));
        evPaneSelectButton = new JToggleButton(Bundle.getMessage("CbusEvents"));
        
        writebutton = new JButton(Bundle.getMessage("WriteNVs"));
        
        selectNodePane.add(nodeSelBox);
        selectNodePane.add(selectNodeButton);
        
        nodesupportlinkbutton.setVisible(false);
        propertiespane.add(nodesupportlinkbutton);
        propertiespane.add(propertieslabel);

        propertiespane.setSize(propertiespane.getPreferredSize());

        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        p1.add(selectNodePane);
        p1.add(propertiespane);
        add(p1, BorderLayout.PAGE_START);        
        
        nodesupportlinkbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUri(supportlink);
            }
        });

        selectNodeButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                nodeselected();
            }
        });        
        
        searchForNodesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchfornodes();
            }
        });
        
        nvpane.setLayout(new BoxLayout(nvpane, BoxLayout.Y_AXIS));
        evpane.setLayout(new BoxLayout(evpane, BoxLayout.Y_AXIS));   

        nvToppane.setLayout(new BoxLayout(nvToppane, BoxLayout.X_AXIS));
        evToppane.setLayout(new BoxLayout(evToppane, BoxLayout.X_AXIS));
        
        nvListpane.setLayout(new BoxLayout(nvListpane, BoxLayout.Y_AXIS));   
        evListpane.setLayout(new BoxLayout(evListpane, BoxLayout.Y_AXIS));   
        
        nvListpanescroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        evListpanescroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        nvpane.add(nvListpanescroll);
        evpane.add(evListpanescroll);
        
        nvpane.setVisible(true);
        evpane.setVisible(true);
        
        nvListpanescroll.setVisible(true);
        evListpanescroll.setVisible(true);
        
        nvorevpane.setPreferredSize(new Dimension(800, 100));
        tablefeedback.setEditable (false);
        
        scrolltablefeedback.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        nvorevpane.setLayout(new BorderLayout());
        
        nvorevbuttonpane.setLayout(new GridLayout(1, 3));
        nvorevbuttonpane.add(nvPaneSelectButton);
        nvorevbuttonpane.add(evPaneSelectButton);
        nvorevbuttonpane.add(writebutton);
        nvorevpane.add(nvorevbuttonpane, BorderLayout.PAGE_START);

        group.add(nvPaneSelectButton);
        group.add(evPaneSelectButton);
        
        cards = new JPanel(new CardLayout());
        cards.add(nvpane, "NVP");
        cards.add(evpane, "EVP");
        nvorevpane.add(cards, BorderLayout.CENTER);
        
        writebutton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                writebuttonClicked();
            }
        }); 
        
        nvPaneSelectButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNvPane();
            }
        }); 
        
        evPaneSelectButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEvPane();
            }
        });
        
        Dimension scrolltablefeedbackminimumSize = new Dimension(150, 20);
        scrolltablefeedback.setMinimumSize(scrolltablefeedbackminimumSize);
        
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, nvorevpane, scrolltablefeedback);
        split.setContinuousLayout(true);
        add(split, BorderLayout.CENTER);

        enableAdminButtons(false);
        searchForNodesButton.setEnabled(true);
        tablefeedback.append(Bundle.getMessage("NodeConfigStartup"));
    }
    
    private void checkWriteButtonDirty(){
        if (nvPaneSelectButton.isSelected()) {
            for (int i=0 ; (i < paramlist.get(6)) ; i++){
                int newnv = (Integer) nvFields.get(i).getValue();
                if ((nvlist.get((i+1))) != newnv ) {
                    writebutton.setEnabled(true);
                    return;
                }
            }
            writebutton.setEnabled(false);
        }
    }
    
    // called following number of events response from node
    private void geteventsonmodulebyindex(){
        ActionListener getEVNumandNodeTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                tablefeedback.append( ls +Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem())); 
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem()), 
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }
        };
        getEVNumandNodeTimer = new Timer( searchForNodesDelay, getEVNumandNodeTimerTimeOut);
        getEVNumandNodeTimer.setRepeats( false );
        getEVNumandNodeTimer.start();

        WAITINGRESPONSE_GET_EV_BY_INDEX=true;
        _numEventResponsesOutstanding = _numevents;
        send.nERD(_nodeinsetup);
    }
    
    private void gettoteventsonmodule(){
        // tablefeedback.append("\nSending message requesting num events stored on module ");
        ActionListener getnumEvTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                tablefeedback.append( ls +Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem())); 
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem()), 
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }
        };
        getnumEvTimer = new Timer( searchForNodesDelay, getnumEvTimerTimeOut);
        getnumEvTimer.setRepeats( false );
        getnumEvTimer.start();
        WAITINGRESPONSE_GET_NUM_EV=true;
        send.rQEVN(_nodeinsetup);
    }
    
    private void writebuttonClicked(){
        if (nvPaneSelectButton.isSelected()) {
            // tablefeedback.append("\nWrite NV button clicked, looking for " +paramlist.get(6) + " NV's");
            int changedtot=0;
            StringBuffer buf = new StringBuffer();
            for (int i=0 ; (i < paramlist.get(6)) ; i++){
                int oldnv =  nvlist.get((i+1));
                int newnv = (Integer) nvFields.get(i).getValue();
                if (oldnv != newnv ) {
                    String listtext="<li>"+ Bundle.getMessage("NVFromTo",(i+1),oldnv,newnv) + "</li>";
                    tablefeedback.append( ls  + Bundle.getMessage("NVFromTo",(i+1),oldnv,newnv));
                    changedtot++;
                    buf.append(listtext);
                }
            }
            if ( changedtot > 0 ) {
                String s = "<html><ul>" + buf.toString() + "</ul></html>";
                int response = JOptionPane.showConfirmDialog(null,
                        (s),
                        (Bundle.getMessage("NVConfirmWrite",_nodeinsetup)), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response != JOptionPane.YES_OPTION) {
                    tablefeedback.append( ls+Bundle.getMessage("NVWriteCancelled"));
                    return;
                } else {
                    tablefeedback.append( ls +Bundle.getMessage("NVWriteStarted"));
                    _nextnodenv=0;
                    setnextnv();
                }
            }
        }
        else {
            CbusNodeEvent newevent = new CbusNodeEvent(-1,-1,_nodeinsetup,-1,paramlist.get(5));
            showEditDialogueEvent(this,newevent);
        }
    }

    private void setnextnv(){
        for (int i=_nextnodenv ; (i < (paramlist.get(6))) ; i++){
            int oldnv =  nvlist.get((i+1));
            int newnv = (Integer) nvFields.get(i).getValue();
            if (oldnv != newnv ) {
                WAITINGRESPONSE_SETNV=true;
                send.nVSET(_nodeinsetup, (i+1), newnv );
                
                ActionListener setNVTimerTimeOut = new ActionListener(){
                    @Override
                    public void actionPerformed( ActionEvent e ){           
                        tablefeedback.append( ls  + Bundle.getMessage("NVSetTimedout",nodeSelBox.getSelectedItem(),_nextnodenv));
                        JOptionPane.showMessageDialog(null, 
                            Bundle.getMessage("NVSetTimedout",nodeSelBox.getSelectedItem(),_nextnodenv), 
                            Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                        WAITINGRESPONSE_SETNV=false;
                        setnextnv();
                    }
                };
                Timer setNVTimer = new Timer( searchForNodesDelay, setNVTimerTimeOut);
                setNVTimer.setRepeats( false );
                setNVTimer.start();
               _nextnodenv=(i+1);
                return;
            }
        }
        tablefeedback.append( ls  + Bundle.getMessage("NVsSent"));
        updateNvPane();
    }
    
    private void updateNvPane(){
        CardLayout cardLayout = (CardLayout) cards.getLayout();
        cardLayout.show(cards, "NVP");
        enableAdminButtons(false);
        writebutton.setText(Bundle.getMessage("WriteNVs"));
        nvFields=null;
        nvFields = new ArrayList<>();
        nvToHex=null;
        nvToHex = new ArrayList<>();
        nvlist=null;
        nvlist = new ArrayList<>();
        nvlist.add(0); // add one here so loop starts at 1;
        nvListpane.remove(nvbuildlist);
        nvListpane.validate();
        nvListpane.repaint();
        nvbuildlist=null;
        nvbuildlist = new JPanel();
        nvbuildlist.setLayout(new BoxLayout(nvbuildlist, BoxLayout.Y_AXIS));
        nvListpane.add(nvbuildlist);
        _nextnodenv=1;     
        getindividnv();
    }
    
    private void updateEvPane(){
        evListpane.remove(evbuildlist);
        evListpane.validate();
        evListpane.repaint();
        eventListRow = null;
        eventListCols = null;
        evEditButList = null;
        eventListRow = new ArrayList<ArrayList<JLabel>>();
        _ndEvArr = new ArrayList<CbusNodeEvent>();
        eventListCols = new ArrayList<JLabel>();
        evEditButList = new ArrayList<JButton>();
        enableAdminButtons(false);
        CardLayout cardLayout = (CardLayout) cards.getLayout();
        cardLayout.show(cards, "EVP");
        writebutton.setText(Bundle.getMessage("NewEvent"));
        evbuildlist=null;
        evbuildlist = new JPanel();
        evbuildlist.setLayout(new BoxLayout(evbuildlist, BoxLayout.Y_AXIS));  
        evListpane.add(evbuildlist);
        gettoteventsonmodule();
    }

    private void getindividnv(){
        JLabel spacerlabel = new JLabel("<html> <br > </html>");
        JPanel spacer= new JPanel();
        spacer.add(spacerlabel);
        if (_nextnodenv==1) {
            nvbuildlist.add(spacer);
        }
        
        if (_nextnodenv <= (paramlist.get(6))) {
            WAITINGRESPONSE_GETNV=true;
            send.nVRD(_nodeinsetup,_nextnodenv);
            _nextnodenv++;
        }
        else {
            nvbuildlist.add(spacer);
            WAITINGRESPONSE_GETNV=false;
            tablefeedback.append( ls +Bundle.getMessage("NVsComplete"));
            split.setResizeWeight(_splitratio);
            enableAdminButtons(true);
            checkWriteButtonDirty();
            validate();
            repaint();
        }
    }
    
    private void searchfornodes(){
        disposeEditDialogueEvent();
        WAITINGRESPONSE_STARTUPISANODEINSETUP=true;
        send.nodeRequestParamSetup();
        
        nodearr=null;
        nodearr = new ArrayList<String>(20);
        searchForNodesButton.setText(Bundle.getMessage("SearchingNodes"));
        enableAdminButtons(false);
        propertieslabel.setText("");
        nodesupportlinkbutton.setVisible(false);
        split.setDividerLocation(0.1);
        nvorevpane.setVisible(false);
        nvPaneSelectButton.setSelected(true);
        
        send.searchForNodes();
        
        ActionListener searchForNodesTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                WAITINGRESPONSE_STARTUPISANODEINSETUP=false;
                int itemCount = nodearr.size();
                searchForNodesButton.setEnabled(true);
                tablefeedback.append( ls  + Bundle.getMessage("NodeSearchComplete",itemCount));
                searchForNodesButton.setText(Bundle.getMessage("Refresh"));
                nodeSelBox.removeAllItems();
                if (itemCount>0) {
                    Collections.sort(nodearr);
                    for (int i = 0; i < nodearr.size(); i++) {
                        nodeSelBox.addItem(nodearr.get(i));
                    }
                    selectNodeButton.setEnabled(true);
                    nodeSelBox.setEnabled(true);
                }
            }
        };
        
        Timer searchForNodesTimer = new Timer( searchForNodesDelay, searchForNodesTimerTimeOut);
        searchForNodesTimer.setRepeats( false );
        searchForNodesTimer.start();
    }
    
    // starts getnodeparametertotal
    private void nodeselected(){
        disposeEditDialogueEvent();
        String nodeName = (String)nodeSelBox.getSelectedItem();
        propertiespane.setVisible(true);
        nvorevpane.setVisible(true);
        paramlist=null;
        paramlist= new ArrayList<Integer>(21);
        split.setDividerLocation(_splitratio);
        propertiespane.setBorder(BorderFactory.createTitledBorder((Bundle.getMessage("CbusNode") + nodeName ))); 
        _nodeinsetup=getfirstintfromstring(nodeName);
        tablefeedback.append( ls  + Bundle.getMessage("NodeSelected",nodeName));
        if (_nodeinsetup>0) {
            getnodeparametertotal(_nodeinsetup);
        }
    }

    // called when node selected
    // sends rQNPN
    private void getnodeparametertotal(Integer _nodeinsetup) {
        // request total node parameters
        _nextnodeparam=1;
        WAITINGRESPONSE_GETNODEPARAM=true;

        nodeParTotFListener = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                tablefeedback.append( ls  + Bundle.getMessage("NodeNoResponseGetPar",(String)nodeSelBox.getSelectedItem()));
                nodeparamtimer.stop();
                nodeparamtimer=null;
                WAITINGRESPONSE_GETNODEPARAM=false;
            }
        };        
        
        nodeparamtimer = new Timer( searchForNodesDelay, nodeParTotFListener);
        nodeparamtimer.setRepeats( false );
        nodeparamtimer.start();
        
        send.rQNPN(_nodeinsetup,0);
        tablefeedback.append( ls  + (Bundle.getMessage("CBUS_RQNPN")));
    }

    // when all params got, update nv or ev pane
    private void getindividparam(){  // node parameters + build parameter pane
        if (_nextnodeparam < (_nodeparameters+1)) {
            send.rQNPN(_nodeinsetup,_nextnodeparam);
            _nextnodeparam++;
        } else if (_nextnodeparam == (_nodeparameters+1)){

            nodeparamtimer.stop();
            nodeparamtimer=null;
            WAITINGRESPONSE_GETNODEPARAM=false;
            
            StringBuilder nodepropbuilder = new StringBuilder(100);
            
            nodepropbuilder.append (CbusOpCodes.getManu(paramlist.get(1)));  
            nodepropbuilder.append (" ");
            nodepropbuilder.append( CbusOpCodes.getModuleType(paramlist.get(1),paramlist.get(3)));
            nodepropbuilder.append("<br>");
            nodepropbuilder.append( CbusOpCodes.getModuleTypeExtra(paramlist.get(1),paramlist.get(3)));
            
            nodepropbuilder.append ("<hr>");
            nodepropbuilder.append (Bundle.getMessage("FirmwareVer"));
            nodepropbuilder.append (paramlist.get(7));   
            int converttochar = paramlist.get(2);
            nodepropbuilder.append(Character.toString((char) converttochar));
            
            if ((paramlist.size()>19) && (paramlist.get(20)>0) ){
                nodepropbuilder.append (" "); 
                nodepropbuilder.append (Bundle.getMessage("FWBeta")); 
                nodepropbuilder.append (paramlist.get(20));
            }
            nodepropbuilder.append ("<br>"); 
            nodepropbuilder.append (Bundle.getMessage("CanID"));
            nodepropbuilder.append (" ");
            nodepropbuilder.append (_nodecanid);
            
            if ((paramlist.size()>9) && (paramlist.get(10)>0)) {
                nodepropbuilder.append (" ");             
                nodepropbuilder.append (CbusOpCodes.getBusType(paramlist.get(10)));
                nodepropbuilder.append (" ");
                nodepropbuilder.append (Bundle.getMessage("BusType"));
            }

            nodesupportlinkbutton.setText("<html>" + CbusOpCodes.getManu(paramlist.get(1)) + 
            "<br>" +  CbusOpCodes.getModuleType(paramlist.get(1),paramlist.get(3)) + 
            "<br>" + Bundle.getMessage("Support") + "</html>");
            
            String supportLinkStr = CbusOpCodes.getModuleSupportLink(paramlist.get(1),paramlist.get(3));
            
            if ( supportLinkStr.length( ) > 0 ) {
                nodesupportlinkbutton.setToolTipText(supportLinkStr);
                try {
                    supportlink=new URI(supportLinkStr);
                } 
                catch (URISyntaxException ex) {
                    log.warn("Unable to create support link URI for module type {} {}", paramlist.get(3), ex);
                }
                nodesupportlinkbutton.setVisible(true);
            } else {
                nodesupportlinkbutton.setVisible(false);
            }
       
            propertieslabel.setText("<html>" + nodepropbuilder.toString() + "</html> ");
            propertiespane.setSize(propertiespane.getPreferredSize());
            propertiespane.validate();
            nvorevpane.setVisible(true);
            if (nvPaneSelectButton.isSelected()) {
                updateNvPane();
            } else {
                updateEvPane();
            }
        }
    }
    
    public void addnvtolist(int nvid){
        JLabel spacerlabel = new JLabel("<html> <br > </html>");
        JPanel spacer= new JPanel();
        spacer.add(spacerlabel);
        
        JPanel individnv= new JPanel(); // row container

        int nvval=nvlist.get(nvid);
        
        String currnvvalplace = "<html>" + Bundle.getMessage("OPC_NV") + " " + nvid +
        " :<span style='background-color:white'>  <b>" + 
        nvlist.get(nvid)  + " </b>  </span> <html>";
        JLabel currnvvaltxt = new JLabel(currnvvalplace);
        currnvvaltxt.setToolTipText(Bundle.getMessage("ButtonDecimal"));
        
        JLabel nvhexlabel = new JLabel("", JLabel.CENTER);

        if ((nvlist.get(nvid)) > 0 ) {
            nvhexlabel.setToolTipText(Bundle.getMessage("toolTipHexDec"));
        }
        nvhexlabel.setText(showformats(nvlist.get(nvid)));
        
        JSpinner numberSpinner = new JSpinner(new SpinnerNumberModel(nvval, 0, 255, 1));
        nvFields.add(numberSpinner);                   
        numberSpinner.setToolTipText(Bundle.getMessage("OPC_NV") + " " + nvid + " " + Bundle.getMessage("ButtonDecimal"));
        
        JComponent comp = numberSpinner.getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
        nvFields.get(Integer.valueOf(nvid-1)).addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkWriteButtonDirty();
                int newval = (Integer) numberSpinner.getValue();
                if ( newval == nvlist.get(nvid) ) {
                    nvToHex.get(Integer.valueOf(nvid-1)).setText("");
                    nvToHex.get(Integer.valueOf(nvid-1)).setToolTipText("");
                    field.setBackground(Color.white);
                } else {
                    nvToHex.get(Integer.valueOf(nvid-1)).setText(showformats(newval));
                    nvToHex.get(Integer.valueOf(nvid-1)).setToolTipText(Bundle.getMessage("toolTipHexDec"));
                    field.setBackground(Color.yellow);
                }
            }
        });
        
        JLabel newnvhexlabel = new JLabel("", JLabel.CENTER);
        nvToHex.add(newnvhexlabel);
        
        individnv.setLayout(new GridLayout(1, 6));
        
        individnv.add(new JLabel(" "));
        individnv.add(currnvvaltxt);
        individnv.add(nvhexlabel);
        individnv.add(new JLabel(Bundle.getMessage("New") + " ",SwingConstants.RIGHT));
        individnv.add(numberSpinner);            
        individnv.add(newnvhexlabel);
        individnv.addMouseListener(new HighlightJPanelsChildMouseListeners());
        
        nvbuildlist.add(individnv);

        if(nvid % 5 == 0){
            nvbuildlist.add(spacer);
        }
        
        nvListpane.validate(); // more GUI intenseive but gives visual clue to user than it's a full refresh
    }
    
    // get ev by ev index
    public void startReval(int nextev, int nextevvar){
        WAITINGRESPONSE_GET_EV_VAL=true;
        ActionListener getEvVarTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                tablefeedback.append( ls  + Bundle.getMessage("NodeNoRespGetEvVar",nodeSelBox.getSelectedItem(),nextev));
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NodeNoRespGetEvVar",nodeSelBox.getSelectedItem(),nextev), 
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
                WAITINGRESPONSE_GET_EV_VAL=false;
                getEvVarTimer=null;
            }
        };
        getEvVarTimer = new Timer( searchForNodesDelay, getEvVarTimerTimeOut);
        getEvVarTimer.setRepeats( false );
        getEvVarTimer.start();
        send.rEVAL(_nodeinsetup, nextev, nextevvar );
    }
    
    // called when all empty event rows have been created
    // and from response to get the next event variable
    public void getEvVarsByEv(){
        _evVarsReceived++;
        int tot = _evVarsReceived/paramlist.get(5);
        if ( ( tot > 0 ) && ( tot % 10 == 0 ) && ( tot < _numevents ) ) {
            tablefeedback.append( ls  + Bundle.getMessage("GetEventsUpdate",tot,_numevents));
        }
        for (int i = 0; i < _ndEvArr.size(); i++) {
            for (int j = 0; j < paramlist.get(5); j++) {
                if ( _ndEvArr.get(i).getEvVar(j+1) < 0 ) {
                    startReval(_ndEvArr.get(i).getIndex(),j+1);
                    return;
                }
            }
            evEditButList.get(i).setEnabled(true);
        }
        tablefeedback.append( ls  + Bundle.getMessage("GetEventsComplete",(_numevents),_numevents));
        enableAdminButtons(true);
    }

    public void enableAdminButtons(boolean trueorfalse) {
        selectNodeButton.setEnabled(trueorfalse);
        searchForNodesButton.setEnabled(trueorfalse);
        nvPaneSelectButton.setEnabled(trueorfalse);
        evPaneSelectButton.setEnabled(trueorfalse);
        nodeSelBox.setEnabled(trueorfalse);
        writebutton.setEnabled(trueorfalse);
        for ( int i=0 ; (i <evEditButList.size() ) ; i++){
            evEditButList.get(i).setEnabled(trueorfalse);
        }   
    }
    
    // frame to edit / create event
    public void showEditDialogueEvent(NodeConfigToolPane tp, CbusNodeEvent ndEv) {
        editevframe = new NodeEditEventFrame(tp,ndEv);
        editevframe.initComponents();
    }
    
    protected void disposeEditDialogueEvent(){
        if (editevframe != null ) {
            editevframe.dispose();
        }
        editevframe = null;
    }
    
    protected void sendunlearn(int newevent, int newvalnd){
        // node should already be iin learn mode
        // EVULN
        WAITINGRESPONSE_UNLEARN_EV=true;
        send.nodeUnlearnEvent( newvalnd, newevent );
        
        ActionListener UnlearnEvListener = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                tablefeedback.append(Bundle.getMessage("NdDelEvConfrm",newevent,newvalnd,nodeSelBox.getSelectedItem()));
                unlearnEvTimer.stop();
                unlearnEvTimer=null;
                WAITINGRESPONSE_UNLEARN_EV=false;
                if (RELEARN_WHEN_DELETED) {
                    setEvVarLoop();
                }
                else {
                    send.nodeExitLearnEvMode(_nodeinsetup);
                    // delay then refresh
                    onStartup = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            // log.debug("setEvVarLoop Complete close window + refresh event list");  
                            onStartup.stop();
                            onStartup=null;
                            editevframe.dispose();
                            editevframe=null;
                            enableAdminButtons(true);
                            updateEvPane();
                            send.nodeExitLearnEvMode(_nodeinsetup); // no harm in sending again due to earlier failure
                        }
                    });
                    onStartup.setRepeats(false); // Only execute once
                    onStartup.start();
                }
            }
        };
        unlearnEvTimer = new Timer( searchForNodesDelay, UnlearnEvListener);
        unlearnEvTimer.setRepeats( false );
        unlearnEvTimer.start();
    }
    
    protected void setEvVarLoop(){
        if ( _nextsetevvar <= paramlist.get(5) ) {
            int newval = editevframe.getEvVar( _nextsetevvar+1 );
            int newevent = editevframe.getEventVal();
            int newvalnd = editevframe.getNodeVal();
            // send teach message
            WAITINGRESPONSE_SET_EV_VAL=true;
            tablefeedback.append( ls  + Bundle.getMessage("NdTeachEv",nodeSelBox.getSelectedItem(),
                newevent,newvalnd,(_nextsetevvar),newval));
            ActionListener SetEvModeListener = new ActionListener(){
                @Override
                public void actionPerformed( ActionEvent e ){
                    tablefeedback.append( ls  +
                    Bundle.getMessage("NdEvVarTimeout",
                    nodeSelBox.getSelectedItem(),newevent,newvalnd,_nextsetevvar,newval));
                    setEvVarTimer.stop();
                    setEvVarTimer=null;
                    WAITINGRESPONSE_SET_EV_VAL=false;
                    JOptionPane.showMessageDialog(null, 
                        Bundle.getMessage("NdEvVarTimeout",
                            nodeSelBox.getSelectedItem(),newevent,newvalnd,_nextsetevvar,newval),
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    setEvVarLoop(); // carry on trying or give up?
                }
            };
            setEvVarTimer = new Timer( searchForNodesDelay, SetEvModeListener);
            setEvVarTimer.setRepeats( false );
            setEvVarTimer.start();
            
            send.nodeTeachEventLearnMode( newvalnd, newevent, _nextsetevvar, newval );
            
        }
        else {
            send.nodeExitLearnEvMode(_nodeinsetup);
            onStartup = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    tablefeedback.append( ls  + Bundle.getMessage("NdCompleteEvVar"));
                    
                    editevframe.dispose();
                    editevframe=null;
                    enableAdminButtons(true);
                    updateEvPane();
                    onStartup.stop();
                    onStartup=null;
                }
            });
            onStartup.setRepeats(false); // Only execute once
            onStartup.start();
        }
        _nextsetevvar++;
    }
    
    public void openEditButton(CbusNodeEvent thisevent ){
        showEditDialogueEvent(this,thisevent);
    }
    
    // if all table event rows received, start getting event vars
    private void addEventTableRow(int eventNode, int eventNum, int eventIndex){
        CbusNodeEvent thisevent = new CbusNodeEvent(eventNode,eventNum,_nodeinsetup,eventIndex,paramlist.get(5));
        _ndEvArr.add(thisevent);
        _numEventResponsesOutstanding--;
        
        JLabel spacerlabel = new JLabel("<html> <br > </html>");
        JPanel spacer= new JPanel();
        spacer.setLayout(new GridLayout(1,1));
        spacer.add(spacerlabel);
        
        JPanel headings= new JPanel(); // row container
        headings.setLayout(new GridLayout(1, ( 3 + paramlist.get(5))));
        headings.add(new JLabel(Bundle.getMessage("CbusEvent"), JLabel.CENTER));
        headings.add(new JLabel(Bundle.getMessage("CbusNode"), JLabel.CENTER));
        headings.add(new JLabel("")); // edit button column        
        for (int i=1 ; (i <= paramlist.get(5)) ; i++){
            headings.add(new JLabel("V" + i, JLabel.CENTER));
        }

        // table header row
        if (_numEventResponsesOutstanding+1==_numevents) {
            evbuildlist.add(spacer);
            evbuildlist.add(headings);
        }
        
        if ( (_numevents - (_numEventResponsesOutstanding +1 ) ) % 5 == 0 ) {
            evbuildlist.add(spacer);
            evbuildlist.add(headings);
        }
        
        JPanel individev= new JPanel(); // row container

        JLabel evData2 = new JLabel("" + (eventNode) + "", JLabel.CENTER);
        JLabel evData3 = new JLabel("" + (eventNum) + "", JLabel.CENTER);
        JButton editEVButton = new JButton(Bundle.getMessage("Edit"));
        editEVButton.setVisible(true);
        editEVButton.setEnabled(false);
        evEditButList.add(editEVButton);
        individev.setLayout(new GridLayout(1, (3 + paramlist.get(5))));
        eventListCols = new ArrayList<JLabel>();
        
        individev.add(evData3);
        individev.add(evData2);
        eventListCols.add(evData2);
        eventListCols.add(evData3);
        individev.add(editEVButton);
        
        for (int i=1 ; (i <= paramlist.get(5)) ; i++){
            JLabel evvalLabel = new JLabel(".", JLabel.CENTER);
            eventListCols.add(evvalLabel);
            individev.add(evvalLabel);
        }
        
        editEVButton.addActionListener (new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                // log.debug("edit clicked {} ",(eventIndex-1));
                
                openEditButton(thisevent);
            }
        });

        eventListRow.add(eventListCols);
        thisevent.setNodeConfigPanelID(eventListRow.size()-1);
        
        individev.addMouseListener(new HighlightJPanelsChildMouseListeners());
        evbuildlist.add(individev);
        
        if ( _numEventResponsesOutstanding == 0 ) {
            getEVNumandNodeTimer.stop();
            WAITINGRESPONSE_GET_EV_BY_INDEX=false;
            evbuildlist.add(spacer);

            _evVarsReceived=-1;
            getEvVarsByEv();
            tablefeedback.append( ls  + Bundle.getMessage("NdEvDone"));
        }
        evListpane.validate();
        evListpane.repaint();
    }
    
    public CbusNodeEvent getNodeEventByIndex(int index) {
        for (int i = 0; i < _ndEvArr.size(); i++) {
            if ( _ndEvArr.get(i).getIndex() == index ) {
                return _ndEvArr.get(i);
            }
        }
        return null;
    }
    
    private void startnodeallocation(int nn) {
        if (NODE_NUM_DIALOGUE_OPEN) {
            return;
        }
        NODE_NUM_DIALOGUE_OPEN=true;
        
        tablefeedback.append( ls  + Bundle.getMessage("NdRqNn",nn));
        rqNNpane = new JPanel();
        JPanel bottomrqNNpane = new JPanel();
        String spinnerlabel=Bundle.getMessage("NdRqNnSelect");
        JLabel rqNNspinnerlabel = new JLabel(spinnerlabel);
        
        bottomrqNNpane.setLayout(new GridLayout(2, 1));
        rqNNpane.setLayout(new BorderLayout());
        rqNNtext = new JLabel(Bundle.getMessage("NdRqNdDetails"));
        
        String popuplabel;
        
        if (nn==0) {
            popuplabel=Bundle.getMessage("NdEntrSlimTitle");
        } else {
            popuplabel=Bundle.getMessage("NdEntrNumTitle",nn);
        }
        
        int newnn = 256;
        for (int i = 0; i < nodearr.size(); i++) {
            if ( newnn == getfirstintfromstring(nodearr.get(i)) ) {
                newnn++;
            }
        }
        
        JSpinner rqnnSpinner = new JSpinner(new SpinnerNumberModel(newnn, 1, 65535, 1));
        rqnnSpinner.setToolTipText((Bundle.getMessage("ToolTipNodeNumber")));
        JComponent rqcomp = rqnnSpinner.getEditor();
        JFormattedTextField rqfield = (JFormattedTextField) rqcomp.getComponent(0);
        DefaultFormatter rqformatter = (DefaultFormatter) rqfield.getFormatter();
        rqformatter.setCommitsOnValidEdit(true);
        rqfield.setBackground(Color.white);
        rqnnSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newval = (Integer) rqnnSpinner.getValue();
                // existing module array list
                String reservedmodtext = CbusOpCodes.getReservedModule(newval);
              
                if (!reservedmodtext.isEmpty()) {
                    rqNNspinnerlabel.setText(reservedmodtext);
                    rqfield.setBackground(Color.yellow);
                }
                else {
                    rqNNspinnerlabel.setText(spinnerlabel);
                    rqfield.setBackground(Color.white);
                }              
              
                for (int i = 0; i < nodearr.size(); i++) {
                    if ( newval == getfirstintfromstring(nodearr.get(i)) ) {
                        rqNNspinnerlabel.setText(Bundle.getMessage("NdNumInUse",(nodearr.get(i))));
                        rqfield.setBackground(Color.red);
                    }
                }
            }
        });
        
        rqNNpane.add(rqNNtext, BorderLayout.CENTER);
        bottomrqNNpane.add(rqNNspinnerlabel);
        bottomrqNNpane.add(rqnnSpinner);
        
        rqNNpane.add(bottomrqNNpane, BorderLayout.PAGE_END);
        
        Toolkit.getDefaultToolkit().beep();
        
        WAITINGRESPONSE_RQNN_PARAMS=true;
        send.nodeRequestParamSetup();
        
        int option = JOptionPane.showOptionDialog(null, 
            rqNNpane, 
            popuplabel, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (option == JOptionPane.CANCEL_OPTION) {
            tablefeedback.append( ls  + Bundle.getMessage("NnAllocCancel"));
            WAITINGRESPONSE_RQNN_PARAMS=false;
            NODE_NUM_DIALOGUE_OPEN=false;
        } else if (option == JOptionPane.OK_OPTION) {
            int newval = (Integer) rqnnSpinner.getValue();
            tablefeedback.append( ls  + Bundle.getMessage("NnAllocSelected",newval));
            
            sNnTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    WAITINGRESPONSE_SNN=false;
                    tablefeedback.append( ls  + "No Confirmation of Setting Node Number ");
                    JOptionPane.showMessageDialog(null, 
                        Bundle.getMessage("NnAllocError",newval), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    sNnTimer.stop();
                    sNnTimer=null;
                    NODE_NUM_DIALOGUE_OPEN=false;
                }
            });
            sNnTimer.setRepeats(false);
            sNnTimer.start();            
            
            WAITINGRESPONSE_SNN=true;
            send.nodeSetNodeNumber(newval);
        }
    }
    
    @Override
    public void reply(CanReply m) {
        int opc = CbusMessage.getOpcode(m);
        int nn = ( m.getElement(1) * 256 ) + m.getElement(2);
        
        if (opc==CbusConstants.CBUS_RQNN){
            startnodeallocation(nn);
        }        
        else if (opc==CbusConstants.CBUS_NNREL) {
            int canid = CbusMessage.getId(m);
            tablefeedback.append( ls  + Bundle.getMessage("NdRelease",nn,canid));
            int response = JOptionPane.showConfirmDialog(null,
                    ("<html>" + Bundle.getMessage("NdRelease",nn,canid) + 
                    "<br>" + Bundle.getMessage("NdRefreshListQ") + "</html>"),
                    Bundle.getMessage("NdReleaseTitle"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                searchfornodes();
            } else {
                return;
            }
        }
        if (WAITINGRESPONSE_SNN) {
            if (opc==CbusConstants.CBUS_NNACK) {
                // node number acknowledged
                sNnTimer.stop();
                sNnTimer=null;
                WAITINGRESPONSE_SNN=false;
                NODE_NUM_DIALOGUE_OPEN=false;
                tablefeedback.append( ls  + Bundle.getMessage("NnAllocConfirm",nn));
                int response = JOptionPane.showConfirmDialog(null,
                    ("<html>" + Bundle.getMessage("NnAllocConfirm",nn) + 
                    "<br>" + Bundle.getMessage("NdRefreshListQ") + "</html>"),
                        Bundle.getMessage("NnAllocConfirmTitl"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    searchfornodes();
                } else {
                    return;
                }
                
            }
        }
        if (WAITINGRESPONSE_RQNN_PARAMS) {
            if (opc==CbusConstants.CBUS_PARAMS) {
                StringBuilder nodepropbuilder = new StringBuilder(40);
                nodepropbuilder.append (CbusOpCodes.getManu(m.getElement(1)));  
                nodepropbuilder.append (" ");
                nodepropbuilder.append( CbusOpCodes.getModuleType(m.getElement(1),m.getElement(3)));
                rqNNtext.setText(nodepropbuilder.toString());
                WAITINGRESPONSE_RQNN_PARAMS=false;
            }
        }
        if (WAITINGRESPONSE_UNLEARN_EV) {
            if ( opc == CbusConstants.CBUS_WRACK) {
                WAITINGRESPONSE_UNLEARN_EV=false;
                unlearnEvTimer.stop();
                unlearnEvTimer=null;
                tablefeedback.append( ls  + Bundle.getMessage("NdModUnlrnConfrm"));
                send.nodeExitLearnEvMode(_nodeinsetup);
                
                // delay then refresh
                onStartup = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        //  tablefeedback.append( ls  + "Completed teaching event variables"); 
                        onStartup.stop();
                        onStartup=null;
                        if (RELEARN_WHEN_DELETED) {
                            tablefeedback.append( ls +Bundle.getMessage("NdModStartLrn"));
                            send.nodeEnterLearnEvMode( _nodeinsetup);
                            // delay then refresh
                            onStartup = new Timer(1000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent arg0) {
                                    setEvVarLoop();
                                    onStartup.stop();
                                    onStartup=null;
                                }
                            });
                            onStartup.setRepeats(false); // Only execute once
                            onStartup.start();   
                        }
                        else {
                            log.warn("node exit learn mode should close window here");
                            editevframe.dispose();
                            updateEvPane();
                        }
                    }
                });
                onStartup.setRepeats(false); // Only execute once
                onStartup.start();
            }
            else if ( opc == CbusConstants.CBUS_CMDERR ) {
                WAITINGRESPONSE_UNLEARN_EV=false;
                setEvVarTimer.stop();
                setEvVarTimer=null;
                tablefeedback.append( ls +Bundle.getMessage("NdDelEvErr") );
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NdDelEvErr"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
                send.nodeExitLearnEvMode(_nodeinsetup);
            }
        }
        if (WAITINGRESPONSE_SET_EV_VAL) {
            if ( opc == CbusConstants.CBUS_WRACK) {
                tablefeedback.append( ls +Bundle.getMessage("NdCnfrmWrite"));
                WAITINGRESPONSE_SET_EV_VAL=false;
                setEvVarTimer.stop();
                setEvVarTimer=null;
                setEvVarLoop();
            }
            else if ( opc == CbusConstants.CBUS_CMDERR ) {
                WAITINGRESPONSE_SET_EV_VAL=false;
                setEvVarTimer.stop();
                setEvVarTimer=null;
                tablefeedback.append( ls  + Bundle.getMessage("NdEvVarWriteError"));
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NdEvVarWriteError"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
                setEvVarLoop(); // or stop write process?
            }
        }
        if (WAITINGRESPONSE_STARTUPISANODEINSETUP) {
            if (opc==CbusConstants.CBUS_PARAMS) { // response from the rqnp sent
                if (!NODE_NUM_DIALOGUE_OPEN) {
                    int canid = CbusMessage.getId(m);
                    tablefeedback.append( ls  + Bundle.getMessage("NdAlreadySetup",canid));
                    int response = JOptionPane.showConfirmDialog(null,
                            ("<html>" + Bundle.getMessage("NdAlreadySetup",canid) + "</html>"),
                            Bundle.getMessage("NdAlreadySetpTitl"), JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE);
                    if (response == JOptionPane.YES_OPTION) {
                        startnodeallocation(0);
                    } else {
                        return;
                    }
                }
            }

            if (opc==CbusConstants.CBUS_PNN) {
                int manu = m.getElement(3);
                int elfour = m.getElement(4);
                nodearr.add(nn + " " + CbusOpCodes.getModuleType(manu,elfour));
                tablefeedback.append( ls  + Bundle.getMessage("CBUS_IN") + " ");
                tablefeedback.append(Bundle.getMessage("CbusNode") + nn);            
                tablefeedback.append(" " + CbusOpCodes.getManu(manu));
                tablefeedback.append(" " + CbusOpCodes.getModuleType(manu,elfour));
                // tablefeedback.append(" " + (m.getElement(5)));
                
                // ensure node is NOT in learn event mode
                send.nodeExitLearnEvMode(nn);
            }
        }
        if (Objects.equals(nn,_nodeinsetup)) {
            if (WAITINGRESPONSE_GETNV) {
                if (opc == CbusConstants.CBUS_NVANS) {
                    nvlist.add(0);
                    nvlist.set(m.getElement(3),m.getElement(4));
                    addnvtolist(m.getElement(3));
                    // get the next one
                    getindividnv();
                }
            }
            
            if (WAITINGRESPONSE_GET_NUM_EV) {
                if (opc == CbusConstants.CBUS_NUMEV) {
                    getnumEvTimer.stop();
                    getnumEvTimer=null;
                    _numevents =  m.getElement(3);
                    WAITINGRESPONSE_GET_NUM_EV=false;
                    tablefeedback.append( ls  + Bundle.getMessage("GotNumEvents",_numevents));
                    if ( _numevents > 0 ) {
                        geteventsonmodulebyindex();
                    } else {
                        enableAdminButtons(true);
                    }
                }
            }
            
            if (WAITINGRESPONSE_GETNODEPARAM) {
                if (opc == CbusConstants.CBUS_PARAN) {
                    if (m.getElement(3)==0) {
                        _nodeparameters = m.getElement(4);
                        _nodecanid=CbusMessage.getId(m);
                        paramlist.add(0);
                    } else {
                        paramlist.add(0);
                        paramlist.set((m.getElement(3)),(m.getElement(4)));
                    }
                    getindividparam();
                }
            }
            
            if (WAITINGRESPONSE_SETNV) {
                setNVTimer.stop();
                setNVTimer=null;
                if (opc == CbusConstants.CBUS_WRACK ) { // WRACK Node Acknowledge response
                    tablefeedback.append( ls  + Bundle.getMessage("NVSetConfirm",_nextnodenv));
                    WAITINGRESPONSE_SETNV=false;
                    setnextnv();
                }
                else if ( opc == CbusConstants.CBUS_CMDERR) { // CMDERR error writing to node
                    tablefeedback.append( ls  + Bundle.getMessage("NVSetFail",_nextnodenv));
                    WAITINGRESPONSE_SETNV=false;
                    JOptionPane.showMessageDialog(null, 
                        Bundle.getMessage("NVSetFailTitle"), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    setnextnv(); // or cancel operation?
                }
            }
        
            if (WAITINGRESPONSE_GET_EV_BY_INDEX) {
                if (opc == CbusConstants.CBUS_ENRSP) {
                    addEventTableRow((
                        (m.getElement(3) * 256 ) + m.getElement(4)),
                        ((m.getElement(5) * 256 ) + m.getElement(6)),
                        m.getElement(7));
                }
            }
        
            if (WAITINGRESPONSE_GET_EV_VAL) {
                if (opc == CbusConstants.CBUS_NEVAL) {
                    WAITINGRESPONSE_GET_EV_VAL=false;
                    getEvVarTimer.stop();
                    getEvVarTimer=null;
                    int index = m.getElement(3);
                    int ev = m.getElement(4);
                    int val = m.getElement(5);
                    getNodeEventByIndex(index).setEvVar(ev,val);
                    String builder="";
                    if ( val > 0 ) {
                      builder = ("<html><span style='background-color:white'> <b> " + val + " </b> </span></html>");
                    }
                    eventListRow.get(getNodeEventByIndex(index).getNodeConfigPanelID()).get((ev+1)).setText(builder);
                    evListpane.validate();
                    // get the next one
                    getEvVarsByEv();
                }
            }
        }
    }
    
    /**
     * Do nothing with outgoing messages so simulators can be tested alongside real hardware
     */
    @Override
    public void message(CanMessage m) {
    }

    public final static String showformats(int num){
        if (num>0) {
            return ("<html> (<span style='background-color:white'>  <b> " + 
                String.valueOf(Integer.toHexString(num)) + 
                " </span> ) " + 
                (String.format("%8s", Integer.toBinaryString(num)).replace(' ', '0')).substring(0,4) + " " +
                (String.format("%8s", Integer.toBinaryString(num)).replace(' ', '0')).substring(4,8) + " </html>");
        }
        return "";
    }

    public static class HighlightJPanelsChildMouseListeners implements MouseListener{
        public HighlightJPanelsChildMouseListeners() {
        }
        public void mouseEntered(MouseEvent e) {
            JPanel parent = (JPanel)e.getSource();
            parent.setBackground(Color.YELLOW);
            parent.revalidate();
        }
        public void mouseExited(MouseEvent e) {
            JPanel parent = (JPanel)e.getSource();
            parent.setBackground(new JPanel().getBackground());
            parent.revalidate();
        }
        public void mousePressed(MouseEvent e){}
        public void mouseReleased(MouseEvent e){}
        public void mouseClicked(MouseEvent e) {}
    }
    
    private static void openUri(URI uri) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e) {
                log.warn("Unable to get URI for {} {}", uri, e);
            }
        }
    }


    public static final int getfirstintfromstring(String toTest){
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<toTest.length(); i++){
            Character chars = toTest.charAt(i);
            if(chars != ' '){
                if(Character.isDigit(chars)){
                    buf.append(chars);
                } else {
                    if (buf.length()>0) {
                        break;
                    }
                }
            }
        }
        if (buf.length()==0) {
            buf.append("0");
        }
        return (Integer.parseInt(buf.toString()));  
    }
    

    @Override
    public void dispose() {
        // disconnect from the CBUS
        if (tc != null) {
            tc.removeCanListener(this);
        }
        tablefeedback.dispose();
    }
    
    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super(Bundle.getMessage("NodeConfigTitle"),
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NodeConfigToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }
    private final static Logger log = LoggerFactory.getLogger(NodeConfigToolPane.class);
}
