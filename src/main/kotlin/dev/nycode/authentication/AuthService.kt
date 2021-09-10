package dev.nycode.authentication

import de.nycode.bcrypt.hash
import de.nycode.bcrypt.verify
import dev.nycode.util.getLogger
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
    companion object {
        private val logger = getLogger<AuthService>()
    }

    private val collection: CoroutineCollection<UserEntry> by lazy {
        client.getDatabase(database)
            .database
            .getCollection(KMongoUtil.defaultCollectionName(UserEntry::class), UserEntry::class.java)
            .coroutine
    }

    init {
        runBlocking {
            collection.ensureUniqueIndex(UserEntry::username)
            val initUserName: String? = System.getenv("ZEITUNG_INITUSER_USERNAME")
            val initUserPass: String? = System.getenv("ZEITUNG_INITUSER_PASSWORD")
            logger.info("Found init environment variables. Trying to create user.")
            if (initUserName != null && initUserPass != null && collection.findOne(UserEntry::username eq initUserName) == null) {
                createUser(initUserName, initUserPass)
                logger.info("Successfully created init user with username $initUserName")
            } else {
                logger.warn("Unable to create user. Check if the user already exits or if both environment variables are set.")
            }
        }
    }

    suspend fun findUser(username: String, password: String): UserEntry? {
        val user = collection.findOne(UserEntry::username eq username) ?: return null
        return if (verify(password + salt, user.password)) {
            user
        } else {
            null
        }
    }

    suspend fun createUser(username: String, password: String): UserEntry {
        val hashedPassword: ByteArray = hash(password + salt)
        val userEntry = UserEntry(username, hashedPassword)
        collection.save(userEntry)
        logger.info("Created a new user `$username`")
        return userEntry
    }
}
