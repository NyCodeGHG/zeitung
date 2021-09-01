package dev.nycode.mongo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import org.bson.codecs.configuration.CodecRegistry
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.id.jackson.IdJacksonModule
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.service.ClassMappingType
import com.fasterxml.jackson.databind.Module as JacksonDatabindModule

@Factory
@Requires(classes = [KMongo::class])
class KMongoFactory {

    /**
     * Inject KMongo's [CodecRegistry] for mapping data classes.
     */
    @Singleton
    fun codecRegistry(): CodecRegistry = ClassMappingType.codecRegistry(MongoClientSettings.getDefaultCodecRegistry())

    /**
     * Inject KMongo's [CoroutineClient] instead of the default [MongoClient].
     */
    @Singleton
    fun coroutineClient(client: MongoClient): CoroutineClient = client.coroutine

    /**
     * Jackson: convert Mongo ObjectId's to string and vice versa.
     */
    @Singleton
    fun idJacksonModule(): JacksonDatabindModule = IdJacksonModule()

    /**
     * Jackson: convert [java.time.LocalDate] to and from strings.
     */
    @Singleton
    fun javaTimeJacksonModule(): JacksonDatabindModule = JavaTimeModule()

}
