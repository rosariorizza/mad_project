package it.polito.mad.g18.mad_lab5.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.ChatData
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.ToastManager
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.ChatRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale.filter
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class DMUserListViewModel @Inject constructor(
    @Named("userMe") val userMe: StateFlow<UserData?>,
    val userRepository: UserRepository,
    val chatRepository: ChatRepository,
    val toastManager: ToastManager
) : ViewModel() {

    val user = userMe.value ?: UserData()

    // search value
    var searchKey by mutableStateOf("")
    fun setSearchKeyValue(s: String) {
        searchKey = s
    }

    // found users
    val allUsers = userRepository.getOtherUsers(user.id)

    private val _foundUsers = MutableStateFlow<List<UserData>>(emptyList())
    val foundUsers: StateFlow<List<UserData>> = _foundUsers

    fun filterUsers(searchTerm: String, users: List<UserData>) {
        _foundUsers.value = users.filter {
            searchTerm.isNotBlank() &&
                    it.userName.contains(searchTerm.lowercase()) &&
                    it.id != user.id
        }
    }

    fun clear() {
        filterUsers("", emptyList())
        setSearchKeyValue("")
    }

    fun startNewChat(user2: UserData, showChat: (String) -> Unit) {
        viewModelScope.launch {
            val chatId = chatRepository.getChats(userMe.value?.id?:"").first().find { it.userId == user2.id }
            if (chatId != null) {
                showChat(chatId.chatId)
            } else {
                val result = chatRepository.createDMChat(user, user2)
                result.onSuccess {
                    showChat(it)
                }
                result.onFailure {
                    toastManager.showToast(it.message.toString())
                }
            }
        }
    }
}