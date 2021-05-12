package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.UserModel
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Component

@Mapper
@Component
interface UserMapper {
    @Insert("INSERT INTO user(name, card_id, enabled) VALUES(#{name}, #{cardId}, #{enabled})")
    fun insert(model:UserModel):Int

    @Select("SELECT * FROM user")
    fun selectAll():List<UserModel>

    @Select("SELECT * FROM user WHERE card_id LIKE #{cardId}")
    fun select(cardId:String):UserModel?

    @Update("UPDATE user SET name = #{name}, enabled = #{enabled} WHERE id == #{id} AND card_id == #{cardId}")
    fun update(userModel: UserModel):Boolean
}