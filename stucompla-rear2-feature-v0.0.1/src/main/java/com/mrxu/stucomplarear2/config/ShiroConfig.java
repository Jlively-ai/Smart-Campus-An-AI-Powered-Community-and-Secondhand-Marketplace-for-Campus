package com.mrxu.stucomplarear2.config;

import com.mrxu.stucomplarear2.shiro.JWTFilter;
import com.mrxu.stucomplarear2.shiro.UserRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean("securityManager")
    public DefaultWebSecurityManager getManager(UserRealm userRealm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(userRealm);
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);
        return manager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean factory(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager);
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JWTFilter());
        factoryBean.setFilters(filterMap);
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        filterRuleMap.put("/user/login", "anon");
        filterRuleMap.put("/user/register", "anon");
        filterRuleMap.put("/user/unauthorized/**", "anon");
        filterRuleMap.put("/admin/info/login", "anon");
        filterRuleMap.put("/category/list", "anon");
        filterRuleMap.put("/goods-category/list", "anon");
        filterRuleMap.put("/post/list", "anon");
        filterRuleMap.put("/goods/getList", "anon");
        filterRuleMap.put("/wall/wallList", "anon");
        filterRuleMap.put("/comment/list/**", "anon");
        filterRuleMap.put("/goods-comment/list/**", "anon");
        filterRuleMap.put("/announcement/publicList", "anon");
        filterRuleMap.put("/logistics/getByOrderId/**", "anon");
        filterRuleMap.put("/follow/check/**", "anon");
        filterRuleMap.put("/follow/checkFollowsMe/**", "anon");
        filterRuleMap.put("/follow/followerCount/**", "anon");
        filterRuleMap.put("/follow/followingCount/**", "anon");
        filterRuleMap.put("/follow/followers/**", "anon");
        filterRuleMap.put("/follow/following/**", "anon");
        filterRuleMap.put("/stats/overview", "anon");
        filterRuleMap.put("/stats/weeklyTrend", "anon");
        filterRuleMap.put("/stats/goodsCategoryStats", "anon");
        filterRuleMap.put("/stats/likeCount", "anon");
        filterRuleMap.put("/user/publicInfo/**", "anon");
        filterRuleMap.put("/user/search", "anon");
        filterRuleMap.put("/user/batchInfo", "anon");
        filterRuleMap.put("/punishment/publicList/**", "anon");
        filterRuleMap.put("/privacy/check", "anon");
        filterRuleMap.put("/ai/chat", "anon");
        filterRuleMap.put("/ai/configs", "anon");
        filterRuleMap.put("/ai/polish", "jwt");
        filterRuleMap.put("/ai/test", "jwt");
        filterRuleMap.put("/ai-config/list", "jwt");
        filterRuleMap.put("/ai-config/active", "jwt");
        filterRuleMap.put("/ai-config/add", "jwt");
        filterRuleMap.put("/ai-config/update", "jwt");
        filterRuleMap.put("/ai-config/delete/**", "jwt");
        filterRuleMap.put("/ai-config/activate/**", "jwt");
        filterRuleMap.put("/ai-config/toggle/**", "jwt");
        filterRuleMap.put("/image/upload", "jwt");
        filterRuleMap.put("/images/**", "anon");
        filterRuleMap.put("/image/**", "anon");
        filterRuleMap.put("/cart/**", "authc");
        filterRuleMap.put("/**", "jwt");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
