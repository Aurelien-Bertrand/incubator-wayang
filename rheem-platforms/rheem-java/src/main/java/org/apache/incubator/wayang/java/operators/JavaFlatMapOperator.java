package org.apache.incubator.wayang.java.operators;

import org.apache.incubator.wayang.basic.operators.FlatMapOperator;
import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.function.FlatMapDescriptor;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.optimizer.costs.LoadProfileEstimator;
import org.apache.incubator.wayang.core.optimizer.costs.LoadProfileEstimators;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.core.util.Tuple;
import org.apache.incubator.wayang.java.channels.CollectionChannel;
import org.apache.incubator.wayang.java.channels.JavaChannelInstance;
import org.apache.incubator.wayang.java.channels.StreamChannel;
import org.apache.incubator.wayang.java.execution.JavaExecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Java implementation of the {@link FlatMapOperator}.
 */
public class JavaFlatMapOperator<InputType, OutputType>
        extends FlatMapOperator<InputType, OutputType>
        implements JavaExecutionOperator {

    /**
     * Creates a new instance.
     *
     * @param functionDescriptor
     */
    public JavaFlatMapOperator(DataSetType<InputType> inputType, DataSetType<OutputType> outputType,
                               FlatMapDescriptor<InputType, OutputType> functionDescriptor) {
        super(functionDescriptor, inputType, outputType);
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public JavaFlatMapOperator(FlatMapOperator<InputType, OutputType> that) {
        super(that);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            JavaExecutor javaExecutor,
            OptimizationContext.OperatorContext operatorContext) {
        assert inputs.length == this.getNumInputs();
        assert outputs.length == this.getNumOutputs();

        final Function<InputType, Iterable<OutputType>> flatmapFunction =
                javaExecutor.getCompiler().compile(this.functionDescriptor);
        JavaExecutor.openFunction(this, flatmapFunction, inputs, operatorContext);

        ((StreamChannel.Instance) outputs[0]).accept(
                ((JavaChannelInstance) inputs[0]).<InputType>provideStream().flatMap(dataQuantum ->
                        StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        flatmapFunction.apply(dataQuantum).iterator(),
                                        Spliterator.ORDERED),
                                false
                        )
                )
        );

        return ExecutionOperator.modelLazyExecution(inputs, outputs, operatorContext);
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new JavaFlatMapOperator<>(this.getInputType(), this.getOutputType(), this.getFunctionDescriptor());
    }


    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "wayang.java.flatmap.load";
    }

    @Override
    public Optional<LoadProfileEstimator> createLoadProfileEstimator(Configuration configuration) {
        final Optional<LoadProfileEstimator> optEstimator =
                JavaExecutionOperator.super.createLoadProfileEstimator(configuration);
        LoadProfileEstimators.nestUdfEstimator(optEstimator, this.functionDescriptor, configuration);
        return optEstimator;
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        assert index <= this.getNumInputs() || (index == 0 && this.getNumInputs() == 0);
        if (this.getInput(index).isBroadcast()) return Collections.singletonList(CollectionChannel.DESCRIPTOR);
        return Arrays.asList(CollectionChannel.DESCRIPTOR, StreamChannel.DESCRIPTOR);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        assert index <= this.getNumOutputs() || (index == 0 && this.getNumOutputs() == 0);
        return Collections.singletonList(StreamChannel.DESCRIPTOR);
    }

}
