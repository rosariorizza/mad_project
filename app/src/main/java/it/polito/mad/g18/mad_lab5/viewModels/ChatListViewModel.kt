package it.polito.mad.g18.mad_lab5.viewModels

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.play.integrity.internal.c
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.AppModule
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.UserChatData
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.repositories.ChatRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ChatListViewModel @Inject constructor(
    val chatRepository: ChatRepository,
    @Named("userMe") val userMe: StateFlow<UserData?>
): ViewModel() {

    val user = userMe.value ?: UserData()

    val chats = chatRepository.getChats(user.id)

    val getChats = chatRepository::getChats
}