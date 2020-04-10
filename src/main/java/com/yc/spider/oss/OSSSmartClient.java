package com.yc.spider.oss;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Date;

public class OSSSmartClient {
    private Logger logger = LoggerFactory.getLogger(OSSSmartClient.class);
    private OSSClient client;

    /**
     * 使用accessKeyId与accessKeySecret构造实例
     *
     * @param accessKeyId     Access Key ID
     * @param accessKeySecret Access Key Secret
     */
    public OSSSmartClient(String accessKeyId, String accessKeySecret) {
        this.client = new OSSClient(accessKeyId, accessKeySecret);
    }

    /**
     * 使用指定的endpoint构建实例
     *
     * @param endpoint        例如：http://oss-cn-hangzhou.aliyuncs.com
     * @param accessKeyId     Access Key ID
     * @param accessKeySecret Access Key Secret
     */
    public OSSSmartClient(String endpoint, String accessKeyId, String accessKeySecret) {
        this.client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    /**
     * 创建bucket
     *
     * @param bucketName bucket名称
     */
    public void creaeteBucket(String bucketName) {
        if (!this.client.doesBucketExist(bucketName)) {
            this.client.createBucket(bucketName);
            this.client.setBucketAcl(bucketName, CannedAccessControlList.PublicRead);
        }
    }

    /**
     * 上传Object
     *
     * @param bucketName  bucket名称
     * @param key         object的key
     * @param filePath    文件地址
     * @param contentType contentType
     * @return
     * @throws FileNotFoundException
     */
    public String putObject(String bucketName, String key, String filePath, String contentType) throws FileNotFoundException {
        if (!this.client.doesBucketExist(bucketName)) {
            this.client.createBucket(bucketName);
        }
        // 获取指定文件的输入流
        File file = new File(filePath);
        InputStream content = new FileInputStream(file);
        // 创建上传Object的Metadata
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.length());
        meta.setContentType(contentType);
        // 上传
        PutObjectResult result = this.client.putObject(bucketName, key, content, meta);
        // 打印ETag
        return result.getETag();
    }

    /**
     * 上传Object
     *
     * @param bucketName  bucket名称
     * @param key         object的key
     * @param data        二进制数组
     * @param contentType contentType
     * @return
     * @throws FileNotFoundException
     */
    public String putObject(String bucketName, String key, byte[] data, String contentType) throws FileNotFoundException {
        if (!this.client.doesBucketExist(bucketName)) {
            this.client.createBucket(bucketName);
        }
        // 获取指定文件的输入流
        ByteArrayInputStream content = new ByteArrayInputStream(data);
        // 创建上传Object的Metadata
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(data.length);
        meta.setContentType(contentType);
        // 上传
        PutObjectResult result = this.client.putObject(bucketName, key, content, meta);
        // 打印ETag
        return result.getETag();
    }

    /**
     * 创建文件夹
     *
     * @param bucketName bucket名称
     * @param folderName 文件夹名称
     * @throws IOException
     */
    public void createFolder(String bucketName, String folderName) throws IOException {
        String objectName = folderName + "/";
        ObjectMetadata objectMeta = new ObjectMetadata();
        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        objectMeta.setContentLength(0);
        try {
            this.client.putObject(bucketName, objectName, in, objectMeta);
        } finally {
            in.close();
        }
    }

    /**
     * 获得object
     *
     * @param bucketName bucket名称
     * @param key        object的key
     * @return
     */
    public String getObjectUrl(String bucketName, String key) {
        AccessControlList acl = this.client.getBucketAcl(bucketName);
        boolean isPrivate = false;
        if (acl.getGrants().isEmpty()) { //私有
            isPrivate = true;
        }

        Date expiration = new Date(new Date().getTime() + 3600 * 1000); //1h过期
        URL url = this.client.generatePresignedUrl(bucketName, key, expiration);
        String urlString = url.toString();

        if (!isPrivate) {
            return urlString.substring(0, urlString.lastIndexOf("?"));
        } else {
            return urlString;
        }
    }

    /**
     * 删除所有object
     *
     * @param bucketName
     */
    public void deleteAllObject(String bucketName) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(bucketName);
        ObjectListing listing = client.listObjects(listObjectsRequest);
        for (OSSObjectSummary objectSummary : listing.getObjectSummaries()) {
            String key = objectSummary.getKey();
            this.client.deleteObject(bucketName, key);
            logger.info("删除文件：" + key);
        }
        for (String commonPrefix : listing.getCommonPrefixes()) {
            this.client.deleteObject(bucketName, commonPrefix);
            logger.info("删除目录：" + commonPrefix);
        }
    }
}
