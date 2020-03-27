package team.undefined.quiz
/*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseCookie
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.csrf.CsrfToken
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository
import org.springframework.security.web.server.csrf.WebSessionServerCsrfTokenRepository
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.URI

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(private val authenticationManager: ReactiveAuthenticationManager) {

    private val CSRF_COOKIE_NAME = "XSRF-TOKEN"

    @Bean
    fun securitygWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http
                .authenticationManager(authenticationManager)
                    .cors()
                .and()
                    .csrf()
                    .csrfTokenRepository(csrfTokenRepository())
                .and()
                    .addFilterAfter({ exchange: ServerWebExchange?, filterChain: WebFilterChain? -> csrfFilter(exchange!!, filterChain!!)!! }, SecurityWebFiltersOrder.CSRF)
                    .authorizeExchange()
                    .pathMatchers("/", "/index.html", "/registration.html", "/login.html").permitAll()
                    .pathMatchers(HttpMethod.OPTIONS).permitAll()
                    .pathMatchers(HttpMethod.POST, "/registrations").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/status").permitAll()
                    .anyExchange().authenticated()
                .and()
                    .formLogin()
                    .loginPage("/login.html")
                    .authenticationSuccessHandler(successHandler())
                .and()
                    .logout()
                .and()
                    .build()
        // TODO: .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
    }

    private fun csrfTokenRepository(): ServerCsrfTokenRepository? {
        val repository = WebSessionServerCsrfTokenRepository()
        repository.setHeaderName(CSRF_COOKIE_NAME)
        return repository
    }

    private fun csrfFilter(exchange: ServerWebExchange, filterChain: WebFilterChain): Mono<Void>? {
        val csrfToken = exchange.getAttribute<Mono<CsrfToken>>(CsrfToken::class.java.name)
        return csrfToken?.map { csrf: CsrfToken? ->
            if (csrf != null) {
                val cookie = exchange.request.cookies.getFirst(CSRF_COOKIE_NAME)
                val token = csrf.token
                if (cookie == null || token != null && token != cookie.value) {
                    exchange.response.addCookie(ResponseCookie.from(CSRF_COOKIE_NAME, token!!)
                            .path("/")
                            .httpOnly(false)
                            .build())
                }
            }
            csrf
        }?.flatMap { csrf: CsrfToken? -> filterChain.filter(exchange) }
                ?: filterChain.filter(exchange)
    }

    private fun successHandler(): ServerAuthenticationSuccessHandler? {
        val successHandler = RedirectServerAuthenticationSuccessHandler()
        successHandler.setLocation(URI.create("/quiz.html"))
        return successHandler
    }

}
*/