package it.polito.mad.g18.mad_lab5.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.g18.mad_lab5.MainModel
import it.polito.mad.g18.mad_lab5.UserData
import it.polito.mad.g18.mad_lab5.gui.ThemePreferences
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    fun toggleTheme() {
        viewModelScope.launch {
            val currentTheme = themePreferences.darkTheme.first()
            themePreferences.setDarkTheme(!currentTheme)
        }
    }

    val darkMode = themePreferences.darkTheme.stateIn(viewModelScope, SharingStarted.Lazily, false)

}