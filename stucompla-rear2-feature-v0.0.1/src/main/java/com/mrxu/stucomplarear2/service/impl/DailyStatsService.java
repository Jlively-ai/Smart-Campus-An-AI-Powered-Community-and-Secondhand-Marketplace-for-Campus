package com.mrxu.stucomplarear2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mrxu.stucomplarear2.entity.DailyStats;
import com.mrxu.stucomplarear2.mapper.DailyStatsMapper;
import com.mrxu.stucomplarear2.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class DailyStatsService {
    @Autowired
    private DailyStatsMapper dailyStatsMapper;

    public void incrementStat(String itemType, String itemId, String userId, String field) {
        Date today = getToday();
        QueryWrapper<DailyStats> wrapper = new QueryWrapper<>();
        wrapper.eq("item_type", itemType)
               .eq("item_id", itemId)
               .eq("stat_date", today);
        DailyStats stats = dailyStatsMapper.selectOne(wrapper);

        if (stats == null) {
            stats = new DailyStats();
            stats.setId(IdGenerator.generateId(IdGenerator.DAILY_STATS));
            stats.setItemType(itemType);
            stats.setItemId(itemId);
            stats.setUserId(userId);
            stats.setViewNum(0);
            stats.setLikeNum(0);
            stats.setCollectNum(0);
            stats.setShareNum(0);
            stats.setStatDate(today);
            stats.setCreateTime(new Date());
            stats.setUpdateTime(new Date());
            setField(stats, field, 1);
            dailyStatsMapper.insert(stats);
        } else {
            setField(stats, field, getField(stats, field) + 1);
            stats.setUpdateTime(new Date());
            dailyStatsMapper.updateById(stats);
        }
    }

    private Date getToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void setField(DailyStats stats, String field, int value) {
        switch (field) {
            case "viewNum": stats.setViewNum(value); break;
            case "likeNum": stats.setLikeNum(value); break;
            case "collectNum": stats.setCollectNum(value); break;
            case "shareNum": stats.setShareNum(value); break;
        }
    }

    private int getField(DailyStats stats, String field) {
        switch (field) {
            case "viewNum": return stats.getViewNum() != null ? stats.getViewNum() : 0;
            case "likeNum": return stats.getLikeNum() != null ? stats.getLikeNum() : 0;
            case "collectNum": return stats.getCollectNum() != null ? stats.getCollectNum() : 0;
            case "shareNum": return stats.getShareNum() != null ? stats.getShareNum() : 0;
            default: return 0;
        }
    }
}
