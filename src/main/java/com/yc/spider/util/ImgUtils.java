package com.yc.spider.util;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.process.ArrayListOutputConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;


/**
 * Created by xsdlr on 15/4/10.
 */
public class ImgUtils {
    static Logger logger = LoggerFactory.getLogger(ImgUtils.class);

    public static boolean convertSize(int[][] sizes, String path, String fileName, String postfix, int oWidth, int oHeight) {
        boolean result = true;
        if (sizes != null && sizes.length > 0) {
            String directoryPath = new File("").getAbsolutePath() + "/";
            String inPath = directoryPath + path + "0x0/" + fileName + "." + postfix;
            for (int i = 0; i < sizes.length; i++) {
                int width = sizes[i][0];
                int height = sizes[i][1];
                Integer targetWidth = width;
                Integer targetHeight = height;
                String dir = path + width + "x" + height + "/";
                try {
                    FileUtils.forceMkdir(new File(dir));
                } catch (IOException e) {
                    logger.error("", e);
                }
                String outPath = directoryPath + path + width + "x" + height + "/" + fileName + ".jpg";
                //是否为0x0
                boolean flag = width != 0 || height != 0;
                if (width == 0) {
                    targetWidth = null;
                    width = oWidth;
                }
                if (height == 0) {
                    targetHeight = null;
                    height = oHeight;
                }
                //原始的宽和高都比压缩设定值小就按照原图比例
                if (width > oWidth && height > oHeight && oWidth > 0 && oHeight > 0) {
                    targetWidth = oWidth;
                    targetHeight = oHeight;
                }
                if (flag) {
                    String targetType = "jpg";
                    if (ImgUtils.isAnimation(inPath)) {
                        inPath += "[0]";
                    }

                    //TODO 240*160的图要切图
//                    if (i == 0) {
//                        if (1.0 * oWidth / oHeight >= 1.0 * width / height) {
//                            result = result && zoomImage(inPath, outPath, 10000, height, targetType, null);
//                        } else {
//                            result = result && zoomImage(inPath, outPath, width, 10000, targetType, null);
//                        }
//                        result = result && cutImage(outPath, outPath, 0, 0, width, height);
//                    } else {
//                        result = result && zoomImage(inPath, outPath, targetWidth, targetHeight, targetType, null);
//                    }
                    if (1.0 * oWidth / oHeight >= 1.0 * width / height) {
                        result = result && zoomImage(inPath, outPath, 10000, height, targetType, null);
                    } else {
                        result = result && zoomImage(inPath, outPath, width, 10000, targetType, null);
                    }
                    result = result && cutImage(outPath, outPath, 0, 0, width, height);
                }
            }
        }
        return result;
    }

    /**
     * 获得图片文件大小[小技巧来获得图片大小]
     *
     * @param imagePath * 文件路径
     * @return 文件大小
     */

