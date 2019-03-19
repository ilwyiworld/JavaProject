package com.znv.kafka.common;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * KafkaAvroSerializer
 **/
public class KafkaAvroSerializer implements Serializer<Object> {
    private String encoding = "UTF8";
    protected static final byte MAGIC_BYTE = 0x0;

    protected static final int IDSIZE = 4;
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        String propertyName = isKey ? "key.serializer.encoding" : "value.serializer.encoding";
        Object encodingValue = configs.get(propertyName);
        if (encodingValue == null) {
            encodingValue = configs.get("serializer.encoding");
        }
        if (encodingValue != null && encodingValue instanceof String) {
            encoding = (String) encodingValue;
        }
    }

    @Override
    public byte[] serialize(String topic, Object record) {
        Schema schema = null;
        // null needs to treated specially since the client most likely just wants to send
        // an individual null value instead of making the subject a null type. Also, null in
        // Kafka has a special meaning for deletion in a topic with the compact retention policy.
        // Therefore, we will bypass schema registration and return a null value in Kafka, instead
        // of an Avro encoded null.
        if (record == null) {
            return null;
        }

        try {
            schema = ZnvSchema.getSchema(record);
            // int id = schemaRegistry.register(subject, schema);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // out.write(MAGIC_BYTE);
            // out.write(ByteBuffer.allocate(IDSIZE).putInt(id).array());
            if (record instanceof byte[]) {
                out.write((byte[]) record);
            } else {
                BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
                DatumWriter<Object> writer = null;
                Object value = record; // instanceof GenericContainer ? ((GenericContainer ) record).getValue() : record;
                if (value instanceof SpecificRecord) {
                    writer = new SpecificDatumWriter<Object>(schema);
                } else {
                    writer = new GenericDatumWriter<Object>(schema);
                }
                writer.write(value, encoder);
                encoder.flush();
            }
            byte[] bytes = out.toByteArray();
            out.close();
            return bytes;
        } catch (Exception e) {
            // avro serialization can throw AvroRuntimeException, NullPointerException,
            // ClassCastException, etc
            throw new SerializationException("Error serializing Avro message", e);
        }
    }

    @Override
    public void close() {

    }
}
