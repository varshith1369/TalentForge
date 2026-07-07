package com.talentforge.datastructures;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the categorized keyword dictionary the Resume Checker scans for,
 * and builds a Trie from it for fast lookup.
 */
public class ResumeKeywordBank {

    private final Map<String, String[]> categorizedKeywords = new LinkedHashMap<>();
    private final Trie trie = new Trie();

    public ResumeKeywordBank() {
        categorizedKeywords.put("Programming Languages", new String[]{
                "Java", "Python", "JavaScript", "C++", "C", "SQL", "Kotlin", "TypeScript", "Dart"
        });
        categorizedKeywords.put("Frameworks & Libraries", new String[]{
                "Spring", "React", "Node.js", "Flutter", "JDBC", "Swing", "Django", "Flask", "Firebase"
        });
        categorizedKeywords.put("Databases & Tools", new String[]{
                "SQLite", "MySQL", "MongoDB", "Git", "GitHub", "Docker", "Maven", "Postman"
        });
        categorizedKeywords.put("Data Structures & Concepts", new String[]{
                "Trie", "Graph", "HashMap", "Queue", "Stack", "LinkedList", "Recursion",
                "Algorithm", "OOP", "REST API"
        });
        categorizedKeywords.put("Soft Skills", new String[]{
                "Leadership", "Teamwork", "Communication", "Problem-solving", "Collaboration"
        });

        for (String[] keywords : categorizedKeywords.values()) {
            for (String keyword : keywords) {
                trie.insert(keyword);
            }
        }
    }

    public Map<String, String[]> getCategorizedKeywords() {
        return categorizedKeywords;
    }

    public Trie getTrie() {
        return trie;
    }
}