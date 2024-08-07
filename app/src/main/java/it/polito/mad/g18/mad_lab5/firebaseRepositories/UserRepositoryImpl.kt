package it.polito.mad.g18.mad_lab5.firebaseRepositories

import android.util.Log
import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class UserRepositoryImpl(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
) : UserRepository {

    private val COLLECTION = "users"
    private val storageRef = storage.reference
    private val usersStorageRef = storageRef.child("users")

    private val firebaseUserUidStateFlow = MutableStateFlow<String?>(null)
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        firebaseUserUidStateFlow.value = auth.currentUser?.uid
    }
    init {
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserMe(): Flow<UserData?> {
        return firebaseUserUidStateFlow.flatMapLatest { firebaseUserUid ->
            if (firebaseUserUid != null) {
                getUser(firebaseUserUid)
            } else {
                flowOf(null)
            }
        }
    }

    override fun getUser(id: String): Flow<UserData?> = callbackFlow {
        val listenerRegistration = db.collection(COLLECTION).document(id)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }

                // Ottieni l'email dell'utente corrente autenticato con Firebase
                val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

                val userData = snapshot?.let {
                    UserData(
                        id = it.reference.id,
                        name = it.getString("name").orEmpty(),
                        surname = it.getString("surname").orEmpty(),
                        userName = it.getString("userName").orEmpty(),
                        profilePicture = it.getString("pfp").orEmpty(),
                        phoneNumber = it.getString("phoneNumber").orEmpty(),
                        description = it.getString("description").orEmpty(),
                        location = it.getString("location").orEmpty(),
                        birthDate = it.getString("birthDate")?.let { d ->
                            try {
                                LocalDate.parse(d)
                            } catch (e: DateTimeParseException) {
                                null
                            }

                        } ?: LocalDate.now(),
                        email = currentUserEmail,
                        teams = (it["teams"] as? List<String>)?.toMutableList() ?: mutableListOf(),
                    )
                }
                trySend(userData)
            }

        awaitClose { listenerRegistration.remove() }

    }

    override fun getFilteredUsers(): Flow<List<UserData>> {
        TODO("Not yet implemented")
    }

    override suspend fun register(user: UserData): Result<String> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        val firebaseUserData = mapOf(
            "name" to user.name,
            "surname" to user.surname,
            "userName" to user.userName,
            "birthDate" to user.birthDate.toString(),
            "phoneNumber" to user.phoneNumber,
            "description" to user.description,
            "teams" to emptyList<Any>(),
            "chats" to emptyList<Any>(),
        )
        return runCatching {
            db.collection(COLLECTION).document(firebaseUserUid)
                .set(firebaseUserData).await()
            firebaseUserUid
        }
    }

    override suspend fun updateUser(user: UserData): Result<String> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))
        val oldUser = getUserMe().first()
        val firebaseUserData = mapOf(
            "name" to user.name,
            "surname" to user.surname,
            "userName" to user.userName,
            "birthDate" to user.birthDate.toString(),
            "phoneNumber" to user.phoneNumber,
            "description" to user.description
        )

        return runCatching {
            val chats = db.collection("users")
                .document(user.id).collection("chats").get().await()

            db.runTransaction { transaction ->
                val userDocument = db.collection(COLLECTION).document(firebaseUserUid)

                oldUser?.teams?.map {
                    db.collection("teams").document(it).collection("members").document(user.id)
                }?.forEach {
                    transaction.update(
                        it, mapOf(
                            "name" to user.name,
                            "surname" to user.surname,
                            "userName" to user.userName,
                        )
                    )
                }

                chats.forEach {
                    val otherUserId = it.getString("userId").orEmpty()
                    if(otherUserId.isNotBlank()) {
                        val otherUserChatDocument = db.collection("users").document(otherUserId)
                            .collection("chats").document(it.id)
                        transaction.update(
                            otherUserChatDocument, mapOf(
                                "name" to user.name,
                                "surname" to user.surname,
                                "userName" to user.userName,
                            )
                        )
                    }
                }


                transaction.update(userDocument, firebaseUserData)

                firebaseUserUid
            }.await()
        }
    }

    // ok
    override suspend fun setUserPfp(userId: String, pfp: Bitmap): Result<String> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        val thisUserStorageRef = usersStorageRef.child(userId)
        val pfpName = "BEGROUP_IMAGE_" + LocalDateTime.now().toString() + ".jpg"
        val pfpStorageRef = thisUserStorageRef.child(pfpName)
        val baos = ByteArrayOutputStream()
        pfp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()

        val userIds = getOtherUsers(userId).first().map { x -> x.id }
        var userChatRefs = emptyList<DocumentReference>()
        userIds.forEach { uId -> // per ogni utente trovato
            db.collection(COLLECTION) // chat dell'utente
                .document(uId)
                .collection("chats")
                .whereEqualTo("userId",userId)
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
                    if (!task.isSuccessful) {
                        Log.d("STORAGE", "Upload failed")
                    }
                    pfpStorageRef.downloadUrl
                }.onSuccessTask { uri ->

                    db.runTransaction { t ->

                        // update user pfp
                        val userDocRef = db.collection(COLLECTION)
                            .document(userId)
                        t.update(userDocRef, "pfp", uri)

                        // update other chats where the user is present
                        userChatRefs.forEach {
                            t.update(it,"pfp",uri)
                        }

                    }
                }
            urlTask.await().toString()
        }
    }

    // ok
    override suspend fun deleteUserPfp(userId: String): Result<String> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        val userIds = getOtherUsers(userId).first().map { x -> x.id }
        var userChatRefs = emptyList<DocumentReference>()
        userIds.forEach { uId -> // per ogni utente trovato
            db.collection(COLLECTION) // chat dell'utente
                .document(uId)
                .collection("chats")
                .whereEqualTo("userId",userId)
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
            db.runTransaction { t ->

                val uri = ""

                // update user pfp
                val userDocRef = db.collection(COLLECTION)
                    .document(userId)
                t.update(userDocRef, "pfp", uri)

                // update other chats where the user is present
                userChatRefs.forEach {
                    t.update(it,"pfp",uri)
                }

            }
            "Operation sent successfully"
        }
    }

    override suspend fun addTeamToUser(teamId: String): Result<Void> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        return runCatching {
            db.collection(COLLECTION).document(firebaseUserUid)
                .update("users", FieldValue.arrayUnion(teamId)).await()
        }
    }

    override suspend fun removeTeamToUser(teamId: String): Result<Void> {
        val firebaseUserUid =
            Firebase.auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        return runCatching {
            db.collection(COLLECTION).document(firebaseUserUid)
                .update("users", FieldValue.arrayRemove(teamId)).await()
        }
    }

    override suspend fun deleteUser(id: String): Result<Void> {
        val firebaseUser =
            Firebase.auth.currentUser ?: return Result.failure(Exception("User not logged in"))
        return runCatching {
            // dovrebbe essere gestita la transazione e la gestione delle cascate
            db.collection(COLLECTION).document(firebaseUser.uid).delete().await()
            firebaseUser.delete().await()
        }
    }

    override suspend fun isUsernameTaken(username: String): Boolean {
        val usersCollection = db.collection("users")

        return try {
            val querySnapshot = usersCollection.whereEqualTo("userName", username).get().await()
            //Log.d("USERNAME QUERY", (!querySnapshot.isEmpty).toString())
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun checkIfUserRegistered(
        userId: String,
        onTrue: () -> Unit,
        onFalse: () -> Unit,
    ) {

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User is registered
                    onTrue()
                } else {
                    // User is not registered
                    onFalse()
                }
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error checking user registration", it)
                // Handle the error appropriately
            }
    }

    override fun getOtherUsers(userMeId: String): Flow<List<UserData>> = callbackFlow {

        val listener = db.collection(COLLECTION)
            .whereNotEqualTo(FieldPath.documentId(), userMeId)
            .addSnapshotListener { res, err ->
                if (err != null) {
                    close(err)
                    return@addSnapshotListener
                }

                val users = res?.documents
                    ?.map { doc ->
                        UserData(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            surname = doc.getString("surname") ?: "",
                            userName = doc.getString("userName") ?: "",
                            profilePicture = doc.getString("pfp") ?: ""
                        )
                    }
                if (users != null) {
                    trySend(users)
                }
            }

        awaitClose { listener.remove() }
    }

}

