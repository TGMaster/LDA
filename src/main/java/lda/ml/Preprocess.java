/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import util.Stopwords;
import util.ToScala;

/**
 *
 * @author S410U
 */
public class Preprocess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");
        // Creates a SparkSession
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLDAExample")
                .config("spark.master", "local[*]")
                .config("spark.executor.memory", "16g")
                .getOrCreate();

        // Hide spark logging
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Loads raw data.
        Dataset<Row> raw = spark.read().json("src/main/resources/Book2.json");
        // Store in Memory and disk
        raw.persist(StorageLevel.MEMORY_AND_DISK());
        raw = raw.filter(raw.col("reviewText").isNotNull());
        
        Dataset<Row> ds = raw.limit(10000);

//        // Tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("reviewText")
                .setOutputCol("tokens");
        ds = tokenizer.transform(ds);
        
        ds.show(false);
        
        String[] english = StopWordsRemover.loadDefaultStopWords("english");
        StopWordsRemover stopwordsRemover = new StopWordsRemover()
                .setStopWords(english)
                .setInputCol("tokens")
                .setOutputCol("filtered");
        ds = stopwordsRemover.transform(ds);
        ds.select(ds.col("filtered")).show();
        
        StructType array = new StructType(new StructField[]{
            new StructField("reviews", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });
        
        Encoder<Row> encoder = RowEncoder.apply(array);
        
        Dataset<String> stringData = ds.select(ds.col("filtered")).as(Encoders.STRING());
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
        newData.write().save("dataset");
        spark.stop();
    }

}
