package it.polito.mad.g18.mad_lab5

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.polito.mad.g18.mad_lab5.firebaseRepositories.ChatRepositoryImpl
import it.polito.mad.g18.mad_lab5.firebaseRepositories.TaskRepositoryImpl
import it.polito.mad.g18.mad_lab5.firebaseRepositories.TeamRepositoryImpl
import it.polito.mad.g18.mad_lab5.firebaseRepositories.UserRepositoryImpl
import it.polito.mad.g18.mad_lab5.gui.ThemePreferences
import it.polito.mad.g18.mad_lab5.repositories.ChatRepository
import it.polito.mad.g18.mad_lab5.repositories.TaskRepository
import it.polito.mad.g18.mad_lab5.repositories.TeamRepository
import it.polito.mad.g18.mad_lab5.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Named
import javax.inject.Singleton

@HiltAndroidApp
class BeGroupApplication : Application() {
    /*    init {
            FirebaseApp.initializeApp(applicationContext)
        }*/

}

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideMainModel(db: FirebaseFirestore): MainModel {
        return MainModel(db)
    }

    @Provides
    @Singleton
    fun provideUserRepository(db: FirebaseFirestore, storage: FirebaseStorage): UserRepository = UserRepositoryImpl(db, storage)

    @Provides
    @Singleton
    fun provideChatRepository(db: FirebaseFirestore, storage: FirebaseStorage): ChatRepository = ChatRepositoryImpl(db, storage)

    @Provides
    @Singleton
    fun provideTaskRepository(
        db: FirebaseFirestore,
        @Named("userMe") userMeFlow: StateFlow<UserData?>
    ): TaskRepository = TaskRepositoryImpl(db, userMeFlow)

    @Provides
    @Singleton
    fun provideTeamRepository(
        db: FirebaseFirestore,
        storage: FirebaseStorage,
        taskRepository: TaskRepository,
        @Named("userMe") userMeFlow: StateFlow<UserData?>
    ): TeamRepository = TeamRepositoryImpl(db, storage,userMeFlow, taskRepository)

    @Provides
    @Singleton
    fun provideThemePreferences(@ApplicationContext context: Context): ThemePreferences {
        return ThemePreferences(context)
    }
    

    @Provides
    @Singleton
    @Named("userMe")
    fun provideLoggedUserFlow(
        userRepository: UserRepository,
    ): StateFlow<UserData?> {

        Firebase.auth.addAuthStateListener {
            if (Firebase.auth.currentUser != null) {
                Firebase.firestore.enableNetwork() // user is logged in ? Connection to db enabled
            } else {
                Firebase.firestore.disableNetwork() // user is not logged in ? Connection to db disabled
            }
        }
        return userRepository.getUserMe().stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.Lazily,
            initialValue = null
        )

    }

    @Provides
    @Singleton
    fun provideToastManager(@ApplicationContext context: Context): ToastManager {
        return ToastManager(context)
    }

}

@Module
@InstallIn(SingletonComponent::class)
class FirebaseModule {

    @Provides
    @Singleton
    fun provideFireStoreInstance(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideFireStoreStorageInstance(): FirebaseStorage {
        return Firebase.storage
    }

}
