package apps.util.issuereporter.swing;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;

import apps.util.issuereporter.*;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jmri.Application;

import org.apiguardian.api.API;

/**
 * User interface for generating an issue report on the JMRI GitHub project.
 * To allow international support, only the UI is localized.
 * The user is requested to supply the report contents in English.
 *
 * @author Randall Wood Copyright 2020
 */
@API(status = API.Status.INTERNAL)
public class IssueReporter extends JFrame implements ClipboardOwner, DocumentListener {

    private static final int BUG = 0; // index in type combo box
    private static final int RFE = 1; // index in type combo box
    private JComboBox<String> typeCB;
    private JComboBox<GitHubRepository> repoCB;
    private JTextArea bodyTA;
    private JToggleButton submitBtn;
    private JTextField titleText;
    private JLabel descriptionLabel;
    private JLabel instructionsLabel;
    private JPanel typeOptionsPanel;
    private JPanel bugReportPanel;
    private JCheckBox profileCB;
    private JCheckBox sysInfoCB;
    private JCheckBox logsCB;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IssueReporter.class);

    /**
     * Creates new form IssueReporterUI
     */
    public IssueReporter() {
        initComponents();
    }

    private void initComponents() {

        titleText = new JTextField();
        bodyTA = new JTextArea();
        submitBtn = new JToggleButton();
        typeCB = new JComboBox<>();
        repoCB = new JComboBox<>();
        typeOptionsPanel = new JPanel();
        bugReportPanel = new JPanel();
        descriptionLabel = new JLabel();
        instructionsLabel = new JLabel();
        JLabel titleLabel = new JLabel();
        JScrollPane bodySP = new JScrollPane();
        JLabel typeLabel = new JLabel();
        JLabel repoLabel = new JLabel();
        profileCB = new JCheckBox();
        sysInfoCB = new JCheckBox();
        logsCB = new JCheckBox();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(Bundle.getMessage("IssueReporterAction.title", ""));
        setPreferredSize(new java.awt.Dimension(400, 600));

        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle()));
        titleLabel.setText(Bundle.getMessage("IssueReporter.titleLabel.text"));

        bodyTA.setColumns(20);
        bodyTA.setLineWrap(true);
        bodyTA.setRows(5);
        bodyTA.setWrapStyleWord(true);
        bodySP.setViewportView(bodyTA);

        submitBtn.setText(Bundle.getMessage("IssueReporter.submitBtn.text"));
        submitBtn.setEnabled(false);
        submitBtn.addActionListener(this::submitBtnActionListener);

        descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(descriptionLabel.getFont().getStyle() | java.awt.Font.BOLD));
        descriptionLabel.setText(Bundle.getMessage("IssueReporter.descriptionLabel.bug"));

        instructionsLabel.setText(Bundle.getMessage("IssueReporter.instructionsLabel.bug"));

        typeLabel.setFont(typeLabel.getFont().deriveFont(typeLabel.getFont().getStyle()));
        typeLabel.setText(Bundle.getMessage("IssueReporter.typeLabel.text"));

        typeCB.setModel(new DefaultComboBoxModel<>(new String[]{Bundle.getMessage("IssueReporterType.bug"), Bundle.getMessage("IssueReporterType.feature")}));
        typeCB.addActionListener(this::typeCBActionListener);

        repoLabel.setFont(repoLabel.getFont().deriveFont(repoLabel.getFont().getStyle()));
        repoLabel.setText(Bundle.getMessage("IssueReporter.repoLabel.text"));

        repoCB.setModel(new GitHubRepositoryComboBoxModel());
        repoCB.setRenderer(new GitHubRepositoryListCellRenderer());

        profileCB.setText(Bundle.getMessage("IssueReporter.profileCB.text"));

        sysInfoCB.setText(Bundle.getMessage("IssueReporter.sysInfoCB.text"));

        logsCB.setText(Bundle.getMessage("IssueReporter.logsCB.text"));

        titleText.getDocument().addDocumentListener(this);

        bodyTA.getDocument().addDocumentListener(this);

        GroupLayout bugReportPanelLayout = new GroupLayout(bugReportPanel);
        bugReportPanel.setLayout(bugReportPanelLayout);
        bugReportPanelLayout.setHorizontalGroup(
                bugReportPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(bugReportPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(bugReportPanelLayout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(sysInfoCB)
                                        .addComponent(logsCB)
                                        .addComponent(profileCB))
                                .addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bugReportPanelLayout.setVerticalGroup(
                bugReportPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(bugReportPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(sysInfoCB)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(logsCB)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(profileCB)
                                .addContainerGap(DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout typeOptionsPanelLayout = new GroupLayout(typeOptionsPanel);
        typeOptionsPanel.setLayout(typeOptionsPanelLayout);
        typeOptionsPanelLayout.setHorizontalGroup(
                typeOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(bugReportPanel, Alignment.TRAILING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
        );
        typeOptionsPanelLayout.setVerticalGroup(
                typeOptionsPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(bugReportPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(bodySP, DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                        .addComponent(instructionsLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(descriptionLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                                                .addGap(0, 0, Short.MAX_VALUE)
                                                .addComponent(submitBtn))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                                                        .addComponent(typeLabel, PREFERRED_SIZE, 70, Short.MAX_VALUE)
                                                        .addComponent(repoLabel, PREFERRED_SIZE, 70, Short.MAX_VALUE)
                                                        .addComponent(titleLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(typeCB, 0, DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(repoCB, 0, DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(titleText)))
                                        .addComponent(typeOptionsPanel, Alignment.TRAILING, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, titleLabel, typeLabel, repoLabel);

        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(typeCB, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                        .addComponent(typeLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(repoCB, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                        .addComponent(repoLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(titleLabel)
                                        .addComponent(titleText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(descriptionLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(instructionsLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bodySP, DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(typeOptionsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(submitBtn)
                                .addContainerGap())
        );

        pack();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        submitBtn.setEnabled(!bodyTA.getText().isEmpty() && !titleText.getText().isEmpty());
    }

    private void typeCBActionListener(ActionEvent e) {
        switch (typeCB.getSelectedIndex()) {
            case BUG:
                descriptionLabel.setText(Bundle.getMessage("IssueReporter.descriptionLabel.bug"));
                instructionsLabel.setText(Bundle.getMessage("IssueReporter.instructionsLabel.bug"));
                if (!typeOptionsPanel.equals(bugReportPanel.getParent())) {
                    typeOptionsPanel.add(bugReportPanel);
                    typeOptionsPanel.setPreferredSize(bugReportPanel.getPreferredSize());
                    bugReportPanel.revalidate();
                    bugReportPanel.repaint();
                }
                break;
            case RFE:
                descriptionLabel.setText(Bundle.getMessage("IssueReporter.descriptionLabel.feature"));
                instructionsLabel.setText(Bundle.getMessage("IssueReporter.instructionsLabel.feature"));
                typeOptionsPanel.remove(bugReportPanel);
                break;
            default:
                log.error("Unexpected selected index {} for issue type", typeCB.getSelectedIndex(), new IllegalArgumentException());
        }
    }

    private void submitBtnActionListener(ActionEvent e) {
        IssueReport report = null;
        switch (typeCB.getSelectedIndex()) {
            case BUG:
                report = new BugReport(titleText.getText(), bodyTA.getText(), profileCB.isSelected(), sysInfoCB.isSelected(), logsCB.isSelected());
                break;
            case RFE:
                report = new EnhancementRequest(titleText.getText(), bodyTA.getText());
                break;
            default:
                log.error("Unexpected selected index {} for issue type", typeCB.getSelectedIndex(), new IllegalArgumentException());
        }
        if (report != null) {
            submitReport(report);
        }
    }

    // package private
    private void submitReport(IssueReport report) {
        try {
            URI uri = report.submit(repoCB.getItemAt(repoCB.getSelectedIndex()));
            List<File> attachments = report.getAttachments();
            if (!attachments.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("IssueReporter.attachments.message"),
                        Bundle.getMessage("IssueReporter.attachments.title"),
                        JOptionPane.INFORMATION_MESSAGE);
                Desktop.getDesktop().open(attachments.get(0).getParentFile());
            }
            if ( Desktop.getDesktop().isSupported( Desktop.Action.BROWSE) ) {
                // Open browser to URL with draft report
                Desktop.getDesktop().browse(uri);
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); // close report window
            } else {
                // Can't open browser, ask the user to instead
                Object[] options = {Bundle.getMessage("IssueReporter.browser.copy"), Bundle.getMessage("IssueReporter.browser.skip")};
                int choice = JOptionPane.showOptionDialog(this,
                    Bundle.getMessage("IssueReporter.browser.message"), // message
                    Bundle.getMessage("IssueReporter.browser.title"), // window title
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null, // icon
                    options,
                    Bundle.getMessage("IssueReporter.browser.copy")
                );
                
                if (choice == 0 ) {
                    Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(
                            new StringSelection(uri.toString()),
                            null
                        );
                    this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)); // close report window
                }
            }
            
        } catch (IOException | URISyntaxException ex) {
            log.error("Unable to report issue", ex);
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("IssueReporter.error.message", ex.getLocalizedMessage()),
                    Bundle.getMessage("IssueReporter.error.title"),
                    JOptionPane.ERROR_MESSAGE);
        } catch (IssueReport414Exception ex) {
            BodyTransferable bt = new BodyTransferable(report.getBody());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(bt, this);
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("IssueReporter.414.message"),
                    Bundle.getMessage("IssueReporter.414.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            submitReport(report);
        }
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents
    ) {
        // ignore -- merely means something else was put on clipboard
    }

    private static class GitHubRepositoryComboBoxModel extends DefaultComboBoxModel<GitHubRepository> {

        public GitHubRepositoryComboBoxModel() {
            super();
            ServiceLoader<GitHubRepository> loader = ServiceLoader.load(GitHubRepository.class);
            Set<GitHubRepository> set = new TreeSet<>();
            loader.forEach(set::add);
            loader.reload();
            set.forEach(r -> {
                addElement(r);
                if (r.getTitle().equals(Application.getApplicationName())) {
                    setSelectedItem(r);
                }
            });
        }

    }

    private static class GitHubRepositoryListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return super.getListCellRendererComponent(list,
                    (value instanceof GitHubRepository) ? ((GitHubRepository) value).getTitle() : value,
                    index,
                    isSelected,
                    cellHasFocus);
        }
    }

    public static class FileTransferable implements Transferable {

        private final List<File> files;

        public FileTransferable(@Nonnull List<File> files) {
            this.files = files;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        @Nonnull
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return files;
        }

    }

    public static class BodyTransferable implements Transferable {

        private final String body;

        public BodyTransferable(@Nonnull String body) {
            this.body = body;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.stringFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.stringFlavor.equals(flavor);
        }

        @Override
        @Nonnull
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return body;
        }

    }

}
