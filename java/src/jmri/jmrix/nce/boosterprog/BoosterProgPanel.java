package jmri.jmrix.nce.boosterprog;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.jmrix.nce.NceSystemConnectionMemo;

/**
 * Panel for configuring an NCE booster.
 *
 * @author ken cameron Copyright (C) 2010 Derived from BoosterProgFrame by
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class BoosterProgPanel extends jmri.jmrix.nce.swing.NcePanel {

    JTextField start = new JTextField(6);
    JTextField length = new JTextField(12);
    JLabel status = new JLabel();

    public BoosterProgPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            try {
                initComponents((NceSystemConnectionMemo) context);
            } catch (Exception e) {
                log.warn("BoosterProg initContext", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.boosterprog.BoosterProgPanel";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(Bundle.getMessage("TitleBoosterProg"));
        return x.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;

        // general GUI config
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // install items in GUI, one line at a time
        // box of entries
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        add(new JLabel(Bundle.getMessage("Warn1")));
        add(new JLabel(Bundle.getMessage("Warn2")));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton b = new JButton(Bundle.getMessage("ButtonSet"));
        p.add(new JLabel(Bundle.getMessage("LabelStart")));
        start.setText("30");
        p.add(start);
        p.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setStartPushed();
            }
        });
        box.add(p);

        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        b = new JButton(Bundle.getMessage("ButtonSet"));
        p.add(new JLabel(Bundle.getMessage("LabelDuration")));
        length.setText("420");
        p.add(length);
        p.add(b);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setDurationPushed();
            }
        });
        box.add(p);

        add(box);

        add(status);
        status.setText(Bundle.getMessage("StatusOK"));
    }

    private AddressedProgrammer p = null;

    private void getProgrammer() {
        p = memo.getProgrammerManager().getAddressedProgrammer(true, 0);
    }

    private void releaseProgrammer() {
        if (p != null) {
            memo.getProgrammerManager().releaseAddressedProgrammer(p);
        }
        p = null;
    }

    void setStartPushed() {
        getProgrammer();
        status.setText(Bundle.getMessage("StatusProgramming"));
        int val = Integer.parseInt(start.getText());

        try {
            p.writeCV("255", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    status.setText(Bundle.getMessage("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(Bundle.getMessage("StatusError") + e);
        } finally {
            releaseProgrammer();
        }
    }

    void setStart(int val) {
        getProgrammer();

        try {
            p.writeCV("255", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                }
            });
        } catch (ProgrammerException e) {
        } finally {
            releaseProgrammer();
        }
    }

    void setDuration(final int val) {
        getProgrammer();

        try {
            p.writeCV("253", val / 256, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {
                            Thread.currentThread().interrupt(); // retain if needed later
                        }
                    }
                    try {
                        p.writeCV("254", val % 256, new ProgListener() {
                            @Override
                            public void programmingOpReply(int value, int retval) {
                            }
                        });
                    } catch (ProgrammerException e) {
                    } finally {
                        releaseProgrammer();
                    }
                }
            });
        } catch (ProgrammerException e) {
            releaseProgrammer();
        }
    }

    void setDurationPushed() {
        getProgrammer();
        status.setText(Bundle.getMessage("StatusProgramming"));
        int val = Integer.parseInt(length.getText()) / 256;

        try {
            p.writeCV("253", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    synchronized (this) {
                        try {
                            wait(1500);  // needed for booster to reset
                        } catch (InterruptedException i) {
                        }
                    }
                    durationPart2();
                }
            });
        } catch (ProgrammerException e) {
            status.setText(Bundle.getMessage("StatusError") + e);
            releaseProgrammer();
        }
    }

    void durationPart2() {
        status.setText(Bundle.getMessage("StatusProgramming"));
        int val = Integer.parseInt(length.getText()) % 256;

        try {
            p.writeCV("254", val, new ProgListener() {
                @Override
                public void programmingOpReply(int value, int retval) {
                    status.setText(Bundle.getMessage("StatusOK"));
                }
            });
        } catch (ProgrammerException e) {
            status.setText(Bundle.getMessage("StatusError") + e);
        } finally {
            releaseProgrammer();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BoosterProgPanel.class);

}
