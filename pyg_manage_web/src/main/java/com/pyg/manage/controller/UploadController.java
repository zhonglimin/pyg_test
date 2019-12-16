package com.pyg.manage.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

/**
 * 接收上传请求
 */
@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("uploadFile")
    public Result uploadFile(MultipartFile file){
        String extName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
            String file_id = client.uploadFile(file.getBytes(), extName, null);

            return new Result(true,FILE_SERVER_URL+file_id);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }

    }

}
