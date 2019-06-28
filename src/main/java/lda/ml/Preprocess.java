/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.*;
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
                .config("spark.executor.memory", "4g")
                .getOrCreate();

        // Loads raw data.
        Dataset<Row> dataset = spark.read()
                .format("csv")
                .option("header", "true")
                .load("src/main/resources/data.csv");

        List<String> dataList = dataset.select(dataset.col("review")).as(Encoders.STRING()).collectAsList();
        List<List<String>> lists = new ArrayList<>();
        for (String t : dataList) {
            String[] temp = t.toLowerCase().split("\\s");
            lists.add(Arrays.asList(temp));
        }

        List<Row> rows = new ArrayList<>();
        for (List<String> item : lists) {
            if (item.size() >= 3) {
                List<String> temp = new ArrayList<>();
                for (String s : item) {
                    if (s.length() >= 3 && s.matches("[A-Za-z]+")
                            && !Stopwords.isStemmedStopword(s) && !Stopwords.isStopword(s)) {
                        temp.add(s);
                    }
                }
                Row row = RowFactory.create(temp);
                rows.add(row);
            }
        }

        StructType schema = new StructType(new StructField[]{
            new StructField("reviews", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });

        Dataset<Row> newData = spark.createDataFrame(rows, schema);

//        JavaRDD<Row> a = newData.toJavaRDD();
//        a.saveAsTextFile("src/main/resources/test");

        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("reviews")
                .setOutputCol("vector")
                .setVocabSize(200000) //Maximum size of vocabulary
                .setMinTF(1) //Minimum Term Frequency to be included in vocabulary
                .setMinDF(2) //Minumum number of document a term must appear
                .fit(newData);
        newData = vectorizer.transform(newData);
        
        // Save dataset
        newData.write().json("dataset");
        spark.stop();
    }

}
