package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.UserModel
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface UserMapper {
    @Insert("INSERT INTO user(name, card_id) VALUES(#{name}, #{cardId})")
    fun insert(model:UserModel):Int

    @Select("SELECT * FROM user")
    fun selectAll():List<UserModel>

    @Select("SELECT * FROM user WHERE #{cardId}")
    fun select(cardId:Long):UserModel?
}