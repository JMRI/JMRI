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
import java.awt.event.WindowEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.Element;
import javax.swing.Timer;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
import jmri.jmrix.can.TrafficController;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for setting node configuration.
 * <p>
 * Listens to requests for node numbers from modules, popup allocation.
 * Methods are certainly subject to change and should not be relied on at present.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 * @since 2.3.1
 */
public class NodeConfigToolPane extends jmri.jmrix.can.swing.CanPanel implements CanListener {

    JButton selectNodeButton;
    JButton searchForNodesButton;
    private JPanel cards;
    private JComboBox<String> nodeSelBox = new JComboBox<>();
    private static TextAreaFIFO tablefeedback = new TextAreaFIFO(1000);
    private JScrollPane scrolltablefeedback = new JScrollPane (tablefeedback);
    private JSplitPane split;
    private double _splitratio = 0.9;
    private JPanel p1 = new JPanel();
 
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
    private JButton framedeletebutton;
    private JButton frameeditevbutton;
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
    private boolean RELEARN_WHEN_DELETED=false;
    private boolean WAITINGRESPONSE_RQNN_PARAMS=false;
    private boolean WAITINGRESPONSE_SNN=false;
    
    private static int _nodeinsetup=0;
    private int _nodeparameters=0;
    private int _nextnodeparam=0;
    private int _nextnodenv=0;
    private int _numevents=0;
    private int _nodecanid;
    private int _nextev;
    private int _nextevvar;
    private int _nextsetevvar;
    private JLabel propertieslabel = new JLabel();
    private List<JSpinner> nvFields;
    private List<JSpinner> evFields;
    private List<JLabel> nvToHex;
    private List<JLabel> evToHex;
    private List<JButton> evEditButList = new ArrayList<JButton>();
    
    private JSpinner numberSpinnerEv;
    private JSpinner numberSpinnernd;
    
    private URI supportlink;
    private String toolTipHexDec="<html>(<span style='background-color:white'> Hex </span>) Binary</html>";
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
    
    private JmriJFrame editevframe;

    private ArrayList<String> nodearr = new ArrayList<String>(20);
    private ArrayList<ArrayList<JLabel>> eventListRow = new ArrayList<ArrayList<JLabel>>();
    private ArrayList<JLabel> eventListCols = new ArrayList<JLabel>();
    private List<Integer> paramlist;
    private List<Integer> nvlist;

    TrafficController tc;

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        super.initComponents(memo);
        tc = memo.getTrafficController();
        tc.addCanListener(this);
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
        
        /*  Uncomment to start node search on startup
        Timer onStartup = new Timer(250, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                searchfornodes();
                onStartup.stop();
                onStartup=null;
            }
        });
        onStartup.setRepeats(false);
        onStartup.start();
        
        */
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
    
