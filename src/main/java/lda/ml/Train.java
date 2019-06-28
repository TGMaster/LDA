/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.io.IOException;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

/**
 *
 * @author TienTran
 */
public class Train {
    
    public static final int NUM_OF_TOPICS = 3;
    public static final int MAX_ITERATIONS = 50;
    public static final long SEED = 1L;

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

        // Loads processed data.
        Dataset<Row> dataset = spark.read()
                .load("dataset");
        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, 1L);
        Dataset<Row> train = splits[0];
        
        // LDA Algorithms
        LDAModel ldaModel = new LDA()
                .setK(NUM_OF_TOPICS)
                .setMaxIter(MAX_ITERATIONS)
                .setSeed(SEED)
                .setFeaturesCol("vector")
                .fit(train);
        
        try {
            ldaModel.save("LDAmodel");
        } catch (IOException ex) {
            System.err.println(ex);
        }

        double ll = ldaModel.logLikelihood(train);
        double lp = ldaModel.logPerplexity(train);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(10);
        System.out.println("The topics described by their top-weighted terms:");
        topics.show(false);

        // Shows the result.
        Dataset<Row> transformed = ldaModel.transform(train);
        transformed.show(false);
        
        // Stop Spark Session
        spark.stop();
    }

}
