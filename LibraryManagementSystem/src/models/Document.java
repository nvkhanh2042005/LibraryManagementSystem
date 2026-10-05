package models;

import java.util.HashMap;
import java.util.Map;

public class Document {
    private int bookId;
    private String text;
    private String[] terms;
    private Map<String, Integer> termFrequency;
    private Map<String, Double> tfidfVector;

    public Document(int bookId, String text, String[] terms) {
        this.bookId = bookId;
        this.text = text;
        this.terms = terms;
        this.termFrequency = new HashMap<>();
        this.tfidfVector = new HashMap<>();
        calculateTermFrequency();
    }

    private void calculateTermFrequency() {
        for (String term : terms) {
            termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
        }
    }

    public int getBookId() { return bookId; }
    public String getText() { return text; }
    public String[] getTerms() { return terms; }
    public Map<String, Integer> getTermFrequency() { return termFrequency; }
    public Map<String, Double> getTfidfVector() { return tfidfVector; }

    public void setTfidfVector(Map<String, Double> tfidfVector) {
        this.tfidfVector = tfidfVector;
    }

    public int getTermCount() {
        return terms.length;
    }
}
