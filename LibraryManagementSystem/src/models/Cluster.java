package models;

import java.util.*;

public class Cluster {
    private int id;
    private List<Integer> bookIds;
    private Map<String, Double> centroid;

    public Cluster(int id) {
        this.id = id;
        this.bookIds = new ArrayList<>();
        this.centroid = new HashMap<>();
    }

    public void addDocument(int bookId) {
        this.bookIds.add(bookId);
    }

    public void clear() {
        this.bookIds.clear();
    }

    public int size() {
        return bookIds.size();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<Integer> bookIds) {
        this.bookIds = bookIds;
    }

    public Map<String, Double> getCentroid() {
        return centroid;
    }

    public void setCentroid(Map<String, Double> centroid) {
        this.centroid = centroid;
    }
}
