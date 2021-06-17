package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.ColumnLimit
import com.n0n5ense.keylocker.model.PhysicalLogModel
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface PhysicalLogMapper {
    @Insert("INSERT INTO physicalLog(action, time) VALUES(#{action}, #{time})")
    fun insert(model: PhysicalLogModel): Int

    @Select("SELECT physicalLog.id,action,time FROM physicalLog ORDER BY time DESC LIMIT #{limit} OFFSET #{offset} ")
    fun selectAll(limit:ColumnLimit): List<PhysicalLogModel>
}