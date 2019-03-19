package com.znv.fss.phoenix.UDF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.function.ScalarFunction;
import org.apache.phoenix.parse.FunctionParseNode.Argument;
import org.apache.phoenix.parse.FunctionParseNode.BuiltInFunction;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PVarchar;

import java.sql.SQLException;
import java.util.List;

/**
 * ReverseFunction
 */
@BuiltInFunction(name = ReverseFunction.NAME, args = { @Argument(allowedTypes = { PVarchar.class }) })
public class ReverseFunction extends ScalarFunction {
    public static final String NAME = "REVERSE";
    private static final Log LOG = LogFactory.getLog(ReverseFunction.class);

    public ReverseFunction() {
    }

    public ReverseFunction(List<Expression> children) throws SQLException {
        super(children);
    }

    @Override
    public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr) {
        // Get the child argument and evaluate it first
        Expression expression = this.getChildren().get(0);
        if (!expression.evaluate(tuple, ptr)) {
            return false;
        }
        // todo: optimize
        // Convert the bytes pointed to into a String passing along any
        // modifiers that need to be applied (for example the bytes
        // may need to be inverted if they're stored DESCENDING)
        String sourceStr = (String) PVarchar.INSTANCE.toObject(ptr, PVarchar.INSTANCE);
        LOG.info("######## ReverseFunction sourceStr:" + sourceStr);
        if (sourceStr == null) {
            return true;
        }
        StringBuilder builder = new StringBuilder(sourceStr);
        ptr.set(PVarchar.INSTANCE.toBytes(builder.reverse().toString() + "abcdefg"));
        return true;
    }

    @Override
    public PDataType getDataType() {
        // todo Auto-generated method stub
        return PVarchar.INSTANCE;
    }

    @Override
    public String getName() {
        // todo Auto-generated method stub
        return NAME;
    }
}
