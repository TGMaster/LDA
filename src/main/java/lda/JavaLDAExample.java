/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lda;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import scala.Tuple2;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.mllib.clustering.DistributedLDAModel;
import org.apache.spark.mllib.clustering.LDA;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.SparkConf;
import org.apache.spark.mllib.clustering.LocalLDAModel;

public class JavaLDAExample {

    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");

        SparkConf conf = new SparkConf().setAppName("LDA Example");
        conf.set("spark.app.name", "My Spark App");
        conf.set("spark.master", "local[*]");
        conf.set("spark.executor.memory", "2g");
        conf.set("spark.ui.port", "36000");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load and parse the data
        String path = "src/main/resources/sample_modified.txt";
        JavaRDD<String> data = sc.textFile(path);
        JavaRDD<Vector> parsedData = data.map(
                new Function<String, Vector>() {
            public Vector call(String s) {
                String[] sarray = s.trim().split(" ");
                int count = 0;
                for (int i = 0; i < sarray.length; i++) {
                    if (sarray[i].equals("0"))
                        count++;
                }
                int[] indices = new int[sarray.length-count];
                double[] values = new double[sarray.length-count];
                int k = 0;
                for (int i = 0; i < sarray.length; i++) {
                    if (!sarray[i].equals("0")) {
                        indices[k] = i;
                        values[k] = Double.parseDouble(sarray[i]);
                        k++;
                    }
                    
                }
                return Vectors.sparse(15, indices, values);
            }
        }
        );
        // Index documents with unique IDs
        JavaPairRDD<Long, Vector> corpus = JavaPairRDD.fromJavaRDD(parsedData.zipWithIndex().map(
                new Function<Tuple2<Vector, Long>, Tuple2<Long, Vector>>() {
            public Tuple2<Long, Vector> call(Tuple2<Vector, Long> doc_id) {
                return doc_id.swap();
            }
        }
        ));
        corpus.cache();
        corpus.saveAsTextFile("src/main/resources/example");

        // Cluster the documents into three topics using LDA
        DistributedLDAModel ldaModel = (DistributedLDAModel) new LDA().setK(3).run(corpus);
        LocalLDAModel localModel = ldaModel.toLocal();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output_train.txt"), "utf-8"))) {
            // Output topics. Each is a distribution over words (matching word count vectors)
            writer.write("Learned topics (as distributions over vocab of " + ldaModel.vocabSize() + " words):\n");
            Matrix topics = ldaModel.topicsMatrix();
            for (int topic = 0; topic < 3; topic++) {
                writer.write("Topic " + topic + ":\n");
                for (int word = 0; word < ldaModel.vocabSize(); word++) {
                    writer.write(" " + topics.apply(word, topic));
                }
                writer.write("\n");
            }
            double perplexity = localModel.logPerplexity(corpus);
            writer.write("Perplexity: " + perplexity);
        } catch (Exception e) {
        }
        sc.stop();
    }
}
