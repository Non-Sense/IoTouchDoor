package com.n0n5ense.keylocker.config

import com.n0n5ense.keylocker.service.UserDetailsServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


@Configuration
@EnableWebSecurity
class WebSecurityConfig @Autowired constructor(val userDetailsService: UserDetailsServiceImpl): WebSecurityConfigurerAdapter() {
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder? {
        return BCryptPasswordEncoder()
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
            "/css/**",
            "/js/**"
        )
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/api/status", "/api/start").permitAll()
            .anyRequest().authenticated()

        http.formLogin()
            .loginPage("/login") //ログインページはコントローラを経由しないのでViewNameとの紐付けが必要
            .loginProcessingUrl("/sign_in") //フォームのSubmitURL、このURLへリクエストが送られると認証処理が実行される
            .usernameParameter("username") //リクエストパラメータのname属性を明示
            .passwordParameter("password")
            .defaultSuccessUrl("/userpage")
            .failureUrl("/login?error")
            .permitAll()

        http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll()
    }

    @Autowired
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder())
    }

}