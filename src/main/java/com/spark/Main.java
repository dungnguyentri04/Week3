package com.spark;

import org.apache.hadoop.tracing.SpanContext;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.classic.Dataset;
import org.apache.spark.sql.classic.SparkSession;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.spark.SparkContext.getOrCreate;
import static org.apache.spark.sql.functions.*;

public class Main {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("SparkFirstProgram")
                .config("spark.master", "local[*]")
                .getOrCreate();
        try {
            Dataset<Row> dataset = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .option("nullValue", "null")
                    .csv("hdfs://localhost:9000/input/converted-file.csv");
            Dataset<Row> summary = dataset.describe();
            summary.coalesce(1)
                    .write()
                    .option("header", "true")
                    .csv("hdfs://localhost:9000/output/summary_1");
            Dataset<Row> summaryTable = dataset
                    .groupBy("Sex", "Pclass")
                    .agg(
                            count("*").alias("Total"),
                            sum("Survived").alias("Survived"),
                            expr("avg(Survived) * 100").alias("SurvivalRate"),
                            avg("Age").alias("AvgAge"),
                            avg("Fare").alias("AvgFare")
                    )
                    .orderBy("Sex", "Pclass");

            // Ghi ra file CSV trong HDFS
            summaryTable.coalesce(1) // chỉ tạo 1 file CSV
                    .write()
                    .option("header", "true")
                    .csv("hdfs://localhost:9000/output/summary_2");
        }
        catch (Exception e) {
            System.err.println("Lỗi khi xử lý dữ liệu: " + e.getMessage());
        }
        finally {
            spark.stop();
        }

    }
}