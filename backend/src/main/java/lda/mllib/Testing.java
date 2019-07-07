/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.mllib;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LocalLDAModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.storage.StorageLevel;
import scala.Tuple2;

/**
 *
 * @author 9999
 */
public class Testing {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int maxTermsPerTopic = 10;

        // TODO code application logic here
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");
        SparkConf conf = new SparkConf().setAppName("LDA Example");
        conf.set("spark.app.name", "My Spark App");
        conf.set("spark.master", "local[*]");
        conf.set("spark.executor.memory", "2g");
        conf.set("spark.ui.port", "36000");
        JavaSparkContext sc = new JavaSparkContext(conf);

        DistributedLDAModel loadModel = DistributedLDAModel.load(sc.sc(), "src/main/resources/models");
        LocalLDAModel ldaModel = loadModel.toLocal();

        JavaRDD<Tuple2<Long, Vector>> documents = sc.objectFile("src/main/resources/documents/test");
        JavaPairRDD<Long, Vector> cor = JavaPairRDD.fromJavaRDD(documents);
        cor.persist(StorageLevel.MEMORY_AND_DISK());

        JavaPairRDD<Long, Vector> Predict = ldaModel.topicDistributions(cor);
        for (Tuple2<Long, Vector> item : Predict.collect()) {
            String vector = "";
            for (int i = 0; i < item._2.size(); i++)
                vector = vector + item._2.apply(i) + " ";
            System.out.println(item._1 + ":\t" + vector + "\n");
        }

        double avgLogLikelihood = ldaModel.logLikelihood(cor);
        double perplexity = ldaModel.logPerplexity(cor);
        System.out.println("likelihood: " + avgLogLikelihood);
        System.out.println("perplexity: " + perplexity);
    }

}
