/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.col;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import scala.collection.mutable.WrappedArray;
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
        Dataset<Row> raw = spark.read().json("D:\\Tien\\Dataset\\Book2.json");
        // Store in Memory and disk
        raw.persist(StorageLevel.MEMORY_AND_DISK());
        raw = raw.filter(raw.col("reviewText").isNotNull());

        raw.printSchema();
        
        Dataset<Row> ds = raw.limit(1000);
        ds.write().mode(SaveMode.Append).json("sample");

        // Tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("reviewText")
                .setOutputCol("tokens");
        Dataset<Row> dataset = tokenizer.transform(ds);

        String[] english = StopWordsRemover.loadDefaultStopWords("english");
        StopWordsRemover stopwordsRemover = new StopWordsRemover()
                .setStopWords(english)
                .setInputCol("tokens")
                .setOutputCol("filtered");
        dataset = stopwordsRemover.transform(dataset);
        
        UDF1 stopword = (UDF1<WrappedArray<String>, List<String>>) data -> {
            List<String> temp = ToScala.toJavaListString(data);
            List<String> array_string = new LinkedList<>();
            for (String s : temp) {
                if (s.length() >= 3) {
                    s = s.replaceAll("[^A-Za-z]+", "");
                    if (!Stopwords.isStemmedStopword(s) && !Stopwords.isStopword(s)) {
                         array_string.add(s);
                    }
                }
            }
            return array_string;
        };

        UDF1 lemma = (UDF1<WrappedArray<String>, List<String>>) data -> {
            List<String> temp = ToScala.toJavaListString(data);
            List<String> array_string = new LinkedList<>();
            for (String i : temp) {
                array_string.add(MorphaStemmer.morpha(i, false));
            }
            return array_string;
        };
        
        spark.udf().register("lemma", lemma, DataTypes.createArrayType(DataTypes.StringType));
        spark.udf().register("stopword", stopword, DataTypes.createArrayType(DataTypes.StringType));
        
        Dataset<Row> finalDataset = dataset.withColumn("terms", callUDF("stopword", col("filtered")));
        finalDataset = finalDataset.withColumn("words", callUDF("lemma", col("terms")));
        finalDataset.printSchema();
        finalDataset.select("words").show(false);

        // Save dataset
        finalDataset.write().mode(SaveMode.Overwrite).save("dataset");

        spark.stop();
    }

}
