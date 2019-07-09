/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lda.util;

import java.util.HashMap;

import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.collection.mutable.WrappedArray;

/**
 *
 * @author 9999
 */
public class ToScala {

    public static <A, B> Map<A, B> toScalaMap(HashMap<A, B> m) {
        return JavaConverters.mapAsScalaMapConverter(m).asScala().toMap(
                Predef.<Tuple2<A, B>>conforms()
        );
    }
    
    public static List<String> toScalaList(java.util.List<String> list) {
        return JavaConverters.asScalaBufferConverter(list).asScala().toList();
    }

    public static java.util.List<Integer> toJavaListInt(WrappedArray<Integer> data) {
        return JavaConverters.seqAsJavaListConverter(data).asJava();
    }

    public static java.util.List<String> toJavaListString(WrappedArray<String> data) {
        return JavaConverters.seqAsJavaListConverter(data).asJava();
    }

    public static java.util.List<Double> toJavaListDouble(WrappedArray<Double> data) {
        return JavaConverters.seqAsJavaListConverter(data).asJava();
    }
}
