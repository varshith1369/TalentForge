package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Company-wise Preparation Module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Graph</b> - Company -> Role -> Skill relationships. Represents a directed
 *       acyclic graph where companies connect to roles, and roles connect to skills.</li>
 * </ul>
 */
public class CompanyPrepPanel extends JPanel {

    /* ============================================================ */
    /*  GRAPH DATA STRUCTURE                                       */
    /* ============================================================ */
    private static class Node {
        final String name;
        final String type; // company, role, skill

        Node(String name, String type) {
            this.name = name;
            this.type = type;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;
            return name.equals(node.name) && type.equals(node.type);
        }

        @Override public int hashCode() {
            return Objects.hash(name, type);
        }
    }

    private static class Graph {
        private final Map<Node, List<Node>> adjList = new LinkedHashMap<>();

        void addNode(Node node) {
            adjList.putIfAbsent(node, new ArrayList<>());
        }

        void addEdge(Node from, Node to) {
            addNode(from);
            addNode(to);
            if (!adjList.get(from).contains(to)) {
                adjList.get(from).add(to);
            }
        }

        List<Node> getNeighbors(Node node) {
            return adjList.getOrDefault(node, Collections.emptyList());
        }
    }

    private static class CompanyProfile {
        final String name;
        final Color accent;
        final String descriptor;
        final String focus;

        CompanyProfile(String name, Color accent, String descriptor, String focus) {
            this.name = name;
            this.accent = accent;
            this.descriptor = descriptor;
            this.focus = focus;
        }
    }

    private static final Color SKY = new Color(56, 189, 248);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color ORANGE = new Color(245, 158, 11);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color INDIGO = new Color(79, 70, 229);
    private static final Color SLATE = new Color(100, 116, 139);

    /* ============================================================ */
    /*  DATA                                                       */
    /* ============================================================ */
    private final Graph relationGraph = new Graph();
    private final List<Node> companyNodes = new ArrayList<>();
    private final Map<String, CompanyProfile> companyProfiles = new LinkedHashMap<>();
    private final Map<String, String> roleDescriptions = new LinkedHashMap<>();
    private final Map<String, String> skillHints = new LinkedHashMap<>();
    private final Map<String, Boolean> completedSkills = new HashMap<>();
    private final Set<String> preparedRoleKeys = new LinkedHashSet<>();

    private Node selectedCompanyNode;
    private Node selectedRoleNode;
    private String companySearch = "";

    /* ============================================================ */
    /*  UI STATE                                                   */
    /* ============================================================ */
    private final JPanel companiesContainer = new JPanel();
    private final JPanel rolesContainer = new JPanel();
    private final JPanel skillsContainer = new JPanel();
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel();
    private final JLabel selectedSummaryLabel = new JLabel();
    private final JLabel roleCountLabel = new JLabel();
    private final JLabel skillProgressLabel = new JLabel();
    private final JLabel companyTitleLabel = new JLabel();
    private final JLabel companyFocusLabel = new JLabel();
    private final JLabel roleTitleLabel = new JLabel();
    private final JLabel roleBodyLabel = new JLabel();
    private final JLabel readinessLabel = new JLabel();
    private final JProgressBar skillProgressBar = new JProgressBar();
    private JButton markReadyButton;

