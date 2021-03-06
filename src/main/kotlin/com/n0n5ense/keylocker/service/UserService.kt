package com.n0n5ense.keylocker.service

import com.n0n5ense.keylocker.mapper.UserMapper
import com.n0n5ense.keylocker.model.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService @Autowired constructor(private val dao: UserMapper) {

    fun insert(user:UserModel):Boolean{
        return dao.insert(user) > 0
    }

    fun selectAll():List<UserModel>{
        return dao.selectAll()
    }

    fun select(cardId:String):UserModel?{
        return dao.select(cardId)
    }

    fun update(user:UserModel):Boolean{
        return dao.update(user)
    }

    fun delete(user:UserModel):Boolean{
        return dao.delete(user)
    }
}