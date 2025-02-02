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
package me.ahoo.cosec.context

import me.ahoo.cosec.api.context.SecurityContext
import me.ahoo.cosec.api.principal.CoSecPrincipal
import me.ahoo.cosec.api.tenant.Tenant
import me.ahoo.cosec.api.tenant.TenantCapable
import me.ahoo.cosec.principal.SimpleTenantPrincipal
import me.ahoo.cosec.tenant.SimpleTenant
import javax.annotation.concurrent.ThreadSafe

/**
 * Security Context.
 *
 * @author ahoo wang
 */
@ThreadSafe
class SimpleSecurityContext(
    override val principal: CoSecPrincipal,
    override val tenant: Tenant = principal.tenant,
    override val attributes: Map<String, Any> = emptyMap()
) : SecurityContext {
    companion object {
        val ANONYMOUS: SecurityContext = SimpleSecurityContext(SimpleTenantPrincipal.ANONYMOUS)
    }

    override fun withAttributes(attributes: Map<String, Any>): SecurityContext =
        SimpleSecurityContext(
            principal = principal,
            tenant = tenant,
            attributes = attributes
        )

    override fun toString(): String {
        return "SimpleSecurityContext(principal.id=${principal.id}, tenantId=${tenant.tenantId})"
    }
}

val CoSecPrincipal.tenant: Tenant
    get() {
        return if (this is TenantCapable) {
            this.tenant
        } else {
            SimpleTenant.DEFAULT
        }
    }
