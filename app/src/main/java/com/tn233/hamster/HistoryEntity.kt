package com.tn233.hamster

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    // category想试试能不能用
    @ColumnInfo(name = "news_id") val newsId: String?,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "click_count") val isClick: Boolean,
    @ColumnInfo(name = "is_share") val isShare: Boolean?,
    @ColumnInfo(name = "slip_distance") val slipDistance: Double,
    @ColumnInfo(name = "slide_count") val slideCount: Int,
    @ColumnInfo(name = "read_time") val readTime: Int,
    // news_time_diff = latest_click_timestamp - news_timestamp
    // news_time_diff越大，时间衰减越大，越不应该展示
    @ColumnInfo(name = "news_time_diff") val newsTimeDiff: Long,
    // user_profile 是用户画像的归一化，[性别,年龄,地理位置,感兴趣的分类(?)]
    @ColumnInfo(name = "user_profile") val userProfile: Double?,
    //    @ColumnInfo(name = "")
    @ColumnInfo(name = "is_like") val isLike: Boolean?
)