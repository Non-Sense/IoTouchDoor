package com.n0n5ense.keylocker.mapper

import com.n0n5ense.keylocker.model.LoginUserModel
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Component

@Mapper
@Component
interface LoginUserMapper {
    @Insert("INSERT INTO login_user(name, password, role) VALUES(#{name}, #{password}, #{role})")
    fun insert(model: LoginUserModel):Int

    @Select("SELECT * FROM login_user")
    fun selectAll():List<LoginUserModel>

    @Select("SELECT * FROM login_user WHERE name = #{name}")
    fun select(name:String): LoginUserModel?
}