package org.apache.incubator.wayang.jdbc.operators;

import org.apache.incubator.wayang.basic.operators.TableSource;
import org.apache.incubator.wayang.core.plan.wayangplan.ExecutionOperator;
import org.apache.incubator.wayang.core.platform.ChannelDescriptor;
import org.apache.incubator.wayang.jdbc.compiler.FunctionCompiler;
import org.apache.incubator.wayang.jdbc.platform.JdbcPlatformTemplate;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

public interface JdbcExecutionOperator extends ExecutionOperator {

    /**
     * Creates a SQL clause for this instance. For {@link TableSource}s it returns an identifier for the table
     * usable in a {@code FROM} clause. For {@link JdbcProjectionOperator}s it returns a list usable in a
     * {@code SELECT} clause. For {@link JdbcFilterOperator}s it creates a condition usable in a {@code WHERE} clause.
     * Also, these different clauses should be compatible for connected {@link JdbcExecutionOperator}s.
     *
     * @param compiler used to create SQL code
     * @return the SQL clause
     */
    String createSqlClause(Connection connection, FunctionCompiler compiler);

    @Override
    JdbcPlatformTemplate getPlatform();

    @Override
    default List<ChannelDescriptor> getSupportedInputChannels(int index) {
        return Collections.singletonList(this.getPlatform().getSqlQueryChannelDescriptor());
    }

    @Override
    default List<ChannelDescriptor> getSupportedOutputChannels(int index) {
        return Collections.singletonList(this.getPlatform().getSqlQueryChannelDescriptor());
    }

}
