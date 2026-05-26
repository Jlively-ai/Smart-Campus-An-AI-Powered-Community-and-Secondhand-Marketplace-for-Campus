package com.mrxu.stucomplarear2.controller;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.entity.AiConfig;
import com.mrxu.stucomplarear2.entity.Announcement;
import com.mrxu.stucomplarear2.entity.Goods;
import com.mrxu.stucomplarear2.entity.MarketOrder;
import com.mrxu.stucomplarear2.entity.Post;
import com.mrxu.stucomplarear2.entity.User;
import com.mrxu.stucomplarear2.entity.Wall;
import com.mrxu.stucomplarear2.mapper.MarketOrderMapper;
import com.mrxu.stucomplarear2.mapper.UserMapper;
import com.mrxu.stucomplarear2.service.AiConfigService;
import com.mrxu.stucomplarear2.service.AnnouncementService;
import com.mrxu.stucomplarear2.service.GoodsService;
import com.mrxu.stucomplarear2.service.PostService;
import com.mrxu.stucomplarear2.service.UserService;
import com.mrxu.stucomplarear2.service.WallService;
import com.mrxu.stucomplarear2.utils.response.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 智能助手控制器
 * 支持本地关键词匹配模式和大模型API模式
 * 大模型API兼容OpenAI格式（DeepSeek/通义千问/智谱AI等均兼容）
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiChatController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private PostService postService;

    @Autowired
    private WallService wallService;

    @Autowired
    private AiConfigService aiConfigService;

    @Autowired
    private MarketOrderMapper marketOrderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    private final RestTemplate restTemplate;

    public AiChatController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10秒连接超时
        factory.setReadTimeout(60000);    // 60秒读取超时（大模型响应可能较慢）
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 商品相关意图关键词
     */
    private static final List<String> GOODS_INTENT_KEYWORDS = Arrays.asList(
            "买", "商品", "东西", "物品", "二手", "闲置", "价格", "便宜",
            "教材", "电脑", "手机", "自行车", "电子产品", "衣服", "鞋",
            "包", "耳机", "键盘", "显示器", "平板", "充电器", "台灯", "风扇", "灯"
    );

    /**
     * 帖子相关意图关键词（不包含表白墙）
     */
    private static final List<String> POST_INTENT_KEYWORDS = Arrays.asList(
            "帖子", "文章", "讨论", "话题", "论坛", "热门", "经验", "分享", "求助", "攻略",
            "怎么办", "进不去", "上不去", "连不上", "打不开", "崩溃", "卡顿",
            "学习", "python", "java", "编程", "考试", "选课", "课程", "作业"
    );

    /**
     * 表白墙相关意图关键词（独立于帖子）
     */
    private static final List<String> WALL_INTENT_KEYWORDS = Arrays.asList(
            "表白", "表白墙", "墙", "匿名", "告白", "暗恋", "喜欢", "心动", "小姐姐", "小哥哥"
    );

    /**
     * 数据统计/趋势相关意图关键词
     */
    private static final List<String> STATS_INTENT_KEYWORDS = Arrays.asList(
            "销售趋势", "收入", "营业额", "销量", "报表", "概览",
            "月收入", "周趋势", "年度", "经营", "赚钱"
    );

    /**
     * 公告相关意图关键词
     */
    private static final List<String> ANNOUNCEMENT_INTENT_KEYWORDS = Arrays.asList(
            "公告", "通知", "公告栏", "最新公告", "重要通知", "系统通知", "新功能", "平台动态", "新动态",
            "上线", "上线时间", "更新", "维护", "升级", "停机", "版本", "发布时间", "什么时候上线",
            "平台上线", "系统维护", "网络维护", "校园网"
    );

    /**
     * 用户相关意图关键词
     */
    private static final List<String> USER_INTENT_KEYWORDS = Arrays.asList(
            "用户", "会员", "注册", "活跃用户", "多少人"
    );

    /**
     * 个人数据相关意图关键词
     */
    private static final List<String> PERSONAL_INTENT_KEYWORDS = Arrays.asList(
            "我的帖子", "我的商品", "我的订单", "我发布", "我获得", "我收到",
            "获赞", "点赞", "收藏数", "粉丝数", "关注数", "我的数据",
            "我发了", "我卖", "我买", "多少个赞", "多少赞", "几个赞",
            "我的赞", "我的收藏", "我的粉丝", "我的关注", "我的浏览",
            "个人主页", "个人数据", "我的信息"
    );

    /**
     * 导航/操作意图关键词及对应路由
     */
    private static final Map<String, String> NAVIGATION_MAP = new LinkedHashMap<String, String>() {{
        put("发布商品", "/goodsPublish");
        put("发布闲置", "/goodsPublish");
        put("卖东西", "/goodsPublish");
        put("上架商品", "/goodsPublish");
        put("发帖", "/postPublish");
        put("发布帖子", "/postPublish");
        put("写帖子", "/postPublish");
        put("写文章", "/postPublish");
        put("表白墙", "/wallList");
        put("上墙", "/wallApply");
        put("申请表白", "/wallApply");
        put("我的订单", "/myOrder");
        put("查看订单", "/myOrder");
        put("订单", "/myOrder");
        put("我的商品", "/myGoods");
        put("我的闲置", "/myGoods");
        put("我的帖子", "/myPost");
        put("个人中心", "/myInfo");
        put("个人信息", "/myInfo");
        put("我的资料", "/myInfo");
        put("消息", "/myLetter");
        put("私信", "/myLetter");
        put("数据统计", "/statsPage");
        put("销售数据", "/statsPage");
        put("公告", "/announcementList");
        put("通知", "/announcementList");
        put("商品列表", "/goodsList");
        put("二手市场", "/goodsList");
        put("帖子列表", "/postList");
        put("论坛", "/postList");
    }};

    /**
     * 导航意图关键词
     */
    private static final List<String> NAVIGATION_INTENT_KEYWORDS = Arrays.asList(
            "怎么发布", "如何发布", "怎么发帖", "如何发帖", "去哪发布", "去哪发帖",
            "我想发布", "我要发布", "我想发帖", "我要发帖", "怎么卖", "如何卖",
            "怎么买", "去哪里", "打开", "跳转", "前往", "进入"
    );

    /**
     * 热门/最新排序相关关键词
     */
    private static final List<String> HOT_KEYWORDS = Arrays.asList(
            "热门", "最热", "火", "人气", "最多", "排行", "排名", "top"
    );
    private static final List<String> LATEST_KEYWORDS = Arrays.asList(
            "最新", "最近", "最新发布", "刚发", "新帖", "新"
    );

    /**
     * 获取可用的AI配置列表（供前端用户选择）
     * 返回所有配置，API Key脱敏，用户可自行选择使用哪个模型
     */
    @ApiOperation("获取可用AI配置列表")
    @GetMapping("/configs")
    public Result getConfigs(@RequestParam(required = false) Integer configType) {
        QueryWrapper<AiConfig> wrapper = new QueryWrapper<>();
        if (configType != null) {
            wrapper.eq("config_type", configType);
        }
        wrapper.orderByDesc("is_active").orderByDesc("create_time");
        List<AiConfig> list = aiConfigService.list(wrapper);
        // 脱敏并只返回必要字段
        List<Map<String, Object>> result = new ArrayList<>();
        for (AiConfig config : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", config.getId());
            item.put("configName", config.getConfigName());
            item.put("provider", config.getProvider());
            item.put("modelName", config.getModelName());
            item.put("isActive", config.getIsActive());
            item.put("thinkingEnabled", config.getThinkingEnabled());
            item.put("configType", config.getConfigType());
            result.add(item);
        }
        return Result.succ(result);
    }

    /**
     * AI 润色接口
     * 根据内容类型（帖子/商品/表白墙）对用户输入的内容进行AI润色
     * 优先使用大模型，无大模型时返回原内容
     */
    @ApiOperation("AI内容润色")
    @PostMapping("/polish")
    public Result polish(@RequestBody Map<String, Object> params) {
        String content = (String) params.get("content");
        String type = (String) params.get("type"); // "post" / "goods" / "wall"

        if (content == null || content.trim().isEmpty()) {
            return Result.fail("内容不能为空");
        }
        content = content.trim();

        // 获取激活的大模型配置
        AiConfig activeConfig = getRealActiveConfig();
        if (activeConfig == null || activeConfig.getApiKey() == null || activeConfig.getApiKey().isEmpty()) {
            // 无大模型配置，返回原内容
            Map<String, Object> result = new HashMap<>();
            result.put("polished", content);
            result.put("hint", "未配置AI大模型，无法润色");
            return Result.succ(result);
        }

        try {
            String url = activeConfig.getApiUrl();
            if (!url.contains("/chat/completions")) {
                url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(activeConfig.getApiKey());

            // 根据类型构建不同的润色提示
            String systemPrompt;
            switch (type != null ? type : "post") {
                case "goods":
                    systemPrompt = "你是一个二手商品描述润色助手。请润色用户给出的商品描述，使其更吸引人、信息更清晰。保持原意不变，可以适当补充商品卖点描述，语气要亲切自然。只输出润色后的内容，不要加任何前缀说明。";
                    break;
                case "wall":
                    systemPrompt = "你是一个表白墙内容润色助手。请润色用户给出的表白墙内容，使其更真诚动人、表达更流畅。保持原意和情感不变，语气要真挚。只输出润色后的内容，不要加任何前缀说明。";
                    break;
                default:
                    systemPrompt = "你是一个帖子内容润色助手。请润色用户给出的帖子内容，使其表达更清晰、逻辑更通顺、更有吸引力。保持原意不变，可以优化措辞和排版。只输出润色后的内容，不要加任何前缀说明。";
                    break;
            }

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", content));

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", activeConfig.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 1024);

            if (!Boolean.TRUE.equals(activeConfig.getThinkingEnabled()) && activeConfig.getTemperature() != null) {
                requestBody.put("temperature", activeConfig.getTemperature());
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    String polished = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    Map<String, Object> result = new HashMap<>();
                    result.put("polished", polished);
                    return Result.succ(result);
                }
            }

            // API调用失败，返回原内容
            Map<String, Object> result = new HashMap<>();
            result.put("polished", content);
            result.put("hint", "AI润色失败，已返回原内容");
            return Result.succ(result);

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("polished", content);
            result.put("hint", "AI润色失败，已返回原内容");
            return Result.succ(result);
        }
    }

    /**
     * AI 聊天接口
     * 接收用户消息和历史对话，返回AI回复
     * 支持通过 configId 指定使用哪个大模型配置，"local" 表示本地搜索
     */
    @ApiOperation("AI智能助手对话")
    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, Object> params) {
        String message = (String) params.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.fail("消息不能为空");
        }
        message = message.trim();

        // 获取前端指定的模式
        String mode = (String) params.get("mode"); // "local" 或 "llm" 或 null(自动)
        Object configIdObj = params.get("configId"); // 指定配置ID

        // 获取历史对话（可选）
        List<Map<String, String>> history = new ArrayList<>();
        Object historyObj = params.get("history");
        if (historyObj instanceof List) {
            try {
                history = (List<Map<String, String>>) historyObj;
            } catch (Exception e) {
                history = new ArrayList<>();
            }
        }

        // 如果前端指定了本地搜索模式
        if ("local".equals(mode)) {
            Result result = doLocalSearch(message);
            addSourceTag(result, "本地搜索");
            return result;
        }

        // AI伴侣模式：纯聊天，不搜索平台数据
        if ("companion".equals(mode)) {
            // 如果前端指定了configId，使用对应配置
            if (configIdObj != null) {
                Integer configId = null;
                try {
                    configId = Integer.parseInt(configIdObj.toString());
                } catch (Exception e) {}
                if (configId != null) {
                    AiConfig config = aiConfigService.getById(configId);
                    if (config != null) {
                        Result result = callCompanionApiWithConfig(message, history, config);
                        addSourceTag(result, "AI伴侣");
                        return result;
                    }
                }
            }
            Result result = callCompanionApi(message, history);
            addSourceTag(result, "AI伴侣");
            return result;
        }

        // 如果前端指定了configId，使用对应配置（用户自行选择，不受系统激活状态限制）
        if (configIdObj != null) {
            Integer configId = null;
            try {
                configId = Integer.parseInt(configIdObj.toString());
            } catch (Exception e) {}
            if (configId != null) {
                AiConfig config = aiConfigService.getById(configId);
                if (config != null) {
                    return callLlmApi(config, message, history);
                }
            }
        }

        // 自动模式：使用当前激活的配置
        AiConfig activeConfig = getRealActiveConfig();
        if (activeConfig != null) {
            return callLlmApi(activeConfig, message, history);
        }

        // 无大模型配置时，使用本地关键词匹配
        Result result = doLocalSearch(message);
        addSourceTag(result, "本地搜索");
        return result;
    }

    /**
     * AI伴侣聊天（纯对话，不搜索平台数据）
     * 使用configType=2的AI伴侣配置
     */
    private Result callCompanionApi(String message, List<Map<String, String>> history) {
        // 优先使用AI伴侣专用配置(configType=2)，如果没有则回退到AI助手配置
        AiConfig activeConfig = getRealActiveConfig(2);
        if (activeConfig == null) {
            activeConfig = getRealActiveConfig(1);
        }
        if (activeConfig == null || activeConfig.getApiKey() == null || activeConfig.getApiKey().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("text", "抱歉，AI伴侣暂时不可用，请稍后再试~");
            return Result.succ(result);
        }

        try {
            String url = activeConfig.getApiUrl();
            if (!url.contains("/chat/completions")) {
                url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(activeConfig.getApiKey());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "你是一个温暖、有趣的AI伴侣，陪伴用户聊天。你的特点是：\n" +
                    "1. 语气亲切自然，像朋友一样交流\n" +
                    "2. 善于倾听，会关心用户的感受\n" +
                    "3. 可以聊生活、学习、情感等各种话题\n" +
                    "4. 回复简洁有趣，不要过长\n" +
                    "5. 适当使用语气词让对话更自然\n" +
                    "注意：你只负责聊天陪伴，不搜索平台数据。如果用户问平台相关内容（如商品、帖子等），建议他们使用AI助手搜索。"));

            // 添加历史对话（最近10条）
            if (history != null && !history.isEmpty()) {
                int start = Math.max(0, history.size() - 10);
                for (int i = start; i < history.size(); i++) {
                    Map<String, String> h = history.get(i);
                    String role = h.get("role");
                    String content = h.get("content");
                    if ("user".equals(role) || "assistant".equals(role)) {
                        Map<String, String> historyMsg = new HashMap<>();
                        historyMsg.put("role", role);
                        historyMsg.put("content", content);
                        messages.add(historyMsg);
                    }
                }
            }
            messages.add(Map.of("role", "user", "content", message));

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", activeConfig.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 512);

            if (!Boolean.TRUE.equals(activeConfig.getThinkingEnabled()) && activeConfig.getTemperature() != null) {
                requestBody.put("temperature", activeConfig.getTemperature());
            } else {
                requestBody.put("temperature", 0.8);
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    String reply = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    Map<String, Object> result = new HashMap<>();
                    result.put("text", reply);
                    return Result.succ(result);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("text", "抱歉，我暂时无法回复，请稍后再试~");
            return Result.succ(result);

        } catch (Exception e) {
            log.error("AI伴侣调用失败: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("text", "网络出了点问题，请稍后再试~（" + e.getMessage() + "）");
            return Result.succ(result);
        }
    }

    /**
     * AI伴侣聊天（使用指定配置）
     */
    private Result callCompanionApiWithConfig(String message, List<Map<String, String>> history, AiConfig config) {
        try {
            String url = config.getApiUrl();
            if (!url.contains("/chat/completions")) {
                url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    "你是一个温暖、有趣的AI伴侣，陪伴用户聊天。你的特点是：\n" +
                    "1. 语气亲切自然，像朋友一样交流\n" +
                    "2. 善于倾听，会关心用户的感受\n" +
                    "3. 可以聊生活、学习、情感等各种话题\n" +
                    "4. 回复简洁有趣，不要过长\n" +
                    "5. 适当使用语气词让对话更自然\n" +
                    "注意：你只负责聊天陪伴，不搜索平台数据。如果用户问平台相关内容（如商品、帖子等），建议他们使用AI助手搜索。"));

            if (history != null && !history.isEmpty()) {
                int start = Math.max(0, history.size() - 10);
                for (int i = start; i < history.size(); i++) {
                    Map<String, String> h = history.get(i);
                    String role = h.get("role");
                    String content = h.get("content");
                    if ("user".equals(role) || "assistant".equals(role)) {
                        Map<String, String> historyMsg = new HashMap<>();
                        historyMsg.put("role", role);
                        historyMsg.put("content", content);
                        messages.add(historyMsg);
                    }
                }
            }
            messages.add(Map.of("role", "user", "content", message));

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", config.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 512);

            if (!Boolean.TRUE.equals(config.getThinkingEnabled()) && config.getTemperature() != null) {
                requestBody.put("temperature", config.getTemperature());
            } else {
                requestBody.put("temperature", 0.8);
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    String reply = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    Map<String, Object> result = new HashMap<>();
                    result.put("text", reply);
                    return Result.succ(result);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("text", "抱歉，我暂时无法回复，请稍后再试~");
            return Result.succ(result);

        } catch (Exception e) {
            log.error("AI伴侣调用失败(指定配置): {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("text", "网络出了点问题，请稍后再试~（" + e.getMessage() + "）");
            return Result.succ(result);
        }
    }

    /**
     * 用大模型分析用户意图（极省token，只返回JSON）
     * 返回 { "intent": "goods/post/wall/announcement/general", "keywords": "关键词1,关键词2" }
     */
    private Map<String, String> analyzeIntentWithLlm(AiConfig config, String message) {
        try {
            String intentPrompt = "分析用户问题，返回JSON：{\"intent\":\"板块\",\"keywords\":\"搜索词\"}\n" +
                    "板块：goods(商品买卖),post(帖子讨论),wall(表白墙),announcement(公告通知),general(综合)\n" +
                    "规则：\n" +
                    "1.关于上线/维护/更新/通知→announcement\n" +
                    "2.买/卖/价格/二手物品→goods\n" +
                    "3.表白/暗恋/脱单→wall\n" +
                    "4.求助/讨论/经验/选课→post\n" +
                    "5.不确定→general\n" +
                    "6.keywords只从用户问题中提取2-3个核心搜索词，用逗号分隔，不要添加用户没提到的词\n" +
                    "用户问题：" + message;

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", "你只返回JSON，不返回其他内容。");
            messages.add(sysMsg);
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", intentPrompt);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", config.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 80);
            requestBody.put("temperature", 0.1);

            String url = config.getApiUrl();
            if (!url.endsWith("/chat/completions")) {
                url = url.replaceAll("/+$", "") + "/chat/completions";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getBody() != null) {
                JSONObject json = JSON.parseObject(response.getBody());
                JSONArray choices = json.getJSONArray("choices");
                if (choices != null && choices.size() > 0) {
                    String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    // 提取JSON部分
                    content = content.trim();
                    int start = content.indexOf("{");
                    int end = content.lastIndexOf("}") + 1;
                    if (start >= 0 && end > start) {
                        JSONObject result = JSON.parseObject(content.substring(start, end));
                        Map<String, String> resultMap = new HashMap<>();
                        resultMap.put("intent", result.getString("intent") != null ? result.getString("intent") : "");
                        resultMap.put("keywords", result.getString("keywords") != null ? result.getString("keywords") : "");
                        return resultMap;
                    }
                }
            }
        } catch (Exception e) {
            // 分析失败，返回null，使用本地意图
        }
        return null;
    }

    /**
     * 获取未脱敏的激活配置（内部使用）
     * @param configType 1=AI助手, 2=AI伴侣, null=不限类型
     */
    private AiConfig getRealActiveConfig(Integer configType) {
        QueryWrapper<AiConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("is_active", true);
        if (configType != null) {
            wrapper.eq("config_type", configType);
        }
        return aiConfigService.getOne(wrapper);
    }

    /**
     * 获取未脱敏的激活配置（默认AI助手类型）
     */
    private AiConfig getRealActiveConfig() {
        return getRealActiveConfig(1);
    }

    /**
     * 调用大模型API（OpenAI兼容格式）
     * 兼容 DeepSeek、通义千问、智谱AI、OpenAI 等接口
     *
     * 请求格式：
     * POST {apiUrl}/chat/completions
     * Headers: Authorization: Bearer {apiKey}
     * Body: { model, messages, temperature, max_tokens, thinking, reasoning_effort, stream }
     */
    /**
     * 可以纯本地搜索回答的简单查询模式（无需调用LLM，节省token）
     * 包括：热门排行、最新排行、纯搜索类（有明确搜索意图且不需要LLM总结）
     */
    private static final List<String> LOCAL_ONLY_PATTERNS = Arrays.asList(
            "热门", "最热", "排行", "排名", "最新发布", "最近发布", "最新帖子", "最新商品",
            "最便宜", "最低价", "价格最低", "最火"
    );

    private Result callLlmApi(AiConfig config, String message, List<Map<String, String>> history) {
        // 1. 先用本地意图识别做快速判断
        String localIntent = detectIntent(message);
        List<String> keywords = extractKeywords(message);
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(message::contains);
        boolean isLatestQuery = LATEST_KEYWORDS.stream().anyMatch(message::contains);
        boolean isRankingQuery = isHotQuery || isLatestQuery;

        // 简单排行/搜索类查询直接本地返回，不调用LLM（节省100% token）
        boolean isLocalOnlyQuery = LOCAL_ONLY_PATTERNS.stream().anyMatch(message::contains)
                || (isRankingQuery && keywords.isEmpty());

        // personal/navigate/stats/user意图直接走本地搜索（这些意图不需要LLM处理）
        if (isLocalOnlyQuery || "personal".equals(localIntent) || "navigate".equals(localIntent)
                || "stats".equals(localIntent) || "user".equals(localIntent)) {
            Result result = doLocalSearch(message);
            addSourceTag(result, "本地搜索");
            return result;
        }

        // 2. 第一步：调用大模型分析意图（极省token，只返回JSON）
        String intent = localIntent;
        List<String> searchKeywords = keywords;
        try {
            Map<String, String> intentResult = analyzeIntentWithLlm(config, message);
            if (intentResult != null) {
                String llmIntent = intentResult.get("intent");
                String llmKeywords = intentResult.get("keywords");
                if (llmIntent != null && !llmIntent.isEmpty()) {
                    intent = llmIntent;
                }
                if (llmKeywords != null && !llmKeywords.isEmpty()) {
                    searchKeywords = Arrays.asList(llmKeywords.split(","));
                }
            }
        } catch (Exception e) {
            // 意图分析失败，使用本地意图继续
        }

        // 公告意图用大模型给的关键词做本地搜索
        if ("announcement".equals(intent)) {
            Result result = doLocalSearchWithIntent(intent, searchKeywords, message);
            addSourceTag(result, "本地搜索");
            return result;
        }

        // 3. 按意图+大模型关键词搜索平台数据
        List<Map<String, Object>> goodsList = new ArrayList<>();
        List<Map<String, Object>> postList = new ArrayList<>();
        List<Map<String, Object>> wallList = new ArrayList<>();

        // 根据意图决定搜索哪些类型
        boolean searchGoods = "goods".equals(intent) || "general".equals(intent);
        boolean searchPosts = "post".equals(intent) || "general".equals(intent);
        boolean searchWalls = "wall".equals(intent); // 只有wall意图才搜表白墙

        if (searchGoods) {
            QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
            goodsWrapper.eq("goods_status", true);
            if (!searchKeywords.isEmpty()) {
                buildFuzzyQuery(goodsWrapper, searchKeywords, "goods_name", "goods_detail");
            }
            goodsWrapper.orderByDesc("create_time");
            goodsWrapper.last("LIMIT 3");
            List<Goods> goodsResult = goodsService.list(goodsWrapper);
            for (Goods g : goodsResult) {
                Map<String, Object> item = new HashMap<>();
                item.put("goodsId", g.getGoodsId());
                item.put("goodsName", g.getGoodsName());
                item.put("goodsPrice", g.getGoodsPrice());
                item.put("goodsImages", g.getGoodsImages());
                item.put("goodsDetail", truncate(g.getGoodsDetail(), 50));
                goodsList.add(item);
            }
        }

        if (searchPosts) {
            QueryWrapper<Post> postWrapper = new QueryWrapper<>();
            if (!searchKeywords.isEmpty()) {
                buildFuzzyQuery(postWrapper, searchKeywords, "title", "detail");
            }
            postWrapper.orderByDesc("create_time");
            postWrapper.last("LIMIT 3");
            List<Post> postResult = postService.list(postWrapper);
            for (Post p : postResult) {
                Map<String, Object> item = new HashMap<>();
                item.put("postId", p.getPostId());
                item.put("postTitle", p.getTitle());
                item.put("postDetail", truncate(p.getDetail(), 50));
                item.put("createTime", p.getCreateTime());
                item.put("nickname", getUserNickname(p.getUserId()));
                item.put("viewNum", p.getViewNum());
                item.put("likeNum", p.getLikeNum());
                postList.add(item);
            }
        }

        if (searchWalls) {
            QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
            wallWrapper.eq("audit_state", 1);
            if (!searchKeywords.isEmpty()) {
                buildFuzzyQuery(wallWrapper, searchKeywords, "wall_content");
            }
            boolean isHotWallQuery = HOT_KEYWORDS.stream().anyMatch(message::contains);
            if (isHotWallQuery) {
                wallWrapper.orderByDesc("view_num").orderByDesc("like_num");
            } else {
                wallWrapper.orderByDesc("create_time");
            }
            wallWrapper.last("LIMIT 5");
            List<Wall> wallResult = wallService.list(wallWrapper);
            for (Wall w : wallResult) {
                Map<String, Object> item = new HashMap<>();
                item.put("wallId", w.getWallId());
                item.put("wallContent", truncate(w.getWallContent(), 50));
                item.put("nickname", Boolean.TRUE.equals(w.getIsAnonymous()) ? "匿名" : getUserNickname(w.getUserId()));
                item.put("createTime", w.getCreateTime());
                item.put("viewNum", w.getViewNum());
                item.put("likeNum", w.getLikeNum());
                wallList.add(item);
            }
        }

        // 搜索无结果时，用大模型给的意图+关键词做本地搜索
        if (goodsList.isEmpty() && postList.isEmpty() && wallList.isEmpty()) {
            Result result = doLocalSearchWithIntent(intent, searchKeywords, message);
            addSourceTag(result, "本地搜索");
            return result;
        }

        // ★ 优化3：极简搜索上下文（只发必要字段，大幅压缩token）
        StringBuilder searchContext = new StringBuilder();
        if (!goodsList.isEmpty()) {
            searchContext.append("[G]");
            for (Map<String, Object> g : goodsList) {
                searchContext.append(g.get("goodsName")).append("￥").append(g.get("goodsPrice")).append(";");
            }
        }
        if (!postList.isEmpty()) {
            searchContext.append("[P]");
            for (Map<String, Object> p : postList) {
                searchContext.append(p.get("postTitle")).append(";");
            }
        }
        if (!wallList.isEmpty()) {
            searchContext.append("[W]");
            for (Map<String, Object> w : wallList) {
                searchContext.append(w.get("wallContent")).append(";");
            }
        }

        try {
            // 4. 检查API Key
            if (config.getApiKey() == null || config.getApiKey().isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("mode", "local");
                result.put("llmError", "API密钥未配置");
                result.put("intent", intent);
                if (!goodsList.isEmpty()) result.put("goods", goodsList);
                if (!postList.isEmpty()) result.put("posts", postList);
                if (!wallList.isEmpty()) result.put("walls", wallList);
                result.put("text", "为你找到以下内容：");
                return Result.succ(result);
            }

            // 5. 构建请求URL
            String url = config.getApiUrl();
            if (!url.contains("/chat/completions")) {
                url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
            }

            // 6. 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            // ★ 优化4：精简但功能完整的system prompt
            String systemPrompt = config.getSystemPrompt();
            if (systemPrompt == null || systemPrompt.isEmpty()) {
                systemPrompt = "智联校园全能AI助手，可搜索推荐商品/帖子/表白墙，分析销售数据给建议，引导用户操作，回答校园问题，帮写文案。";
            }
            // 搜索结果会作为独立消息发送，LLM只需基于搜索结果回答
            systemPrompt += "\n搜索结果会单独提供，请基于搜索结果回答，不要自己编造商品/帖子名称。如无搜索结果，建议用户换关键词或前往对应页面查看。回答要简洁。";

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // ★ 优化5：压缩历史对话（只保留最近2轮，且截断长内容）
            int contextRounds = Math.min(config.getContextRounds() != null ? config.getContextRounds() : 2, 2);
            if (history != null && !history.isEmpty()) {
                int start = Math.max(0, history.size() - contextRounds * 2);
                for (int i = start; i < history.size(); i++) {
                    Map<String, String> h = history.get(i);
                    String content = h.get("content");
                    // 截断历史消息中过长内容（避免旧对话消耗大量token）
                    if (content != null && content.length() > 100) {
                        Map<String, String> trimmed = new HashMap<>();
                        trimmed.put("role", h.get("role"));
                        trimmed.put("content", content.substring(0, 100) + "...");
                        messages.add(trimmed);
                    } else {
                        messages.add(h);
                    }
                }
            }

            // ★ 优化6：用户消息不再重复附搜索上下文，改为单独一条系统消息
            // 这样用户消息保持原始长度，搜索上下文作为独立消息更高效
            messages.add(Map.of("role", "user", "content", message));
            // 搜索上下文作为紧跟的系统提示（比附在用户消息后更省token）
            if (searchContext.length() > 0) {
                messages.add(Map.of("role", "system", "content", "搜索结果:" + searchContext.toString()));
            }

            // 7. 构建请求体
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", config.getModelName());
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            // 思考模式（DeepSeek等模型支持）
            if (Boolean.TRUE.equals(config.getThinkingEnabled())) {
                JSONObject thinking = new JSONObject();
                thinking.put("type", "enabled");
                requestBody.put("thinking", thinking);
                if (config.getReasoningEffort() != null && !config.getReasoningEffort().isEmpty()) {
                    requestBody.put("reasoning_effort", config.getReasoningEffort());
                }
            } else {
                if (config.getTemperature() != null) {
                    requestBody.put("temperature", config.getTemperature());
                }
            }

            // ★ 优化7：限制max_tokens为较小值（助手回复不需要太长）
            int maxTokens = config.getMaxTokens() != null ? Math.min(config.getMaxTokens(), 1024) : 512;
            requestBody.put("max_tokens", maxTokens);

            // 8. 发送请求
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject messageObj = choice.getJSONObject("message");

                    Map<String, Object> result = new HashMap<>();
                    result.put("mode", "llm");
                    result.put("source", config.getConfigName() + "（" + config.getModelName() + "）");
                    result.put("text", messageObj.getString("content"));

                    // 返回搜索到的商品、帖子和表白墙（前端展示卡片）
                    if (!goodsList.isEmpty()) result.put("goods", goodsList);
                    if (!postList.isEmpty()) result.put("posts", postList);
                    if (!wallList.isEmpty()) result.put("walls", wallList);

                    // 思考内容
                    String reasoningContent = messageObj.getString("reasoning_content");
                    if (reasoningContent != null && !reasoningContent.isEmpty()) {
                        result.put("reasoningContent", reasoningContent);
                    }

                    JSONObject usage = responseBody.getJSONObject("usage");
                    if (usage != null) {
                        result.put("usage", usage);
                    }

                    return Result.succ(result);
                }
            }

            // API调用失败，降级到本地搜索
            Map<String, Object> result = new HashMap<>();
            result.put("mode", "local");
            result.put("source", "本地搜索（大模型调用失败，已降级）");
            result.put("intent", intent);
            result.put("text", "为你找到以下内容：");
            if (!goodsList.isEmpty()) result.put("goods", goodsList);
            if (!postList.isEmpty()) result.put("posts", postList);
            if (!wallList.isEmpty()) result.put("walls", wallList);
            return Result.succ(result);

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("mode", "local");
            result.put("source", "本地搜索（大模型调用失败，已降级）");
            result.put("intent", intent);
            result.put("text", "为你找到以下内容：");
            if (!goodsList.isEmpty()) result.put("goods", goodsList);
            if (!postList.isEmpty()) result.put("posts", postList);
            if (!wallList.isEmpty()) result.put("walls", wallList);
            return Result.succ(result);
        }
    }

    /**
     * 截断字符串辅助方法
     */
    private String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }

    /**
     * 为结果添加来源标签
     */
    @SuppressWarnings("unchecked")
    private void addSourceTag(Result result, String source) {
        if (result != null && result.getData() instanceof Map) {
            ((Map<String, Object>) result.getData()).put("source", source);
        }
    }

    /**
     * 获取用户昵称
     */
    private String getUserNickname(String userId) {
        if (userId == null || userId.isEmpty()) return "匿名";
        try {
            User user = userService.getById(userId);
            return user != null && user.getNickname() != null && !user.getNickname().isEmpty() ? user.getNickname() : "用户";
        } catch (Exception e) {
            return "用户";
        }
    }

    /**
     * 执行本地搜索（降级模式）
     * 本地搜索用完整关键词，不拆分（拆分是为了LLM节省token）
     */
    /**
     * 用大模型给的意图和关键词做本地搜索
     */
    private Result doLocalSearchWithIntent(String intent, List<String> searchKeywords, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("mode", "local");

        // 特殊意图直接处理
        switch (intent) {
            case "navigate":
                return handleNavigation(message, result);
            case "personal":
                return searchPersonal(message, result);
            case "stats":
                return searchStats(message, result);
            case "announcement":
                return searchAnnouncements(message, result);
            case "wall":
                return searchWallLocal(cleanKeyword(message), result);
            case "user":
                return searchUserInfo(message, result);
        }

        // 用大模型给的关键词拼接成搜索词
        String keyword = searchKeywords.isEmpty() ? cleanKeyword(message) : String.join("", searchKeywords);
        result.put("keyword", keyword);

        switch (intent) {
            case "goods":
                return searchGoodsLocal(keyword, message, result);
            case "post":
                return searchPostsLocal(keyword, message, result);
            default:
                return searchGeneralLocal(keyword, message, result);
        }
    }

    private Result doLocalSearch(String message) {
        String intent = detectIntent(message);
        Map<String, Object> result = new HashMap<>();
        result.put("intent", intent);
        result.put("mode", "local");

        switch (intent) {
            case "navigate":
                return handleNavigation(message, result);
            case "personal":
                return searchPersonal(message, result);
            case "stats":
                return searchStats(message, result);
            case "announcement":
                return searchAnnouncements(message, result);
            case "wall":
                return searchWallLocal(cleanKeyword(message), result);
            case "user":
                return searchUserInfo(message, result);
            default:
                break;
        }

        // 商品/帖子搜索用完整关键词，传入原始消息用于热门判断
        String keyword = cleanKeyword(message);
        result.put("keyword", keyword);

        switch (intent) {
            case "goods":
                return searchGoodsLocal(keyword, message, result);
            case "post":
                return searchPostsLocal(keyword, message, result);
            default:
                return searchGeneralLocal(keyword, message, result);
        }
    }

    private Result doLocalSearch(String message, Map<String, Object> result) {
        String intent = detectIntent(message);
        result.put("intent", intent);
        if (!result.containsKey("mode")) {
            result.put("mode", "local");
        }

        switch (intent) {
            case "navigate":
                return handleNavigation(message, result);
            case "personal":
                return searchPersonal(message, result);
            case "stats":
                return searchStats(message, result);
            case "announcement":
                return searchAnnouncements(message, result);
            case "wall":
                return searchWallLocal(cleanKeyword(message), result);
            case "user":
                return searchUserInfo(message, result);
            default:
                break;
        }

        String keyword = cleanKeyword(message);
        result.put("keyword", keyword);

        switch (intent) {
            case "goods":
                return searchGoodsLocal(keyword, message, result);
            case "post":
                return searchPostsLocal(keyword, message, result);
            default:
                return searchGeneralLocal(keyword, message, result);
        }
    }

    private Result doLocalSearch(String message, String configName, String errorHint) {
        Map<String, Object> result = new HashMap<>();
        result.put("mode", "local");
        if (configName != null) {
            result.put("configName", configName);
        }
        if (errorHint != null) {
            result.put("llmError", errorHint);
        }
        return doLocalSearch(message, result);
    }

    /**
     * 降级到本地搜索（旧方法兼容）
     */
    private Result fallbackToLocalSearch(String message, String configName) {
        return doLocalSearch(message, configName, null);
    }

    /**
     * 测试AI配置连接
     */
    @ApiOperation("测试AI配置连接")
    @PostMapping("/test")
    public Result testConnection(@RequestBody AiConfig config) {
        if (config.getApiUrl() == null || config.getApiUrl().isEmpty()) {
            return Result.fail("API地址不能为空");
        }
        if (config.getModelName() == null || config.getModelName().isEmpty()) {
            return Result.fail("模型名称不能为空");
        }

        // 如果API密钥为空，尝试从数据库获取（编辑时留空表示不修改）
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("****")) {
            if (config.getId() != null) {
                AiConfig existingConfig = aiConfigService.getById(config.getId());
                if (existingConfig != null) {
                    apiKey = existingConfig.getApiKey();
                }
            }
        }
        if (apiKey == null || apiKey.isEmpty()) {
            return Result.fail("API密钥不能为空");
        }

        try {
            String url = config.getApiUrl();
            if (!url.contains("/chat/completions")) {
                url = url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 构建最小测试请求
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", config.getModelName());
            requestBody.put("max_tokens", 50);
            requestBody.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", "你好"));
            requestBody.put("messages", messages);

            // 思考模式
            if (Boolean.TRUE.equals(config.getThinkingEnabled())) {
                JSONObject thinking = new JSONObject();
                thinking.put("type", "enabled");
                requestBody.put("thinking", thinking);
                if (config.getReasoningEffort() != null) {
                    requestBody.put("reasoning_effort", config.getReasoningEffort());
                }
            }

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);

            // 使用带超时的restTemplate
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject responseBody = JSON.parseObject(response.getBody());
                JSONArray choices = responseBody.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("reply", content);
                    // 返回模型信息
                    result.put("model", responseBody.getString("model"));
                    JSONObject usage = responseBody.getJSONObject("usage");
                    if (usage != null) {
                        result.put("usage", usage);
                    }
                    return Result.succ(result);
                }
            }

            return Result.fail("连接成功但响应格式异常");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = "未知错误";
            }
            // 提取更友好的错误信息
            if (errorMsg.contains("Connection refused") || errorMsg.contains("连接被拒绝")) {
                errorMsg = "连接被拒绝，请检查API地址是否正确";
            } else if (errorMsg.contains("timed out") || errorMsg.contains("Timeout") || errorMsg.contains("超时")) {
                errorMsg = "连接超时，请检查API地址是否可达或增加超时时间";
            } else if (errorMsg.contains("401") || errorMsg.contains("Unauthorized")) {
                errorMsg = "认证失败，请检查API密钥是否正确";
            } else if (errorMsg.contains("403") || errorMsg.contains("Forbidden")) {
                errorMsg = "无权限访问，请检查API密钥权限";
            } else if (errorMsg.contains("404")) {
                errorMsg = "接口地址不存在，请检查API地址和模型名称是否正确";
            } else if (errorMsg.contains("429")) {
                errorMsg = "请求过于频繁，请稍后再试";
            } else if (errorMsg.length() > 150) {
                errorMsg = errorMsg.substring(0, 150) + "...";
            }
            return Result.fail("连接失败：" + errorMsg);
        }
    }

    /**
     * 检测用户意图
     * 优先级：导航 > 个人 > 公告 > 统计 > 表白墙 > 商品 > 帖子 > 通用
     */
    private String detectIntent(String message) {
        // 查询类关键词：如果包含这些词，说明是搜索/查询，不是导航
        List<String> queryIndicators = Arrays.asList(
                "有什么", "是多少", "多少", "最新", "最近", "有没有", "哪些",
                "查看", "搜索", "查找", "推荐", "热门", "分析", "统计",
                "趋势", "数据", "内容", "列表", "概况", "怎么样", "如何"
        );
        boolean isQuery = queryIndicators.stream().anyMatch(message::contains);

        // 只有明确要操作/跳转时才识别为导航
        if (!isQuery) {
            boolean hasNavIntent = NAVIGATION_INTENT_KEYWORDS.stream().anyMatch(message::contains);
            if (hasNavIntent) {
                for (String navKey : NAVIGATION_MAP.keySet()) {
                    if (message.contains(navKey)) {
                        return "navigate";
                    }
                }
            }
        }

        // 个人数据查询（优先级高，因为涉及隐私）
        for (String kw : PERSONAL_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "personal";
        }

        // 公告
        for (String kw : ANNOUNCEMENT_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "announcement";
        }

        // 统计
        for (String kw : STATS_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "stats";
        }

        // 表白墙（必须在帖子之前检测，因为"墙"等词可能和帖子重叠）
        for (String kw : WALL_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "wall";
        }

        // 商品
        for (String kw : GOODS_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "goods";
        }

        // 帖子
        for (String kw : POST_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "post";
        }

        // 用户
        for (String kw : USER_INTENT_KEYWORDS) {
            if (message.contains(kw)) return "user";
        }

        return "general";
    }

    /**
     * 智能提取搜索关键词
     * 1. 去掉语气词和连接词
     * 2. 去掉意图词（但保留有搜索价值的词如"灯"、"教材"等）
     * 3. 如果清理后为空或太通用，返回空字符串（调用方会跳过关键词过滤）
     */
    private String cleanKeyword(String message) {
        String cleaned = message;
        // 去掉语气词、连接词、疑问词
        String[] fillerPhrases = {
                "推荐一些", "帮我找", "帮我搜索", "帮我查", "我想买", "我想找",
                "有没有", "有什么", "我想", "我要", "能不能", "可以",
                "请问", "请问一下", "想问", "想知道",
                "吗", "？", "?", "呢", "啊", "吧", "了", "的", "一", "一个",
                "最近", "最新", "热门", "最热", "有什么", "有没有"
        };
        for (String phrase : fillerPhrases) {
            cleaned = cleaned.replace(phrase, "");
        }
        cleaned = cleaned.trim();

        // 通用词列表：这些词不适合做LIKE搜索，返回空让调用方跳过过滤
        List<String> genericWords = Arrays.asList(
                "商品", "东西", "物品", "二手", "闲置", "帖子", "文章", "讨论", "论坛",
                "公告", "通知", "公告栏", "最新公告", "重要通知", "系统通知",
                "用户", "个人", "自己", "粉丝", "关注", "关注者",
                "表白墙", "墙", "表白", "匿名"
        );
        if (genericWords.contains(cleaned) || cleaned.isEmpty()) {
            return "";
        }

        return cleaned;
    }

    /**
     * 搜索统计数据（销售趋势、经营建议等）
     */
    private Result searchStats(String message, Map<String, Object> result) {
        // 获取全局统计数据
        QueryWrapper<MarketOrder> orderWrapper = new QueryWrapper<>();
        orderWrapper.in("order_status", 1, 2, 3, 5);
        List<MarketOrder> allOrders = marketOrderMapper.selectList(orderWrapper);
        long totalOrders = allOrders.size();
        double totalRevenue = allOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

        // 本月数据
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(year, month, 1, 0, 0, 0);
        monthStart.set(Calendar.MILLISECOND, 0);

        QueryWrapper<MarketOrder> monthWrapper = new QueryWrapper<>();
        monthWrapper.in("order_status", 1, 2, 3, 5);
        monthWrapper.ge("create_time", monthStart.getTime());
        List<MarketOrder> monthOrders = marketOrderMapper.selectList(monthWrapper);
        long monthOrders2 = monthOrders.size();
        double monthRevenue = monthOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum();

        // 近7天趋势
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        List<Map<String, Object>> weekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -i);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date dayEnd = cal.getTime();

            QueryWrapper<MarketOrder> dayWrapper = new QueryWrapper<>();
            dayWrapper.in("order_status", 1, 2, 3, 5);
            dayWrapper.ge("create_time", dayStart);
            dayWrapper.le("create_time", dayEnd);
            List<MarketOrder> dayOrders = marketOrderMapper.selectList(dayWrapper);

            Map<String, Object> item = new HashMap<>();
            item.put("date", sdf.format(dayStart));
            item.put("orderCount", dayOrders.size());
            item.put("revenue", Math.round(dayOrders.stream().mapToDouble(o -> o.getTotalPrice() != null ? o.getTotalPrice() : 0).sum() * 100) / 100.0);
            weekTrend.add(item);
        }

        // 商品分类统计
        long goodsTotal = goodsService.count();
        long postTotal = postService.count();
        long userTotal = userMapper.selectCount(null);

        // 构建文本回复
        StringBuilder text = new StringBuilder();
        text.append("📊 平台数据概览\n\n");
        text.append("🔹 总订单：").append(totalOrders).append("单\n");
        text.append("🔹 总销售额：￥").append(String.format("%.2f", totalRevenue)).append("\n");
        text.append("🔹 本月订单：").append(monthOrders2).append("单\n");
        text.append("🔹 本月销售额：￥").append(String.format("%.2f", monthRevenue)).append("\n");
        text.append("🔹 平台用户：").append(userTotal).append("人\n");
        text.append("🔹 在售商品：").append(goodsTotal).append("件\n");
        text.append("🔹 帖子数量：").append(postTotal).append("篇\n\n");

        // 近7天趋势
        text.append("📈 近7天销售趋势\n");
        for (Map<String, Object> day : weekTrend) {
            text.append(day.get("date")).append("：").append(day.get("orderCount")).append("单 / ￥").append(day.get("revenue")).append("\n");
        }

        // 简单建议
        text.append("\n💡 经营建议\n");
        if (monthRevenue > 0) {
            double avgOrderPrice = totalRevenue / Math.max(totalOrders, 1);
            text.append("• 平均客单价：￥").append(String.format("%.2f", avgOrderPrice)).append("\n");
            if (avgOrderPrice < 50) {
                text.append("• 建议增加高价值商品，提升客单价\n");
            }
            if (monthOrders2 < 5) {
                text.append("• 订单量较少，建议多发布优质商品和帖子增加曝光\n");
            }
        } else {
            text.append("• 暂无销售数据，建议先发布一些热门商品\n");
        }
        text.append("• 关注热门帖子话题，及时上架相关商品\n");
        text.append("• 合理定价，二手商品建议为原价5-7折\n");

        result.put("text", text.toString());
        result.put("stats", Map.of(
                "totalOrders", totalOrders,
                "totalRevenue", Math.round(totalRevenue * 100) / 100.0,
                "monthOrders", monthOrders2,
                "monthRevenue", Math.round(monthRevenue * 100) / 100.0,
                "weekTrend", weekTrend,
                "goodsTotal", goodsTotal,
                "postTotal", postTotal,
                "userTotal", userTotal
        ));
        return Result.succ(result);
    }

    /**
     * 从用户消息中提取多个搜索关键词，用于模糊搜索
     * 例如："选课系统进不去怎么办" → ["选课", "系统", "进不去"]
     * 英文单词保持完整不拆分（如"python"不会拆成"py","yt"）
     */
    private List<String> extractKeywords(String message) {
        // 先去掉常见意图词
        String cleaned = message;
        String[] intentPhrases = {
                "推荐一些", "帮我找", "有没有", "有什么", "我想买", "我想找",
                "帮我搜索", "搜索", "查找", "找一下", "推荐", "便宜的",
                "二手", "闲置", "商品", "物品", "东西", "帖子", "文章",
                "最近", "热门", "最新", "一些", "一下", "吗", "？", "?",
                "呢", "啊", "吧", "的", "了", "怎么办", "怎么", "如何",
                "能不能", "可以", "好吗", "是不是", "为什么", "什么"
        };
        for (String phrase : intentPhrases) {
            cleaned = cleaned.replace(phrase, "");
        }
        // 去掉中文量词组合（一个、一台、一本等），保留后面的名词
        cleaned = cleaned.replaceAll("[一二两三四五六七八九十几多数百千万]+[个只条本件台套部辆架把瓶包袋块张片座所门节节课]", "");
        // 去掉残留的单独量词（可能出现在中间位置）
        cleaned = cleaned.replaceAll("[个只条本件台套部辆架把瓶包袋块张片座所门节节课]", "");
        cleaned = cleaned.trim();

        List<String> keywords = new ArrayList<>();

        // 提取英文单词（保持完整，不拆分成2字符碎片）
        List<String> englishWords = new ArrayList<>();
        StringBuilder enWord = new StringBuilder();
        for (char c : cleaned.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) {
                enWord.append(c);
            } else {
                if (enWord.length() >= 2) {
                    englishWords.add(enWord.toString().toLowerCase());
                }
                enWord = new StringBuilder();
            }
        }
        if (enWord.length() >= 2) {
            englishWords.add(enWord.toString().toLowerCase());
        }
        keywords.addAll(englishWords);

        // 去掉英文部分，只保留中文内容做滑动窗口
        String chinesePart = cleaned.replaceAll("[a-zA-Z0-9]+", " ").trim();

        if (chinesePart.length() >= 2) {
            int len = chinesePart.length();
            if (len == 2) {
                keywords.add(chinesePart);
            } else if (len == 3) {
                keywords.add(chinesePart);
                keywords.add(chinesePart.substring(0, 2));
            } else if (len <= 6) {
                keywords.add(chinesePart);
                for (int i = 0; i <= len - 2; i++) {
                    String sub = chinesePart.substring(i, i + 2);
                    if (!sub.contains(" ") && !isStopPhrase(sub)) {
                        keywords.add(sub);
                    }
                }
            } else {
                for (int i = 0; i <= len - 2; i++) {
                    String sub = chinesePart.substring(i, i + 2);
                    if (!sub.contains(" ") && !isStopPhrase(sub)) {
                        keywords.add(sub);
                    }
                }
                for (int i = 0; i <= len - 3; i++) {
                    String sub = chinesePart.substring(i, i + 3);
                    if (!sub.contains(" ") && !isStopPhrase(sub)) {
                        keywords.add(sub);
                    }
                }
            }
        }

        // 去重并限制数量
        return keywords.stream().distinct().limit(10).collect(Collectors.toList());
    }

    /** 判断是否为无意义的停用短语 */
    private boolean isStopPhrase(String phrase) {
        Set<String> stopPhrases = Set.of(
                "一个", "一些", "一下", "什么", "怎么", "如何", "为什么",
                "可以", "能不能", "是不是", "好吗", "没有", "不是",
                "这个", "那个", "这些", "那些", "哪个", "哪些",
                "关于", "对于", "还是", "或者", "而且", "但是",
                "如果", "因为", "所以", "虽然", "只是", "已经",
                "应该", "可能", "需要", "想要", "希望", "觉得"
        );
        return stopPhrases.contains(phrase);
    }

    /**
     * 构建多关键词模糊搜索条件
     * 对每个关键词在指定字段中做 LIKE 匹配，用 OR 连接
     */
    private <T> QueryWrapper<T> buildFuzzyQuery(QueryWrapper<T> wrapper, List<String> keywords, String... fields) {
        if (keywords == null || keywords.isEmpty()) {
            return wrapper;
        }
        // 只取前5个关键词避免查询过重
        List<String> topKeywords = keywords.stream().limit(5).collect(Collectors.toList());
        wrapper.and(w -> {
            for (int i = 0; i < topKeywords.size(); i++) {
                String kw = topKeywords.get(i);
                if (kw.isEmpty()) continue;
                if (i == 0) {
                    buildFieldLikes(w, kw, fields);
                } else {
                    w.or(inner -> buildFieldLikes(inner, kw, fields));
                }
            }
        });
        return wrapper;
    }

    private <T> void buildFieldLikes(QueryWrapper<T> w, String keyword, String[] fields) {
        if (fields.length == 1) {
            w.like(fields[0], keyword);
        } else {
            w.or(inner -> {
                for (int j = 0; j < fields.length; j++) {
                    if (j == 0) {
                        inner.like(fields[j], keyword);
                    } else {
                        inner.or().like(fields[j], keyword);
                    }
                }
            });
        }
    }

    /**
     * 处理导航意图：返回跳转路由和提示文字
     */
    private Result handleNavigation(String message, Map<String, Object> result) {
        String route = null;
        String targetName = null;
        // 匹配最长的关键词（优先精确匹配）
        for (String navKey : NAVIGATION_MAP.keySet()) {
            if (message.contains(navKey)) {
                route = NAVIGATION_MAP.get(navKey);
                targetName = navKey;
                break; // LinkedHashMap按插入顺序，先匹配的优先
            }
        }
        if (route == null) {
            result.put("text", "抱歉，我不确定你要前往哪个页面。你可以试试说：\"我要发布商品\" 或 \"打开我的订单\"。");
            return Result.succ(result);
        }

        // 页面名称映射
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("/goodsPublish", "发布商品");
        nameMap.put("/postPublish", "发布帖子");
        nameMap.put("/wallList", "表白墙");
        nameMap.put("/wallApply", "申请表白墙");
        nameMap.put("/myOrder", "我的订单");
        nameMap.put("/myGoods", "我的闲置");
        nameMap.put("/myPost", "我的帖子");
        nameMap.put("/myInfo", "个人中心");
        nameMap.put("/myLetter", "消息中心");
        nameMap.put("/statsPage", "数据统计");
        nameMap.put("/announcementList", "公告列表");
        nameMap.put("/goodsList", "商品列表");
        nameMap.put("/postList", "帖子列表");

        String pageName = nameMap.getOrDefault(route, targetName);

        result.put("text", "好的，正在为你跳转到「" + pageName + "」页面 🚀");
        result.put("navigate", route);
        result.put("navigateName", pageName);
        return Result.succ(result);
    }

    /**
     * 搜索公告（支持多关键词模糊搜索）
     */
    private Result searchAnnouncements(String message, Map<String, Object> result) {
        // 公告搜索用cleanKeyword保留更多关键词，同时用extractKeywords做补充
        String mainKeyword = cleanKeyword(message);
        List<String> keywords = extractKeywords(message);
        // 合并去重
        if (!mainKeyword.isEmpty() && !keywords.contains(mainKeyword)) {
            keywords.add(0, mainKeyword);
        }

        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (!keywords.isEmpty()) {
            buildFuzzyQuery(wrapper, keywords, "title", "content");
        }
        wrapper.orderByDesc("create_time").last("LIMIT 5");
        List<Announcement> announcements = announcementService.list(wrapper);

        // 如果关键词搜索无结果，尝试只用主关键词搜索
        if (announcements.isEmpty() && !mainKeyword.isEmpty()) {
            QueryWrapper<Announcement> mainWrapper = new QueryWrapper<>();
            mainWrapper.eq("status", 1);
            mainWrapper.and(w -> w.like("title", mainKeyword).or().like("content", mainKeyword));
            mainWrapper.orderByDesc("create_time").last("LIMIT 5");
            announcements = announcementService.list(mainWrapper);
        }

        // 如果仍然无结果，返回最新公告
        if (announcements.isEmpty()) {
            QueryWrapper<Announcement> fallbackWrapper = new QueryWrapper<>();
            fallbackWrapper.eq("status", 1);
            fallbackWrapper.orderByDesc("create_time").last("LIMIT 3");
            announcements = announcementService.list(fallbackWrapper);
        }

        if (announcements.isEmpty()) {
            result.put("text", "暂无相关公告。");
        } else {
            result.put("text", "为你找到以下公告：");
            result.put("announcements", announcements.stream().map(a -> {
                Map<String, Object> item = new HashMap<>();
                item.put("announcementId", a.getAnnouncementId());
                item.put("title", a.getTitle());
                item.put("content", truncate(a.getContent(), 100));
                item.put("createTime", a.getCreateTime());
                return item;
            }).collect(Collectors.toList()));
        }
        return Result.succ(result);
    }

    /**
     * 搜索用户信息/平台概况/个人数据
     */
    private Result searchUserInfo(String message, Map<String, Object> result) {
        long userCount = userMapper.selectCount(null);
        long goodsCount = goodsService.count();
        long postCount = postService.count();

        // 最近注册的用户
        QueryWrapper<User> recentUserWrapper = new QueryWrapper<>();
        recentUserWrapper.orderByDesc("create_time").last("LIMIT 5");
        List<User> recentUsers = userMapper.selectList(recentUserWrapper);

        // 活跃用户数
        long activeUserCount = postService.list(new QueryWrapper<Post>()
                .select("DISTINCT user_id")).size();

        StringBuilder text = new StringBuilder();

        // 判断是否是个人查询（粉丝/关注我）
        boolean isPersonalQuery = message.contains("关注我") || message.contains("粉丝") || message.contains("我的");

        if (isPersonalQuery) {
            text.append("👥 个人数据\n\n");
            text.append("由于隐私保护，AI无法查看你的个人粉丝/关注数据。\n");
            text.append("请前往「个人中心」页面查看你的粉丝和关注列表。\n\n");
            result.put("navigate", "/myInfo");
            result.put("navigateName", "个人中心");
        } else {
            text.append("👥 平台用户概况\n\n");
            text.append("🔹 注册用户：").append(userCount).append("人\n");
            text.append("🔹 活跃用户：").append(activeUserCount).append("人\n");
            text.append("🔹 在售商品：").append(goodsCount).append("件\n");
            text.append("🔹 帖子数量：").append(postCount).append("篇\n\n");

            if (!recentUsers.isEmpty()) {
                text.append("🆕 最近加入的用户\n");
                for (User u : recentUsers) {
                    text.append("• ").append(u.getNickname() != null ? u.getNickname() : u.getUsername());
                    if (u.getSignature() != null && !u.getSignature().isEmpty()) {
                        text.append(" - ").append(u.getSignature().length() > 20 ?
                                u.getSignature().substring(0, 20) + "..." : u.getSignature());
                    }
                    text.append("\n");
                }
            }
        }

        result.put("text", text.toString());
        result.put("userStats", Map.of(
                "totalUsers", userCount,
                "activeUsers", activeUserCount,
                "goodsCount", goodsCount,
                "postCount", postCount
        ));
        return Result.succ(result);
    }

    /**
     * 搜索个人数据（引导到对应页面）
     * 先分析用户查询属于哪个板块，再引导到对应页面查看
     */
    private Result searchPersonal(String message, Map<String, Object> result) {
        StringBuilder text = new StringBuilder();
        String navigate = null;
        String navigateName = null;

        if (message.contains("赞") || message.contains("点赞") || message.contains("多少个赞") || message.contains("多少赞")) {
            text.append("👥 个人获赞数据\n\n");
            text.append("由于隐私保护，AI无法直接查看你的获赞数据。\n");
            text.append("请前往「个人中心」查看你的获赞数和互动统计。\n\n");
            text.append("💡 提示：在个人中心页面可以看到你的帖子获赞数、收藏数等详细数据。");
            navigate = "/myInfo";
            navigateName = "个人中心";
        } else if (message.contains("帖子") || message.contains("发布") || message.contains("发了")) {
            text.append("📝 我的帖子\n\n");
            text.append("请前往「我的帖子」页面查看你发布的所有帖子。\n\n");
            text.append("💡 提示：你可以在帖子列表中查看每篇帖子的浏览量、点赞数和评论数。");
            navigate = "/myPost";
            navigateName = "我的帖子";
        } else if (message.contains("商品") || message.contains("闲置") || message.contains("卖")) {
            text.append("📦 我的商品\n\n");
            text.append("请前往「我的闲置」页面查看你发布的所有商品。\n\n");
            text.append("💡 提示：你可以在商品列表中管理商品状态、查看浏览量和收藏数。");
            navigate = "/myGoods";
            navigateName = "我的闲置";
        } else if (message.contains("订单") || message.contains("买")) {
            text.append("🛒 我的订单\n\n");
            text.append("请前往「我的订单」页面查看你的购买和销售记录。");
            navigate = "/myOrder";
            navigateName = "我的订单";
        } else if (message.contains("粉丝") || message.contains("关注")) {
            text.append("👥 粉丝/关注\n\n");
            text.append("由于隐私保护，AI无法直接查看你的粉丝数据。\n");
            text.append("请前往「个人中心」查看你的粉丝和关注列表。");
            navigate = "/myInfo";
            navigateName = "个人中心";
        } else if (message.contains("收藏")) {
            text.append("⭐ 我的收藏\n\n");
            text.append("请前往「我的收藏」页面查看你收藏的内容。");
            navigate = "/myCollect";
            navigateName = "我的收藏";
        } else if (message.contains("浏览")) {
            text.append("👁️ 浏览记录\n\n");
            text.append("请前往「个人中心」查看你的浏览数据统计。");
            navigate = "/myInfo";
            navigateName = "个人中心";
        } else {
            // 通用个人数据查询：分析可能涉及的板块并引导
            text.append("📊 我的数据\n\n");
            text.append("由于隐私保护，AI无法直接查看你的个人数据。\n");
            text.append("请根据你想查看的内容前往对应页面：\n\n");
            text.append("• 「个人中心」- 查看获赞数、粉丝数等综合数据\n");
            text.append("• 「我的帖子」- 查看发布的帖子和互动数据\n");
            text.append("• 「我的闲置」- 查看发布的商品和浏览量\n");
            text.append("• 「我的订单」- 查看购买/销售记录\n");
            text.append("• 「数据统计」- 查看销售趋势和经营数据");
            navigate = "/myInfo";
            navigateName = "个人中心";
        }

        result.put("text", text.toString());
        if (navigate != null) {
            result.put("navigate", navigate);
            result.put("navigateName", navigateName);
        }
        return Result.succ(result);
    }

    /**
     * 本地搜索表白墙（独立于帖子）
     */
    private Result searchWallLocal(String keyword, Map<String, Object> result) {
        QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
        wallWrapper.eq("audit_state", 1);
        if (!keyword.isEmpty()) {
            wallWrapper.like("wall_content", keyword);
        }
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(keyword::contains);
        if (isHotQuery) {
            wallWrapper.orderByDesc("view_num").orderByDesc("like_num");
        } else {
            wallWrapper.orderByDesc("create_time");
        }
        wallWrapper.last("LIMIT 5");
        List<Wall> wallList = wallService.list(wallWrapper);

        if (wallList.isEmpty()) {
            result.put("text", keyword.isEmpty() ?
                    "表白墙暂无内容。" : "没有找到与「" + keyword + "」相关的表白墙内容。");
            return Result.succ(result);
        }

        result.put("text", "为你找到以下表白墙内容：");
        result.put("walls", wallList.stream().map(w -> {
            Map<String, Object> item = new HashMap<>();
            item.put("wallId", w.getWallId());
            item.put("wallContent", truncate(w.getWallContent(), 80));
            item.put("nickname", Boolean.TRUE.equals(w.getIsAnonymous()) ? "匿名" : getUserNickname(w.getUserId()));
            item.put("createTime", w.getCreateTime());
            item.put("viewNum", w.getViewNum());
            item.put("likeNum", w.getLikeNum());
            return item;
        }).collect(Collectors.toList()));
        return Result.succ(result);
    }

    /**
     * 通用搜索：只搜帖子+商品（不搜表白墙，避免无关内容）
     */
    private Result searchGeneralLocal(String keyword, String originalMessage, Map<String, Object> result) {
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(originalMessage::contains);

        // 搜索帖子
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        if (!keyword.isEmpty()) {
            postWrapper.and(w -> w.like("title", keyword).or().like("detail", keyword));
        }
        if (isHotQuery) {
            postWrapper.orderByDesc("view_num").orderByDesc("like_num");
        } else {
            postWrapper.orderByDesc("create_time");
        }
        postWrapper.last("LIMIT 3");
        List<Post> postList = postService.list(postWrapper);

        // 搜索商品
        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("goods_status", true);
        if (!keyword.isEmpty()) {
            goodsWrapper.and(w -> w.like("goods_name", keyword).or().like("goods_detail", keyword));
        }
        if (isHotQuery) {
            goodsWrapper.orderByDesc("view_num").orderByDesc("create_time");
        } else {
            goodsWrapper.orderByDesc("create_time");
        }
        goodsWrapper.last("LIMIT 3");
        List<Goods> goodsList = goodsService.list(goodsWrapper);

        if (goodsList.isEmpty() && postList.isEmpty()) {
            result.put("text", keyword.isEmpty() ?
                    "请告诉我你想搜索什么内容？" : "没有找到与「" + keyword + "」相关的内容，换个关键词试试？");
            return Result.succ(result);
        }

        result.put("text", "为你搜索到以下内容：");
        if (!postList.isEmpty()) {
            result.put("posts", postList.stream().map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("postId", p.getPostId());
                item.put("postTitle", p.getTitle());
                item.put("postDetail", truncate(p.getDetail(), 50));
                item.put("createTime", p.getCreateTime());
                item.put("userId", p.getUserId());
                item.put("viewNum", p.getViewNum());
                item.put("likeNum", p.getLikeNum());
                return item;
            }).collect(Collectors.toList()));
        }
        if (!goodsList.isEmpty()) {
            result.put("goods", goodsList.stream().map(g -> {
                Map<String, Object> item = new HashMap<>();
                item.put("goodsId", g.getGoodsId());
                item.put("goodsName", g.getGoodsName());
                item.put("goodsPrice", g.getGoodsPrice());
                item.put("goodsImages", g.getGoodsImages());
                return item;
            }).collect(Collectors.toList()));
        }
        return Result.succ(result);
    }

    /**
     * 本地搜索商品
     * keyword为空时不做关键词过滤，返回最新数据
     */
    private Result searchGoodsLocal(String keyword, String originalMessage, Map<String, Object> result) {
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(originalMessage::contains);

        QueryWrapper<Goods> wrapper = new QueryWrapper<>();
        wrapper.eq("goods_status", true);
        if (!keyword.isEmpty()) {
            wrapper.and(w -> w.like("goods_name", keyword).or().like("goods_detail", keyword));
        }
        if (isHotQuery) {
            wrapper.orderByDesc("view_num").orderByDesc("create_time");
        } else {
            wrapper.orderByDesc("create_time");
        }
        wrapper.last("LIMIT 5");
        List<Goods> goodsList = goodsService.list(wrapper);

        result.put("text", goodsList.isEmpty() ?
                "没有找到与「" + keyword + "」相关的商品，换个关键词试试？" :
                "为你找到以下商品：");
        result.put("goods", goodsList.stream().map(g -> {
            Map<String, Object> item = new HashMap<>();
            item.put("goodsId", g.getGoodsId());
            item.put("goodsName", g.getGoodsName());
            item.put("goodsPrice", g.getGoodsPrice());
            item.put("goodsImages", g.getGoodsImages());
            item.put("goodsDetail", g.getGoodsDetail() != null && g.getGoodsDetail().length() > 50 ?
                    g.getGoodsDetail().substring(0, 50) + "..." : g.getGoodsDetail());
            return item;
        }).collect(Collectors.toList()));

        return Result.succ(result);
    }

    /**
     * 本地搜索帖子
     * keyword为空时不做关键词过滤，返回最新/热门数据
     */
    private Result searchPostsLocal(String keyword, String originalMessage, Map<String, Object> result) {
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(originalMessage::contains);

        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if (!keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("detail", keyword));
        }
        if (isHotQuery) {
            wrapper.orderByDesc("view_num").orderByDesc("like_num");
        } else {
            wrapper.orderByDesc("create_time");
        }
        wrapper.last("LIMIT 5");
        List<Post> postList = postService.list(wrapper);

        result.put("text", postList.isEmpty() ?
                "没有找到与「" + keyword + "」相关的帖子，换个关键词试试？" :
                "为你找到以下帖子：");
        result.put("posts", postList.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("postId", p.getPostId());
            item.put("postTitle", p.getTitle());
            item.put("postDetail", p.getDetail() != null && p.getDetail().length() > 80 ?
                    p.getDetail().substring(0, 80) + "..." : p.getDetail());
            item.put("createTime", p.getCreateTime());
            item.put("userId", p.getUserId());
            item.put("viewNum", p.getViewNum());
            item.put("likeNum", p.getLikeNum());
            return item;
        }).collect(Collectors.toList()));

        // 帖子意图不再自动搜索表白墙（表白墙有独立的wall意图）
        return Result.succ(result);
    }

    /**
     * 本地搜索表白墙
     */
    private void searchWallsLocal(String keyword, Map<String, Object> result) {
        QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
        wallWrapper.eq("audit_state", 1);
        if (!keyword.isEmpty()) {
            wallWrapper.like("wall_content", keyword);
        }
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(keyword::contains);
        if (isHotQuery) {
            wallWrapper.orderByDesc("view_num").orderByDesc("like_num");
        } else {
            wallWrapper.orderByDesc("create_time");
        }
        wallWrapper.last("LIMIT 3");
        List<Wall> wallList = wallService.list(wallWrapper);
        if (!wallList.isEmpty()) {
            result.put("walls", wallList.stream().map(w -> {
                Map<String, Object> item = new HashMap<>();
                item.put("wallId", w.getWallId());
                item.put("wallContent", w.getWallContent() != null && w.getWallContent().length() > 80 ?
                        w.getWallContent().substring(0, 80) + "..." : w.getWallContent());
                item.put("nickname", Boolean.TRUE.equals(w.getIsAnonymous()) ? "匿名" : getUserNickname(w.getUserId()));
                item.put("createTime", w.getCreateTime());
                item.put("viewNum", w.getViewNum());
                item.put("likeNum", w.getLikeNum());
                return item;
            }).collect(Collectors.toList()));
        }
    }

    /**
     * 本地通用搜索：同时搜商品、帖子和表白墙
     * keyword为空时不做关键词过滤，返回最新数据
     */
    private Result searchAllLocal(String keyword, Map<String, Object> result) {
        boolean isHotQuery = HOT_KEYWORDS.stream().anyMatch(keyword::contains);

        QueryWrapper<Goods> goodsWrapper = new QueryWrapper<>();
        goodsWrapper.eq("goods_status", true);
        if (!keyword.isEmpty()) {
            goodsWrapper.and(w -> w.like("goods_name", keyword).or().like("goods_detail", keyword));
        }
        if (isHotQuery) {
            goodsWrapper.orderByDesc("view_num").orderByDesc("create_time");
        } else {
            goodsWrapper.orderByDesc("create_time");
        }
        goodsWrapper.last("LIMIT 3");
        List<Goods> goodsList = goodsService.list(goodsWrapper);

        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        if (!keyword.isEmpty()) {
            postWrapper.and(w -> w.like("title", keyword).or().like("detail", keyword));
        }
        if (isHotQuery) {
            postWrapper.orderByDesc("view_num").orderByDesc("like_num");
        } else {
            postWrapper.orderByDesc("create_time");
        }
        postWrapper.last("LIMIT 3");
        List<Post> postList = postService.list(postWrapper);

        // 搜索表白墙
        QueryWrapper<Wall> wallWrapper = new QueryWrapper<>();
        wallWrapper.eq("audit_state", 1);
        if (!keyword.isEmpty()) {
            wallWrapper.like("wall_content", keyword);
        }
        if (isHotQuery) {
            wallWrapper.orderByDesc("view_num").orderByDesc("like_num");
        } else {
            wallWrapper.orderByDesc("create_time");
        }
        wallWrapper.last("LIMIT 3");
        List<Wall> wallList = wallService.list(wallWrapper);

        if (goodsList.isEmpty() && postList.isEmpty() && wallList.isEmpty()) {
            result.put("text", "没有找到与「" + keyword + "」相关的内容。你可以试试更具体的关键词。");
        } else {
            result.put("text", "为你搜索到以下内容：");
        }

        result.put("goods", goodsList.stream().map(g -> {
            Map<String, Object> item = new HashMap<>();
            item.put("goodsId", g.getGoodsId());
            item.put("goodsName", g.getGoodsName());
            item.put("goodsPrice", g.getGoodsPrice());
            item.put("goodsImages", g.getGoodsImages());
            return item;
        }).collect(Collectors.toList()));

        result.put("posts", postList.stream().map(p -> {
            Map<String, Object> item = new HashMap<>();
            item.put("postId", p.getPostId());
            item.put("postTitle", p.getTitle());
            item.put("postDetail", p.getDetail() != null && p.getDetail().length() > 80 ?
                    p.getDetail().substring(0, 80) + "..." : p.getDetail());
            item.put("createTime", p.getCreateTime());
            item.put("userId", p.getUserId());
            item.put("viewNum", p.getViewNum());
            item.put("likeNum", p.getLikeNum());
            return item;
        }).collect(Collectors.toList()));

        if (!wallList.isEmpty()) {
            result.put("walls", wallList.stream().map(w -> {
                Map<String, Object> item = new HashMap<>();
                item.put("wallId", w.getWallId());
                item.put("wallContent", w.getWallContent() != null && w.getWallContent().length() > 80 ?
                        w.getWallContent().substring(0, 80) + "..." : w.getWallContent());
                item.put("nickname", Boolean.TRUE.equals(w.getIsAnonymous()) ? "匿名" : getUserNickname(w.getUserId()));
                item.put("createTime", w.getCreateTime());
                item.put("viewNum", w.getViewNum());
                item.put("likeNum", w.getLikeNum());
                return item;
            }).collect(Collectors.toList()));
        }

        return Result.succ(result);
    }
}
