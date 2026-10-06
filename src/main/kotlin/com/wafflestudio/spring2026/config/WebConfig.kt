package com.wafflestudio.spring2026.config

import com.wafflestudio.spring2026.auth.AuthInterceptor
import com.wafflestudio.spring2026.auth.LoginUserArgumentResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authInterceptor: AuthInterceptor,
    private val loginUserArgumentResolver: LoginUserArgumentResolver,
    @Value("\${waggle.auth.enabled:false}") private val authEnabled: Boolean,
) : WebMvcConfigurer {
    // 2주차 테스트는 토큰 없이 요청하므로, 3주차 테스트로 바꾸기 전까지는 인터셉터를 끄고 둔다.
    // application.yaml 의 waggle.auth.enabled 를 true 로 바꾸면 켜진다.
    override fun addInterceptors(registry: InterceptorRegistry) {
        if (!authEnabled) return

        registry
            .addInterceptor(authInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/", "/index.html", "/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loginUserArgumentResolver)
    }
}
