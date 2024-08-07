package it.polito.mad.g18.mad_lab5.viewModels

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.TeamData
import it.polito.mad.g18.mad_lab5.TeamModel
import it.polito.mad.g18.mad_lab5.ToastManager
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamData
import it.polito.mad.g18.mad_lab5.UserTeamRequestData
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.reflect.Member
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class TeamViewModel @Inject constructor(
    val model: MainModel,
    private val taskRepository: TaskRepository,
    private val teamRepository: TeamRepository,
    private val toastManager: ToastManager,
    @Named("userMe") val userMe: StateFlow<UserData?>
) : ViewModel() {

    // id del team, proveniente dalla navigazione, usato per il team details pane
    private val _teamDetailId = MutableStateFlow<String?>(null)
    fun setTeamDetail(id: String?) {
        _teamDetailId.value = id
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val team: StateFlow<TeamData> = _teamDetailId
        .flatMapLatest { id ->
            if (id != null) {
                withContext(Dispatchers.IO) {
                    teamRepository.getTeam(id)
                }
            } else flowOf(TeamData())
        }.stateIn(viewModelScope, SharingStarted.Lazily, TeamData())


    @OptIn(ExperimentalCoroutinesApi::class)
    val members: StateFlow<List<UserTeamData>> = _teamDetailId
        .flatMapLatest { id ->
            if (id != null) withContext(Dispatchers.IO) {
                teamRepository.getTeamMembers(id)
            }
            else flowOf(listOf<UserTeamData>())
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf<UserTeamData>())

    @OptIn(ExperimentalCoroutinesApi::class)
    val requests: StateFlow<List<UserTeamRequestData>> = _teamDetailId
        .flatMapLatest { id ->
            if (id != null) withContext(Dispatchers.IO) {
                teamRepository.getTeamRequests(id)
            }
            else flowOf(listOf<UserTeamRequestData>())
        }.stateIn(viewModelScope, SharingStarted.Lazily, listOf<UserTeamRequestData>())


    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks: StateFlow<Map<String, List<TaskData>>> = _teamDetailId
        .flatMapLatest { teamId ->
            if (teamId != null) {
                withContext(Dispatchers.IO) {
                    taskRepository.getFilteredTasks(TaskFilters(teams = listOf(teamId)))
                }
            } else {
                flowOf(emptyMap())
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())


    @OptIn(ExperimentalCoroutinesApi::class)
    val role: StateFlow<UserRole> = _teamDetailId
        .flatMapLatest { id ->
            if (id != null) {
                withContext(Dispatchers.IO) {
                    teamRepository.getUserMeRole(id)

                }
            } else flowOf(UserRole.VIEWER)
        }.stateIn(viewModelScope, SharingStarted.Lazily, UserRole.VIEWER)
    //val tasks = taskRepository.getFilteredTasks(TaskFilters(teams = listOf(team.value.id)))


    var isClosingWithoutSaving by mutableStateOf(false)
        private set

    fun setIsClosingWithoutSaving(v: Boolean) {
        isClosingWithoutSaving = v
    }

    var isLeavingTeam by mutableStateOf(false)
        private set

    fun setIsLeavingTeam(v: Boolean) {
        isLeavingTeam = v
    }

    var isDeleteTeam by mutableStateOf(false)
        private set

    fun setIsDeleteTeam(v: Boolean) {
        isDeleteTeam = v
    }

    //### SINGLE TEAM STATES W/ ERRORS

    var name = mutableStateOf(value = "")
        private set

    fun setName(v: String) {
        name.value = v
    }

    var nameError = mutableStateOf(value = "")
        private set


    var profilePicture = mutableStateOf<String>(value = "") //team.profilePicture
        private set

    fun setProfilePicture(teamId: String, pfp: Bitmap?) {
        if (pfp != null) {
            viewModelScope.launch(Dispatchers.IO) {
                teamRepository.setTeamPfp(teamId, pfp)
            }
        }
    }

    fun setProfilePictureValue(pfp: String) {
        profilePicture.value = pfp
    }

    fun deleteProfilePicture(teamId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            teamRepository.deleteTeamPfp(teamId)
        }
    }


    var description = mutableStateOf(value = "")
        private set

    fun setDescription(v: String) {
        description.value = v
    }

    var creationDate = mutableStateOf(value = LocalDate.now())
        private set

    fun setCreationDate(v: LocalDate) {
        creationDate.value = v
    }

    var creationDateError = mutableStateOf(value = "")
        private set


    var category = mutableStateOf(value = "")
        private set

    fun setCategory(v: String) {
        category.value = v
    }

    var categoryError = mutableStateOf(value = "")
        private set

    var tags = mutableStateOf(value = listOf<String>())
        private set

    fun setTags(t: List<String>) {
        tags.value = t
    }

    fun addTag(teamId: String?, tag: String?) {
        viewModelScope.launch {
            if (teamId != null && tag != null) {
                val result = teamRepository.addTagToTeam(teamId, tag)
                result.onSuccess {
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }


    fun removeTag(teamId: String?, tag: String?) {
        viewModelScope.launch {
            if (teamId != null && tag != null) {
                val result = teamRepository.removeTagFromTeam(teamId, tag)
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }


    /*
    fun addTag(t: String) {
        tags.value += t
    }

    fun removeTag(t: String) {
        tags.value -= t
    }
    */

    //region TO SEE
    // TODO??? salvare i tag quando si esce dallo tagsScreen
    fun saveTag(navigate: () -> Unit) {
        if (validate()) {
            clear()
            viewModelScope.launch {
                val result = teamRepository.updateTeam(
                    getTeamData(id = oldTeam.value.id, chatId = oldTeam.value.chatId)
                )
                result.onSuccess {
                    navigate()
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }
    //endregion

    var achievements = mutableStateOf(value = listOf<Int>())
        private set

    fun setAchievements(t: List<Int>) {
        achievements.value = t
    }

    fun addAchievements(t: Int) {
        achievements.value += t
    }

    fun removeAchievements(t: Int) {
        achievements.value -= t
    }

    var chatId = mutableStateOf(value = "")
        private set

    fun setChatId(v: String) {
        chatId.value = v
    }


    /*var members = mutableStateOf(value = listOf<UserTeamData>())
        private set

    private fun setMembers(a: List<UserTeamData>) {
        members.value = a
    }

    fun addMember(a: UserTeamData) {
        members.value += a
    }

    fun removeMember(a: UserTeamData) {
        members.value -= a

    }*/

    //NON DOVREBBE SERVIRE: IN AUTOMATICO C'È LO USER_ME
    var membersError = mutableStateOf(value = "")
        private set


    //### CHECK E VALIDATION

    fun validate(): Boolean {
        checkName()
        checkCategory()

        if (nameError.value.isBlank() && categoryError.value.isBlank()) {
            return true
        }
        return false
    }

    private fun checkName() {
        nameError.value = if (name.value.isBlank()) "Team name cannot be blank" else ""
    }

    private fun checkCategory() {
        categoryError.value = if (category.value.isBlank()) "Category cannot be blank" else ""
    }

    private fun clear() {
        nameError.value = ""
        categoryError.value = ""
    }

    ///### OTHER FUN

    fun saveEdit(navigate: () -> Unit) {
        if (validate()) {
            clear()
            viewModelScope.launch {
                val result = teamRepository.updateTeam(
                    getTeamData(id = oldTeam.value.id, chatId = oldTeam.value.chatId)
                )
                result.onSuccess {
                    navigate()
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    fun saveNew(navigate: (String) -> Unit) {
        if (validate()) {
            viewModelScope.launch {
                val result = teamRepository.createTeam(
                    getTeamData()
                )
                result.onSuccess {
                    clear()
                    navigate(it)
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    private fun hasTeamChanged(): Boolean {
        if (oldTeam.value.id.isBlank()) {
            return getTeamData() != TeamData(
                creationDate = LocalDate.now(),
            )
        }
        return getTeamData() != oldTeam.value
    }

    fun back(navigate: () -> Unit) {
        clear()
        if (hasTeamChanged()) {
            isClosingWithoutSaving = true
        } else navigate()
    }

    private fun getTeamData(
        id: String? = null,
        name: String? = null,
        description: String? = null,
        category: String? = null,
        creationDate: LocalDate? = null,
        chatId: String? = null,
        achievements: List<Int>? = null,
        tags: List<String>? = null,
    ): TeamData {
        return TeamData(
            id = id ?: oldTeam.value.id,
            name = name ?: this.name.value,
            description = description ?: this.description.value,
            category = category ?: this.category.value,
            creationDate = creationDate ?: this.creationDate.value,
            chatId = chatId ?: this.chatId.value,
            achievements = achievements ?: this.achievements.value,
            tags = tags ?: this.tags.value,

            )
    }


    fun setEditTeam(teamId: String) {
        viewModelScope.launch {
            teamRepository.getTeam(teamId).collect { team ->
                setName(team.name)
                setDescription(team.description)
                setProfilePictureValue(team.profilePicture)
                setCategory(team.category)
                setCreationDate(team.creationDate)
                setChatId(team.chatId)
                setAchievements(team.achievements)
                setTags(team.tags)
                setOldTeam(team)

            }
        }
    }

    // stato necessario per controllare eventuali cambiamenti del team modificato
    private var oldTeam = mutableStateOf(value = TeamData())
        private set

    private fun setOldTeam(v: TeamData) {
        oldTeam.value = v

    }

    fun deleteTeam(v: String) {
        model.deleteTeam(v)
    }

    //passo l'id del team
    /*fun leaveTeam() {
        model.leaveTeam()
    }*/

    fun rejectRequest(teamId: String?, userId: String?) {
        viewModelScope.launch {
            if (teamId != null && userId != null) {
                val result = teamRepository.rejectTeamMemberRequest(teamId, userId)
                result.onSuccess {
                    toastManager.showToast("User request removed :)")
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    //accetto la richiesta e aggiungo il membro
    fun acceptRequest(teamId: String?, user: UserTeamRequestData?, role: UserRole?) {
        viewModelScope.launch {
            if (teamId != null && user != null && role != null) {
                val result = teamRepository.addTeamMember(teamId, user, role)
                result.onSuccess {
                    toastManager.showToast("Member added :)")
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }


    /*

        fun addAchievement(v: Int) {
            team.addAchievement(v)
        }

        fun removeAchievement(t: Int) {
            team.removeAchievement(t)
        }



        fun addMember(t: UserTeamData) {
            team.addMember(t)
        }


        var memberToRemove: UserTeamData? = mutableStateOf(null)
            //private set

        fun removeMemberAfterOk(v: UserTeamData?) {
            memberToRemove = v
        }

        fun removeMember(t: UserTeamData) {
            model.removeMember(t)
        }



        //passo l'id del team
        fun deleteTeam(v: String) {
            model.deleteTeam(v)
        }

        //passo l'id del team
        fun leaveTeam() {
            model.leaveTeam()
        }


        fun acceptRequest(t: UserTeamRequestData, r: UserRole) {
            model.addRequestMember(t, r)
        }

        fun rejectRequest(t: UserTeamRequestData) {
            model.rejectRequestMember(t)
        }

        fun addNewTeam() {
            val newTeam = TeamModel(/*TODO*/
            )
            model.addTeam()
        }

        fun closeEditing() {/*TODO*/
        }

        fun saveEdit(navigate: () -> Unit) {
            if (validate()) {
                clear()
                model.editTeam()
                navigate()
            }
        }

        fun saveNew(navigate: (String) -> Unit) {
            if (validate()) {
                clear()
                navigate(model.addTeam())
            }
        }



        fun showBadge(chatId: String): Boolean {
            return model.hasUnreadMessages(chatId)
        }

        fun getDirectChat(userId: (String)): String?{
            return model.getDirectChat(userId)
        }

    */

    //### PER FAR FUNZIONARE IL COMPONENT

    var memberToRemove: UserTeamData? by mutableStateOf(null)
    //private set

    fun removeMemberAfterOk(v: UserTeamData?) {
        memberToRemove = v
    }

    fun removeMember(teamId: String?, userId: String?) {
        viewModelScope.launch {
            if (teamId != null && userId != null) {
                val result = teamRepository.removeTeamMember(teamId, userId)
                result.onSuccess {
                    toastManager.showToast("Member removed :(")
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    fun leaveTeam(teamId: String?, userId: String?) {
        viewModelScope.launch {
            if (teamId != null && userId != null) {
                val result = teamRepository.removeTeamMember(teamId, userId)
                result.onSuccess {
                    toastManager.showToast("You left the team :(")
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }


    fun showBadge(chatId: String): Boolean {
        //return model.hasUnreadMessages(chatId)
        return false
    }

    fun getDirectChat(userId: (String)): String? {
        //return model.getDirectChat(userId)
        return "suca"
    }

    /*    fun acceptRequest(t: UserTeamRequestData, r: UserRole) {
            model.addRequestMember(t, r)
        }

        fun rejectRequest(t: UserTeamRequestData) {
            model.rejectRequestMember(t)
        }*/

    fun addTeamRequest(teamId: String?, user: UserTeamRequestData, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (teamId != null) {
                val result = teamRepository.addTeamMemberRequest(teamId, user)
                result.onSuccess {
                    onSuccess()
                    toastManager.showToast("Join request sent :)")
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }

    fun changeRole(teamId: String, userId: String, role: UserRole) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {teamRepository.changeUserRole(teamId, userId, role)}
            result.onFailure {
                toastManager.showToast(it.message.toString())
            }
        }
    }

}

