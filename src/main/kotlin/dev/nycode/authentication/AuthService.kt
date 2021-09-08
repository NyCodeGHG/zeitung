package dev.nycode.authentication

import de.nycode.bcrypt.hash
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.util.KMongoUtil

@Singleton
class AuthService(
    client: CoroutineClient,
    @Property(name = "mongodb.database") database: String,
    @Property(name = "zeitung.salt") private val salt: String
) {
    private val collection: CoroutineCollection<UserEntry> by lazy {
        client.getDatabase(database)
            .database
            .getCollection(KMongoUtil.defaultCollectionName(UserEntry::class), UserEntry::class.java)
            .coroutine
    }

    init {
        runBlocking {
            collection.ensureUniqueIndex(UserEntry::username)
        }
    }

    suspend fun findUser(username: String, password: String): UserEntry? {
        val hashedPassword: ByteArray = hash(password + salt)
        return collection.findOne(and(UserEntry::username eq username, UserEntry::password eq hashedPassword))
    }

    suspend fun createUser(username: String, password: String): UserEntry {
        val hashedPassword: ByteArray = hash(password + salt)
        val userEntry = UserEntry(username, hashedPassword)
        collection.save(userEntry)
        return userEntry
    }
}
