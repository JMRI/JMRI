package jmri.jmrit.symbolicprog.symbolicframe;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.progsupport.ProgModePane;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.ValueEditor;
import jmri.jmrit.symbolicprog.ValueRenderer;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.VariableValue;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a table-organized command station programmer from decoder
 * definition files
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2007
 */
public class SymbolicProgFrame extends jmri.util.JmriJFrame {

    JTextField locoRoadName = new JTextField(12);
    JTextField locoRoadNumber = new JTextField(5);
    JTextField locoMfg = new JTextField(12);
    JTextField locoModel = new JTextField(12);

    JLabel progStatus = new JLabel(" OK ");

    JButton selectFileButton = new JButton();
    JButton storeFileButton = new JButton();

    CvTableModel cvModel = new CvTableModel(progStatus, null);
    JTable cvTable = new JTable(cvModel);
    JScrollPane cvScroll = new JScrollPane(cvTable);

    IndexedCvTableModel icvModel = new IndexedCvTableModel(progStatus, null);
    JTable icvTable = new JTable(icvModel);
    JScrollPane icvScroll = new JScrollPane(icvTable);

    VariableTableModel variableModel = new VariableTableModel(progStatus,
            new String[]{"Name", "Value", "Range", "State", "Read", "Write", "CV", "Mask", "Comment"},
            cvModel, icvModel);
    JTable variableTable = new JTable(variableModel);
    JScrollPane variableScroll = new JScrollPane(variableTable);

    JButton newCvButton = new JButton();
    JLabel newCvLabel = new JLabel();
    JTextField newCvNum = new JTextField(4);

    JButton newVarButton = new JButton();
    JLabel newVarNameLabel = new JLabel();
    JTextField newVarName = new JTextField(4);
    JLabel newVarCvLabel = new JLabel();
    JTextField newVarCv = new JTextField(4);
    JLabel newVarMaskLabel = new JLabel();
    JTextField newVarMask = new JTextField(9);

    ProgModePane modePane = new ProgModePane(BoxLayout.X_AXIS);

    JLabel decoderMfg = new JLabel("         ");
    JLabel decoderModel = new JLabel("         ");

    // member to find and remember the configuration file in and out
    JFileChooser fci;
    final JFileChooser fco = new JFileChooser("xml" + File.separator + "decoders" + File.separator);

