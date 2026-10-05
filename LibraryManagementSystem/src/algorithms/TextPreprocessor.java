package algorithms;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TextPreprocessor {
    private Set<String> stopWords;

    public TextPreprocessor() {
        this.stopWords = new HashSet<>();
        loadDefaultVietnameseStopWords();
    }

    private void loadDefaultVietnameseStopWords() {
        // Chỉ giữ stopwords thực sự chung chung
        String[] defaultStopWords = {
                "và", "của", "là", "các", "này", "đó", "do", "cũng"
        };
        stopWords.addAll(Arrays.asList(defaultStopWords));
    }

    public String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        text = text.toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFC);

        // Giữ lại ký tự tiếng Việt, Latin và số
        text = text.replaceAll("[^a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ0-9\\s]", " ");
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    public String[] tokenize(String text) {
        text = preprocess(text);
        return text.split("\\s+");
    }

    public String[] removeStopWords(String[] tokens) {
        return Arrays.stream(tokens)
                .filter(token -> !stopWords.contains(token) && token.length() > 1)
                .toArray(String[]::new);
    }

    public String[] processText(String text) {
        String[] tokens = tokenize(text);
        return removeStopWords(tokens);
    }

    // Tăng trọng số cho title và category
    public String[] extractKeywords(String title, String category, String description) {
        StringBuilder combined = new StringBuilder();

        // Title có trọng số cao nhất
        if (title != null && !title.isEmpty()) {
            for (int i = 0; i < 10; i++) {
                combined.append(title).append(" ");
            }
        }

        // Category trọng số cao thứ hai
        if (category != null && !category.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                combined.append(category).append(" ");
            }
        }

        // Description và content
        if (description != null && !description.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                combined.append(description).append(" ");
            }
        }

        return processText(combined.toString());
    }
}
