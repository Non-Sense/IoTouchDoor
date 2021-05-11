package com.n0n5ense.keylocker

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/")
class AppController {
    @RequestMapping("/userpage")
    fun userpage(model: Model){
        val auth = SecurityContextHolder.getContext().authentication
        val name = auth.name
        model.addAttribute("userName",name)
        return
    }
}