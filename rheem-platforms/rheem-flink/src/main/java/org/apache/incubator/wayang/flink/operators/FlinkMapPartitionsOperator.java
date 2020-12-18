package org.apache.incubator.wayang.flink.operators;

import org.apache.flink.api.common.functions.MapPartitionFunction;
import org.apache.flink.api.common.functions.RichMapPartitionFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.incubator.wayang.basic.operators.MapPartitionsOperator;
import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.function.MapPartitionsDescriptor;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.optimizer.costs.LoadProfileEstimator;
import org.apache.incubator.wayang.core.optimizer.costs.LoadProfileEstimators;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.lineage.ExecutionLineageNode;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.core.util.Tuple;
import org.apache.incubator.wayang.flink.channels.DataSetChannel;
import org.apache.incubator.wayang.flink.execution.FlinkExecutionContext;
import org.apache.incubator.wayang.flink.execution.FlinkExecutor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Flink implementation of the {@link MapPartitionsOperator}.
 */
public class FlinkMapPartitionsOperator<InputType, OutputType>
        extends MapPartitionsOperator<InputType, OutputType>
        implements FlinkExecutionOperator {


    /**
     * Creates a new instance.
     */
    public FlinkMapPartitionsOperator(MapPartitionsDescriptor<InputType, OutputType> functionDescriptor,
                                      DataSetType<InputType> inputType, DataSetType<OutputType> outputType) {
        super(functionDescriptor, inputType, outputType);
    }

    /**
     * Creates a new instance.
     */
    public FlinkMapPartitionsOperator(MapPartitionsDescriptor<InputType, OutputType> functionDescriptor) {
        this(functionDescriptor,
                DataSetType.createDefault(functionDescriptor.getInputType()),
                DataSetType.createDefault(functionDescriptor.getOutputType()));
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public FlinkMapPartitionsOperator(MapPartitionsOperator<InputType, OutputType> that) {
        super(that);
    }

    @Override
    public Tuple<Collection<ExecutionLineageNode>, Collection<ChannelInstance>> evaluate(
            ChannelInstance[] inputs,
            ChannelInstance[] outputs,
            FlinkExecutor flinkExecutor,
            OptimizationContext.OperatorContext operatorContext) {
        assert inputs.length == this.getNumInputs();
        assert outputs.length == this.getNumOutputs();

        final DataSetChannel.Instance input = (DataSetChannel.Instance) inputs[0];
        final DataSetChannel.Instance output = (DataSetChannel.Instance) outputs[0];

        final DataSet dataSetInput = input.provideDataSet();
        final Class class_output = this.getOutput().getType().getDataUnitType().getTypeClass();
        DataSet dataSetOutput;
        if( this.getNumBroadcastInputs() > 0 ) {
            Tuple<String, DataSet> names = searchBroadcast(inputs);

            FlinkExecutionContext fex = new FlinkExecutionContext(this, inputs, 0);

            final RichMapPartitionFunction<InputType, OutputType> richFunction =
                    flinkExecutor.compiler.compile(
                            this.getFunctionDescriptor(),
                            fex
                    );

            fex.setRichFunction(richFunction);

            dataSetOutput = dataSetInput
                    .mapPartition(richFunction)
                    .withBroadcastSet(names.field1, names.field0)
                    .returns(class_output);

        }else{
            final MapPartitionFunction<InputType, OutputType> mapFunction =
                    flinkExecutor.compiler.compile(this.getFunctionDescriptor());

            dataSetOutput = dataSetInput.mapPartition(mapFunction).returns(class_output);
        }

        output.accept(dataSetOutput, flinkExecutor);

        return ExecutionOperator.modelLazyExecution(inputs, outputs, operatorContext);
    }

    private Tuple<String, DataSet> searchBroadcast(ChannelInstance[] inputs) {
        for(int i = 0; i < this.inputSlots.length; i++){
            if( this.inputSlots[i].isBroadcast() ){
                DataSetChannel.Instance dataSetChannel = (DataSetChannel.Instance)inputs[inputSlots[i].getIndex()];
                return new Tuple<>(inputSlots[i].getName(), dataSetChannel.provideDataSet());
            }
        }
        return null;
    }

    @Override
    protected ExecutionOperator createCopy() {
        return new FlinkMapPartitionsOperator<>(this.getFunctionDescriptor(), this.getInputType(), this.getOutputType());
    }

    @Override
    public String getLoadProfileEstimatorConfigurationKey() {
        return "wayang.flink.mappartitions.load";
    }

    @Override
    public Optional<LoadProfileEstimator> createLoadProfileEstimator(Configuration configuration) {
        final Optional<LoadProfileEstimator> optEstimator =
                FlinkExecutionOperator.super.createLoadProfileEstimator(configuration);
        LoadProfileEstimators.nestUdfEstimator(optEstimator, this.functionDescriptor, configuration);
        return optEstimator;
    }

    @Override
    public List<ChannelDescriptor> getSupportedInputChannels(int index) {
        return Arrays.asList(DataSetChannel.DESCRIPTOR, DataSetChannel.DESCRIPTOR_MANY);
    }

    @Override
    public List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        return Collections.singletonList(DataSetChannel.DESCRIPTOR);
    }

    @Override
    public boolean containsAction() {
        return false;
    }

}
