package JsonTest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

/**
 * Created by ct on 2016-08-19.
 */
public class TestJson {

    public static void main(String[] args) throws IOException {

        String test = "{ \"result\": \"success\"," + "\"data\": {" + " \"age\": 27," + "\"gender\": 0,"
            + "\"eyeglass\": 0," + "\"sunglass\": 0," + "\"smile\": 0," + "\"mask\": 0," + "\"race\": 0,"
            + "\"eyeOpen\": 1," + "\"mouthOpen\": 0," + "\"beard\": 0" + " }" + "} ";
        JSONObject json = JSON.parseObject(test);
        JSONObject data = JSON.parseObject(json.get("data").toString());
        System.out.println(data.get("age"));
        JSONObject jtemp = new JSONObject();
        for (String key : data.keySet()) {
            jtemp.put(key, data.get(key));
        }
        System.out.println(jtemp);

        // Schema schema = new Schema.Parser()
        // .parse("{\"type\":\"map\", \"values\": [\"null\", \"int\", \"long\", \"float\", \"double\", \"string\",
        // \"boolean\", \"bytes\"]}");

        // JSONObject json = new JSONObject();
        // json.put("key1", "metadata");
        // json.put("key2", 111);
        // json.put("key3", 222L);
        // json.put("key4", 3.33F);
        // json.put("key5", 4.44);
        // json.put("key6", "i am xf");
        // json.put("key7", true);
        // json.put("key7"," ");
        //
        // String key6 = "key6";
        // System.out.println("=====" + json.get(key6));
        // System.out.println(json.toJSONString());
        // System.out.println(json.toString());
        //
        // ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
        // SpecificDatumWriter<JSONObject> write = new SpecificDatumWriter<JSONObject>(schema);
        // write.write(json, encoder);
        // encoder.flush();
        // out.close();
        //
        // SpecificDatumReader<Map<String, Object>> reader = new SpecificDatumReader<Map<String, Object>>(schema);
        // Decoder decoder = DecoderFactory.get().binaryDecoder(out.toByteArray(), null);
        // Map<String, Object> map = reader.read(null, decoder);
        //
        // System.out.format("%s\n",map.get(new Utf8("key6")));
        //
        // if(map.containsKey(new Utf8("key7")))
        // {
        // Object key7 = map.get(new Utf8("key7"));
        // if (key7 == null)
        // {
        // System.out.println("key7 is null");
        // }
        // else
        // {
        //
        // System.out.println("key7:" + key7.toString());
        // System.out.println(map.get(new Utf8("key7")));
        // }
        // }
        // else
        // {
        // System.out.println("no key6");
        // }
        //
        // for (Object key : map.keySet()) {
        // System.out.println("key class " + key.getClass());
        // Object value = map.get(key);
        // //System.out.println("value to string " + value.toString());
        // System.out.format("%s=%s(%s)\n", key, value, value != null ? value.getClass() : "null");
        // }
    }

}
