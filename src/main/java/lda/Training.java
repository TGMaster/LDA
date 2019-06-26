package lda;

import scala.Tuple2;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.linalg.Vector;

import java.util.List;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.clustering.LocalLDAModel;
import org.apache.spark.mllib.clustering.OnlineLDAOptimizer;
import org.apache.spark.storage.StorageLevel;

public class Training {

    public static void main(String[] args) {
        int numTopics = 100;
        int maxIterations = 50;
        int maxTermsPerTopic = 20;
        boolean isTest = false;

        System.setProperty("hadoop.home.dir", "C:\\Spark\\");

        SparkConf conf = new SparkConf().setAppName("LDA Example");
        conf.set("spark.app.name", "My Spark App");
        conf.set("spark.master", "local[*]");
        conf.set("spark.executor.memory", "2g");
        conf.set("spark.ui.port", "36000");
        JavaSparkContext sc = new JavaSparkContext(conf);
        
        JavaRDD<Tuple2<Long, Vector>> documents = sc.objectFile("src/main/resources/documents/train");
        JavaPairRDD<Long, Vector> cor = JavaPairRDD.fromJavaRDD(documents);
        cor.persist(StorageLevel.MEMORY_AND_DISK());

        //LDA
//        OnlineLDAOptimizer optimizer = new OnlineLDAOptimizer().setMiniBatchFraction(2.0/maxIterations);
        LDAModel ldaModel = new LDA()
                .setK(numTopics)
                .setMaxIterations(maxIterations)
//                .setAlpha(50/numTopics) //50/numTopics
//                .setBeta(0.1) //0.1.
//                .setTopicConcentration(-1)
//                .setDocConcentration(-1)
                .setSeed(1L)
//                .setOptimizer(optimizer)
                .run(cor);

        ldaModel.save(sc.sc(), "src/main/resources/models");
        DistributedLDAModel distLDA = (DistributedLDAModel) ldaModel;
        
        double avgLogLikelihood = distLDA.logLikelihood() / documents.count();

        JavaRDD<Tuple2<Object, Vector>> topicdistributes = distLDA.topicDistributions().toJavaRDD();
        Tuple2<int[], double[]>[] topicIndices = ldaModel.describeTopics(maxTermsPerTopic);


//		for(Tuple2<Object, Vector> item: topicdistributes.collect()){
//			System.out.println(item._2);
//		}
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output_train.txt"), "utf-8"))) {
            int count = 0;
            writer.write("Vocabsize: " + distLDA.vocabSize() + "\n");
            writer.write("Log likelihood: " + avgLogLikelihood + "\n");
            for (Tuple2<int[], double[]> topic : topicIndices) {
                count++;
                writer.write("TOPIC " + count + ":\n");
                int[] terms = topic._1;
                double[] termWeights = topic._2;
                for (int i = 0; i < terms.length; i++) {
                    writer.write(terms[i] + ":\t" + termWeights[i] + "\n");
                }
                writer.write("\n");

            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
}
