package com.talentforge.datastructures;

import java.util.ArrayList;
import java.util.List;

/**
 * A Trie (prefix tree) for fast keyword lookup and prefix search.
 * Used by the Resume Checker to efficiently match resume text against
 * a large dictionary of skill/tool keywords — O(word length) per lookup
 * regardless of how many keywords are stored, and it naturally supports
 * "starts with" queries for partial matches.
 */
public class Trie {

    private static class Node {
        final java.util.Map<Character, Node> children = new java.util.HashMap<>();
        boolean isEndOfWord = false;
        String originalKeyword; // preserves original casing/formatting for display
    }

    private final Node root = new Node();
    private int wordCount = 0;

    /** Inserts a keyword (case-insensitive) into the trie. */
    public void insert(String keyword) {
        Node current = root;
        String normalized = keyword.toLowerCase().trim();
        for (char c : normalized.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new Node());
        }
        if (!current.isEndOfWord) {
            wordCount++;
        }
        current.isEndOfWord = true;
        current.originalKeyword = keyword;
    }

    /** Returns true if the exact keyword exists in the trie. */
    public boolean contains(String word) {
        Node node = findNode(word.toLowerCase().trim());
        return node != null && node.isEndOfWord;
    }

    /** Returns all stored keywords that start with the given prefix. */
    public List<String> keywordsWithPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        Node node = findNode(prefix.toLowerCase().trim());
        if (node != null) {
            collectWords(node, results);
        }
        return results;
    }

    public int size() {
        return wordCount;
    }

    private Node findNode(String s) {
        Node current = root;
        for (char c : s.toCharArray()) {
            current = current.children.get(c);
            if (current == null) return null;
        }
        return current;
    }

    private void collectWords(Node node, List<String> results) {
        if (node.isEndOfWord) {
            results.add(node.originalKeyword);
        }
        for (Node child : node.children.values()) {
            collectWords(child, results);
        }
    }
}