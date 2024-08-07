package it.polito.mad.g18.mad_lab5.firebaseRepositories

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.viewModels.GroupBy
import it.polito.mad.g18.mad_lab5.viewModels.Order
import it.polito.mad.g18.mad_lab5.viewModels.TaskFilters
import it.polito.mad.g18.mad_lab5.viewModels.TaskStatus
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.viewModels.Repetition
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named


class TaskRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    @Named("userMe") var userMe: StateFlow<UserData?>
) : TaskRepository {

    override fun getTask(id: String): Flow<TaskData> = callbackFlow {
        val listenerRegistration = db.collection("tasks").document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val taskData = snapshot?.let {
                    TaskData(
                        id = it.reference.id,
                        title = it.getString("title").orEmpty(),
                        teamId = it.getString("teamId").orEmpty(),
                        assignees = (it["assignees"] as? List<String>) ?: emptyList(),
                        creationDate = it.getString("creationDate")?.let { d ->
                            LocalDate.parse(d)
                        } ?: LocalDate.now(),
                        //repeat = ,
                        dueDate = it.getString("dueDate")?.let { d ->
                            LocalDate.parse(d)
                        } ?: LocalDate.now(),
                        tags = (it["tags"] as? List<String>) ?: emptyList(),

                        status = TaskStatus.valueOf(it.getString("status") ?: "PENDING"),
                        category = it.getString("category").orEmpty(),
                        description = it.getString("description").orEmpty(),
                        chatId = it.getString("chatId").orEmpty()
                    )

                }


                trySend(taskData ?: TaskData())
            }

        awaitClose { listenerRegistration.remove() }
    }

    override fun getFilteredTasks(filters: TaskFilters): Flow<Map<String, List<TaskData>>> =
        callbackFlow {

            val snapshotListener = db.collection("tasks")
                //.whereIn("teamId", filters.teams)
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        val tasks = snapshot.map {
                            TaskData(
                                id = it.reference.id,
                                title = it.getString("title").orEmpty(),
                                teamId = it.getString("teamId").orEmpty(),
                                assignees = (it["assignees"] as? List<String>) ?: emptyList(),
                                creationDate = it.getString("creationDate")?.let { d ->
                                    LocalDate.parse(d)
                                } ?: LocalDate.now(),
                                //repeat = ,
                                dueDate = it.getString("dueDate")?.let { d ->
                                    LocalDate.parse(d)
                                } ?: LocalDate.now(),
                                tags = (it["tags"] as? List<String>) ?: emptyList(),

                                status = TaskStatus.valueOf(it.getString("status") ?: "PENDING"),
                                category = it.getString("category").orEmpty(),
                                description = it.getString("description").orEmpty(),
                                chatId = it.getString("name").orEmpty()
                            )
                        }

                        trySend(applyFilter(tasks, filters))

                    } else {
                        close(e)
                    }
                }
            awaitClose {
                snapshotListener.remove()
            }
        }

    override fun getCategories(teamId: String?): Flow<List<String>> = callbackFlow {
        val query = if(teamId!= null){
            db.collection("tasks").whereEqualTo("teamId", teamId)
        } else{userMe.value?.teams?.let {
            if (it.isNotEmpty()) {
                db.collection("tasks").whereIn("teamId", it)
            } else {
                trySend(emptyList())
                close()
                return@callbackFlow
            }
        } ?: run {
            close(Exception("User not logged in"))
            return@callbackFlow
        }}

        val snapshotListener = query
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val categories = snapshot.map {
                        it.getString("category").orEmpty()
                    }.filter { it.isNotBlank() }
                    trySend(categories)
                } else {
                    close(e)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }


    override suspend fun createTask(task: TaskData, numRepetitions: Int): Result<String> {

        val user = userMe.value ?: return Result.failure(Exception("User not logged in"))

        val firebaseTasks = mutableListOf<Map<String, Any?>>()
        repeat(numRepetitions){
            val dueDate = when(task.repeat){
                    Repetition.NONE ->  task.dueDate.toString()
                    Repetition.DAILY -> task.dueDate?.plusDays(it.toLong()).toString()
                    Repetition.WEEKLY -> task.dueDate?.plusWeeks(it.toLong()).toString()
                    Repetition.MONTHLY -> task.dueDate?.plusMonths(it.toLong()).toString()
            }

            firebaseTasks.add(mapOf(
                "title" to task.title,
                "teamId" to task.teamId,
                "assignees" to task.assignees,
                "creationDate" to LocalDate.now().toString(),
                "dueDate" to dueDate,
                "tags" to task.tags,
                "status" to task.status,
                "category" to task.category,
                "description" to task.description,
            ))
        }


        return runCatching {
            var taskIdToShow = ""

            db.runTransaction { transaction ->

                firebaseTasks.forEach { firebaseTask->
                    val taskDocument = db.collection("tasks").document()
                    val chatDocument = db.collection("chats").document()

                    if(taskIdToShow.isBlank()) taskIdToShow = taskDocument.id

                    transaction.set(taskDocument, firebaseTask)
                    transaction.set(
                        chatDocument, mapOf(
                            "teamId" to task.teamId,
                            "taskId" to taskDocument.id,
                            "firstUserId" to "",
                            "secondUserId" to ""
                        )
                    )
                    transaction.update(taskDocument, mapOf("chatId" to chatDocument.id))
                    transaction.set(taskDocument.collection("history").document(), mapOf(
                            "userId" to user.id,
                    "timeStamp" to Timestamp.now(),
                    "msgContent" to "Task created by @${user.userName}"
                    ))

                    task.assignees.forEach {
                        val userChatDocument = db.collection("users")
                            .document(it).collection("chats").document(chatDocument.id)

                        transaction.set(
                            userChatDocument, mapOf(
                                "userId" to "",
                                "title" to task.title,
                                "pfp" to "",
                                //empty fields
                                "teamId" to task.teamId,
                                "taskId" to taskDocument.id,
                                "lastMessage" to mapOf(
                                    "userId" to user.id,
                                    "timeStamp" to Timestamp.now(),
                                    "msgContent" to ""
                                ),
                                "unread" to false
                            )
                        )
                    }
                }
                taskIdToShow
            }.await()
        }
    }

    override suspend fun updateTask(task: TaskData): Result<String> {
        val user = userMe.value ?: return Result.failure(Exception("User not logged in"))


        val firebaseTask = mapOf(
            "title" to task.title,
            "assignees" to task.assignees,
            //"repetition" to
            "dueDate" to task.dueDate.toString(),
            "tags" to task.tags,
            "status" to task.status,
            "category" to task.category,
            "description" to task.description,
            //history
        )

        return runCatching {
            val taskDocument = db.collection("tasks").document(task.id)
            val (oldTitle, oldAssignees) = taskDocument.get().await().let{
                Pair(
                    it.getString("title").orEmpty(),
                    (it["assignees"] as? List<String>) ?: emptyList()
                )
            }
            val assigneesToRemove = oldAssignees.filter { !task.assignees.contains(it) }
            val assigneesToAdd = task.assignees.groupBy { !oldAssignees.contains(it) }

            db.runTransaction { transaction ->

                transaction.set(taskDocument.collection("history").document(), mapOf(
                    "userId" to user.id,
                    "timeStamp" to Timestamp.now(),
                    "msgContent" to "Task updated by @${user.userName}"
                ))
                transaction.update(taskDocument, firebaseTask)

                assigneesToAdd[true]?: emptyList<String>().forEach{
                    val userChatDocument = db.collection("users")
                        .document(it).collection("chats").document(task.chatId)

                    transaction.set(userChatDocument, mapOf(
                        "userId" to "",
                        "title" to task.title,
                        "pfp" to "",
                        //empty fields
                        "teamId" to task.teamId,
                        "taskId" to taskDocument.id,
                        "lastMessage" to mapOf(
                            "userId" to user.id,
                            "timeStamp" to Timestamp.now(),
                            "msgContent" to ""
                        ),
                        "unread" to true
                    ))
                }

                if(oldTitle != task.title) {
                    assigneesToAdd[false] ?: emptyList<String>().forEach {
                        val userChatDocument = db.collection("users")
                            .document(it).collection("chats").document(task.chatId)
                        transaction.update(userChatDocument, mapOf("title" to task.title))
                    }
                }

                assigneesToRemove.forEach{
                    val userChatDocument = db.collection("users")
                        .document(it).collection("chats").document(task.chatId)

                    transaction.delete(userChatDocument)
                }


                taskDocument.id

            }.await()
        }
    }

    override suspend fun deleteTask(id: String): Result<String> {
        return runCatching {
            val task = getTask(id).first()

            db.runTransaction { transaction ->
                val taskDocument = db.collection("tasks").document(id)
                val taskChatDocument = db.collection("chats").document(task.chatId)

                task.assignees.forEach{
                    val userChatDocument = db.collection("users")
                        .document(it).collection("chats").document(id)

                    transaction.delete(userChatDocument)
                }

                transaction.delete(taskChatDocument)
                transaction.delete(taskDocument)
                id
            }.await()
        }
    }

    override fun getHistory(taskId: String): Flow<List<Message>> = callbackFlow {

        val snapshotListener = db.collection("tasks").document(taskId).collection("history")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val history = snapshot.map {
                        Message(
                            userId = it.getString("userId").orEmpty(),
                            timeStamp = (it?.get("timeStamp") as Timestamp)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime(),
                            msgContent = it.getString("msgContent").orEmpty()
                        )
                    }
                    trySend(history)

                } else {
                    close(e)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

    private fun applyFilter(
        list: List<TaskData>,
        filters: TaskFilters
    ): Map<String, List<TaskData>> {

        var res = list
            /*            .filter {
                        searchTerm.isBlank() ||
                                (it.team.lowercase().contains(searchTerm) ||
                                        it.assignees.any { a ->
                                            a.lowercase().contains(searchTerm)
                                        } ||
                                        it.tags.any { a ->
                                            a.lowercase().contains(searchTerm)
                                        } ||
                                        it.status.displayed.lowercase().contains(searchTerm) ||
                                        it.category.lowercase().contains(searchTerm) ||
                                        it.description.lowercase().contains(searchTerm) ||
                                        it.discussion.any { a ->
                                            a.msgContent.toString().lowercase()
                                                .contains(searchTerm) //CAMBIATO QUI PERCHÉ IL FORMATO ORA È DIVERSO
                                        }
                                        )
                    }*/
            .filter {
                (filters.teams.isEmpty() || filters.teams.all { teamId -> teamId == it.teamId }) &&
                        (filters.assignees.isEmpty() || it.assignees.any { a ->
                            filters.assignees.contains(
                                a
                            )
                        }) &&
                        (filters.tags.isEmpty() || it.tags.any { a -> filters.tags.contains(a) }) &&
                        (filters.statuses.isEmpty() || filters.statuses.contains(it.status)) &&
                        (filters.categories.isEmpty() || filters.categories.contains((it.category))) &&
                        it.dueDate!! >= (filters.startDate
                    ?: LocalDate.MIN) && it.dueDate <= (filters.endDate
                    ?: LocalDate.MAX)
            }.sortedBy {
                when (filters.groupBy) {
                    GroupBy.DATE -> it.dueDate
                    GroupBy.STATUS -> it.status.ordinal
                    GroupBy.CATEGORY -> it.category
                }.toString()
            }

        if (filters.order == Order.DESC) {
            res = res.reversed()
        }


        return res.groupBy {
            when (filters.groupBy) {
                GroupBy.DATE -> it.dueDate.run {
                    val date = this!!
                    if (date.year == LocalDate.now().year && date.dayOfYear == LocalDate.now().dayOfYear) {
                        date.format(DateTimeFormatter.ofPattern("EEE dd MMM")) + " (Today)"
                    }
                    date.format(DateTimeFormatter.ofPattern("EEE dd MMM"))
                }

                GroupBy.STATUS -> it.status.displayed
                GroupBy.CATEGORY -> it.category
            }.toString()
        }
    }

}
