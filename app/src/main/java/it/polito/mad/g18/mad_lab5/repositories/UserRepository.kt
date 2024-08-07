package it.polito.mad.g18.mad_lab5.repositories

import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseUser
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserMe(): Flow<UserData?>
    fun getUser(id: String): Flow<UserData?>
    fun getFilteredUsers(/*todo*/): Flow<List<UserData>>
    suspend fun register(user: UserData): Result<String>
    suspend fun updateUser(user: UserData): Result<String>
    suspend fun setUserPfp(userId: String, pfp: Bitmap): Result<String>
    suspend fun deleteUserPfp(userId: String): Result<String>
    suspend fun addTeamToUser(teamId: String): Result<Void>
    suspend fun removeTeamToUser(teamId: String): Result<Void>
    suspend fun deleteUser(id: String): Result<Void>
    fun getOtherUsers(userMeId: String): Flow<List<UserData>>
    suspend fun isUsernameTaken(username: String): Boolean
    fun checkIfUserRegistered(userId: String, onTrue: () -> Unit, onFalse: () -> Unit)
}