package org.apache.incubator.wayang.sqlite3.operators;

import org.apache.incubator.wayang.basic.data.Record;
import org.apache.incubator.wayang.basic.function.ProjectionDescriptor;
import org.apache.incubator.wayang.basic.operators.MapOperator;
import org.apache.incubator.wayang.jdbc.operators.JdbcProjectionOperator;
import org.apache.incubator.wayang.sqlite3.platform.Sqlite3Platform;

/**
 * Implementation of the {@link JdbcProjectionOperator} for the {@link Sqlite3Platform}.
 */
public class Sqlite3ProjectionOperator extends JdbcProjectionOperator {

    public Sqlite3ProjectionOperator(ProjectionDescriptor<Record, Record> functionDescriptor) {
        super(functionDescriptor);
    }

    public Sqlite3ProjectionOperator(Class<Record> inputClass, Class<Record> outputClass, String... fieldNames) {
        super(fieldNames);
    }

    public Sqlite3ProjectionOperator(MapOperator<Record, Record> that) {
        super(that);
    }

    @Override
    public Sqlite3Platform getPlatform() {
        return Sqlite3Platform.getInstance();
    }

}
