package org.bangkit.kiddos_android.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bangkit.kiddos_android.data.remote.response.ErrorResponse
import org.bangkit.kiddos_android.data.remote.response.LoginResponse
import org.bangkit.kiddos_android.data.repository.AuthRepository
import org.bangkit.kiddos_android.utils.Event
import retrofit2.HttpException

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    // Membuat token sebagai LiveData<Event<String>>
    private val _token = MutableLiveData<Event<String>>()
    val token: LiveData<Event<String>> = _token

    init {
        // Inisialisasi token dari repository dan bungkus dalam Event
        viewModelScope.launch {
            val savedToken = repository.getToken().first() // Using first to get initial token
            _token.value = Event(savedToken)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.login(email, password)
                _loginResult.value = Result.success(response)

                // Membungkus token dalam Event dan memperbarui LiveData token
                if (!response.error) {
                    _token.value = Event(response.token)
                }

            } catch (e: HttpException) {
                val jsonInString = e.response()?.errorBody()?.string()
                val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
                val errorMessage = errorBody.message ?: "An error occurred"
                _loginResult.value = Result.failure(Exception(errorMessage))
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}



