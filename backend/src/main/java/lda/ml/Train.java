/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import util.ToScala;

/**
 *
 * @author TienTran
 */
public class Train {

    public static final int K = 20;
    public static final long SEED = 1435876747;

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
        
        
        // Loads processed data.
        Dataset<Row> dataset = spark.read().load("dataset");

        // Index word
        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("reviews")
                .setOutputCol("vector")
                .setVocabSize(1500000) //Maximum size of vocabulary
                .setMinTF(2) //Minimum Term Frequency to be included in vocabulary
                .setMinDF(2) //Minumum number of document a term must appear
                .fit(dataset);
        dataset = vectorizer.transform(dataset);

        dataset.show(false);
        dataset.printSchema();

        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, SEED);
        Dataset<Row> train = splits[0];// Store in Memory and disk
        train.persist(StorageLevel.MEMORY_AND_DISK());

        // LDA Algorithms
        LDAModel ldaModel = new LDA()
                .setK(K)
                .setMaxIter(100)
                .setSeed(SEED)
                .setFeaturesCol("vector")
                .fit(train);

//        Pipeline pipeLine = new Pipeline().setStages(new PipelineStage[]{tokenizer, stopwordsRemover, vectorizer});
        try {
            ldaModel.write().overwrite().save("model");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        double ll = ldaModel.logLikelihood(train);
        double lp = ldaModel.logPerplexity(train);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(5);
        System.out.println("The topics described by their top-weighted terms:");
        topics.show(false);

        String[] vocabulary = vectorizer.vocabulary();
        StructType array = new StructType(new StructField[]{
            new StructField("terms", new ArrayType(DataTypes.StringType, true), false, Metadata.empty())
        });

        Encoder<Row> encoder = RowEncoder.apply(array);
        Dataset<String> topicsString = topics.select(topics.col("termIndices")).as(Encoders.STRING());

        Dataset<Row> result = topicsString.map((MapFunction<String, Row>) row -> {
            String[] topicIndices = row.split(",");
            LinkedList<String> rs = new LinkedList<>();
            for (String s : topicIndices) {
                s = s.replaceAll("[^0-9]+", "");
                rs.add(vocabulary[Integer.parseInt(s)]);
            }
            Row r = RowFactory.create(ToScala.toScalaList(rs));
            return r;
        }, encoder);

        result = result.join(topics);
        result.show(false);

//        result = result.join(topics, topics.col("termWeights"));
//        result.write().mode(SaveMode.Append).json("result");
        // Stop Spark Session
        spark.stop();
    }

}
