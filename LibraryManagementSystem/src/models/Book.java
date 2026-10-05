package models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private String description;
    private String content;
    private int year;
    private int clusterId;

    public Book() {
        this.clusterId = -1;
    }

    public Book(int id, String title, String author, String category,
                String description, String content, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
        this.content = content;
        this.year = year;
        this.clusterId = -1;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public int getYear() { return year; }
    public int getClusterId() { return clusterId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setContent(String content) { this.content = content; }
    public void setYear(int year) { this.year = year; }
    public void setClusterId(int clusterId) { this.clusterId = clusterId; }

    // Kết hợp nội dung để phân cụm
    public String getFullText() {
        StringBuilder sb = new StringBuilder();
        if (title != null) sb.append(title).append(" ");
        if (description != null) sb.append(description).append(" ");
        if (content != null) sb.append(content);
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s (%d)", id, title, author, year);
    }
}
