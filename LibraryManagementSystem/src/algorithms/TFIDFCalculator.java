package algorithms;

import models.Document;
import java.util.*;

public class TFIDFCalculator {
    private List<Document> documents;
    private Map<String, Integer> documentFrequency;
    private Set<String> vocabulary;

    public TFIDFCalculator() {
        this.documents = new ArrayList<>();
        this.documentFrequency = new HashMap<>();
        this.vocabulary = new HashSet<>();
    }

    public void addDocument(Document doc) {
        documents.add(doc);

        Set<String> uniqueTerms = new HashSet<>();
        for (String term : doc.getTerms()) {
            vocabulary.add(term);
            uniqueTerms.add(term);
        }

        for (String term : uniqueTerms) {
            documentFrequency.put(term, documentFrequency.getOrDefault(term, 0) + 1);
        }
    }

    public void calculateTFIDF() {
        int totalDocuments = documents.size();

        System.out.println("Total documents: " + totalDocuments);
        System.out.println("Vocabulary size: " + vocabulary.size());

        for (Document doc : documents) {
            Map<String, Double> tfidfVector = new HashMap<>();
            Map<String, Integer> termFreq = doc.getTermFrequency();
            int maxFreq = termFreq.values().stream().max(Integer::compareTo).orElse(1);

            for (String term : vocabulary) {
                int tf = termFreq.getOrDefault(term, 0);
                if (tf > 0) {
                    double tfValue = calculateTF(tf, maxFreq);
                    double idf = calculateIDF(documentFrequency.getOrDefault(term, 0), totalDocuments);
                    double tfidf = tfValue * idf;

                    if (tfidf > 0) {
                        tfidfVector.put(term, tfidf);
                    }
                }
            }

            normalizeVector(tfidfVector);
            doc.setTfidfVector(tfidfVector);
        }

        // Debug: In thông tin vector
        if (!documents.isEmpty()) {
            int totalTerms = 0;
            for (Document doc : documents) {
                totalTerms += doc.getTfidfVector().size();
            }
            System.out.println("Average terms per document: " + (totalTerms / documents.size()));
        }
    }

    private double calculateTF(int termFreq, int maxFreq) {
        if (maxFreq == 0) return 0;
        // Augmented TF to prevent bias
        return 0.5 + (0.5 * termFreq / maxFreq);
    }

    private double calculateIDF(int docFreq, int totalDocs) {
        if (docFreq == 0) return 0;
        // Smooth IDF
        return Math.log((double)(totalDocs + 1) / (docFreq + 1)) + 1;
    }

    private void normalizeVector(Map<String, Double> vector) {
        double norm = Math.sqrt(vector.values().stream()
                .mapToDouble(v -> v * v)
                .sum());

        if (norm > 0) {
            vector.replaceAll((k, v) -> v / norm);
        }
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public Set<String> getVocabulary() {
        return vocabulary;
    }

    public int getVocabularySize() {
        return vocabulary.size();
    }
}
