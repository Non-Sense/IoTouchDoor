package com.n0n5ense.keylocker

import com.n0n5ense.keylocker.model.UserModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.n0n5ense.keylocker.service.UserService

@RestController
class HomeController @Autowired constructor(val userService: UserService) {
    @GetMapping("")
    fun index(): String = "Hello!"

    @RequestMapping("/new")
    fun addUser(model: Model): String{
        //val t = model.addAttribute("User", UserModel(null,"Unko","KOSJDFKLJA"))
        val t=userService.insert(UserModel(null,"Unko","KOSJDFKLJA"))
        return "success $t"
    }
}