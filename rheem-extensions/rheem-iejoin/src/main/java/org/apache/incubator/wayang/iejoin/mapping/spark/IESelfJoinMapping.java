package org.apache.incubator.wayang.iejoin.mapping.spark;

import org.apache.incubator.wayang.basic.data.Record;
import org.apache.incubator.wayang.core.mapping.Mapping;
import org.apache.incubator.wayang.core.mapping.OperatorPattern;
import org.apache.incubator.wayang.core.mapping.PlanTransformation;
import org.apache.incubator.wayang.core.mapping.ReplacementSubplanFactory;
import org.apache.incubator.wayang.core.mapping.SubplanMatch;
import org.apache.incubator.wayang.core.mapping.SubplanPattern;
import org.apache.incubator.wayang.core.plan.wayangplan.Operator;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.iejoin.operators.IEJoinMasterOperator;
import org.apache.incubator.wayang.iejoin.operators.IESelfJoinOperator;
import org.apache.incubator.wayang.iejoin.operators.SparkIESelfJoinOperator;
import org.apache.incubator.wayang.spark.platform.SparkPlatform;

import java.util.Collection;
import java.util.Collections;

/**
 * Mapping from {@link IESelfJoinOperator} to {@link SparkIESelfJoinOperator}.
 */
public class IESelfJoinMapping implements Mapping {

    @Override
    public Collection<PlanTransformation> getTransformations() {
        return Collections.singleton(new PlanTransformation(this.createSubplanPattern(), new ReplacementFactory(),
                SparkPlatform.getInstance()));
    }

    private SubplanPattern createSubplanPattern() {
        final OperatorPattern operatorPattern = new OperatorPattern(
                "ieselfjoin", new IESelfJoinOperator<>(DataSetType.none(), null, IEJoinMasterOperator.JoinCondition.GreaterThan, null, IEJoinMasterOperator.JoinCondition.GreaterThan), false);
        return SubplanPattern.createSingleton(operatorPattern);
    }

    private static class ReplacementFactory<InputType0 extends Record, InputType1 extends Record,
            Type0 extends Comparable<Type0>, Type1 extends Comparable<Type1>> extends ReplacementSubplanFactory {

        @Override
        protected Operator translate(SubplanMatch subplanMatch, int epoch) {
            final IESelfJoinOperator<?, ?, ?> originalOperator = (IESelfJoinOperator<?, ?, ?>) subplanMatch.getMatch("ieselfjoin").getOperator();
            return new SparkIESelfJoinOperator(originalOperator.getInputType(), originalOperator.getGet0Pivot(), originalOperator.getCond0(), originalOperator.getGet0Ref(), originalOperator.getCond1()).at(epoch);
        }
    }
}
