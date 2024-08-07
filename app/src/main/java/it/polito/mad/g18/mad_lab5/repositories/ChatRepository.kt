package it.polito.mad.g18.mad_lab5.repositories

import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.UserChatData
import it.polito.mad.g18.mad_lab5.UserData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface ChatRepository {
    fun getChats(userId: String):Flow<List<UserChatData>>

    fun getCurrentChat(userId: String, chatId: String): Flow<UserChatData>

    fun getMessages(chatId: String): Flow<List<Message>?>

/*
    fun getLastMessageTimestamp(chatId: String, timestamp: LocalDateTime): Flow<Boolean>
*/

    suspend fun setChatRead(userId: String, chatId: String, m: Message): Result<String>

    suspend fun createDMChat(user1: UserData, user2: UserData): Result<String>

    suspend fun deleteChat(chatId: String): Result<String>
    suspend fun sendMessage(
        chatId: String,
        m: Message,
        userMeId: String,
        userId: String,
        teamId: String,
        taskId: String
    ): Result<String>
}