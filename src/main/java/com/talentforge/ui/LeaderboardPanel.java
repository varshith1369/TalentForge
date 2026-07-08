package com.talentforge.ui;

import com.talentforge.db.StatsService;
import com.talentforge.db.StatsService.UserRank;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

/**
 * Leaderboard Panel.
 *
 * <h3>Data Structures (project requirement)</h3>
 * <ul>
 *   <li><b>PriorityQueue&lt;UserRank&gt;</b> - Max-heap ranking system. Surfaces the top
 *       performers in score order by repeated poll() operations.</li>
 * </ul>
 */
public class LeaderboardPanel extends JPanel {

    private enum FilterMode {
        ALL,
        TOP_TEN,
        CURRENT_USER
    }

    private static final Comparator<UserRank> RANK_COMPARATOR = (left, right) -> {
        int scoreCompare = Integer.compare(right.getScore(), left.getScore());
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        int solvedCompare = Integer.compare(right.problemsSolved, left.problemsSolved);
        if (solvedCompare != 0) {
            return solvedCompare;
        }
        return safeName(left.name).compareToIgnoreCase(safeName(right.name));
    };

    private static final Color SKY = new Color(56, 189, 248);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color ROSE = new Color(244, 63, 94);
    private static final Color GOLD = new Color(245, 180, 52);
    private static final Color SILVER = new Color(148, 163, 184);
    private static final Color BRONZE = new Color(180, 110, 74);

    private final PriorityQueue<UserRank> rankQueue = new PriorityQueue<>(RANK_COMPARATOR);
    private final List<UserRank> rankedUsers = new ArrayList<>();

    private final JTextField searchField = new JTextField();
    private final JPanel podiumPanel = new JPanel();
    private final JPanel standingsPanel = new JPanel();
    private final JPanel spotlightPanel = new JPanel(new BorderLayout());

    private JLabel totalPlayersValue;
    private JLabel averageScoreValue;
    private JLabel topScoreValue;
    private JLabel yourRankValue;
    private JLabel summaryLabel;
    private JLabel scoreFormulaLabel;
    private JButton allButton;
    private JButton topTenButton;
    private JButton myRankButton;

