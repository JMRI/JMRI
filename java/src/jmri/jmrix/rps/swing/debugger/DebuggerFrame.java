package jmri.jmrix.rps.swing.debugger;

import java.awt.FlowLayout;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.ReadingListener;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for manual operation and debugging of the RPS system.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class DebuggerFrame extends jmri.util.JmriJFrame
        implements ReadingListener, MeasurementListener {

    RpsSystemConnectionMemo memo = null;

    public DebuggerFrame(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;

        NUMSENSORS = Engine.instance().getMaxReceiverNumber();

        setTitle(title());
    }

    protected String title() {
        return "RPS Debugger";
    }  // product name, not translated

    @Override
    public void dispose() {
        // separate from data source
        Distributor.instance().removeReadingListener(this);
        Distributor.instance().removeMeasurementListener(this);
        // and unwind swing
        super.dispose();
    }

    java.text.NumberFormat nf;

    JComboBox<String> mode;
    JButton doButton;

    JTextField vs = new JTextField(18);
    JTextField offset = new JTextField(10);

    JTextField x = new JTextField(18);
    JTextField y = new JTextField(18);
    JTextField z = new JTextField(18);
    JLabel code = new JLabel();

    JTextField id = new JTextField(5);

    DebuggerTimePane timep = new DebuggerTimePane();

    int NUMSENSORS;

    @Override
    public void initComponents() {

        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add panes in the middle
        JPanel p, p1;

        // Time inputs
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        p.add(new JLabel("Time measurements: "));

        timep.initComponents();
        JScrollPane sc = new JScrollPane(timep);
        p.add(sc);

        // add id field at bottom
        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout());
        p5.add(new JLabel("Id: "));
        p5.add(id);
        p.add(p5);

        getContentPane().add(p);

        getContentPane().add(new JSeparator());

        // x, y, z results
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel("Results:"));
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("X:"));
        p1.add(x);
        p.add(p1);
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Y:"));
        p1.add(y);
        p.add(p1);
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Z:"));
        p1.add(z);
        p.add(p1);
        p1 = new JPanel();
        p1.setLayout(new FlowLayout());
        p1.add(new JLabel("Code:"));
        p1.add(code);
        p.add(p1);
        getContentPane().add(p);

        getContentPane().add(new JSeparator());

        // add controls at bottom
        p = new JPanel();

        mode = new JComboBox<>(new String[]{"From time fields", "from X,Y,Z fields", "from time file", "from X,Y,Z file"});
        p.add(mode);
        p.setLayout(new FlowLayout());

        doButton = new JButton("Do Once");
        doButton.addActionListener(e -> doOnce());
        p.add(doButton);
        getContentPane().add(p);

        // start working
        Distributor.instance().addReadingListener(this);
        Distributor.instance().addMeasurementListener(this);

        // add file menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrix.rps.swing.CsvExportAction("Export Readings as CSV...", memo));
        fileMenu.add(new jmri.jmrix.rps.swing.CsvExportMeasurementAction("Export Measurements as CSV...", memo));
        setJMenuBar(menuBar);

        // add help
        addHelpMenu("package.jmri.jmrix.rps.swing.debugger.DebuggerFrame", true);

        // prepare for display
        pack();
    }

    /**
     * Invoked by button to do one cycle
     */
    void doOnce() {
        try {
            switch (mode.getSelectedIndex()) {
                case 0: // From time fields
                    doReadingFromTimeFields();
                    break;
                case 1: // From X,Y,Z fields
                    doMeasurementFromPositionFields();
                    break;
                case 2: // From time file
                    doLoadReadingFromFile();
                    doReadingFromTimeFields();
                    break;
                case 3: // From X,Y,Z file
                    doLoadMeasurementFromFile();
                    break;
                default: // should not happen
                    log.error("Did not expect selected mode {}", mode.getSelectedIndex());
            }
        } catch (IOException e) {
            log.error("exception ", e);
        }
    }

    void doLoadReadingFromFile() throws IOException {
        if (readingInput == null) {
            readingInput = getParser(readingFileChooser);
        }

        // get and load a line
        if (readingInput.getRecords().isEmpty()) {
            // read failed, try once to get another file
            readingInput = getParser(readingFileChooser);
            if (readingInput.getRecords().isEmpty()) {
                throw new IOException("no valid file");
            }
        }
        CSVRecord readingRecord = readingInput.getRecords().get(0);

        // item 0 is the ID, not used right now
        for (int i = 0; i < Math.min(NUMSENSORS, readingRecord.size() + 1); i++) {
            timep.times[i].setText(readingRecord.get(i + 1));
        }
    }

    /**
     *
     * @throws IOException if unable to read file
     * @deprecated since 4.19.3; use
     * {@link #getParser(javax.swing.JFileChooser)}
     * instead
     */
    @Deprecated
    void setupReadingFile() throws IOException {
        readingInput = getParser(readingFileChooser);
    }

    private CSVParser getParser(JFileChooser chooser) throws IOException {
        // get file
        CSVParser parser = null;

        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            // create and keep reader
            Reader reader = new FileReader(chooser.getSelectedFile());
            parser = new CSVParser(reader, CSVFormat.DEFAULT.withSkipHeaderRecord());
        }
        return parser;
    }

    void doLoadMeasurementFromFile() throws IOException {
        if (measurementInput == null) {
            measurementInput = getParser(measurementFileChooser);
        }

        // get and load a line
        if (measurementInput.getRecords().isEmpty()) {
            // read failed, try once to get another file
            measurementInput = getParser(measurementFileChooser);
            if (measurementInput.getRecords().isEmpty()) {
                throw new IOException("no valid file");
            }
        }
        CSVRecord measurementRecord = measurementInput.getRecords().get(0);

        // item 0 is the ID, not used right now
        Measurement m = new Measurement(null,
                Double.valueOf(measurementRecord.get(1)),
                Double.valueOf(measurementRecord.get(2)),
                Double.valueOf(measurementRecord.get(3)),
                Engine.instance().getVSound(),
                0,
                "Data File"
        );

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }

    /**
     *
     * @throws IOException if unable to read file
     * @deprecated since 4.19.3; use
     * {@link #getParser(javax.swing.JFileChooser)}
     * instead
     */
    @Deprecated
    void setupMeasurementFile() throws IOException {
        measurementInput = getParser(measurementFileChooser);
    }

    Measurement lastPoint = null;

    Reading getReadingFromTimeFields() {

        double[] values = new double[NUMSENSORS + 1];

        // parse input
        for (int i = 0; i <= NUMSENSORS; i++) {
            values[i] = 0.;
            if ((timep.times[i] != null) && !timep.times[i].getText().equals("")) {
                values[i] = Double.valueOf(timep.times[i].getText());
            }
        }

        // get the id number and make reading
        Reading r = new Reading(id.getText(), values);
        return r;
    }

    void doReadingFromTimeFields() {
        // get the reading
        Reading r = getReadingFromTimeFields();

        // and forward
        Distributor.instance().submitReading(r);
    }

    @Override
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work used.

        id.setText("" + r.getId());
        timep.notify(r);
    }

    void doMeasurementFromPositionFields() {
        // contain dummy Reading
        Reading r = new Reading(id.getText(), new double[]{0., 0., 0., 0.});

        Measurement m = new Measurement(r,
                Double.valueOf(x.getText()),
                Double.valueOf(y.getText()),
                Double.valueOf(z.getText()),
                Engine.instance().getVSound(),
                0,
                "Position Data"
        );

        lastPoint = m;
        Distributor.instance().submitMeasurement(m);
    }

    @Override
    public void notify(Measurement m) {
        // show result
        x.setText(nf.format(m.getX()));
        y.setText(nf.format(m.getY()));
        z.setText(nf.format(m.getZ()));
        code.setText(m.textCode());

        timep.notify(m);
    }

    // to find and remember the input files
    CSVParser readingInput = null;
    final JFileChooser readingFileChooser = new JFileChooser("rps/readings.csv");

    CSVParser measurementInput = null;
    final JFileChooser measurementFileChooser = new JFileChooser("rps/positions.csv");

    private final static Logger log = LoggerFactory.getLogger(DebuggerFrame.class);

}
