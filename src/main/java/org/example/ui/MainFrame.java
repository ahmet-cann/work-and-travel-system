package org.example.ui;

import org.example.db.WAT_Student_Management_System;
import org.example.model.Employer;
import org.example.model.JobOffer;
import org.example.model.Student;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class MainFrame extends JFrame {

    private static final Color BG        = new Color(245, 245, 243);
    private static final Color CARD      = Color.WHITE;
    private static final Color DARK      = new Color(26, 26, 46);
    private static final Color MUTED     = new Color(110, 110, 105);
    private static final Color DANGER    = new Color(185, 28, 28);
    private static final Color DANGER_BG = new Color(254, 226, 226);
    private static final Color SUCCESS   = new Color(15, 110, 86);
    private static final Color BORDER    = new Color(220, 218, 210);

    private final CardLayout cards = new CardLayout();
    private final JPanel     root  = new JPanel(cards);

    private Student   currentStudent;
    private Employer  selectedEmployer;
    private JobOffer  selectedJob;

    private JLabel    compUserLbl, roleUserLbl, dashUserLbl;
    private JPanel    employerListPanel, jobListPanel;
    private JButton   empNextBtn, jobConfirmBtn;
    private ButtonGroup empGroup, jobGroup;
    private JLabel    sucCompany, sucPosition, sucLocation;
    private JTextPane dashTextPane;

    public MainFrame() {
        setTitle("WAT Management System — Öğrenci Portalı");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(540, 620);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG);

        root.setOpaque(false);
        root.add(buildLoginPanel(),    "LOGIN");
        root.add(buildEmployerPanel(), "EMPLOYER");
        root.add(buildJobPanel(),      "JOB");
        root.add(buildSuccessPanel(),  "SUCCESS");
        root.add(buildDashboardPanel(), "DASHBOARD");

        add(root);
        cards.show(root, "LOGIN");
        setVisible(true);
    }

    private JPanel buildLoginPanel() {
        JPanel outer = centeredPanel();
        JPanel card = card(400, 440);

        JPanel logoRow = hPanel();
        JLabel logoIcon = label("✈", 26, Font.PLAIN, DARK);
        JPanel logoText = vPanel();
        logoText.add(label("WAT Management System", 14, Font.BOLD, DARK));
        logoText.add(label("Öğrenci Portalı", 11, Font.PLAIN, MUTED));
        logoRow.add(logoIcon);
        logoRow.add(hGap(10));
        logoRow.add(logoText);

        JLabel title    = label("Giriş yap", 20, Font.BOLD, DARK);
        JLabel subtitle = label("Okul e-posta adresinle devam et.", 12, Font.PLAIN, MUTED);
        title.setAlignmentX(LEFT_ALIGNMENT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel emailLbl = fieldLabel("E-posta adresi");
        JTextField emailFld = inputField();

        JLabel passLbl = fieldLabel("Şifre");
        JPasswordField passFld = new JPasswordField();
        styleInput(passFld);

        JLabel errLbl = label("", 12, Font.PLAIN, DANGER);
        errLbl.setAlignmentX(LEFT_ALIGNMENT);

        JButton btn = darkBtn("  Giriş yap  →");
        btn.setAlignmentX(LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            String email = emailFld.getText().trim();
            String pass  = new String(passFld.getPassword());
            errLbl.setText("");

            if (!email.contains("@") || email.length() < 5) {
                errLbl.setText("Geçerli bir e-posta gir."); return;
            }

            btn.setEnabled(false);
            btn.setText("Kontrol ediliyor...");

            new SwingWorker<Student, Void>() {
                protected Student doInBackground() { return WAT_Student_Management_System.login(email, pass); }
                protected void done() {
                    try {
                        currentStudent = get();
                        if (currentStudent != null) {
                            if (WAT_Student_Management_System.hasExistingApplication(currentStudent.studentID)) {
                                loadDashboardPanel();
                                cards.show(root, "DASHBOARD");
                            } else {
                                loadEmployerPanel();
                                cards.show(root, "EMPLOYER");
                            }
                        } else {
                            errLbl.setText("E-posta hatalı veya öğrenci bulunamadı.");
                        }
                    } catch (Exception ex) {
                        errLbl.setText("Bağlantı hatası! SQL Server açık mı?");
                    }
                    btn.setEnabled(true);
                    btn.setText("  Giriş yap  →");
                }
            }.execute();
        });

        passFld.addActionListener(e -> btn.doClick());

        card.add(logoRow);
        card.add(vGap(16)); card.add(divider());
        card.add(vGap(16)); card.add(title);
        card.add(vGap(4));  card.add(subtitle);
        card.add(vGap(18)); card.add(emailLbl);
        card.add(vGap(4));  card.add(emailFld);
        card.add(vGap(12)); card.add(passLbl);
        card.add(vGap(4));  card.add(passFld);
        card.add(vGap(8));  card.add(errLbl);
        card.add(vGap(14)); card.add(btn);

        outer.add(card);
        return outer;
    }

    private JPanel buildEmployerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));
        compUserLbl = label("", 14, Font.BOLD, DARK);
        JButton logout = outlineBtn("Çıkış");
        logout.addActionListener(e -> doLogout());
        top.add(compUserLbl, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);
        top.add(stepBar(1), BorderLayout.SOUTH);

        JLabel sectionLbl = label("Şirket seç", 12, Font.BOLD, MUTED);
        sectionLbl.setBorder(new EmptyBorder(10, 0, 8, 0));

        employerListPanel = new JPanel();
        employerListPanel.setLayout(new BoxLayout(employerListPanel, BoxLayout.Y_AXIS));
        employerListPanel.setOpaque(false);

        JScrollPane scroll = scroll(employerListPanel);

        empNextBtn = darkBtn("Devam et →");
        empNextBtn.setEnabled(false);
        empNextBtn.addActionListener(e -> {
            loadJobPanel();
            cards.show(root, "JOB");
        });

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        bot.setOpaque(false);
        bot.add(empNextBtn);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(sectionLbl, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        center.add(bot, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void loadEmployerPanel() {
        compUserLbl.setText("👤  " + currentStudent.getFullName() + " · " + currentStudent.major);
        selectedEmployer = null;
        empNextBtn.setEnabled(false);
        employerListPanel.removeAll();
        empGroup = new ButtonGroup();

        List<Employer> list = WAT_Student_Management_System.getEmployers();
        for (Employer emp : list) {
            JPanel row = employerRow(emp);
            employerListPanel.add(row);
            employerListPanel.add(vGap(6));
        }
        employerListPanel.revalidate();
        employerListPanel.repaint();
    }

    private JPanel employerRow(Employer emp) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(CARD);
        row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(11, 13, 11, 13)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JRadioButton radio = new JRadioButton();
        radio.setOpaque(false);
        empGroup.add(radio);

        JPanel texts = vPanel();
        texts.add(label(emp.companyName, 13, Font.BOLD, DARK));
        texts.add(label(emp.city + ", " + emp.state + "  ·  " + emp.contactPerson, 11, Font.PLAIN, MUTED));

        radio.addActionListener(e -> {
            selectedEmployer = emp;
            empNextBtn.setEnabled(true);
        });
        row.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { radio.doClick(); }
            public void mouseEntered(MouseEvent e) { row.setBackground(BG); }
            public void mouseExited(MouseEvent e)  { row.setBackground(CARD); }
        });

        row.add(radio, BorderLayout.WEST);
        row.add(texts, BorderLayout.CENTER);
        return row;
    }

    private JPanel buildJobPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 14, 0));
        roleUserLbl = label("", 14, Font.BOLD, DARK);
        JButton back = outlineBtn("← Geri");
        back.addActionListener(e -> {
            selectedJob = null;
            cards.show(root, "EMPLOYER");
        });
        top.add(roleUserLbl, BorderLayout.WEST);
        top.add(back, BorderLayout.EAST);
        top.add(stepBar(2), BorderLayout.SOUTH);

        JLabel hint = label("Pozisyon seçimi yapın.", 11, Font.PLAIN, MUTED);
        hint.setBorder(new EmptyBorder(10, 0, 8, 0));

        jobListPanel = new JPanel();
        jobListPanel.setLayout(new BoxLayout(jobListPanel, BoxLayout.Y_AXIS));
        jobListPanel.setOpaque(false);

        JScrollPane scroll = scroll(jobListPanel);

        jobConfirmBtn = darkBtn("Seçimi Onayla →");
        jobConfirmBtn.setEnabled(false);
        jobConfirmBtn.addActionListener(e -> doConfirm());

        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        bot.setOpaque(false);
        bot.add(jobConfirmBtn);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(hint, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        center.add(bot, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private void loadJobPanel() {
        roleUserLbl.setText(selectedEmployer.companyName + " — " + selectedEmployer.city + ", " + selectedEmployer.state);
        selectedJob = null;
        jobConfirmBtn.setEnabled(false);
        jobListPanel.removeAll();
        jobGroup = new ButtonGroup();

        List<JobOffer> jobs = WAT_Student_Management_System.getJobsByEmployer(selectedEmployer.employerID);
        for (JobOffer job : jobs) {
            JPanel row = jobRow(job);
            jobListPanel.add(row);
            jobListPanel.add(vGap(6));
        }
        jobListPanel.revalidate();
        jobListPanel.repaint();
    }

    private JPanel jobRow(JobOffer job) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(CARD);
        row.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(11, 13, 11, 13)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JPanel texts = vPanel();
        texts.add(label(job.positionTitle, 13, job.taken ? Font.ITALIC : Font.BOLD, job.taken ? MUTED : DARK));
        texts.add(label("$" + String.format("%.2f", job.hourlyWage) + "/hr  ·  " + job.city + ", " + job.state, 11, Font.PLAIN, MUTED));

        if (job.taken) {
            JLabel badge = label(" DOLU ", 10, Font.BOLD, DANGER);
            badge.setOpaque(true); badge.setBackground(DANGER_BG);
            badge.setBorder(new EmptyBorder(2, 6, 2, 6));
            row.add(texts, BorderLayout.CENTER);
            row.add(badge, BorderLayout.EAST);
        } else {
            JRadioButton radio = new JRadioButton();
            radio.setOpaque(false);
            jobGroup.add(radio);
            radio.addActionListener(e -> {
                selectedJob = job;
                jobConfirmBtn.setEnabled(true);
            });
            row.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) { radio.doClick(); }
                public void mouseEntered(MouseEvent e) { row.setBackground(BG); }
                public void mouseExited(MouseEvent e)  { row.setBackground(CARD); }
            });
            row.add(radio, BorderLayout.WEST);
            row.add(texts, BorderLayout.CENTER);
        }
        return row;
    }

    private void doConfirm() {
        int ok = JOptionPane.showConfirmDialog(this,
                "<html><b>" + selectedEmployer.companyName + "</b><br>Pozisyon: <b>" + selectedJob.positionTitle + "</b></html>",
                "Seçimi Onayla", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (ok != JOptionPane.YES_OPTION) return;

        jobConfirmBtn.setEnabled(false);
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() {
                return WAT_Student_Management_System.applyToJob(currentStudent.studentID, selectedJob.jobID);
            }
            protected void done() {
                try {
                    if (get()) {
                        showSuccess();
                        cards.show(root, "SUCCESS");
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.this, "Seçilen pozisyon doldu.");
                        loadJobPanel();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                jobConfirmBtn.setEnabled(true);
            }
        }.execute();
    }

    private JPanel buildSuccessPanel() {
        JPanel outer = centeredPanel();
        JPanel card  = card(420, 330);

        JLabel icon = label("✅", 40, Font.PLAIN, DARK);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel title = label("Başvurun tamamlandı!", 20, Font.BOLD, DARK);
        title.setAlignmentX(CENTER_ALIGNMENT);

        sucCompany  = label("", 13, Font.PLAIN, MUTED);
        sucPosition = label("", 14, Font.BOLD, SUCCESS);
        sucLocation = label("", 12, Font.PLAIN, MUTED);
        sucCompany.setAlignmentX(CENTER_ALIGNMENT);
        sucPosition.setAlignmentX(CENTER_ALIGNMENT);
        sucLocation.setAlignmentX(CENTER_ALIGNMENT);

        JButton homeBtn = darkBtn("  Sürecimi Görüntüle  ");
        homeBtn.setAlignmentX(CENTER_ALIGNMENT);
        homeBtn.addActionListener(e -> {
            loadDashboardPanel();
            cards.show(root, "DASHBOARD");
        });

        card.add(icon); card.add(vGap(14)); card.add(title);
        card.add(vGap(12)); card.add(sucCompany); card.add(vGap(4));
        card.add(sucPosition); card.add(vGap(4)); card.add(sucLocation);
        card.add(vGap(24)); card.add(homeBtn);

        outer.add(card);
        return outer;
    }

    private void showSuccess() {
        sucCompany.setText(selectedEmployer.companyName);
        sucPosition.setText(selectedJob.positionTitle + "  ($" + String.format("%.2f", selectedJob.hourlyWage) + "/hr)");
        sucLocation.setText(selectedJob.city + ", " + selectedJob.state);
    }

    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        dashUserLbl = label("", 14, Font.BOLD, DARK);
        JButton logout = outlineBtn("Çıkış Yap");
        logout.addActionListener(e -> doLogout());
        top.add(dashUserLbl, BorderLayout.WEST);
        top.add(logout, BorderLayout.EAST);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(20, 20, 20, 20)));

        dashTextPane = new JTextPane();
        dashTextPane.setEditable(false);
        dashTextPane.setContentType("text/html");
        card.add(new JScrollPane(dashTextPane), BorderLayout.CENTER);

        panel.add(top, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void loadDashboardPanel() {
        dashUserLbl.setText("👤 " + currentStudent.getFullName() + " [" + currentStudent.studentID + "]");
        String summary = WAT_Student_Management_System.getStudentDashboardSummary(currentStudent.studentID);
        dashTextPane.setText(summary);
    }

    private void doLogout() {
        currentStudent = null; selectedEmployer = null; selectedJob = null;
        cards.show(root, "LOGIN");
    }

    private JPanel centeredPanel() { JPanel p = new JPanel(new GridBagLayout()); p.setOpaque(false); return p; }
    private JPanel hPanel() { FlowLayout p = new FlowLayout(FlowLayout.LEFT, 0, 0); JPanel pan = new JPanel(p); pan.setOpaque(false); pan.setAlignmentX(LEFT_ALIGNMENT); return pan; }
    private JPanel vPanel() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); return p; }
    private JPanel card(int w, int h) { JPanel c = new JPanel(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); c.setBackground(CARD); c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(30, 36, 30, 36))); c.setPreferredSize(new Dimension(w, h)); return c; }
    private JLabel label(String text, int size, int style, Color color) { JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", style, size)); l.setForeground(color); return l; }
    private JLabel fieldLabel(String text) { JLabel l = label(text, 12, Font.BOLD, MUTED); l.setAlignmentX(LEFT_ALIGNMENT); return l; }
    private JTextField inputField() { JTextField f = new JTextField(); styleInput(f); return f; }
    private void styleInput(JComponent c) { c.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38)); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); c.setFont(new Font("Segoe UI", Font.PLAIN, 14)); c.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(4, 10, 4, 10))); c.setBackground(CARD); c.setAlignmentX(LEFT_ALIGNMENT); }
    private JSeparator divider() { JSeparator sep = new JSeparator(); sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); sep.setForeground(BORDER); sep.setAlignmentX(LEFT_ALIGNMENT); return sep; }
    private Component vGap(int h) { return Box.createVerticalStrut(h); }
    private Component hGap(int w) { return Box.createHorizontalStrut(w); }
    private JScrollPane scroll(JPanel content) { JScrollPane s = new JScrollPane(content); s.setOpaque(false); s.getViewport().setOpaque(false); s.setBorder(null); return s; }

    private JButton darkBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(DARK); b.setForeground(Color.WHITE);
        b.setOpaque(true); b.setBorderPainted(false);
        b.setBorder(new EmptyBorder(9, 20, 9, 20));
        return b;
    }

    private JButton outlineBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        b.setForeground(MUTED); b.setBackground(CARD); b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(5,12,5,12)));
        return b;
    }

    private JPanel stepBar(int activeStep) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4)); p.setOpaque(false);
        String[] steps = {"1. Şirket seç", "2. Pozisyon seç", "3. Onayla"};
        for (int i = 0; i < steps.length; i++) {
            JLabel lbl = label(steps[i], 11, Font.PLAIN, i + 1 == activeStep ? DARK : (i + 1 < activeStep ? SUCCESS : MUTED));
            p.add(lbl);
            if (i < steps.length - 1) p.add(label(" › ", 11, Font.PLAIN, MUTED));
        }
        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame();
        });
    }
}