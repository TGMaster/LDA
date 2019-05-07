/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lda;

import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.lda.Doc;
import com.github.chen0040.lda.Lda;
import com.github.chen0040.lda.LdaResult;

import com.lda.util.ReadFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author S410U
 */
public class Test {
    
    private final static File folder = new File("src/main/resources/pos");

    public static void main(String[] args) {
//        String[] abc = listFilesForFolder(folder);
//        System.out.println("Line: " + abc.length);
//        for (String t : abc) {
//            System.out.println(t);
//        }

        List<String> docs = ReadFile.Dataset(folder);

        Lda method = new Lda();
        method.setTopicCount(20);
        method.setMaxVocabularySize(200000);
        //method.setStemmerEnabled(true);
        //method.setRemoveNumber(true);
        //method.setRemoveXmlTag(true);
        //method.addStopWords(Arrays.asList(stopwords));

        LdaResult result = method.fit(docs);

        System.out.println("Topic Count: " + result.topicCount());
        int topicCount = result.topicCount();

        for (int topicIndex = 0; topicIndex < topicCount; ++topicIndex) {
            String topicSummary = result.topicSummary(topicIndex);
            List<TupleTwo<String, Integer>> topKeyWords = result.topKeyWords(topicIndex, 10);
            List<TupleTwo<Doc, Double>> topStrings = result.topDocuments(topicIndex, 5);

            System.out.println("Topic #" + (topicIndex + 1) + ": " + topicSummary);

            for (TupleTwo<String, Integer> entry : topKeyWords) {
                String keyword = entry._1();
                int score = entry._2();
                System.out.println("Keyword: " + keyword + "(" + score + ")");
            }

            for (TupleTwo<Doc, Double> entry : topStrings) {
                double score = entry._2();
                int docIndex = entry._1().getDocIndex();
                String docContent = entry._1().getContent();
                System.out.println("Doc (" + docIndex + ", " + score + ")): " + docContent);
            }
        }
    }
}
