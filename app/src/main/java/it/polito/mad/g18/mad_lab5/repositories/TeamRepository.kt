package it.polito.mad.g18.mad_lab5.repositories

import android.graphics.Bitmap
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.UserTeamRequestData
import it.polito.mad.g18.mad_lab5.viewModels.TeamFilters
import kotlinx.coroutines.flow.Flow

interface TeamRepository {
    fun getTeam(id: String): Flow<TeamData>
    suspend fun createTeam(team: TeamData): Result<String>
    suspend fun updateTeam(team: TeamData): Result<String>
    suspend fun setTeamPfp(teamId: String, pfp: Bitmap): Result<String>
    suspend fun deleteTeamPfp(teamId: String): Result<String>
    suspend fun deleteTeam(id: String): Result<Void>
    suspend fun addTeamMember(
        teamId: String,
        user: UserTeamRequestData,
        role: UserRole
    ): Result<String>

    suspend fun removeTeamMember(teamId: String, userId: String): Result<String>
    suspend fun addTeamMemberRequest(teamId: String, user: UserTeamRequestData): Result<String>
    suspend fun rejectTeamMemberRequest(teamId: String, userId: String): Result<String>
    fun getFilteredTeams(filters: TeamFilters = TeamFilters()): Flow<List<TeamData>>
    fun getTeamMembers(id: String): Flow<List<UserTeamData>>
    fun getTeamRequests(id: String): Flow<List<UserTeamRequestData>>
    suspend fun addTagToTeam(teamId: String, tag: String): Result<String>
    suspend fun removeTagFromTeam(teamId: String, tag: String): Result<String>
    fun getCategories(): Flow<List<String>>
    suspend fun changeUserRole(teamId: String, userId: String, role: UserRole): Result<String>
    suspend fun getUserMeRole(teamId: String): Flow<UserRole>
}