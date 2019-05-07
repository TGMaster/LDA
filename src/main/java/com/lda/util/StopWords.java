/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lda.util;

//https://github.com/uttesh/exude
import com.uttesh.exude.ExudeData;
import com.uttesh.exude.exception.InvalidDataException;

/**
 *
 * @author TGMaster
 */
public class StopWords {

    public static String removeStopWords(String string) {
        String result = "";
        String[] words = string.split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (word.length() < 3) {
                continue; //remove stopwords
            }
            result += (word + " ");
        }
        try {
            result = ExudeData.getInstance().filterStoppingsKeepDuplicates(result);
        } catch (InvalidDataException e) {
            e.printStackTrace();
        }

        return result;
    }
}
