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
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.*;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;
import scala.collection.mutable.WrappedArray;
import util.ToScala;

import static org.apache.spark.sql.functions.*;

/**
 *
 * @author TienTran
 */
public class Train {

    public static final int K = 5;
    public static final long SEED = 1435876747;
    public static final double ALPHA[] = {0.05,0.1,0.5,1,5,10};
    public static final double ETA[] = {0.05,0.1,0.5,1,5,10};
    

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

        // Loads processed data.
        Dataset<Row> dataset = spark.read().load("dataset");

        // Index word
        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("words")
                .setOutputCol("vector")
                .setVocabSize(10000) //Maximum size of vocabulary
                .setMinTF(1) //Minimum Term Frequency to be included in vocabulary
                .setMinDF(2) //Minumum number of document a term must appear
                .fit(dataset);
        dataset = vectorizer.transform(dataset);
        String[] vocabulary = vectorizer.vocabulary();

//        dataset.show(false);
//        dataset.printSchema();

        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, SEED);
        Dataset<Row> train = splits[0];
        Dataset<Row> test = splits[1];
        train.persist(StorageLevel.MEMORY_AND_DISK()); // Store in Memory and disk

        // LDA Algorithms
        LDAModel ldaModel = new LDA()
                .setK(K)
                .setMaxIter(100)
                .setFeaturesCol("vector")
                .setDocConcentration(ALPHA[0])
                .setTopicConcentration(ETA[3])
                .fit(train);

        try {
            ldaModel.write().overwrite().save("model");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        double ll = ldaModel.logLikelihood(test);
        double lp = ldaModel.logPerplexity(test);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

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
        Dataset<Row> topics = ldaModel.describeTopics().withColumn("terms", callUDF("index2String", col("termIndices")));
        System.out.println("The topics described by their top-weighted terms:");
        topics.select("topic", "terms", "termWeights").show(false);

        Dataset<Row> tempTopics = topics.withColumn("result", explode(callUDF("zipUDF", col("terms"), col("termWeights"))));
        Dataset<Row> result = tempTopics.select(col("topic").as("topicId"), col("result.term").as("term"), col("result.probability").as("probability"));
        
//        result.show(false);

//        
        // Stop Spark Session
        spark.stop();
    }

}
