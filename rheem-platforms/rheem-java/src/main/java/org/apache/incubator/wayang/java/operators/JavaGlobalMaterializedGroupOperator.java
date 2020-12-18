package org.apache.incubator.wayang.java.operators;

import org.apache.incubator.wayang.basic.operators.GlobalMaterializedGroupOperator;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.core.util.Tuple;
import org.apache.incubator.wayang.java.channels.CollectionChannel;
import org.apache.incubator.wayang.java.execution.JavaExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Java implementation of the {@link GlobalMaterializedGroupOperator}.
 */
public class JavaGlobalMaterializedGroupOperator<Type>
        extends GlobalMaterializedGroupOperator<Type>
        implements JavaExecutionOperator {

    public JavaGlobalMaterializedGroupOperator(DataSetType<Type> inputType, DataSetType<Iterable<Type>> outputType) {
        super(inputType, outputType);
    }

    public JavaGlobalMaterializedGroupOperator(Class<Type> typeClass) {
        super(typeClass);
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public JavaGlobalMaterializedGroupOperator(GlobalMaterializedGroupOperator<Type> that) {
        super(that);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            JavaExecutor javaExecutor,
            OptimizationContext.OperatorContext operatorContext) {
        assert inputs.length == 1;
        assert outputs.length == 1;

        CollectionChannel.Instance inputChannelInstance = (CollectionChannel.Instance) inputs[0];
        final Collection<?> dataQuanta = inputChannelInstance.provideCollection();
        Collection<Iterable<?>> dataQuantaGroup = new ArrayList<>(1);
        dataQuantaGroup.add(dataQuanta);

        CollectionChannel.Instance outputChannelInstance = (CollectionChannel.Instance) outputs[0];
        outputChannelInstance.accept(dataQuantaGroup);

        return ExecutionOperator.modelEagerExecution(inputs, outputs, operatorContext);
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        return Collections.singletonList(CollectionChannel.DESCRIPTOR);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        return Collections.singletonList(CollectionChannel.DESCRIPTOR);
    }


    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "wayang.java.globalgroup.load";
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new JavaGlobalMaterializedGroupOperator<>(this.getInputType(), this.getOutputType());
    }

}
