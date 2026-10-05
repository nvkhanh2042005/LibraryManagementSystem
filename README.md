# 📚 Library Management System

> **Hệ thống quản lý thư viện kết hợp phân cụm dữ liệu sách bằng thuật toán K-Means**

Đây là đồ án xây dựng **ứng dụng quản lý thư viện** bằng Java, cho phép quản lý thông tin sách, nhập dữ liệu từ CSV và hỗ trợ **phân nhóm sách tự động** dựa trên nội dung sử dụng các kỹ thuật xử lý văn bản và khai phá dữ liệu.

---

## ✨ Tính năng chính

### 📖 1. Quản lý sách

- Thêm sách mới
- Cập nhật thông tin sách
- Xóa sách
- Hiển thị danh sách sách
- Tìm kiếm và quản lý dữ liệu sách
- Lưu trữ dữ liệu bằng SQLite

### 📥 2. Import dữ liệu

Hỗ trợ nhập dữ liệu sách từ file CSV.

Dữ liệu mẫu được lưu tại:

```text
data/books_sample.csv
```

### 🤖 3. Phân cụm sách bằng K-Means

Hệ thống sử dụng thuật toán **K-Means Clustering** để tự động phân nhóm các cuốn sách dựa trên nội dung.

Quy trình xử lý:

```text
Thông tin sách
     ↓
Tiền xử lý văn bản
     ↓
TF-IDF
     ↓
Vector đặc trưng
     ↓
Cosine Similarity
     ↓
K-Means Clustering
     ↓
Các nhóm sách tương đồng
```

### 🧠 4. Xử lý ngôn ngữ

Hệ thống hỗ trợ các bước xử lý văn bản:

- Chuẩn hóa văn bản
- Loại bỏ Stop Words
- Tách và xử lý từ
- Tính toán TF-IDF
- Biểu diễn văn bản dưới dạng vector

Danh sách stop words tiếng Việt:

```text
data/vietnamese_stopwords.txt
```

### 📊 5. Đánh giá kết quả phân cụm

Project có module:

```text
ClusterEvaluator
```

để hỗ trợ đánh giá kết quả phân cụm.

---

## 🛠️ Công nghệ sử dụng

| Công nghệ | Mục đích |
|---|---|
| ☕ Java | Ngôn ngữ lập trình chính |
| 🖥️ Java Swing | Xây dựng giao diện Desktop |
| 🗄️ SQLite | Lưu trữ dữ liệu |
| 📊 K-Means | Phân cụm dữ liệu |
| 🔤 TF-IDF | Trích xuất đặc trưng văn bản |
| 📐 Cosine Similarity | Đo độ tương đồng giữa các văn bản |
| 📄 CSV | Nhập dữ liệu sách |
| 🧠 Text Preprocessing | Tiền xử lý dữ liệu văn bản |
| 💻 IntelliJ IDEA | Môi trường phát triển |

---

## 📂 Cấu trúc project

```text
LibraryManagementSystem/
│
├── data/
│   ├── books.db
│   ├── books_sample.csv
│   └── vietnamese_stopwords.txt
│
├── lib/
│   └── sqlite-jdbc-3.42.0.0.jar
│
├── src/
│   ├── algorithms/
│   │   ├── ClusterEvaluator.java
│   │   ├── CosineSimilarity.java
│   │   ├── KMeansClustering.java
│   │   ├── TextPreprocessor.java
│   │   └── TFIDFCalculator.java
│   │
│   ├── database/
│   │   └── DatabaseManager.java
│   │
│   ├── gui/
│   │   ├── BookManagementPanel.java
│   │   ├── ClusteringPanel.java
│   │   ├── ClusterResultPanel.java
│   │   └── MainFrame.java
│   │
│   ├── models/
│   │   ├── Book.java
│   │   ├── Cluster.java
│   │   └── Document.java
│   │
│   ├── utils/
│   │   └── CSVImporter.java
│   │
│   └── Main.java
│
├── .gitignore
└── LibraryManagementSystem.iml
```

---

## 🧩 Kiến trúc chương trình

Project được tổ chức thành các nhóm chức năng:

### `models`

Chứa các class mô hình dữ liệu:

```text
Book
Cluster
Document
```

### `database`

Quản lý kết nối và thao tác với SQLite:

```text
DatabaseManager
```

### `algorithms`

Chứa các thuật toán xử lý dữ liệu:

```text
TextPreprocessor
        ↓
TFIDFCalculator
        ↓
CosineSimilarity
        ↓
KMeansClustering
        ↓
ClusterEvaluator
```

