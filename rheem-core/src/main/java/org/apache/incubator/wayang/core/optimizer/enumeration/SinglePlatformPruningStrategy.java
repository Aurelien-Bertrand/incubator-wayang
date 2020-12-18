package org.apache.incubator.wayang.core.optimizer.enumeration;

import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.platform.Platform;

/**
 * This {@link PlanEnumerationPruningStrategy} retains only the best {@link PlanImplementation}s employing a single
 * {@link Platform} only.
 * <p>There is one caveat, though: If for some reason the most efficient way to communicate for two
 * {@link org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator}s from the same {@link Platform} goes over another
 * {@link Platform}, then we will prune the corresponding {@link PlanImplementation}. The more complete way is to
 * look only for non-cross-platform {@link org.apache.incubator.wayang.core.platform.Junction}s. We neglect this issue for now.</p>
 */
@SuppressWarnings("unused")
public class SinglePlatformPruningStrategy implements PlanEnumerationPruningStrategy {


    @Override
    public void configure(Configuration configuration) {
    }

    @Override
    public void prune(PlanEnumeration planEnumeration) {
        planEnumeration.getPlanImplementations().removeIf(
                planImplementation -> planImplementation.getUtilizedPlatforms().size() > 1
        );
    }

}
