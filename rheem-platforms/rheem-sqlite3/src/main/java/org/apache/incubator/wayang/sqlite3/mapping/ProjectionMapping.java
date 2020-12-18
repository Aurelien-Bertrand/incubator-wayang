package org.apache.incubator.wayang.sqlite3.mapping;

import org.apache.incubator.wayang.basic.data.Record;
import org.apache.incubator.wayang.basic.function.ProjectionDescriptor;
import org.apache.incubator.wayang.basic.operators.MapOperator;
import org.apache.incubator.wayang.core.mapping.Mapping;
import org.apache.incubator.wayang.core.mapping.OperatorPattern;
import org.apache.incubator.wayang.core.mapping.PlanTransformation;
import org.apache.incubator.wayang.core.mapping.ReplacementSubplanFactory;
import org.apache.incubator.wayang.core.mapping.SubplanPattern;
import org.apache.incubator.wayang.sqlite3.operators.Sqlite3ProjectionOperator;
import org.apache.incubator.wayang.sqlite3.platform.Sqlite3Platform;

import java.util.Collection;
import java.util.Collections;

/**
 * Mapping from {@link MapOperator} to {@link Sqlite3ProjectionOperator}.
 */
@SuppressWarnings("unchecked")
public class ProjectionMapping implements Mapping {

    @Override
    public Collection<PlanTransformation> getTransformations() {
        return Collections.singleton(
                new PlanTransformation(
                        this.createSubplanPattern(),
                        this.createReplacementSubplanFactory(),
                        Sqlite3Platform.getInstance()
                )
        );
    }

    private SubplanPattern createSubplanPattern() {
        final OperatorPattern<MapOperator<Record, Record>> operatorPattern = new OperatorPattern<>(
                "projection", new MapOperator<>(null, Record.class, Record.class), false
        )
                .withAdditionalTest(op -> op.getFunctionDescriptor() instanceof ProjectionDescriptor)
                .withAdditionalTest(op -> op.getNumInputs() == 1); // No broadcasts.
        return SubplanPattern.createSingleton(operatorPattern);
    }

    private ReplacementSubplanFactory createReplacementSubplanFactory() {
        return new ReplacementSubplanFactory.OfSingleOperators<MapOperator<Record, Record>>(
                (matchedOperator, epoch) -> new Sqlite3ProjectionOperator(matchedOperator).at(epoch)
        );
    }
}
