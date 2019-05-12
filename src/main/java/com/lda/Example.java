/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lda;

import com.lda.util.ReadFile;

import java.io.File;

import org.apache.spark.ml.clustering.LDA;
import org.apache.spark.ml.clustering.LDAModel;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Row;

/**
 *
 * @author TGMaster
 */
public class Example {

    private final static File folder = new File("src/main/resources/pos");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("hadoop.home.dir", "C:\\Spark\\");
        
        //ReadFile.numberFile = 10;
        // TODO code application logic here
        SparkSession spark = SparkSession
                .builder()
                .appName("JavaLDAExample")
                .master("local")
                .getOrCreate();

        Dataset<Row> rData = spark.createDataset(ReadFile.Dataset(folder), Encoders.STRING()).toDF();
        LDA lda = new LDA();
        lda.setK(10); //Set the number of topics to infer, i.e., the number of soft cluster centers.
        lda.setMaxIter(10); //Set the maximum number of iterations allowed.
        LDAModel ldaModel = lda.fit(rData);

        // Describe topics.
        Dataset<Row> topics = ldaModel.describeTopics(3); //Return the topics described by weighted terms
        System.out.println("The topics described by their top-weighted terms:");
        topics.show(false);
        
        spark.stop();
    }

}
