package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.UserModel
import org.apache.ibatis.annotations.*
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

    @Delete("DELETE FROM user WHERE id == #{id} AND card_id == #{cardId}")
    fun delete(userModel: UserModel):Boolean
}