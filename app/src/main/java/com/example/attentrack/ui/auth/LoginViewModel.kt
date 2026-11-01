package com.example.attentrack.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attentrack.data.ApiResult
import com.example.attentrack.data.AuthRepository
import com.example.attentrack.ui.Event
import kotlinx.coroutines.launch

data class LoginUiState(val loading: Boolean = false, val error: String? = null)

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableLiveData(LoginUiState())
    val state: LiveData<LoginUiState> = _state

    private val _success = MutableLiveData<Event<Unit>>()
    val success: LiveData<Event<Unit>> = _success

    fun login(userId: String, password: String) {
        if (_state.value?.loading == true) return
        if (userId.isBlank() || password.isEmpty()) {
            _state.value = LoginUiState(error = "Enter your ID and password.")
            return
        }
        _state.value = LoginUiState(loading = true)
        viewModelScope.launch {
            when (val r = repo.login(userId.trim(), password)) {
                is ApiResult.Failure -> _state.value = LoginUiState(error = r.message)
                is ApiResult.Success -> {
                    _state.value = LoginUiState()
                    _success.value = Event(Unit)
                }
            }
        }
    }
}
