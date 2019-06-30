/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.clustering.LocalLDAModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.storage.StorageLevel;

/**
 *
 * @author S410U
 */
public class Test {

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

        // Hide spark logging
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Loads processed data.
        Dataset<Row> dataset = spark.read()
                .load("dataset");
        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, 1L);
        Dataset<Row> test = splits[1];
        
        // Store in Memory and disk
        test.persist(StorageLevel.MEMORY_AND_DISK());

        LocalLDAModel ldaModel = LocalLDAModel.load("LDAmodel");
        double ll = ldaModel.logLikelihood(test);
        double lp = ldaModel.logPerplexity(test);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(10);
        System.out.println("The topics described by their top-weighted terms:");
        topics.show(false);

        // Shows the result.
        Dataset<Row> transformed = ldaModel.transform(test);
        transformed.show(false);

        // Stop Spark Session
        spark.stop();
    }

}
