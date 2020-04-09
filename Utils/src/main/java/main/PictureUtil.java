package main;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;

/**
 * Created by Administrator on 2018/3/20.
 */
public class PictureUtil {
    /**
     * 将图片转换成Base64编码
     *
     * @param imgFile 待处理图片
     * @return
     */
    public static String getImgStr(String imgFile) {
        //将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        //读取图片字节数组
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(data));
    }

    static byte[] bytes;

    public static void main(String[] args) throws Exception {
        File img = new File("C:\\Users\\yiliang\\Desktop\\44.png");
        fileToByte(img);
        ByteToFile(bytes);
    }

    public static void fileToByte(File img) throws Exception {
        BufferedImage bi;
        bi = ImageIO.read(img);
        bytes = bufferedImageToBytes(bi, 1);
        System.err.println(bytes.length);
    }

    static void ByteToFile(byte[] bytes) throws Exception {
        //ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        //BufferedImage bi1 =ImageIO.read(bais);
        OutputStream os = new FileOutputStream("C:\\Users\\yiliang\\Desktop\\test2.jpg");
        try {
            //File w2 = new File("C:\\Users\\yiliang\\Desktop\\test2.jpg");//可以是jpg,png,gif格式
            //ImageIO.write(bi1, "jpg", w2);//不管输出什么格式图片，此处不需改动
            os.write(bytes, 0, bytes.length);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os.close();
            //bais.close();
        }
    }

    /**
     * 自己设置压缩质量来把图片压缩成byte[]
     * @param image   压缩源图片
     * @param quality 压缩质量，在0-1之间，
     * @return 返回的字节数组
     */
    private static byte[] bufferedImageToBytes(BufferedImage image, float quality) {
        //如果图片空，返回空
        if (image == null) {
            return null;
        }
        BufferedImage bImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        bImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);

        // 得到指定Format图片的writer
        Iterator<ImageWriter> iter = ImageIO
                .getImageWritersByFormatName("jpg");// 得到迭代器
        ImageWriter writer = (ImageWriter) iter.next(); // 得到writer

        // 得到指定writer的输出参数设置(ImageWriteParam )
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); // 设置可否压缩
        iwp.setCompressionQuality(quality); // 设置压缩质量参数

        iwp.setProgressiveMode(ImageWriteParam.MODE_DISABLED);

        ColorModel colorModel = ColorModel.getRGBdefault();
        // 指定压缩时使用的色彩模式
        iwp.setDestinationType(
                new javax.imageio.ImageTypeSpecifier(
                        colorModel,
                        colorModel.createCompatibleSampleModel(16, 16)));

        // 开始打包图片，写入byte[]
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  // 取得内存输出流
        IIOImage iIamge = new IIOImage(bImage, null, null);
        try {
            //此处因为ImageWriter中用来接收write信息的output要求必须是ImageOutput
            //通过ImageIo中的静态方法，得到byteArrayOutputStream的ImageOutput
            writer.setOutput(ImageIO.createImageOutputStream(byteArrayOutputStream));
            writer.write(null, iIamge, iwp);
        } catch (IOException e) {
            System.out.println("write error");
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String getPicBase64ByUrl(String imgUrl) {
        //return HttpClientUtil.doGet(url,(Map)null,(Map)null);
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        try {
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();
            is = httpUrl.getInputStream();
            outStream = new ByteArrayOutputStream();
            //创建一个Buffer字符串
            byte[] buffer = new byte[1024];
            //每次读取的字符串长度，如果为-1，代表全部读取完毕
            int len = 0;
            //使用一个输入流从buffer里把数据读取出来
            while ((len = is.read(buffer)) != -1) {
                //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(buffer, 0, len);
            }
            // 对字节数组Base64编码
            return Base64Util.encode(outStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
        return imgUrl;
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null),
                    image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null),
                    image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }
}
