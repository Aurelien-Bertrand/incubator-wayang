package org.apache.incubator.wayang.spark.operators;

import org.apache.spark.api.java.JavaRDD;
import org.apache.incubator.wayang.basic.operators.CollectionSource;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.plan.wayangplan.WayangPlan;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.core.util.WayangCollections;
import org.apache.incubator.wayang.core.util.Tuple;
import org.apache.incubator.wayang.java.channels.CollectionChannel;
import org.apache.incubator.wayang.java.platform.JavaPlatform;
import org.apache.incubator.wayang.spark.channels.RddChannel;
import org.apache.incubator.wayang.spark.execution.SparkExecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides a {@link Collection} to a Spark job. Can also be used to convert {@link CollectionChannel}s of the
 * {@link JavaPlatform} into {@link RddChannel}s.
 */
public class SparkCollectionSource<Type> extends CollectionSource<Type> implements SparkExecutionOperator {

    /**
     * Create a new instance to convert a {@link CollectionChannel} into a {@link RddChannel}.
     */
    public SparkCollectionSource(DataSetType<Type> type) {
        this(null, type);
    }

    /**
     * Create a new instance to use a {@code collection} in a {@link WayangPlan}.
     */
    public SparkCollectionSource(Collection<Type> collection, DataSetType<Type> type) {
        super(collection, type);
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public SparkCollectionSource(CollectionSource that) {
        super(that);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            SparkExecutor sparkExecutor,
            OptimizationContext.OperatorContext operatorContext) {
        assert inputs.length <= 1;
        assert outputs.length == this.getNumOutputs();

        final Collection<Type> collection;
        if (this.collection != null) {
            collection = this.collection;
        } else {
            final CollectionChannel.Instance input = (CollectionChannel.Instance) inputs[0];
            collection = input.provideCollection();
            assert collection != null : String.format("Instance of %s is not providing a collection.", input.getChannel());
        }
        final List<Type> list = WayangCollections.asList(collection);

        final RddChannel.Instance output = (RddChannel.Instance) outputs[0];
        final JavaRDD<Type> rdd = sparkExecutor.sc.parallelize(list, sparkExecutor.getNumDefaultPartitions());
        this.name(rdd);
        output.accept(rdd, sparkExecutor);

        return ExecutionOperator.modelLazyExecution(inputs, outputs, operatorContext);
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new SparkCollectionSource<>(this.getCollection(), this.getType());
    }

    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "wayang.spark.collectionsource.load";
    }

    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        assert index <= this.getNumInputs() || (index == 0 && this.getNumInputs() == 0);
        return Arrays.asList(CollectionChannel.DESCRIPTOR);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        assert index <= this.getNumOutputs() || (index == 0 && this.getNumOutputs() == 0);
        return Collections.singletonList(RddChannel.UNCACHED_DESCRIPTOR);
    }

    @Override
    public boolean containsAction() {
        return false;
    }

}
