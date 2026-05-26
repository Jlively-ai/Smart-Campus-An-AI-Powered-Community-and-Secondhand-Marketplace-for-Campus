package com.mrxu.stucomplarear2.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.entity.Category;
import com.mrxu.stucomplarear2.entity.GoodsCategory;
import com.mrxu.stucomplarear2.mapper.CategoryMapper;
import com.mrxu.stucomplarear2.mapper.GoodsCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    private static final List<String> POST_CATEGORIES = Arrays.asList(
            "学习交流", "生活杂谈", "求助问答", "校园活动", "技术分享",
            "情感话题", "求职招聘", "考研考公", "兼职实习", "租房合租",
            "美食探店", "旅行出行", "影视娱乐", "游戏动漫", "摄影美妆",
            "运动健身", "音乐艺术", "志愿公益", "失物招领", "二手闲置"
    );

    private static final List<String> GOODS_CATEGORIES = Arrays.asList(
            "教材书籍", "电子产品", "生活用品", "运动器材", "服装配饰", "其他",
            "数码配件", "文具办公", "美妆护肤", "食品零食", "乐器音响",
            "租房房源", "票券卡券", "交通工具", "家居装饰", "母婴用品",
            "宠物用品", "图书音像", "手工文创", "虚拟服务"
    );

    @Override
    public void run(String... args) {
        initPostCategories();
        initGoodsCategories();
    }

    private void initPostCategories() {
        List<Category> existing = categoryMapper.selectList(null);
        List<String> existingNames = existing.stream().map(Category::getCategoryName).collect(Collectors.toList());
        for (String name : POST_CATEGORIES) {
            if (!existingNames.contains(name)) {
                Category c = new Category();
                c.setCategoryName(name);
                categoryMapper.insert(c);
            }
        }
    }

    private void initGoodsCategories() {
        List<GoodsCategory> existing = goodsCategoryMapper.selectList(null);
        List<String> existingNames = existing.stream().map(GoodsCategory::getGoodsCategoryName).collect(Collectors.toList());
        for (String name : GOODS_CATEGORIES) {
            if (!existingNames.contains(name)) {
                GoodsCategory c = new GoodsCategory();
                c.setGoodsCategoryName(name);
                goodsCategoryMapper.insert(c);
            }
        }
    }
}
