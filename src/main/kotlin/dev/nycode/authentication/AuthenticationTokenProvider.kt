package dev.nycode.authentication

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationFailureReason
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import jakarta.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.flux
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

@Singleton
class AuthenticationTokenProvider(
    private val authService: AuthService
) : AuthenticationProvider {

    override fun authenticate(
        httpRequest: HttpRequest<*>?,
        authenticationRequest: AuthenticationRequest<*, *>
    ): Publisher<AuthenticationResponse> {
        return flux {
            val username = authenticationRequest.identity as? String
            val password = authenticationRequest.secret as? String
            if (username == null || password == null) {
                throw AuthenticationResponse.exception()
            } else {
                val user = authService.findUser(username, password)
                if (user == null) {
                    throw AuthenticationResponse.exception()
                } else {
                    channel.send(AuthenticationResponse.success(user.username))
                }
            }
        }
    }
}
