package org.apache.incubator.wayang.spark.operators;

import org.junit.Assert;
import org.junit.Test;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.types.DataSetType;
import org.apache.incubator.wayang.spark.channels.RddChannel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Test suite for the {@link SparkCollectionSource}.
 */
public class SparkCollectionSourceTest extends SparkOperatorTestBase {

    @Test
    public void testExecution() {
        Set<Integer> inputValues = new HashSet<>(Arrays.asList(1, 2, 3));
        SparkCollectionSource<Integer> collectionSource = new SparkCollectionSource<>(
                inputValues,
                DataSetType.createDefault(Integer.class));
        RddChannel.Instance output = this.createRddChannelInstance();

        // Set up the ChannelInstances.
        final ChannelInstance[] inputs = new ChannelInstance[]{};
        final ChannelInstance[] outputs = new ChannelInstance[]{output};

        // Execute.
        this.evaluate(collectionSource, inputs, outputs);

        final Set<Integer> outputValues = new HashSet<>(output.<Integer>provideRdd().collect());
        Assert.assertEquals(outputValues, inputValues);
    }
}
