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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.*;
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

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class DataType {
    private String column;
    private String type;
}

public class Preprocess {


    // view schema
    public static List<DataType> preview(String filename) {

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
        Dataset<Row> raw = spark.read().json("D:\\dataset\\" + filename);

        // Store in Memory and disk
        raw.persist(StorageLevel.MEMORY_AND_DISK());

        List<StructField> schema = ToScala.toJavaListStructField(raw.schema().toSeq());
        List<DataType> listOfSchema = new ArrayList<>();
        spark.stop();

        for (int i = 0; i < schema.size(); i++) {
            DataType temp = new DataType(schema.get(i).name(),schema.get(i).dataType().typeName());
            listOfSchema.add(temp);
        }
        return listOfSchema;
    }

    // Pre-process a string
    public static Dataset<Row> preprocess(String string) {

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

        List<Row> list=new ArrayList<>();
        list.add(RowFactory.create(string));

        Dataset<Row> dataset = spark.createDataFrame(list, DataTypes.createStructType(
                new StructField[] {
                        DataTypes.createStructField("reviewText", DataTypes.StringType, false)
                }
        ));

        // Tokenizer
        dataset = tokenize(dataset, "reviewText");

        dataset = removeStopWord(dataset, "tokens", spark);
        return dataset;
    }

    // Pre-process a dataset
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
        Dataset<Row> ds = spark.read().json("D:\\dataset\\" + filename);

        // Store in Memory and disk
        ds.persist(StorageLevel.MEMORY_AND_DISK());

        ds = tokenize(ds, column);
        
        ds = removeStopWord(ds, "tokens", spark);

        Dataset<Row> demo = ds.select("reviewText", "words").limit(5);
        List<String> jsonArray = demo.toJSON().collectAsList();

        // Save dataset
        ds.write().mode(SaveMode.Overwrite).save("dataset");

        return jsonArray;
    }


    // Utils
    public static Dataset<Row> tokenize(Dataset<Row> dataset, String column) {
        // Tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol(column)
                .setOutputCol("tokens");
        dataset = tokenizer.transform(dataset);
        return dataset;
    }

    public static Dataset<Row> removeStopWord(Dataset<Row> dataset, String column, SparkSession spark) {
        String[] english = StopWordsRemover.loadDefaultStopWords("english");
        StopWordsRemover stopwordsRemover = new StopWordsRemover()
                .setStopWords(english)
                .setInputCol(column)
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

        Dataset<Row> result = dataset.withColumn("terms", callUDF("stopword", col("filtered")));
        result = result.withColumn("words", callUDF("lemma", col("terms")));
        return result;
    }

}
