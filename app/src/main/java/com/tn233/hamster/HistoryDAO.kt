package com.tn233.hamster

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface HistoryDAO {

    @Query("SELECT * FROM history")
    fun getAll(): List<History>

    @Query("SELECT * FROM history WHERE news_id IN (:newsIds)")
    fun loadAllByIds(newsIds: Array<String>): List<History>

    // sqlite中，random()返回一个有符号的 64 位整数，他的取值范围为[-(2^63), (2^63)]
    // 将取值范围看成一个整体，大于60%的数被删除 = 删除整体的40% = 删除正整数的20%
    // (2^63) * 0.6 = 5534023222112865485
    @Query("DELETE FROM history WHERE random() > 5534023222112865485")
    fun deleteRandomly()

    @Update
    fun updateHistory(vararg history: History)

//    @Query("SELECT * FROM history WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): History

    // 写个冲突策略，如果遇到primary key相同直接覆盖，这样可以当update用
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg history: History)

    @Delete
    fun delete(history: History)
}