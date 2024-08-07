package it.polito.mad.g18.mad_lab5.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.Attachment
import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.ToastManager
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.ChatRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatViewModel @Inject constructor(
    val chatRepository: ChatRepository,
    val teamRepository: TeamRepository,
    val toastManager: ToastManager,
    @Named("userMe") val userMe: StateFlow<UserData?>
) : ViewModel() {

    val user = userMe.value ?: UserData()

    val currentChat = chatRepository::getCurrentChat

    val members = teamRepository::getTeamMembers

    val messageList = chatRepository::getMessages

    val memberList = teamRepository::getTeamMembers

    var msgDraft by mutableStateOf("")
        private set

    fun setMsgDraftValue(s: String) {
        msgDraft = s
    }

    fun sendMessage(
        chatId: String, m: Message,
        userId: String,
        teamId: String,
        taskId: String
    ) {
        if (msgDraft.isNotBlank()) {
            setMsgDraftValue("")
            viewModelScope.launch(Dispatchers.IO) {
                val res = chatRepository.sendMessage(chatId, m, userMe.value?.id?:"",  userId, teamId, taskId)
                res.onSuccess { }
                res.onFailure { "Send message failed" }
            }
        }
    }


    fun setLastMessageRead(chatId: String, m: Message?) {
        if (m != null) {
            viewModelScope.launch(Dispatchers.IO) {

                chatRepository.setChatRead(user.id, chatId, m)
            }
        }
    }

}