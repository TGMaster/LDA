/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.*;
import org.apache.spark.storage.StorageLevel;

/**
 *
 * @author TienTran
 */
public class Train_perplexity {

    public static final int K = 20;
    public static final long SEED = 1435876747;
    public static final double ALPHA[] = {0.05, 0.1, 0.5, 1, 5, 10};
    public static final double ETA[] = {0.05, 0.1, 0.5, 1, 5, 10};

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

        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, SEED);
        Dataset<Row> test = splits[1];

        // Index word
        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("words")
                .setOutputCol("vector")
                .setVocabSize(10000) //Maximum size of vocabulary
                .setMinDF(5) //Minumum number of document a term must appear
                .setMaxDF(0.6 * 1000)
                .fit(dataset);
        dataset = vectorizer.transform(dataset);

        vectorizer = new CountVectorizer()
                .setInputCol("words")
                .setOutputCol("vector")
                .setVocabSize(10000) //Maximum size of vocabulary
                .setMinDF(5) //Minumum number of document a term must appear
                .fit(test);
        test = vectorizer.transform(test);

        dataset.persist(StorageLevel.MEMORY_AND_DISK()); // Store in Memory and disk
        test.persist(StorageLevel.MEMORY_AND_DISK()); // Store in Memory and disk

        List<Double> perplexity = new ArrayList<>();

        for (int i = 0; i < ALPHA.length; i++) {
            for (int j = 0; j < ETA.length; j++) {
                // LDA Algorithms
                LDAModel ldaModel = new LDA()
                        .setK(K)
                        .setMaxIter(100)
                        .setFeaturesCol("vector")
                        .setDocConcentration(ALPHA[i])
                        .setTopicConcentration(ETA[j])
                        .setSeed(SEED)
                        .fit(dataset);

//                double ll = ldaModel.logLikelihood(test);
                double lp = ldaModel.logPerplexity(test);
//                System.out.println("The upper bound on perplexity: " + lp);
                perplexity.add(lp);
            }
        }
        // LDA Algorithms
        LDAModel ldaModel = new LDA()
                .setK(K)
                .setMaxIter(100)
                .setFeaturesCol("vector")
                .setDocConcentration(0.05)
                .setTopicConcentration(0.5)
                .setSeed(SEED)
                .fit(dataset);
        double lp = ldaModel.logPerplexity(test);
        System.out.println("The upper bound on perplexity: " + lp);
        // Stop Spark Session
        spark.stop();

        System.out.println("PERPLEXITY");
        for (Double t : perplexity) {
            System.out.println(t);
        }
    }

}
