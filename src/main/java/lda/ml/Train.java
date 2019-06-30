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
import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.ml.feature.IndexToString;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.storage.StorageLevel;
import scala.collection.mutable.Seq;
import scala.collection.mutable.WrappedArray;

/**
 *
 * @author TienTran
 */
public class Train {

    public static String[] read() {
        String[] x = null;
        try (BufferedReader br = Files.newBufferedReader(Paths.get("vocabulary.txt"))) {
            String line = br.readLine();
            x = new String[Integer.parseInt(line)];
            int i = 0;
            while ((line = br.readLine()) != null) {
                x[i] = line;
                i++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return x;
    }

    public static final int K = 20;
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
                .config("spark.executor.memory", "8g")
                .getOrCreate();

        // Hide spark logging
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Loads processed data.
        Dataset<Row> dataset = spark.read()
                .load("dataset");
        Dataset<Row>[] splits = dataset.randomSplit(new double[]{0.8, 0.2}, 1L);
        Dataset<Row> train = splits[0];

        // Store in Memory and disk
        train.persist(StorageLevel.MEMORY_AND_DISK());

        // LDA Algorithms
        LDAModel ldaModel = new LDA()
                .setK(K)
                .setSeed(SEED)
                .setFeaturesCol("vector")
                .fit(train);

//        try {
//            ldaModel.save("LDAmodel");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
        double ll = ldaModel.logLikelihood(train);
        double lp = ldaModel.logPerplexity(train);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(20);
        System.out.println("The topics described by their top-weighted terms:");
        topics.show(false);
        topics.printSchema();

        String[] vocabulary = read();
        IndexToString index2string = new IndexToString().setLabels(vocabulary).setInputCol("termIndices").setOutputCol("terms");
        index2string.transform(topics).show(false);
        
        UserDefinedFunction converter = functions.udf(new UDF1<WrappedArray<Integer>, String[]>() {
            @Override
            public String[] call(WrappedArray<Integer> a) {
                
                return null;
            }
        }, new ArrayType(DataTypes.StringType, false));
        UserDefinedFunction termsIdx2Str = functions.udf(
                (Seq<Integer>) termIndieces -> termIndices.map(idx -> vocabulary(idx))
        );
//        UserDefinedFunction = {
//            udf((indices: mutable.WrappedArray[Int]
//            ) => indices.map(values(_))
//        
//        
        // Stop Spark Session
        spark.stop();
    }

}
