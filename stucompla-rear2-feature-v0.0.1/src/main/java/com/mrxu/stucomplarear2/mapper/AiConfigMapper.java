package com.mrxu.stucomplarear2.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mrxu.stucomplarear2.entity.AiConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AiConfigMapper extends BaseMapper<AiConfig> {

    /**
     * 重置自增ID起始值
     */
    @Update("ALTER TABLE ai_config AUTO_INCREMENT = 1")
    void resetAutoIncrement();

    /**
     * 将指定ID之后的所有记录ID前移一位（用于删除后填补空缺）
     */
    @Update("UPDATE ai_config SET id = id - 1 WHERE id > #{deletedId}")
    void shiftIdsAfterDelete(@Param("deletedId") Integer deletedId);
}
