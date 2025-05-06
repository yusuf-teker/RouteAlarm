package org.yusufteker.routealarm.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun createClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                // ContentNegotiation ekstra özellik direkt olarak JSONu Data Classa setliyor
                //json() // JSON desteği ekliyoruz
                json(
                    json = Json { // json u özelliştiriyoruz
                        ignoreUnknownKeys = true // apide fazladan özellik varsa bizim data classımıza parse ederken sorun çıkarmamasını sağlıyor
                    }
                )

            }
            install(HttpTimeout) {
                socketTimeoutMillis = 20_000L // Bağlantı kurulduktan sonra 20 saniye veri akışı olmazsa bağlantıyı bitir
                requestTimeoutMillis = 20_000L // toplam istek 20 sn sürecek client gidis + sunucu donus 20sn
            }
            install(Logging) { // ekstra client özelliği ekleme
                logger = object : Logger { // Ktor default loggerı yok o yüzden mecbur bir logger eklemeliyi
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }
            defaultRequest { // sunucuya json tipinde veri göndereceğimizi beliritiyoruz
                contentType(ContentType.Application.Json)
            }
            /* defaultRequest ile her post put vs işlemince header belirtmemize gerek olmaz
            client.post("https://api.example.com/data") {
                headers {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
                setBody(myData)
                }
            * */
        }
    }
}