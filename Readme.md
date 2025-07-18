# 📝 Big Data Report: Xử Lý Dataset Titanic với Apache Spark & Hadoop
## 1. Hadoop
### Hadoop HDFS

- Hệ thống file phân tán.
- Lưu trữ dữ liệu dùng cho Apache Spark.

### Các thành phần hadoop
- NameNode: Quản lý metadata (thông tin về tập tin, thư mục, block).
- DataNode: Lưu trữ thực tế dữ liệu (block).
- SecondaryNameNode: Hỗ trợ backup metadata để giảm tải cho NameNode.
- ResourceManager (YARN): Quản lý tài nguyên và lịch trình tác vụ.
- NodeManager: Quản lý tài nguyên cụ thể trên từng máy worker.

## 2. Apache Spark

- Nền tảng xử lý dữ liệu lớn nhanh, hỗ trợ in-memory computation.
- Hỗ trợ RDD và DataFrame API.

### Spark vs Hadoop MapReduce
MapReduce (MR) :
- Input được đọc từ HDFS → xử lý → output ghi lại vào HDFS → lặp lại các bước này cho đến khi hoàn thành.
- Input được chia thành các block độc lập → các task Map/Reduce được thực hiện song song → phù hợp xử lý dữ liệu lớn.

Hạn chế của MapReduce:
- Mỗi bước xử lý đều ghi dữ liệu ra HDFS, gây tốn thời gian I/O do phụ thuộc vào disk.
- Gây ra vấn đề về lưu trữ, replication, và độ trễ trong xử lý.
- Code khó phát triển và debug do tính phức tạp, dài dòng.

Spark
- Sử dụng khái niệm RDD (Resilient Distributed Dataset):
    - Là tập dữ liệu bất biến, phân tán, lưu ở read-only memory, xử lý song song.
- Input chỉ cần load 1 lần từ storage, các bước xử lý được lập kế hoạch tối ưu và thực thi liên tục.
- Chạy chủ yếu trên RAM → tận dụng I/O tốc độ cao → khi thiếu RAM mới ghi ra disk.
- Hiệu suất cao hơn MapReduce từ 10–100 lần, do:
    - Giảm thao tác ghi đọc disk.
    - Tăng tốc độ xử lý nhờ thực hiện trên bộ nhớ.


### Định dạng file

- CSV: Text thuần, không hỗ trợ schema, không nén.
- Parquet: File theo cột, hỗ trợ schema, nén tốt.
- ORC: Cột, tối ưu cho Hive, nén tốt nhất.

## 4. Dữ liệu

- File: `titanic.csv`
- Tạo bộ dữ liệu 1GB+: để thực hành với Spark.

## 5. Cấu hình và chạy các thành phần hadoop

## 5. Upload dữ liệu lên HDFS
```bash
hdfs dfs -mkdir -p /input 
hdfs dfs -put converted-file.csv /input
```

## 6. Xử lý dữ liệu với Spark (Java)
### Khởi tạo SparkSession
```java
SparkSession spark = SparkSession.builder()
    .appName("SparkFirstProgram")
    .config("spark.master", "local[*]")
    .getOrCreate();
```
- Khởi tạo một phiên làm việc với Spark.
- `local[*]` cho phép sử dụng tất cả các core có sẵn trên máy để thực thi.

### Đọc file CSV từ HDFS
```java
Dataset<Row> dataset = spark.read()
    .option("header", "true")
    .option("inferSchema", "true")
    .option("nullValue", "null")
    .csv("hdfs://localhost:9000/input/converted-file.csv");
```
- Đọc tập tin CSV trên HDFS.

### Lấy dữ liệu phân tích tổng quát
```java
Dataset<Row> summary = dataset.describe();
summary.coalesce(1)
    .write()
    .option("header", "true")
    .csv("hdfs://localhost:9000/output/summary_1");
```
- `describe()` sinh ra thống kê tổng quát: count, mean, stddev, min, max cho mỗi cột.
- `coalesce(1)` gom tất cả dữ liệu về 1 file duy nhất.

### Phân tích theo giới tính & hạng vé
```java
Dataset<Row> summaryTable = dataset
    .groupBy("Sex", "Pclass")
    .agg(
        count("").alias("Total"),
        sum("Survived").alias("Survived"),
        expr("avg(Survived)  100").alias("SurvivalRate"),
        avg("Age").alias("AvgAge"),
        avg("Fare").alias("AvgFare")
    )
    .orderBy("Sex", "Pclass");
```
- Tính toán tổng số người, số người sống sót, tỷ lệ sống sót trung bình, tuổi và giá vé trung bình.
- Sắp xếp kết quả để dễ theo dõi.

### Ghi kết quả ra HDFS
```java
summaryTable.coalesce(1)
    .write()
    .option("header", "true")
    .csv("hdfs://localhost:9000/output/summary_2");
```
- Ghi kết quả phân tích xuống HDFS.
- Dữ liệu được lưu dưới dạng CSV, có header, gom thành 1 file duy nhất.

## 7. Biên dịch và chạy

```bash
mvn clean package 
spark-submit --class com.spark.Main target/spark-demo-1.0.jar
```

## 7. Output

## 8. Kết quả & So sánh


## 9. Kết luận


