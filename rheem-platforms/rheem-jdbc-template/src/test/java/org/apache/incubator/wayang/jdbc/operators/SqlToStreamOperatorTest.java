package org.apache.incubator.wayang.jdbc.operators;

import org.junit.Assert;
import org.junit.Test;
import org.apache.incubator.wayang.basic.data.Record;
import org.apache.incubator.wayang.core.api.Configuration;
import org.apache.incubator.wayang.core.api.Job;
import org.apache.incubator.wayang.core.function.PredicateDescriptor;
import org.apache.incubator.wayang.core.optimizer.OptimizationContext;
import org.apache.incubator.wayang.core.plan.executionplan.ExecutionTask;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.plan.wayangplan.OutputSlot;
import org.apache.incubator.wayang.core.platform.ChannelInstance;
import org.apache.incubator.wayang.core.platform.CrossPlatformExecutor;
import org.apache.incubator.wayang.core.profiling.FullInstrumentationStrategy;
import org.apache.incubator.wayang.java.channels.StreamChannel;
import org.apache.incubator.wayang.java.execution.JavaExecutor;
import org.apache.incubator.wayang.java.platform.JavaPlatform;
import org.apache.incubator.wayang.jdbc.channels.SqlQueryChannel;
import org.apache.incubator.wayang.jdbc.test.HsqldbFilterOperator;
import org.apache.incubator.wayang.jdbc.test.HsqldbPlatform;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link SqlToStreamOperator}.
 */
public class SqlToStreamOperatorTest extends OperatorTestBase {

    @Test
    public void testWithHsqldb() throws SQLException {
        Configuration configuration = new Configuration();

        Job job = mock(Job.class);
        when(job.getConfiguration()).thenReturn(configuration);

        CrossPlatformExecutor cpe = new CrossPlatformExecutor(job, new FullInstrumentationStrategy());
        when(job.getCrossPlatformExecutor()).thenReturn(cpe);
        final JavaExecutor javaExecutor = new JavaExecutor(JavaPlatform.getInstance(), job);

        HsqldbPlatform hsqldbPlatform = new HsqldbPlatform();

        // Create some test data.
        try (Connection jdbcConnection = hsqldbPlatform.createDatabaseDescriptor(configuration).createJdbcConnection()) {
            final Statement statement = jdbcConnection.createStatement();
            statement.execute("CREATE TABLE testWithHsqldb (a INT, b VARCHAR(6));");
            statement.execute("INSERT INTO testWithHsqldb VALUES (0, 'zero');");
            statement.execute("INSERT INTO testWithHsqldb VALUES (1, 'one');");
            statement.execute("INSERT INTO testWithHsqldb VALUES (2, 'two');");
        }

        final ExecutionOperator filterOperator = new HsqldbFilterOperator(
                new PredicateDescriptor<>(x -> false, Record.class)
        );
        final SqlQueryChannel sqlQueryChannel = new SqlQueryChannel(
                HsqldbPlatform.getInstance().getSqlQueryChannelDescriptor(),
                filterOperator.getOutput(0)
        );
        SqlQueryChannel.Instance sqlQueryChannelInstance = sqlQueryChannel.createInstance(
                hsqldbPlatform.createExecutor(job),
                mock(OptimizationContext.OperatorContext.class),
                0
        );
        sqlQueryChannelInstance.setSqlQuery("SELECT * FROM testWithHsqldb;");
        ExecutionTask producer = new ExecutionTask(filterOperator);
        producer.setOutputChannel(0, sqlQueryChannel);

        StreamChannel.Instance streamChannelInstance =
                new StreamChannel(StreamChannel.DESCRIPTOR, mock(OutputSlot.class)).createInstance(
                        javaExecutor,
                        mock(OptimizationContext.OperatorContext.class),
                        0
                );

        SqlToStreamOperator sqlToStreamOperator = new SqlToStreamOperator(HsqldbPlatform.getInstance());
        evaluate(
                sqlToStreamOperator,
                new ChannelInstance[]{sqlQueryChannelInstance},
                new ChannelInstance[]{streamChannelInstance}
        );

        List<Record> output = streamChannelInstance.<Record>provideStream().collect(Collectors.toList());
        List<Record> expected = Arrays.asList(
                new Record(0, "zero"),
                new Record(1, "one"),
                new Record(2, "two")
        );

        Assert.assertEquals(expected, output);
    }

    @Test
    public void testWithEmptyHsqldb() throws SQLException {
        Configuration configuration = new Configuration();

        Job job = mock(Job.class);
        when(job.getConfiguration()).thenReturn(configuration);

        CrossPlatformExecutor cpe = new CrossPlatformExecutor(job, new FullInstrumentationStrategy());
        when(job.getCrossPlatformExecutor()).thenReturn(cpe);
        final JavaExecutor javaExecutor = new JavaExecutor(JavaPlatform.getInstance(), job);

        HsqldbPlatform hsqldbPlatform = new HsqldbPlatform();

        // Create some test data.
        try (Connection jdbcConnection = hsqldbPlatform.createDatabaseDescriptor(configuration).createJdbcConnection()) {
            final Statement statement = jdbcConnection.createStatement();
            statement.execute("CREATE TABLE testWithEmptyHsqldb (a INT, b VARCHAR(6));");
        }

        final ExecutionOperator filterOperator = new HsqldbFilterOperator(
                new PredicateDescriptor<>(x -> false, Record.class)
        );
        final SqlQueryChannel sqlQueryChannel = new SqlQueryChannel(
                HsqldbPlatform.getInstance().getSqlQueryChannelDescriptor(),
                filterOperator.getOutput(0)
        );
        SqlQueryChannel.Instance sqlQueryChannelInstance = sqlQueryChannel.createInstance(
                hsqldbPlatform.createExecutor(job),
                mock(OptimizationContext.OperatorContext.class),
                0
        );
        sqlQueryChannelInstance.setSqlQuery("SELECT * FROM testWithEmptyHsqldb;");
        ExecutionTask producer = new ExecutionTask(filterOperator);
        producer.setOutputChannel(0, sqlQueryChannel);

        StreamChannel.Instance streamChannelInstance =
                new StreamChannel(StreamChannel.DESCRIPTOR, mock(OutputSlot.class)).createInstance(
                        javaExecutor,
                        mock(OptimizationContext.OperatorContext.class),
                        0
                );

        SqlToStreamOperator sqlToStreamOperator = new SqlToStreamOperator(HsqldbPlatform.getInstance());
        evaluate(
                sqlToStreamOperator,
                new ChannelInstance[]{sqlQueryChannelInstance},
                new ChannelInstance[]{streamChannelInstance}
        );

        List<Record> output = streamChannelInstance.<Record>provideStream().collect(Collectors.toList());
        Assert.assertTrue(output.isEmpty());
    }

}
