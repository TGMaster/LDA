/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import edu.washington.cs.knowitall.morpha.MorphaStemmer;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.clustering.LocalLDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.explode;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.storage.StorageLevel;
import scala.collection.mutable.WrappedArray;
import util.Stopwords;
import util.ToScala;

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
        Dataset<String> test_file = spark.read().textFile("src/main/resources/test_file.txt");
        Dataset<Row> ds = test_file.toDF();

        // Tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("value")
                .setOutputCol("tokens");
        Dataset<Row> dataset = tokenizer.transform(ds);

        String[] english = StopWordsRemover.loadDefaultStopWords("english");
        StopWordsRemover stopwordsRemover = new StopWordsRemover()
                .setStopWords(english)
                .setInputCol("tokens")
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

        Dataset<Row> finalDataset = dataset.withColumn("terms", callUDF("stopword", col("filtered")));
        finalDataset = finalDataset.withColumn("words", callUDF("lemma", col("terms")));
        finalDataset.select("words").show(false);

        // Index word
        CountVectorizerModel vectorizer = new CountVectorizer()
                .setInputCol("words")
                .setOutputCol("vector")
                .setVocabSize(10000) //Maximum size of vocabulary
                .fit(finalDataset);
        Dataset<Row> test = vectorizer.transform(finalDataset);

        test.persist(StorageLevel.MEMORY_AND_DISK());

        LocalLDAModel ldaModel = LocalLDAModel.load("model");
        System.out.println("Seed: " + ldaModel.getSeed());
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
        transformed.printSchema();
        transformed.select("topicDistribution").show(false);

        // Stop Spark Session
        spark.stop();
    }

}
