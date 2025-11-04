package com.fakenews.nlp;

import com.fakenews.model.Claim;
import com.fakenews.model.Article;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts claim-like sentences via regex + simple factual indicators. */
public class ClaimExtractor {
    private static final Pattern CLAIM_PATTERN = Pattern.compile(
            "([A-Z][^.!?]*(?:said|claims|reports|states|announced|revealed|according to)[^.!?]*[.!?])"
    );

    public List<Claim> extractClaims(Article article) {
        List<Claim> out = new ArrayList<>();
        String content = article.getContent() == null ? "" : article.getContent();

        Matcher m = CLAIM_PATTERN.matcher(content);
        int id = 0;
        while (m.find()) {
            String text = m.group(1).trim();
            if (text.length() > 20) {
                out.add(new Claim(article.getId() + "_claim_" + (id++), text, article.getId()));
            }
        }

        // Additional factual statements (numbers, %s, names)
        for (String s : extractKeyStatements(content)) {
            out.add(new Claim(article.getId() + "_claim_" + (id++), s, article.getId()));
        }
        return out;
    }

    private List<String> extractKeyStatements(String content) {
        List<String> list = new ArrayList<>();
        String[] sentences = content.split("[.!?]+");
        for (String s : sentences) {
            String t = s.trim();
            if (t.length() <= 30) continue;
            if (containsFactualIndicators(t)) list.add(t);
        }
        return list;
    }

    private boolean containsFactualIndicators(String s) {
        return s.matches(".*\\b\\d{4}\\b.*") ||
                s.matches(".*\\b\\d+%.*") ||
                s.matches(".*\\b\\d+\\s*(million|billion|thousand)\\b.*") ||
                s.matches(".*\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b.*");
    }
}
