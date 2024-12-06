package org.bangkit.kiddos_android.ui.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.repository.ArticleRepository
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.domain.model.Data
import org.bangkit.kiddos_android.domain.model.User

class HomeViewModel(
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _articles = MutableLiveData<List<Data>>()
    val articles: LiveData<List<Data>> get() = _articles

    fun fetchUser(userId: String) {
        viewModelScope.launch {
            try {
                val userResponse = userRepository.getUser(userId)
                _user.value = userResponse[userId]
            } catch (e: Exception) {
                _user.value = null
                // Handle error
            }
        }
    }

    fun fetchArticles() {
        viewModelScope.launch {
            try {
                val articleResponse = articleRepository.getArticles()
                _articles.value = articleResponse
            } catch (e: Exception) {
                _articles.value = emptyList()
                // Handle error
            }
        }
    }
}
