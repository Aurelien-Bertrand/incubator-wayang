/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wayang.apps.wordcount;

import org.apache.wayang.api.JavaPlanBuilder;
import org.apache.wayang.basic.data.Tuple2;
import org.apache.wayang.basic.operators.ParquetSource;
import org.apache.wayang.core.api.WayangContext;
import org.apache.wayang.java.Java;
import org.apache.wayang.spark.Spark;

import java.util.Arrays;
import java.util.Collection;

public class WordCountParquet {

    public static void main(String[] args){

        if (args.length == 0) {
            System.err.print("Usage: <input file URL>");
            System.exit(1);
        }

        WayangContext wayangContext = new WayangContext();
        for (String platform : args[0].split(",")) {
            switch (platform) {
                case "java":
                    wayangContext.register(Java.basicPlugin());
                    break;
                case "spark":
                    wayangContext.register(Spark.basicPlugin());
                    break;
                default:
                    System.err.format("Unknown platform: \"%s\"\n", platform);
                    System.exit(3);
                    return;
            }
        }

        /* Get a plan builder */
        JavaPlanBuilder planBuilder = new JavaPlanBuilder(wayangContext)
                .withJobName("WordCount")
                .withUdfJarOf(WordCountParquet.class);

        /* Start building the Apache WayangPlan */
        Collection<Tuple2<String, Integer>> wordcounts = planBuilder
                /* Read the text file */
                // .readParquet(new ParquetSource(args[1], new String[] { projectionColumns }, Arrays.copyOfRange(args, 2, args.length))) // In case of projection
                .readParquet(new ParquetSource(args[1], null, Arrays.copyOfRange(args, 2, args.length)))
                .withName("Load file")

                /* Split each line by non-word characters */
                .flatMap(record -> Arrays.asList(record.getString(0).split("\\W+")))
                .withSelectivity(1, 100, 0.9)
                .withName("Split words")

                /* Filter empty tokens */
                .filter(token -> !token.isEmpty())
                .withName("Filter empty words")

                /* Attach counter to each word */
                .map(word -> new Tuple2<>(word.toLowerCase(), 1)).withName("To lower case, add counter")

                /* Sum up counters for every word */
                .reduceByKey(
                        Tuple2::getField0,
                        (t1, t2) -> new Tuple2<>(t1.getField0(), t1.getField1() + t2.getField1())
                )
                .withName("Add counters")

                /* Execute the plan and collect the results */
                .collect();

        System.out.printf("Found %d words:\n", wordcounts.size());
        wordcounts.forEach(wc -> System.out.printf("%dx %s\n", wc.field1, wc.field0));
    }
}
