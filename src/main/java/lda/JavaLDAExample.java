package lda;

import scala.Function1;
import scala.Tuple1;
import scala.Tuple2;
import scala.collection.Seq;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.ListBuffer;
import util.Stopwords;
import scala.collection.immutable.Map;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.clustering.LDAModel;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import java.util.List;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.lang.time.StopWatch;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import scala.collection.JavaConverters;
import scala.collection.JavaConversions;

class Content implements Function<String, List<String>>, Serializable {

    public List<String> call(String content) throws Exception {
        String[] string_arrays = content.toLowerCase().split("\\s");
        return Arrays.asList(string_arrays);
    }
}

public class JavaLDAExample {

    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");

        SparkConf conf = new SparkConf().setAppName("LDA Example");
        conf.set("spark.app.name", "My Spark App");
        conf.set("spark.master", "local[2]");
        conf.set("spark.ui.port", "36000");
        JavaSparkContext sc = new JavaSparkContext(conf);
        // Load and parse the data
        //	    String path = "/home/thuy/sample.txt";
        String path = "src/main/resources/imdb_master.csv";
        SQLContext sqlContext = new SQLContext(sc);
        Dataset<Row> df = sqlContext.read().format("com.databricks.spark.csv").option("header", "true").load(path);
        JavaRDD<String> data = df.toJavaRDD().map(
                new Function<Row, String>() {
            @Override
            public String call(Row t) throws Exception {
                // TODO Auto-generated method stub
                return t.toString();
            }
        }
        );

        JavaRDD<List<String>> corpus = data.map(new Content());
        corpus.cache();
        ArrayList<List<String>> lists = new ArrayList<List<String>>();

        for (List<String> item : corpus.collect()) {
            if (item.size() > 3) {
                List<String> tmp = sc.parallelize(item).filter(new Function<String, Boolean>() {
                    @Override
                    public Boolean call(String s) throws Exception {
                        // TODO Auto-generated method stub
                        return s.length() > 3 && s.matches("[A-za-z]+")
                                && !Stopwords.isStemmedStopword(s) && !Stopwords.isStopword(s);
                    }
                }).collect();
                lists.add(tmp);
            }
        }

        JavaRDD<List<String>> corpuss = sc.parallelize(lists);
        List<Tuple2<String, Long>> termCounts = corpuss.flatMap(
                new FlatMapFunction<List<String>, String>() {
            public Iterator<String> call(List<String> list) {
                return list.iterator();
            }
        })
                .mapToPair(
                        new PairFunction<String, String, Long>() {
                    public Tuple2<String, Long> call(String s) {
                        return new Tuple2<String, Long>(s, 1L);
                    }
                })
                .reduceByKey(
                        new Function2<Long, Long, Long>() {
                    public Long call(Long i1, Long i2) {
                        return i1 + i2;
                    }
                })
                .collect();
        
        List<Tuple2<String, Long>> modifiableList = new ArrayList<Tuple2<String, Long>>(termCounts);

        Collections.sort(modifiableList, new Comparator<Tuple2<String, Long>>() {
            @Override
            public int compare(Tuple2<String, Long> v1, Tuple2<String, Long> v2) {
                // TODO Auto-generated method stub
                return (int) (v2._2 - v1._2);
            }
        });

        int numStopwords = termCounts.size() > 20 ? 20 : termCounts.size();
        List<String> vocabArray = new ArrayList<String>();
        for (int i = 0; i < numStopwords - 1; i++) {
            vocabArray.add(termCounts.get(i)._1);
        }

        final HashMap<String, Long> vocab = new HashMap<String, Long>();
        for (Tuple2<String, Long> item : sc.parallelize(vocabArray).zipWithIndex().collect()) {
            vocab.put(item._1, item._2);
        }

        JavaRDD<Tuple2<Long, Vector>> documents = corpuss.zipWithIndex().map(
                new Function<Tuple2<List<String>, Long>, Tuple2<Long, Vector>>() {
            @SuppressWarnings("unchecked")
            @Override
            public Tuple2<Long, Vector> call(Tuple2<List<String>, Long> t) throws Exception {
                HashMap<Long, Double> counts = new HashMap<Long, Double>(0);
                for (String item : t._1) {
                    if (vocab.containsKey(item)) {
                        Long idx = vocab.get(item);
                        if (!counts.containsKey(idx)) {
                            counts.put(idx, 0.0);
                        }
                        counts.put(idx, counts.get(idx) + 1.0);
                    }
                }

                //					JavaRDD<Vector<Tuple2<Integer,Double>>> parsedData 
                int[] key = new int[counts.size() + 1];
                double[] value = new double[counts.size() + 1];

                int i = 0;
                ArrayList<Tuple2<Integer, Double>> c = new ArrayList<>();
                for (Long item : counts.keySet()) {
                    //						c.add(new Tuple2(item.intValue(), counts.get(item)));
                    key[i] = item.intValue();
                    value[i] = counts.get(item);
                    i++;

                }
                return new Tuple2(t._2, Vectors.sparse(vocab.size(), key, value));
            }
        }
        );

//		try (Writer writer = new BufferedWriter(new OutputStreamWriter( new FileOutputStream("output.txt"), "utf-8"))) {
//			for( Tuple2<String,Long> t : termCounts){
//				writer.write(t._1 + " -> " + t._2);
//			}
//	
//		}
//		catch (Exception e) {
//			// TODO: handle exception
//		}
        JavaPairRDD<Long, Vector> cor = JavaPairRDD.fromJavaRDD(documents);

        int numTopics = 5;
        int maxIterations = 20;

        DistributedLDAModel ldaModel = (DistributedLDAModel) new LDA()
                .setK(numTopics)
                .setMaxIterations(maxIterations)
                .run(cor);
        double avgLogLikelihood = ldaModel.logLikelihood() / documents.count();

        int maxTermsPerTopic = 10;

        JavaRDD<Tuple2<Object, Vector>> topicdistributes = ldaModel.topicDistributions().toJavaRDD();
        Tuple2<int[], double[]>[] topicIndices = ldaModel.describeTopics(maxTermsPerTopic);

//		for(Tuple2<Object, Vector> item: topicdistributes.collect()){
//			System.out.println(item._2);
//		}
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "utf-8"))) {
            for (Tuple2<int[], double[]> topic : topicIndices) {
                writer.write("TOPIC:\n");
                int[] terms = topic._1;
                double[] termWeights = topic._2;
                for (int i = 0; i < terms.length; i++) {
                    writer.write(vocabArray.get(terms[i]) + ":\t" + termWeights[i] + "\n");
                }
                writer.write("\n");

            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }
}