    public static int getSize(String imagePath) {
        int size = 0;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imagePath);
            size = inputStream.available();
            inputStream.close();
            inputStream = null;
        } catch (FileNotFoundException e) {
            size = 0;
            logger.error("图片文件未找到", e);
        } catch (IOException e) {
            size = 0;
            logger.error("读取文件大小错误", e);
        } catch (Exception e) {
            size = 0;
            logger.error("读取文件错误", e);
        } finally {
            // 可能异常为关闭输入流,所以需要关闭输入流
            if (null != inputStream) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭文件读入流异常", e);
                }
                inputStream = null;
            }
        }
        return size;
    }

    /**
     * 获得图片的宽度
     *
     * @param imagePath 文件路径
     * @return 图片宽度
     */
    public static int getWidth(String imagePath) {
        boolean isAnimation = isAnimation(imagePath);
        if (isAnimation) {
            imagePath += "[0]";
        }
        int line = 0;
        try {
            IMOperation op = new IMOperation();
            op.format("%w"); // 设置获取宽度参数
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = Integer.parseInt(cmdOutput.get(0));
        } catch (Exception e) {
            line = 0;
            logger.error("运行指令出错", e);
        }
        return line;
    }

    /**
     * 判断是否为animation
     *
     * @param imagePath
     * @return
     */
    public static boolean isAnimation(String imagePath) {
        try {
            String line = null;
            IMOperation op = new IMOperation();
            op.format("%s"); // 设置获取压缩类型参数
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = cmdOutput.get(0);
            if (line != null && !line.equals("0")) {
                return true;
            }
        } catch (Exception e) {
            logger.error("运行指令出错", e);
        }
        return false;
    }

    /**
     * 获得图片的高度
     *
     * @param imagePath 文件路径
     * @return 图片高度
     */
    public static int getHeight(String imagePath) {
        boolean isAnimation = isAnimation(imagePath);
        if (isAnimation) {
            imagePath += "[0]";
        }
        int line = 0;
        try {
            IMOperation op = new IMOperation();

            op.format("%h"); // 设置获取高度参数
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = Integer.parseInt(cmdOutput.get(0));
        } catch (Exception e) {
            line = 0;
            logger.error("运行指令出错", e);
        }
        return line;
    }

    /**
     * 获得图片格式类型
     *
     * @param imagePath
     * @return
     */
    public static String getImageFormat(String imagePath) {
        boolean isAnimation = isAnimation(imagePath);
        if (isAnimation) {
            imagePath += "[0]";
        }
        String line = null;
        try {
            IMOperation op = new IMOperation();
            op.format("%m");
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = cmdOutput.get(0);
        } catch (Exception e) {
            logger.error("", e);
        }
        return line;
    }

    /**
     * 图片信息
     *
     * @param imagePath
     * @return
     */
    public static String getImageInfo(String imagePath) {
        String line = null;
        try {
            IMOperation op = new IMOperation();
            op.format("width:%w,height:%h,path:%d%f,size:%b%[EXIF:DateTimeOriginal]");
            op.addImage(1);
            IdentifyCmd identifyCmd = new IdentifyCmd(true);
            ArrayListOutputConsumer output = new ArrayListOutputConsumer();
            identifyCmd.setOutputConsumer(output);
            identifyCmd.run(op, imagePath);
            ArrayList<String> cmdOutput = output.getOutput();
            assert cmdOutput.size() == 1;
            line = cmdOutput.get(0);
        } catch (Exception e) {
            logger.error("", e);
        }
        return line;
    }

    /**
     * 裁剪图片
     *
     * @param imagePath 源图片路径
     * @param newPath   处理后图片路径
     * @param x         起始X坐标
     * @param y         起始Y坐标
     * @param width     裁剪宽度
     * @param height    裁剪高度
     * @return 返回true说明裁剪成功, 否则失败
     */
    public static boolean cutImage(String imagePath, String newPath, int x, int y,
                                   int width, int height) {
        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
            op.strip();
            op.gaussianBlur(0.5d);
            /** width：裁剪的宽度 * height：裁剪的高度 * x：裁剪的横坐标 * y：裁剪纵坐标 */
            op.crop(width, height, x, y);
            op.addImage(newPath);
            ConvertCmd convert = new ConvertCmd(true);
            convert.run(op);
            flag = true;
        } catch (IOException e) {
            flag = false;
            logger.error("文件读取错误", e);
        } catch (InterruptedException e) {
            flag = false;
            logger.error("", e);
        } catch (IM4JavaException e) {
            flag = false;
            logger.error("", e);
        } finally {

        }
        return flag;
    }

    /**
     * 根据尺寸缩放图片[等比例缩放:参数height为null,按宽度缩放比例缩放;参数width为null,按高度缩放比例缩放]
     *
     * @param imagePath   源图片路径
     * @param newPath     处理后图片路径
     * @param width       缩放后的图片宽度
     * @param height      缩放后的图片高度
     * @param convertType 缩放后的图片类型
     * @return 返回true 说明缩放成功, 否则失败
     */
    public static boolean zoomImage(String imagePath, String newPath, Integer width,
                                    Integer height, String convertType, Double quality) {
        int max = Integer.MAX_VALUE;
        boolean flag = false;
        try {
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
            if (quality != null) {
                op.quality(quality);
            }
            op.strip();
            op.gaussianBlur(0.5d);
            if (width == null) {// 根据高度缩放图片
                op.resize(max, height);
            } else if (height == null) {// 根据宽度缩放图片
                op.resize(width, max);
            } else {
                op.resize(width, height);
            }
            if (convertType != null && !convertType.isEmpty()) {
                op.addImage(convertType + ":" + newPath);
            } else {
                op.addImage(newPath);
            }
            ConvertCmd convert = new ConvertCmd(true);
            convert.run(op);
            flag = true;
        } catch (IOException e) {
            flag = false;
            logger.error("文件读取错误", e);
        } catch (InterruptedException e) {
            flag = false;
            logger.error("", e);
        } catch (IM4JavaException e) {
            flag = false;
            logger.error("", e);
        } finally {
        }
        return flag;
    }

    /**
     * 图片旋转
     *
     * @param imagePath 源图片路径
     * @param newPath   处理后图片路径
     * @param degree    旋转角度
     */
    public static boolean rotate(String imagePath, String newPath, double degree) {
        boolean flag = false;
        try {
            // 1.将角度转换到0-360度之间
            degree = degree % 360;
            if (degree <= 0) {
                degree = 360 + degree;
            }
            IMOperation op = new IMOperation();
            op.addImage(imagePath);
            op.rotate(degree);
            op.addImage(newPath);
            ConvertCmd cmd = new ConvertCmd(true);
            cmd.run(op);
            flag = true;
        } catch (Exception e) {
            flag = false;
            logger.error("图片旋转失败", e);
        }
        return flag;
    }
    public static void downloadToFile(String urlPath, String filePath) throws Exception {
        Response response = Request.Get(urlPath).execute();
        HttpResponse httpResponse = response.returnResponse();
        Header[] headers = httpResponse.getHeaders("Content-Type");
        boolean isImage = Stream.of(headers).anyMatch((value) -> value.getValue().startsWith("image"));
        if (isImage) {
            byte[] data = EntityUtils.toByteArray(httpResponse.getEntity());
            FileUtils.writeByteArrayToFile(new File(filePath), data);
        } else {
            Request.Get(urlPath).execute().saveContent(new File(filePath));
        }
    }
}
