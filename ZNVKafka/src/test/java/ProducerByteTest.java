import com.alibaba.fastjson.JSONObject;
import com.znv.kafka.ProducerBase;
import sun.misc.BASE64Decoder;

import java.nio.ByteBuffer;

/**
 * Created by ct on 2016-12-29.
 */
public class ProducerByteTest {

    private BASE64Decoder decoder = new BASE64Decoder();
    String feature = "VU4AAAAAAAAMAgAA1lP+vSDwBT1s7Ii9RKIlPVbLGb0n/0G+gQlKvmKsBL0SZ0G9P1d0Pa42272EZAa9GVWtPLY49rzxc4A8fzz1vKAYGD2h2rU90OXTPGBbJL5HUaC9dACzvEnGsr2gDyI917McPtFi/r00voA9aeA2vd7yEbxG50U8ewUDvUd9vr0eT+29fmOmO9gfrTyC4xm+ViT3Pc5RjD1XUzW8biBwvSZG4D2jna89nQsqPhVKOr5lrT27IN78O6r+KD7hWww9MGu8PbuYhr0VEkq9ba2QvCIVqjy6EG89NhnAPSEs/D1YRSE+ihBJvTrNP7xU3sm9w5v0vTaiRT6mG3O9kostu1InyD26tgW+WIeJPci7Cr72MAe+/5EcvX1aEz2GVI49IwLhOx850z3vEwM9K2NBvmGkjbxvBhM+JYuNvfj7Pb4jFQW8j90pPb2DFbvWbIa818aWvSBHMz4/+nC8B/mgOw9nhLzjuSG+LJv8PfF24L2QUPQ97ghjvLRMrz3hu9C8JKV8vWDtvb2qCkO9LsfevA0sbDkBtWu7xTeovXfhAj7Sl/S8dbgOvYIVK73ivp+9Sdv/Ox2i5D39yIg9ZR4RvpuT4z3lakm99T+jPAlyn72yPA29DCCYvQxlAj1wPpy8zacZPX/eAb41lPS9z0HNvUskrD2UJwO9/mZjPQ+dqj0=";
    ProducerBase prodcucer = new ProducerBase();

    public void test() throws Exception {
        prodcucer.init("1229-test");

        for (int i = 0; i < 1000; i++) {
            byte[] byteFeatrue = decoder.decodeBuffer(feature);
            System.out.println("decode len:" + byteFeatrue.length);
            ByteBuffer warpFeature = ByteBuffer.wrap(byteFeatrue);
            System.out.println("wrap len:" + warpFeature.capacity());
            JSONObject data = new JSONObject();
            data.put("msg_type", "image");
            data.put("feature", warpFeature);

            prodcucer.sendData(data);
            System.out.println("send ok");
        }

    }

    public static void main(String[] args) {
        try {
            ProducerByteTest test = new ProducerByteTest();
            test.test();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