    public CompanyPrepPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        initializeGraph();
        buildUI();
        selectCompany(companyNodes.get(0));

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  GRAPH INITIALIZATION                                       */
    /* ============================================================ */
    private void initializeGraph() {
        addCompanyProfile("Google", SKY, "Search, infra, scale-heavy roles", "Strong fundamentals, problem solving, and clean systems thinking.");
        addCompanyProfile("Microsoft", INDIGO, "Platform, cloud, and product engineering", "Balanced DSA, product sense, and system reliability.");
        addCompanyProfile("Amazon", ORANGE, "Ownership-driven engineering roles", "Data-driven thinking, leadership principles, and scalable backend work.");
        addCompanyProfile("TCS", GREEN, "Enterprise delivery and platform roles", "Core CS, Java, communication, and execution discipline.");
        addCompanyProfile("Infosys", new Color(20, 184, 166), "Services, systems, and cloud support tracks", "Solid fundamentals, practical tooling, and adaptability.");

        roleDescriptions.put("Software Development Engineer", "High-signal engineering track with emphasis on DSA, code quality, and scalable design discussions.");
        roleDescriptions.put("Data Analyst", "Analytical role centered on SQL fluency, scripting, and turning raw data into business answers.");
        roleDescriptions.put("Cloud Support Engineer", "Support and infra role focused on networks, debugging, and customer-facing technical clarity.");
        roleDescriptions.put("Systems Engineer", "Broader execution role that rewards strong CS basics, implementation discipline, and debugging stamina.");

        skillHints.put("Data Structures & Algorithms", "Practice medium-difficulty problem solving, edge cases, and time-complexity explanation.");
        skillHints.put("System Design", "Learn service boundaries, scalability tradeoffs, data flow, and failure handling at a high level.");
        skillHints.put("Java Programming", "Review collections, OOP, exceptions, threads, and writing clean implementation code.");
        skillHints.put("SQL & Databases", "Be comfortable with joins, aggregation, normalization basics, and writing precise data queries.");
        skillHints.put("Computer Networks", "Focus on OSI/TCP-IP basics, HTTP, DNS, latency, and practical troubleshooting patterns.");
        skillHints.put("Operating Systems", "Revisit processes vs threads, scheduling, memory, synchronization, and deadlock concepts.");
        skillHints.put("Python Scripting", "Use Python for quick automation, data handling, and interview-friendly problem solving.");

        Node google = companyNode("Google");
        Node microsoft = companyNode("Microsoft");
        Node amazon = companyNode("Amazon");
        Node tcs = companyNode("TCS");
        Node infosys = companyNode("Infosys");

        Node sde = roleNode("Software Development Engineer");
        Node dataAnalyst = roleNode("Data Analyst");
        Node cloudSupport = roleNode("Cloud Support Engineer");
        Node systemsEng = roleNode("Systems Engineer");

        Node dsa = skillNode("Data Structures & Algorithms");
        Node systemDesign = skillNode("System Design");
        Node java = skillNode("Java Programming");
        Node sql = skillNode("SQL & Databases");
        Node networking = skillNode("Computer Networks");
        Node operatingSystems = skillNode("Operating Systems");
        Node python = skillNode("Python Scripting");

        relationGraph.addEdge(google, sde);
        relationGraph.addEdge(google, dataAnalyst);

        relationGraph.addEdge(microsoft, sde);
        relationGraph.addEdge(microsoft, cloudSupport);

        relationGraph.addEdge(amazon, sde);
        relationGraph.addEdge(amazon, dataAnalyst);
        relationGraph.addEdge(amazon, cloudSupport);

        relationGraph.addEdge(tcs, systemsEng);
        relationGraph.addEdge(tcs, dataAnalyst);

        relationGraph.addEdge(infosys, systemsEng);
        relationGraph.addEdge(infosys, cloudSupport);

        relationGraph.addEdge(sde, dsa);
        relationGraph.addEdge(sde, systemDesign);
        relationGraph.addEdge(sde, java);

        relationGraph.addEdge(dataAnalyst, sql);
        relationGraph.addEdge(dataAnalyst, python);

        relationGraph.addEdge(cloudSupport, networking);
        relationGraph.addEdge(cloudSupport, sql);

        relationGraph.addEdge(systemsEng, java);
        relationGraph.addEdge(systemsEng, operatingSystems);
    }

    private void addCompanyProfile(String name, Color accent, String descriptor, String focus) {
        companyProfiles.put(name, new CompanyProfile(name, accent, descriptor, focus));
        companyNodes.add(companyNode(name));
    }

    private Node companyNode(String name) {
        return new Node(name, "company");
    }

    private Node roleNode(String name) {
        return new Node(name, "role");
    }

    private Node skillNode(String name) {
        return new Node(name, "skill");
    }

