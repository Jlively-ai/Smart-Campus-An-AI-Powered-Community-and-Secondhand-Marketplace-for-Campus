package com.mrxu.stucomplarear2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrxu.stucomplarear2.entity.Letter;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Mr.Xu
 * @since 2022-04-28
 */
@Component
public interface LetterMapper extends BaseMapper<Letter> {

    @Select("SELECT message_type, COUNT(*) as cnt FROM letter WHERE receiver_id = #{userId} AND letter_status = 0 AND NOT (message_type = 'letter' AND sender_id = '0') AND session_id NOT LIKE 'post_notify_%' AND session_id NOT LIKE 'goods_notify_%' AND session_id NOT LIKE 'wall_notify_%' GROUP BY message_type")
    List<Map<String, Object>> countUnreadByType(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM letter WHERE receiver_id = #{userId} AND letter_status = 0 AND message_type = 'system' AND (session_id LIKE '%_violation_%' OR session_id LIKE '%_punishment_%' OR session_id LIKE '%_report_%')")
    long countUnreadPunishment(@Param("userId") String userId);

    @Select("SELECT " +
            "COALESCE(SUM(CASE WHEN session_id LIKE 'system_%_post_%' THEN 1 ELSE 0 END), 0) as forum, " +
            "COALESCE(SUM(CASE WHEN session_id LIKE 'system_%_order_%' OR session_id LIKE 'system_%_goods_%' THEN 1 ELSE 0 END), 0) as trade, " +
            "COALESCE(SUM(CASE WHEN session_id LIKE 'system_%_wall_%' THEN 1 ELSE 0 END), 0) as wall " +
            "FROM letter WHERE receiver_id = #{userId} AND letter_status = 0 " +
            "AND NOT (message_type = 'letter' AND sender_id = '0') " +
            "AND session_id NOT LIKE 'post_notify_%' " +
            "AND session_id NOT LIKE 'goods_notify_%' " +
            "AND session_id NOT LIKE 'wall_notify_%'")
    Map<String, Object> countUnreadBySection(@Param("userId") String userId);

}
