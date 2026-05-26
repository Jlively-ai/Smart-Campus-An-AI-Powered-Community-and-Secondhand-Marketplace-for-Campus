package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mrxu.stucomplarear2.entity.Image;
import com.mrxu.stucomplarear2.mapper.ImageMapper;
import com.mrxu.stucomplarear2.service.ImageService;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import com.mrxu.stucomplarear2.utils.jwt.JWTUtil;
import com.mrxu.stucomplarear2.utils.response.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.UUID;

@Service
public class ImageServiceImpl extends ServiceImpl<ImageMapper, Image> implements ImageService {

    @Value("${mrxu.stucomplarear2.image.save-path}")
    private String savePath;

    @Override
    public Result uploadImage(HttpServletRequest request, MultipartFile[] files) {
        String token = request.getHeader("Authorization");
        String userId = JWTUtil.getUserId(token);
        StringBuilder urls = new StringBuilder();
        // 确保保存目录存在
        java.io.File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;
            File dest = new File(saveDir, newFileName);
            try {
                file.transferTo(dest);
                Image image = new Image();
                image.setImageId(IdGenerator.generateId(IdGenerator.IMAGE));
                image.setImageName(originalFilename);
                image.setImageUrl("/images/" + newFileName);
                image.setImageType(extension);
                image.setImageSize(file.getSize());
                image.setUserId(userId);
                image.setCreateTime(new Date());
                this.save(image);
                urls.append(image.getImageUrl()).append(",");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (urls.length() > 0) {
            urls.setLength(urls.length() - 1);
        }
        return Result.succ(urls.toString());
    }

    @Override
    public void viewImage(HttpServletResponse response, String imageId) throws IOException {
        Image image = this.getById(imageId);
        if (image == null) {
            return;
        }
        File file = new File(savePath + File.separator + image.getImageUrl().replace("/images/", ""));
        if (!file.exists()) {
            // 尝试使用绝对路径
            file = new File(new File(savePath).getAbsolutePath(), image.getImageUrl().replace("/images/", ""));
        }
        if (!file.exists()) {
            return;
        }
        response.setContentType("image/jpeg");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }

    @Override
    public Result listImages(int page, int size) {
        Page<Image> pageObj = new Page<>(page, size);
        QueryWrapper<Image> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        Page<Image> imagePage = this.page(pageObj, queryWrapper);
        return Result.succ(imagePage);
    }

    @Override
    public Result deleteById(String imageId) {
        Image image = this.getById(imageId);
        if (image != null) {
            File file = new File(new File(savePath).getAbsolutePath(), image.getImageUrl().replace("/images/", ""));
            if (file.exists()) {
                file.delete();
            }
            this.removeById(imageId);
            return Result.succ("删除成功");
        }
        return Result.fail("图片不存在");
    }
}
