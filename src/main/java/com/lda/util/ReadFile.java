/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lda.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author TGMaster
 */
public class ReadFile {
    
    public static int numberFile = 1000;

    public static List<String> Dataset(final File folder) {
        String[] dataset = new String[numberFile];
        int i = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                Dataset(fileEntry);
            } else {
                //System.out.println(fileEntry.getName());
                try {
                    Scanner sc = new Scanner(fileEntry);

                    while (sc.hasNextLine()) {
                        String t = sc.nextLine();
                        t = StopWords.removeStopWords(t);
                        //System.out.println(t);
                        if (i >= numberFile) {
                            break;
                        }
                        dataset[i] = t;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                i++;
            }
        }
        List<String> result = Arrays.asList(dataset);
        return result;
    }
    
    
}
