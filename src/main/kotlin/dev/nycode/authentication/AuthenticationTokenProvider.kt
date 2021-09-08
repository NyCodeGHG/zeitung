package dev.nycode.authentication

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationFailureReason
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher

@Singleton
class AuthenticationTokenProvider(
    private val authService: AuthService
) : AuthenticationProvider {
    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>
    ): Publisher<AuthenticationResponse> {
        return flow<AuthenticationResponse> {
            val username = authenticationRequest.identity as? String
            val password = authenticationRequest.secret as? String

            if (username == null || password == null) {
                throw AuthenticationResponse.exception()
            }

            val user = authService.findUser(username, password) ?: throw AuthenticationResponse.exception()
            emit(AuthenticationResponse.success(user.username))
        }.asPublisher()
    }
}
