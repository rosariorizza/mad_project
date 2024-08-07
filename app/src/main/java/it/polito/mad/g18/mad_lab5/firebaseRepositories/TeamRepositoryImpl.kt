package it.polito.mad.g18.mad_lab5.firebaseRepositories

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.util.fastJoinToString
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.UserTeamRequestData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.viewModels.Order
import it.polito.mad.g18.mad_lab5.viewModels.OrderField
import it.polito.mad.g18.mad_lab5.viewModels.TeamFilters
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import it.polito.mad.g18.mad_lab5.viewModels.TaskFilters
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Named


class TeamRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    @Named("userMe") var userMe: StateFlow<UserData?>,
    private val taskRepository: TaskRepository
) : TeamRepository {

    private val storageRef = storage.reference
    private val teamsStorageRef = storageRef.child("teams")

    override fun getTeam(id: String): Flow<TeamData> = callbackFlow {
        val listenerRegistration = db.collection("teams").document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                val teamData = snapshot?.let {
                    TeamData(
                        //profilePicture = ,
                        id = it.reference.id,
                        name = it.getString("name").orEmpty(),
                        profilePicture = it.getString("pfp").orEmpty(),
                        description = it.getString("description").orEmpty(),
                        category = it.getString("category").orEmpty(),
                        creationDate = it.getString("creationDate")?.let { d ->
                            LocalDate.parse(d)
                        } ?: LocalDate.now(),
                        chatId = it.getString("chatId").orEmpty(),
                        achievements = (it["achievements"] as? List<Int>) ?: emptyList(),
                        tags = (it["tags"] as? List<String>) ?: emptyList(),
                    )
                }
                trySend(teamData?:TeamData())
            }

        awaitClose { listenerRegistration.remove() }

    }

    override suspend fun getUserMeRole(teamId: String): Flow<UserRole> = callbackFlow {
        val userId = userMe.value?.id  ?: run {
            close()
            return@callbackFlow
        }
        val listenerRegistration = db.collection("teams").document(teamId).collection("members").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val role = UserRole.valueOf(snapshot?.getString("role") ?: UserRole.VIEWER.toString())
                trySend(role)
            }

        awaitClose { listenerRegistration.remove() }

    }

    override fun getFilteredTeams(filters: TeamFilters) = callbackFlow {


        val query = userMe.value?.teams?.let {
            if (it.isNotEmpty()) {
                db.collection("teams").whereIn(FieldPath.documentId(), it)
            } else {
                trySend(emptyList())
                close()
                return@callbackFlow
            }
        } ?: run {
            close(Exception("User not logged in"))
            return@callbackFlow
        }



        val snapshotListener = query.addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val teams = snapshot.map {
                        TeamData(

                            //profilePicture = ,
                            id = it.reference.id,
                            name = it.getString("name").orEmpty(),
                            profilePicture = it.getString("pfp").orEmpty(),
                            description = it.getString("description").orEmpty(),
                            category = it.getString("category").orEmpty(),
                            creationDate = it.getString("creationDate")?.let { d ->
                                LocalDate.parse(d)
                            } ?: LocalDate.now(),
                            chatId = it.getString("chatId").orEmpty(),
                            achievements = (it["achievements"] as? List<Int>) ?: emptyList(),
                            tags = (it["tags"] as? List<String>) ?: emptyList(),
                        )
                    }

                    // mi piange il cuore ma apparentemente firebase non supporta
                    // query di disuguaglianza con altre di uguaglianza
                    // (quindi le date non possono essere filtrate)
                    // si fa tutto in locale
                    trySend(applyFilter(teams, filters))
                } else {
                    close(e)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

    override fun getCategories(): Flow<List<String>> = callbackFlow {

        val query = userMe.value?.teams?.let {
            if (it.isNotEmpty()) {
                db.collection("teams").whereIn(FieldPath.documentId(), it)
            } else {
                trySend(emptyList())
                close()
                return@callbackFlow
            }
        } ?: run {
            close(Exception("User not logged in"))
            return@callbackFlow
        }
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



    override fun getTeamMembers(id: String): Flow<List<UserTeamData>> = callbackFlow {
        if(id.isBlank()){
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val snapshotListener = db.collection("teams").document(id).collection("members")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val members = snapshot.map {
                        UserTeamData(
                            //profilePicture = ,
                            id = it.reference.id,
                            name = it.getString("name").orEmpty(),
                            surname = it.getString("surname").orEmpty(),
                            userName = it.getString("userName").orEmpty(),
                            role = UserRole.valueOf(it.getString("role") ?: UserRole.VIEWER.displayed)
                        )
                    }
                    trySend(members)
                } else {
                    close(e)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

    override fun getTeamRequests(id: String): Flow<List<UserTeamRequestData>> = callbackFlow {
        val snapshotListener = db.collection("teams").document(id).collection("requests")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val requests = snapshot.map {
                        UserTeamRequestData(
                            //profilePicture = ,
                            id = it.reference.id,
                            name = it.getString("name").orEmpty(),
                            surname = it.getString("surname").orEmpty(),
                            userName = it.getString("userName").orEmpty(),
                            requestDate = it.getString("requestData")?.let { d ->
                                LocalDateTime.parse(d)
                            } ?: LocalDateTime.MIN,

                        )
                    }
                    trySend(requests)
                } else {
                    close(e)
                }
            }
        awaitClose {
            snapshotListener.remove()
        }
    }

    override suspend fun createTeam(team: TeamData): Result<String> {

        val user = userMe.value ?: return Result.failure(Exception("User not logged in"))

        val firebaseTeam = mapOf(
            "name" to team.name,
            "category" to team.category,
            "creationDate" to team.creationDate.toString(),
            "description" to team.description,
            "achievements" to listOf(0, 1)
        )
        val firebaseTeamMember = mapOf(
            "name" to user.name,
            "surname" to user.surname,
            "userName" to user.userName,
            "role" to UserRole.ADMIN,
            "joinDate" to LocalDate.now().toString()
            //profilePicture
            //performance
        )



        return runCatching {
            db.runTransaction { transaction ->
                //team creation
                val teamDocument = db.collection("teams").document()
                transaction.set(teamDocument, firebaseTeam)

                //chat creation
                val chatDocument = db.collection("chats").document()
                val chat = mapOf(
                    "firstUserId" to "",
                    "secondUserId" to "",
                    "teamId" to teamDocument.id,
                    "taskId" to "",
                )
                transaction.set(chatDocument, chat)
                teamDocument.update("chatId", chatDocument.id)

                // user updates
                val userDocument = db.collection("users").document(user.id)
                transaction.set(
                    teamDocument.collection("members").document(user.id),
                    firebaseTeamMember
                )
                val userDocumentChat = userDocument.collection("chats").document(chatDocument.id)
                transaction.set(userDocumentChat, mapOf(
                    "userId" to "",
                    "title" to team.name,
                    "pfp" to team.profilePicture,
                    //empty fields
                    "teamId" to teamDocument.id,
                    "taskId" to "",
                    "lastMessage" to mapOf(
                        "userId" to userDocument.id,
                        "timeStamp" to Timestamp.now(),
                        "msgContent" to ""
                    ),
                    "unread" to false
                ))
                transaction.update(
                    userDocument,
                    FieldPath.of("teams"),
                    FieldValue.arrayUnion(teamDocument.id)
                )
                transaction.update(teamDocument, mapOf(
                    "chatId" to chatDocument.id
                ))

                teamDocument.id
            }.await()
        }
    }

    override suspend fun updateTeam(team: TeamData): Result<String> {
        val firebaseTeam = mapOf(
            "profilePicture" to team.profilePicture,
            "name" to team.name,
            "category" to team.category,
            "creationDate" to team.creationDate.toString(),
            "description" to team.description,
            "achievements" to emptyList<Any>()
        )

        val members = getTeamMembers(team.id).first()

        return runCatching {
            db.runTransaction { transaction ->
                val teamDocument = db.collection("teams").document(team.id)

                transaction.update(teamDocument, firebaseTeam)
                members.forEach{
                    val user = db.collection("users").document(it.id)
                        .collection("chats").document(team.chatId)
                    transaction.update(user, mapOf("title" to team.name, "pfp" to team.profilePicture))
                }

                teamDocument.id
            }.await()
        }
    }

    override suspend fun setTeamPfp(teamId: String, pfp: Bitmap): Result<String> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        val thisUserStorageRef = teamsStorageRef.child(teamId)
        val pfpName = "BEGROUP_IMAGE_"+LocalDateTime.now().toString()+".jpg"
        val pfpStorageRef = thisUserStorageRef.child(pfpName)
        val baos = ByteArrayOutputStream()
        pfp.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val bytes = baos.toByteArray()

        val userIds = getTeamMembers(teamId).first().map { x -> x.id }
        var userChatRefs = emptyList<DocumentReference>()
        userIds.forEach { uId -> // per ogni utente trovato
            db.collection("users") // chat dell'utente
                .document(uId)
                .collection("chats")
                .whereEqualTo("teamId",teamId)
                .get()
                .addOnSuccessListener { userChatSnapshot ->
                    val userChats = userChatSnapshot.documents.map { x -> x.id }
                    userChats.forEach { x ->
                        val userChatDocRef = db.collection("users").document(uId).collection("chats").document(x)
                        userChatRefs+= userChatDocRef
                    }
                }.await()

        }

        return runCatching {
            val urlTask = pfpStorageRef
                .putBytes(bytes)
                .continueWithTask { task ->
                    if(!task.isSuccessful) {
                        Log.d("STORAGE", "Upload failed")
                    }
                    pfpStorageRef.downloadUrl
                }.onSuccessTask {uri ->

                    db.runTransaction {t ->

                        // update team pfp
                        val userDocRef = db.collection("teams")
                            .document(teamId)
                        t.update(userDocRef,"pfp",uri)

                        // update other chats where the user is present
                        userChatRefs.forEach {
                            t.update(it,"pfp",uri)
                        }

                    }
                }
            urlTask.await().toString()
        }
    }

    override suspend fun deleteTeamPfp(teamId: String): Result<String> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        val userIds = getTeamMembers(teamId).first().map { x -> x.id }
        var userChatRefs = emptyList<DocumentReference>()
        userIds.forEach { uId -> // per ogni utente trovato
            db.collection("users") // chat dell'utente
                .document(uId)
                .collection("chats")
                .whereEqualTo("teamId",teamId)
                .get()
                .addOnSuccessListener { userChatSnapshot ->
                    val userChats = userChatSnapshot.documents.map { x -> x.id }
                    userChats.forEach { x ->
                        val userChatDocRef = db.collection("users").document(uId).collection("chats").document(x)
                        userChatRefs+= userChatDocRef
                    }
                }.await()

        }
        return runCatching {
            db.runTransaction {t ->

                val uri = ""

                // update team pfp
                val userDocRef = db.collection("teams")
                    .document(teamId)
                t.update(userDocRef,"pfp",uri)

                // update other chats where the user is present
                userChatRefs.forEach {
                    t.update(it,"pfp",uri)
                }

            }
            "Operation sent successfully"
        }
    }

    override suspend fun addTeamMember(
        teamId: String,
        user: UserTeamRequestData,
        role: UserRole
    ): Result<String> {

        val firebaseUserTeam = mapOf(
            "name" to user.name,
            "surname" to user.surname,
            "userName" to user.userName,
            "role" to role,
            "joinDate" to LocalDate.now().toString()
            //profilePicture
            //performance
        )

        val teamDocument = db.collection("teams").document(teamId)
        val team = getTeam(teamId).first()


        return runCatching {
            db.runTransaction { transaction ->

                val userDocument = db.collection("users").document(user.id)
                val requestDocument = teamDocument.collection("requests").document(user.id)

                //se si rompe verificare l'esistenza di request document
                transaction.delete(requestDocument)

                transaction.set(
                    teamDocument.collection("members").document(user.id),
                    firebaseUserTeam
                )
                transaction.update(
                    userDocument,
                    FieldPath.of("teams"),
                    FieldValue.arrayUnion(teamDocument.id)
                )

                val userDocumentChat = userDocument.collection("chats").document(team.chatId)
                transaction.set(userDocumentChat, mapOf(
                    "userId" to "",
                    "title" to team.name,
                    "pfp" to team.profilePicture,
                    //empty fields
                    "teamId" to teamDocument.id,
                    "taskId" to "",
                    "lastMessage" to mapOf(
                        "userId" to userDocument.id,
                        "timeStamp" to Timestamp.now(),
                        "msgContent" to ""
                    ),
                    "unread" to false
                ))
                userDocument.id
            }.await()
        }
    }

    override suspend fun removeTeamMember(teamId: String, userId: String): Result<String> {
        val teamDocument = db.collection("teams").document(teamId)
        val chatId = getTeam(teamId).first().chatId
        val tasks = taskRepository.getFilteredTasks(TaskFilters(teams = listOf(teamId), assignees = listOf(userId))).first()


        return runCatching {
            db.runTransaction { transaction ->
                val userDocument = db.collection("users").document(userId)
                val memberDocument = teamDocument.collection("members").document(userId)
                val memberChatDocument = userDocument.collection("chats").document(chatId)

                //cancello la membership
                transaction.delete(memberDocument)
                //cancello la membership lato user
                transaction.update(
                    userDocument,
                    FieldPath.of("teams"),
                    FieldValue.arrayRemove(teamId)
                )
                //cancello la chat lato user
                transaction.delete(memberChatDocument)
                //task
                tasks.values.flatten().forEach{
                    val taskDocument = db.collection("tasks").document(it.id)
                    if(it.assignees.size == 1){
                        //cancello i task assegnati solo all'utente
                        transaction.delete(taskDocument)
                    } else{
                        //aggiorno i task non assegnati all'utente
                        transaction.update(taskDocument, mapOf("assignees" to it.assignees.filter { a-> a!= userId }))
                    }
                }

                userDocument.id
            }.await()
        }
    }

    override suspend fun addTeamMemberRequest(
        teamId: String,
        user: UserTeamRequestData
    ): Result<String> {

        val firebaseUserTeamRequest = mapOf(
            "name" to user.name,
            "surname" to user.surname,
            "userName" to user.userName,
            "requestDate" to LocalDate.now().toString()
        )
        return runCatching {
            db.runTransaction { transaction ->
                val teamDocument = db.collection("teams").document(teamId)

                transaction.set(
                    teamDocument.collection("requests").document(user.id),
                    firebaseUserTeamRequest
                )
                user.id
            }.await()
        }
    }

    override suspend fun rejectTeamMemberRequest(teamId: String, userId: String): Result<String> {

        return runCatching {
            db.runTransaction { transaction ->
                val teamDocument = db.collection("teams").document(teamId)
                val requestDocument = teamDocument.collection("requests").document(userId)

                transaction.delete(requestDocument)
                userId
            }.await()
        }
    }

    override suspend fun deleteTeam(id: String): Result<Void> {
        // non potendo usare le firebase functions, non si fa
        // https://firebase.google.com/docs/firestore/manage-data/delete-data#:~:text=//%20Deleting%20collections%20from%20an%20Android%20client%20is%20not%20recommended.

        Log.e("ERROR", "Delete team is not supported")
        return Result.failure(Exception("Delete team is not supported"))
    }

    override suspend fun addTagToTeam(teamId: String, tag: String): Result<String> {
        return runCatching {
            db.collection("teams").document(teamId)
                .update("tags", FieldValue.arrayUnion(tag))
                .await()
            teamId
        }
    }

    override suspend fun removeTagFromTeam(teamId: String, tag: String): Result<String> {
        return runCatching {
            db.collection("teams").document(teamId)
                .update("tags", FieldValue.arrayRemove(tag))
                .await()
            teamId
        }
    }

    override suspend fun changeUserRole(teamId: String, userId: String, role: UserRole): Result<String> {
        return runCatching {
            db.collection("teams").document(teamId).collection("members").document(userId)
                .update("role", role)
                .await()
            teamId
        }
    }


    private fun applyFilter(
        list: List<TeamData>,
        filters: TeamFilters
    ): List<TeamData> {
        var res = list.filter {
            filters.searchTerm.isBlank() ||
                    (it.name.lowercase().contains(filters.searchTerm) ||
                            it.members.any { a ->
                                a.name.lowercase().contains(filters.searchTerm) ||
                                        a.surname.lowercase().contains(filters.searchTerm) ||
                                        a.userName.lowercase().contains(filters.searchTerm)
                            } ||
                            it.category.lowercase().contains(filters.searchTerm) ||
                            it.description.lowercase().contains(filters.searchTerm)
                            )
        }
            .filter {
                (filters.categories.isEmpty() || filters.categories.contains(it.category)) &&
                        it.creationDate >= (filters.startDate
                    ?: LocalDate.MIN) && it.creationDate <= (filters.endDate
                    ?: LocalDate.MAX)
            }.sortedBy {
                when (filters.orderField) {
                    OrderField.NAME -> it.name
                    OrderField.DATE -> it.creationDate
                }.toString()
            }


        if (filters.order == Order.DESC) {
            res = res.reversed()
        }
        return res
    }

}
