package org.apache.incubator.wayang.spark.operators;

import org.apache.commons.lang3.Validate;
import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.optimizer.cardinality.CardinalityEstimator;
import org.apache.incubator.wayang.core.optimizer.cardinality.DefaultCardinalityEstimator;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.plan.wayangplan.UnaryToUnaryOperator;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.core.util.Tuple;
import org.apache.incubator.wayang.java.channels.CollectionChannel;
import org.apache.incubator.wayang.java.platform.JavaPlatform;
import org.apache.incubator.wayang.spark.channels.RddChannel;
import org.apache.incubator.wayang.spark.execution.SparkExecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Converts a {@link RddChannel} into a {@link CollectionChannel} of the {@link JavaPlatform}.
 */
public class SparkCollectOperator<Type>
        extends UnaryToUnaryOperator<Type, Type>
        implements SparkExecutionOperator {

    public SparkCollectOperator(DataSetType<Type> type) {
        super(type, type, false);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            SparkExecutor sparkExecutor,
            OptimizationContext.OperatorContext operatorContext) {
        RddChannel.Instance input = (RddChannel.Instance) inputs[0];
        CollectionChannel.Instance output = (CollectionChannel.Instance) outputs[0];

        @SuppressWarnings("unchecked")
        final List<Type> collectedRdd = (List<Type>) input.provideRdd().collect();
        output.accept(collectedRdd);

        return ExecutionOperator.modelEagerExecution(inputs, outputs, operatorContext);
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        return Arrays.asList(RddChannel.UNCACHED_DESCRIPTOR, RddChannel.CACHED_DESCRIPTOR);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        return Collections.singletonList(CollectionChannel.DESCRIPTOR);
    }

    @Override
    public boolean containsAction() {
        return true;
    }

    @Override
    public Optional<CardinalityEstimator> createCardinalityEstimator(
            final int outputIndex,
            final Configuration configuration) {
        Validate.inclusiveBetween(0, 0, outputIndex);
        return Optional.of(new DefaultCardinalityEstimator(1d, 1, this.isSupportingBroadcastInputs(),
                inputCards -> inputCards[0]));
    }

    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "wayang.spark.collect.load";
    }

}
