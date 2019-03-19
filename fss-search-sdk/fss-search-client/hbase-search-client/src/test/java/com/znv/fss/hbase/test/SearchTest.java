package com.znv.fss.hbase.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.znv.fss.hbase.HBaseConfig;
import com.znv.fss.hbase.HBaseManager;
import com.znv.fss.hbase.MultiHBaseSearch;
import com.znv.fss.hbase.searchbyimage.FssJsonInput;
import com.znv.fss.hbase.searchbyimage.FssReportServiceIn;

import java.io.*;
import java.util.Scanner;

/**
 * Created by Administrator on 2016/6/23.
 */
public class SearchTest {

    public static String format(String jsonStr) {
        int level = 0;
        StringBuffer jsonForMatStr = new StringBuffer();
        for (int i = 0; i < jsonStr.length(); i++) {
            char c = jsonStr.charAt(i);
            if (level > 0 && '\n' == jsonForMatStr.charAt(jsonForMatStr.length() - 1)) {
                jsonForMatStr.append(getLevelStr(level));
            }

            switch (c) {
                case '{':
                case '[':
                    jsonForMatStr.append(c + "\n");
                    level++;
                    break;
                case ',':
                    jsonForMatStr.append(c + "\n");
                    break;
                case '}':
                case ']':
                    jsonForMatStr.append("\n");
                    level--;
                    jsonForMatStr.append(getLevelStr(level));
                    jsonForMatStr.append(c);
                    break;
                default:
                    jsonForMatStr.append(c);
                    break;
            }
        }
        return jsonForMatStr.toString();
    }

    private static String getLevelStr(int level) {
        StringBuffer levelStr = new StringBuffer();
        for (int levelI = 0; levelI < level; levelI++) {
            levelStr.append("\t");
        }
        return levelStr.toString();
    }

