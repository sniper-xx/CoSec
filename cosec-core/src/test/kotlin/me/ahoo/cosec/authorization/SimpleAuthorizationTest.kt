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

package me.ahoo.cosec.authorization

import io.mockk.every
import io.mockk.mockk
import me.ahoo.cosec.api.authorization.AuthorizeResult
import me.ahoo.cosec.api.context.SecurityContext
import me.ahoo.cosec.api.context.request.Request
import me.ahoo.cosec.api.policy.Effect
import me.ahoo.cosec.api.policy.Policy
import me.ahoo.cosec.api.principal.CoSecPrincipal
import me.ahoo.cosec.configuration.JsonConfiguration
import me.ahoo.cosec.context.SimpleSecurityContext
import me.ahoo.cosec.policy.StatementData
import me.ahoo.cosec.policy.action.AllActionMatcher
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

internal class SimpleAuthorizationTest {
    @Test
    fun authorizeWhenPrincipalIsRoot() {
        val policyRepository = mockk<PolicyRepository>()
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request>()
        val securityContext = mockk<SecurityContext> {
            every { principal.id } returns CoSecPrincipal.ROOT_ID
        }
        authorization.authorize(request, securityContext)
            .test()
            .expectNext(AuthorizeResult.ALLOW)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenPolicyIsEmpty() {
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.empty()
            every { getRolePolicy(any()) } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.empty()
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request> {
        }

        authorization.authorize(request, SimpleSecurityContext.ANONYMOUS)
            .test()
            .expectNext(AuthorizeResult.IMPLICIT_DENY)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenGlobalPolicyIsAllowAll() {
        val globalPolicy = mockk<Policy>() {
            every { id } returns "globalPolicy"
            every { statements } returns listOf(
                StatementData(
                    effect = Effect.ALLOW,
                    actions = listOf(AllActionMatcher(JsonConfiguration.EMPTY))
                )
            )
        }
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.just(setOf(globalPolicy))
            every { getRolePolicy(any()) } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.empty()
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request> {
        }

        authorization.authorize(request, SimpleSecurityContext.ANONYMOUS)
            .test()
            .expectNext(AuthorizeResult.ALLOW)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenGlobalPolicyIsDenyAll() {
        val globalPolicy = mockk<Policy>() {
            every { id } returns "globalPolicy"
            every { statements } returns listOf(
                StatementData(
                    effect = Effect.DENY,
                    actions = listOf(AllActionMatcher(JsonConfiguration.EMPTY))
                )
            )
        }
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.just(setOf(globalPolicy))
            every { getRolePolicy(any()) } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.empty()
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request>()

        authorization.authorize(request, SimpleSecurityContext.ANONYMOUS)
            .test()
            .expectNext(AuthorizeResult.EXPLICIT_DENY)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenGlobalPolicyIsEmptyAndPrincipalIsAllowAll() {
        val principalPolicy = mockk<Policy>() {
            every { id } returns "policyId"
            every { statements } returns listOf(
                StatementData(
                    effect = Effect.ALLOW,
                    actions = listOf(AllActionMatcher(JsonConfiguration.EMPTY))
                )
            )
        }
        val securityContext = mockk<SecurityContext>() {
            every { principal.authenticated() } returns false
            every { principal.id } returns ""
            every { principal.policies } returns setOf("principalPolicy")
        }
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.just(setOf(principalPolicy))
            every { getRolePolicy(any()) } returns Mono.empty()
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request> {
        }

        authorization.authorize(request, securityContext)
            .test()
            .expectNext(AuthorizeResult.ALLOW)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenGlobalPolicyIsEmptyAndPrincipalIsDenyAll() {
        val principalPolicy = mockk<Policy>() {
            every { id } returns "policyId"
            every { statements } returns listOf(
                StatementData(
                    effect = Effect.DENY,
                    actions = listOf(AllActionMatcher(JsonConfiguration.EMPTY))
                )
            )
        }
        val securityContext = mockk<SecurityContext>() {
            every { principal.authenticated() } returns false
            every { principal.id } returns ""
            every { principal.policies } returns setOf("principalPolicy")
        }
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.just(setOf(principalPolicy))
            every { getRolePolicy(any()) } returns Mono.empty()
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request> {
        }

        authorization.authorize(request, securityContext)
            .test()
            .expectNext(AuthorizeResult.EXPLICIT_DENY)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenGlobalAndPrincipalPolicyIsEmptyAndRoleIsAllowAll() {
        val rolePolicy = mockk<Policy>() {
            every { id } returns "policyId"
            every { statements } returns listOf(
                StatementData(
                    effect = Effect.ALLOW,
                    actions = listOf(AllActionMatcher(JsonConfiguration.EMPTY))
                )
            )
        }
        val securityContext = mockk<SecurityContext>() {
            every { principal.authenticated() } returns false
            every { principal.id } returns ""
            every { principal.policies } returns emptySet()
            every { principal.roles } returns setOf("rolePolicy")
        }
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.empty()
            every { getRolePolicy(any()) } returns Mono.just(setOf(rolePolicy))
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request> {
        }

        authorization.authorize(request, securityContext)
            .test()
            .expectNext(AuthorizeResult.ALLOW)
            .verifyComplete()
    }

    @Test
    fun authorizeWhenGlobalAndPrincipalPolicyIsEmptyAndRoleIsDenyAll() {
        val rolePolicy = mockk<Policy>() {
            every { id } returns "policyId"
            every { statements } returns listOf(
                StatementData(
                    effect = Effect.DENY,
                    actions = listOf(AllActionMatcher(JsonConfiguration.EMPTY))
                )
            )
        }
        val securityContext = mockk<SecurityContext>() {
            every { principal.authenticated() } returns false
            every { principal.id } returns ""
            every { principal.policies } returns emptySet()
            every { principal.roles } returns setOf("rolePolicy")
        }
        val policyRepository = mockk<PolicyRepository>() {
            every { getGlobalPolicy() } returns Mono.empty()
            every { getPolicies(any()) } returns Mono.empty()
            every { getRolePolicy(any()) } returns Mono.just(setOf(rolePolicy))
        }
        val authorization = SimpleAuthorization(policyRepository)
        val request = mockk<Request> {
        }

        authorization.authorize(request, securityContext)
            .test()
            .expectNext(AuthorizeResult.EXPLICIT_DENY)
            .verifyComplete()
    }
}
