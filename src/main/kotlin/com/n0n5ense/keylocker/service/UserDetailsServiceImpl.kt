package com.n0n5ense.keylocker.service


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl @Autowired constructor(private val loginUserService: LoginUserService) :
    UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = loginUserService.select(username) ?: throw UsernameNotFoundException("$username not found in database")

        val grantList: MutableList<GrantedAuthority> = ArrayList()
        val authority: GrantedAuthority = SimpleGrantedAuthority(user.role)
        grantList.add(authority)
        return User(user.name, user.password, grantList)
    }
}