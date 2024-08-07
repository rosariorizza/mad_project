package it.polito.mad.g18.mad_lab5.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.UserRole
import it.polito.mad.g18.mad_lab5.UserTeamRequestData
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class AddTeamUserListViewModel @Inject constructor(
    @Named("userMe") val userMe: StateFlow<UserData?>,
    val userRepository: UserRepository,
    val teamRepository: TeamRepository
) : ViewModel() {

    val user = userMe.value ?: UserData()

    // search value
    var searchKey by mutableStateOf("")
    fun setSearchKeyValue(s: String) {
        searchKey = s
    }

    // current members
    val currentMembers = teamRepository::getTeamMembers

    // found users
    val allUsers = userRepository.getOtherUsers(user.id)

    private val _foundUsers = MutableStateFlow<List<UserData>>( emptyList())
    val foundUsers: StateFlow<List<UserData>> = _foundUsers

    fun filterUsers(searchTerm: String, users: List<UserData>) {
        _foundUsers.value = users.filter {
            searchTerm.isNotBlank() &&
            it.userName.contains(searchTerm.lowercase()) &&
            !newMembers.contains(it)
        }
    }

    // new members
    val newMembers = mutableStateListOf<UserData>()

    fun addNewMember(u: UserData) {
        if(!newMembers.contains(u)) {
            newMembers.add(u)
            _foundUsers.value -= u
        }
    }
    fun removeNewMember(u: UserData) {
        newMembers.remove(u)
        _foundUsers.value += u
    }

    fun clear() {
        filterUsers("", emptyList())
        setSearchKeyValue("")
    }
    fun save(teamId: String, back: () -> Unit ) {
        var flag = true
        viewModelScope.launch(Dispatchers.IO) {
            newMembers.forEach {
                val user = UserTeamRequestData(
                    id = it.id,
                    name = it.name,
                    surname = it.surname,
                    userName = it.userName
                )
                teamRepository.addTeamMember(teamId,user,UserRole.VIEWER).onFailure {
                    flag = false
                }
            }
        }
        if(flag) { back() }
    }
}