package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.CardTouchLogModel
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface CardTouchLogMapper {
    @Insert("INSERT INTO touchLog(card_id, time) VALUES(#{cardId}, #{time})")
    fun insert(model: CardTouchLogModel): Int

    @Select("SELECT * FROM touchLog")
    fun selectAll(): List<CardTouchLogModel>
}