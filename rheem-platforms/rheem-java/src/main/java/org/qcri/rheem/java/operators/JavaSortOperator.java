package org.qcri.rheem.java.operators;

import org.qcri.rheem.basic.operators.SortOperator;
import org.qcri.rheem.core.api.Configuration;
import org.qcri.rheem.core.optimizer.costs.DefaultLoadEstimator;
import org.qcri.rheem.core.optimizer.costs.LoadEstimator;
import org.qcri.rheem.core.optimizer.costs.LoadProfileEstimator;
import org.qcri.rheem.core.optimizer.costs.NestableLoadProfileEstimator;
import org.qcri.rheem.core.plan.rheemplan.ExecutionOperator;
import org.qcri.rheem.core.platform.ChannelDescriptor;
import org.qcri.rheem.core.types.DataSetType;
import org.qcri.rheem.java.channels.CollectionChannel;
import org.qcri.rheem.java.channels.JavaChannelInstance;
import org.qcri.rheem.java.channels.StreamChannel;
import org.qcri.rheem.java.compiler.FunctionCompiler;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Java implementation of the {@link SortOperator}.
 */
public class JavaSortOperator<Type>
        extends SortOperator<Type>
        implements JavaExecutionOperator {


    /**
     * Creates a new instance.
     *
     * @param type type of the dataset elements
     */
    public JavaSortOperator(DataSetType<Type> type) {
        super(type);
    }

    @Override
    public void evaluate(JavaChannelInstance[] inputs, JavaChannelInstance[] outputs, FunctionCompiler compiler) {
        assert inputs.length == this.getNumInputs();
        assert outputs.length == this.getNumOutputs();

        ((StreamChannel.Instance) outputs[0]).accept(inputs[0].provideStream().sorted());
    }

    @Override
    public Optional<LoadProfileEstimator> getLoadProfileEstimator(Configuration configuration) {
        return Optional.of(new NestableLoadProfileEstimator(
                new DefaultLoadEstimator(1, 1, 0.7d, (inCards, outCards) -> 2000 * inCards[0] + 330000),
                LoadEstimator.createFallback(1, 1)
        ));
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new JavaSortOperator<>(this.getInputType());
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        assert index <= this.getNumInputs() || (index == 0 && this.getNumInputs() == 0);
        return Arrays.asList(CollectionChannel.DESCRIPTOR, StreamChannel.DESCRIPTOR);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        assert index <= this.getNumOutputs() || (index == 0 && this.getNumOutputs() == 0);
        return Collections.singletonList(StreamChannel.DESCRIPTOR);
    }
}
