package org.apache.incubator.wayang.spark.operators;

import org.apache.spark.api.java.JavaRDD;
import org.apache.incubator.wayang.basic.operators.DistinctOperator;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.core.util.Tuple;
import org.apache.incubator.wayang.spark.channels.RddChannel;
import org.apache.incubator.wayang.spark.execution.SparkExecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Spark implementation of the {@link DistinctOperator}.
 */
public class SparkDistinctOperator<Type>
        extends DistinctOperator<Type>
        implements SparkExecutionOperator {


    /**
     * Creates a new instance.
     *
     * @param type type of the dataset elements
     */
    public SparkDistinctOperator(DataSetType<Type> type) {
        super(type);
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public SparkDistinctOperator(DistinctOperator<Type> that) {
        super(that);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            SparkExecutor sparkExecutor,
            OptimizationContext.OperatorContext operatorContext) {
        assert inputs.length == this.getNumInputs();
        assert outputs.length == this.getNumOutputs();

        final RddChannel.Instance input = (RddChannel.Instance) inputs[0];
        final RddChannel.Instance output = (RddChannel.Instance) outputs[0];

        final JavaRDD<Type> inputRdd = input.provideRdd();
        final JavaRDD<Type> outputRdd = inputRdd.distinct(sparkExecutor.getNumDefaultPartitions());
        this.name(outputRdd);
        output.accept(outputRdd, sparkExecutor);

        return ExecutionOperator.modelLazyExecution(inputs, outputs, operatorContext);
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new SparkDistinctOperator<>(this);
    }

    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "wayang.spark.distinct.load";
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        return Arrays.asList(RddChannel.UNCACHED_DESCRIPTOR, RddChannel.CACHED_DESCRIPTOR);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        return Collections.singletonList(RddChannel.UNCACHED_DESCRIPTOR);
    }

    @Override
    public boolean containsAction() {
        return false;
    }

}
