package com.mrxu.stucomplarear2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mrxu.stucomplarear2.entity.Image;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ImageService extends IService<Image> {

    Result uploadImage(HttpServletRequest request, MultipartFile[] files);

    void viewImage(HttpServletResponse response, String imageId) throws IOException;

    Result listImages(int page, int size);

    Result deleteById(String imageId);
}
