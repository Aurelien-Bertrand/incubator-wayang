package org.apache.incubator.wayang.giraph.mappings;

import org.apache.incubator.wayang.basic.operators.PageRankOperator;
import org.apache.incubator.wayang.core.mapping.*;
import org.apache.incubator.wayang.core.plan.wayangplan.Operator;
import org.apache.incubator.wayang.core.plan.wayangplan.Subplan;
import org.apache.incubator.wayang.giraph.operators.GiraphPageRankOperator;
import org.apache.incubator.wayang.giraph.platform.GiraphPlatform;

import java.util.Collection;
import java.util.Collections;

/**
 * Maps {@link PageRankOperator}s to {@link GiraphPageRankOperator}s.
 */
public class PageRankMapping implements Mapping {

    @Override
    public Collection<PlanTransformation> getTransformations() {
        return Collections.singleton(
                new PlanTransformation(
                        this.createSubplanPattern(),
                        this.createReplacementSubplanFactory(),
                        GiraphPlatform.getInstance()
                )
        );
    }

    @SuppressWarnings("unchecked")
    private SubplanPattern createSubplanPattern() {
        final OperatorPattern operatorPattern = new OperatorPattern(
                "pageRank", new PageRankOperator(1), false);
        return SubplanPattern.createSingleton(operatorPattern);
    }

    private ReplacementSubplanFactory createReplacementSubplanFactory() {
        return new ReplacementSubplanFactory.OfSingleOperators<PageRankOperator>(
                (matchedOperator, epoch) -> new GiraphPageRankOperator(matchedOperator).at(epoch)
        );
    }
}