    // ctor
    public SymbolicProgFrame() {
        super();

        // configure GUI elements
        selectFileButton.setText("Read File");
        selectFileButton.setVisible(true);
        selectFileButton.setToolTipText("Press to select & read a configuration file");

        storeFileButton.setText("Store File");
        storeFileButton.setVisible(true);
        storeFileButton.setToolTipText("Press to store the configuration file");

        variableTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
        variableTable.setDefaultRenderer(JButton.class, new ValueRenderer());
        variableTable.setDefaultEditor(JTextField.class, new ValueEditor());
        variableTable.setDefaultEditor(JButton.class, new ValueEditor());
        variableTable.setRowHeight(new JButton("X").getPreferredSize().height);
        variableScroll.setColumnHeaderView(variableTable.getTableHeader());
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        // instead of forcing the columns to fill the frame (and only fill)
        variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        cvTable.setDefaultRenderer(JTextField.class, new ValueRenderer());
        cvTable.setDefaultRenderer(JButton.class, new ValueRenderer());
        cvTable.setDefaultEditor(JTextField.class, new ValueEditor());
        cvTable.setDefaultEditor(JButton.class, new ValueEditor());
        cvTable.setRowHeight(new JButton("X").getPreferredSize().height);
        cvScroll.setColumnHeaderView(cvTable.getTableHeader());
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        // instead of forcing the columns to fill the frame (and only fill)
        cvTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        newCvButton.setText("Create new CV");
        newCvLabel.setText("CV number:");
        newCvNum.setText("");

        newVarButton.setText("Create new variable");
        newVarNameLabel.setText("Name:");
        newVarName.setText("");
        newVarCvLabel.setText("Cv number:");
        newVarCv.setText("");
        newVarMaskLabel.setText("Bit mask:");
        newVarMask.setText("VVVVVVVV");

        // add actions to buttons
        selectFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                selectFileButtonActionPerformed(e);
            }
        });
        storeFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                writeFile();
            }
        });
        newCvButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                cvModel.addCV(newCvNum.getText(), false, false, false);
            }
        });
        newVarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                newVarButtonPerformed();
            }
        });

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // general GUI config
        setTitle("Symbolic Programmer");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // install items in GUI
        JPanel tPane1 = new JPanel();
        tPane1.setLayout(new BoxLayout(tPane1, BoxLayout.X_AXIS));
        tPane1.add(new JLabel("Road Name: "));
        tPane1.add(locoRoadName);
        tPane1.add(new JLabel("Number: "));
        tPane1.add(locoRoadNumber);
        tPane1.add(new JLabel("Manufacturer: "));
        tPane1.add(locoMfg);
        tPane1.add(new JLabel("Model: "));
        tPane1.add(locoModel);
        getContentPane().add(tPane1);

        JPanel tPane3 = new JPanel();
        tPane3.setLayout(new BoxLayout(tPane3, BoxLayout.X_AXIS));
        tPane3.add(selectFileButton);
        tPane3.add(Box.createHorizontalGlue());
        tPane3.add(new JLabel("Decoder Manufacturer:"));
        tPane3.add(decoderMfg);
        tPane3.add(new JLabel("Model:"));
        tPane3.add(decoderModel);
        tPane3.add(Box.createHorizontalGlue());
        tPane3.add(storeFileButton);
        tPane3.add(Box.createHorizontalGlue());
        getContentPane().add(tPane3);

        JPanel tPane2 = new JPanel();
        tPane2.setLayout(new BoxLayout(tPane2, BoxLayout.X_AXIS));
        tPane2.add(modePane);
        tPane2.add(Box.createHorizontalGlue());
        getContentPane().add(tPane2);

        getContentPane().add(new JSeparator());

        getContentPane().add(progStatus);

        getContentPane().add(variableScroll);

        getContentPane().add(cvScroll);

        JPanel tPane4 = new JPanel();
        tPane4.setLayout(new BoxLayout(tPane4, BoxLayout.X_AXIS));
        tPane4.add(newCvButton);
        tPane4.add(newCvLabel);
        tPane4.add(newCvNum);
        tPane4.add(Box.createHorizontalGlue());
        getContentPane().add(tPane4);

        tPane4 = new JPanel();
        tPane4.setLayout(new BoxLayout(tPane4, BoxLayout.X_AXIS));
        tPane4.add(newVarButton);
        tPane4.add(newVarNameLabel);
        tPane4.add(newVarName);
        tPane4.add(newVarCvLabel);
        tPane4.add(newVarCv);
        tPane4.add(newVarMaskLabel);
        tPane4.add(newVarMask);
        tPane4.add(Box.createHorizontalGlue());
        getContentPane().add(tPane4);

        // for debugging
        pack();
    }

    protected void selectFileButtonActionPerformed(java.awt.event.ActionEvent e) {
        if (fci == null) {
            fci = new JFileChooser("xml" + File.separator + "decoders" + File.separator);
            fci.setFileFilter(new jmri.util.NoArchiveFileFilter());
        }
        // show dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("selectFileButtonActionPerformed: located file " + file + " for XML processing");
            }
            progStatus.setText("Reading file...");
            // handle the file (later should be outside this thread?)
            readAndParseConfigFile(file);
            progStatus.setText("OK");
            if (log.isDebugEnabled()) {
                log.debug("selectFileButtonActionPerformed: parsing complete");
            }

        }
    }

    protected void newVarButtonPerformed() {
        String name = newVarName.getText();
        String CV = newVarCv.getText();
        String mask = newVarMask.getText();

        // ask Table model to do the actuall add
        variableModel.newDecVariableValue(name, CV, null, mask, false, false, false, false);
        variableModel.configDone();
    }

    // Close the window when the close box is clicked
    public void windowClosing(java.awt.event.WindowEvent e) {
        // check for various types of dirty - first table data not written back
        if (cvModel.decoderDirty() || variableModel.decoderDirty()) {
            if (JOptionPane.showConfirmDialog(null,
                    "Some changes have not been written to the decoder. They will be lost. Close window?",
                    "choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        if (variableModel.fileDirty()) {
            if (JOptionPane.showConfirmDialog(null,
                    "Some changes have not been written to a configuration file. Close window?",
                    "choose one", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        modePane.dispose();
        //OK, close
        super.windowClosing(e);
    }

    void readAndParseConfigFile(File file) {
        try {
            DecoderFile xf = new DecoderFile() {
            };  // XmlFile is abstract
            Element root = xf.rootFromFile(file);

            // decode type, invoke proper processing routine if a decoder file
            if (root.getChild("decoder") != null) {
                log.debug("Attempt to open as decoder file");
                processDecoderFile(root.getChild("decoder"), xf);
                log.debug("succeeded");
            } else { // try again as a loco file
                log.debug("Attempt to open as loco file");
                if (root.getChild("locomotive") != null) {
                    processLocoFile(root.getChild("locomotive"));
                } else {
                    log.error("Unrecognized config file contents");
                }
            }
        } catch (org.jdom2.JDOMException | java.io.IOException e) {
            log.warn("readAndParseDecoderConfig: readAndParseDecoderConfig exception: " + e);
        }
    }

    void processDecoderFile(Element decoderElem, DecoderFile xf) {
        // store name, type
        decoderMfg.setText(DecoderFile.getMfgName(decoderElem));

        // load variables to table
        xf.loadVariableModel(decoderElem, variableModel);
    }

    void processLocoFile(Element loco) {
        // load the name et al
        locoRoadName.setText(loco.getAttributeValue("roadName"));
        locoRoadNumber.setText(loco.getAttributeValue("roadNumber"));
        locoMfg.setText(loco.getAttributeValue("mfg"));
        locoModel.setText(loco.getAttributeValue("model"));
        // load the variable definitions for the decoder
        Element decoder = loco.getChild("decoder");
        if (decoder != null) {
            // get the file name
            String mfg = decoder.getAttribute("mfg").getValue();
            String model = decoder.getAttribute("model").getValue();
            String filename = "xml" + File.separator + mfg + "_" + model + ".xml";
            if (log.isDebugEnabled()) {
                log.debug("will read decoder info from " + filename);
            }
            readAndParseConfigFile(new File(filename));
            if (log.isDebugEnabled()) {
                log.debug("finished processing decoder file for loco file");
            }
        } else {
            log.error("No decoder element found in config file");
        }

        // get the CVs and load
        Element values = loco.getChild("values");
        if (values != null) {
            // get the CV values and load
            List<Element> varList = values.getChildren("CVvalue");
            if (log.isDebugEnabled()) {
                log.debug("Found " + varList.size() + " CVvalues");
            }

            for (int i = 0; i < varList.size(); i++) {
                // locate the row
                if (((varList.get(i))).getAttribute("name") == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in name " + ((varList.get(i))) + " " + ((varList.get(i))).getAttributes());
                    }
                    break;
                }
                if (((varList.get(i))).getAttribute("value") == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in value " + ((varList.get(i))) + " " + ((varList.get(i))).getAttributes());
                    }
                    break;
                }

                String name = ((varList.get(i))).getAttribute("name").getValue();
                String value = ((varList.get(i))).getAttribute("value").getValue();
                if (log.isDebugEnabled()) {
                    log.debug("CV: " + i + "th entry, CV number " + name + " has value: " + value);
                }

                CvValue cvObject = cvModel.allCvMap().get(name);
                cvObject.setValue(Integer.valueOf(value).intValue());
                cvObject.setState(CvValue.FROMFILE);
            }
            variableModel.configDone();
        } else {
            log.error("no values element found in config file; CVs not configured");
            return;
        }
        // get the variable values and load
        Element decoderDef = values.getChild("decoderDef");
        if (decoderDef != null) {
            List<Element> varList = decoderDef.getChildren("varValue");
            if (log.isDebugEnabled()) {
                log.debug("Found " + varList.size() + " varValues");
            }

            for (int i = 0; i < varList.size(); i++) {
                // locate the row
                Attribute itemAttr = null;
                if ((itemAttr = varList.get(i).getAttribute("item")) == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in item " + varList.get(i));
                    }
                    break;
                }
                if ((itemAttr = varList.get(i).getAttribute("name")) == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in name " + varList.get(i));
                    }
                    break;
                }
                String item = itemAttr.getValue();

                if (((varList.get(i))).getAttribute("value") == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in value " + ((varList.get(i))));
                    }
                    break;
                }
                String value = ((varList.get(i))).getAttribute("value").getValue();

                if (log.isDebugEnabled()) {
                    log.debug("Variable " + i + " is " + item + " value: " + value);
                }

                int row;
                for (row = 0; row < variableModel.getRowCount(); row++) {
                    if (variableModel.getLabel(row).equals(item)) {
                        break;
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Variable " + item + " is row " + row);
                }
                if (!value.equals("")) { // don't set if no value was stored
                    variableModel.setIntValue(row, Integer.valueOf(value).intValue());
                }
                variableModel.setState(row, VariableValue.FROMFILE);
            }
            variableModel.configDone();
        } else {
            log.error("no decoderDef element found in config file");
        }

        // the act of loading values marks as dirty, but we're actually in synch
        variableModel.setFileDirty(false);
    }

    @SuppressFBWarnings(value = "REC_CATCH_EXCEPTION") // dead class doesn't need this fixed right now
    void writeFile() {
        log.warn("SymbolicProgFrame writeFile invoked - is this still right, or should the LocoFile method be used?");
        log.warn("Note use of VersionID attribute...");
        try {
            // get the file
            int retVal = fco.showSaveDialog(this);
            // handle selection or cancel
            if (retVal != JFileChooser.APPROVE_OPTION) {
                return; // leave early
            }
            File file = fco.getSelectedFile();

            // This is taken in large part from "Java and XML" page 368
            // create root element
            Element root = new Element("locomotive-config");
            Document doc = jmri.jmrit.XmlFile.newDocument(root, jmri.jmrit.XmlFile.dtdLocation + "locomotive-config.dtd");

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/locomotive.xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<String, String>();
            m.put("type", "text/xsl");
            m.put("href", jmri.jmrit.XmlFile.xsltLocation + "locomotive.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            // add top-level elements
            Element values;
            root.addContent(new Element("locomotive") // locomotive values are first item
                    .setAttribute("roadNumber", locoRoadNumber.getText())
                    .setAttribute("roadName", locoRoadName.getText())
                    .setAttribute("mfg", locoMfg.getText())
                    .setAttribute("model", locoModel.getText())
                    .addContent(new Element("decoder")
                            .setAttribute("model", decoderModel.getText())
                            .setAttribute("mfg", decoderMfg.getText())
                            .setAttribute("versionID", "")
                            .setAttribute("mfgID", "")
                    )
                    .addContent(values = new Element("values"))
            );

            // Append a decoderDef element to values
            Element decoderDef;
            values.addContent(decoderDef = new Element("decoderDef"));
            // add the variable values to the decoderDef Element
            for (int i = 0; i < variableModel.getRowCount(); i++) {
                decoderDef.addContent(new Element("varValue")
                        .setAttribute("item", variableModel.getLabel(i))
                        .setAttribute("value", variableModel.getValString(i))
                );
            }
            // add the CV values to the values Element
            for (int i = 0; i < cvModel.getRowCount(); i++) {
                values.addContent(new Element("CVvalue")
                        .setAttribute("name", cvModel.getName(i))
                        .setAttribute("value", cvModel.getValString(i))
                );
            }

            // write the result to selected file
            java.io.FileOutputStream o = new java.io.FileOutputStream(file);
            try {
                XMLOutputter fmt = new XMLOutputter();
                fmt.setFormat(Format.getPrettyFormat()
                        .setLineSeparator(System.getProperty("line.separator"))
                        .setTextMode(Format.TextMode.PRESERVE));
                fmt.output(doc, o);
            } finally {
                o.close();
            }

            // mark file as OK
            variableModel.setFileDirty(false);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SymbolicProgFrame.class.getName());

}
