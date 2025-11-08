package com.fakenews.nlp;

import com.fakenews.model.*;
import com.fakenews.datastructures.EvidenceKDTree;

import java.util.*;

/**
 * Retrieves semantically relevant evidence using a KD-tree (relevance, recency)
 * and re-ranks by semantic similarity from the NLP model.
 */
public class EvidenceRetriever {
    private final EvidenceKDTree index = new EvidenceKDTree();
    private final Map<String, Evidence> store = new HashMap<>();
    private final RoBERTaModel model;

    public EvidenceRetriever(RoBERTaModel model) { this.model = model; }

    public void indexEvidence(Evidence e) {
        if (e == null) return;
        store.put(e.getId(), e);
        index.insert(e.getId(), e.getRelevanceScore(), 1.0, e); // recency=1.0 (demo)
    }

    public List<Evidence> retrieveRelevantEvidence(Claim claim, int topK) {
        List<Evidence> out = new ArrayList<>();
        if (claim == null) return out;

        List<EvidenceKDTree.EvidenceResult> nn = index.kNearestNeighbors(0.5, 0.5, Math.max(2, topK * 2));
        List<Scored> scored = new ArrayList<>();

        for (EvidenceKDTree.EvidenceResult r : nn) {
            Evidence e = store.get(r.getEvidenceId());
            if (e != null) {
                double sim = model.computeSimilarity(claim.getText(), e.getText());
                scored.add(new Scored(e, sim));
            }
        }

        scored.sort((a,b) -> Double.compare(b.score, a.score));
        for (int i = 0; i < Math.min(topK, scored.size()); i++) out.add(scored.get(i).e);
        return out;
    }

    /** Returns support ratio in [0..1] weighted by relevance. */
    public double verifyClaimWithEvidence(Claim claim, List<Evidence> evidences) {
        if (evidences == null || evidences.isEmpty()) return 0.5;
        double support = 0.0, total = 0.0;
        for (Evidence e : evidences) {
            double w = Math.max(0.0, Math.min(1.0, e.getRelevanceScore()));
            if (e.isSupports()) support += w;
            total += w;
        }
        return total > 0 ? support / total : 0.5;
    }

    private static class Scored {
        final Evidence e; final double score;
        Scored(Evidence e, double score) { this.e = e; this.score = score; }
    }
}