    private void geteventsonmodulebyindex(){
        ActionListener getEVNumandNodeTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                tablefeedback.append("\n"+Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem())); 
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem()), 
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }
        };
        getEVNumandNodeTimer = new Timer( searchForNodesDelay, getEVNumandNodeTimerTimeOut);
        getEVNumandNodeTimer.setRepeats( false );
        getEVNumandNodeTimer.start();
        
        // tablefeedback.append("\nSending message requesting each event on module to get event / node.");
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NERD);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        WAITINGRESPONSE_GET_EV_BY_INDEX=true;
        tc.sendCanMessage(m, null);        
    }
    
    private void gettoteventsonmodule(){
        // tablefeedback.append("\nSending message requesting num events stored on module ");
        ActionListener getnumEvTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){           
                tablefeedback.append("\n"+Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem())); 
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NodeNoResponseGetEv",(String)nodeSelBox.getSelectedItem()), 
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            }
        };
        getnumEvTimer = new Timer( searchForNodesDelay, getnumEvTimerTimeOut);
        getnumEvTimer.setRepeats( false );
        getnumEvTimer.start();

        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQEVN);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        WAITINGRESPONSE_GET_NUM_EV=true;
        tc.sendCanMessage(m, null);
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
                    tablefeedback.append("\n" + Bundle.getMessage("NVFromTo",(i+1),oldnv,newnv));
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
                    tablefeedback.append("\n"+Bundle.getMessage("NVWriteCancelled"));
                    return;
                } else {
                    tablefeedback.append("\n"+Bundle.getMessage("NVWriteStarted"));
                    _nextnodenv=0;
                    setnextnv();
                }
            }
        }
        else {
            showEditDialogueEvent(-1);
        }
    }

    private void setnextnv(){
        for (int i=_nextnodenv ; (i < (paramlist.get(6))) ; i++){
            int oldnv =  nvlist.get((i+1));
            int newnv = (Integer) nvFields.get(i).getValue();
            if (oldnv != newnv ) {
                WAITINGRESPONSE_SETNV=true;
                CanMessage m = new CanMessage(tc.getCanid());
                m.setNumDataElements(5);
                CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
                m.setElement(0, CbusConstants.CBUS_NVSET);
                m.setElement(1, _nodeinsetup >> 8);
                m.setElement(2, _nodeinsetup & 0xff);
                m.setElement(3, (i+1));
                m.setElement(4, newnv);
                tc.sendCanMessage(m, null);
                ActionListener setNVTimerTimeOut = new ActionListener(){
                    @Override
                    public void actionPerformed( ActionEvent e ){           
                        tablefeedback.append("\n" + Bundle.getMessage("NVSetTimedout",nodeSelBox.getSelectedItem(),_nextnodenv));
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
        tablefeedback.append("\n" + Bundle.getMessage("NVsSent"));
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
        nvbuildlist=null;
        nvbuildlist = new JPanel();
        nvbuildlist.setLayout(new BoxLayout(nvbuildlist, BoxLayout.Y_AXIS));
        nvListpane.add(nvbuildlist);
        _nextnodenv=1;     
        getindividnv();
    }
    
    private void updateEvPane(){
        evListpane.remove(evbuildlist);
        eventListRow = null;
        eventListCols = null;
        evEditButList = null;
        eventListRow = new ArrayList<ArrayList<JLabel>>();
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
            //   tablefeedback.append("\n" + "Try to get nv " + _nextnodenv + " ");
            CanMessage m = new CanMessage(tc.getCanid());
            m.setNumDataElements(4);
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            m.setElement(0, CbusConstants.CBUS_NVRD);
            m.setElement(1, _nodeinsetup >> 8);
            m.setElement(2, _nodeinsetup & 0xff);
            m.setElement(3, _nextnodenv); // get total parameters for this module
            tc.sendCanMessage(m, null);
            _nextnodenv++;
        }
        else {
            nvbuildlist.add(spacer);
            WAITINGRESPONSE_GETNV=false;
            tablefeedback.append("\n"+Bundle.getMessage("NVsComplete"));
            split.setResizeWeight(_splitratio);
            enableAdminButtons(true);
            checkWriteButtonDirty();
            validate();
            repaint();
        }
    }
    
    private void searchfornodes(){
        WAITINGRESPONSE_STARTUPISANODEINSETUP=true;
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQNP);
        tc.sendCanMessage(m, null);
        nodearr=null;
        nodearr = new ArrayList<String>(20);
        searchForNodesButton.setText(Bundle.getMessage("SearchingNodes"));
        enableAdminButtons(false);
        propertieslabel.setText("");
        nodesupportlinkbutton.setVisible(false);
        split.setDividerLocation(0.1);
        nvorevpane.setVisible(false);
        nvPaneSelectButton.setSelected(true);
        
        tablefeedback.append("\n" + Bundle.getMessage("NodeSearchStart"));
        CanMessage n = new CanMessage(tc.getCanid());
        n.setNumDataElements(1);
        CbusMessage.setPri(n, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        n.setElement(0, CbusConstants.CBUS_QNN);
        tc.sendCanMessage(n, null);
        
        ActionListener searchForNodesTimerTimeOut = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                WAITINGRESPONSE_STARTUPISANODEINSETUP=false;
                int itemCount = nodearr.size();
                searchForNodesButton.setEnabled(true);
                tablefeedback.append("\n" + Bundle.getMessage("NodeSearchComplete",itemCount));
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
    
    public int getfirstintfromstring(String petName){
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<petName.length(); i++){
            Character chars = petName.charAt(i);
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
    
    private void nodeselected(){
        String petName = (String)nodeSelBox.getSelectedItem();
        propertiespane.setVisible(true);
        nvorevpane.setVisible(true);
        paramlist=null;
        paramlist= new ArrayList<Integer>(21);
        split.setDividerLocation(_splitratio);
        propertiespane.setBorder(BorderFactory.createTitledBorder((Bundle.getMessage("CbusNode") + petName ))); 
        _nodeinsetup=getfirstintfromstring(petName);
        tablefeedback.append("\n" + Bundle.getMessage("NodeSelected",petName));
        if (_nodeinsetup>0) {
            getnodeparametertotal(_nodeinsetup);
        }
    }

    private void getnodeparametertotal(Integer _nodeinsetup) {
        // request total node parameters
        _nextnodeparam=1;
        WAITINGRESPONSE_GETNODEPARAM=true;
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(4);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQNPN);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        m.setElement(3, 0); // get total parameters for this module
        // add time wait + check to see if parameters received

        nodeParTotFListener = new ActionListener(){
            @Override
            public void actionPerformed( ActionEvent e ){
                tablefeedback.append("\n" + Bundle.getMessage("NodeNoResponseGetPar",(String)nodeSelBox.getSelectedItem()));
                nodeparamtimer.stop();
                nodeparamtimer=null;
                WAITINGRESPONSE_GETNODEPARAM=false;
            }
        };        
        
        nodeparamtimer = new Timer( searchForNodesDelay, nodeParTotFListener);
        nodeparamtimer.setRepeats( false );
        nodeparamtimer.start();
        tablefeedback.append("\n" + (Bundle.getMessage("CBUS_RQNPN")));
        tc.sendCanMessage(m, null);

    }

    private void getindividparam(){  // node parameters + build parameter pane
        if (_nextnodeparam < (_nodeparameters+1)) {
            CanMessage m = new CanMessage(tc.getCanid());
            m.setNumDataElements(4);
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            m.setElement(0, CbusConstants.CBUS_RQNPN);
            m.setElement(1, _nodeinsetup >> 8);
            m.setElement(2, _nodeinsetup & 0xff);
            m.setElement(3, _nextnodeparam); // get total parameters for this module
            tc.sendCanMessage(m, null);
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
            
            /*
            nodepropbuilder.append ("<br>");              
            nodepropbuilder.append (paramlist.get(6));
            nodepropbuilder.append (" Node Variables. ");            
            nodepropbuilder.append (getnodeflags(paramlist.get(8)));   

            nodepropbuilder.append ("<br>");           
            nodepropbuilder.append (paramlist.get(4));             
            nodepropbuilder.append (" Max events, "); 
            nodepropbuilder.append (paramlist.get(5));   
            nodepropbuilder.append (" variables per event.");             
            
            nodepropbuilder.append ("<br> Processor:"); 
            nodepropbuilder.append (paramlist.get(9));              
            nodepropbuilder.append (" Load Address:"); 
            nodepropbuilder.append (paramlist.get(11)); 
            nodepropbuilder.append (paramlist.get(12)); 
            nodepropbuilder.append (paramlist.get(13));             
            nodepropbuilder.append (paramlist.get(14)); 
            nodepropbuilder.append ("<br> CPU manufacturer id:"); 
            nodepropbuilder.append (paramlist.get(15));
            nodepropbuilder.append (paramlist.get(16));
            nodepropbuilder.append (paramlist.get(17));            
            nodepropbuilder.append (paramlist.get(18));
            nodepropbuilder.append (" CPU manufacturer code:");             
            nodepropbuilder.append (paramlist.get(19));
            */
       
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
    
    public String getnodeflags(int flags){
        StringBuilder toreturn = new StringBuilder(34);
        String nodeflags = String.format("%8s", 
            Integer.toBinaryString(flags & 0xFF)).replace(' ', '0');
        if (Objects.equals("1",String.valueOf(nodeflags.charAt(7)))) {
            toreturn.append(Bundle.getMessage("Consumer"));
            toreturn.append(" ");
        }
        if (Objects.equals("1",String.valueOf(nodeflags.charAt(6)))) {
            toreturn.append(Bundle.getMessage("Producer"));
            toreturn.append(" ");
            }
        if (Objects.equals("1",String.valueOf(nodeflags.charAt(5)))) {
            toreturn.append(Bundle.getMessage("Flim"));
            toreturn.append(" ");
            }
        if (Objects.equals("1",String.valueOf(nodeflags.charAt(4)))) {
            toreturn.append(Bundle.getMessage("Bootloader"));
            toreturn.append(" ");
            }
        return toreturn.toString();
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
            nvhexlabel.setToolTipText(toolTipHexDec);
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
                    nvToHex.get(Integer.valueOf(nvid-1)).setToolTipText(toolTipHexDec);
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
    
    public void getEvVarsByEv(){
        //  log.debug("912 num events {} Next Ev {} Var {} ",_numevents,_nextev,_nextevvar);
        if ( _nextevvar > paramlist.get(5)) {
            _nextevvar=1;
            _nextev++;
            evEditButList.get(_nextev-2).setEnabled(true);
            if((_nextev % 10 == 0) && (_nextev < _numevents)){
                tablefeedback.append("\n" + Bundle.getMessage("GetEventsUpdate",_nextev,_numevents));
            }   
        }
        if (_nextev > _numevents) {
            tablefeedback.append("\n" + Bundle.getMessage("GetEventsComplete",(_nextev-1),_numevents));
            enableAdminButtons(true);
        } else {
            // log.debug("927 Next Ev {} Var {} ",_nextev,_nextevvar);
            WAITINGRESPONSE_GET_EV_VAL=true;
            ActionListener getEvVarTimerTimeOut = new ActionListener(){
                @Override
                public void actionPerformed( ActionEvent e ){           
                    tablefeedback.append("\n" + Bundle.getMessage("NodeNoRespGetEvVar",nodeSelBox.getSelectedItem(),_nextev));
                    JOptionPane.showMessageDialog(null, 
                        Bundle.getMessage("NodeNoRespGetEvVar",nodeSelBox.getSelectedItem(),_nextev), 
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    WAITINGRESPONSE_GET_EV_VAL=false;
                    getEvVarTimer=null;
                }
            };
            getEvVarTimer = new Timer( searchForNodesDelay, getEvVarTimerTimeOut);
            getEvVarTimer.setRepeats( false );
            getEvVarTimer.start();            
            
            CanMessage m = new CanMessage(tc.getCanid());
            m.setNumDataElements(5);
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            m.setElement(0, CbusConstants.CBUS_REVAL);
            m.setElement(1, _nodeinsetup >> 8);
            m.setElement(2, _nodeinsetup & 0xff);
            m.setElement(3, _nextev);
            m.setElement(4, _nextevvar);
            tc.sendCanMessage(m, null);
            _nextevvar++;
        }
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
    
    public void showhidedeletebutton( int intevent, int nodeint){
        int newvalev = (Integer) numberSpinnerEv.getValue();
        int newvalnd = (Integer) numberSpinnernd.getValue();
        if (( newvalev == intevent ) && ( newvalnd == nodeint )) {
            framedeletebutton.setEnabled(true);
        } else {
            framedeletebutton.setEnabled(false);
        }
    }
    
    public void enabledisableeditbutton(int evnum){
        int newvalev = (Integer) numberSpinnerEv.getValue();
        int newvalnd = (Integer) numberSpinnernd.getValue();
        int oldVale=getfirstintfromstring(eventListRow.get((evnum-1)).get(1).getText());
        int oldValn=getfirstintfromstring(eventListRow.get((evnum-1)).get(0).getText());
        if ( newvalev != oldVale ) {
            frameeditevbutton.setEnabled(true);
            return;
        }
        if ( newvalnd != oldValn ) {
            frameeditevbutton.setEnabled(true);
            return;
        }
        for ( int ci=1 ; ci < ((paramlist.get(5))+1) ; ci++){
            int newval = (Integer) evFields.get((ci+1)).getValue();
            int oldVal = getfirstintfromstring(eventListRow.get((evnum-1)).get(ci+1).getText());
            // log.debug("new vals i:{} props5:{} new:{} old:{} ",ci,paramlist.get(5),newval,oldVal);
            if (newval!=oldVal){
                frameeditevbutton.setEnabled(true);
                return;                
            }
        }
        frameeditevbutton.setEnabled(false);
        return;
    }
    
    public void showEditDialogueEvent(int evnum) {
        enableAdminButtons(false);
        evFields=null;
        evFields = new ArrayList<>();
        evToHex=null;
        evToHex = new ArrayList<>();
        
        String title;
        if (evnum<0){
            title=Bundle.getMessage("CbusNode") + (String)nodeSelBox.getSelectedItem() + 
            " " + Bundle.getMessage("NewEvent");
        } else {
            title=Bundle.getMessage("CbusNode") + (String)nodeSelBox.getSelectedItem() +
            " " + Bundle.getMessage("EditEvent") + evnum + "";
        }
        editevframe = new JmriJFrame(title);
        
        JPanel setevpanel = new JPanel();
        setevpanel.setLayout(new BoxLayout(setevpanel, BoxLayout.Y_AXIS));
        
        JPanel inputpanel = new JPanel();
        frameeditevbutton = new JButton(Bundle.getMessage("EditEvent"));
        JButton framenewevbutton = new JButton(Bundle.getMessage("NewEvent"));
        framedeletebutton = new JButton(Bundle.getMessage("ButtonDelete"));
        JButton framecancelbutton = new JButton(Bundle.getMessage("Cancel"));
        
        inputpanel.add(framecancelbutton);
        
        if (evnum<0){
            inputpanel.add(framenewevbutton);
        } else {
            inputpanel.add(frameeditevbutton);
            frameeditevbutton.setEnabled(false);
            inputpanel.add(framedeletebutton);
        }
        
        JPanel individeventEv= new JPanel(); // row container
        individeventEv.setLayout(new GridLayout(1, 3));
        
        String eventstring="0";
        if (evnum>0) {
            eventstring = eventListRow.get((evnum-1)).get(1).getText();
        }
        int intevent=Integer.parseInt(eventstring);
        JLabel newnvhexlabelEv = new JLabel(" ", JLabel.CENTER);
        evToHex.add(newnvhexlabelEv);


        numberSpinnerEv = new JSpinner(new SpinnerNumberModel(intevent, 0, 65535, 1));
        evFields.add(numberSpinnerEv);                   
        numberSpinnerEv.setToolTipText(Bundle.getMessage("NdEvEditToolTip",intevent));
        JComponent compEv = numberSpinnerEv.getEditor();
        JFormattedTextField fieldEv = (JFormattedTextField) compEv.getComponent(0);
        DefaultFormatter formatterEv = (DefaultFormatter) fieldEv.getFormatter();
        formatterEv.setCommitsOnValidEdit(true);

        individeventEv.add(new JLabel(Bundle.getMessage("CbusEvent"), JLabel.RIGHT));
        individeventEv.add(numberSpinnerEv);
        individeventEv.add(newnvhexlabelEv);
        setevpanel.add(individeventEv);
        
        JPanel individeventNd= new JPanel(); // row container
        individeventNd.setLayout(new GridLayout(1, 3));
        String nodestring="0";
        if (evnum>0) {
            nodestring = eventListRow.get((evnum-1)).get(0).getText();
        }    
        int nodeint=Integer.parseInt(nodestring);
        JLabel newnvhexlabelNd = new JLabel(" ", JLabel.CENTER);
        evToHex.add(newnvhexlabelNd);     
     
        numberSpinnernd = new JSpinner(new SpinnerNumberModel(nodeint, 0, 65535, 1));
        evFields.add(numberSpinnernd);
        numberSpinnernd.setToolTipText(Bundle.getMessage("NdEvEditToolTip",nodeint));
        JComponent compNd = numberSpinnernd.getEditor();
        JFormattedTextField fieldNd = (JFormattedTextField) compNd.getComponent(0);
        DefaultFormatter formatterNd = (DefaultFormatter) fieldNd.getFormatter();
        formatterNd.setCommitsOnValidEdit(true);
        
        individeventNd.add(new JLabel(Bundle.getMessage("CbusNode"), JLabel.RIGHT));
        individeventNd.add(numberSpinnernd);
        individeventNd.add(newnvhexlabelNd);

        setevpanel.add(individeventNd);
        
        evFields.get(0).addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newvalev = (Integer) numberSpinnerEv.getValue();
                if ( newvalev == intevent ) {
                    fieldEv.setBackground(Color.white);
                } else {
                    fieldEv.setBackground(Color.yellow);
                }
                showhidedeletebutton( intevent, nodeint);
                if (evnum>0) {
                    enabledisableeditbutton((evnum));
                }
            }
        });
        
        evFields.get(1).addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newvalnd = (Integer) numberSpinnernd.getValue();
                if ( newvalnd == nodeint ) {
                    fieldNd.setBackground(Color.white);
                } else {
                    fieldNd.setBackground(Color.yellow);
                }
                showhidedeletebutton( intevent, nodeint);
                if (evnum>0) {
                    enabledisableeditbutton((evnum));
                }
            }
        });
        
        // loop through each ev var
        for ( int ei=1 ; (ei <= paramlist.get(5)) ; ei++){
            final int myei = ei;
            String eventVal = "0";
            if (evnum>0) {
                eventVal = eventListRow.get((evnum-1)).get(ei+1).getText();
            }
            if (eventVal.length() == 0) {
                eventVal="0";
            }
            final int intEventVal=getfirstintfromstring(eventVal);
            
            // template stuff "may" go here-ish ?
            // move everything down here into seperate function ?
            
            JLabel newnvhexlabel = new JLabel(" ", JLabel.CENTER);
            evToHex.add(newnvhexlabel);
            if (intEventVal>0) {
                newnvhexlabel.setText(showformats(intEventVal));
                newnvhexlabel.setToolTipText(toolTipHexDec);
            }            
            
            JPanel individevent= new JPanel(); // row container
            individevent.setLayout(new GridLayout(1, 3));
            
            JSpinner numberSpinner = new JSpinner(new SpinnerNumberModel(intEventVal, 0, 255, 1));
            evFields.add(numberSpinner);
            numberSpinner.setToolTipText(Bundle.getMessage("NdEvEditToolTip",intEventVal));
            JComponent comp = numberSpinner.getEditor();
            JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
            DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
            formatter.setCommitsOnValidEdit(true);
            evFields.get(myei+1).addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int newval = (Integer) numberSpinner.getValue();
                    
                    if (newval==0) {
                        
                        evToHex.get(Integer.valueOf(myei+1)).setText("");
                        evToHex.get(Integer.valueOf(myei+1)).setToolTipText("");
                        
                    } else {
                    
                        evToHex.get(Integer.valueOf(myei+1)).setText(showformats(newval));
                        evToHex.get(Integer.valueOf(myei+1)).setToolTipText(toolTipHexDec);
                    }

                    if ( newval == intEventVal ) {

                        field.setBackground(Color.white);
                    } else {

                        field.setBackground(Color.yellow);
                    }
                    if (evnum>0) {
                        enabledisableeditbutton((evnum));
                    }
                }
            });
            
            individevent.add(new JLabel(Bundle.getMessage("EvVar",ei), JLabel.RIGHT));
            individevent.add(numberSpinner);
            individevent.add(newnvhexlabel);
            
            setevpanel.add(individevent);
        }
        
        setevpanel.validate();
        editevframe.add(setevpanel, BorderLayout.CENTER);
        editevframe.add(inputpanel, BorderLayout.PAGE_END);
        
        Dimension editevframeminimumSize = new Dimension(150, 200);
        editevframe.setMinimumSize(editevframeminimumSize);
        editevframe.pack();
        editevframe.setResizable(true);

        editevframe.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                editevframe.dispose();
                enableAdminButtons(true);
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                editevframe.dispose();
                enableAdminButtons(true);
            }
        });
        
        framedeletebutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int newevent = (Integer) numberSpinnerEv.getValue();
                int newvalnd = (Integer) numberSpinnernd.getValue();
                int response = JOptionPane.showConfirmDialog(null,
                        (Bundle.getMessage("NdDelEvConfrm",newevent,newvalnd,nodeSelBox.getSelectedItem())),
                        (Bundle.getMessage("DelEvPopTitle")), JOptionPane.YES_NO_OPTION,         
                        JOptionPane.ERROR_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    editevframe.setEnabled(false);
                    
                    nodeEnterLearnEvMode(_nodeinsetup);

                    // wait for learn mode, send delete message with timeout
                    onStartup = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            onStartup.stop();
                            onStartup=null;
                            RELEARN_WHEN_DELETED=false;
                            sendunlearn(newevent, newvalnd);
                        }
                    });
                    onStartup.setRepeats(false);
                    onStartup.start();
                } else {
                    return;
                }
            }
        });
        
        framecancelbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editevframe.dispose();
                enableAdminButtons(true);
            }
        });        
        
        framenewevbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // make sure event >1
                int newevent = (Integer) numberSpinnerEv.getValue();
                int newvalnd = (Integer) numberSpinnernd.getValue();
                if (newevent < 1) {
                    JOptionPane.showMessageDialog(null, 
                        (Bundle.getMessage("EnterEventNum")), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // check for existing event / node combination
                for ( int i=0 ; (i <eventListRow.size() ) ; i++){
                    String testeventstring = eventListRow.get((i)).get(1).getText();
                    String testnodestring = eventListRow.get((i)).get(0).getText();
                    int testevent=Integer.parseInt(testeventstring);
                    int testnode=Integer.parseInt(testnodestring);
                    
                    if ((newevent==testevent) && (newvalnd==testnode)) {
                        JOptionPane.showMessageDialog(null, 
                            (Bundle.getMessage("DuplicateEvNd")), Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                editevframe.setEnabled(false);
                nodeEnterLearnEvMode(_nodeinsetup);
                _nextsetevvar=1;
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
        });        
        
        frameeditevbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String testeventstring = eventListRow.get(((evnum-1))).get(1).getText();
                String testnodestring = eventListRow.get(((evnum-1))).get(0).getText();
                int testevent=Integer.parseInt(testeventstring);
                int testnode=Integer.parseInt(testnodestring);
                
                int response = JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("NdConfirmEditEv",testevent,testnode,nodeSelBox.getSelectedItem()),
                    (Bundle.getMessage("ConfirmQuestion")), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    editevframe.setEnabled(false);
                    tablefeedback.append("\n"+Bundle.getMessage("EditingEvent") );
                    nodeEnterLearnEvMode(_nodeinsetup);
                    _nextsetevvar=1;
                    onStartup = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            RELEARN_WHEN_DELETED=true;
                            sendunlearn(testevent, testnode);
                            onStartup.stop();
                            onStartup=null;
                        }
                    });
                    onStartup.setRepeats(false);
                    onStartup.start();
                }
            }
        });           
        
        editevframe.setVisible(true);
    }
    
    private void sendunlearn(int newevent, int newvalnd){
        // node should already be iin learn mode
        // EVULN
        WAITINGRESPONSE_UNLEARN_EV=true;
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(5);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_EVULN);
        m.setElement(1, newvalnd >> 8);
        m.setElement(2, newvalnd & 0xff);
        m.setElement(3, newevent >> 8);
        m.setElement(4, newevent & 0xff);
        tc.sendCanMessage(m, null);
        
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
                    nodeExitLearnEvMode(_nodeinsetup);
                    // delay then refresh
                    onStartup = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            // log.debug("setEvVarLoop Complete close window + refresh event list");  
                            onStartup.stop();
                            onStartup=null;
                            editevframe.dispose();
                            enableAdminButtons(true);
                            updateEvPane();
                            nodeExitLearnEvMode(_nodeinsetup); // no harm in sending again due to earlier failure
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
    
    private void setEvVarLoop(){
        if ( _nextsetevvar <= paramlist.get(5) ) {
            int newval = (Integer) evFields.get((_nextsetevvar+1)).getValue();
            int newevent = (Integer) numberSpinnerEv.getValue();
            int newvalnd = (Integer) numberSpinnernd.getValue();
            // send teach message
            WAITINGRESPONSE_SET_EV_VAL=true;
            tablefeedback.append("\n" + Bundle.getMessage("NdTeachEv",nodeSelBox.getSelectedItem(),
                newevent,newvalnd,(_nextsetevvar),newval));
            ActionListener SetEvModeListener = new ActionListener(){
                @Override
                public void actionPerformed( ActionEvent e ){
                    tablefeedback.append("\n" +
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
            
            CanMessage m = new CanMessage(tc.getCanid());
            CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            m.setNumDataElements(7);
            m.setElement(0, CbusConstants.CBUS_EVLRN);
            m.setElement(1, newvalnd >> 8);
            m.setElement(2, newvalnd & 0xff);
            m.setElement(3, newevent >> 8);
            m.setElement(4, newevent & 0xff);
            m.setElement(5, _nextsetevvar);
            m.setElement(6, newval);
            tc.sendCanMessage(m, null);
        }
        else {
            nodeExitLearnEvMode(_nodeinsetup);
            onStartup = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    tablefeedback.append("\n" + Bundle.getMessage("NdCompleteEvVar"));
                    editevframe.dispose();
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
    
    private void nodeEnterLearnEvMode( int nn) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NNLRN);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        tablefeedback.append("\n" + Bundle.getMessage("NdReqEnterLearn",(String)nodeSelBox.getSelectedItem()));
        tc.sendCanMessage(m, null);
    }
    
    private void nodeExitLearnEvMode( int nn) {
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(3);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_NNULN);
        m.setElement(1, _nodeinsetup >> 8);
        m.setElement(2, _nodeinsetup & 0xff);
        tablefeedback.append("\n" + Bundle.getMessage("NdReqExitLearn",(String)nodeSelBox.getSelectedItem()));
        tc.sendCanMessage(m, null);
    }    
    
    private void addEventTableRow(int data2, int data3, int eventnum){
        
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

        if (eventnum==1) {
            evbuildlist.add(spacer);
            evbuildlist.add(headings);
        }
        
        JPanel individev= new JPanel(); // row container

        JLabel evData2 = new JLabel("" + (data2) + "", JLabel.CENTER);
        JLabel evData3 = new JLabel("" + (data3) + "", JLabel.CENTER);
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
                // log.debug("edit clicked {} ",(eventnum-1));
                showEditDialogueEvent(eventnum);
            }
        }); 

        eventListRow.add(eventListCols);
        individev.addMouseListener(new HighlightJPanelsChildMouseListeners());
        evbuildlist.add(individev);
        
        if(eventnum % 5 == 0){
            evbuildlist.add(spacer);
            evbuildlist.add(headings);
        }
        
        if (eventnum==_numevents) {
            getEVNumandNodeTimer.stop();
            WAITINGRESPONSE_GET_EV_BY_INDEX=false;
            evbuildlist.add(spacer);
            _nextev=1;
            _nextevvar=1;
            getEvVarsByEv();
            tablefeedback.append("\n" + Bundle.getMessage("NdEvDone"));
        }
        evListpane.validate();
        evListpane.repaint();
    }
    
    private void startnodeallocation(int nn) { // x500000
        tablefeedback.append("\n" + Bundle.getMessage("NdRqNn",nn));
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
        CanMessage m = new CanMessage(tc.getCanid());
        m.setNumDataElements(1);
        CbusMessage.setPri(m, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
        m.setElement(0, CbusConstants.CBUS_RQNP);
        tc.sendCanMessage(m, null);
        int option = JOptionPane.showOptionDialog(null, 
            rqNNpane, 
            popuplabel, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (option == JOptionPane.CANCEL_OPTION) {
            tablefeedback.append("\n" + Bundle.getMessage("NnAllocCancel"));
            WAITINGRESPONSE_RQNN_PARAMS=false;
        } else if (option == JOptionPane.OK_OPTION) {
            int newval = (Integer) rqnnSpinner.getValue();
            tablefeedback.append("\n" + Bundle.getMessage("NnAllocSelected",newval));
            
            sNnTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    WAITINGRESPONSE_SNN=false;
                    tablefeedback.append("\n" + "No Confirmation of Setting Node Number ");
                    JOptionPane.showMessageDialog(null, 
                        Bundle.getMessage("NnAllocError",newval), Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                    sNnTimer.stop();
                    sNnTimer=null;
                }
            });
            sNnTimer.setRepeats(false);
            sNnTimer.start();            
            
            WAITINGRESPONSE_SNN=true;
            CanMessage mn = new CanMessage(tc.getCanid());
            mn.setNumDataElements(3);
            CbusMessage.setPri(mn, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
            mn.setElement(0, CbusConstants.CBUS_SNN);
            mn.setElement(1, newval >> 8);
            mn.setElement(2, newval & 0xff);
            tc.sendCanMessage(mn, null);
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
            tablefeedback.append("\n" + Bundle.getMessage("NdRelease",nn,canid));
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
                tablefeedback.append("\n" + Bundle.getMessage("NnAllocConfirm",nn));
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
                tablefeedback.append("\n" + Bundle.getMessage("NdModUnlrnConfrm"));
                nodeExitLearnEvMode(_nodeinsetup);
                
                // delay then refresh
                onStartup = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        //  tablefeedback.append("\n" + "Completed teaching event variables"); 
                        onStartup.stop();
                        onStartup=null;
                        if (RELEARN_WHEN_DELETED) {
                            tablefeedback.append("\n"+Bundle.getMessage("NdModStartLrn"));
                            nodeEnterLearnEvMode( _nodeinsetup);
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
                tablefeedback.append("\n"+Bundle.getMessage("NdDelEvErr") );
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NdDelEvErr"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
                nodeExitLearnEvMode(_nodeinsetup);
            }
        }
        if (WAITINGRESPONSE_SET_EV_VAL) {
            if ( opc == CbusConstants.CBUS_WRACK) {
                tablefeedback.append("\n"+Bundle.getMessage("NdCnfrmWrite"));
                WAITINGRESPONSE_SET_EV_VAL=false;
                setEvVarTimer.stop();
                setEvVarTimer=null;
                setEvVarLoop();
            }
            else if ( opc == CbusConstants.CBUS_CMDERR ) {
                WAITINGRESPONSE_SET_EV_VAL=false;
                setEvVarTimer.stop();
                setEvVarTimer=null;
                tablefeedback.append("\n" + Bundle.getMessage("NdEvVarWriteError"));
                JOptionPane.showMessageDialog(null, 
                    Bundle.getMessage("NdEvVarWriteError"), Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
                setEvVarLoop(); // or stop write process?
            }
        }
        if (WAITINGRESPONSE_STARTUPISANODEINSETUP) {
            if (opc==CbusConstants.CBUS_PARAMS) { // response from the rqnp sent
                // WAITINGRESPONSE_STARTUPISANODEINSETUP=false;
                int canid = CbusMessage.getId(m);
                tablefeedback.append("\n" + Bundle.getMessage("NdAlreadySetup",canid));
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

            if (opc==CbusConstants.CBUS_PNN) {
                int manu = m.getElement(3);
                int elfour = m.getElement(4);
                nodearr.add(nn + " " + CbusOpCodes.getModuleType(manu,elfour));
                tablefeedback.append("\n" + Bundle.getMessage("CBUS_IN") + " ");
                tablefeedback.append(Bundle.getMessage("CbusNode") + nn);            
                tablefeedback.append(" " + CbusOpCodes.getManu(manu));
                tablefeedback.append(" " + CbusOpCodes.getModuleType(manu,elfour));
                // tablefeedback.append(" " + getnodeflags(m.getElement(5)));
                
                // ensure node is NOT in learn event mode
                CanMessage mn = new CanMessage(tc.getCanid());
                mn.setNumDataElements(3);
                CbusMessage.setPri(mn, CbusConstants.DEFAULT_DYNAMIC_PRIORITY * 4 + CbusConstants.DEFAULT_MINOR_PRIORITY);
                mn.setElement(0, CbusConstants.CBUS_NNULN);
                mn.setElement(1, nn >> 8);
                mn.setElement(2, nn & 0xff);
                tc.sendCanMessage(mn, null);
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
                    tablefeedback.append("\n" + Bundle.getMessage("GotNumEvents",_numevents));
                    geteventsonmodulebyindex();
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
                    tablefeedback.append("\n" + Bundle.getMessage("NVSetConfirm",_nextnodenv));
                    WAITINGRESPONSE_SETNV=false;
                    setnextnv();
                }
                else if ( opc == CbusConstants.CBUS_CMDERR) { // CMDERR error writing to node
                    tablefeedback.append("\n" + Bundle.getMessage("NVSetFail",_nextnodenv));
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
                    int en = m.getElement(3);
                    int ev = m.getElement(4);
                    int var = m.getElement(5);
                    String builder="";
                    
                    if ( var > 0 ) {
                      builder = ("<html><span style='background-color:white'> <b> " + var + " </b> </span></html>");
                    }
                    eventListRow.get((en-1)).get((ev+1)).setText(builder);
                    evListpane.validate();
                    // get the next one
                    getEvVarsByEv();
                }
            }
        }
    }
    
    @Override
    public void message(CanMessage m) {
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

    @Override
    public void dispose() {
        // disconnect from the CBUS
        if (tc != null) {
            tc.removeCanListener(this);
        }
    }

    /**
     * Keeps the message log windows to a reasonable length
     * https://community.oracle.com/thread/1373400
     */
    private static class TextAreaFIFO extends JTextArea implements DocumentListener {
        private int maxLines;
    
        public TextAreaFIFO(int lines) {
            maxLines = lines;
            getDocument().addDocumentListener( this );
        }
    
        public void insertUpdate(DocumentEvent e) {
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    removeLines();
                }
            });
        }
        public void removeUpdate(DocumentEvent e) {}
        public void changedUpdate(DocumentEvent e) {}
        public void removeLines()
        {
            Element root = getDocument().getDefaultRootElement();
            while (root.getElementCount() > maxLines) {
                Element firstLine = root.getElement(0);
                try {
                    getDocument().remove(0, firstLine.getEndOffset());
                } catch(BadLocationException ble) {
                    System.out.println(ble);
                }
            }
        setCaretPosition( getDocument().getLength() );
        }
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
