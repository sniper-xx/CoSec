/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.cosec.servlet

import io.mockk.every
import io.mockk.mockk
import me.ahoo.cosec.api.authorization.Authorization
import me.ahoo.cosec.api.authorization.AuthorizeResult
import me.ahoo.cosec.context.SecurityContextHolder
import me.ahoo.cosec.context.SimpleSecurityContext
import me.ahoo.cosec.jwt.Jwts
import me.ahoo.cosec.servlet.ServletRequests.setSecurityContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import reactor.kotlin.core.publisher.toMono
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class AuthorizationFilterTest {

    @Test
    fun doFilter() {
        val authorization = mockk<Authorization> {
            every { authorize(any(), any()) } returns AuthorizeResult.ALLOW.toMono()
        }
        val filter = AuthorizationFilter(
            InjectSecurityContextParser,
            authorization,
            ServletRequestParser(ServletRemoteIpResolver)
        )
        val servletRequest = mockk<HttpServletRequest> {
            every { servletPath } returns "/path"
            every { method } returns "GET"
            every { remoteHost } returns "remoteHost"
            every { getHeader(Jwts.AUTHORIZATION_KEY) } returns null
            every { getHeader(HttpHeaders.ORIGIN) } returns null
            every { getHeader(HttpHeaders.REFERER) } returns null
            every { setSecurityContext(any()) } returns Unit
        }
        val filterChain = mockk<FilterChain> {
            every { doFilter(servletRequest, any()) } returns Unit
        }
        filter.doFilter(servletRequest, mockk<HttpServletResponse>(), filterChain)
        assertThat(SecurityContextHolder.requiredContext, equalTo(SimpleSecurityContext.ANONYMOUS))
    }

    @Test
    fun doFilterDeny() {
        val authorization = mockk<Authorization> {
            every { authorize(any(), any()) } returns AuthorizeResult.EXPLICIT_DENY.toMono()
        }
        val filter = AuthorizationFilter(
            InjectSecurityContextParser,
            authorization,
            ServletRequestParser(ServletRemoteIpResolver)
        )
        val servletRequest = mockk<HttpServletRequest> {
            every { servletPath } returns "/path"
            every { method } returns "GET"
            every { remoteHost } returns "remoteHost"
            every { getHeader(Jwts.AUTHORIZATION_KEY) } returns null
            every { getHeader(HttpHeaders.ORIGIN) } returns "ORIGIN"
            every { getHeader(HttpHeaders.REFERER) } returns "REFERER"
            every { setSecurityContext(any()) } returns Unit
        }
        val servletResponse = mockk<HttpServletResponse> {
            every { status = HttpStatus.UNAUTHORIZED.value() } returns Unit
            every { outputStream.write(any() as ByteArray) } returns Unit
            every { outputStream.flush() } returns Unit
        }
        val filterChain = mockk<FilterChain> {
            every { doFilter(servletRequest, any()) } returns Unit
        }
        filter.doFilter(servletRequest, servletResponse, filterChain)
        assertThat(SecurityContextHolder.requiredContext, equalTo(SimpleSecurityContext.ANONYMOUS))
    }
}