    private FilterMode filterMode = FilterMode.ALL;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);

        loadRankings();
        buildUI();
        refreshView();

        Theme.addListener(() -> SwingUtilities.invokeLater(this::onThemeChanged));
    }

    private void loadRankings() {
        rankQueue.clear();

        List<UserRank> loaded = StatsService.loadAllUserRanks();
        for (UserRank rank : loaded) {
            rankQueue.offer(normalizeRank(rank));
        }

        if (loaded.size() < 5) {
            rankQueue.offer(new UserRank(-10, "Priya Sharma", 85, 95, 12, 92));
            rankQueue.offer(new UserRank(-20, "Rahul Verma", 72, 88, 10, 85));
            rankQueue.offer(new UserRank(-30, "Aarav Mehta", 60, 90, 8, 80));
            rankQueue.offer(new UserRank(-40, "Sneha Iyer", 95, 98, 15, 96));
            rankQueue.offer(new UserRank(-50, "Aditya Rao", 78, 84, 11, 88));
        }

        rebuildRankedUsers();
    }

    private void rebuildRankedUsers() {
        rankedUsers.clear();
        PriorityQueue<UserRank> temp = new PriorityQueue<>(rankQueue);
        while (!temp.isEmpty()) {
            rankedUsers.add(temp.poll());
        }
    }

    private void buildUI() {
        removeAll();

        JPanel scrollContent = new JPanel();
        scrollContent.setOpaque(false);
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBorder(new EmptyBorder(24, 24, 24, 24));

        scrollContent.add(buildHeroSection());
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(buildSummaryStrip());
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(buildShowcaseSection());
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(buildStandingsSection());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JComponent buildHeroSection() {
        GradientPanel hero = new GradientPanel(new BorderLayout(24, 0), Theme.PRIMARY_START, Theme.PRIMARY_END);
        hero.setBorder(new EmptyBorder(22, 24, 22, 24));
        hero.setPreferredSize(new Dimension(0, 170));
        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel eyebrow = new JLabel("Performance leaderboard");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        eyebrow.setForeground(new Color(225, 231, 255));

        JLabel title = new JLabel("Compete, track momentum, and see your position.");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>Rankings combine coding progress, aptitude performance, mock interviews, and resume strength into one clean scoreboard.</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(230, 236, 255));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        scoreFormulaLabel = new JLabel("Score formula: solved x 2 + aptitude + interviews x 10 + resume");
        scoreFormulaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scoreFormulaLabel.setForeground(new Color(214, 222, 255));

        textBlock.add(eyebrow);
        textBlock.add(Box.createVerticalStrut(10));
        textBlock.add(title);
        textBlock.add(Box.createVerticalStrut(8));
        textBlock.add(subtitle);
        textBlock.add(Box.createVerticalGlue());
        textBlock.add(Box.createVerticalStrut(14));
        textBlock.add(scoreFormulaLabel);

        JPanel actionBlock = new JPanel();
        actionBlock.setOpaque(false);
        actionBlock.setLayout(new BoxLayout(actionBlock, BoxLayout.Y_AXIS));

        JButton refreshButton = createPrimaryButton("Refresh rankings");
        refreshButton.addActionListener(e -> refreshAll());
        refreshButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryLabel.setForeground(Color.WHITE);
        summaryLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel helper = new JLabel("Live board across every practice module");
        helper.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helper.setForeground(new Color(222, 229, 255));
        helper.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        chips.setOpaque(false);
        chips.add(createHeroChip("Coding"));
        chips.add(createHeroChip("Aptitude"));
        chips.add(createHeroChip("Interview"));
        chips.add(createHeroChip("Resume"));

        actionBlock.add(refreshButton);
        actionBlock.add(Box.createVerticalStrut(16));
        actionBlock.add(summaryLabel);
        actionBlock.add(Box.createVerticalStrut(4));
        actionBlock.add(helper);
        actionBlock.add(Box.createVerticalGlue());
        actionBlock.add(chips);

        hero.add(textBlock, BorderLayout.CENTER);
        hero.add(actionBlock, BorderLayout.EAST);

        return hero;
    }

    private JComponent buildSummaryStrip() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        totalPlayersValue = new JLabel();
        averageScoreValue = new JLabel();
        topScoreValue = new JLabel();
        yourRankValue = new JLabel();

        panel.add(createMetricCard("Players", totalPlayersValue, "Total ranked learners", SKY));
        panel.add(createMetricCard("Average score", averageScoreValue, "Overall performance level", TEAL));
        panel.add(createMetricCard("Top score", topScoreValue, "Current best on the board", AMBER));
        panel.add(createMetricCard("Your rank", yourRankValue, "Personal standing snapshot", ROSE));
        return panel;
    }

    private JComponent buildShowcaseSection() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 18, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        spotlightPanel.setOpaque(false);
        spotlightPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        panel.add(spotlightPanel);

        JPanel podiumSurface = new SurfacePanel(new BorderLayout(0, 16));
        podiumSurface.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = createSectionTitle("Top Performers");
        JLabel subtitle = createMutedLabel("The strongest profiles right now, ordered by the weighted score.");

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        podiumPanel.setOpaque(false);
        podiumPanel.setLayout(new GridLayout(1, 3, 14, 0));

        podiumSurface.add(header, BorderLayout.NORTH);
        podiumSurface.add(podiumPanel, BorderLayout.CENTER);

        panel.add(podiumSurface);
        return panel;
    }

    private JComponent buildStandingsSection() {
        JPanel panel = new SurfacePanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel top = new JPanel(new BorderLayout(16, 0));
        top.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(createSectionTitle("Full Standings"));
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(createMutedLabel("Search by learner name or narrow the board to your own placement."));

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);

        styleSearchField(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshStandings();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshStandings();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshStandings();
            }
        });

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterGroup.setOpaque(false);

        allButton = createFilterButton("All", FilterMode.ALL);
        topTenButton = createFilterButton("Top 10", FilterMode.TOP_TEN);
        myRankButton = createFilterButton("My rank", FilterMode.CURRENT_USER);

        filterGroup.add(allButton);
        filterGroup.add(topTenButton);
        filterGroup.add(myRankButton);

        controls.add(searchField, BorderLayout.CENTER);
        controls.add(filterGroup, BorderLayout.EAST);

        top.add(titleBlock, BorderLayout.WEST);
        top.add(controls, BorderLayout.CENTER);

        standingsPanel.setOpaque(false);
        standingsPanel.setLayout(new BoxLayout(standingsPanel, BoxLayout.Y_AXIS));

        JPanel rowsWrapper = new JPanel(new BorderLayout());
        rowsWrapper.setOpaque(false);
        rowsWrapper.add(standingsPanel, BorderLayout.NORTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(rowsWrapper, BorderLayout.CENTER);
        return panel;
    }

    private void refreshAll() {
        loadRankings();
        refreshView();
    }

    private void refreshView() {
        refreshSummary();
        refreshSpotlight();
        refreshPodium();
        refreshStandings();
    }

    private void refreshSummary() {
        int totalPlayers = rankedUsers.size();
        int totalScore = 0;
        int bestScore = 0;

        for (UserRank rank : rankedUsers) {
            totalScore += rank.getScore();
            bestScore = Math.max(bestScore, rank.getScore());
        }

        int average = totalPlayers == 0 ? 0 : Math.round((float) totalScore / totalPlayers);
        int yourRank = currentUserPosition();

        totalPlayersValue.setText(String.valueOf(totalPlayers));
        averageScoreValue.setText(average + " pts");
        topScoreValue.setText(bestScore + " pts");
        yourRankValue.setText(yourRank > 0 ? "#" + yourRank : "Unranked");

        if (summaryLabel != null) {
            if (yourRank > 0) {
                summaryLabel.setText("You are currently #" + yourRank + " out of " + totalPlayers);
            } else {
                summaryLabel.setText(totalPlayers + " learners on the board");
            }
        }
    }

    private void refreshSpotlight() {
        spotlightPanel.removeAll();

        JPanel card = new SurfacePanel(new BorderLayout(0, 16));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        card.add(createSpotlightHeader(), BorderLayout.NORTH);
        card.add(createSpotlightBody(), BorderLayout.CENTER);

        spotlightPanel.add(card, BorderLayout.CENTER);
        spotlightPanel.revalidate();
        spotlightPanel.repaint();
    }

    private JComponent createSpotlightHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(createSectionTitle("Your Spotlight"));
        header.add(Box.createVerticalStrut(4));
        header.add(createMutedLabel("A quick read of where you stand and what is driving your score."));
        return header;
    }

    private JComponent createSpotlightBody() {
        UserRank currentUser = currentUserRank();
        if (currentUser == null) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);

            JLabel title = new JLabel("No personal ranking available yet");
            title.setFont(new Font("Segoe UI", Font.BOLD, 20));
            title.setForeground(Theme.PRIMARY_TEXT);

            JLabel text = new JLabel("<html>Complete practice modules and save progress to see your own standing appear here.</html>");
            text.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            text.setForeground(Theme.MUTED_TEXT);

            empty.add(title, BorderLayout.NORTH);
            empty.add(text, BorderLayout.CENTER);
            return empty;
        }

        int rankIndex = currentUserPosition();
        int totalPlayers = Math.max(1, rankedUsers.size());
        int percentile = 100 - Math.round(((float) (rankIndex - 1) / totalPlayers) * 100f);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(safeName(currentUser.name));
        name.setFont(new Font("Segoe UI", Font.BOLD, 24));
        name.setForeground(Theme.PRIMARY_TEXT);

        JLabel line = new JLabel("Rank #" + rankIndex + " of " + totalPlayers + "   |   " + percentile + "th percentile");
        line.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        line.setForeground(Theme.MUTED_TEXT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setOpaque(false);
        badgeRow.add(createInlineBadge("Score " + currentUser.getScore() + " pts", Theme.PRIMARY_START, Color.WHITE));
        badgeRow.add(createInlineBadge("Solved " + currentUser.problemsSolved, mix(TEAL, Theme.PANEL_BG, 0.15f), TEAL));
        badgeRow.add(createInlineBadge("Interviews " + currentUser.mockInterviews, mix(AMBER, Theme.PANEL_BG, 0.14f), new Color(148, 98, 0)));

        left.add(name);
        left.add(Box.createVerticalStrut(6));
        left.add(line);
        left.add(Box.createVerticalStrut(14));
        left.add(badgeRow);

        JPanel metrics = new JPanel(new GridLayout(2, 2, 10, 10));
        metrics.setOpaque(false);
        metrics.add(createMiniMetric("Coding", String.valueOf(currentUser.problemsSolved), SKY));
        metrics.add(createMiniMetric("Aptitude", currentUser.aptitudeScore + "%", TEAL));
        metrics.add(createMiniMetric("Interviews", String.valueOf(currentUser.mockInterviews), AMBER));
        metrics.add(createMiniMetric("Resume", currentUser.resumeScore + "%", ROSE));

        body.add(left, BorderLayout.CENTER);
        body.add(metrics, BorderLayout.EAST);
        return body;
    }

    private void refreshPodium() {
        podiumPanel.removeAll();

        if (rankedUsers.isEmpty()) {
            podiumPanel.add(createEmptyPodiumColumn());
            podiumPanel.add(createEmptyPodiumColumn());
            podiumPanel.add(createEmptyPodiumColumn());
        } else {
            UserRank first = rankedUsers.size() > 0 ? rankedUsers.get(0) : null;
            UserRank second = rankedUsers.size() > 1 ? rankedUsers.get(1) : null;
            UserRank third = rankedUsers.size() > 2 ? rankedUsers.get(2) : null;

            podiumPanel.add(createPodiumCard(second, "2", SILVER, 150));
            podiumPanel.add(createPodiumCard(first, "1", GOLD, 188));
            podiumPanel.add(createPodiumCard(third, "3", BRONZE, 132));
        }

        podiumPanel.revalidate();
        podiumPanel.repaint();
    }

    private JComponent createEmptyPodiumColumn() {
        JPanel panel = new SurfacePanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(18, 14, 18, 14));
        panel.setPreferredSize(new Dimension(0, 160));
        panel.add(createMutedCenteredLabel("Waiting for more data"), BorderLayout.CENTER);
        return panel;
    }

    private JComponent createPodiumCard(UserRank rank, String position, Color accent, int height) {
        JPanel panel = new SurfacePanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(16, 14, 16, 14));
        panel.setPreferredSize(new Dimension(0, height));

        if (rank == null) {
            panel.add(createMutedCenteredLabel("No player"), BorderLayout.CENTER);
            return panel;
        }

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JLabel crown = new JLabel(position);
        crown.setAlignmentX(Component.CENTER_ALIGNMENT);
        crown.setFont(new Font("Segoe UI", Font.BOLD, 28));
        crown.setForeground(accent);

        JLabel name = new JLabel(safeName(rank.name), SwingConstants.CENTER);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 16));
        name.setForeground(Theme.PRIMARY_TEXT);

        JLabel score = new JLabel(rank.getScore() + " pts", SwingConstants.CENTER);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        score.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        score.setForeground(Theme.MUTED_TEXT);

        top.add(crown);
        top.add(Box.createVerticalStrut(4));
        top.add(name);
        top.add(Box.createVerticalStrut(4));
        top.add(score);

        JPanel details = new JPanel(new GridLayout(2, 2, 8, 8));
        details.setOpaque(false);
        details.add(createPodiumStat("Solved", String.valueOf(rank.problemsSolved), accent));
        details.add(createPodiumStat("Aptitude", rank.aptitudeScore + "%", accent));
        details.add(createPodiumStat("Interview", String.valueOf(rank.mockInterviews), accent));
        details.add(createPodiumStat("Resume", rank.resumeScore + "%", accent));

        panel.add(top, BorderLayout.NORTH);
        panel.add(details, BorderLayout.CENTER);
        return panel;
    }

    private JComponent createPodiumStat(String label, String value, Color accent) {
        JPanel panel = new JPanel(new BorderLayout(0, 2));
        panel.setOpaque(false);

        JLabel caption = new JLabel(label, SwingConstants.CENTER);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        caption.setForeground(Theme.MUTED_TEXT);

        JLabel metric = new JLabel(value, SwingConstants.CENTER);
        metric.setFont(new Font("Segoe UI", Font.BOLD, 12));
        metric.setForeground(accent);

        panel.add(caption, BorderLayout.NORTH);
        panel.add(metric, BorderLayout.CENTER);
        return panel;
    }

    private void refreshStandings() {
        standingsPanel.removeAll();

        List<Integer> indexes = filteredIndexes();
        if (indexes.isEmpty()) {
            standingsPanel.add(createEmptyState());
        } else {
            int topScore = rankedUsers.isEmpty() ? 1 : Math.max(1, rankedUsers.get(0).getScore());
            for (Integer index : indexes) {
                UserRank rank = rankedUsers.get(index);
                standingsPanel.add(createRankRow(rank, index + 1, topScore));
                standingsPanel.add(Box.createVerticalStrut(10));
            }
        }

        standingsPanel.revalidate();
        standingsPanel.repaint();
        syncFilterButtons();
    }

    private List<Integer> filteredIndexes() {
        List<Integer> indexes = new ArrayList<>();
        String query = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ENGLISH);
        int currentUserId = UserProfileCache.getCurrentUserId();

        for (int i = 0; i < rankedUsers.size(); i++) {
            UserRank rank = rankedUsers.get(i);
            boolean matchesQuery = query.isEmpty() || safeName(rank.name).toLowerCase(Locale.ENGLISH).contains(query);
            if (!matchesQuery) {
                continue;
            }

            boolean matchesFilter = switch (filterMode) {
                case ALL -> true;
                case TOP_TEN -> i < 10;
                case CURRENT_USER -> rank.userId == currentUserId;
            };

            if (matchesFilter) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    private JComponent createRankRow(UserRank rank, int position, int topScore) {
        JPanel card = new SurfacePanel(new BorderLayout(16, 0));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean isCurrentUser = rank.userId == UserProfileCache.getCurrentUserId();

        JPanel left = new JPanel(new BorderLayout(14, 0));
        left.setOpaque(false);

        left.add(createRankBadge(position), BorderLayout.WEST);

        JPanel identity = new JPanel();
        identity.setOpaque(false);
        identity.setLayout(new BoxLayout(identity, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(safeName(rank.name));
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(Theme.PRIMARY_TEXT);

        JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tagRow.setOpaque(false);
        if (isCurrentUser) {
            tagRow.add(createInlineBadge("You", mix(Theme.PRIMARY_START, Theme.PANEL_BG, 0.14f), Theme.PRIMARY_START));
        }
        tagRow.add(createInlineBadge("Coding " + rank.problemsSolved, mix(SKY, Theme.PANEL_BG, 0.14f), SKY.darker()));
        tagRow.add(createInlineBadge("Aptitude " + rank.aptitudeScore + "%", mix(TEAL, Theme.PANEL_BG, 0.14f), new Color(9, 116, 102)));

        identity.add(name);
        identity.add(Box.createVerticalStrut(6));
        identity.add(tagRow);

        left.add(identity, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);

        JPanel metrics = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        metrics.setOpaque(false);
        metrics.add(createValueLabel("Interview", String.valueOf(rank.mockInterviews)));
        metrics.add(createValueLabel("Resume", rank.resumeScore + "%"));
        metrics.add(createScorePill(rank.getScore() + " pts"));

        JProgressBar bar = new JProgressBar(0, topScore);
        bar.setValue(rank.getScore());
        bar.setBorderPainted(false);
        bar.setStringPainted(false);
        bar.setForeground(isCurrentUser ? Theme.PRIMARY_START : TEAL);
        bar.setBackground(Theme.isDark() ? new Color(47, 49, 64) : new Color(229, 231, 235));
        bar.setPreferredSize(new Dimension(220, 8));

        right.add(metrics, BorderLayout.NORTH);
        right.add(bar, BorderLayout.SOUTH);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JComponent createRankBadge(int position) {
        Color background;
        Color foreground;

        if (position == 1) {
            background = mix(GOLD, Theme.PANEL_BG, 0.20f);
            foreground = GOLD.darker();
        } else if (position == 2) {
            background = mix(SILVER, Theme.PANEL_BG, 0.18f);
            foreground = new Color(90, 102, 118);
        } else if (position == 3) {
            background = mix(BRONZE, Theme.PANEL_BG, 0.18f);
            foreground = new Color(140, 85, 58);
        } else {
            background = mix(Theme.FIELD_BORDER, Theme.PANEL_BG, 0.35f);
            foreground = Theme.PRIMARY_TEXT;
        }

        JLabel badge = new JLabel("#" + position, SwingConstants.CENTER);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setForeground(foreground);
        badge.setPreferredSize(new Dimension(58, 32));
        badge.setOpaque(true);
        badge.setBackground(background);
        return badge;
    }

    private JComponent createScorePill(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(Theme.PRIMARY_START);
        label.setBorder(new EmptyBorder(7, 12, 7, 12));
        return label;
    }

    private JComponent createValueLabel(String label, String value) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel caption = new JLabel(label, SwingConstants.RIGHT);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        caption.setForeground(Theme.MUTED_TEXT);
        caption.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel metric = new JLabel(value, SwingConstants.RIGHT);
        metric.setFont(new Font("Segoe UI", Font.BOLD, 13));
        metric.setForeground(Theme.PRIMARY_TEXT);
        metric.setAlignmentX(Component.RIGHT_ALIGNMENT);

        panel.add(caption);
        panel.add(Box.createVerticalStrut(2));
        panel.add(metric);
        return panel;
    }

    private JComponent createEmptyState() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 6, 20, 6));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("No leaderboard entries match this view.");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Theme.PRIMARY_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel copy = new JLabel("Try a different search or switch back to the full board.");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copy.setForeground(Theme.MUTED_TEXT);
        copy.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(8));
        panel.add(copy);
        return panel;
    }

    private JPanel createMetricCard(String label, JLabel valueLabel, String caption, Color accent) {
        JPanel card = new SurfacePanel(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel(label);
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(Theme.PRIMARY_TEXT);

        JPanel accentDot = new JPanel();
        accentDot.setBackground(accent);
        accentDot.setPreferredSize(new Dimension(10, 10));

        top.add(title, BorderLayout.WEST);
        top.add(accentDot, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(Theme.PRIMARY_TEXT);

        JLabel help = new JLabel(caption);
        help.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        help.setForeground(Theme.MUTED_TEXT);

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(help, BorderLayout.SOUTH);
        return card;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 90), getWidth(), getHeight(), new Color(255, 255, 255, 28)));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 16, 10, 16));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createHeroChip(String text) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        chip.setOpaque(true);
        chip.setBackground(new Color(255, 255, 255, 36));
        chip.setBorder(new EmptyBorder(7, 12, 7, 12));

        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chip.add(label);
        return chip;
    }

    private JButton createFilterButton(String text, FilterMode targetMode) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        button.addActionListener(e -> {
            filterMode = targetMode;
            refreshStandings();
        });
        return button;
    }

    private void syncFilterButtons() {
        styleFilterButton(allButton, filterMode == FilterMode.ALL);
        styleFilterButton(topTenButton, filterMode == FilterMode.TOP_TEN);
        styleFilterButton(myRankButton, filterMode == FilterMode.CURRENT_USER);
    }

    private void styleFilterButton(JButton button, boolean active) {
        if (button == null) {
            return;
        }
        button.setOpaque(true);
        button.setBackground(active ? Theme.PRIMARY_START : Theme.PANEL_BG);
        button.setForeground(active ? Color.WHITE : Theme.PRIMARY_TEXT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? Theme.PRIMARY_START : Theme.FIELD_BORDER),
                new EmptyBorder(9, 14, 9, 14)));
    }

    private void styleSearchField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setForeground(Theme.PRIMARY_TEXT);
        field.setBackground(Theme.FIELD_BG);
        field.setCaretColor(Theme.PRIMARY_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELD_BORDER),
                new EmptyBorder(10, 12, 10, 12)));
        field.setPreferredSize(new Dimension(250, 40));
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Theme.PRIMARY_TEXT);
        return label;
    }

    private JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Theme.MUTED_TEXT);
        return label;
    }

    private JLabel createMutedCenteredLabel(String text) {
        JLabel label = createMutedLabel(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JComponent createInlineBadge(String text, Color background, Color foreground) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(foreground);
        label.setOpaque(true);
        label.setBackground(background);
        label.setBorder(new EmptyBorder(5, 10, 5, 10));
        return label;
    }

    private JPanel createMiniMetric(String label, String value, Color accent) {
        JPanel panel = new SurfacePanel(new BorderLayout(0, 2));
        panel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel caption = new JLabel(label);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        caption.setForeground(Theme.MUTED_TEXT);

        JLabel metric = new JLabel(value);
        metric.setFont(new Font("Segoe UI", Font.BOLD, 16));
        metric.setForeground(accent);

        panel.add(caption, BorderLayout.NORTH);
        panel.add(metric, BorderLayout.CENTER);
        return panel;
    }

    private UserRank currentUserRank() {
        int currentUserId = UserProfileCache.getCurrentUserId();
        if (currentUserId <= 0) {
            return null;
        }
        for (UserRank rank : rankedUsers) {
            if (rank.userId == currentUserId) {
                return rank;
            }
        }
        return null;
    }

    private int currentUserPosition() {
        int currentUserId = UserProfileCache.getCurrentUserId();
        if (currentUserId <= 0) {
            return -1;
        }
        for (int i = 0; i < rankedUsers.size(); i++) {
            if (rankedUsers.get(i).userId == currentUserId) {
                return i + 1;
            }
        }
        return -1;
    }

    private UserRank normalizeRank(UserRank rank) {
        return new UserRank(
                rank.userId,
                safeName(rank.name),
                Math.max(0, rank.problemsSolved),
                Math.max(0, rank.aptitudeScore),
                Math.max(0, rank.mockInterviews),
                Math.max(0, rank.resumeScore)
        );
    }

    private static String safeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "TalentForge Learner";
        }
        return name.trim();
    }

    private static Color mix(Color a, Color b, float ratioFromA) {
        float ratioA = Math.min(1f, Math.max(0f, ratioFromA));
        float ratioB = 1f - ratioA;
        int red = Math.round(a.getRed() * ratioA + b.getRed() * ratioB);
        int green = Math.round(a.getGreen() * ratioA + b.getGreen() * ratioB);
        int blue = Math.round(a.getBlue() * ratioA + b.getBlue() * ratioB);
        return new Color(red, green, blue);
    }

    private void onThemeChanged() {
        String existingQuery = searchField.getText();
        buildUI();
        searchField.setText(existingQuery);
        refreshView();
        revalidate();
        repaint();
    }

    private static class SurfacePanel extends JPanel {
        SurfacePanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = Theme.PANEL_BG;
            Color border = Theme.isDark() ? new Color(61, 65, 82) : new Color(228, 232, 240);

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.setColor(border);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            g2.dispose();

            super.paintComponent(graphics);
        }
    }

    private static class GradientPanel extends JPanel {
        private final Color start;
        private final Color end;

        GradientPanel(LayoutManager layout, Color start, Color end) {
            super(layout);
            this.start = start;
            this.end = end;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
            g2.dispose();

            super.paintComponent(graphics);
        }
    }
}
