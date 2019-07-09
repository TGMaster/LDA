/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;


import java.util.LinkedList;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import lda.util.Stopwords;
import lda.util.ToScala;

/**
 *
 * @author S410U
 */
public class CSVProcess {

    public static void preprocess(String filename) {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");
        // Creates a SparkSession
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLDAExample")
                .config("spark.master", "local[*]")
                .config("spark.executor.memory", "4g")
                .getOrCreate();

        // Hide spark logging
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Loads raw data.

        Dataset<Row> csv = spark.read()
                .format("csv")
                .option("header", "true")
                .load("src/main/resources/" + filename);

        // Store in Memory and disk
        csv.persist(StorageLevel.MEMORY_AND_DISK());
        csv = csv.filter(csv.col("review").isNotNull());

        // Tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("review")
                .setOutputCol("tokens");
        csv = tokenizer.transform(csv);
        
        String[] english = StopWordsRemover.loadDefaultStopWords("english");
        StopWordsRemover stopwordsRemover = new StopWordsRemover()
                .setStopWords(english)
                .setInputCol("tokens")
                .setOutputCol("filtered");
        csv = stopwordsRemover.transform(csv);
        csv.select(csv.col("filtered")).show();
        
        StructType array = new StructType(new StructField[]{
            new StructField("reviews", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });
        
        Encoder<Row> encoder = RowEncoder.apply(array);
        
        Dataset<String> stringData = csv.select(csv.col("filtered")).as(Encoders.STRING());
        Dataset<Row> newData = stringData.map((MapFunction<String, Row>) row -> {
            String[] temp = row.split(",");
            LinkedList<String> filtered = new LinkedList<>();
            for (String s : temp) {
                if (s.length() >= 3) {
                    s = s.replaceAll("[^A-Za-z]+", "");
                    if (!Stopwords.isStemmedStopword(s) && !Stopwords.isStopword(s)) {
                        filtered.add(s);
                    }
                }
            }
            Row result = RowFactory.create(ToScala.toScalaList(filtered));
            return result;
        }, encoder);

        // Save dataset
        newData.write().mode(SaveMode.Overwrite).save("dataset");
        spark.stop();
    }

}
