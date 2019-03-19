package com.znv.fss.common;

import com.znv.fss.common.utils.FeatureCompUtil;
import com.znv.fss.common.utils.FeatureCompressByBs;
import com.znv.fss.common.utils.FeatureCompression;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class FeatureCompressionTest {


    public static void testReadAWrite(String[] features) throws IOException{
        FeatureCompUtil fc = new FeatureCompUtil();
        FeatureCompression fCompress = new FeatureCompression();
//        FeatureCompressByBs fCompress = new FeatureCompressByBs();
        int ava_len = 0;
        for (String feature: features) {
            byte[] rt_feature = new Base64().decode(feature);
            float[] f_array = fc.getFloatArray(rt_feature);
//            byte[] f_compress = fCompress.compression(f_array);
//            int[] val = fCompress.decompression(f_compress);
            byte[] f_compress = fCompress.writeQuanV3(f_array);
            int[] val = fCompress.readQuanV3(f_compress);
            ava_len+= f_compress.length;
        }
        ava_len /= features.length;
        System.out.println("avarage len:"+ava_len);

    }

    public static void testCompression(String[] features) throws IOException {
        Base64 base64 = new Base64();
        FeatureCompUtil fc = new FeatureCompUtil();
        FeatureCompression fCompress = new FeatureCompression();
//        FeatureCompressByBs fCompress = new FeatureCompressByBs();
        List<byte[]> farray = new ArrayList<>();
        for (String feature: features) {
            farray.add( base64.decode(feature));
        }
        int loop = 10*10000;
        long begin = System.currentTimeMillis();
        for (int i = 0 ; i < loop; i++) {
            for (byte[] f : farray) {
                float[] f_array = fc.getFloatArray(f);
//                byte[] f_compress = fCompress.compression(f_array);
                byte[] f_compress = fCompress.writeQuanV2(f_array);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("压缩%d万次，耗时：%dms",loop*farray.size()/10000,end-begin));

    }

    public static void testDecompression(String[] features) throws IOException {
        Base64 base64 = new Base64();
        FeatureCompUtil fc = new FeatureCompUtil();
        FeatureCompression fCompress = new FeatureCompression();
//        FeatureCompressByBs fCompress = new FeatureCompressByBs();
        List<byte[]> farray = new ArrayList<>();
        for (String feature: features) {
            byte[] rt_feature = base64.decode(feature);
            float[] f_array = fc.getFloatArray(rt_feature);
//            byte[] f_compress = fCompress.compression(f_array);
            byte[] f_compress = fCompress.writeQuanV2(f_array);
            farray.add(f_compress);
        }
        int loop = 100*10000;
        long begin = System.currentTimeMillis();
        for (int i = 0 ; i < loop; i++) {
            for (byte[] f : farray) {
//                int[] f_compress = fCompress.decompression(f);
                int[] val = fCompress.readQuanV2(f);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("解压%d万次，耗时：%dms",loop*farray.size()/10000,end-begin));
    }

    public static void testComp(String[] features) {
        Base64 base64 = new Base64();
        FeatureCompUtil fc = new FeatureCompUtil();
        FeatureCompression fCompress = new FeatureCompression();
        List<byte[]> farray = new ArrayList<>();
        for (String feature: features) {
            farray.add(base64.decode(feature));
        }
        int loop = 100*10000;
        byte[] f0 = farray.get(0);
        float sim = 0;
        int cnt = 0;
        long begin = System.currentTimeMillis();
        for (int i = 0 ; i < loop; i++) {
            for (byte[] f : farray) {
                sim = fc.Dot(f0, f, 12);
                if (sim > 1) {
                    cnt++;
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println(String.format("比对%d万次，耗时：%dms %d",loop*farray.size()/10000,end-begin,cnt));
    }

    public static void testCompWithCompress(String[] features) {
        Base64 base64 = new Base64();
        FeatureCompUtil fc = new FeatureCompUtil();
        FeatureCompression fCompress = new FeatureCompression();
        List<byte[]> farray = new ArrayList<>();
        List<byte[]> forg = new ArrayList<>();
        for (String feature: features) {
            byte[] rt_feature = base64.decode(feature);
            float[] f_array = fc.getFloatArray(rt_feature);
//            byte[] f_compress = fCompress.compression(f_array);
            byte[] f_compress = fCompress.writeQuanV2(f_array);
            farray.add(f_compress);
            forg.add(rt_feature);
        }
        int loop = 1;//100*10000;
        byte[] f0 = base64.decode(features[0]);
        long begin = System.currentTimeMillis();
        float sim1 = 0f;
        float sim2 = 0f;
        int cnt = 0;
        for (int i = 0 ; i < loop; i++) {
            for (int j = 0; j < farray.size(); j++) {
                sim1 = fCompress.DotV2(f0, farray.get(j));
                sim2 = fc.Dot(f0,forg.get(j),12);
                if (Math.abs(sim1-sim2) > 0.01) System.out.println(String.format("org sim:%f, compres sim: %f",sim2,sim1));
                if (sim1 > 5) {
                    cnt++;
                }
            }
        }
        long end = System.currentTimeMillis();
//        sim1=sim1*100;
        System.out.println(String.format("比对%d万次，耗时：%dms %d",loop*farray.size()/10000,end-begin,cnt));
    }

    public static void main(String[] args) throws IOException {
        String[] fs = {"7qpXQsFdAAAAAQAAtS8fvRkaV7zwvhG9/i0uPBvNgb0S2aU9hd2vPVyERr77+4O9yqDQvXKGBz7w+nK931vHvLlbib3OGTc8v6A/Pd/KeL0iL1U7oOw1PPtU67zuSj09C/oyPi3EkT03tyA9nO0uPt4zD71saZQ9EoGEPI6ntT0TtZ48vdcUPhUmCzw7pva85YVwPQSgQryP76S9pyi5vc0NWb1lmNW8uaoZPaOlFr1es889scWQPY0wBb2y4z+8Bc/ovUcw8bxvqi09rpMSOgspgTxNWYC9qEe4vS16aD1jP9k99G2uPOKKk7zvB8q9uy6PPeMX8byZQBE92faMPagi5ryaSe48lrCZPEqL4js31Ak9oMAHvSMr7Lxm3w881HVMPX5gsL07jTg95Nr3PI6HjT02ySe91GAvPRzYFb3Prqg96fOOPBzpMj6h+sm9sFOvvb8KVju1TEa9eEWQvFaKg72UKSo9Yx6XvZjqBzx/wxU9g9wHvYbPlD0BdPE8WWeoOxc1bLxhqhK83uhKu/fDQT2CAhm9YEuyPOtY7z3uXL27YFcEPea0jzwZZIQ8KZ8LPZUhAzwZbas9sbEUPls2pjsr6pW84hTMPRoge72V35S9hXM9PW4AVb38VwG+XbHFPIV+ljtViFq96TvrPFhKr704PIw7zWLAPct2hbsANaI9FKiqPWpAWT3ts8S6+wghvCjYN7345qO9qtogPectCj3lcX+9xuPMvO7qvzzSWBa8aDaLPMoflj2wA3e9Ph6UPV/EIb1qSRS9mAcQvRf4aD3fxUO94kL1PMY4xr0ILLo8ZOoOu66xN73cShM8pJOsPflNPL0AzCq9D9ctveeprL3obUc97h22vG0Qnbn2dKU9KsmIvG2wNr0SapA9PmspvcRfaDxL/Ii9QTsSPAaNZr1P48E7oCnsPSfVvb3h/lw96lrwPdp0fz167yE9ySNnPWzwiz37PdQ9kTIBvLx3vz3hYDY+5bxfPPSMjbwte9m7QiqAPIlt5b3eqns9OYM9vbSKuDzIdYe8Uza5PBl+Jz02RiS97aVAvTttjz1hqXU98RezPMFzv73F0LQ9fJ6cPZksZbyIiSS9H0L+vLqkgb3NBl88N7+2Pcujhb3nHKs9F0QevvoGkDznraU9GdKqPAXvqzw1wbg8O2YaOzu6ND1twFy9K+kWvW5XObzk3yg9Dnyzu9q7trt24uw8eayqPXXtCz1Jgik93sygPR3aCz0mLQW9/LdjvLQ5Kb0/ZR09yuEBviq/m7wpccy8miGTvYx6NTpcqms8OIQAPtsRzr10B6g8YiNVvTKi9L2IIj69akWDPZiTb73W/le9tJxQvfHykb3EMYm9KseAPVFZ7rnu6I4903eBvQ==",
                "7qpXQsFdAAAAAQAAqm00vVxvE7yL54O9gMnVvR3MhDwFVfQ94gKzPS728jxct6g9nIv5u6uO57ywq8I8ancPu2jTob0hlK891S1sPQsWBj1EzTW9qqa2PIYmOrynOt68esYGPqhzej0Tplw93vX/PF2CjLx42MK8UePyPNA5SL1WpLa9uN0DOxs/yz1mxqg7VfnKPKf3rr2r4yQ9NvUZvi6wJLy+z528UUk4PNohDT4NZKA9YXwGvWhmM7u+S+y8jS1vPUbmED0Gi668G9SXvT3QnLz6h009JBprvbwalL1UwJA7K3kYvYInAb13XIc9yJEqvQ1f17xhPT29qTU+PaS/Ej3oEqY9ArYxuw08HL2jE6C77y5evfxgh7yeHTi92k6rPY0ArDw4Ula9cjJxvfJrSr2pd1O8jbc7vD1Er73JW8o8yHhqvWZNB70N0B+9R5V+vc3Vs7rHhL88S1EGO1pFhDyqSSo8dpslvoGls7x/Ohs8660kPR8OJT2dzQG9hxUiu0WBsr00zCA+ueUNPgU1QLzePPq83wrWPX6zIT2C00w9071WPakTaz2SFAk7bBA6POsPXL3tP8w916hovBeN3zxRWxg9RHHZuiu7gLzK1BE9EP1XPXq/oT2WRh49SU52PFSp1LkPeGC8UnKSvHIDZzxaF6K9w6TOPI7uo73NAd29TuBMvdHSNb478Ma93FNvvQvjGT6Wbjw9dNF8vWQEtju17ok9KiblPUr0vr28zuA852gvPav4Ij0NPHa9t93wvIT1j7xu6LO8BoryvC9P77zJxDw8TeuRvYzOxD2gF+S9sUVEvd+adbtlCwK9urVWvG7U6D12GOm9m7uTPQBmbz24jw89UwSTO5gAXb15kFO9RGH3Pas6lD0ZDRa7IpltvFbsi73YESi9SromvbIqBj50z4G8DMOEO9RYkb3FbAY+2TRKPSywabzu3Ak+r5hIvaeHXzw3U289KI4uPc6xND0+rxG9EiCcPAEOFj0NOgG9HYuqPWXTcL0/CL+9S0E1PdhRO7xDdIe9Q7xVPbbHqj3Y4KI5oXEMur4Fc70BTzC97c9NveDtVTrRlbM9hdCLPD2GKz2JREe9JLNEPRm6U712CQS+EGq2PSp3xTzLEY+9vKmQvBAxFb11fuG7EdYTPQ8zBTwxxtG7n18DPGXKezqkENY89bKmvJ+BeL16ZgC8mvldPbZrLr1YsL69IjmePNZIBb4Ntdw95LkVvFSRW70SzQM8OWFjvcYPbL0k+FI9lUEKPcdH1z0neVU7eM5VPTDDrT353KY92ZxIvi74UT48ozM9IRxoPaPIET3542+8j9eMvVLhLr1pT4y9Rq2aPHd2nTyukXQ90LgDvGEaHjzrs869LeWKvA==",
                "7qpXQsFdAAAAAQAAOhEqPP+V5zyi/fs94YdkvdFgZ7xU42Y9EfXmvRpEwDrLpt68aPAsPX5gTb01Xye9VnKtPG5cBL66LpE9LP78u2c/CD361ic96xXJPOKhhb3ksRs9sQrBPD3S+bwemSm9Lmi2PdEfpb2swRW9QgMVPQ03Zzplcmg9zLsAvFB5Sr1doHo96XEkvaXWAD0C5Nw5qhZkPYSBkr2R/UE9JSQpvdcyvry1bRq9t/ApPGxlMb3Vjak9tZ0QvvgAgLzUxD69NRHIPS59ET2D7L89fdOEvVw8U72rXZ083tAzvUQdkr2argO7ToB1PXfNFT5m1aG9MOtXPevDmj1YMnW7BzESvRsLBrwhmbi83DGVPLdSe71DZQO7VdrCvdEGq7tS/8s80qnEPNQziz1MoAC9eO81PqK/kz2xqNg9eQvXPfPgpj02eXA8wxyyvFDglb256NY8p9W0vGPzQ71u3ZO8qtVjvdUE8rtxZdg8hn5Fvac62zyngNS8waO1Pao1z7xLqe49NJ8Qvdd+yb1YWom9M/n6ueG6sD3VNDY9bu7ru3RaC725Pew99vN0PV6dkD3Y6tW8gY5Mu6Z2s72W35S99cBUvVydBzzo88i78UVdvWeXq7yuIps9pdjyvI15sjtvGgQ9ss/dPT65Jj1i9wY90JIfvdv1dzyrF/u88gl5PfMQWj0xHCU93lotvrEwCr1jyFO9osVQPRIZEb7oz9281nvgPVGM9Dxx0DK97QcZvNE4BD01I+28WQ7TvQxjxD2c/x0+XGKJPWYZ2z0F9uS8PfFuPWWGD75t9Ua9zwCmvSjJJD0ffg294ptqPSUmNrphQ5U9rQPhPOHpCT7o2pe8Dk2sPaGnuT31UsG8dcx2vZ/JDb1KkqC9Q79mPZuOeL39kFI9yzQ/vUuW/jy3A029LPwQPdOhK73/97A8hzjrPG40ML6AmWO9XRpuvXSGAT2UuTy9p9OgvJpfOj1Lhaa90pRkPYT7fTtxYuE9yopzvYy0oLwesrI9VbEhvV9J1b1294G8pQo1Pb/i6T0AGoW9rmuzuhHxy7z1mAA+OKTZvVcfub0woAq9EWztPTphwrwJyJg7VcJ2PewGD73Zqkm9gzWWvOIASj2PQRS9dDWBvQp+ET14Tp492Ea0Pb0jzDzlz409uIRBvfdg4zzbF469sxYZvfUgmr1Qm/c8LbeAPQZIcr2O33c6X5X3vSzqEDv4RRs9t+KxvFptmT0fruG75iXwvBN12b0VHbM74E9OvN1akzykHBc8E3ygPfLyGjzJqkA999HSvNwDNL3SwpU9qwxEPbf6LbzQ+yy+Md7wPPVTDz2XQxc7Ow0Wu6aV1byAK+C8W792vPb897tx8YK8I4lRvQ==",
                "7qpXQsFdAAAAAQAAvnWgvBNxATz+lGc9V6GyPDpS+rsSQ+u7hsrJvUSfED1CoAm8rL5/vQNaAbwbf0a7g87jPeZfCj3CzbC89ZBsvGeWRr1QF3a9iHcIPcq/8LyYjUi9JnhGvfQWr7wDixq+rNBQvC7sj72VLbu9tdaBvb6RPz3nvWU9Hxg8vcSYS72NzS49qvjJvJpqm71IdSC9qIoXPUy4wD0b4Ow8psuyvbFb2j2/Vi89xxRePCCHWbxD05s9an38PPgRUD0eZBS9ZP7HvDzkqT0EEzK9r50AvUdTsL3WTcA92dAUvZDg5ry4t4K96yEOPf7l9zyHaPK81GjuPHeJ5TznlJ496LQrPcOdXbszOD887SdGuzstlrwhufs7HKRDPVuZhj28XQM+ZtcdPUvRtT2xAbW9dpoRPbXmh7yb2AI93tfEvXg9hz3CS3w9aVF+vbijE7xN1xO9fd2UvKpp8byhtjM9rgyNPZOjZjuR/dc9IoEAvZvzSD1UrLa9RsOSvbALFrsu6g48SgV9vWNug71gJgm+XH8PuymY5b31Hy0+eaXdvBzeF70YNbk9ibNFvEhnbTxSv/M9xCkqvFKbZT3doaC8PpLnPIpa4bwflc88bECcvSpfX7z0Iq69RwQLvjyWzLulBxm9Nu/uPeyjtT1I61y982KTvYGEHbsp8cq8RduVvHYdiz3W2bg97DSuPAJMvj20S7W87dyFPFIcQ7wGQWm9yNHUvUgpxTyfbc49IpV1PfIhnjyAyqA9fRNEvEUQmb0l+Yu9hq8IPoLy2zy3HNm9pIFXvcEV+b1Ykl+8DCg0PYCElD1RTsI9BowxPZEEHrwzbOu9eQ3XvAdQGT2BTrG8N5rXvZb6R73nRMC8ycZ6PX1fozyjmIM94Q/0PPoF5bsFsQu9SFmsPOBSV70fsz09YN8uu0T20jsH9dI9gdaBPZkS3L1saGU9iSrZvMF2Vz1AqI893iY5PQnqLD27/kq9XRUpPpHVz7uPMc66QZ1Ou7iyGb4fnHY9OmJCPXsJub1V95W9OfuQPfzvZz1yhpy9tUsUvY4cEL5i5Ui9h1EPPsMLl71imZ89DfMnPAE1CTt4MHu9oV6EPYy0dryyubO9AMqQvUf9ZT0To/q83PRIvJnJJTvB3Ag9wZLAPTG/XLsR5as7mkrmvNVi973HHz49n/58PHsWt729mau9Gak8vUzdKz0wCIA8DyaPO030hb2klw28vbCWOw2mPL3Qoc49AGt4PZOI6j3Aj4I9BkGsvRQlp7uX+5s9O7X1vdmjoL019qU5kffXPS/yujwbguc8rj5YOTg1XDwtC5w9FsX0u0BBA7yfeIW9MPsUPYZk1j3era+8yIhLPVNzD7xXLTI8wUtIvA==",
                "7qpXQsFdAAAAAQAAwksoPTd8VL1XGFW8J/qYvQpnsD3UxdA7sdowPSxRqjxrtZm9ecGfPYt8Sr5gReA9mNNJvZR5mT3XBgA+nAZRPICbYDyLpyM9elmOvOZabryU7gw93xldPJt3qjyhXxy91ounPXPaEr3s+nm9sm6cvVhFAb1UnUO9BhugvXAvWD2nUv+8QfU1PJ+EA76PeQG+DWmQva/+4rtIxms9kToKPVTPpLx5bIM9RkoaPc2iwD1DCQ69zDFXvSIv7T21DlK9NNuPPQEVg7t6xbe83KA3PSeDt7w2guK9Sm92vQxMZ73utAS9f4aMvZz2MT26Sna9PyFQPYrRRr18DIO9RNa5vbkUiTyVIR69croSvaYK2L1yzXe9BecGvRwHAr4dNui8X0gSPV2l1T3nDps8IogTPQZnrrwz/nu7DibgPYQg0jtYoUE6HgHBO0ha5zxlElk9IhpKvbSlybzwhIG9JOVJPd0wa737tew8Z2ukvQT4gT1WNsO9CAWRveSTbz20dxI+W0edu1Uxyb0ply688i0kPbkEWr370lI8rf4AvjH0hL3VJxS+c4JBPQ9rgzzq8bS8C7PuO4/BJL2yDXa9ouwHvadc5Lydl4e9TOPmvJwYerxRU1I9slkaPEChpTxyZ7Y9hf1YvVnoGzubGGa9EUG9Perq1z00buY93VQSPTZJhb2Ed669fOQWvGxdtL2UvCa+bsBuPP2fbToPKT+9TM5IPJ+tozzbXI29RQiZvQ9E3z1p07I8+Ll9uuXVfb1JACu+4HD8OxUm4zwBCtO9hHrevBW1Bz0dhiE8pEObO3z9Oj29izU9fmOVOyZiaz2UvhU9lKFivTqXjryrpAC98+6EOwGuqr1ah6w7OdH3PePzcT1OQ/C9tgFvPWqyfLzQIiQ9ZqJgPQF6+D2zaGq9MZOsOIldNL2Y06K8xc0/vVsAkr3h2GI9FoEovfKzAD21LFe6BOR6vaOfqT2qJgW+KPY7PWHvg73Ag/O8ms6evX34irwWdK+9ujWeOmdsVr1IB+287IrzOgMwvr1NTVI8+Vmnu4fNkr2FIWW9rQ+fPQ4DBb5u/gu9BmYjPQrOobzev4W9r0lavbNurr23Jog9VcyfPSu5lbzkU4o9ksk+vYr67TyVrlQ9syQwvNUaiTwvNuu9fFR/vXPeizzq64098vkpvYipELzi/ja9gv4Yu9pllz0elMy82G9YvPRNcT03ERk9qAnHvYaPmLxNEc087KyDvLbuWD3RT6Y8UNTfOROfPL2YXNo8IJwwPaIagD0IpCQ9o8xWvVJDMr0r/6y99InmvW7E7rts7ZO9vVFCvCbG5r3rMc68MWwKPRBP9b1oMIG9fcWXOhUxVz1aKAG9m5GnPQ==",
                "7qpXQsFdAAAAAQAAzqxnvc4nXT3HEs07Af7KvKqGdD3lmSG9J4Yju6XrDz0c7zi7QrXXPZWTTLznb5K9meGRPWvDo70gh4S93nibvYflIz2pX0q99mY5vbWwRr2BXTA95aZXPX4UZr7nuzS9iiIDPt1A47w76EM9CeFKPGKa8T29ZoK9BACevW0qrDyNGkM9tQAdPoGfhzx786+9RXekPD5nFr64OZm9ojWovVXgyj3jrYE9hOafvQgy0Dysq7Q8K5/gvHtinj3wrbm9HxKdPQJIgDxEr2O95FwFvJLQdbxFCgc9lo/vvIv7Lj1fmga9S3FbvVFUaz0WeSW9kk/UPSnbtjpb78I8RLKDvFLmYzyS1AA+pJTRu+EFGD7gWzK9YJKEPEwUMb3GSgU+x2mlvQyW9jvhMps9JqdsO9WSAD2avkm9zv7dvDdNjz1dyoA9L315PLcSqz1+ufG8dRcfvfvmWTx8Bxc9KMetPDKUurypwfe8fzRHvacDmLww4oG95PqPOupUqj2196q9NjN/vd5uSr3TbLu7aae4PdmCPbxpamw92qVEvUkkJLsUUxM91PL/PS5PCr2CjI29hZh3vSBA0L39fq49QQoIvc7huTzQmLo5L73ZvBffhj17cbK9GIGKvJ5eID2zyIu8zedSvNEi6D3HbB4+qE7hPfoJDb1JMdM8iGUTvXI//7uH2tk8FjDQPcdegL2pgIY8iZFNPY+Kyrs6AUG96peMPOkBlrz2ylg7IRxhPWKdt7sEyow8LgxwvedwirxXyhg9OZv6vWKGlr1UFYY9c2cYvWvkz71ocB+9iQ8gvf4Mmjxgo646SMRXPVdmHj2TcHg9iPgVvUDgn71J62u9ryjLPIHYmD1r6N08nHFqPUxkWj3wUEU9haIUPeykh7z9IY+8U/avvbCtjjxoxLa9VIjKO+nDVbyTnRQ9KaHrPOqCorzKBhm+wwATPRSPELwNdcc9YdfdPbUUUz2QZiS9/8atPO0zFT4uTuC8y+gdPSESir1bsvw9URyHPOvl3zwFxH87aJJiPT41B71riGW9npJtPLg7MT2sah29hEUbPUxgFL4BSPw8cm5EvO0u4TwHXG89UDg/PVlbYDyRUKa9SIZ3vPrZgT0j6uS7/8obvd07gLwpcEO9lX4SPdCrx70smU+8fqkYPmgyMz3UJCA95c5AvFpFqb3HvVW8QkI/vdRZDj2HBeg6pP9HPLll+7xmolu9obycvajNOz0YbG078qwRPqK+uz0yHo+703sEPYL9h73+p3o9SmTCPU2WMr2YFmQ8A+IqPThgg704pAC99aqvPITvOrxpEpY8vJ8xvnkUEzzNaH09O6HVvXu3vL3rT1O8gYSVPKl7pr30AVu71Y4zvQ==",
                "7qpXQsFdAAAAAQAAoNNWvJQttr0kwf09QrJovWtAzL3sZK88Cb4kPdMHFLxbID89YRizvYkdpbwxUri8kPuQPaQ9yTyAmqY80qzDPc4bxruH8sW8BG3QPZyxmr3+z5I8jf9yvRM7Kb5deZ09ACO3PTGIy71/Jaq9qNnMvD69zrtkvIe93LzbvLPiIb5Hchy8eg6XPX8BCD30+A49pUUtvqVbrL2ai8q98uxLvaJN9rwV+Oc64JR6Pd4Jlj0IdbY8pUPMPHd48z3axIk9RCypvCc4h7ycwJ68RJycu10vAj2JgMk8amTOuxaCQ73ItZQ9FXX0vDHunj1Flp49phQVvLPI0j25Hvi9ZTBLvX4Fdz2xSqo9yIMHuzNZ7buWapM9Oy16vamE/zxp5q+8L32wPJ7rhL2lRoO88i2lPQ83Er3kSYs92obOvDpo5zzoji68EZdSO7ULyr3kBYA9W8jMvKBytTmDVzy52CNFvIqO3Tx1M6k9z5YBvTci6z2u5aG9RDy4vQCgbr0otYA9TjOJvPgMNT1XnXi95RG/PSCdlTxZB/09w3HyvLGZqb1SED89kZy2POasyz25jqG9ViDGvWmjAbyUM6U8zd8Vuv6brT0zNWs8za+HPM44jL2Zlq48wNkfvLK9mD1hcRW9D1iOPX45P73927Q8v9HPPV8Wjr31Ejs9XwjMPSEmNL3wRM68++b+PcGbcz0P7Q+9QwqpPNIYrD2hDr48nGsSvZcAyr1oH1C7pVsdPZxqwzuVwJm8Evj3PFAcBj7nzpg8SNn9PO+IET4iPTc8uCZxPU9Q6ruxwj09ktgRvdZJs71T3cS8tpP4O8ZPk7xuIp27EmWEPYK/kbwGa7Y9wllhPW6GMz0gqQY9M7EJvXk1zL1ITYg9cBdMu7y1V70kmpW92UyFvGulYr22yOS8L183POZe+DwgNQE9igyRPVBxHD2I44S8W+RxvXSpCr2/XI89FWvkvDYVtLx6UOG7o26CPSPu2L1SrP67hnPWvCv9y72EOco96akCvp/slrzC+o08RuHtvOdsDD6J5w0+QhtLPeTUOT3onUg7PiA6O8rMALvEQSO+7DW7vDjjqr2vOgW+y/t5vNzQMLz0zii+M/mnPYgKBT1X7589X8mevOe4hL17HM07+ZPjPYOJyTzvKP68fqHkPCLUp72CE/a8ouaCPVAWELz1yv4858VRPYXkBzwbfHW9Ti8SPQzKyL1yQrw8AS3BvR0Kqr3uZ6+8+XpnPKoxxzwGtiS8m1lGvWXieD0CHKc8j1ApPV3amTwEvpE6LKsXvQ6Fn7yoW8Q9t3i2vdNQuz2Or8e6tNqBPd/0CTodLYC8gyyTO6b1tj0ZHiy7dvAZvdir2jys/Rc9B7R3vA==",
                "7qpXQsFdAAAAAQAAN0LPPOJK5714aX874L8vOpvxET3V1kw9hB9bPfpjqTzv0cy7uv4yvYYOsrueGPG9JWPOPUAzRT0O+d48gUiHvYLmxjy+X6S9GiQIu+CGXb2uQ1A8YEvevAoci7xbb7m9Fs/iuMUrQTuR6gw90NGqvVm+VD2OU8480HoKvdpV+Tynqzw8t/BHvYfgKL1R3vC8YT42PUdg8b2EW7K9boRrvUNVy7wImtg8Nd07PSzJwr0wigI9ns6MvfPvj73HS8s8tieBva5cDLwo6Wc9x9toPV08pz1/brQ8aYAYvZW4Sb10I+Y9zz2Su2nc57x1wQM9KUEavqKArL1/z129WxQaPrZBuD2B1oC9hLK9vdGz3z2W1rq8eCIwvV5CG70sTpM9i23VvYMZnz2bmJO98kHmvVswh7w7vqy9O49+PNJRuDtDzpW7tmm2vStPMzyfE8W8XmHXPCLMrr3+oP09d8gcvE1s4TzqZNY9cza2PIEcij2hpOi91U0ePMtz4rvOvu48w1BOvRCIqz32tYC9JNMCPXmYnjwYK6280hUyvV31zD1nv7s97WsxPQMFtT1SeAo+0jUevkahMjwKLMg90/OEvNzfob3MoKE8KRw5PPW4VL2Q6Du99nOaPRxo1zzC7Do9n+ozvfOmXj3rD0U93nC9vayhDjwibps91UZsPaFej71NleU8o+CuPR8mwj2Br249uIFLvdJbFL03jbQ8XW7qvL3t1T1oQdg83RfVvSuA/j2fQRs9U9MUvJba27tCzhe9OjDMvNE7eju0ASm9PTj6O80lP71qPk28a8gPuwkxyL1jYf08FACNvG6wPL30keI9RjNKPe21oz1Qx2U8oWsEPWRN1bzmnxM9JVOsvbE3oD3QsgS9IdcKPfYj3LxxtTQ8/ommPRN0hTcmnfS8WCpTvWVaNb27oHS8WNl+PdxeAD0JVtU7h7IiPU4ahj1VToI9DQhwvBRRqj3MJ748EGb7vWE7uLxI/M69Fj9WPQe0Lj2+kGs7Rh3SPWV4Gb08RKA9YkKHPQlUSz3gqM075+xuvNs4hb2mT+E8WouHPfvK7bxpgOg9raTBPYo4yzv1+Tq9Z85KvSfe9rrrFRO+IFmqPcaAaT2N0q09ji2+vfh9uz3km7a54HEDvZzqyb0PqyW9OGjquzpTo71/7Cy80s/RPQ402L1WAuK9fbIAPb9wyj31og29VXMTvaZwZr0KOLA9RAErPfNfQTye1TG9eisJPlRNZ71K2f69lx6ivXO/zLxNu+O8x9qLvQuDkb3C6W29UJeSPAwMcLuXKwc9DLurOxwVOj05BIW9pu8nPd2QtbtPzYe9jYLZvVCsOj2cYTW81c20O5l0pjxAN3u8yEl3vA==",
                "7qpXQsFdAAAAAQAA8UyQPevL37xTdSC9n/BxvbDb4zwbKDg9c0m2PHproL2l2Qy8/YQHvbdj871fO6O8TwEaPd69i70wlq49/1M5PVkauDyHENo9lGS7PPqvS732ZNg9BYMpvWlFobwrILM9cNyOPbfdJz1Kkkc7iASVPRJGAz0yC+q8SWWRPckXyT1+PI89+wnVvJCxiT2b02m8soU2vDGJy7y3IAy9XmDEPA0xjLwIJzS+2NmOPJq2grueh0+99N+DPRC/4z2C/fc8ohK1vWA6yT0CjoW8CDstPVixZ71nkka9b8xMPQymGruxaQW+x4IrPXL2CT358gI+FN7xvZJzlT1LSS29YRqpuxJ9M71wqME9Op8SvTneKj01d8A86A4XvC8CZT31ysI8v6uMPTT1mL36tQO9x0o4vaffp73G8oI9yqaHvQizFb2ARBG92XJePJxeCr1WN2g9nNXfvPfA8rxkIyU9dUR4vOeZhTwSumq88SzEPSBboDrr1UA802meOybBPjstvh89FKKFvdgIQD0SF7a8Z7ykvaAKszxzCKk8PASGPIspV7zJXio8py11Owkq2jzAd5E85ulmPYN7SD0vqpc99U1hvZppyT1O4pO5UJlXPd6lOz0mv749Z//+PIIfdzxTbcI8dZKsPXnJnr2WJwc9lzW1vbStkb1TKBg9LdZqPMxlXT15XmW8ZRCZPUv6xzz505y9Z1SzPWXloj3r+ua7UujsPMcgkb36/d69h/IqvdOQBL1eU1Q9v+CNPSfYPb2+0P69ZWZmPeTvRDxPGya9juYbPnmSX72RSXq9AukXvudR/rxMifU94JhjvdL4gLyBexY9NnVuvM92lz123Jg9qQJgvIjgE75Ams88YCc1O44wiT1snYG9POVYvDMvRDxIRjG8dQxtvLAUlr03bGU9dMlQvQUqJjs+OpQ9BSRPvfeWELzBtM880ZU9vKGBqbzlbh49EemMvdOvgD2Pkg29H7B9OuDgXb1aEDe9yQdEvS9n0D0vhLy6oT4mOzLSUD1fZ769EowfPYFLUT5ULLI9IjOfPZuLnzqn3sS8GIKiPAJd6Lx31za9wd1OPVdrHb0DKek7W9UEvXX6AD1Gs5C8XwWEPffSTz14z4w9uSpzPe0Cq73HcpS92HrZPaICkb0Khpu9s0+GPeJ6zr24O0i79M/SvIW8Ir2gwWQ9WNjuPQDiN7uAZWu9GLlRPawBob3wCma9BrR/vSWuGb1ewMo88rY3vdXbJz16oqw8AWf3PPcLKz0TdPs9wLIPvZKcjbykExY93sDTPIDJez0/i8U9LVAXvhJQAr7y4I+9aqwMPQNjxrzG0yw+r2tSPbNmFz4efbi9JRh5vdexXrw4/ME91f3avA==",
                "7qpXQsFdAAAAAQAAmBsLPQOzg7zPokM9sDhsvJUZITznltA9AJ/zPDj6nb16GS89bFUVPROUGr0Hoy89bNELPtBKdzwMtRC8WsMKPZZc5DqJApc9GCFfPcRZST3rxmY96f76vHnzabyms7A8RFQjPeI7qD0XnY09t9IsPWEh0TwrNK49e1MkPT7LcjxyiyA9UoHIPDKslD1AU2E80tadvLrBrb3BhVg8cjTMvcIOzLwdkMW9GUxdPUOduzwhf6y9aRF8vJTdAj5R3hI8wQkDPAkSQD4+bwu9rLyIPX5juLzlcRw9QKTAu2ziNTyKodK9D6AivREvdz2lYO090usrPKXGKL0HFbG9uDXZvfTM/DuRNvU9clkhPYDtirz9w5C9OGpLu3Wamj16hXQ9Caa1PODGpr3QQ9+9L29SPBbV4LxBL1Q92LHNPSvbLz0nYVy96H9OvcS2CT3Y5kI9ctKCPIBaYb0bexY92tOOPfuhsDpDJaU8KZWvPPKoLz2DFYg9t7VYPf98pj3bgSw92KKPvAKyczyzrQ08sHVBOsGKxTzO9h49YxOLPWcbGb2mCsW8WGm9vFOeRT3ZiOm7nmYKPKOPyT1KJrw9oPstvdQj3z2Oa2G9uB5GvRj1Ej4/i968gALAPCJjwzzyD2C9mefOPak+BLyO/8s8nMtfPaTXWrxrwQ0+D1ISPbnWmDyr4Io94zC2PdVSAr2AriW9sEduPZBtSz0ji3U9gjYXPX3Dqr0miQS+ls0avVPfALyFg2Y911odOcLlhL1EK9e8NbrpPLMTJz14qzy93x32PbZvzbynk0O9iJE3veKwLTznFhI+GvC9uqBqzr32CUs9LyRoPPNsdT24e2Y9c3/yvDCVJL7CxzY7mS8pPORiVbzks/C8soqAvQ98fD2RVoO92Kh7vDCRKD00HCo9s9V4vEcIz7yJgrQ9vm6/vLn/6bxetXa95liXvAUXmDw1z4S7fM0bO3d1eD3GDQY6TBLdPOhqOj0ALc88QgICvo3kbj1NCUS9Huj2OxUqhbxI7c28dCuAPVk5ND0qttE9PBfOPUvDP72xLIc73F0VPM1cEzxBFey9K6yNO7cQn70p0wS+/UCLPSmyH73gK568HSbzPALXhz3Qmmo9Tyc4vfcA/L082CG9MaEvPkDVCj3nqiE7JdyXPbS6B76/HRu9iLfIOS4CWrwjTKy7f84BPQM+pT0O8069wHlLO+4Xgb3+zlQ9P/TrO1sA+L0SO+k9p68HPpa4QD2Db4G9w9KXvJ2Mub2fbqw9e9kGvbw1Gz2h+Gk9ek6vPXPbfD0PNZs9DVvIvMgggr2P8oo9iKIiPX+kDz0xVgk+BLjAPOzYkz2PoGi8i45RvcyzjL2vzyo+3slevQ=="
};

        while(true) {
            System.out.println("1-测试编解码正确性，2-测试编码速度，3-测试解码速度，4-测试比对速度，5-测试解码比对速度，请选择（-1退出）：");
            Scanner scanner = new Scanner(System.in, "UTF-8");
            int i = scanner.nextInt();

            switch(i) {
                case -1:
                    System.exit(0);
                    break;
                case 1:
                    testReadAWrite(fs);
                    break;
                case 2:
                    testCompression(fs);
                    break;
                case 3:
                    testDecompression(fs);
                    break;
                case 4:
                    testComp(fs);
                    break;
                case 5:
                    testCompWithCompress(fs);
                    break;
                default:
                    System.out.println("参数有误！");
                    break;
            }
        }

    }
}
