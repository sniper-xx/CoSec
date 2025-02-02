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

dependencies {
    api(platform("org.springframework.boot:spring-boot-dependencies:2.7.7"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.5"))
    api(platform("me.ahoo.cosid:cosid-bom:1.16.9"))
    api(platform("me.ahoo.cocache:cocache-bom:0.4.0"))
    api(platform("me.ahoo.cosky:cosky-bom:3.1.0"))
    api(platform("me.zhyd.oauth:JustAuth:1.16.5"))
    api(platform("io.opentelemetry:opentelemetry-bom:1.22.0"))
    constraints {
        api("ognl:ognl:3.3.4")
        api("com.auth0:java-jwt:4.2.1")
        api("org.lionsoul:ip2region:2.6.6")
        api("com.google.guava:guava:31.1-jre")
        api("io.opentelemetry:opentelemetry-semconv:1.20.1-alpha")
        api("org.junit-pioneer:junit-pioneer:1.9.1")
        api("org.hamcrest:hamcrest:2.2")
        api("io.mockk:mockk:1.13.3")
        api("org.openjdk.jmh:jmh-core:1.36")
        api("org.openjdk.jmh:jmh-generator-annprocess:1.36")
        api("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")
    }
}
