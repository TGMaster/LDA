/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.clustering.LocalLDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;
import scala.collection.mutable.WrappedArray;
import lda.util.ToScala;

import static org.apache.spark.sql.functions.*;

/**
 * @author TienTran
 */
public class Train {

    public static final long SEED = 1435876747;

    public static List<String> train(int K, double alpha, double beta) {
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

        // Loads processed data.
        Dataset<Row> dataset = spark.read().load("dataset");

        // Index word
        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("words")
                .setOutputCol("vector")
                .setVocabSize(1500000) //Maximum size of vocabulary
                .setMinDF(5) //Minumum number of document a term must appear
                .setMaxDF(0.6*1000)
                .fit(dataset);
        dataset = vectorizer.transform(dataset);

        try {
            vectorizer.write().overwrite().save("vectorizer");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        String[] vocabulary = vectorizer.vocabulary();

        // LDA Algorithms
        LDAModel ldaModel = new LDA()
                .setK(K)
                .setMaxIter(100)
                .setSeed(SEED)
                .setFeaturesCol("vector")
                .setDocConcentration(alpha)
                .setTopicConcentration(beta)
                .fit(dataset);

        try {
            ldaModel.write().overwrite().save("model");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        List<String> jsonArray = json(vocabulary,spark,ldaModel);
        // Stop Spark Session
        spark.stop();

        return jsonArray;
    }

    // Load LDA Model
    public static List<String> loadModel() {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");
        // Creates a SparkSession
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLDAExample")
                .config("spark.master", "local[*]")
                .config("spark.executor.memory", "4g")
                .getOrCreate();

        LocalLDAModel ldaModel = LocalLDAModel.load("model");
        CountVectorizerModel vectorizer = CountVectorizerModel.load("vectorizer");
        String[] vocabulary = vectorizer.vocabulary();

        List<String> jsonArray = json(vocabulary,spark,ldaModel);
        // Stop Spark Session
        spark.stop();

        return jsonArray;
    }

    public static List<String> json (String[] vocabulary, SparkSession spark, LDAModel ldaModel) {
        UDF1 index2String = (UDF1<WrappedArray<Integer>, List<String>>) data -> {
            List<Integer> temp = ToScala.toJavaListInt(data);
            List<String> array_string = new LinkedList<>();
            for (int i : temp) {
                array_string.add(vocabulary[i]);
            }
            return array_string;
        };

        UDF2 zipUDF = (UDF2<WrappedArray<String>, WrappedArray<Double>, List<Tuple2<String, Double>>>) (a, b) -> {
            List<Tuple2<String, Double>> list = new LinkedList<>();
            List<String> term = ToScala.toJavaListString(a);
            List<Double> proba = ToScala.toJavaListDouble(b);
            for (int i = 0; i < term.size(); i++) {
                Tuple2<String, Double> temp = new Tuple2(term.get(i), proba.get(i));
                list.add(temp);
            }
            return list;
        };

        StructType tuple = new StructType(new StructField[]{
                new StructField("term", DataTypes.StringType, false, Metadata.empty()),
                new StructField("probability", DataTypes.DoubleType, false, Metadata.empty())
        });
        spark.udf().register("index2String", index2String, DataTypes.createArrayType(DataTypes.StringType));
        spark.udf().register("zipUDF", zipUDF, DataTypes.createArrayType(tuple));

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(20).withColumn("terms", callUDF("index2String", col("termIndices")));
        System.out.println("The topics described by their top-weighted terms:");
        topics.select("topic", "terms", "termWeights").show(false);

        Dataset<Row> tempTopics = topics.withColumn("result", explode(callUDF("zipUDF", col("terms"), col("termWeights"))));
        Dataset<Row> result = tempTopics.select(col("topic").as("topicId"), col("result.term").as("text"), col("result.probability").as("probability"));

        List<String> jsonArray = result.toJSON().collectAsList();
        return jsonArray;
    }

}
