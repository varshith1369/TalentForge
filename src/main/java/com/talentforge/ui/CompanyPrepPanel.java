package com.talentforge.ui;

import com.talentforge.db.StatsService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

/**
 * Company-wise Preparation Module.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>Graph</b> — Company → Role → Skill relationships. Represents a directed
 *       acyclic graph where companies connect to roles, and roles connect to skills.</li>
 * </ul>
 */
public class CompanyPrepPanel extends JPanel {

    /* ============================================================ */
    /*  GRAPH DATA STRUCTURE                                        */
    /* ============================================================ */
    private static class Node {
        final String name;
        final String type; // "company", "role", "skill"

        Node(String name, String type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Node node)) return false;
            return name.equals(node.name) && type.equals(node.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
    }

    private static class Graph {
        private final Map<Node, List<Node>> adjList = new HashMap<>();

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

    /* ============================================================ */
    /*  UI STATE & REFERENCES                                       */
    /* ============================================================ */
    private final Graph relationGraph = new Graph();
    private Node selectedCompanyNode = null;
    private Node selectedRoleNode = null;

    private final JPanel companiesContainer = new JPanel();
    private final JPanel rolesContainer = new JPanel();
    private final JPanel skillsContainer = new JPanel();
    private final JLabel statusLabel = new JLabel();

    private final Map<String, Boolean> completedSkills = new HashMap<>();

