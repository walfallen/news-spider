package com.yc.spider.conf;

import com.yc.spider.Manager;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by youmingwei on 16/7/19.
 */
public class ConfigUtil {
    private static Map<String,String> settings = new HashMap<>(); //配置信息
    static {
        InputStream configStream;
        try {
            if (Manager.runLocal == true) {
                configStream = ConfigUtil.class.getResourceAsStream("/conf.properties");
            } else {
                configStream = new FileInputStream("./conf.properties");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(configStream,"utf-8"));
            String aline;
            while((aline = br.readLine())!= null){
                String[] splits = aline.split(" = ");
                if(splits.length != 2) continue;
                settings.put(splits[0],splits[1]);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getInt(String key){
        if(!settings.containsKey(key)) return 0;
        else{
            return Integer.parseInt(settings.get(key));
        }
    }

    public static float getFloat(String key){
        if(!settings.containsKey(key)) return 0;
        else{
            return Float.parseFloat(settings.get(key));
        }
    }

    public static String getString(String key){
        if(settings.containsKey(key)){
            return settings.get(key);
        }else{
            return "";
        }
    }

}
