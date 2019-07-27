/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.ml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.spark.ml.clustering.LocalLDAModel;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author S410U
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
class Topic {
    private Integer topic;
    private Double probability;
}

public class Test {

    public static List<Topic> Search(String input) {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");
        // Creates a SparkSession
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLDAExample")
                .config("spark.master", "local[*]")
                .config("spark.executor.memory", "4g")
                .getOrCreate();

        Dataset<Row> dataset = Preprocess.preprocess(input);

        // Index word
        CountVectorizerModel vectorizer = CountVectorizerModel.load("vectorizer");
        dataset = vectorizer.transform(dataset);

        final long SEED = 1435876747;

        LocalLDAModel ldaModel = LocalLDAModel.load("model");
        double ll = ldaModel.logLikelihood(dataset);
        double lp = ldaModel.logPerplexity(dataset);
        System.out.println("The lower bound on the log likelihood of the entire corpus: " + ll);
        System.out.println("The upper bound on perplexity: " + lp);

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(20);
        System.out.println("The topics described by their top-weighted terms:");
        topics.show(false);

        // Shows the result.
        Dataset<Row> transformed = ldaModel.transform(dataset);
        List<String> json = transformed.select("topicDistribution").toJSON().collectAsList();
        List<Topic> result = new ArrayList<>();

        for (String t : json) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(t);
            } catch (JSONException err) {
                err.printStackTrace();
            }
            JSONObject value = jsonObject.getJSONObject("topicDistribution");
            JSONArray arrJson = value.getJSONArray("values");
            Double[] dArr = new Double[arrJson.length()];
            for (int i = 0; i < arrJson.length(); i++) {
                dArr[i] = arrJson.getDouble(i);
                Topic topic = new Topic(i, dArr[i] * 100.0);
                result.add(topic);
            }
        }
        List<String> x = new ArrayList<>();
        JSONArray ja = new JSONArray(result);
        for (int i = 0; i < ja.length(); i++) {
            x.add(ja.get(i).toString());
        }
        try {
            Files.write(Paths.get("output.json"), x, Charset.defaultCharset());
        } catch (IOException ex) {
        }
        return result;
    }

}
