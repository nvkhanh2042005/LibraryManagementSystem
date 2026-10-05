package algorithms;

import models.Cluster;
import models.Document;
import java.util.*;

public class ClusterEvaluator {

    // Tính Within-Cluster Sum of Squares (WCSS)
    public double calculateWCSS(List<Document> documents, List<Cluster> clusters) {
        double wcss = 0.0;

        for (Cluster cluster : clusters) {
            Map<String, Double> centroid = cluster.getCentroid();

            for (int bookId : cluster.getBookIds()) {
                Document doc = documents.stream()
                        .filter(d -> d.getBookId() == bookId)
                        .findFirst()
                        .orElse(null);

                if (doc != null) {
                    double distance = 1 - CosineSimilarity.calculate(
                            doc.getTfidfVector(), centroid);
                    wcss += distance * distance;
                }
            }
        }

        return wcss;
    }

    // Tính Silhouette Score
    public double calculateSilhouetteScore(List<Document> documents, List<Cluster> clusters) {
        if (clusters.size() < 2) return 0.0;

        double totalScore = 0.0;
        int count = 0;

        for (Document doc : documents) {
            double a = calculateAverageIntraClusterDistance(doc, documents, clusters);
            double b = calculateMinInterClusterDistance(doc, documents, clusters);

            double silhouette = (b - a) / Math.max(a, b);
            totalScore += silhouette;
            count++;
        }

        return count > 0 ? totalScore / count : 0.0;
    }

    private double calculateAverageIntraClusterDistance(Document doc,
                                                        List<Document> documents,
                                                        List<Cluster> clusters) {
        Cluster docCluster = findDocumentCluster(doc, clusters);
        if (docCluster == null || docCluster.size() <= 1) return 0.0;

        double totalDistance = 0.0;
        int count = 0;

        for (int bookId : docCluster.getBookIds()) {
            if (bookId != doc.getBookId()) {
                Document otherDoc = documents.stream()
                        .filter(d -> d.getBookId() == bookId)
                        .findFirst()
                        .orElse(null);

                if (otherDoc != null) {
                    double distance = 1 - CosineSimilarity.calculate(
                            doc.getTfidfVector(), otherDoc.getTfidfVector());
                    totalDistance += distance;
                    count++;
                }
            }
        }

        return count > 0 ? totalDistance / count : 0.0;
    }

    private double calculateMinInterClusterDistance(Document doc,
                                                    List<Document> documents,
                                                    List<Cluster> clusters) {
        Cluster docCluster = findDocumentCluster(doc, clusters);
        double minDistance = Double.MAX_VALUE;

        for (Cluster cluster : clusters) {
            if (cluster != docCluster) {
                double totalDistance = 0.0;
                int count = 0;

                for (int bookId : cluster.getBookIds()) {
                    Document otherDoc = documents.stream()
                            .filter(d -> d.getBookId() == bookId)
                            .findFirst()
                            .orElse(null);

                    if (otherDoc != null) {
                        double distance = 1 - CosineSimilarity.calculate(
                                doc.getTfidfVector(), otherDoc.getTfidfVector());
                        totalDistance += distance;
                        count++;
                    }
                }

                if (count > 0) {
                    double avgDistance = totalDistance / count;
                    minDistance = Math.min(minDistance, avgDistance);
                }
            }
        }

        return minDistance == Double.MAX_VALUE ? 0.0 : minDistance;
    }

    // Tính Davies-Bouldin Index (thấp hơn là tốt hơn)
    public double calculateDaviesBouldinIndex(List<Document> documents, List<Cluster> clusters) {
        if (clusters.size() < 2) return 0.0;

        double sumDB = 0.0;

        for (int i = 0; i < clusters.size(); i++) {
            double maxRatio = 0.0;

            for (int j = 0; j < clusters.size(); j++) {
                if (i != j) {
                    double si = calculateIntraClusterDistance(clusters.get(i), documents);
                    double sj = calculateIntraClusterDistance(clusters.get(j), documents);
                    double dij = calculateInterClusterDistance(
                            clusters.get(i), clusters.get(j), documents);

                    if (dij > 0) {
                        double ratio = (si + sj) / dij;
                        maxRatio = Math.max(maxRatio, ratio);
                    }
                }
            }

            sumDB += maxRatio;
        }

        return sumDB / clusters.size();
    }

    private double calculateIntraClusterDistance(Cluster cluster, List<Document> documents) {
        if (cluster.size() == 0) return 0.0;

        Map<String, Double> centroid = cluster.getCentroid();
        double totalDistance = 0.0;

        for (int bookId : cluster.getBookIds()) {
            Document doc = documents.stream()
                    .filter(d -> d.getBookId() == bookId)
                    .findFirst()
                    .orElse(null);

            if (doc != null) {
                double distance = 1 - CosineSimilarity.calculate(
                        doc.getTfidfVector(), centroid);
                totalDistance += distance;
            }
        }

        return totalDistance / cluster.size();
    }

    private double calculateInterClusterDistance(Cluster c1, Cluster c2,
                                                 List<Document> documents) {
        return 1 - CosineSimilarity.calculate(c1.getCentroid(), c2.getCentroid());
    }

    private Cluster findDocumentCluster(Document doc, List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            if (cluster.getBookIds().contains(doc.getBookId())) {
                return cluster;
            }
        }
        return null;
    }
}
