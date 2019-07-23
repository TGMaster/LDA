/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import lda.util.Stopwords;
import lda.util.ToScala;
import scala.collection.mutable.WrappedArray;

import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.col;

/**
 *
 * @author S410U
 */
public class Preprocess {

    public static List<String> preview(String filename) {
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
        Dataset<Row> raw = spark.read().json("D:\\" + filename);

        // Store in Memory and disk
        raw.persist(StorageLevel.MEMORY_AND_DISK());

        List<StructField> schema = ToScala.toJavaListStructField(raw.schema().toSeq());
        List<String> result = new ArrayList<>();
        spark.stop();

        for (int i = 0; i < schema.size(); i++) {
            result.add(schema.get(i).name() + " [" + schema.get(i).dataType().typeName() + "]");
        }
        return result;
    }

    public static List<String> preprocess(String filename, String column) {
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
        Dataset<Row> ds = spark.read().json("D:\\" + filename);

        // Store in Memory and disk
        ds.persist(StorageLevel.MEMORY_AND_DISK());

//        // Tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol(column)
                .setOutputCol("tokens");
        ds = tokenizer.transform(ds);
        
        String[] english = StopWordsRemover.loadDefaultStopWords("english");
        StopWordsRemover stopwordsRemover = new StopWordsRemover()
                .setStopWords(english)
                .setInputCol("tokens")
                .setOutputCol("filtered");
        ds = stopwordsRemover.transform(ds);

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

        Dataset<Row> dataset = ds.withColumn("terms", callUDF("stopword", col("filtered")));
        dataset = dataset.withColumn("words", callUDF("lemma", col("terms")));

        Dataset<Row> demo = dataset.limit(10);
        List<String> jsonArray = demo.toJSON().collectAsList();

        // Save dataset
        dataset.write().mode(SaveMode.Overwrite).save("dataset");
        spark.stop();

        return jsonArray;
    }

}
