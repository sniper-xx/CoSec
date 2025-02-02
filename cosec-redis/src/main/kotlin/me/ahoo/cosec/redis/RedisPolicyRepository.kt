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

package me.ahoo.cosec.redis

import me.ahoo.cosec.api.policy.Policy
import me.ahoo.cosec.authorization.PolicyRepository
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class RedisPolicyRepository(
    private val globalPolicyIndexCache: GlobalPolicyIndexCache,
    private val rolePolicyCache: RolePolicyCache,
    private val policyCache: PolicyCache
) : PolicyRepository {
    override fun getGlobalPolicy(): Mono<Set<Policy>> {
        return globalPolicyIndexCache[GlobalPolicyIndexKey]
            .orEmpty()
            .let {
                getPolicies(it)
            }
    }

    override fun getRolePolicy(roleIds: Set<String>): Mono<Set<Policy>> {
        return roleIds.flatMap {
            rolePolicyCache[it].orEmpty()
        }.toSet()
            .let {
                getPolicies(it)
            }
    }

    override fun getPolicies(policyIds: Set<String>): Mono<Set<Policy>> {
        return policyIds.mapNotNull {
            policyCache[it]
        }.toSet().toMono()
    }
}
