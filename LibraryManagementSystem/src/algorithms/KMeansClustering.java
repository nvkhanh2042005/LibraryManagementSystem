package algorithms;

import models.Cluster;
import models.Document;
import java.util.*;

public class KMeansClustering {
    private int k;
    private int maxIterations;
    private List<Document> documents;
    private List<Cluster> clusters;
    private Random random;

    public KMeansClustering(int k, int maxIterations) {
        this.k = k;
        this.maxIterations = maxIterations;
        this.clusters = new ArrayList<>();
        this.random = new Random(42); // Fixed seed for reproducibility
    }

    public List<Cluster> cluster(List<Document> documents) {
        this.documents = documents;

        if (documents.size() < k) {
            throw new IllegalArgumentException("Số lượng tài liệu phải >= k");
        }

        // Initialize clusters with K-Means++
        initializeClustersKMeansPlusPlus();

        int iteration = 0;
        boolean changed = true;

        while (iteration < maxIterations && changed) {
            // Clear all clusters
            for (Cluster cluster : clusters) {
                cluster.clear();
            }

            // Assign documents to nearest cluster
            assignDocumentsToClusters();

            // Update centroids and check if changed
            changed = updateCentroids();

            iteration++;
            System.out.println("Iteration " + iteration + " completed");

            // Print cluster distribution
            printClusterDistribution();
        }

        System.out.println("Clustering completed after " + iteration + " iterations");

        return clusters;
    }

    private void initializeClustersKMeansPlusPlus() {
        clusters.clear();

        for (int i = 0; i < k; i++) {
            clusters.add(new Cluster(i));
        }

        List<Integer> selectedIndices = new ArrayList<>();

        // Chọn centroid đầu tiên ngẫu nhiên
        int firstIndex = random.nextInt(documents.size());
        selectedIndices.add(firstIndex);
        clusters.get(0).setCentroid(new HashMap<>(documents.get(firstIndex).getTfidfVector()));

        // Chọn các centroid còn lại với K-Means++
        for (int i = 1; i < k; i++) {
            double[] distances = new double[documents.size()];
            double totalDistance = 0;

            for (int j = 0; j < documents.size(); j++) {
                if (selectedIndices.contains(j)) {
                    distances[j] = 0;
                    continue;
                }

                // Tìm khoảng cách nhỏ nhất đến các centroid đã chọn
                double minDistance = Double.MAX_VALUE;
                for (int selected : selectedIndices) {
                    double similarity = CosineSimilarity.calculate(
                            documents.get(j).getTfidfVector(),
                            documents.get(selected).getTfidfVector()
                    );
                    double distance = 1 - similarity;
                    minDistance = Math.min(minDistance, distance);
                }

                distances[j] = minDistance * minDistance; // Square for emphasis
                totalDistance += distances[j];
            }

            // Chọn document tiếp theo theo xác suất tỉ lệ với khoảng cách
            if (totalDistance > 0) {
                double randomValue = random.nextDouble() * totalDistance;
                double sum = 0;
                int nextIndex = 0;

                for (int j = 0; j < documents.size(); j++) {
                    sum += distances[j];
                    if (sum >= randomValue) {
                        nextIndex = j;
                        break;
                    }
                }

                selectedIndices.add(nextIndex);
                clusters.get(i).setCentroid(new HashMap<>(documents.get(nextIndex).getTfidfVector()));
            } else {
                // Fallback: chọn ngẫu nhiên
                int nextIndex;
                do {
                    nextIndex = random.nextInt(documents.size());
                } while (selectedIndices.contains(nextIndex));

                selectedIndices.add(nextIndex);
                clusters.get(i).setCentroid(new HashMap<>(documents.get(nextIndex).getTfidfVector()));
            }
        }

        System.out.println("K-Means++ initialization completed");
    }

    private void assignDocumentsToClusters() {
        for (Document doc : documents) {
            int bestCluster = 0;
            double maxSimilarity = -1;

            for (int i = 0; i < clusters.size(); i++) {
                Cluster cluster = clusters.get(i);
                double similarity = CosineSimilarity.calculate(
                        doc.getTfidfVector(),
                        cluster.getCentroid()
                );

                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    bestCluster = i;
                }
            }

            clusters.get(bestCluster).addDocument(doc.getBookId());
        }
    }

    private boolean updateCentroids() {
        boolean changed = false;

        for (Cluster cluster : clusters) {
            if (cluster.size() == 0) {
                // Cluster trống - giữ nguyên centroid cũ
                continue;
            }

            Map<String, Double> oldCentroid = new HashMap<>(cluster.getCentroid());
            Map<String, Double> newCentroid = calculateNewCentroid(cluster);

            cluster.setCentroid(newCentroid);

            // Check if centroid changed
            double similarity = CosineSimilarity.calculate(oldCentroid, newCentroid);
            if (similarity < 0.9999) { // Threshold for convergence
                changed = true;
            }
        }

        return changed;
    }

    private Map<String, Double> calculateNewCentroid(Cluster cluster) {
        Map<String, Double> newCentroid = new HashMap<>();
        int clusterSize = cluster.size();

        if (clusterSize == 0) {
            return newCentroid;
        }

        // Sum all vectors in cluster
        for (int bookId : cluster.getBookIds()) {
            Document doc = findDocumentByBookId(bookId);
            if (doc != null) {
                Map<String, Double> vector = doc.getTfidfVector();
                for (Map.Entry<String, Double> entry : vector.entrySet()) {
                    newCentroid.merge(entry.getKey(), entry.getValue(), Double::sum);
                }
            }
        }

        // Average
        for (String term : newCentroid.keySet()) {
            newCentroid.put(term, newCentroid.get(term) / clusterSize);
        }

        // Normalize
        double norm = Math.sqrt(newCentroid.values().stream()
                .mapToDouble(v -> v * v)
                .sum());

        if (norm > 0) {
            for (String term : newCentroid.keySet()) {
                newCentroid.put(term, newCentroid.get(term) / norm);
            }
        }

        return newCentroid;
    }

    private Document findDocumentByBookId(int bookId) {
        for (Document doc : documents) {
            if (doc.getBookId() == bookId) {
                return doc;
            }
        }
        return null;
    }

    private void printClusterDistribution() {
        System.out.println("Cluster distribution:");
        for (Cluster cluster : clusters) {
            System.out.println("  Cluster " + (cluster.getId() + 1) + ": " + cluster.size() + " documents");
        }
    }

    public List<Cluster> getClusters() {
        return clusters;
    }
}
