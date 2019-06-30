/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.io.Serializable;
import java.util.List;
import java.util.LinkedList;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import util.Stopwords;

/**
 *
 * @author S410U
 */
class Review implements Serializable {

    List<String> review;
}

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
                .config("spark.executor.memory", "8g")
                .getOrCreate();

        // Hide spark logging
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Loads raw data.
        Dataset<Row> dataset = spark.read()
                .format("csv")
                .option("header", "true")
                .load("src/main/resources/data.csv");
//        Dataset<Row> dataset = spark.read()
//                .json("src/main/resources/Books_5.json");

        // Store in Memory and disk
        dataset.persist(StorageLevel.MEMORY_AND_DISK());

        dataset.printSchema();

        // Creates a temporary view using the DataFrame
//        dataset.createOrReplaceTempView("bookReview");
        dataset.createOrReplaceTempView("movieReview");

        Dataset<Row> ds = spark.sql("SELECT * FROM movieReview WHERE review is NOT NULL AND review <> '' ");
//        Dataset<Row> reviewText = spark.sql("SELECT reviewText,summary FROM bookReview WHERE reviewText is NOT NULL AND reviewText <> ''");
//        reviewText.select(dataset.col("reviewText")).show(false);
//        reviewText.select(dataset.col("summary")).show(false);

//        Dataset<Row>[] listOfData = reviewText.randomSplit(new double[]{0.01,0.8}, 1L);
//        for (int i = 0; i < listOfData.length; i++) {
//            listOfData[i].select(dataset.col("reviewText")).show(false);
//            System.out.println("Size = " + listOfData[i].count());
//        }
//        System.out.println("Size = " + listOfData[0].count());


        // Tokenizer and Remove stop words
        LinkedList<Row> rows = new LinkedList<>();
        List<String> dataList = ds.select(dataset.col("review")).as(Encoders.STRING()).collectAsList();
        
        for (String t : dataList) {
            String[] temp = t.toLowerCase().split("\\s");
            LinkedList<String> filtered = new LinkedList<>();
            for (String s : temp) {
                if (s.length() >= 3 && s.matches("[A-Za-z]+")
                        && !Stopwords.isStemmedStopword(s) && !Stopwords.isStopword(s)) {
                    filtered.add(s);
                }
            }
            Row row = RowFactory.create(filtered);
            rows.add(row);
        }


        StructType schema = new StructType(new StructField[]{
            new StructField("reviews", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });

        Dataset<Row> newData = spark.createDataFrame(rows, schema);

        // Store in memory
        newData.cache();

        // Index word
        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("reviews")
                .setOutputCol("vector")
                .setVocabSize(200000) //Maximum size of vocabulary
                .setMinTF(1) //Minimum Term Frequency to be included in vocabulary
                .setMinDF(2) //Minumum number of document a term must appear
                .fit(newData);
        newData = vectorizer.transform(newData);

        newData.show(false);
        newData.printSchema();
        // Save dataset
        ds.write().save("dataset");
        spark.stop();
    }

}
