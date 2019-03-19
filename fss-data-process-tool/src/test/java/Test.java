import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 自己直接来进行测试时间用的------------
 */
public class Test {

    public static long fromDateStringToLong(String inVal) { //此方法计算时间毫秒
        Date date = null;   //定义时间类型
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-mm-dd hh:ss");
        try {
            date = inputFormat.parse(inVal); //将字符型转换成日期型
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();   //返回毫秒数
    }

    public static void main(String args[]) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String s1 = df.format(System.currentTimeMillis());
        System.out.println(s1);
        String s2 = "2018-05-29 11:50:23";
        long startT = fromDateStringToLong(s1);
        long endT = fromDateStringToLong(s2);
        long seconds = (endT - startT) / (1000); //共计秒数
        System.out.println(seconds);
        System.out.println(sdf.format(new Date()));

        String params="[{rowkey=12345678902345622343, uuid=8888888}, {rowkey=1212, uuid=4324324}]";
        List list1 = Arrays.asList(params);
        for(int i=0;i<list1.size();i++){

        }
    }

}

