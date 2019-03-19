package com.znv.fss.phoenix.UDF;

import com.znv.fss.common.utils.FeatureCompUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.phoenix.compile.KeyPart;
import org.apache.phoenix.expression.Expression;
import org.apache.phoenix.expression.function.ScalarFunction;
import org.apache.phoenix.parse.FunctionParseNode;
import org.apache.phoenix.schema.tuple.Tuple;
import org.apache.phoenix.schema.types.PBinaryArray;
import org.apache.phoenix.schema.types.PDataType;
import org.apache.phoenix.schema.types.PFloat;
import org.apache.phoenix.schema.types.PVarbinary;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//import sun.misc.BASE64Decoder;

/**
 * FeatureCompFunction
 */
@FunctionParseNode.BuiltInFunction(name = FeatureCompFunction.NAME, args = {
    @FunctionParseNode.Argument(allowedTypes = { PBinaryArray.class, PBinaryArray.class }) })
public class FeatureCompFunction extends ScalarFunction {
    private static final Log LOG = LogFactory.getLog(FeatureCompFunction.class);
    public static final String NAME = "FeatureComp";
    // private static final Logger logger = LoggerFactory.getLogger(FeatureCompFunction.class);

    public FeatureCompFunction() {
    }

    public FeatureCompFunction(List<Expression> children) throws SQLException {
        super(children);
    }

    @Override
    public boolean evaluate(Tuple tuple, ImmutableBytesWritable ptr) {
        int i = 0;
        List<byte[]> list = new ArrayList<byte[]>();
        // List<String> strList = new ArrayList<String>();
        // BASE64Encoder encoder = new BASE64Encoder();
        // BASE64Decoder deoder = new BASE64Decoder();
        FeatureCompUtil compUtil = new FeatureCompUtil();
        // Get the child argument and evaluate it first
        Expression expression = this.getChildren().get(0);
        if (!expression.evaluate(tuple, ptr)) {
            return false;
        }
        byte[] src = (byte[]) PVarbinary.INSTANCE.toObject(ptr, PVarbinary.INSTANCE, expression.getSortOrder());
        list.add(src);
        // strList.add(encoder.encode(src));

        expression = this.getChildren().get(1);
        if (!expression.evaluate(tuple, ptr)) {
            return false;
        }
        // String strdst = (String) PVarchar.INSTANCE.toObject(ptr, PVarchar.INSTANCE, expression.getSortOrder());
        byte[] dst = (byte[]) PVarbinary.INSTANCE.toObject(ptr, PVarbinary.INSTANCE, expression.getSortOrder());
        /*
         * try { list.add(deoder.decodeBuffer(strdst)); } catch (IOException e) { e.printStackTrace(); }
         */
        list.add(dst);
        // strList.add(strdst);

        /*
         * for (i = 0; i < 2; i++) { Expression expression = this.getChildren().get(i); if (!expression.evaluate(tuple,
         * ptr)) { return false; } byte[] src = (byte[])PVarbinary.INSTANCE.toObject(ptr,
         * PVarbinary.INSTANCE,expression.getSortOrder()); if(i == 2) { byte[] bf = deoder.decodeBuffer(feature1); }
         * list.add(src); strList.add(encoder.encode(src)); }
         */
        // float result = (float)0.9;// FeatureCompare.featureComp(list.get(0), list.get(1));
//        int result = 0; // V241 2018-05-21
        float  result = 0.0f; // V242 2018-05-21
        try {
            // result = (int) (compUtil.Comp(list.get(0), list.get(1)) * 100);
            // result = (int) (compUtil.Comp(list.get(0), list.get(1), 12) * 100); // ԭ����
//            result = (int) Math.floor(compUtil.Comp(list.get(0), list.get(1), 12) * 100); // ����ȡ�� V241 2018-05-21
            float tempResult = compUtil.Dot(list.get(0), list.get(1), 12); // V242 2018-05-21
            if (tempResult >= 0.0f) {
                //result =new BigDecimal(String.valueOf(tempResult)).doubleValue(); // [lq-add] The binary representation is an 4 byte float with the sign bit flipped
                result = tempResult;
            }
            // result = 99;
            // result = (int)(FeatureCompare.featureComp(list.get(0), list.get(1))*100);
        } catch (Exception e) {
            LOG.error(e);
        }
        // logger.info("#f1:"+strList.get(0));
        // logger.info("#f2:"+strList.get(1));
        // logger.info("#result:"+result);
        // todo: optimize
        // Convert the bytes pointed to into a String passing along any
        // modifiers that need to be applied (for example the bytes
        // may need to be inverted if they're stored DESCENDING)
        // buffer = PDataType.DECIMAL.toBytes(this.cachedResult);
        // ptr.set(PInteger.INSTANCE.toBytes(result));
        ptr.set(PFloat.INSTANCE.toBytes(result));  // [lq-modify-2018-05-24]
        //ptr.set(PDouble.INSTANCE.toBytes(result));
        return true;
    }

    @Override
    public PDataType getDataType() {
        // TODO Auto-generated method stub
        // return PInteger.INSTANCE;
        //return PDouble.INSTANCE;
        return PFloat.INSTANCE;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return NAME;
    }

    public OrderPreserving preservesOrder() {
        return OrderPreserving.NO;
    }

    /**
     * Determines whether or not a function may be used to form the start/stop key of a scan
     *
     * @return the zero-based position of the argument to traverse into to look for a primary key column reference, or
     *         {@value #NO_TRAVERSAL} if the function cannot be used to form the scan key.
     */
    public int getKeyFormationTraversalIndex() {
        return NO_TRAVERSAL;
    }

    /**
     * Manufactures a KeyPart used to construct the KeyRange given a constant and a comparison operator.
     *
     * @param childPart the KeyPart formulated for the child expression at the {@link #getKeyFormationTraversalIndex()}
     *            position.
     * @return the KeyPart for constructing the KeyRange for this function.
     */
    public KeyPart newKeyPart(KeyPart childPart) {
        return null;
    }
}