### `gui`

Chứa giao diện người dùng:

```text
MainFrame
├── BookManagementPanel
├── ClusteringPanel
└── ClusterResultPanel
```

### `utils`

Các tiện ích hỗ trợ hệ thống:

```text
CSVImporter
```

---

## ⚙️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/YOUR_USERNAME/LibraryManagementSystem.git
```

Sau đó:

```bash
cd LibraryManagementSystem
```

### 2. Mở project

Mở project bằng **IntelliJ IDEA**.

Project sử dụng Java và thư viện SQLite JDBC được đặt trong:

```text
lib/sqlite-jdbc-3.42.0.0.jar
```

### 3. Kiểm tra thư viện

Đảm bảo file:

```text
sqlite-jdbc-3.42.0.0.jar
```

đã được thêm vào **Project Structure → Libraries**.

### 4. Chạy chương trình

Chạy class:

```text
src/Main.java
```

hoặc chạy:

```text
Main
```

---

## 🗄️ Cơ sở dữ liệu

Hệ thống sử dụng **SQLite**, database được lưu tại:

```text
data/books.db
```

Ưu điểm của SQLite trong project:

- Không cần cài đặt Database Server
- Dễ triển khai
- Dữ liệu nằm trong một file
- Phù hợp với ứng dụng Desktop
- Dễ dàng sao lưu và chia sẻ project

---

## 📊 Thuật toán phân cụm

### K-Means Clustering

K-Means được sử dụng để chia tập sách thành các nhóm dựa trên mức độ tương đồng về nội dung.

Các bước cơ bản:

```text
1. Chọn số lượng cụm K
        ↓
2. Khởi tạo các Centroid
        ↓
3. Tính khoảng cách
        ↓
4. Gán dữ liệu vào cụm gần nhất
        ↓
5. Cập nhật Centroid
        ↓
6. Lặp lại đến khi hội tụ
```

### TF-IDF

TF-IDF được sử dụng để chuyển nội dung sách thành vector đặc trưng.

```text
Text
 ↓
Tokenization
 ↓
Remove Stop Words
 ↓
TF-IDF
 ↓
Feature Vector
```

### Cosine Similarity

Cosine Similarity được sử dụng để xác định mức độ tương đồng giữa các vector văn bản.

Giá trị càng gần `1` thì hai văn bản càng tương đồng.

---

## 🖥️ Giao diện hệ thống

Hệ thống gồm các khu vực chính:

### Quản lý sách

Cho phép người dùng xem và thao tác với dữ liệu sách.

### Phân cụm dữ liệu

Cho phép lựa chọn số cụm và thực hiện thuật toán K-Means.

### Kết quả phân cụm

Hiển thị các nhóm sách sau khi thuật toán hoàn thành.

---

## 📌 Mục tiêu của đồ án

Project được xây dựng nhằm:

- Áp dụng kiến thức lập trình Java vào một ứng dụng thực tế.
- Xây dựng ứng dụng quản lý dữ liệu bằng giao diện Desktop.
- Làm việc với cơ sở dữ liệu SQLite.
- Thực hành xử lý dữ liệu văn bản.
- Áp dụng TF-IDF vào bài toán khai phá dữ liệu.
- Áp dụng Cosine Similarity để tính độ tương đồng.
- Áp dụng thuật toán K-Means vào bài toán phân cụm.
- Trực quan hóa kết quả phân nhóm sách.

---

## 🚀 Hướng phát triển

Trong tương lai, hệ thống có thể được mở rộng với:

- 🔍 Tìm kiếm sách thông minh.
- 🤖 Gợi ý sách dựa trên nội dung.
- 📊 Thêm biểu đồ trực quan kết quả phân cụm.
- 🔐 Hệ thống đăng nhập và phân quyền.
- 📚 Quản lý độc giả.
- 📅 Quản lý mượn/trả sách.
- ⭐ Xây dựng hệ thống đánh giá và đề xuất sách.
- 🧠 Kết hợp Machine Learning nâng cao.
- 🌐 Phát triển phiên bản Web hoặc REST API.

---

## 👨‍💻 Tác giả

**Sinh viên:** Nguyễn Văn Khánh

**Mục đích:** Đồ án học tập / Bài tập lớn

**Lĩnh vực:** Công nghệ thông tin – Quản lý dữ liệu & Khai phá dữ liệu

---

## 📄 License

Project được phát triển phục vụ mục đích **học tập và nghiên cứu**.

Bạn có thể sử dụng, chỉnh sửa và phát triển project cho mục đích học tập.
