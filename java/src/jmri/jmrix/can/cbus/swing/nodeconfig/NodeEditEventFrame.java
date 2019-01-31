package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatter;
import javax.swing.Timer;
import jmri.jmrix.can.cbus.node.CbusNodeEvent;
import jmri.jmrix.can.cbus.CbusNameService;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to control an instance of CBUS highlighter to highlight events.
 *
 * @author Steve Young Copyright (C) 2019
 */
public class NodeEditEventFrame extends JmriJFrame {

   // private JmriJFrame editevframe;
    
    private JSpinner numberSpinnerEv;
    private JSpinner numberSpinnernd;
    private List<JSpinner> evFields;
    private List<JLabel> evToHex;
    private JButton framedeletebutton;
    private JButton frameeditevbutton;
    private Timer waitForLearnMode;
    private NodeConfigToolPane _tp;
    private CbusNodeEvent _ndEv;
    private JLabel ndEvNameLabel;
    
    /**
     * Create a new instance of NodeEditEventFrame.
     */
    public NodeEditEventFrame(NodeConfigToolPane tp, CbusNodeEvent ndEv) {
        super();
        _ndEv = ndEv;
        _tp=tp;
    }

    @Override
    public void initComponents() {
        log.debug("init components");
        _tp.enableAdminButtons(false);
        evFields = new ArrayList<>();
        evToHex = new ArrayList<>();
        
        JPanel setevpanel = new JPanel();
        setevpanel.setLayout(new BoxLayout(setevpanel, BoxLayout.Y_AXIS));
        JPanel setCoreNodeEventPanel = new JPanel();
        setCoreNodeEventPanel.setLayout(new BoxLayout(setCoreNodeEventPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollsetevpanel = new JScrollPane (setevpanel);
        
        JPanel inputpanel = new JPanel();
        frameeditevbutton = new JButton(Bundle.getMessage("EditEvent"));
        JButton framenewevbutton = new JButton(Bundle.getMessage("NewEvent"));
        framedeletebutton = new JButton(Bundle.getMessage("ButtonDelete"));
        JButton framecancelbutton = new JButton(Bundle.getMessage("Cancel"));
        
        inputpanel.add(framecancelbutton);
        
        if (_ndEv.getEn()<0){
            inputpanel.add(framenewevbutton);
        } else {
            inputpanel.add(frameeditevbutton);
            frameeditevbutton.setEnabled(false);
            inputpanel.add(framedeletebutton);
        }
        
        JPanel individeventEv= new JPanel(); // row container
        individeventEv.setLayout(new GridLayout(1, 3));
        
        JLabel newnvhexlabelEv = new JLabel(" ", JLabel.CENTER);
        evToHex.add(newnvhexlabelEv);

        numberSpinnerEv = new JSpinner(new SpinnerNumberModel(Math.max(1,_ndEv.getEn()), 1, 65535, 1));
        evFields.add(numberSpinnerEv);                   
        numberSpinnerEv.setToolTipText(Bundle.getMessage("NdEvEditToolTip",_ndEv.getEn() ) );
        JComponent compEv = numberSpinnerEv.getEditor();
        JFormattedTextField fieldEv = (JFormattedTextField) compEv.getComponent(0);
        DefaultFormatter formatterEv = (DefaultFormatter) fieldEv.getFormatter();
        formatterEv.setCommitsOnValidEdit(true);

        individeventEv.add(new JLabel(Bundle.getMessage("CbusEvent"), JLabel.RIGHT));
        individeventEv.add(numberSpinnerEv);
        individeventEv.add(newnvhexlabelEv);
        setCoreNodeEventPanel.add(individeventEv);
        
        JPanel individeventNd= new JPanel(); // row container
        individeventNd.setLayout(new GridLayout(1, 3));
        
        int nodeint=_ndEv.getNn();
        
        JLabel newnvhexlabelNd = new JLabel(" ", JLabel.CENTER);
        evToHex.add(newnvhexlabelNd);     
     
        numberSpinnernd = new JSpinner(new SpinnerNumberModel(Math.max(0,_ndEv.getNn()), 0, 65535, 1));
        evFields.add(numberSpinnernd);
        numberSpinnernd.setToolTipText(Bundle.getMessage("NdEvEditToolTip",nodeint));
        JComponent compNd = numberSpinnernd.getEditor();
        JFormattedTextField fieldNd = (JFormattedTextField) compNd.getComponent(0);
        DefaultFormatter formatterNd = (DefaultFormatter) fieldNd.getFormatter();
        formatterNd.setCommitsOnValidEdit(true);
        
        individeventNd.add(new JLabel(Bundle.getMessage("CbusNode"), JLabel.RIGHT));
        individeventNd.add(numberSpinnernd);
        individeventNd.add(newnvhexlabelNd);
      //  individeventNd.setVisible(true);

        setCoreNodeEventPanel.add(individeventNd);
        setCoreNodeEventPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        ndEvNameLabel = new JLabel("",SwingConstants.CENTER);
        ndEvNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateSelectedEventNodeName();
        
        setCoreNodeEventPanel.add( ndEvNameLabel);
        
        evFields.get(0).addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int newvalev = (Integer) numberSpinnerEv.getValue();
                if ( newvalev == _ndEv.getEn() ) {
                    fieldEv.setBackground(Color.white);
                } else {
                    fieldEv.setBackground(Color.yellow);
                }
                showhidedeletebutton( _ndEv.getEn(), nodeint);
                if (_ndEv.getEn()>0) {
                    enabledisableeditbutton();
                }
                updateSelectedEventNodeName();
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
                showhidedeletebutton( _ndEv.getEn(), nodeint);
                if (_ndEv.getEn()>0) {
                    enabledisableeditbutton();
                }
                updateSelectedEventNodeName();
            }
        });
        