    public static String readFileBuff(String path) {
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer();
        try {
            isr = new InputStreamReader(new FileInputStream(new File(path)), "UTF-8");
            br = new BufferedReader(isr);
            String result = null;
            while (null != (result = br.readLine())) {
                sb.append(result + "\n");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    br = null;
                }
            }
            if (null != isr) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    isr = null;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 测试代码
     *
     * @param
     * @throws Exception
     */
    private static void getSearchByImageResult(/*String str*/) throws Exception {

        String feature11 = "7qpXQsFdAAAAAQAAN8WbPJREe7y31L28UTAXvSbOmjxRqLE9GlSLPd0lN7vcI+K7PHqsPM3zgz3lsCK86WBsPVbdaL2Q8oE8W2PSOy5yjb3FzAM90kNRPB7+gLu7Yam7OLsvu3G3GT003Iu8Zi7lvWhP1Ltko7M85u4HPRKCxTwDPlq98pyMvRAqFj5NBea9m2TgPQBuVT0+vdC9Xj0Avg0nvb3BJJy8bvsuvTpcDr2wk5G9iiofPRfsvzwRGrM9XVESu55D9L3bA529hCn2PU5rOz25uQE9GCrzvbjliDxzp6Q8kiVOvYNjNz0VO8o9XCENPDwZs72x6oy9iRUOvlG3Bz5/gbU80WXOO+MsaDx1IIk7FbITvUz9y73sJzm9iMMwuxdjGb2QRJY9oftdPSnPFD0yUZ+98lXLvbQKrb1PHVs9iolQvWrvgD2gFPI8O2mKvbQzlL0Oa+S7vxomPL9QlzwFQ0s94F0GPctiXDsq4gK9wmLMvfjJDryW5iU8+km+PLlsJD3ETp+9R2/dvfGVib1tUia9Sq5APa6Sb7yFl0I9mJ1+vUgszz26olw9fVOZPK4C7zzymUW8xfXlvRnmZLwN5LI9U5dAvULPWj1J8i++SLjtu9dmjrxPDrA8X9euPXdMCb2pT3K9URRAvL8vjbwtQh8+ZeE/PbaLpjwpnjQ9HzwPvftC0jw5oSQ9JgDnPXyZm71O0rO80FWRPf+ksD3AsIa9vO78vF8r/rxp/lw9IfhJveWbej1Jb8S8BVQ/PNeyNj0pTKq8gVOuvelqmTv8t3+9b9NFvHSNaL2sMd87+qapvYPZ572jgr48Lc1GPUW76T2Iz6U7NGoFPA3xWz28fw0+/RY8vYNKs71NSUe9hCGBPI7QuT1yTsy9pefGPdKQ8LxtRMI7lV1jvGHslbpFP7q9pQtRPc7+ljyDzVG934ZXPQ8VQzuDvFc9BUACvSF9Ez1gc5U9KW/BvEbtHT0HbrS90sGLvH1Pg70DoXu9abzKveC8iLzn2k89XbAcvGFBtL1fOq89NtK9veWTZT0l5nA8292SPN0XIL14jJA9gTe0vSuZMb2FTbM8WjFgPVwMsTz/iu89L9y6vV/PQj23NVI8iDxHvBSBKL1hEMa8DDIYvYc/mzvzbhK90/jGPfbEgDuk/+y9cYJGPbV4sLyC6Ua9uiAmvK4qnb0gL9C8uimePYuw/zwZhbm97sJ2vbLCTr08hT69M7+0vHQ7BrsMUgQ+UxtGvGTn4j3obVA9iPabPScxBb4uBrY9sabnvdI7qr0apYM8WOw5vdc0szwxx7q8MM6yveVcUbvun+S8aTCsvAY5yDqnYAy9HEHyPYMjXb3YiRy8HKbCPOZSXTtQ2sy95IvsPQ==";

        String feature12 = "7qpXQsFdAAAAAQAAlKYVvjgiMD0zwCq9lBnPvfhB7z2PGw07CU9yvZlym7zKeqk9Y4kfPGY68jy74gi+2J9ZusV2k70z5OW9VvMuPB7ISz16LU+9FalCPWHpAj2i4rQ948QdPGE5Wr0QUaC8C7YTvS5Hvz3XA1m9twykPaa6ATym0pm89UqavIgGqL3Q0wu9LJCwPVpgt734x2670XNpvSgdcr3wqLw9U4nwvHT8Lb3y1Rg90YbsPK3MK70vxIS9D3RQumzOEb2ZFde8VeE8vZHh8zvazXC9DcmGPWuM4LwZ+Fa9p0AlPYCQUb20tUk9zcQzvH1qYLyNHRM6tcE9vVwUMj5+2Dy95aFxPeRjaj2PZhg+WWgDPvhCEb3itTC+Gka8vdpyZL3GJm69EBKQO+8fzT3KFaQ8VAe1vJtVsjwXBTY9x/mjPHfOPD0VRPk8OCboPIi5sj05QyG+nECAPOOoa73cBuC9BggvvbimiT2ldZo9Us4WPXvACb0Yv3G9tfHtPcgDDT2de9q83IqgPDTKezx3epQ9dhYWPQzVYL1nHWK9z0DNPHRCkT0Lzh291TkZPZkbKb6M03a8WKnOPVriEj7Z4oa94eQJvAsdsDhrZMk93zEXPYxd/zxImos86ro6vccHJrxgIAo96fl0PVZUSz1vEzQ97WWWPGWxSzy7DRC9df5GvQG+KTyu6Q097jSzPPSMAb5iKrm9nYaGPCgKJD1Vt+M8XHsYvZOvMb14tgE9Zdg8PHvjYrzxrKU8ZSIYPeyl+L1Y79o8w1YePCxXkb2M8u49ICgZPUWXj71BUKI88SByvcSQAbuOOR49BxR2PWuh8LyavJg9fjNWPKz1rLwcw5y9GSTHPKPzc72QZtO9x78BPVOheT3bHhg9ObG0Pc5NsbxBrCs+AJHjPeOwO72fg744ZCWdPfNT1LxXdfi8SMIjPamzp72AH+g8xw+vPSF8ZD2efVw7a6IUPXesET3u/eW9bfzdPQaIIr27OUO9B6XcvNktbT0nLeU8scW6vL/74zw9LUm8JINzPeB9H7u06Ko8yRLNPLGuA75roje9+dIXPRyE7ry6r4K97lH+PAVejj23KoW84ij7vNydkDx6Jxm9YOC8vVkywbyAl8U9UE5bPPb3CLtPTFI9NAUrvUzxVbzkPAG+HZPnPPaV/ztmz9M9vvWnunYLv710PX89+eULveV3O71PVQ+9NjGUPcmlCz4I94k5SOCVPCbgADwDpok9/KRIvdOMgz23M5k95kbVvXY/szxzOA89v0O2PKP1izsAMD89isQQPfks6zzOzIa9hhK/PagRiD3cHT29slQqPXxAyDxjI+Y8Pf7pvZl4pTxzepY9apCmPeFXtL10jmM8aaI7PQ==";
        String feature13 = "7qpXQsFdAAAAAQAARXphveRBAz3K0iA94dLxvegByz3v5YW9n239PGyj4z2aGlK7TXplNmYfCj1k8we9AJ/vu/tMDL5vKOS8hbnivecUITw2W3U8gALrvbbdcjxwWAy95RBivSh70zwzTdw9UGTMPU4IWj2G66k7vy60O4zphb2ngfG7wLZTPYkI2jzPWji837hdvMZyqT1zjw47t73YO97URL3b9qo9Rmixu+t7KT5WpJ+8V6IdPeJBlLz+swE89ypePAAk4z1v4tK97SU/PULKpr37bwy9kTR6PaORiz3TXU+9e3boPIb0Pz6L4qO9nyGBvJi4h70nxDS+yv/kPPbnzb3Uvbg96xNJve8u7zoOUQ49GAu/Ow+wlb2JX7S9WaULPaEEqb0sxIM8uAY8PT0SvT1R8ta8LepWvSwLIr33y8q8tsH9PI1+uLt23929U2W3PYFC+r17BwE9y0wDPLRNmrxSNe890D1mPZQRH73mmj298AMXveQi77sUygq98aWZPSFqGb31CG69/XtXu6+UfD1k0YS8D8ihO63Gdr3gssk8r+n7u3rnZb0Qz148P4lzPRriij0K1jW7snjKvaQ+v711PDq9PoMbveFH372Pu3i9JMCVOu9Y6Dzlx3m8wuDOPLUrfL1ftLK91HmtvWy4zz205y0+z6SOPGB9qr0spsc98KQHPWbs+rssJgi+iXYIPfO6OzzYGYO9uYE9vXvdLr0JeiO9D3a/PaNxvrzUWyY9cMAHPgIODb0L47c911yMPRdNk72OIgo98H6qvX28yb3Gv+Q9ksOTvRF3BLx8klC9DOKDvawK6jyYxwo+4G5LvJB3AzwUeBc9OCQzvcBvOT3UjR694pusPeIP2b2/+YI64J6pvDw0yDw6iyg9EhVzPfgOuTzBOs07850PPSG/vL3Pazi9Cpd/vYU0F7wJete7DcQUPbhrab2KjLa9CP2Svel0lD1/F/671JXJvYvgnjt++YU8zz05vQoL4bwTjae84aa6vfZqLb1W9XA8SM09vOMUkr1LIDU9T1CevNe55rxoAey9r0DuPH4cqbz+wBG9TuGovZ1Njr2sXwc9JGY3PbBlubzJrSw9h2SOvVlP27xQ2pO8I0i9vd+WUj1tioU8BRHDPE5Tuj2/E9k8EjqjPeEmgz0Pd/Q8w/OIPMoH5zt4NxK9vS4+PFYp1r07qKq95b2lPHnzB71m8AY9CcVrva0x+b1PqLm7DbNkvXaE77zuOQy8dRc5PexQHzwTBTS9nbXgvPcVSz2afjM9gYUGPmcQsz0pf929JsunutvOhDxo98O8ncQwvFcpMz1os4I9+rPTvGIXDjstnqO99qikvRNn8bzSqCe97/VRvdh1Nj2d80+9DMRNPQ==";
        String feature = "7qpXQsFdAAAAAQAAJnH1PapjTDtXSii+kMzJvU/s0jwfcNy9KWlivDXHr7ynn4S8ijgQvVQzljwEp6k8yh4zPVg0FTygtM87BNoHveVsFb2e3MW8KjM7vISBSr1ga4i9M7yTvZ+/jj3r6no8meV4PUk8BD19MSo9CrnAu6vatz38wFC9YL0sPOIwQr11G/S8MEZHPFWO5T3OTN69G61Rvf0ylTwceZQ8iZzkPEacDTxybCq9g0wEvv9qGz2hVMM9l5W7PNOmnD1375e9RGDPvAwt0Tv2OqM9lr81vtH9XrxtrZu8jMXcO/VZFT1H4Dw9tLr8vOSTFz2Sqoo9dGkOPdJs/z022ve8cI69PSBsZT2NRfg8PYCwPbcYgrsEt629E0IMvgCMHD3bN9K7/lsDvcibxT3ofQo9RYxKvSjXGz0s31I9ankWPZYNTD0pVuM9vM41vUWcFT3sp4U9LUMcvUtTdrswZYy9iakIvdXQjr2eG3c9Qc2KvZ4CeT0YtYc7qL/gvIWeTb1P4SC9GWJ8Pa3drLxKZ6U9oXxavXectD3NTa09qgMRPr1rNr7WZ748SfpXvegTeTyXN4O9OwBjvcfW5rxiqt+8xhNxvfnxIL3XgCW+PriWvX1+hD1FLkG8vCHFvBPuHj46Bi29w1XVvKjOsTxROYI97BwOvtihIz0RkPU8skWoPXDQtL2XsR68y0X9uxiajb2V+Lo9GXNlvTETeL0E92q9rbvdvBC01j0aI6y8YZ+ZPGRT9LwZOju9vE4CO5ZONT3Qmuy8fF5YOkdl6j0rr807Ex9jvM2Dt72bF4u9t7yzvSjeyD1/Ude7iKDDPYZb8TwoAvQ7hqhqvTn+07pNtSW9AOkYPbRiML4s1449/K2LPIsgzj2C8sy83ZOtvMyCRDzXxS68yjl9PKKb5D0xxSo8LrU4vD7JpTvXyz08js6tPVeB/z3UK2Y9cigjvWI+kD2HKeI81IhdPcsjPD3wngA9dzTQPXEVVjvYKGC7F70yPYrM5r39B1k86sqFvFEQzjwlGCS9MV5TvXF6iz2Likq93W8EPblkdb0d9ZM9A53IPWSHLT2v1Ra+rSdvvWsA7j3HtOU9i5sSPfr4gz3Yy/48tKX/vMOVWD3aJEe8zo5qvdvmdLxJXIS9t5khvdvWk7uJr3w9m7kuPDwshzxmM6E8yRdTvI3AiL2/UEW9yTE6vXqWZb3Lq4o8RSZVvXL+7DvHQYS9fhBlvQmwgb142hi56tnxPNegl7xVAY48TLwKvQmBNDlk4RI8gcecvcbr3zuMU4698zOyPUDn6T1I/sW9+Rc6vdFFvj0fByE9K245O8RirzylanK9ZC0yvQ3tCL0xMqO93lROvWqAEj2QpRu9xaUuOg==";
        String feature14 = "7qpXQsFdAAAAAQAAL8PBvBpnOjvl/mU9IN8FPTDvnT0e1888/QzKPe8Tzj3ld3c9ZTn0PHxggzsYzH49tOgvPCrUID205369t6q+vXq1ab2TIxC+AhjzPOHgzT2JrK47YqC3vQC3LzslyTi9dzHqvJCxm70fB688nxcWvW2tiLwuiqi76h68O66rtryqYTw9k694vfMXCz4NxkK9MJEkPTtjkL3nIQ09cImUvRb7nr1oItu8MCKkPG37c7wQHTk9S1U+O8GQHj3Mx969NH2qPbrpBjxkrSC+B82muo7DLD35GQa+NPOUPLfbzL0dUra8EeU0vRAjEr6hCoy9jayPO/XPZb1A2nI9CZdQvT2/vL3dZE69IpDwPPUiXrr7lza9h8rcvXvDo7zlEcY9PEvyvea5lDyOO/u92OWHvaFTNT0EBZ29msdGPLuYUr1TQCe9KdVivFAxvz0uTC49rqqvvQmAAz1z+qg9LYsKPsGkfr1iDSe7rzKpvXE6fDxqGRm+WP/svKyeML5X/sQ94yeBvbUzkj1hdtE9UDYHPBStqT17V449gsHCvLeDr71KUNA7qIIxvVWvqL3Ioww91UaDPMlOY73+4AC9yKAAvHHyaLwsNIO9ATmQPdU6nz1ZwTO+ZTbLPdi/5jw9elo9epb3OQZHIbzNtMU9J36ZvCxoNT1eugs9qCZqPcE3DDwFk3Y9xzCHveg02r2r2Fs9LU2ZPW/7vjze5w69vhiZvGhcebsdxdO92WhUPZCvOD1d/2s8wTV1vXqixzxHF2g9Ll+cu5c9ATzNY508X0ONO/gFu7y6Egq9fsuIvFEhkbsQb5899hS8vdka0TxtnN88FKksPSwihDsSH3o9kZ3cOlKg4jymyOy8inzwPFMZ57z1yHY9EZtBvcteOj1ctqo6pcC3PffROjy9Pt48bugOPLl/UDvl+5y8OS8BvhdAAr1G3KY92nh8vaZGwbxV/Bw+iBaTPFbhO73OeGQ9g8jPu5A4TrxMbDK91epfPSCKq7xKC4S9V22vvetZw7yes+y8fqOXvGNahDun5b29xh9iPVithT1O9yQ9thmmPekDQz1lF4g9ptx4PbZaBjxLaqc7NpMGPKsZYT2AM5M9HbsRvdwyaL1Qej29qSCJuw1ksDxNgZa9eGXEPSuMCr333mG9in6IvRKsCr0h02O9sQ7OPKb1bLwisZm889ynPQyk6r3oP468+ePxvXhHH72gS7I8+lyPvcwfsbwJGm+8LLk9vY07272jYL07S8qEvZz/Qb1GihE+1yp7PRJwUD29IdE9W/DGPIh3uL13D+u869ypvfqFkb2406+9iRx1PTy+AbspcOC8NKiRvaw5BbulWSc81AyRPe+/nT24HUi9OeFVvQ==";

        String str = "{'reportService':{ 'id':'12001', 'type':'request','fssSearch':{ "
                + "'startTime': '2017-10-10 00:00:00', 'endTime': '2017-10-26 23:59:59',"
                // + "'cameraIds': ['11000000001310000090','11000000001310000087','11000000001310000088','11000000001310000089','11000000001310000081','11000000001310000082','11000000001310000083','11000000001310000084','11000000001310000085','11000000001310000086'],"
                //+ "'cameraIds':['11000000001310000087'],"
//                + "'cameraIds':['11000000001310000032','11000000001310000033','11000000001310000034','11000000001310000035','11000000001310000036','11000000001310000037','11000000001310000038','11000000001310000039','11000000001310000040','11000000001310000041','11000000001310000042','11000000001310000043','11000000001310000044','11000000001310000045','11000000001310000046','11000000001310000047','11000000001310000048','11000000001310000049','11000000001310000050','11000000001310000051','11000000001310000052','11000000001310000053','11000000001310000054','11000000001310000055','11000000001310000056','11000000001310000057','11000000001310000058','11000000001310000059','11000000001310000060','11000000001310000061','11000000001310000062','11000000001310000063','11000000001310000064','11000000001310000065','11000000001310000066','11000000001310000067','11000000001310000068','11000000001310000069','11000000001310000070','11000000001310000071','11000000001310000072','11000000001310000073','11000000001310000074','11000000001310000075','11000000001310000076','11000000001310000077','11000000001310000078','11000000001310000079','11000000001310000080','11000000001310000081','11000000001310000132','11000000001310000133','11000000001310000134','11000000001310000135','11000000001310000136','11000000001310000137','11000000001310000138','11000000001310000139','11000000001310000140','11000000001310000141','11000000001310000142','11000000001310000143','11000000001310000144','11000000001310000145','11000000001310000146','11000000001310000147','11000000001310000148','11000000001310000149','11000000001310000150','11000000001310000151','11000000001310000152','11000000001310000153','11000000001310000154','11000000001310000155','11000000001310000156','11000000001310000157','11000000001310000158','11000000001310000159','11000000001310000160','11000000001310000161','11000000001310000162','11000000001310000163','11000000001310000164','11000000001310000165','11000000001310000166','11000000001310000167','11000000001310000168','11000000001310000169','11000000001310000170','11000000001310000171','11000000001310000172','11000000001310000173','11000000001310000174','11000000001310000175','11000000001310000176','11000000001310000177','11000000001310000178','11000000001310000179','11000000001310000180','11000000001310000181'],"
                + "'page':{'size':'1','index':'1'},"
                + "'searchFeatures':['" + feature14 + "'],"
                //+ "'searchFeatures':['"+feature11+"','"+feature12+"'],"
                + "'picFilterType':'1','sortOrder':'desc','sortType':'1','simThreshold':89}}}";

        Scanner in = new Scanner(System.in, "utf-8");
        int i = 0;
        // FssJsonInput inputParam = new FssJsonInput();
        MultiHBaseSearch search = HBaseManager.createSearch(str);

        while (true) {

            if (search != null) {

                String str1 = search.getJsonResult(str);

                if (str1 != null) {
                    System.out.println(format(str1));
                }
                // JsonInput
                FssJsonInput inputParam = JSON.parseObject(str, FssJsonInput.class);
                FssReportServiceIn serviceInput = inputParam.getReportService();

                if (str1 != null && !str1.equals("")) {
                    // jsonOutput 获取新的lastId
                    JSONObject jsonOut = JSON.parseObject(str);
                    JSONObject serviceOut = jsonOut.getJSONObject("reportService");
                    JSONObject fssSearch = serviceOut.getJSONObject("fssSearch");
                    JSONObject page = fssSearch.getJSONObject("page");
                    String lastPageIdx = page.getString("index");
                    int pageIndex = Integer.parseInt(lastPageIdx) + 1;
                    serviceInput.getFssSearch().getPage().setIndex(String.valueOf(pageIndex));
                    inputParam.setReportService(serviceInput);

                    str = JSON.toJSONString(inputParam);

                }
            }
            System.out.print("下一页 [0 -(不查询) / 1 -（查询）] : ");
            i = in.nextInt();
            if (i == 0) {
                break;
            }
        }
        System.out.println("SearchByImage Test end!");
    }

    private static void getBlackStaticResult() throws Exception {
        String str = "{'reportService':{ 'id':'12002', 'type':'request','fssSearch':{ "
                + "'page':{'size':'10','index':'1'}, " + "'sim':'90',"
                // E:\FssProgram\V1.0\全表扫描\02-黑名单静态比对\历史图片\1.jpg
                + "'searchFeature':'VU4AAAAAAAAMAgAAG5yFvTleFzwqh5u9r3wDPaUKEb2DZ2i+qEsRvpfPGz0VP668bwb0vNumUr3pxim9eFp3vZbOyrs8xmc9NwHAvTWgwz0osfS8wvr5vcY9CD3dEPq9fck/vUituL24Q3M91/QGPgs4lLxW7z+8+8dgvbh8b7w22h49Yn9+PTc6nTr7/6K9mzMcvFw/Jb1jdFO9dtMsPkBtFj5uMkE84TwtPdAxyToG3MO7yrMEvfa6CL5BveG8xnPBvZpuOT0xEIE8oVKDPSxNGr4HcVS+2ualvXu/wr1GrW++LxsovjB/Jj0MQMM7shWlveA4dr1HQwA8cahQvcxB3zw64E69Bb3lvduYzz1i+Ra9StMRPgfbID1f/q69hQh5vTJYgzxYakE9kw2svGuJgDxoUoW9306EPuuVk72bNyQ9sHoDvadJ0LsN91+9Do34PdD7Rz2S3n476IQhvcL/Gj3EaGe9q1K8Orx84D2QcZu7Mx9nvC4sijzz7m6+VQ5lPLg+r72exJI9Y8wzvFFmkj1J0Tq+/p3RPT73RL5WKea98paZvbXA0z0efg89T6G/vd6hbb0Mwyi9vZKYPdcX1D0rrwm+va2qvafT5Lx9o4M87meGPZJHL76l6Oq9QVwlPViCZT2i+Gi9bJM/PmFOLz2pEEs9BeomPWMMCL4fdSY9Pur+PAUm8js='"
                + "}}}"; // 图片特征值，

        Scanner in = new Scanner(System.in, "utf-8");
        int i = 0;
        FssJsonInput inputParam;
        MultiHBaseSearch search = HBaseManager.createSearch(str);

        while (true) {
            if (search != null) {
                String str1 = search.getJsonResult(str);
                if (str1 != null) {
                    System.out.println(format(str1));
                }
                // JsonInput
                inputParam = JSON.parseObject(str, FssJsonInput.class);
                FssReportServiceIn serviceInput = inputParam.getReportService();
                if (str1 != null && !str1.equals("")) {
                    // jsonOutput 获取新的lastId
                    JSONObject jsonOut = JSON.parseObject(str);
                    JSONObject serviceOut = jsonOut.getJSONObject("reportService");
                    JSONObject fssSearch = serviceOut.getJSONObject("fssSearch");
                    JSONObject page = fssSearch.getJSONObject("page");
                    String lastPageIdx = page.getString("index");
                    int pageIndex = Integer.parseInt(lastPageIdx) + 1;
                    serviceInput.getFssSearch().getPage().setIndex(String.valueOf(pageIndex));
                    inputParam.setReportService(serviceInput);
                    str = JSON.toJSONString(inputParam);
                }
            }
            System.out.print("下一页 [0 -(不查询) / 1 -（查询）] : ");
            i = in.nextInt();
            if (i == 0) {
                break;
            }
        }
        System.out.println("BlackStaticSearch Test end!");

    }

    private static void getSearchByTimeResult() throws Exception {

        String str = "{'reportService':{ 'id':'12003', 'type':'request','fssSearch':{ "
                + "'startTime': '2017-10-27 23:00:00', 'endTime': '2017-10-27 20:59:59'," + "'cameraIds': []," // '96','99','17','31','70'
                // '46','100'
                + "'page':{'size':'10','index':'1'}, " + "'sortType':'1'" // 0：相似度倒排序，1：抓拍时间倒排序
                + "}}}"; // 图片特征值，
        // string
        // string
        Scanner in = new Scanner(System.in, "utf-8");
        int i = 0;
        MultiHBaseSearch search = HBaseManager.createSearch(str);
        while (true) {
            if (search != null) {
                String str1 = search.getJsonResult(str);
                if (str1 != null) {
                    // System.out.println(format(str1));
                }
                // JsonInput
                FssJsonInput inputParam = JSON.parseObject(str, FssJsonInput.class);
                FssReportServiceIn serviceInput = inputParam.getReportService();

                if (!str1.equals("")) { // && str1 != null
                    // jsonOutput 获取新的lastId
                    JSONObject jsonOut = JSON.parseObject(str);
                    JSONObject serviceOut = jsonOut.getJSONObject("reportService");
                    JSONObject fssSearch = serviceOut.getJSONObject("fssSearch");
                    JSONObject page = fssSearch.getJSONObject("page");
                    String lastPageIdx = page.getString("index");
                    int pageIndex = Integer.parseInt(lastPageIdx) + 1;
                    serviceInput.getFssSearch().getPage().setIndex(String.valueOf(pageIndex));
                    inputParam.setReportService(serviceInput);
                    str = JSON.toJSONString(inputParam);
                }
            }
            System.out.print("下一页 [0 -(不查询) / 1 -（查询）] : ");
            i = in.nextInt();
            if (i == 0) {
                break;
            }
        }
        System.out.println("SearchByTime Test end!");

    }

    private static void getRelationshipResult() throws Exception {
        // hy:
        // 7qpXQpFlAACAAAAAhEaavHSKWLypcTK7xqM8PCGmgz1g4pw9qq+hvQGdML6qCHM9yiaOPbUOEb7Zn3I9EPFlPQx30r0YVp88keoWPeGlXTu6Yzc83L+MvXCQHr4p4pw9Z0oYvhXLej2k2yw9EIl1PQNH5Dud7sK9gnwMPEdODjxBWGK9mRbkPQz0Dz7zV667BIDbvETPh73Yqxs8QWfCvErlbj1GDm29mB71vbw6jbzlHhq+5d0zvSOytTt2GFO8hCryPfzjWzx9EzE8oyGlPS9CMT5JeeW9BFybO+sMDLznoA69NJ/jPPuLUD2NHQo+pkApvtyVmzxbcVm+OPjfPYlE2L127rO7lsbZPT2dqzxd5Wq9GpE5PVOmCr1xaYE9a5+9PBtKOL68N2u79xeDvUIpwzwnmD29292svQzTXjydkkc9PPVkvOUbH742i5g9Taxvvdb5Vr3agXE9Y5DuPM05ub1zyM08fzlTPYhFI7xpWFw9Ng4MPqpZO71nuAI9MIDzPHqxRD1tOoS+97MsveaXHr5oORU+aixJvljImTue6VI9mVvHPVu2Mj7cQIQ9pdBKPU8dOL1FPhU+cbeRPZdPqz0wGya9Fd1uvm2GGbwKF4q9Tc+ava3bHz1xuaw9xhROvlfGOj2kvOs8Pd9XvV+EQL1bTNq8elUxvpLoAb7RDFS9ocrtu9CRbzw=
        // yjp:
        // 7qpXQpFlAACAAAAA6iMSvs821Dyvrzq9k/gLvS4crr2UyJS9NSQjviqvB76EUeo9Sk4kvX+VCr7C3Su8AZAgPeO/L77u0qW8WwoBPq1rjz0uVuy8+dzZvR+4uT3HPgO+tRUePrrj7jx9bao7k8KVPHvL3rwZYUE+3qiQPSdfDL77pow7Q6giPmLsDT2n91W9B4qevaIP9L07s487ul8hvTRHBL4fF2S8Jy+fvBFk8byHSZ494yJyvGqu0j3W+pw9PawMvj/vkL1a4UC9nlpKubPqAz7St0g9sJ+avJneqT1Dj5o8lbTGO5ChHr0CkIm9cqMUvQDsg72DQJI9w50oPXaq6L2GPCW92Prsvbfgaj1UXhm8BmGRvfnWdb3Gtcg8rPatPGPnDzxfsgQ9EJMcvVbvjj25qgw+8NyqPdmNhz2qBoi9e/sCvA4FBD7BJVe9Y4khPQ0pH7xKMrc8mTffvG+Wrj2IvRE+uWTYPCw6kb5mF+C9fh/PvQjOV7y+4pY99thSPl49oj0aq9C9hYzOO6s+Hb5yJZ89c+jEvej8VL1HUwC85C42u/ILxLxO7bC9GWT3PD1zHr0A6XO9MPu3uxAu97xwYeG8wjbNOJNujr14BKW9PhNGvhEYbL5fVti9jyb4PeEqH777+t46JfT/O0anebzz/zy9Eo65Owv1gD3Hiwa9NzgsPtTvhL0=
        // String feature = "7qpXQpFlAACAAAAAAtoSvvgXir3Ll8u9QLNWvWOMFT3+GJ4864yNvh0QEb7o7909egAcO4Zxkb3nJZo8X9ORvWEGYr5OXKy9N759PZNDwj3LBCm94GUGvtzZSz3wSrG9rldfPpw1iz3cWWk8OVQYPdMjN70qLws+7FdwPRivAb6H2Si9CiVPPtm9BT6kKJW9aGMMvmHAGb62Wl69NVj5vQjJcr3/ohK9VrHZPC9oJ72j4Vg99vdZvZAPwj1QwkA9rS6rvWJ5tbt7tpC9tez+vOfG1D2pUKs839xePdaO5z366fW7nEj/PVX0bLxexzy+fKbRvY5DYzyk5ZE9suOdPUOnWb10V/W9zRPAvAWjq7yvrGO9F/3cvdTijL1bbLe7zuGqvff/JL09lkc8OBBsviG4jzydLfQ8fF8PPRfSdr2uGqW9L4+YvU6T2T0GgKS9VsOQPJ2BNbwgPhW9MF/ivY4+uz0aVmM+WVvUPTga870KDcy9EqcZPSqsBDvtwxA+Sh6CPYEhnry+Lgy9+qoVPbSuwb0Z1F09Du0mvZo2K70KBUA9pxCRvQLqtb3Iy3Q82dNCPMNNlrzFYLS9dfpPPRvnoD2YN7i9fPaOu1qJm72Kiie82guivLabGr5faGO9g4KrPTiXqb2sDi48xFO7PToYGb3A4WC9sLEMPZQV7TzhaVw9gfUYPeI8q7s=";
        // ljw:
        // 7qpXQpFlAACAAAAAzL7+PN8vqTxNsAw+QF7qvL8h3b0Bhsy9QlyXvcRckL2O7Sm8FV6JPeoXrzwraCK9RlLoPWEa1LzKzsm83OaqvLDK6j1wCCs9j9r7PP0h3Dx+ARK+kc6avfYAILu2LVo9Dv7vveTxnT2MfzM+r9a/PYa5P72nFw28IR/QPVdk0T3pt4u9zK58vAyrrL16NG08jVQOvjv+YT24Hag8PkSmPaB1JT4U4s49FNrcPN5anrwjjyG9vpypvXJ00bxQMQ+9JmKEPE6+i71Gdge+Gxveu82v2bycGjW9yNsQvuCvwj33vxy+Ib2JPRbHED0p8ns9tMJuPfAkIT03qIg9xvrmvNRNDb1Vpf89dcyrPSF+1L2cpIC91Wd4vRzM6Lz7FqC9zMJOvSyiVL53qc09bcXeOzKuj70OdUa9oz0uvXXGg70foek9G70CPTr3RD2/VYa8t8NjvT/DkzyIuau8VGFpvcajwb3oNsg9H8ukvH0dsbx5YrA9AN7avZLjBj4lgTU+r/wEPj8cMb1SdTC+TDzsPVvxL77s9aQ993O4OwI3zTwNa2s96NgRvSja6rtiJau8hCtQPeqlQr7Z+Da8HIyMvQGmQDzs6sk9tC2SPbakazwILsy8311BvrsN/D1QwXm+IN96PUMd7rt4kWu+vC2JvY02R73/DRy9VqUrvjamAj4=
        // cxd:
        // String feature = "7qpXQpFlAACAAAAAXMMHvuWnJ777XAK+yChOvLGU5L3lQ6u9AQ80vgdCJz2rcJI9UMZyPSeiCD5BeQU99S+2uZcqwbv2QWO9iLMqu3fD+LsLZ1Q9yqKkvZH+FT44BMe8+zUnPtVZJD7a9CA8IQBZvZDv5TymTxW7uT5xPTwIAL7+7vQ9GuOjvDz4gzw0XzQ94SViuwiKQ74ceRo+I6lXvOGS6LvQqSS8cOEvvSEGQr00LMA9AGnevOUWDj5D5OI9PWPEvQzwkL7EQSC9hwivvHsa8D16nRQ97RG0PaG5Dr5CuWE8EtbRvR40IT4IucQ9MGsBvX69Kb2/XgQ+UJOIPenNfj1ZxwO+Ug+ZPQnvnr2R0Eo9g+fPOs127L2ZIbm7QtfIPFdpdz2v7x27GowGPjfAXT2lrQ8+b06oPXCzwL3JGay9xuJQPndGn70hYQe+kcqYvRAHjr0G/wy9I2dzvVUXQ71vrQw+V8dsPVsHBb0xnsg8P4YCvX6cyb263II9WH6UvGAXEL5xBEa9yb2OPKnIY71RVAc7A7RNvZXWdz3x+pk9PCe7vJ97/722KMi9SGGCPUPvCL5cfSY9PNT1vU1okz1DDHw8kk/GvWKuiT2v+o+9/fYwvSzDxLzX/be9WV4EPRhK17sUo3I91HW9vWPAoT068qy8jSc1PEZwBD7u+gS+/CCEvG/k0rw=";
        // liuyan:
        //String feature = "7qpXQpFlAACAAAAApdNqvfyz8btF+Is9i85+PQ1vFj5ERh48BuVvvZvRjTxYP7I9B54pO9jH5zxPzpS9mxV7PnWNp7x42968SYf9vF1YAj74kCq82V9JvCf79bvKq/28JTcEvnTuK7wNQzo8hW/Lvcrjaj2kGbK8svZZPth0GD2yAaK7G09UPWgwib3aZL69tOphPYlnFz2K5iC96XMlvUGBkTyYfuI9ABQOvvEtDD7R3iC+a/PEvTG18D0JD0O9UMq/vJjOorz7jsg8RjecvKfeFj7WqHI96X6nvRMYeD25itO9SbTEvYT7Oz5kn38+Uk0KvTnuPTwhEbU8gVAKPlXH2L08iEk+L8GdPAm7BL2t75s9Cm8KvgJAZDzXr849Jm6oPceNaL3+Yy2978jvvYX7B713Uhy+66fgvaJXgr0nL5a9bEiUvcOp670C8Em99TiPvRaXZb34AJW9o6UxPABUlT1sJTU+ILUYPZHTsTzkUxU9iO1DPDzj6ToGEJi9x+UyPWUbRj16zJw7NO0mu28OLT6k/M49FnsbPKTk0j0Fpu48k6oGvqXJl72uAKY5DXRivXXdnbuvrRY9Ai1BPjwvSDxHk9e9CkpWvV4BLr0WFEK9wr3HvA2CpT0tg/O98v+XvSPfDj7Glgg+SCklO0tOFT5AL5g9vQJfvQoJhj3qxxc9uTtuvXIEDz4=";
        //xiongfan 256:
        //String feature = "7qpXQsFdAAAAAQAAiri7PU460jwUjYW9Pnf8PVcT+jw0cva9mpMouz9CEboWdPa8bVuqvacNkb14JzC9UxBdvLwAFD17P729FG8zPCl+Ajw6M3m8j5kKvmqnjDwH/Qc9bgRhvTfdWL0g58w8A/GEO4//TjzK+4e8DamIPRABzD2KKzi9wDmCPdTSqDynb729ZbuNPYKI9TyNQqE8WJjPvIHRtzynBGw9j0ImvCY94rwmmXy9YqZYPFiBRz6JIKm8X/GWPTNlTbssJGw8wYMUPF8WLjxe9j89dyEVvtXjxz00ku07HONDvSc3Hb17jwY8mdG9PJjSfL10ePs8s1WCvO5IF71e0rQ9nNrUPBQGyr1yv6i4nQ5SPZ+P4rxrJKS9fRMuvLFipTzFhv+8p13hu8I0j7uuE288sX5tveWu7L2O56Y8o4Vhu2wCpD3gwge9CXyFvkcKgLwZ9FI8WGUuPR2Of70ibvE9A2UCPuPZlD2FCXc96l91PQw5aLsGC/I9k0sVvUcasbuMZbI9FMHXPFtL2Dxtozq8GSwavnovlb1qFKq8sEWpveIC37z0L6i9eXgEPnGOIL23r1w9mV17PEfVAD0GFdm9yo7nvdolejxCmY67/IPLveviHD1qZ5M9qVTIvZlfHz3ShBG91YsvvqkXWL0nkBy8rVDOPVP2lr3zJRG9IlnbPKMEFrt9EMm90GsYvX/5qz0ZnJK8acLjPEn2Ib2PIAC9DEThOwroJr3EV289WtJ/PZVkGD00e5y9L9GuvSGnUjyM0IG9Q5w0vdhMiTza6Cs8w64oPU0hSr0SBQy9iSYyvTqphz0ZJVg9RafDPHEJjD3xque8FBEHvWzMEL6AJuo78gtTvUMzjz2BZsO8YfrgvMxxgLpoQ/S8gl7IPDGKRL3tfPM8u17RPPbghTwioGK8WgsguAACZD2Xeju9x3RLvUu/e7uBC+O9R7Rbva4qCz0jYTw7O3PGPFMuWr0I8+y9neG4PKuAFrsyQmc9/v8cvS+W3r1f8us8BcC0OAVAvbvL0ac9ftQGu7gk67tAWIM9WMvAPZ35sj2sgsW9lF0dvKSrb735xkQ724ZWPQZggL1BahY7UUi9PGlGxr2RMo68YDCEPZpQRD0NZCY9laPvO+1RfD2mfKk9tfaxvQoQXr1ii4S8TMs+PUTgCz3imfy82TVmPZ0rcz07aTY9lUOlPbx4PLzPvw++pSmFvY880r2C4+K9438fPGjgKjyC8Yo8v31svIVeyD19Cew8+LSaPdQ8Fb7oBm48Vl15PBMIlj0aCJ47eZKxPNw4nDyOIby70rftvJ5h/rvg9Si9piu+PH+UPju2J+49iwekvb1WXT0UY+G8GhupvFTuaz1QVuk9ubetuw==";
        //test 256
        // String feature = "7qpXQsFdAAAAAQAAjEBqvVT2F771Ju+8GkO6vXGHLDyzJhA+9L8FvY4W6jx5VIg7m2LXvavis70U3F49pPDYPQhKSb326oQ9rGGdvcNgjb2x3/Y8+n2nuiKAdT080hG9TQ0Jvd10ob3JPv27tRmRO9PwVD19e9C7PSpTvdZRyzxr7B481QEhvdGmBj5eioc9p9C6u6MGhTuiGKy9LGz1uwCYyDydKIQ9Xqg+vS3qrD2Ps+o7gWdOu1tl0TvMnYQ9+yNKvQ+CjD2z3h28XJthvQ0Ra70eCwK9Sf4FPEFbsLygEs+802j6PKDkMD25ys897d38Ok/LfzxAwsA92qNtvHT9j71lXE28+eIpvfAsrbwHauk9C3qrPYLxQjzFPq+9tWGBulmHs73AKcW9Su2ePOEZ6z289ai9k4qSPOptID38x3k7yrmQPUDyL710+EK7y9W2vAwHgD3EpLa9bvaaPL1gZ71ic2U91/gvvZBZEb6Wr2G91v0MPQW27Lzm2KO8ye2HPJChhT3bvu+7qMb0PCLd7z1ej7w8GZ88PSQ1nT13ikU7KnkNvRq/0LyfjYG9fCOfPNlQBj2WZrm9QGqAPd4kAj3i3Fo9FyoFPSavVb0+Bm+9TMaxPEzyEbvxcvM9QdLMPf+EU72/CSc9pyxbPMoihbs9IGS9jtjKPShxRz1uJs+8leYIPRXwfDsfSxu+3Pc4PIXE5D17bvG92TCyvFkqi7256La8MbK5vQStBr2Cq9M80mAkPa5MeL3QHhI+VzhIPYvGCrv8/kw9E7EHvJt4ujwNIvO8GyonvUCPPjyfNJe8YqC/vPjqCL3yqMA9QLtiPDpRKrx7DH0992onPRMT3L31YKa8tNM3PT1Hi71kX028t+DXPf0YDj11fhk97J3IPcqDD77fIXc8dmucPU0/CL6Hexg8UZeSvNuoH7w8ZV49ZzRNvaUga73LctG9ubzZvPMCsTzHiiQ9deuCvD9ndDwhBgu9TBTBvCdEZD3AuoI9HFynvdb0ij15BXI5IbaCveDuUz2eW+Q9g06zPR7mGL18u6o8Q+ywvYBPaj1dczS90AzbvVpKv72RVwO9TSlNPa/JE73tGeC8WTKDPWlASz27yy69ttCVvRUpkTyI97K9JIq8PB+mzDlphUw8cqaxveJ6BT4QR/89IvObvUb6JD1Ygha991nJvdP9iby6Tww9eKo6OqDw3jz+/AE9zelePd6EJb1RB8i8iSdWvWJLVD3bMYC8CJKDvTURfz03Ws+9VlbjPacY2z2Ngb09XjCKvBfzQL3dFoK9cLYgvqpl+7znH5y9KFrPPQBwwr1j28u9IAOLPI7707v+iWA9WSJgPVkU+zz9npO8i3YDvthpaD32VYW9CYyFPA==";
        String path = "D:\\项目\\不超过64KB的照片\\mo.jpg";
        String feature = com.znv.fss.hbase.test.PictureUtils.getStrFeature(com.znv.fss.hbase.test.PictureUtils.httpExecute(new File(path)));
        /*
         * String str = "{\n" + "'reportService': {\n" + "'id': '12005',\n" + "'type': 'request',    \n" +
         * "'relationshipParam': {\t\n" + " 'startTime': '2017-08-04 00:00:00',\n" +
         * " 'endTime': '2017-08-10 23:59:59',\n" + " 'topN':30,\n" + " 'peerInterval':10,\n" +
         * "'associationType': '1',\n" + "'searchFeature':'"+feature+"'" + "    }    \n" + "  }\n" + "}";
         */
        String str = "{'reportService':{'id':'12005',"
                + "'relationshipParam':{'sortType':'1','endTime':'2017-10-30 23:59:59','peerInterval':10,"
                + "'searchFeature':['" + feature + "'],"
                + "'startTime':'2017-10-27 00:00:00','topN':10, 'picFilterType':'1','officeIds':[], 'cameraIds':[]},'type':'request'}}";
        // string
        MultiHBaseSearch search = HBaseManager.createSearch(str);

        if (search != null) {
            String str1 = search.getJsonResult(str);
            if (str1 != null) {
                System.out.println(format(str1));
            }

        }

        System.out.println("Relationship Search Test end!");

    }

    /***
     * 测试驻留时间查询表
     *
     * @throws Exception
     */
    public static void getStayTimeSearch() throws Exception {
        String str = " {'reportService':{'id': '12006','type': 'request','size':100,'" +
                "stayTime':{'reportType':'01','officeIds': ['11000020003'],'cameraIds': ['11000000001310000109']," +
                "'simThreshold': 89,'startTime':'2018-02-02 00:00:00','endTime':'2018-02-02 23:59:59'}}}";

        MultiHBaseSearch search = HBaseManager.createSearch(str);
        if (search != null) {
            String str1 = search.getJsonResult(str);
            System.out.println(format(str1));
        }
    }

    public static void getByTrialSearch(/*String str*/) throws Exception {
        String feature1 = "7qpXQsFdAAAAAQAAlKYVvjgiMD0zwCq9lBnPvfhB7z2PGw07CU9yvZlym7zKeqk9Y4kfPGY68jy74gi+2J9ZusV2k70z5OW9VvMuPB7ISz16LU+9FalCPWHpAj2i4rQ948QdPGE5Wr0QUaC8C7YTvS5Hvz3XA1m9twykPaa6ATym0pm89UqavIgGqL3Q0wu9LJCwPVpgt734x2670XNpvSgdcr3wqLw9U4nwvHT8Lb3y1Rg90YbsPK3MK70vxIS9D3RQumzOEb2ZFde8VeE8vZHh8zvazXC9DcmGPWuM4LwZ+Fa9p0AlPYCQUb20tUk9zcQzvH1qYLyNHRM6tcE9vVwUMj5+2Dy95aFxPeRjaj2PZhg+WWgDPvhCEb3itTC+Gka8vdpyZL3GJm69EBKQO+8fzT3KFaQ8VAe1vJtVsjwXBTY9x/mjPHfOPD0VRPk8OCboPIi5sj05QyG+nECAPOOoa73cBuC9BggvvbimiT2ldZo9Us4WPXvACb0Yv3G9tfHtPcgDDT2de9q83IqgPDTKezx3epQ9dhYWPQzVYL1nHWK9z0DNPHRCkT0Lzh291TkZPZkbKb6M03a8WKnOPVriEj7Z4oa94eQJvAsdsDhrZMk93zEXPYxd/zxImos86ro6vccHJrxgIAo96fl0PVZUSz1vEzQ97WWWPGWxSzy7DRC9df5GvQG+KTyu6Q097jSzPPSMAb5iKrm9nYaGPCgKJD1Vt+M8XHsYvZOvMb14tgE9Zdg8PHvjYrzxrKU8ZSIYPeyl+L1Y79o8w1YePCxXkb2M8u49ICgZPUWXj71BUKI88SByvcSQAbuOOR49BxR2PWuh8LyavJg9fjNWPKz1rLwcw5y9GSTHPKPzc72QZtO9x78BPVOheT3bHhg9ObG0Pc5NsbxBrCs+AJHjPeOwO72fg744ZCWdPfNT1LxXdfi8SMIjPamzp72AH+g8xw+vPSF8ZD2efVw7a6IUPXesET3u/eW9bfzdPQaIIr27OUO9B6XcvNktbT0nLeU8scW6vL/74zw9LUm8JINzPeB9H7u06Ko8yRLNPLGuA75roje9+dIXPRyE7ry6r4K97lH+PAVejj23KoW84ij7vNydkDx6Jxm9YOC8vVkywbyAl8U9UE5bPPb3CLtPTFI9NAUrvUzxVbzkPAG+HZPnPPaV/ztmz9M9vvWnunYLv710PX89+eULveV3O71PVQ+9NjGUPcmlCz4I94k5SOCVPCbgADwDpok9/KRIvdOMgz23M5k95kbVvXY/szxzOA89v0O2PKP1izsAMD89isQQPfks6zzOzIa9hhK/PagRiD3cHT29slQqPXxAyDxjI+Y8Pf7pvZl4pTxzepY9apCmPeFXtL10jmM8aaI7PQ==";
        String feature = "7qpXQsFdAAAAAQAAJnH1PapjTDtXSii+kMzJvU/s0jwfcNy9KWlivDXHr7ynn4S8ijgQvVQzljwEp6k8yh4zPVg0FTygtM87BNoHveVsFb2e3MW8KjM7vISBSr1ga4i9M7yTvZ+/jj3r6no8meV4PUk8BD19MSo9CrnAu6vatz38wFC9YL0sPOIwQr11G/S8MEZHPFWO5T3OTN69G61Rvf0ylTwceZQ8iZzkPEacDTxybCq9g0wEvv9qGz2hVMM9l5W7PNOmnD1375e9RGDPvAwt0Tv2OqM9lr81vtH9XrxtrZu8jMXcO/VZFT1H4Dw9tLr8vOSTFz2Sqoo9dGkOPdJs/z022ve8cI69PSBsZT2NRfg8PYCwPbcYgrsEt629E0IMvgCMHD3bN9K7/lsDvcibxT3ofQo9RYxKvSjXGz0s31I9ankWPZYNTD0pVuM9vM41vUWcFT3sp4U9LUMcvUtTdrswZYy9iakIvdXQjr2eG3c9Qc2KvZ4CeT0YtYc7qL/gvIWeTb1P4SC9GWJ8Pa3drLxKZ6U9oXxavXectD3NTa09qgMRPr1rNr7WZ748SfpXvegTeTyXN4O9OwBjvcfW5rxiqt+8xhNxvfnxIL3XgCW+PriWvX1+hD1FLkG8vCHFvBPuHj46Bi29w1XVvKjOsTxROYI97BwOvtihIz0RkPU8skWoPXDQtL2XsR68y0X9uxiajb2V+Lo9GXNlvTETeL0E92q9rbvdvBC01j0aI6y8YZ+ZPGRT9LwZOju9vE4CO5ZONT3Qmuy8fF5YOkdl6j0rr807Ex9jvM2Dt72bF4u9t7yzvSjeyD1/Ude7iKDDPYZb8TwoAvQ7hqhqvTn+07pNtSW9AOkYPbRiML4s1449/K2LPIsgzj2C8sy83ZOtvMyCRDzXxS68yjl9PKKb5D0xxSo8LrU4vD7JpTvXyz08js6tPVeB/z3UK2Y9cigjvWI+kD2HKeI81IhdPcsjPD3wngA9dzTQPXEVVjvYKGC7F70yPYrM5r39B1k86sqFvFEQzjwlGCS9MV5TvXF6iz2Likq93W8EPblkdb0d9ZM9A53IPWSHLT2v1Ra+rSdvvWsA7j3HtOU9i5sSPfr4gz3Yy/48tKX/vMOVWD3aJEe8zo5qvdvmdLxJXIS9t5khvdvWk7uJr3w9m7kuPDwshzxmM6E8yRdTvI3AiL2/UEW9yTE6vXqWZb3Lq4o8RSZVvXL+7DvHQYS9fhBlvQmwgb142hi56tnxPNegl7xVAY48TLwKvQmBNDlk4RI8gcecvcbr3zuMU4698zOyPUDn6T1I/sW9+Rc6vdFFvj0fByE9K245O8RirzylanK9ZC0yvQ3tCL0xMqO93lROvWqAEj2QpRu9xaUuOg==";
        String feature11 = "7qpXQsFdAAAAAQAAN8WbPJREe7y31L28UTAXvSbOmjxRqLE9GlSLPd0lN7vcI+K7PHqsPM3zgz3lsCK86WBsPVbdaL2Q8oE8W2PSOy5yjb3FzAM90kNRPB7+gLu7Yam7OLsvu3G3GT003Iu8Zi7lvWhP1Ltko7M85u4HPRKCxTwDPlq98pyMvRAqFj5NBea9m2TgPQBuVT0+vdC9Xj0Avg0nvb3BJJy8bvsuvTpcDr2wk5G9iiofPRfsvzwRGrM9XVESu55D9L3bA529hCn2PU5rOz25uQE9GCrzvbjliDxzp6Q8kiVOvYNjNz0VO8o9XCENPDwZs72x6oy9iRUOvlG3Bz5/gbU80WXOO+MsaDx1IIk7FbITvUz9y73sJzm9iMMwuxdjGb2QRJY9oftdPSnPFD0yUZ+98lXLvbQKrb1PHVs9iolQvWrvgD2gFPI8O2mKvbQzlL0Oa+S7vxomPL9QlzwFQ0s94F0GPctiXDsq4gK9wmLMvfjJDryW5iU8+km+PLlsJD3ETp+9R2/dvfGVib1tUia9Sq5APa6Sb7yFl0I9mJ1+vUgszz26olw9fVOZPK4C7zzymUW8xfXlvRnmZLwN5LI9U5dAvULPWj1J8i++SLjtu9dmjrxPDrA8X9euPXdMCb2pT3K9URRAvL8vjbwtQh8+ZeE/PbaLpjwpnjQ9HzwPvftC0jw5oSQ9JgDnPXyZm71O0rO80FWRPf+ksD3AsIa9vO78vF8r/rxp/lw9IfhJveWbej1Jb8S8BVQ/PNeyNj0pTKq8gVOuvelqmTv8t3+9b9NFvHSNaL2sMd87+qapvYPZ572jgr48Lc1GPUW76T2Iz6U7NGoFPA3xWz28fw0+/RY8vYNKs71NSUe9hCGBPI7QuT1yTsy9pefGPdKQ8LxtRMI7lV1jvGHslbpFP7q9pQtRPc7+ljyDzVG934ZXPQ8VQzuDvFc9BUACvSF9Ez1gc5U9KW/BvEbtHT0HbrS90sGLvH1Pg70DoXu9abzKveC8iLzn2k89XbAcvGFBtL1fOq89NtK9veWTZT0l5nA8292SPN0XIL14jJA9gTe0vSuZMb2FTbM8WjFgPVwMsTz/iu89L9y6vV/PQj23NVI8iDxHvBSBKL1hEMa8DDIYvYc/mzvzbhK90/jGPfbEgDuk/+y9cYJGPbV4sLyC6Ua9uiAmvK4qnb0gL9C8uimePYuw/zwZhbm97sJ2vbLCTr08hT69M7+0vHQ7BrsMUgQ+UxtGvGTn4j3obVA9iPabPScxBb4uBrY9sabnvdI7qr0apYM8WOw5vdc0szwxx7q8MM6yveVcUbvun+S8aTCsvAY5yDqnYAy9HEHyPYMjXb3YiRy8HKbCPOZSXTtQ2sy95IvsPQ==";

        String feature13 = "7qpXQsFdAAAAAQAA0OyVvfbL0bt9Dme8gI6Evb8NsD2uUOw85ESnvaQvob2EHde9UOVLvA/86z2Mhrk9jTPmvIHzsDxtduO8HuEWPXPIdjr/Ugk815GVPOBLcD0vLDm9ESaYPXWSi70Zy6e9jVADPsixlzwyjdI8FUv0vCWH471KrBe9gNAcPVfs8rzJTx88b8NSPFniL70A5h49IOExvW/LAD2ThpQ94d06va7gd70O6z29ShBnvN+/hrrRKdM7zn8ovQ5yIL1J9Ky939yXvY6UwLxJOmS8Rguzu++3JL51MLQ9VYOGvapHFj092qM9yic1vbMfOL0LnNs7h5IVPWAKxj1a8im9ft/CvdF4Lr3rIiM9ecXEPTjD57y7F7e8kKmHvV9Ogbuu12G9n9WFPYIQbDxLJas9Igglvbjl7rwV7qi9//ENu9YIOb4XMys9B9LLPGSbQz2fhi29diWOPbujGruDqlm8ZuhCPFY4nTwgkj49GP8/PFFU3b0uc3m9gdzfPfrfBL7bWyg97aubPb3j/jzJjsS9wCDavN9sKTsyzJs9IOhVvVANxzsTHQY9Kid5PXUcgTNgemK94PS1PQ1Vmz0oAWS8BBBwvaDmgj1d0Js8arHEO2BUCj7LRzo9VV1WO/gtdz0PA8e9xPGKPZyOo7wWcr09PSjTPA6W8T130Nw8a4aSvVBMG71GYZu8hHwWvRK8kzwt4nI8bDqKPZ7p6D3gM6i8hbG5PPuVzbwOJVu9KBHbPPBfiT1/ZxE99dKpvSVtrz13Isa737a+vWDfA72vtly8Yo6/uuuuijw6yoS9xk2NPCH6JT3tgu88cRGPO9lomr2VCW+9J+7UvdSTir0D6zc8RukNvopvrzzoK1W9OVYCPuNiVr2+ATO8x2TZPY2MUz0Izio8v6XSPSHV2TzkH5Y9d3CSPKayIr3rJ1A99/gIPgNzlL1TEm88qr6KPQQ/3D3t9Nw8AZqIPdr4bDxv0429tba6PBMQqr3LKMu7umZLvYtcCD4c4kG8KS1+PbXYmb1WyZ48HPhKvQ87abxpPtC9yCTzO1lylb3077c9SjhovelHLD3Aqm+94lHUvJOOqj0T+Xu9rX3NPAGgMr1f5KO6Pz8zvfZ0zL2aBm09IDymvQa9U71bC1a8x+4iPtq7TL26nV+9DQ0rvM72Gz0x7V09TEGSPWWEYb00T+887WiZPZZmW7wzwhk8veOqvCO7RL1EjJA9zcSqPQ4tkLyMC0k9AaqdvdhqOTzb7CK8ySfJve2dTD384Yi9VUEGPm0+Oj0jkYK9L1WHPJNrdTw8ve08LKuPPc6S3D2GdaC7FPckPiVr4rwepZ09c/evvTgEVT1uB1o958GQPVRFRb2AvTk8GDoGvQ==";


        String str = "{'reportService':{ 'id':'12007', 'type':'request','fssSearch':{ "
                + "'startTime': '2017-10-20 00:00:00', 'endTime': '2017-10-26 23:59:59',"
//                + "'cameraIds':['11000000001310000032','11000000001310000033','11000000001310000034','11000000001310000035','11000000001310000036','11000000001310000037','11000000001310000038','11000000001310000039','11000000001310000040','11000000001310000041','11000000001310000042','11000000001310000043','11000000001310000044','11000000001310000045','11000000001310000046','11000000001310000047','11000000001310000048','11000000001310000049','11000000001310000050','11000000001310000051','11000000001310000052','11000000001310000053','11000000001310000054','11000000001310000055','11000000001310000056','11000000001310000057','11000000001310000058','11000000001310000059','11000000001310000060','11000000001310000061','11000000001310000062','11000000001310000063','11000000001310000064','11000000001310000065','11000000001310000066','11000000001310000067','11000000001310000068','11000000001310000069','11000000001310000070','11000000001310000071','11000000001310000072','11000000001310000073','11000000001310000074','11000000001310000075','11000000001310000076','11000000001310000077','11000000001310000078','11000000001310000079','11000000001310000080','11000000001310000081','11000000001310000132','11000000001310000133','11000000001310000134','11000000001310000135','11000000001310000136','11000000001310000137','11000000001310000138','11000000001310000139','11000000001310000140','11000000001310000141','11000000001310000142','11000000001310000143','11000000001310000144','11000000001310000145','11000000001310000146','11000000001310000147','11000000001310000148','11000000001310000149','11000000001310000150','11000000001310000151','11000000001310000152','11000000001310000153','11000000001310000154','11000000001310000155','11000000001310000156','11000000001310000157','11000000001310000158','11000000001310000159','11000000001310000160','11000000001310000161','11000000001310000162','11000000001310000163','11000000001310000164','11000000001310000165','11000000001310000166','11000000001310000167','11000000001310000168','11000000001310000169','11000000001310000170','11000000001310000171','11000000001310000172','11000000001310000173','11000000001310000174','11000000001310000175','11000000001310000176','11000000001310000177','11000000001310000178','11000000001310000179','11000000001310000180','11000000001310000181'],"

                + "'page':{'size':'10','index':'1'},"
                + "'searchFeatures':['" + feature11 + "'],"
                //+ "'searchFeatures':['"+feature1+"','"+feature1+"','"+feature1+"'],"
                + "'picFilterType':'1','sortOrder':'desc','sortType':'2','simThreshold':89}}}";

        Scanner in = new Scanner(System.in, "utf-8");
        int i = 0;
        // FssJsonInput inputParam = new FssJsonInput();
        MultiHBaseSearch search = HBaseManager.createSearch(str);

        while (true) {

            if (search != null) {

                String str1 = search.getJsonResult(str);

                if (str1 != null) {
                    System.out.println(format(str1));
                }
                // JsonInput
                FssJsonInput inputParam = JSON.parseObject(str, FssJsonInput.class);
                FssReportServiceIn serviceInput = inputParam.getReportService();

                if (str1 != null && !str1.equals("")) {
                    // jsonOutput 获取新的lastId
                    JSONObject jsonOut = JSON.parseObject(str);
                    JSONObject serviceOut = jsonOut.getJSONObject("reportService");
                    JSONObject fssSearch = serviceOut.getJSONObject("fssSearch");
                    JSONObject page = fssSearch.getJSONObject("page");
                    String lastPageIdx = page.getString("index");
                    int pageIndex = Integer.parseInt(lastPageIdx) + 1;
                    serviceInput.getFssSearch().getPage().setIndex(String.valueOf(pageIndex));
                    inputParam.setReportService(serviceInput);

                    str = JSON.toJSONString(inputParam);

                }
            }
            System.out.print("下一页 [0 -(不查询) / 1 -（查询）] : ");
            i = in.nextInt();
            if (i == 0) {
                break;
            }
        }
        System.out.println("SearchByTrial Test end!");
    }

    public static void getPeerTrackSearch() throws Exception {
        // 李青4
        String feature = "7qpXQsFdAAAAAQAAxrs+PXKfAr44a6M9jABIvcPD/zy++xU9nlYIu79bWbx57YO9+14pvefrDT25vmS8GP6wPfaNd71k1kO9aj+CvTB0vr30/Go9zWSDvSmBXj2Wbc86zi3Uu+8VPr3R9xC9imGVPflzbr14XNS8ozIgO/blBL0UyhS9Z+RSPfBYA75VF9e9sS75u3bOtjxXvUu9Cc2kvMJw1D2HJ7y8QNPku6gMuz3NmT28I6XGPUvOb70+0x89TCvgPSlMBr0h0Fe9uVV7vYMyKr2jtEg85yELvvQA8jycIvS9UfiEu1qcA73u2kq9vCfNPC2dlD3GL5U9JcrBvVo0db3TWyE9Cb0YPb/gFjzvYp+7gfb1PYy/DTz6CQO8PdQSO9xdY73s4BM+x6FZvWAa7jwBog++pKajPRqboLyoZIq9SrCEvUFTRj1Gx6c8qAJ5vTm1/D33a9y96HtzPOwMir3WAdk9DPBvPfM9or2I4DS8BbsIvR699zzCBko9DqssvMf/Mb33daU8jbV7vGlvpD3a8po9ztsZPq7sbT3ySNS7rBNNPb/Z0LxMc4i9/PenvJ7X2bykOjQ9sowtPSfJAz6hqnC9jtM3PQyR5r3o0GU9HuaVPdZUkj29rRC9vFfGPdyUWb3FDrk8sEd2uulKsbsWIX+66JsUPfdGXj1BdKE9JsIWPQTkH71vNqM8/JVYPdTyi73GNBa9PeQUvEYWaj0Qdw292Fw5PAvAu73o4sy86jQgPY5gyrxQ2wc+Out9PfEf9TxUaru6eTGJvRvemT1NiaK6eUE+vZV/XTvGMMo9NQt/vZFjnLzMJbC8mmu2vHoMgjzSMKA8prhbu20FAr7xFXU9bplVPUv2xz1JuI28MmTxuwWZsDxmIbg8JXadPdhRu72aL5W69JkHPt9v2ru87O49Hfa2PHvLub251Jo9Lm/wvVde1bw8WKo9V3qdvXRQkL141ZQ99IxDvQ+wTz0m4ne7CWc5Ppt3rj0Uaa+8xUtavSvMpL3B5xm73cOzO/gjMz1QHU09MYiAPe2mND2+jp09tXqzvJUx6DwtMSc9+mz1vPQ8Rj2BL5s9klbeO90XyTzbSlq9izwAvY9lrDsSMzs9ZhJJvbIo8TwVfeI979AdvUPvhTlUWR89RdfROxYim7yqWCo9uRx2vRpYwD28lOk9SElavagYzrxxWFE9QX8VvSuPK72rIQE+xHT/POiihz1GjXc60NQhuuJ7eL3/ezo7oA2vvZnExL0BIe88n6qbvHzFsT1QODU9lZyoOUZrTrw37v880WMpPbDcQb7j7w28hw02vZ2mbzw+jxg889CEPGDP070vT389I8XOPTUPsTwlFHq9SdVbvRewqb24ERm970mEPQ==";
        String str = "{\n" + "'reportService': {\n" + "'id': '12008',\n" + "'type': 'request',\n"
                + "'peerTrackParam': {\t\n" + " 'startTime': '2017-10-23 00:00:00',\n"
                + "'page':{'size':'10','index':'1'},\n" + " 'endTime': '2017-10-24 23:59:59',\n" + " 'topN':30,\n"
                + " 'peerInterval':10,\n" + "'officeIds': ['010100000','11000020001','11000020003','11010120006'],\n"
                + "'searchFeature':['" + feature + "']" + "    }    \n" + "  }\n" + "}";

        // + "'searchFeatures':['" + feature11 + "']," 15:25:36
        // string
        System.out.println(str);
        MultiHBaseSearch search = HBaseManager.createSearch(str);

        if (search != null) {
            String str1 = search.getJsonResult(str);
            if (str1 != null) {
                System.out.println(format(str1));
            }

        }

        System.out.println("Relationship Search Test end!");

    }

    public static void main(String[] args) throws Exception {
        try {
            // 初始化
            //String hostUrl = "hdfs://lv102.dct-znv.com:8020/user/fss_V113/development";
            String hostUrl = "hdfs://lv120.dct-znv.com:8020/user/fss_V113/development";
            HBaseConfig.initConnection(hostUrl);

            // String filePath = "hdfs://znv.bfw02:8020/user/hbaseconfig/HBaseConfAndTables-DCIM.json"; //
            // lv09.dct-znv.com
            // znv.bfw02
            // HBaseConfig.initConnection(filePath);

            // 获取查询结果
            while (true) {
                Scanner in = new Scanner(System.in, "utf-8");
                String searchcontext = "[0-不查询, 1- 以图搜图（图） ，2- 黑名单静态比对 ，3- 以图搜图（时间），"
                        + " 4- 历史人物关系查询，5- 驻留时间查询, 6-轨迹查询,7-同行人轨迹分析]\n" + " 请输入查询报表 : ";
                System.out.print(searchcontext);
                int i = in.nextInt();
                switch (i) {
                    case 1:
                        getSearchByImageResult();
                        break;
                    case 2:
                        getBlackStaticResult();
                        break;
                    case 3:
                        getSearchByTimeResult();
                        break;
                    case 4:
                        getRelationshipResult();
                        break;
                    case 5:
                        getStayTimeSearch();
                        break;
                    case 6:
                        getByTrialSearch();
                        break;
                    case 7:
                        getPeerTrackSearch();
                        break;
                    default:
                        break;
                }
                if (i == 0) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 结束，关闭实例
            HBaseConfig.closeConnection();
        }
    }
}
