package com.n0n5ense.keylocker.service

import com.n0n5ense.keylocker.mapper.LoginUserMapper
import com.n0n5ense.keylocker.model.LoginUserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LoginUserService @Autowired constructor(private val dao: LoginUserMapper) {

    fun insert(user: LoginUserModel):Boolean{
        return dao.insert(user) > 0
    }

    fun selectAll():List<LoginUserModel>{
        return dao.selectAll()
    }

    fun select(name:String): LoginUserModel?{
        return dao.select(name)
    }
}