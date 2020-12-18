package org.apache.incubator.wayang.core.platform;

import org.apache.incubator.wayang.core.api.Job;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.plan.executionplan.ExecutionStage;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;

/**
 * An executor executes {@link ExecutionOperator}s.
 */
public interface Executor extends CompositeExecutionResource {

    /**
     * Executes the given {@code stage}.
     *
     * @param stage          should be executed; must be executable by this instance, though
     * @param optimizationContext
     *@param executionState provides and accepts execution-related objects  @return collected metadata from instrumentation
     */
    void execute(ExecutionStage stage, OptimizationContext optimizationContext, ExecutionState executionState);

    /**
     * Releases any instances acquired by this instance to execute {@link ExecutionStage}s.
     */
    void dispose();

    /**
     * @return the {@link Platform} this instance belongs to
     */
    Platform getPlatform();

    /**
     * If this instance is instrumented by a {@link CrossPlatformExecutor}, this method provides the latter.
     *
     * @return the instrumenting {@link CrossPlatformExecutor} or {@code null} if none
     */
    CrossPlatformExecutor getCrossPlatformExecutor();

    /**
     * Factory for {@link Executor}s.
     */
    interface Factory {

        /**
         * @return a new {@link Executor}
         */
        Executor create(Job job);

    }

}
