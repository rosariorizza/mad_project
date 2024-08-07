package it.polito.mad.g18.mad_lab5.repositories

import it.polito.mad.g18.mad_lab5.Message
import it.polito.mad.g18.mad_lab5.TaskData
import it.polito.mad.g18.mad_lab5.viewModels.TaskFilters
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTask(id: String): Flow<TaskData>
    fun getFilteredTasks(filters: TaskFilters):Flow<Map<String, List<TaskData>>>
    fun getCategories(teamId: String?): Flow<List<String>>
    suspend fun createTask(task: TaskData, numRepetitions: Int): Result<String>
    suspend fun updateTask(task: TaskData): Result<String>
    suspend fun deleteTask(id: String): Result<String>
    fun getHistory(taskId: String): Flow<List<Message>>

}