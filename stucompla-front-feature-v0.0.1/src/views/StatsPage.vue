<!--
  组件名：StatsPage
  功能描述：数据统计页
  主要职责：
    1. 个人数据概览
    2. 帖子/商品/表白墙统计
    3. 订单统计
    4. 图表展示
-->
<template>
  <div>
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 论坛数据 -->
      <el-tab-pane label="论坛数据" name="post">
        <!-- 帖子分类统计 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="24">
            <el-card shadow="hover">
              <div slot="header" style="font-weight:bold;">帖子分类统计</div>
              <div v-if="postStats.postCategoryStats && postStats.postCategoryStats.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else style="display:flex;gap:20px;">
                <div style="flex:1;">
                  <div v-for="(item, idx) in (postStats.postCategoryStats || []).slice(0, 10)" :key="'pc-l-'+idx" style="display:flex;align-items:center;margin-bottom:12px;">
                    <span style="width:70px;text-align:right;margin-right:10px;font-size:13px;">{{ item.name }}</span>
                    <el-progress :percentage="getPercentage(item.value, postStats.postCategoryStats)" :color="colors[idx % colors.length]" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                    <span style="width:40px;text-align:right;font-size:13px;color:#666;">{{ item.value }}</span>
                  </div>
                </div>
                <div v-if="(postStats.postCategoryStats || []).length > 10" style="width:1px;background:#e4e7ed;"></div>
                <div v-if="(postStats.postCategoryStats || []).length > 10" style="flex:1;">
                  <div v-for="(item, idx) in (postStats.postCategoryStats || []).slice(10)" :key="'pc-r-'+idx" style="display:flex;align-items:center;margin-bottom:12px;">
                    <span style="width:70px;text-align:right;margin-right:10px;font-size:13px;">{{ item.name }}</span>
                    <el-progress :percentage="getPercentage(item.value, postStats.postCategoryStats)" :color="colors[(idx + 10) % colors.length]" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                    <span style="width:40px;text-align:right;font-size:13px;color:#666;">{{ item.value }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 帖子浏览量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子浏览量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#409EFF;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>浏览量</span>
              </div>
              <div v-if="postWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postWeekTrend" :key="'pwv-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>浏览量：{{ item.viewNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.viewNum, postWeekTrend, 'viewNum') + 'px' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postWeekTrend" :key="'pwl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子浏览量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#67C23A;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>浏览量</span>
              </div>
              <div v-if="postYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postYearTrend" :key="'pyv-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>浏览量：{{ item.viewNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.viewNum, postYearTrend, 'viewNum') + 'px', background: 'linear-gradient(180deg,#67C23A,#95d475)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postYearTrend" :key="'pyl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 帖子点赞量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子点赞量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#F56C6C;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>点赞量</span>
              </div>
              <div v-if="postWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postWeekTrend" :key="'pwlk-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>点赞量：{{ item.likeNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.likeNum, postWeekTrend, 'likeNum') + 'px', background: 'linear-gradient(180deg,#F56C6C,#fab6b6)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postWeekTrend" :key="'pwlkl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子点赞量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#E6A23C;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>点赞量</span>
              </div>
              <div v-if="postYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postYearTrend" :key="'pylk-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>点赞量：{{ item.likeNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.likeNum, postYearTrend, 'likeNum') + 'px', background: 'linear-gradient(180deg,#E6A23C,#eebe77)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postYearTrend" :key="'pylkl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 帖子收藏量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子收藏量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#E6A23C;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>收藏量</span>
              </div>
              <div v-if="postWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postWeekTrend" :key="'pwc-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>收藏量：{{ item.collectNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.collectNum, postWeekTrend, 'collectNum') + 'px', background: 'linear-gradient(180deg,#E6A23C,#eebe77)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postWeekTrend" :key="'pwcl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子收藏量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#9b59b6;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>收藏量</span>
              </div>
              <div v-if="postYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postYearTrend" :key="'pyc-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>收藏量：{{ item.collectNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.collectNum, postYearTrend, 'collectNum') + 'px', background: 'linear-gradient(180deg,#9b59b6,#c39bd3)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postYearTrend" :key="'pycl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 帖子分享量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子分享量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#9b59b6;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>分享量</span>
              </div>
              <div v-if="postWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postWeekTrend" :key="'pws-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>分享量：{{ item.shareNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.shareNum, postWeekTrend, 'shareNum') + 'px', background: 'linear-gradient(180deg,#9b59b6,#c39bd3)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postWeekTrend" :key="'pwsl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">帖子分享量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#e74c3c;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>分享量</span>
              </div>
              <div v-if="postYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in postYearTrend" :key="'pys-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>分享量：{{ item.shareNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.shareNum, postYearTrend, 'shareNum') + 'px', background: 'linear-gradient(180deg,#e74c3c,#f1948a)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in postYearTrend" :key="'pysl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 交易数据 -->
      <el-tab-pane label="交易数据" name="trade">
        <!-- 订单状态分布 & 卖出的商品分类统计 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="font-weight:bold;">订单状态分布</div>
              <div v-if="stats.orderStatusStats && stats.orderStatusStats.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else>
                <div v-for="(item, idx) in (stats.orderStatusStats || [])" :key="'status-'+idx" style="display:flex;align-items:center;margin-bottom:12px;">
                  <span style="width:70px;text-align:right;margin-right:10px;font-size:13px;">{{ getOrderStatusName(item.name) }}</span>
                  <el-progress :percentage="getPercentage(item.value, stats.orderStatusStats)" :color="orderStatusColors[getOrderStatusIndex(item.name) % orderStatusColors.length]" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                  <span style="width:40px;text-align:right;font-size:13px;color:#666;">{{ item.value }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="font-weight:bold;">卖出的商品分类统计</div>
              <div v-if="stats.soldGoodsCategoryStats && stats.soldGoodsCategoryStats.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else style="display:flex;gap:20px;">
                <div style="flex:1;">
                  <div v-for="(item, idx) in (stats.soldGoodsCategoryStats || []).slice(0, 10)" :key="'sold-l-'+idx" style="display:flex;align-items:center;margin-bottom:12px;">
                    <span style="width:70px;text-align:right;margin-right:10px;font-size:13px;">{{ item.name }}</span>
                    <el-progress :percentage="getPercentage(item.value, stats.soldGoodsCategoryStats)" :color="colors[idx % colors.length]" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                    <span style="width:40px;text-align:right;font-size:13px;color:#666;">{{ item.value }}</span>
                  </div>
                </div>
                <div v-if="(stats.soldGoodsCategoryStats || []).length > 10" style="width:1px;background:#e4e7ed;"></div>
                <div v-if="(stats.soldGoodsCategoryStats || []).length > 10" style="flex:1;">
                  <div v-for="(item, idx) in (stats.soldGoodsCategoryStats || []).slice(10)" :key="'sold-r-'+idx" style="display:flex;align-items:center;margin-bottom:12px;">
                    <span style="width:70px;text-align:right;margin-right:10px;font-size:13px;">{{ item.name }}</span>
                    <el-progress :percentage="getPercentage(item.value, stats.soldGoodsCategoryStats)" :color="colors[(idx + 10) % colors.length]" :stroke-width="16" :text-inside="true" style="flex:1;"></el-progress>
                    <span style="width:40px;text-align:right;font-size:13px;color:#666;">{{ item.value }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 近七天销售趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="24">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">近七天销售趋势</span>
                <div style="display:flex;gap:16px;font-size:12px;">
                  <span><span style="display:inline-block;width:12px;height:12px;background:#409EFF;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>订单量</span>
                  <span><span style="display:inline-block;width:12px;height:2px;background:#F56C6C;vertical-align:middle;margin-right:4px;"></span>销售额</span>
                </div>
              </div>
              <div v-if="weekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in weekTrend" :key="idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>订单量：{{ item.orderCount }}</div>
                          <div>收入：￥{{ item.revenue }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.orderCount, weekTrend, 'orderCount') + 'px' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                  <svg class="chart-line" viewBox="0 0 100 100" preserveAspectRatio="none">
                    <polyline :points="calcPctLine(weekTrend, 'revenue')" fill="none" stroke="#F56C6C" stroke-width="1" stroke-linejoin="round" stroke-linecap="round" vector-effect="non-scaling-stroke"/>
                  </svg>
                  <div v-for="(item, idx) in weekTrend" :key="'d'+idx" class="chart-dot" :style="dotStyle(idx, weekTrend.length, item.revenue, weekTrend, 'revenue')">
                    <el-tooltip placement="top" effect="dark">
                      <div slot="content">
                        <div>日期：{{ item.date }}</div>
                        <div>订单量：{{ item.orderCount }}</div>
                        <div>收入：￥{{ item.revenue }}</div>
                      </div>
                      <div class="dot-inner" style="background:#F56C6C;"></div>
                    </el-tooltip>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in weekTrend" :key="'l'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 近一年销售趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="24">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">近一年销售趋势</span>
                <div style="display:flex;gap:16px;font-size:12px;">
                  <span><span style="display:inline-block;width:12px;height:12px;background:#67C23A;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>订单量</span>
                  <span><span style="display:inline-block;width:12px;height:2px;background:#E6A23C;vertical-align:middle;margin-right:4px;"></span>销售额</span>
                </div>
              </div>
              <div v-if="yearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in yearTrend" :key="idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>订单量：{{ item.orderCount }}</div>
                          <div>销售额：￥{{ item.revenue }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.orderCount, yearTrend, 'orderCount') + 'px', background: 'linear-gradient(180deg,#67C23A,#95d475)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                  <svg class="chart-line" viewBox="0 0 100 100" preserveAspectRatio="none">
                    <polyline :points="calcPctLine(yearTrend, 'revenue')" fill="none" stroke="#E6A23C" stroke-width="1" stroke-linejoin="round" stroke-linecap="round" vector-effect="non-scaling-stroke"/>
                  </svg>
                  <div v-for="(item, idx) in yearTrend" :key="'d'+idx" class="chart-dot" :style="dotStyle(idx, yearTrend.length, item.revenue, yearTrend, 'revenue')">
                    <el-tooltip placement="top" effect="dark">
                      <div slot="content">
                        <div>月份：{{ item.date }}</div>
                        <div>订单量：{{ item.orderCount }}</div>
                        <div>销售额：￥{{ item.revenue }}</div>
                      </div>
                      <div class="dot-inner" style="background:#E6A23C;"></div>
                    </el-tooltip>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in yearTrend" :key="'l'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 表白墙数据 -->
      <el-tab-pane label="表白墙数据" name="wall">
        <!-- 表白墙浏览量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙浏览量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#409EFF;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>浏览量</span>
              </div>
              <div v-if="wallWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallWeekTrend" :key="'wwv-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>浏览量：{{ item.viewNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.viewNum, wallWeekTrend, 'viewNum') + 'px' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallWeekTrend" :key="'wwl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙浏览量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#67C23A;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>浏览量</span>
              </div>
              <div v-if="wallYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallYearTrend" :key="'wyv-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>浏览量：{{ item.viewNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.viewNum, wallYearTrend, 'viewNum') + 'px', background: 'linear-gradient(180deg,#67C23A,#95d475)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallYearTrend" :key="'wyl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 表白墙点赞量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙点赞量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#F56C6C;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>点赞量</span>
              </div>
              <div v-if="wallWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallWeekTrend" :key="'wwlk-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>点赞量：{{ item.likeNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.likeNum, wallWeekTrend, 'likeNum') + 'px', background: 'linear-gradient(180deg,#F56C6C,#fab6b6)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallWeekTrend" :key="'wwlkl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙点赞量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#E6A23C;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>点赞量</span>
              </div>
              <div v-if="wallYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallYearTrend" :key="'wylk-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>点赞量：{{ item.likeNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.likeNum, wallYearTrend, 'likeNum') + 'px', background: 'linear-gradient(180deg,#E6A23C,#eebe77)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallYearTrend" :key="'wylkl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 表白墙收藏量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙收藏量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#E6A23C;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>收藏量</span>
              </div>
              <div v-if="wallWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallWeekTrend" :key="'wwc-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>收藏量：{{ item.collectNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.collectNum, wallWeekTrend, 'collectNum') + 'px', background: 'linear-gradient(180deg,#E6A23C,#eebe77)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallWeekTrend" :key="'wwcl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙收藏量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#9b59b6;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>收藏量</span>
              </div>
              <div v-if="wallYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallYearTrend" :key="'wyc-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>收藏量：{{ item.collectNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.collectNum, wallYearTrend, 'collectNum') + 'px', background: 'linear-gradient(180deg,#9b59b6,#c39bd3)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallYearTrend" :key="'wycl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 表白墙分享量趋势 -->
        <el-row :gutter="20" style="margin-bottom:20px;">
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙分享量趋势（近七天）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#9b59b6;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>分享量</span>
              </div>
              <div v-if="wallWeekTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallWeekTrend" :key="'wws-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>日期：{{ item.date }}</div>
                          <div>分享量：{{ item.shareNum }}</div>
                        </div>
                        <div class="chart-bar" :style="{ height: getBarH(item.shareNum, wallWeekTrend, 'shareNum') + 'px', background: 'linear-gradient(180deg,#9b59b6,#c39bd3)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallWeekTrend" :key="'wwsl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div slot="header" style="display:flex;justify-content:space-between;align-items:center;">
                <span style="font-weight:bold;">表白墙分享量趋势（近一年）</span>
                <span style="font-size:12px;"><span style="display:inline-block;width:12px;height:12px;background:#e74c3c;border-radius:2px;vertical-align:middle;margin-right:4px;"></span>分享量</span>
              </div>
              <div v-if="wallYearTrend.length === 0" style="text-align:center;color:#999;padding:40px 0;">暂无数据</div>
              <div v-else class="chart-container">
                <div class="chart-area">
                  <div class="chart-bars">
                    <div v-for="(item, idx) in wallYearTrend" :key="'wys-'+idx" class="chart-bar-item">
                      <el-tooltip placement="top" effect="dark">
                        <div slot="content">
                          <div>月份：{{ item.date }}</div>
                          <div>分享量：{{ item.shareNum }}</div>
                        </div>
                        <div class="chart-bar year-bar" :style="{ height: getBarH(item.shareNum, wallYearTrend, 'shareNum') + 'px', background: 'linear-gradient(180deg,#e74c3c,#f1948a)' }"></div>
                      </el-tooltip>
                    </div>
                  </div>
                </div>
                <div class="chart-labels">
                  <span v-for="(item, idx) in wallYearTrend" :key="'wysl-'+idx" style="flex:1;text-align:center;font-size:11px;color:#999;">{{ item.date.substring(5) }}月</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
const COLORS = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#f47983']

export default {
  name: 'StatsPage',
  data() {
    return {
      activeTab: 'post',
      stats: {},
      weekTrend: [],
      yearTrend: [],
      postStats: {},
      postWeekTrend: [],
      postYearTrend: [],
      wallWeekTrend: [],
      wallYearTrend: [],
      colors: COLORS,
      orderStatusColors: ['#909399', '#409EFF', '#E6A23C', '#67C23A', '#F56C6C', '#9b59b6', '#e74c3c', '#8e44ad', '#2ecc71', '#f39c12']
    }
  },
  created() { this.loadData() },
  watch: {
    activeTab(val) {
      if (val === 'post') this.loadPostStats()
      else if (val === 'trade') this.loadTradeStats()
      else if (val === 'wall') this.loadWallStats()
    }
  },
  methods: {
    getOrderStatusName(name) {
      var statusMap = { '0': '未支付', '1': '已支付', '2': '已发货', '3': '已完成', '4': '退款中', '5': '退货中', '6': '已退款', '7': '已退货', '8': '已完成', '9': '已评价' }
      return statusMap[String(name)] || name
    },
    getOrderStatusIndex(name) {
      var statusOrder = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9']
      var idx = statusOrder.indexOf(String(name))
      return idx >= 0 ? idx : 0
    },
    async loadData() {
      this.loadTradeStats()
      this.loadPostStats()
      this.loadWallStats()
    },
    async loadTradeStats() {
      try {
        const r = await this.$axios.get('/stats/myStats')
        if (r.code === 200) {
          const d = r.data || {}
          this.stats = d
          this.weekTrend = d.myWeekTrend || []
          this.yearTrend = d.myYearTrend || []
        }
      } catch (e) {}
    },
    async loadPostStats() {
      try {
        const r = await this.$axios.get('/stats/postStats')
        if (r.code === 200) {
          const d = r.data || {}
          this.postStats = d
          this.postWeekTrend = d.postWeekTrend || []
          this.postYearTrend = d.postYearTrend || []
        }
      } catch (e) {}
    },
    async loadWallStats() {
      try {
        const r = await this.$axios.get('/stats/wallStats')
        if (r.code === 200) {
          const d = r.data || {}
          this.wallWeekTrend = d.wallWeekTrend || []
          this.wallYearTrend = d.wallYearTrend || []
        }
      } catch (e) {}
    },
    getBarH(value, list, field) {
      const max = Math.max(...list.map(i => i[field] || 0), 1)
      return Math.max((value / max) * 180, 2)
    },
    barCenterX(idx, n) {
      return ((idx + 0.5) / n) * 100
    },
    calcPctLine(list, field) {
      if (!list.length) return ''
      const max = Math.max(...list.map(i => i[field] || 0), 1)
      const n = list.length
      return list.map((item, idx) => {
        const x = this.barCenterX(idx, n)
        const y = 100 - ((item[field] || 0) / max) * 90 - 5
        return x + ',' + y
      }).join(' ')
    },
    dotStyle(idx, total, value, list, field) {
      const max = Math.max(...list.map(i => i[field] || 0), 1)
      const n = total
      const leftPct = this.barCenterX(idx, n)
      const bottomPct = ((value || 0) / max) * 90 + 5
      return {
        left: leftPct + '%',
        bottom: bottomPct + '%'
      }
    },
    getPercentage(value, list) {
      const total = (list || []).reduce((s, i) => s + (i.value || 0), 0)
      return total === 0 ? 0 : Math.round((value / total) * 100)
    }
  }
}
</script>

<style scoped>
.chart-container { position: relative; }
.chart-area { position: relative; height: 200px; }
.chart-bars { display: flex; align-items: flex-end; height: 200px; position: relative; z-index: 1; }
.chart-bar-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
.chart-bar { width: 100%; max-width: 36px; background: linear-gradient(180deg, #409EFF, #79bbff); border-radius: 4px 4px 0 0; transition: height 0.3s ease, opacity 0.3s ease; cursor: pointer; }
.chart-bar:hover { opacity: 0.85; }
.chart-bar.year-bar { max-width: 28px; }
.chart-labels { display: flex; margin-top: 6px; }
.chart-line { position: absolute; top: 0; left: 0; width: 100%; height: 200px; z-index: 2; pointer-events: none; }
.chart-dot { position: absolute; z-index: 3; transform: translate(-50%, 50%); }
.dot-inner { width: 8px; height: 8px; border-radius: 50%; border: 2px solid #fff; cursor: pointer; box-shadow: 0 0 4px rgba(0,0,0,0.2); transition: transform 0.3s ease; }
.dot-inner:hover { transform: scale(1.3); }

/* Card beautification */
::v-deep .el-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  border: none;
}
::v-deep .el-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}
::v-deep .el-card__header {
  font-weight: 600;
  color: #303133;
}

/* Tab beautification */
::v-deep .el-tabs--border-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  border: none;
  overflow: hidden;
}
</style>
