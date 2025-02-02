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

package me.ahoo.cosec.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import me.ahoo.cosec.api.policy.Policy
import me.ahoo.cosec.api.policy.PolicyType
import me.ahoo.cosec.api.policy.Statement
import me.ahoo.cosec.api.tenant.Tenant.Companion.TENANT_ID_KEY
import me.ahoo.cosec.policy.PolicyData

const val POLICY_ID_KEY = "id"
const val POLICY_NAME_KEY = "name"
const val POLICY_CATEGORY_KEY = "category"
const val POLICY_DESCRIPTION_KEY = "description"
const val POLICY_TYPE_KEY = "type"
const val POLICY_STATEMENTS_KEY = "statements"

object JsonPolicySerializer : StdSerializer<Policy>(Policy::class.java) {
    override fun serialize(value: Policy, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField(POLICY_ID_KEY, value.id)
        gen.writeStringField(POLICY_NAME_KEY, value.name)
        gen.writeStringField(POLICY_CATEGORY_KEY, value.category)
        gen.writeStringField(POLICY_DESCRIPTION_KEY, value.description)
        gen.writePOJOField(POLICY_TYPE_KEY, value.type)
        gen.writeStringField(TENANT_ID_KEY, value.tenantId)
        if (value.statements.isNotEmpty()) {
            gen.writeArrayFieldStart(POLICY_STATEMENTS_KEY)
            value.statements.forEach {
                gen.writeObject(it)
            }
            gen.writeEndArray()
        }
        gen.writeEndObject()
    }
}

object JsonPolicyDeserializer : StdDeserializer<Policy>(Policy::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Policy {
        val jsonNode = p.codec.readTree<JsonNode>(p)

        val statements = jsonNode.get(POLICY_STATEMENTS_KEY)?.map {
            it.traverse(p.codec).readValueAs(Statement::class.java)
        }.orEmpty()

        return PolicyData(
            id = requireNotNull(jsonNode.get(POLICY_ID_KEY)) {
                "$POLICY_ID_KEY is required!"
            }.asText(),
            name = requireNotNull(jsonNode.get(POLICY_NAME_KEY)) {
                "$POLICY_NAME_KEY is required!"
            }.asText(),
            category = requireNotNull(jsonNode.get(POLICY_CATEGORY_KEY)) {
                "$POLICY_CATEGORY_KEY is required!"
            }.asText(),
            description = requireNotNull(jsonNode.get(POLICY_DESCRIPTION_KEY)) {
                "$POLICY_DESCRIPTION_KEY is required!"
            }.asText(),
            type = requireNotNull(jsonNode.get(POLICY_TYPE_KEY)) {
                "$POLICY_TYPE_KEY is required!"
            }.traverse(p.codec).readValueAs(PolicyType::class.java),
            tenantId = requireNotNull(jsonNode.get(TENANT_ID_KEY)) {
                "$TENANT_ID_KEY is required!"
            }.asText(),
            statements = statements
        )
    }
}
