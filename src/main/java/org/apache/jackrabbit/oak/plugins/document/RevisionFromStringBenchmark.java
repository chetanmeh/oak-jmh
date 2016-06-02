/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.jackrabbit.oak.plugins.document;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class RevisionFromStringBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        String revision = "r155104206fe-0-1";
    }

    @Benchmark
    public Revision revisionFromStringDefault(BenchmarkState state){
        return Revision.fromString(state.revision);
    }

    @Benchmark
    public Revision revisionFromStringNew(BenchmarkState state) {
        String rev = state.revision;
        return fromString(rev);
    }

    static Revision fromString(String rev) {
        boolean isBranch = rev.charAt(0) == 'b';
        int start = isBranch ? 2 : 1;
        long timestamp = 0;
        int clusterId = 0;
        int counter = 0;

        int idx = start;
        int digit;
        //Parse timestamp
        for (; idx < rev.length(); idx++) {
            char c = rev.charAt(idx);
            if (c == '-') {
                timestamp = -timestamp;
                break;
            }
            digit = c >= 'a'? c - 'a' + 10 : c - '0';
            timestamp = timestamp << 4;
            timestamp -= digit;
        }

        //Parse counter
        for (idx++; idx < rev.length(); idx++) {
            char c = rev.charAt(idx);
            if (c == '-') {
                counter = -counter;
                break;
            }
            digit = c >= 'a'? c - 'a' + 10 : c - '0';
            counter = counter << 4;
            counter -= digit;
        }

        //Parse clusterId
        for (idx++; idx < rev.length(); idx++) {
            char c = rev.charAt(idx);
            digit = c >= 'a'? c - 'a' + 10 : c - '0';
            clusterId = clusterId << 4;
            clusterId -= digit;
        }
        clusterId = -clusterId;
        return new Revision(timestamp, counter, clusterId, isBranch);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RevisionFromStringBenchmark.class.getName() + ".*")
                .warmupIterations(5)
                .measurementIterations(5)
                .threads(4)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(opt).run();
    }
}
