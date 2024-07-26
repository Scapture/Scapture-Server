package com.server.scapture.util.S3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/* S3Service.java */
@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {
    @Value("${cloud.aws.s3.videoBucketName}")
    private String videoBucket;
    @Value("${cloud.aws.s3.stadiumImageBucketName}")
    private String stadiumImageBucket;
    @Value("${cloud.aws.s3.userImageBucketName}")
    private String userImageBucket;
    private final AmazonS3 amazonS3;

    public String upload(MultipartFile multipartFile, String dirName, String fileName) throws IOException {
        String name = dirName + "/" + fileName;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3.putObject(stadiumImageBucket, name, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(stadiumImageBucket, name).toString();
    }
    // name: User PK
    public String modifyUserImage(MultipartFile multipartFile, String name) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3.putObject(userImageBucket, name, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(userImageBucket, name).toString();
    }
}
