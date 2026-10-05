package algorithms;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CosineSimilarity {

    public static double calculate(Map<String, Double> vector1, Map<String, Double> vector2) {
        if (vector1 == null || vector2 == null || vector1.isEmpty() || vector2.isEmpty()) {
            return 0.0;
        }

        Set<String> commonTerms = new HashSet<>(vector1.keySet());
        commonTerms.retainAll(vector2.keySet());

        if (commonTerms.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;

        for (String term : commonTerms) {
            dotProduct += vector1.get(term) * vector2.get(term);
        }

        // Vectors are already normalized in TF-IDF calculation
        // So cosine similarity = dot product

        return Math.max(0.0, Math.min(1.0, dotProduct)); // Clamp to [0, 1]
    }
}
