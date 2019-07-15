/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.clustering.LocalLDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.col;
import org.apache.spark.storage.StorageLevel;

/**
 *
 * @author S410U
 */
public class Test {

    static final int K = 20;
    static final long SEED = 1435876747;

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

        //        String[] vocabulary = vectorizer.vocabulary();
        //        write(vocabulary);
        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, SEED);
        Dataset<Row> test = splits[1];// Store in Memory and disk
        test.persist(StorageLevel.MEMORY_AND_DISK());

        LocalLDAModel ldaModel = LocalLDAModel.load("model");
        double ll = ldaModel.logLikelihood(test);
        double lp = ldaModel.logPerplexity(test);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

        // Describe topics.
//        Dataset<Row> topics = ldaModel.describeTopics();
//        System.out.println("The topics described by their top-weighted terms:");
//        
//        topics.show(false);

        // Shows the result.
        Dataset<Row> transformed = ldaModel.transform(test);
        transformed.select("topicDistribution").show(false);
        ldaModel.topicsMatrix();

        // Stop Spark Session
        spark.stop();
    }

}
