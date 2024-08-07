package it.polito.mad.g18.mad_lab5.firebaseRepositories

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import com.google.firebase.storage.FirebaseStorage
import it.polito.mad.g18.mad_lab5.ChatData
import it.polito.mad.g18.mad_lab5.ChatTeamData
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.UserChatData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.ChatRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId

class ChatRepositoryImpl(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
): ChatRepository {

    private val COLLECTION = "chats"
    private val SUBCOLLECTION ="messages"

    // ok
    override fun getChats(userId: String): Flow<List<UserChatData>> = callbackFlow {

        val listener = db
            .collection("users")
            .document(userId)
            .collection(COLLECTION)
            //.whereEqualTo("taskId", "")
            .orderBy("lastMessage")
            .addSnapshotListener { res, err ->
                if(err != null){
                    return@addSnapshotListener
                }

                val chats = res?.map { x ->
                        x.let {
                          val lastMessage = it.data["lastMessage"] as? Map<*,*>?
                            UserChatData(
                                chatId = it.id,
                                userId = it.getString("userId").orEmpty(),
                                teamId = it.getString("teamId").orEmpty(),
                                taskId = it.getString("taskId").orEmpty(),
                                pfp = it.getString("pfp") ?: "",
                                title = it.getString("title") ?: "",
                                lastMessage = if(lastMessage == null ) { Message() } else {
                                    Message(
                                        userId = lastMessage["userId"].toString(),
                                        timeStamp = (lastMessage["timeStamp"] as? Timestamp)
                                            ?.toInstant()
                                            ?.atZone(ZoneId.systemDefault())
                                            ?.toLocalDateTime()?:LocalDateTime.MIN,
                                        msgContent = lastMessage["msgContent"].toString()
                                    )
                                },
                                unread = it.getBoolean("unread")?:false
                            )
                        }
                    }
                    ?.toList()

                if (chats != null) { trySend(chats.filter{ it.taskId.isBlank() }.sortedByDescending { it.lastMessage.timeStamp }) }
            }
        awaitClose { listener.remove() }
    }

    // ok
    override fun getCurrentChat(userId: String, chatId: String): Flow<UserChatData> = callbackFlow {
        val listener = db
            .collection("users")
            .document(userId)
            .collection(COLLECTION)
            .document(chatId)
            .addSnapshotListener { res, err ->
                if(err != null){
                    return@addSnapshotListener
                }

                val chat = res?.let {
                    UserChatData(
                        chatId = it.id,
                        userId = it.getString("userId").orEmpty(),
                        teamId = it.getString("teamId").orEmpty(),
                        taskId = it.getString("taskId").orEmpty(),
                        pfp = it.getString("pfp") ?: "",
                        title = it.getString("title") ?: "",
                    )
                }
                trySend(chat?:UserChatData())
            }
        awaitClose { listener.remove() }

    }

    // ok
    override fun getMessages(chatId: String): Flow<List<Message>?> = callbackFlow {
        val listener = db
            .collection(COLLECTION)
            .document(chatId)
            .collection(SUBCOLLECTION)
            .orderBy("timeStamp")
            .addSnapshotListener { res, err ->
                if(err != null){
                    return@addSnapshotListener
                }

                val messages = res
                    ?.map { x ->
                        x.let {
                            Message(
                                userId = it.getString("userId") ?: "",
                                timeStamp = (it?.get("timeStamp") as Timestamp)
                                    .toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime(),
                                msgContent = it.getString("msgContent") ?: "",
                            )
                        }
                    }
                    ?.toList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    // ok
    override suspend fun sendMessage(chatId: String, m: Message, userMeId: String, userId: String, teamId: String, taskId: String): Result<String> {
        val firebaseUser = com.google.firebase.Firebase.auth.currentUser ?: return Result.failure(Exception("User not logged in"))

        val newMessage = mapOf(
            "userId" to m.userId,
            "timeStamp" to Timestamp.now(),
            "msgContent" to m.msgContent
        )

        return runCatching {
            val membersDocuments = if (teamId.isNotBlank()) {
                db.collection("teams")
                    .document(teamId)
                    .collection("members")
                    .get()
                    .await()
                    .documents
            } else emptyList()

            val taskAssignees = if (teamId.isNotBlank() && taskId.isNotBlank()) {
                val taskDocument = db.collection("tasks")
                    .document(taskId)
                    .get()
                    .await()
                (taskDocument["assignees"] as? List<String>) ?: emptyList()
            } else emptyList()

            db.runTransaction { transaction ->
                val messageDocument = db.collection(COLLECTION)
                    .document(chatId)
                    .collection("messages")
                    .document()
                transaction.set(messageDocument, newMessage)

                // REPLACE WITH CLOUD FUNCTION
                val userMeChatDocument = db.collection("users")
                    .document(userMeId)
                    .collection("chats")
                    .document(chatId)
                transaction.update(userMeChatDocument,
                    mapOf(
                        "unread" to false,
                        "lastMessage" to newMessage
                    ))

                if (userId.isNotBlank()) {
                    val userChatDocument = db.collection("users")
                        .document(userId)
                        .collection("chats")
                        .document(chatId)
                    transaction.update(userChatDocument,
                        mapOf(
                            "unread" to true,
                            "lastMessage" to newMessage
                        ))
                } else if (teamId.isNotBlank()) {
                    if (taskId.isNotBlank()) {
                        membersDocuments.filter{it.id!=userMeId}.filter {
                            taskAssignees.contains(it.id)
                        }.forEach { member ->
                            val userChatDocument = db.collection("users")
                                .document(member.id)
                                .collection("chats")
                                .document(chatId)
                            transaction.update(userChatDocument,
                                mapOf(
                                    "unread" to true,
                                    "lastMessage" to newMessage
                                ))
                        }

                    } else {
                        membersDocuments.filter{it.id!=userMeId}.forEach { member ->
                            val userChatDocument = db.collection("users")
                                .document(member.id)
                                .collection("chats")
                                .document(chatId)
                            transaction.update(userChatDocument,
                                mapOf(
                                    "unread" to true,
                                    "lastMessage" to newMessage
                                ))
                        }
                    }
                }
                messageDocument.id
            }.await()
        }
    }

    override suspend fun setChatRead(userId: String, chatId: String, m: Message): Result<String> {
        val firebaseUser = com.google.firebase.Firebase.auth.currentUser ?:
        return Result.failure(Exception("User not logged in"))


        return runCatching {
            db.collection("users")
                .document(userId)
                .collection(COLLECTION).document(chatId)
                .update("unread", false)

            chatId
        }
    }

    // ok
    override suspend fun createDMChat(user1: UserData, user2: UserData): Result<String> {


        // we need to add 3 documents
        val chat = mapOf(
            "firstUserId" to user1.id,
            "secondUserId" to user2.id,
            "teamId" to "",
            "taskId" to "",
        )

        val firstUserChat = mapOf(
            "userId" to user2.id,
            "title" to "${user2.name} ${user2.surname}",
            "pfp" to user2.profilePicture,
            //empty fields
            "teamId" to "",
            "taskId" to "",
            "lastMessage" to mapOf(
                "userId" to user1.id,
                "timeStamp" to Timestamp.now(),
                "msgContent" to ""
            ),
            "unread" to false

        )


        val secondUserChat = mapOf(
            "userId" to user1.id,
            "title" to "${user1.name} ${user1.surname}",
            "pfp" to user1.profilePicture,
            //empty fields
            "teamId" to "",
            "taskId" to "",
            "lastMessage" to mapOf(
                "userId" to user1.id,
                "timeStamp" to Timestamp.now(),
                "msgContent" to ""
            ),
            "unread" to false
        )

        return runCatching {

            db.runTransaction {transaction ->

                val chatDocument = db.collection("chats").document()
                transaction.set(chatDocument, chat)

                val firstUserChatDocument = db
                    .collection("users")
                    .document(user1.id)
                    .collection("chats")
                    .document(chatDocument.id)

                transaction.set(firstUserChatDocument, firstUserChat)

                val secondUserChatDocument = db
                    .collection("users")
                    .document(user2.id)
                    .collection("chats")
                    .document(chatDocument.id)

                transaction.set(secondUserChatDocument, secondUserChat)

                chatDocument.id
            }.await()
        }
    }

    // not tested
    override suspend fun deleteChat(chatId: String): Result<String> {

        return runCatching {
            val chatDoc = db
                .collection(COLLECTION)
                .document(chatId)
                .get()
                .result

            val userIds: List<String>? = chatDoc.getString("userIds")?.split("-")
            val teamIds: List<String>? = chatDoc.getString("userIds")?.split("-")

            if(userIds != null) {

                // delete users chat
                db.runTransaction {t ->

                    val user1Path = db.collection("users")
                        .document(userIds[0])
                        .collection(COLLECTION)

                    val user2Path = db.collection("users")
                        .document(userIds[1])
                        .collection(COLLECTION)

                    val user1ChatId = user1Path
                        .whereEqualTo("chatId", chatId)
                        .get()
                        .result
                        .documents[0].id

                    val user2ChatId = user2Path
                        .whereEqualTo("chatId", chatId)
                        .get()
                        .result
                        .documents[0].id

                    t.delete(user1Path.document(user1ChatId))
                        .delete(user2Path.document(user2ChatId))
                }.await()

            } else if (teamIds != null) {
                if (teamIds.size == 1) {
                    // delete team chat
                    db.runTransaction {t ->

                        val teamChatPath = db.collection("teams")
                            .document(teamIds[0])
                            .collection(COLLECTION)

                        val teamChatId = teamChatPath
                            .whereEqualTo("chatId", chatId)
                            .get()
                            .result
                            .documents[0].id

                        t.delete(teamChatPath.document(teamChatId))
                    }

                } else {
                    // delete task chat
                    db.runTransaction {t ->

                        // ?????
                    }
                }

            }


            "Operation sent successfully"
        }
    }
}