    /* ============================================================ */
    /*  UI ASSEMBLY                                                */
    /* ============================================================ */
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 24, 24, 24));

        root.add(buildHeroPanel(), BorderLayout.NORTH);
        root.add(buildWorkspace(), BorderLayout.CENTER);

        add(buildTopBar(), BorderLayout.NORTH);
        add(root, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(18, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.PANEL_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setPaint(new GradientPaint(0, 0,
                    new Color(SKY.getRed(), SKY.getGreen(), SKY.getBlue(), Theme.isDark() ? 70 : 40),
                    getWidth(), 0,
                    new Color(INDIGO.getRed(), INDIGO.getGreen(), INDIGO.getBlue(), Theme.isDark() ? 46 : 24)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
            new EmptyBorder(16, 22, 16, 22)));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = new JLabel("Company Prep Studio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Theme.PRIMARY_TEXT);
        JLabel sub = new JLabel("Map target companies to roles and skills, then track readiness with a cleaner prep roadmap.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(Theme.MUTED_TEXT);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(makePillLabel(companyProfiles.size() + " Companies", SKY, false));
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(pillBorder(INDIGO));
        updateStatusLabel();
        right.add(statusLabel);

        bar.add(left, BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildHeroPanel() {
        GradientCard hero = new GradientCard(new BorderLayout(18, 0), SKY, INDIGO);
        hero.setBorder(new EmptyBorder(24, 24, 24, 24));
        hero.setPreferredSize(new Dimension(0, 140));

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Build a sharper target list");
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Pick a company, choose the role, then close the required skills until the role is actually ready.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(255, 255, 255, 220));

        selectedSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        selectedSummaryLabel.setForeground(Color.WHITE);

        copy.add(title);
        copy.add(Box.createVerticalStrut(6));
        copy.add(sub);
        copy.add(Box.createVerticalStrut(12));
        copy.add(selectedSummaryLabel);

        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(360, 0));
        roleCountLabel.setText("-");
        skillProgressLabel.setText("-");
        stats.add(heroStat("Roles", roleCountLabel));
        stats.add(heroStat("Skill Progress", skillProgressLabel));
        stats.add(heroStat("Prepared", new JLabel(String.valueOf(UserProfileCache.getCompaniesPrepared()))));

        hero.add(copy, BorderLayout.CENTER);
        hero.add(stats, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildWorkspace() {
        JPanel workspace = new JPanel(new GridBagLayout());
        workspace.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0;
        gc.weightx = 0.34;
        gc.insets = new Insets(0, 0, 0, 14);
        workspace.add(buildCompanyRail(), gc);

        gc.gridx = 1;
        gc.weightx = 0.26;
        gc.insets = new Insets(0, 0, 0, 14);
        workspace.add(buildRoleRail(), gc);

        gc.gridx = 2;
        gc.weightx = 0.40;
        gc.insets = new Insets(0, 0, 0, 0);
        workspace.add(buildDetailPanel(), gc);

        return workspace;
    }

    private JPanel buildCompanyRail() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Target Companies"), BorderLayout.WEST);
        header.add(makeTinyLabel("Search by company or role track"), BorderLayout.EAST);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search companies...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.FIELD_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        companiesContainer.setLayout(new BoxLayout(companiesContainer, BoxLayout.Y_AXIS));
        companiesContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(companiesContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(searchField, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRoleRail() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 14));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionLabel("Role Map"), BorderLayout.WEST);
        header.add(makeTinyLabel("Graph neighbors from the selected company"), BorderLayout.EAST);

        rolesContainer.setLayout(new BoxLayout(rolesContainer, BoxLayout.Y_AXIS));
        rolesContainer.setOpaque(false);

        JScrollPane scroll = new JScrollPane(rolesContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDetailPanel() {
        SurfacePanel panel = new SurfacePanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        companyTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        companyTitleLabel.setForeground(Theme.PRIMARY_TEXT);
        companyFocusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        companyFocusLabel.setForeground(Theme.MUTED_TEXT);
        roleTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roleTitleLabel.setForeground(Theme.PRIMARY_TEXT);
        roleBodyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleBodyLabel.setForeground(Theme.MUTED_TEXT);

        top.add(companyTitleLabel, BorderLayout.NORTH);
        top.add(companyFocusLabel, BorderLayout.CENTER);

        JPanel roleSummary = new JPanel(new BorderLayout(0, 6));
        roleSummary.setOpaque(false);
        roleSummary.add(roleTitleLabel, BorderLayout.NORTH);
        roleSummary.add(roleBodyLabel, BorderLayout.CENTER);
        top.add(roleSummary, BorderLayout.SOUTH);

        JPanel progressPanel = new JPanel(new BorderLayout(0, 8));
        progressPanel.setOpaque(false);
        readinessLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        readinessLabel.setForeground(Theme.PRIMARY_TEXT);
        skillProgressBar.setBorderPainted(false);
        skillProgressBar.setStringPainted(true);
        skillProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        skillProgressBar.setForeground(INDIGO);
        skillProgressBar.setBackground(Theme.isDark() ? new Color(45, 45, 60) : new Color(229, 231, 235));
        progressPanel.add(readinessLabel, BorderLayout.NORTH);
        progressPanel.add(skillProgressBar, BorderLayout.CENTER);

        skillsContainer.setLayout(new BoxLayout(skillsContainer, BoxLayout.Y_AXIS));
        skillsContainer.setOpaque(false);
        JScrollPane skillsScroll = new JScrollPane(skillsContainer);
        skillsScroll.setBorder(null);
        skillsScroll.setOpaque(false);
        skillsScroll.getViewport().setOpaque(false);
        skillsScroll.getVerticalScrollBar().setUnitIncrement(12);

        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        markReadyButton = createGradientButton("Mark Role Ready", 170, 40);
        markReadyButton.addActionListener(e -> markRolePrepared());
        JPanel actionWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actionWrap.setOpaque(false);
        actionWrap.add(markReadyButton);
        bottom.add(actionWrap, BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(progressPanel, BorderLayout.CENTER);
        panel.add(skillsScroll, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 14));
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.CENTER);
        wrapper.add(bottom, BorderLayout.SOUTH);
        return wrapper;
    }

    /* ============================================================ */
    /*  SELECTION + GRAPH TRAVERSAL                                */
    /* ============================================================ */
    private void onSearchChanged() {
        companySearch = searchField.getText().trim().toLowerCase();
        rebuildCompanyList();
    }

    private void selectCompany(Node company) {
        selectedCompanyNode = company;
        selectedRoleNode = null;

        rebuildCompanyList();
        rebuildRoleList();
        updateCompanySummary();
    }

    private void selectRole(Node role) {
        selectedRoleNode = role;
        rebuildRoleList();
        rebuildSkillsList();
        updateCompanySummary();
    }

    private void rebuildCompanyList() {
        companiesContainer.removeAll();

        List<Node> visibleCompanies = filteredCompanies();
        if (visibleCompanies.isEmpty()) {
            companiesContainer.add(emptyLabel("No companies match this search."));
        } else {
            for (Node company : visibleCompanies) {
                companiesContainer.add(buildCompanyCard(company, company.equals(selectedCompanyNode)));
                companiesContainer.add(Box.createVerticalStrut(10));
            }
            if (selectedCompanyNode == null || !visibleCompanies.contains(selectedCompanyNode)) {
                selectedCompanyNode = visibleCompanies.get(0);
            }
        }

        companiesContainer.revalidate();
        companiesContainer.repaint();

        if (selectedCompanyNode != null && (selectedRoleNode == null || !relationGraph.getNeighbors(selectedCompanyNode).contains(selectedRoleNode))) {
            List<Node> roles = relationGraph.getNeighbors(selectedCompanyNode);
            selectedRoleNode = roles.isEmpty() ? null : roles.get(0);
            rebuildRoleList();
            rebuildSkillsList();
            updateCompanySummary();
        }
    }

    private List<Node> filteredCompanies() {
        if (companySearch.isBlank()) return new ArrayList<>(companyNodes);

        List<Node> visible = new ArrayList<>();
        for (Node company : companyNodes) {
            if (company.name.toLowerCase().contains(companySearch)) {
                visible.add(company);
                continue;
            }
            for (Node role : relationGraph.getNeighbors(company)) {
                if (role.name.toLowerCase().contains(companySearch)) {
                    visible.add(company);
                    break;
                }
            }
        }
        return visible;
    }

    private void rebuildRoleList() {
        rolesContainer.removeAll();

        if (selectedCompanyNode == null) {
            rolesContainer.add(emptyLabel("Select a company to see role paths."));
        } else {
            List<Node> roles = relationGraph.getNeighbors(selectedCompanyNode);
            if (roles.isEmpty()) {
                rolesContainer.add(emptyLabel("No mapped roles for this company yet."));
                selectedRoleNode = null;
            } else {
                if (selectedRoleNode == null || !roles.contains(selectedRoleNode)) {
                    selectedRoleNode = roles.get(0);
                }
                for (Node role : roles) {
                    rolesContainer.add(buildRoleCard(role, role.equals(selectedRoleNode)));
                    rolesContainer.add(Box.createVerticalStrut(10));
                }
            }
        }

        rolesContainer.revalidate();
        rolesContainer.repaint();
    }

    private void rebuildSkillsList() {
        skillsContainer.removeAll();

        if (selectedRoleNode == null || selectedCompanyNode == null) {
            skillsContainer.add(emptyLabel("Select a role to load the required skills."));
            setProgressState(0, 0);
        } else {
            List<Node> skills = relationGraph.getNeighbors(selectedRoleNode);
            if (skills.isEmpty()) {
                skillsContainer.add(emptyLabel("No mapped skills for this role."));
                setProgressState(0, 0);
            } else {
                for (Node skill : skills) {
                    skillsContainer.add(buildSkillCard(skill));
                    skillsContainer.add(Box.createVerticalStrut(10));
                }
                setProgressState(completedSkillCount(skills), skills.size());
            }
        }

        skillsContainer.revalidate();
        skillsContainer.repaint();
    }

    private void updateCompanySummary() {
        if (selectedCompanyNode == null) {
            companyTitleLabel.setText("Select a company");
            companyFocusLabel.setText("");
            roleTitleLabel.setText("");
            roleBodyLabel.setText("");
            selectedSummaryLabel.setText("Choose a company and role to start the roadmap.");
            roleCountLabel.setText("-");
            skillProgressLabel.setText("-");
            return;
        }

        CompanyProfile profile = companyProfiles.get(selectedCompanyNode.name);
        List<Node> roles = relationGraph.getNeighbors(selectedCompanyNode);
        companyTitleLabel.setText(profile.name);
        companyFocusLabel.setText(profile.descriptor + "  |  " + profile.focus);
        selectedSummaryLabel.setText(profile.name + " / " + roles.size() + " mapped roles / graph-driven prep roadmap");
        roleCountLabel.setText(String.valueOf(roles.size()));

        if (selectedRoleNode != null) {
            roleTitleLabel.setText(selectedRoleNode.name);
            roleBodyLabel.setText(htmlText(roleDescriptions.getOrDefault(selectedRoleNode.name, "Review the required skill stack and build readiness before marking the role prepared."), 360));
            List<Node> skills = relationGraph.getNeighbors(selectedRoleNode);
            skillProgressLabel.setText(completedSkillCount(skills) + "/" + skills.size());
        } else {
            roleTitleLabel.setText("No role selected");
            roleBodyLabel.setText("");
            skillProgressLabel.setText("-");
        }
    }

    private void markRolePrepared() {
        if (selectedCompanyNode == null || selectedRoleNode == null) return;

        String roleKey = rolePrepKey(selectedCompanyNode, selectedRoleNode);
        List<Node> skills = relationGraph.getNeighbors(selectedRoleNode);
        int completed = completedSkillCount(skills);
        boolean alreadyPrepared = preparedRoleKeys.contains(roleKey);

        if (alreadyPrepared) {
            JOptionPane.showMessageDialog(this,
                "This role is already marked prepared in the current session.",
                "Already Counted", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String message = completed < skills.size()
            ? "Only " + completed + " of " + skills.size() + " required skills are checked.\nMark this role prepared anyway?"
            : "Mark preparation complete for \"" + selectedRoleNode.name + "\" at " + selectedCompanyNode.name + "?";

        int choice = JOptionPane.showConfirmDialog(this, message, "Mark Role Ready", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        preparedRoleKeys.add(roleKey);
        int preparedCount = UserProfileCache.getCompaniesPrepared() + 1;
        UserProfileCache.setCompaniesPrepared(preparedCount);
        int userId = UserProfileCache.getCurrentUserId();
        if (userId != -1) {
            StatsService.saveCompaniesPrepared(userId, preparedCount);
        }
        updateStatusLabel();
        updateCompanySummary();

        JOptionPane.showMessageDialog(this,
            "Role readiness saved for " + selectedCompanyNode.name + " / " + selectedRoleNode.name + ".",
            "Prep Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private int completedSkillCount(List<Node> skills) {
        int count = 0;
        for (Node skill : skills) {
            if (completedSkills.getOrDefault(skillKey(selectedCompanyNode, selectedRoleNode, skill), false)) {
                count++;
            }
        }
        return count;
    }

    private void setProgressState(int completed, int total) {
        skillProgressBar.setMaximum(Math.max(1, total));
        skillProgressBar.setValue(Math.min(completed, Math.max(1, total)));
        skillProgressBar.setString(total == 0 ? "No skill map" : completed + " / " + total + " complete");
        readinessLabel.setText(total == 0 ? "No readiness data yet" : readinessText(completed, total));
        skillProgressLabel.setText(total == 0 ? "-" : completed + "/" + total);
        if (markReadyButton != null) {
            markReadyButton.setEnabled(total > 0);
        }
    }

    private String readinessText(int completed, int total) {
        if (completed == 0) return "Readiness is just getting started.";
        if (completed < total) return "You have closed some gaps, but not the full role stack yet.";
        return "All mapped skills are checked. This role is ready to count.";
    }

    private String rolePrepKey(Node company, Node role) {
        return company.name + "::" + role.name;
    }

    private String skillKey(Node company, Node role, Node skill) {
        return company.name + "::" + role.name + "::" + skill.name;
    }

    private void updateStatusLabel() {
        statusLabel.setText("Roles Prepared " + UserProfileCache.getCompaniesPrepared());
    }

    /* ============================================================ */
    /*  CARD BUILDERS                                              */
    /* ============================================================ */
    private JComponent buildCompanyCard(Node company, boolean selected) {
        CompanyProfile profile = companyProfiles.get(company.name);
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setAccent(selected ? profile.accent : null);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel name = new JLabel(company.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(Theme.PRIMARY_TEXT);

        JLabel desc = new JLabel(htmlText(profile.descriptor, 210));
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        desc.setForeground(Theme.MUTED_TEXT);

        JLabel focus = new JLabel("Focus: " + profile.focus);
        focus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        focus.setForeground(profile.accent);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(name);
        text.add(Box.createVerticalStrut(4));
        text.add(desc);
        text.add(Box.createVerticalStrut(6));
        text.add(focus);

        card.add(text, BorderLayout.CENTER);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectCompany(company);
            }
        });
        return card;
    }

    private JComponent buildRoleCard(Node role, boolean selected) {
        Color accent = selected ? INDIGO : SLATE;
        SurfacePanel card = new SurfacePanel(new BorderLayout(0, 8));
        card.setAccent(selected ? accent : null);
        card.setBorder(new EmptyBorder(14, 16, 14, 14));

        JLabel title = new JLabel(role.name);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel body = new JLabel(htmlText(roleDescriptions.getOrDefault(role.name, "Role description unavailable."), 210));
        body.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        body.setForeground(Theme.MUTED_TEXT);

        int skillCount = relationGraph.getNeighbors(role).size();
        JLabel meta = new JLabel(skillCount + " mapped skills");
        meta.setFont(new Font("Segoe UI", Font.BOLD, 11));
        meta.setForeground(accent);

        card.add(title, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(meta, BorderLayout.SOUTH);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                selectRole(role);
            }
        });
        return card;
    }

    private JComponent buildSkillCard(Node skill) {
        SurfacePanel card = new SurfacePanel(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(12, 14, 12, 14));

        JCheckBox check = new JCheckBox(skill.name);
        check.setOpaque(false);
        check.setFont(new Font("Segoe UI", Font.BOLD, 12));
        check.setForeground(Theme.PRIMARY_TEXT);

        JLabel hint = new JLabel(htmlText(skillHints.getOrDefault(skill.name, "Review the interview expectations for this skill."), 300));
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(Theme.MUTED_TEXT);

        String key = skillKey(selectedCompanyNode, selectedRoleNode, skill);
        check.setSelected(completedSkills.getOrDefault(key, false));
        check.addActionListener(e -> {
            completedSkills.put(key, check.isSelected());
            List<Node> skills = relationGraph.getNeighbors(selectedRoleNode);
            setProgressState(completedSkillCount(skills), skills.size());
            updateCompanySummary();
        });

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(check);
        text.add(Box.createVerticalStrut(4));
        text.add(hint);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JLabel emptyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    /* ============================================================ */
    /*  THEME + VISUAL HELPERS                                     */
    /* ============================================================ */
    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        if (selectedCompanyNode != null) {
            rebuildCompanyList();
            rebuildRoleList();
            rebuildSkillsList();
            updateCompanySummary();
        }
        repaint();
    }

    private JPanel heroStat(String label, JLabel valueLabel) {
        JPanel stat = new JPanel(new BorderLayout(0, 4));
        stat.setOpaque(false);
        stat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1, true),
            new EmptyBorder(12, 12, 12, 12)));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        labelLbl.setForeground(new Color(255, 255, 255, 210));
        stat.add(valueLabel, BorderLayout.CENTER);
        stat.add(labelLbl, BorderLayout.SOUTH);
        return stat;
    }

    private JPanel statRow(String label, JLabel value, Color accent) {
        SurfacePanel row = new SurfacePanel(new BorderLayout(8, 0));
        row.setBorder(new EmptyBorder(10, 12, 10, 12));
        JLabel name = new JLabel(label);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        name.setForeground(Theme.MUTED_TEXT);
        value.setFont(new Font("Segoe UI", Font.BOLD, 12));
        value.setForeground(accent);
        row.add(name, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private String htmlText(String text, int width) {
        return "<html><body style='width:" + width + "px'>" + text + "</body></html>";
    }

    private JLabel sectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(Theme.PRIMARY_TEXT);
        return label;
    }

    private JLabel makeTinyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private JLabel makePillLabel(String text, Color color, boolean filled) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(filled ? Color.WHITE : color);
        label.setOpaque(true);
        label.setBackground(filled ? color : new Color(color.getRed(), color.getGreen(), color.getBlue(), Theme.isDark() ? 34 : 16));
        label.setBorder(pillBorder(color));
        return label;
    }

    private EmptyBorder pillPadding() {
        return new EmptyBorder(6, 12, 6, 12);
    }

    private javax.swing.border.Border pillBorder(Color color) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120), 1, true),
            pillPadding());
    }

    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class GradientCard extends JPanel {
        private final Color start;
        private final Color end;

        GradientCard(LayoutManager layout, Color start, Color end) {
            super(layout);
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class SurfacePanel extends JPanel {
        private Color accent;

        SurfacePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        void setAccent(Color accent) {
            this.accent = accent;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Theme.PANEL_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(Theme.FIELD_BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (accent != null) {
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight(), 5, 5);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
