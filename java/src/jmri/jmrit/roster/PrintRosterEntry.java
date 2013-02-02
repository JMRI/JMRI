// PaneProgFrame.java

package jmri.jmrit.roster;

import org.apache.log4j.Logger;
import jmri.util.davidflanagan.*;
import java.io.IOException;
import java.awt.Font;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

import jmri.util.JmriJFrame;
import jmri.jmrit.XmlFile;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.ResetTableModel;

import jmri.util.BusyGlassPane;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;

import jmri.jmrit.symbolicprog.tabbedframe.*;

public class PrintRosterEntry implements PaneContainer {

    RosterEntry _rosterEntry;
    
    List<JPanel>        _paneList        = new ArrayList<JPanel>();
    FunctionLabelPane   _flPane         = null;
    RosterMediaPane     _rMPane         = null;
    JmriJFrame          _parent         = null;
    
    public PrintRosterEntry(RosterEntry rosterEntry, JmriJFrame parent, String filename) {
        _rosterEntry = rosterEntry;
        _flPane = new FunctionLabelPane(rosterEntry);
        _rMPane = new RosterMediaPane(rosterEntry);
        _parent = parent;
        JLabel progStatus   = new JLabel("StateIdle");
        jmri.Programmer mProgrammer   = null;
        ResetTableModel resetModel    = new ResetTableModel(progStatus, mProgrammer);
        
        
        XmlFile pf = new XmlFile(){};
        Element base = null;
        try {
            Element root = pf.rootFromName(filename);
            if(root == null){
                log.error("Programmer file name incorrect " + filename);
                return;
            }
            if ( (base = root.getChild("programmer")) == null) {
                log.error("xml file top element is not programmer");
                return;
            }
        } catch (Exception e) {
            log.error("exception reading programmer file: "+filename, e);
            // provide traceback too
            e.printStackTrace();
            return;
        }
        

        CvTableModel cvModel       = new CvTableModel(progStatus, mProgrammer);
        IndexedCvTableModel iCvModel      = new IndexedCvTableModel(progStatus, mProgrammer);

        VariableTableModel variableModel = new VariableTableModel(progStatus, new String[]  {"Name", "Value"},
                                                 cvModel, iCvModel);
        
        String decoderModel = _rosterEntry.getDecoderModel();
        String decoderFamily = _rosterEntry.getDecoderFamily();
        
        if (log.isDebugEnabled()) log.debug("selected loco uses decoder "+decoderFamily+" "+decoderModel);
        // locate a decoder like that.
        List<DecoderFile> l = DecoderIndexFile.instance().matchingDecoderList(null, decoderFamily, null, null, null, decoderModel);
        if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches");
        if (l.size() == 0) {
            log.debug("Loco uses "+decoderFamily+" "+decoderModel+" decoder, but no such decoder defined");
            // fall back to use just the decoder name, not family
            l = DecoderIndexFile.instance().matchingDecoderList(null, null, null, null, null, decoderModel);
            if (log.isDebugEnabled()) log.debug("found "+l.size()+" matches without family key");
        }
        DecoderFile d=null;
        if (l.size() > 0) {
             d = l.get(0);
        } else {
            if (decoderModel.equals(""))
                log.debug("blank decoderModel requested, so nothing loaded");
            else
                log.warn("no matching \""+decoderModel+"\" decoder found for loco, no decoder info loaded");
        }
        
        if (d==null)
            return;
        Element decoderRoot;
        try {
            decoderRoot = d.rootFromName(DecoderFile.fileLocation+d.getFilename());
        
        }
        catch (org.jdom.JDOMException exj) {
            log.error("could not parse "+d.getFilename()+": "+exj.getMessage());
            return;
        }
        catch (java.io.IOException exj) {
            log.error("could not read "+d.getFilename()+": "+exj.getMessage());
            return;
        }

        d.loadVariableModel(decoderRoot.getChild("decoder"), variableModel);
        if (variableModel.piCv() >= 0) {
            resetModel.setPiCv(variableModel.piCv());
        }
        if (variableModel.siCv() >= 0) {
            resetModel.setSiCv(variableModel.siCv());
        }
        d.loadResetModel(decoderRoot.getChild("decoder"), resetModel);
        
        @SuppressWarnings("unchecked")
        List<Element> rawPaneList = base.getChildren("pane");
        for (int i=0; i<rawPaneList.size(); i++) {
            // load each pane
            String name = rawPaneList.get(i).getAttribute("name").getValue();
            PaneProgPane p = new PaneProgPane(this, name, rawPaneList.get(i), cvModel, iCvModel, variableModel, d.getModelElement());
            _paneList.add(p);
        }
    }
    