        // loop through each ev var
        for ( int ei=1 ; ( ei <= _ndEv.getNumEvVars() ) ; ei++){
            final int myei = ei;
            
            JLabel newnvhexlabel = new JLabel(" ", JLabel.CENTER);
            evToHex.add(newnvhexlabel);
            if (_ndEv.getEvVar(ei)>0) {
                newnvhexlabel.setText(NodeConfigToolPane.showformats(_ndEv.getEvVar(ei)));
                newnvhexlabel.setToolTipText(Bundle.getMessage("toolTipHexDec"));
            }            
            
            JPanel individevent= new JPanel(); // row container
            individevent.setLayout(new GridLayout(1, 3));
            
            JSpinner numberSpinner = new JSpinner(new SpinnerNumberModel(Math.max(0,_ndEv.getEvVar(ei)), 0, 255, 1));
            evFields.add(numberSpinner);
            numberSpinner.setToolTipText(Bundle.getMessage("NdEvEditToolTip",_ndEv.getEvVar(ei)));
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
                        evToHex.get(Integer.valueOf(myei+1)).setText(NodeConfigToolPane.showformats(newval));
                        evToHex.get(Integer.valueOf(myei+1)).setToolTipText(Bundle.getMessage("toolTipHexDec"));
                    }

                    if ( newval == _ndEv.getEvVar(myei) ) {
                        field.setBackground(Color.white);
                    } else {
                        field.setBackground(Color.yellow);
                    }
                    if ( _ndEv.getEn() >0 ) {
                        enabledisableeditbutton();
                    }
                }
            });
            
            individevent.add(new JLabel(Bundle.getMessage("EvVar",myei), JLabel.RIGHT));
            individevent.add(numberSpinner);
            individevent.add(newnvhexlabel);
            
            setevpanel.add(individevent);
        }
        
        scrollsetevpanel.validate();
        setCoreNodeEventPanel.validate();
        
        add(setCoreNodeEventPanel, BorderLayout.PAGE_START);
        add(scrollsetevpanel, BorderLayout.CENTER);
        add(inputpanel, BorderLayout.PAGE_END);
        
        Dimension editevframeminimumSize = new Dimension(150, 200);
        setMinimumSize(editevframeminimumSize);
        pack();
        this.setResizable(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if ( _tp != null ) {
                    _tp.enableAdminButtons(true);
                }
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                if ( _tp != null ) {
                    _tp.enableAdminButtons(true);
                }
            }
        });
        
        framedeletebutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int newevent = (Integer) numberSpinnerEv.getValue();
                int newvalnd = (Integer) numberSpinnernd.getValue();
                int response = JOptionPane.showConfirmDialog(null,
                        (Bundle.getMessage("NdDelEvConfrm",newevent,newvalnd,_tp.nodeSelBox.getSelectedItem())),
                        (Bundle.getMessage("DelEvPopTitle")), JOptionPane.YES_NO_OPTION,         
                        JOptionPane.ERROR_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    setEnabled(false);
                    _tp.send.nodeEnterLearnEvMode(_tp._nodeinsetup);

                    // wait for learn mode, send delete message with timeout
                    waitForLearnMode = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            waitForLearnMode.stop();
                            waitForLearnMode=null;
                            _tp.RELEARN_WHEN_DELETED=false;
                            _tp.sendunlearn(newevent, newvalnd);
                        }
                    });
                    waitForLearnMode.setRepeats(false);
                    waitForLearnMode.start();
                    
                } else {
                    return;
                }
            }
        });
        
        framecancelbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                _tp.enableAdminButtons(true);
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
                for ( int i=0 ; (i < _tp._ndEvArr.size() ) ; i++){
                    if ( _tp._ndEvArr.get(i).matches(newvalnd,newevent) ) {
                        JOptionPane.showMessageDialog(null, 
                            (Bundle.getMessage("DuplicateEvNd")), Bundle.getMessage("WarningTitle"),
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                setEnabled(false);
                _tp.send.nodeEnterLearnEvMode(_tp._nodeinsetup);
                _tp._nextsetevvar=1;
                waitForLearnMode = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        _tp.setEvVarLoop();
                        waitForLearnMode.stop();
                        waitForLearnMode=null;
                        
                    }
                });
                waitForLearnMode.setRepeats(false); // Only execute once
                waitForLearnMode.start();
            }
        });        
        
        frameeditevbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("NdConfirmEditEv",_ndEv.getEn(),_ndEv.getNn(),_tp.nodeSelBox.getSelectedItem()),
                    (Bundle.getMessage("ConfirmQuestion")), JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    
                    setEnabled(false);
                    _tp.tablefeedback.append( NodeConfigToolPane.ls +Bundle.getMessage("EditingEvent") );
                    _tp.send.nodeEnterLearnEvMode(_tp._nodeinsetup);
                    _tp._nextsetevvar=1;
                    waitForLearnMode = new Timer(1000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            _tp.RELEARN_WHEN_DELETED=true;
                            _tp.sendunlearn(_ndEv.getEn(), _ndEv.getNn());
                            waitForLearnMode.stop();
                            waitForLearnMode=null;
                        }
                    });
                    waitForLearnMode.setRepeats(false);
                    waitForLearnMode.start();
                }
            }
        });           
        setTitle(title());
        setVisible(true);
    }

    private void updateSelectedEventNodeName(){
        
        ndEvNameLabel.setText("<html><div style='text-align: center;'>" + 
        new CbusNameService().getEventNodeString(getNodeVal(), getEventVal() )
        + "</div></html>");
        
    }


    private void showhidedeletebutton( int intevent, int nodeint){
        int newvalev = (Integer) numberSpinnerEv.getValue();
        int newvalnd = (Integer) numberSpinnernd.getValue();
        if (( newvalev == intevent ) && ( newvalnd == nodeint )) {
            framedeletebutton.setEnabled(true);
        } else {
            framedeletebutton.setEnabled(false);
        }
    }
    
    private void enabledisableeditbutton(){
        int newvalev = (Integer) numberSpinnerEv.getValue();
        int newvalnd = (Integer) numberSpinnernd.getValue();
        if ( newvalev != _ndEv.getEn() ) {
            frameeditevbutton.setEnabled(true);
            return;
        }
        if ( newvalnd != _ndEv.getNn() ) {
            frameeditevbutton.setEnabled(true);
            return;
        }
        for ( int ci=1 ; ci < (( _ndEv.getNumEvVars() )+1) ; ci++){
            int newval = (Integer) evFields.get((ci+1)).getValue();
            if ( newval != _ndEv.getEvVar(ci) ) {
                frameeditevbutton.setEnabled(true);
                return;                
            }
        }
        frameeditevbutton.setEnabled(false);
        return;
    }
    
    protected int getEventVal(){
        return (Integer) numberSpinnerEv.getValue();
    }

    protected int getNodeVal(){
        return (Integer) numberSpinnernd.getValue();
    }
    
    protected int getEvVar(int index){
        return (Integer) evFields.get(index).getValue();
    }

    private String title() {
        String title;
        if ( _ndEv.getEn() <0 ) {
            title=Bundle.getMessage("CbusNode") + (String) _tp.nodeSelBox.getSelectedItem() + 
            " " + Bundle.getMessage("NewEvent");
        } else {
            title=Bundle.getMessage("CbusNode") + (String) _tp.nodeSelBox.getSelectedItem() +
            " " + Bundle.getMessage("EditEvent") + _ndEv.getEn() + "";
        }
        return title;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(NodeEditEventFrame.class);
}
