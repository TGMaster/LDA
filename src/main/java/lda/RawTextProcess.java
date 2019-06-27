/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda;

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
import java.util.List;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.feature.HashingTF;
import org.apache.spark.mllib.feature.IDF;
import org.apache.spark.mllib.feature.IDFModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import scala.Tuple2;
import util.Stopwords;

/**
 *
 * @author 9999
 */
class Content implements Function<String, List<String>>, Serializable {

    public List<String> call(String content) throws Exception {
        String[] string_arrays = content.toLowerCase().split("\\s");
        return Arrays.asList(string_arrays);
    }
}

public class RawTextProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.setProperty("hadoop.home.dir", "C:\\Spark\\");

        SparkConf conf = new SparkConf().setAppName("LDA Example");
        conf.set("spark.app.name", "My Spark App");
        conf.set("spark.master", "local[*]");
        conf.set("spark.executor.memory", "2g");
        conf.set("spark.ui.port", "36000");
        JavaSparkContext sc = new JavaSparkContext(conf);
        // Load and parse the data
        //	    String path = "/home/thuy/sample.txt";
        String path = "src/main/resources/data.csv";
        SQLContext sqlContext = new SQLContext(sc);
        Dataset<Row> dataSet = sqlContext.read().format("com.databricks.spark.csv").option("header", "true").load(path);
        dataSet = dataSet.select(
                dataSet.col("review")
        );

        JavaRDD<String> data = dataSet.toJavaRDD().map(
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

        ArrayList<List<String>> lists = new ArrayList<>();

        for (List<String> item : corpus.collect()) {
            if (item.size() > 3) {
                List<String> tmp = sc.parallelize(item).filter(new Function<String, Boolean>() {
                    @Override
                    public Boolean call(String s) throws Exception {
                        // TODO Auto-generated method stub
                        return s.length() > 3 && s.matches("[A-Za-z]+")
                                && !Stopwords.isStemmedStopword(s) && !Stopwords.isStopword(s);
                    }
                }).collect();
                lists.add(tmp);
            }
        }

        JavaRDD<List<String>> corpuss = sc.parallelize(lists);

        HashingTF hashTF = new HashingTF();
        JavaRDD<Vector> tf = hashTF.transform(corpuss);
        IDFModel idf = new IDF().fit(tf);
        JavaRDD<Vector> tfidf = idf.transform(tf);
        // Index documents with unique IDs
        JavaRDD<Tuple2<Long, Vector>> corpus2 = tfidf.zipWithIndex().map(
                new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
            public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
                return doc_id.swap();
            }
        }
        );
        
        corpus2.saveAsTextFile("src/main/resources/corpus2");

        List<Tuple2<String, Long>> termCounts = corpuss.flatMap(
                new FlatMapFunction<List<String>, String>() {
            @Override
            public Iterator<String> call(List<String> list) {
                return list.iterator();
            }
        })
                .mapToPair(
                        new PairFunction<String, String, Long>() {
                    @Override
                    public Tuple2<String, Long> call(String s) {
                        return new Tuple2<>(s, 1L);
                    }
                })
                .reduceByKey(
                        new Function2<Long, Long, Long>() {
                    @Override
                    public Long call(Long i1, Long i2) {
                        return i1 + i2;
                    }
                })
                .collect();

        List<Tuple2<String, Long>> modifiableList = new ArrayList<>(termCounts);

        // Sort the weight of words in descending order
        Collections.sort(modifiableList, new Comparator<Tuple2<String, Long>>() {
            @Override
            public int compare(Tuple2<String, Long> v1, Tuple2<String, Long> v2) {
                // TODO Auto-generated method stub
                return (int) (v2._2 - v1._2);
            }
        });

        int numStopwords = modifiableList.size();
        List<String> vocabArray = new ArrayList<>();
        for (int i = 0; i < numStopwords - 1; i++) {
            vocabArray.add(modifiableList.get(i)._1);
        }

        final HashMap<String, Long> dictionary = new HashMap<>();
        for (Tuple2<String, Long> item : sc.parallelize(vocabArray).zipWithIndex().collect()) {
            dictionary.put(item._1, item._2);
        }

        JavaRDD<Tuple2<Long, Vector>> documents = corpuss.zipWithIndex().map(
                new Function<Tuple2<List<String>, Long>, Tuple2<Long, Vector>>() {
            @SuppressWarnings("unchecked")
            @Override
            public Tuple2<Long, Vector> call(Tuple2<List<String>, Long> t) throws Exception {
                HashMap<Long, Double> counts = new HashMap<>();
                for (String item : t._1) {
                    if (dictionary.containsKey(item)) {
                        Long idx = dictionary.get(item);
                        if (!counts.containsKey(idx)) {
                            counts.put(idx, 0.0);
                        }
                        counts.put(idx, counts.get(idx) + 1.0);
                    }
                }

                //					JavaRDD<Vector<Tuple2<Integer,Double>>> parsedData 
                int[] key = new int[counts.size()];
                double[] value = new double[counts.size()];

                int i = 0;
//                ArrayList<Tuple2<Integer, Double>> c = new ArrayList<>();
                for (Long item : counts.keySet()) {
                    //						c.add(new Tuple2(item.intValue(), counts.get(item)));
                    key[i] = item.intValue();
                    value[i] = counts.get(item);
                    i++;

                }
                return new Tuple2(t._2, Vectors.sparse(dictionary.size(), key, value));
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
        // 80% Train, 20% Test
        JavaRDD<Tuple2<Long, Vector>>[] splits = corpus2.randomSplit(new double[]{0.8, 0.2});
        documents.saveAsTextFile("src/main/resources/corpus1");
        splits[0].saveAsObjectFile("src/main/resources/documents/train");
        splits[1].saveAsObjectFile("src/main/resources/documents/test");

        sc.stop();
    }

}