    public BusyGlassPane getBusyGlassPane() { return null; }
    
    public void prepGlassPane(javax.swing.AbstractButton activeButton){}
    
    public void enableButtons(boolean enable){}
    
    public void paneFinished(){}
    
    public boolean isBusy() { return false; }
    
    public PrintRosterEntry(RosterEntry rosterEntry, List<JPanel> paneList, FunctionLabelPane flPane,RosterMediaPane rMPane, JmriJFrame parent) {
        _rosterEntry = rosterEntry;
        _paneList = paneList;
        _flPane = flPane;
        _rMPane = rMPane;
        _parent = parent;
    }
    
    public void doPrintPanes(boolean preview) {
        //choosePrintItems();
        HardcopyWriter w = null;
        try {
            w = new HardcopyWriter(_parent, _rosterEntry.getId(), 10, .8, .5, .5, .5, preview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }
        printInfoSection(w);

        if (_flPane.includeInPrint())
            _flPane.printPane(w);
        for (int i=0; i<_paneList.size(); i++) {
            if (log.isDebugEnabled()) log.debug("start printing page "+i);
            PaneProgPane pane = (PaneProgPane)_paneList.get(i);
            if (pane.includeInPrint())
                pane.printPane(w);
        }
        w.write(w.getCurrentLineNumber(),0,w.getCurrentLineNumber(),w.getCharactersPerLine() + 1);
        w.close();
    }
    
    public void printPanes(final boolean preview) {
        final JFrame frame = new JFrame("Select Items to Print");
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.PAGE_AXIS));
        JLabel l1 = new JLabel("Select the items that you");
        p1.add(l1);
        l1 = new JLabel("wish to appear in the print out");
        p1.add(l1);
        JPanel select = new JPanel();
        final Hashtable<JCheckBox, PaneProgPane> printList = new Hashtable<JCheckBox, PaneProgPane>();
        select.setLayout(new BoxLayout(select, BoxLayout.PAGE_AXIS));
        final JCheckBox funct = new JCheckBox("Function List");
        funct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _flPane.includeInPrint(funct.isSelected());
            }
        });
        _flPane.includeInPrint(false);
        select.add(funct);
        for (int i=0; i<_paneList.size(); i++){
            final PaneProgPane pane = (PaneProgPane) _paneList.get(i);
            pane.includeInPrint(false);
            final JCheckBox item = new JCheckBox(_paneList.get(i).getName());
            printList.put(item, pane);
            item.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pane.includeInPrint(item.isSelected());
                }
            });
            select.add(item);
        }
        final JCheckBox selectAll = new JCheckBox("Select All");
        selectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _flPane.includeInPrint(selectAll.isSelected());
                funct.setSelected(selectAll.isSelected());
                Enumeration<JCheckBox> en = printList.keys();
                while (en.hasMoreElements()) {
                    JCheckBox check = en.nextElement();
                    printList.get(check).includeInPrint(selectAll.isSelected());
                    check.setSelected(selectAll.isSelected());
                }
            }
        });
        select.add(selectAll);
        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("Okay");
        
        cancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    frame.dispose();
                }
            });
        ok.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    doPrintPanes(preview);
                    frame.dispose();
                }
            });
        JPanel buttons = new JPanel();
        buttons.add(cancel);
        buttons.add(ok);
        p1.add(select);
        p1.add(buttons);
        
        frame.add(p1);
        frame.pack();
        frame.setVisible(true);
    
    }
    
    public void printInfoSection(HardcopyWriter w) {
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        w.write(icon.getImage(), new JLabel(icon));
        w.setFontStyle(Font.BOLD);
        //Add a number of blank lines
        int height = icon.getImage().getHeight(null);
        int blanks = (height-w.getLineAscent())/w.getLineHeight();
        
        try{
            for(int i = 0; i<blanks; i++){
                String s = "\n";
                w.write(s,0,s.length());
            }
        } catch (IOException e) { log.warn("error during printing: "+e);
        }
        _rosterEntry.printEntry(w);
        w.setFontStyle(Font.PLAIN);
    }
    
    static Logger log = Logger.getLogger(PrintRosterEntry.class.getName());
}