    public CompanyPrepPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        initializeGraph();
        buildUI();
        selectCompany(new Node("Google", "company"));

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChange));
    }

    /* ============================================================ */
    /*  GRAPH INITIALIZATION                                        */
    /* ============================================================ */
    private void initializeGraph() {
        // Companies
        Node google = new Node("Google", "company");
        Node microsoft = new Node("Microsoft", "company");
        Node amazon = new Node("Amazon", "company");
        Node tcs = new Node("TCS", "company");
        Node infosys = new Node("Infosys", "company");

        // Roles
        Node sde = new Node("Software Development Engineer", "role");
        Node dataAnalyst = new Node("Data Analyst", "role");
        Node cloudSupport = new Node("Cloud Support Engineer", "role");
        Node systemsEng = new Node("Systems Engineer", "role");

        // Skills
        Node dsa = new Node("Data Structures & Algorithms", "skill");
        Node systemDesign = new Node("System Design", "skill");
        Node java = new Node("Java Programming", "skill");
        Node sql = new Node("SQL & Databases", "skill");
        Node networking = new Node("Computer Networks", "skill");
        Node operatingSystems = new Node("Operating Systems", "skill");

        // Edges: Google
        relationGraph.addEdge(google, sde);
        relationGraph.addEdge(google, dataAnalyst);
        // Edges: Microsoft
        relationGraph.addEdge(microsoft, sde);
        relationGraph.addEdge(microsoft, cloudSupport);
        // Edges: Amazon
        relationGraph.addEdge(amazon, sde);
        relationGraph.addEdge(amazon, dataAnalyst);
        relationGraph.addEdge(amazon, cloudSupport);
        // Edges: TCS
        relationGraph.addEdge(tcs, systemsEng);
        relationGraph.addEdge(tcs, dataAnalyst);
        // Edges: Infosys
        relationGraph.addEdge(infosys, systemsEng);
        relationGraph.addEdge(infosys, cloudSupport);

        // Edges: SDE -> Skills
        relationGraph.addEdge(sde, dsa);
        relationGraph.addEdge(sde, systemDesign);
        relationGraph.addEdge(sde, java);

        // Edges: Data Analyst -> Skills
        relationGraph.addEdge(dataAnalyst, sql);
        relationGraph.addEdge(dataAnalyst, pythonNode());

        // Edges: Cloud Support -> Skills
        relationGraph.addEdge(cloudSupport, networking);
        relationGraph.addEdge(cloudSupport, sql);

        // Edges: Systems Engineer -> Skills
        relationGraph.addEdge(systemsEng, java);
        relationGraph.addEdge(systemsEng, operatingSystems);
    }

    private Node pythonNode() {
        return new Node("Python Scripting", "skill");
    }

    /* ============================================================ */
    /*  UI ASSEMBLY                                                 */
    /* ============================================================ */
    private void buildUI() {
        // TOP Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.FIELD_BORDER),
                new EmptyBorder(16, 24, 16, 24)));

        JLabel title = new JLabel("🏢 Company-wise Preparation");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Theme.PRIMARY_TEXT);

        JLabel subtitle = new JLabel("Explore company roles and prepare the required skills through our graph relationships.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(Theme.MUTED_TEXT);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(title, BorderLayout.NORTH);
        left.add(subtitle, BorderLayout.SOUTH);

        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(Theme.PRIMARY_START);
        updateStatusLabel();

        header.add(left, BorderLayout.CENTER);
        header.add(statusLabel, BorderLayout.EAST);

        // Grid Panel for Columns
        JPanel columnsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        columnsPanel.setOpaque(false);
        columnsPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Col 1: Companies list
        columnsPanel.add(buildColumn("1. Select Company", companiesContainer));

        // Col 2: Roles list
        columnsPanel.add(buildColumn("2. Available Roles", rolesContainer));

        // Col 3: Skills list
        columnsPanel.add(buildColumn("3. Required Skills", skillsContainer));

        add(header, BorderLayout.NORTH);
        add(columnsPanel, BorderLayout.CENTER);
    }

    private JPanel buildColumn(String title, JPanel container) {
        JPanel col = new JPanel(new BorderLayout(0, 8));
        col.setOpaque(false);

        JLabel colTitle = new JLabel(title);
        colTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        colTitle.setForeground(Theme.PRIMARY_TEXT);

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        col.add(colTitle, BorderLayout.NORTH);
        col.add(scroll, BorderLayout.CENTER);

        return col;
    }

    /* ============================================================ */
    /*  SELECTION LOGIC & GRAPH TRAVERSAL                            */
    /* ============================================================ */
    private void selectCompany(Node company) {
        selectedCompanyNode = company;
        selectedRoleNode = null;

        // Rebuild Company cards
        companiesContainer.removeAll();
        Node[] companies = {
            new Node("Google", "company"),
            new Node("Microsoft", "company"),
            new Node("Amazon", "company"),
            new Node("TCS", "company"),
            new Node("Infosys", "company")
        };
        for (Node c : companies) {
            companiesContainer.add(buildCompanyCard(c, c.equals(selectedCompanyNode)));
            companiesContainer.add(Box.createVerticalStrut(8));
        }

        // Rebuild Roles based on Graph neighbors of selected Company
        rolesContainer.removeAll();
        List<Node> roles = relationGraph.getNeighbors(selectedCompanyNode);
        if (roles.isEmpty()) {
            JLabel empty = new JLabel("No roles found.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(Theme.MUTED_TEXT);
            rolesContainer.add(empty);
        } else {
            for (Node r : roles) {
                rolesContainer.add(buildRoleCard(r, r.equals(selectedRoleNode)));
                rolesContainer.add(Box.createVerticalStrut(8));
            }
            // Auto select first role
            selectRole(roles.get(0));
        }

        companiesContainer.revalidate();
        companiesContainer.repaint();
        rolesContainer.revalidate();
        rolesContainer.repaint();
    }

    private void selectRole(Node role) {
        selectedRoleNode = role;

        // Refresh roles highlight
        rolesContainer.removeAll();
        List<Node> roles = relationGraph.getNeighbors(selectedCompanyNode);
        for (Node r : roles) {
            rolesContainer.add(buildRoleCard(r, r.equals(selectedRoleNode)));
            rolesContainer.add(Box.createVerticalStrut(8));
        }

        // Rebuild Skills based on Graph neighbors of selected Role
        skillsContainer.removeAll();
        List<Node> skills = relationGraph.getNeighbors(selectedRoleNode);
        if (skills.isEmpty()) {
            JLabel empty = new JLabel("No skills required.");
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            empty.setForeground(Theme.MUTED_TEXT);
            skillsContainer.add(empty);
        } else {
            for (Node s : skills) {
                skillsContainer.add(buildSkillCard(s));
                skillsContainer.add(Box.createVerticalStrut(8));
            }
            // Add a "Mark Role Prepped" button at the bottom of the list
            skillsContainer.add(Box.createVerticalStrut(16));
            JButton prepBtn = createGradientButton("Mark Role Ready ✓", 160, 36);
            prepBtn.addActionListener(e -> markRolePrepared());
            skillsContainer.add(prepBtn);
        }

        rolesContainer.revalidate();
        rolesContainer.repaint();
        skillsContainer.revalidate();
        skillsContainer.repaint();
    }

    private void markRolePrepared() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Mark preparation complete for \"" + selectedRoleNode.name + "\" at " + selectedCompanyNode.name + "?",
                "Mark Complete", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            int preppedCount = UserProfileCache.getCompaniesPrepared() + 1;
            UserProfileCache.setCompaniesPrepared(preppedCount);
            StatsService.saveCompaniesPrepared(UserProfileCache.getCurrentUserId(), preppedCount);
            updateStatusLabel();

            JOptionPane.showMessageDialog(this,
                    "🎉 Role preparation marked complete!\nYour dashboard stats have been updated.",
                    "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateStatusLabel() {
        statusLabel.setText("Roles Prepped: " + UserProfileCache.getCompaniesPrepared());
    }

    /* ============================================================ */
    /*  CARD BUILDERS                                               */
    /* ============================================================ */
    private ElevatedCard buildCompanyCard(Node company, boolean selected) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        if (selected) {
            card.setAccentColor(Theme.PRIMARY_START);
        }

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        inner.setOpaque(false);

        String emoji = switch (company.name) {
            case "Google" -> "🌐";
            case "Microsoft" -> "💻";
            case "Amazon" -> "📦";
            default -> "🏢";
        };

        JLabel icon = new JLabel(emoji);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel name = new JLabel(company.name);
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        name.setForeground(Theme.PRIMARY_TEXT);

        inner.add(icon);
        inner.add(name);
        card.add(inner, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectCompany(company);
            }
        });

        return card;
    }

    private ElevatedCard buildRoleCard(Node role, boolean selected) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());
        if (selected) {
            card.setAccentColor(Theme.PRIMARY_END);
        }

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel name = new JLabel("<html><body><b>" + role.name + "</b></body></html>");
        name.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        name.setForeground(Theme.PRIMARY_TEXT);

        inner.add(name, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectRole(role);
            }
        });

        return card;
    }

    private ElevatedCard buildSkillCard(Node skill) {
        ElevatedCard card = new ElevatedCard(new BorderLayout());

        JPanel inner = new JPanel(new BorderLayout(8, 0));
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(12, 16, 12, 16));

        JCheckBox check = new JCheckBox(skill.name);
        check.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        check.setForeground(Theme.PRIMARY_TEXT);
        check.setOpaque(false);

        String skillKey = selectedCompanyNode.name + "_" + selectedRoleNode.name + "_" + skill.name;
        check.setSelected(completedSkills.getOrDefault(skillKey, false));
        check.addActionListener(e -> completedSkills.put(skillKey, check.isSelected()));

        inner.add(check, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private void onThemeChange() {
        setBackground(Theme.PAGE_BG);
        selectCompany(selectedCompanyNode);
        repaint();
    }

    private JButton createGradientButton(String text, int w, int h) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_START, getWidth(), 0, Theme.PRIMARY_END));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(w, h));
        btn.setMaximumSize(new Dimension(w, h));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
