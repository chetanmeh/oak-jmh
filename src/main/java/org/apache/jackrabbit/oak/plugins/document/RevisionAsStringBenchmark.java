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
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class RevisionAsStringBenchmark {
    /**
     * All possible chars for representing a number as a String
     */
    final static char[] digits = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
            'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
            'o' , 'p' , 'q' , 'r' , 's' , 't' ,
            'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        Revision revision = Revision.fromString("r155104206fe-0-1");
    }

    @Benchmark
    public String revisionAsStringDefault(BenchmarkState state){
        return state.revision.toString();
    }

    @Benchmark
    public String revisionAsStringNew(BenchmarkState state) {
        return toString(state.revision);
    }

    static String toString(Revision rev) {
        long timestamp = rev.getTimestamp();
        int counter = rev.getCounter();
        int clusterId = rev.getClusterId();
        StringBuilder sb = new StringBuilder(20);
        if (rev.isBranch()){
            sb.append('b');
        }
        sb.append('r');
        toHexString(sb, timestamp);
        sb.append('-');


        if (counter < 10) {
            sb.append(counter);
        } else {
            toHexString(sb, counter);
        }
        sb.append('-');

        if (clusterId < 10) {
            sb.append(clusterId);
        } else {
            toHexString(sb, clusterId);
        }
        return sb.toString();
    }

    /**
     * Convert the integer to an unsigned number.
     */
    private static void toHexString(StringBuilder sb, long i) {
        int shift = 4;
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        long mask = radix - 1;
        do {
            buf[--charPos] = digits[(int)(i & mask)];
            i >>>= shift;
        } while (i != 0);
        sb.append(buf, charPos, (64 - charPos));
    }

    private static void toHexString(StringBuilder sb, int i) {
        char[] buf = new char[32];
        int shift = 4;
        int charPos = 32;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[i & mask];
            i >>>= shift;
        } while (i != 0);
        sb.append(buf, charPos, (32 - charPos));
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
