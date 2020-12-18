package org.apache.incubator.wayang.basic.operators;

import org.apache.commons.lang3.Validate;
import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.function.FunctionDescriptor;
import org.apache.incubator.wayang.core.function.ReduceDescriptor;
import org.apache.incubator.wayang.core.optimizer.cardinality.CardinalityEstimator;
import org.apache.incubator.wayang.core.optimizer.cardinality.FixedSizeCardinalityEstimator;
import org.apache.incubator.wayang.core.plan.wayangplan.UnaryToUnaryOperator;
import org.apache.incubator.wayang.core.types.BasicDataUnitType;
import org.apache.incubator.wayang.core.types.DataSetType;

import java.util.Optional;

/**
 * This operator groups the elements of a data set and aggregates the groups.
 */
public class GlobalReduceOperator<Type> extends UnaryToUnaryOperator<Type, Type> {

    protected final ReduceDescriptor<Type> reduceDescriptor;

    /**
     * Creates a new instance.
     */
    public GlobalReduceOperator(FunctionDescriptor.SerializableBinaryOperator<Type> reduceFunction,
                                Class<Type> typeClass) {
        this(new ReduceDescriptor<>(reduceFunction, typeClass));
    }

    /**
     * Creates a new instance.
     *
     * @param reduceDescriptor describes the reduction to be performed on the elements
     */
    public GlobalReduceOperator(ReduceDescriptor<Type> reduceDescriptor) {
        this(reduceDescriptor, DataSetType.createDefault(
                (BasicDataUnitType<Type>) reduceDescriptor.getInputType().getBaseType()));
    }

    /**
     * Copies an instance (exclusive of broadcasts).
     *
     * @param that that should be copied
     */
    public GlobalReduceOperator(GlobalReduceOperator<Type> that) {
        super(that);
        this.reduceDescriptor = that.reduceDescriptor;
    }


    /**
     * Creates a new instance.
     *
     * @param reduceDescriptor describes the reduction to be performed on the elements
     * @param type             type of the reduce elements (i.e., type of {@link #getInput()} and {@link #getOutput()})
     */
    public GlobalReduceOperator(ReduceDescriptor<Type> reduceDescriptor, DataSetType<Type> type) {
        super(type, type, true);
        this.reduceDescriptor = reduceDescriptor;
    }


    public DataSetType<Type> getType() {
        return this.getInputType();
    }

    public ReduceDescriptor<Type> getReduceDescriptor() {
        return this.reduceDescriptor;
    }

    @Override
    public Optional<CardinalityEstimator> createCardinalityEstimator(
            final int outputIndex,
            final Configuration configuration) {
        Validate.inclusiveBetween(0, this.getNumOutputs() - 1, outputIndex);
        // TODO: Come up with a decent way to estimate the "distinctness" of reduction keys.
        return Optional.of(new FixedSizeCardinalityEstimator(1));
    }
}
