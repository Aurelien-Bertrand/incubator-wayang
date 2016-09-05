package org.qcri.rheem.java.operators;

import org.qcri.rheem.basic.operators.CollectionSource;
import org.qcri.rheem.basic.operators.TextFileSource;
import org.qcri.rheem.core.api.Configuration;
import org.qcri.rheem.core.optimizer.OptimizationContext;
import org.qcri.rheem.core.optimizer.costs.LoadProfileEstimator;
import org.qcri.rheem.core.optimizer.costs.LoadProfileEstimators;
import org.qcri.rheem.core.optimizer.costs.NestableLoadProfileEstimator;
import org.qcri.rheem.core.plan.rheemplan.ExecutionOperator;
import org.qcri.rheem.core.platform.ChannelDescriptor;
import org.qcri.rheem.core.platform.ChannelInstance;
import org.qcri.rheem.core.types.DataSetType;
import org.qcri.rheem.java.channels.CollectionChannel;
import org.qcri.rheem.java.execution.JavaExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This is execution operator implements the {@link TextFileSource}.
 */
public class JavaCollectionSource<T> extends CollectionSource<T> implements JavaExecutionOperator {

    public JavaCollectionSource(Collection<T> collection, DataSetType<T> type) {
        super(collection, type);
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public JavaCollectionSource(CollectionSource<T> that) {
        super(that);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void evaluate(ChannelInstance[] inputs,
                         ChannelInstance[] outputs,
                         JavaExecutor javaExecutor,
                         OptimizationContext.OperatorContext operatorContext) {
        assert inputs.length == 0;
        assert outputs.length == 1;
        ((CollectionChannel.Instance) outputs[0]).accept(this.getCollection());
    }


    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "rheem.java.collectionsource.load";
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new JavaCollectionSource<>(this.getCollection(), this.getType());
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        throw new UnsupportedOperationException(String.format("%s does not support input channels.", this));
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        assert index <= this.getNumOutputs() || (index == 0 && this.getNumOutputs() == 0);
        return Collections.singletonList(CollectionChannel.DESCRIPTOR);
    }

    @Override
    public boolean isExecutedEagerly() {
        return false;
    }

}
