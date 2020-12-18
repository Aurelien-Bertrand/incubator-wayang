package org.apache.incubator.wayang.profiler.spark;

import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.profiler.util.ProfilingUtils;
import org.apache.incubator.wayang.spark.channels.RddChannel;
import org.apache.incubator.wayang.spark.operators.SparkExecutionOperator;

import java.util.function.Supplier;

/**
 * {@link SparkOperatorProfiler} implementation for {@link SparkExecutionOperator}s with one input and one output.
 */
public abstract class SparkSourceProfiler extends SparkOperatorProfiler {

    public SparkSourceProfiler(Supplier<SparkExecutionOperator> operatorGenerator,
                               Configuration configuration,
                               Supplier<?> dataQuantumGenerator) {
        super(operatorGenerator, configuration, dataQuantumGenerator);
    }

    @Override
    protected Result executeOperator() {
        final RddChannel.Instance outputChannelInstance = createChannelInstance(this.sparkExecutor);

        // Let the operator execute.
        ProfilingUtils.sleep(this.executionPaddingTime); // Pad measurement with some idle time.
        final long startTime = System.currentTimeMillis();
        this.evaluate(
                this.operator,
                new ChannelInstance[]{},
                new ChannelInstance[]{outputChannelInstance}
        );

        // Force the execution of the operator.
        outputChannelInstance.provideRdd().foreach(dataQuantum -> {
        });
        final long endTime = System.currentTimeMillis();
        ProfilingUtils.sleep(this.executionPaddingTime); // Pad measurement with some idle time.

        // Yet another run to count the output cardinality.
        final long outputCardinality = outputChannelInstance.provideRdd().count();

        // Gather and assemble all result metrics.
        return new Result(
                this.inputCardinalities,
                outputCardinality,
                endTime - startTime,
                this.provideDiskBytes(startTime, endTime),
                this.provideNetworkBytes(startTime, endTime),
                this.provideCpuCycles(startTime, endTime),
                this.numMachines,
                this.numCoresPerMachine
        );
    }
}
