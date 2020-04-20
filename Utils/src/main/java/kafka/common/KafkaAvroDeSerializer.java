package kafka.common;

import org.apache.avro.Schema;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * KafkaAvroDeSerializer
**/
public class KafkaAvroDeSerializer implements Deserializer<Object> {
    boolean isKey = false;

    private ByteBuffer getByteBuffer(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        return buffer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.isKey = isKey;
        // nothing to do
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        // Even if the caller requests schema & version, if the payload is null we cannot include it. The caller must
        // handle
        // this case.
        Object result = null;
        try {
            ByteBuffer buffer = getByteBuffer(data);

            Schema schema = null;
            if (isKey) { // key按String处理，暂不支持其他类型
                schema = ZnvSchema.CharSequence.schema(); // primitiveSchemas.get("String");
            } else { // 只处理value用map schema的情况
                schema = ZnvSchema.Map.schema();
            }

            SpecificDatumReader<Schema> reader = new SpecificDatumReader<Schema>(schema);
            Decoder decoder = DecoderFactory.get().binaryDecoder(buffer.array(), null);
            Object object = reader.read(null, decoder);

            if (schema.getType().equals(Schema.Type.STRING)) {
                object = object.toString(); // Utf8 -> String
            }
            result = object;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
