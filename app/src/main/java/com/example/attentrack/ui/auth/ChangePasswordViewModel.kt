package com.example.attentrack.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attentrack.data.ApiResult
import com.example.attentrack.data.AuthRepository
import com.example.attentrack.ui.Event
import kotlinx.coroutines.launch

data class ChangePasswordUiState(val loading: Boolean = false, val error: String? = null)

class ChangePasswordViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableLiveData(ChangePasswordUiState())
    val state: LiveData<ChangePasswordUiState> = _state

    private val _done = MutableLiveData<Event<Unit>>()
    val done: LiveData<Event<Unit>> = _done

    fun submit(current: String, newPassword: String, confirm: String) {
        if (_state.value?.loading == true) return
        val problem = when {
            current.isEmpty() -> "Enter your current password."
            newPassword.length < 6 -> "The new password must be at least 6 characters."
            newPassword == current -> "The new password must be different from the current one."
            newPassword != confirm -> "The new passwords do not match."
            else -> null
        }
        if (problem != null) {
            _state.value = ChangePasswordUiState(error = problem)
            return
        }
        _state.value = ChangePasswordUiState(loading = true)
        viewModelScope.launch {
            when (val r = repo.changePassword(current, newPassword)) {
                is ApiResult.Failure -> _state.value = ChangePasswordUiState(error = r.message)
                is ApiResult.Success -> {
                    _state.value = ChangePasswordUiState()
                    _done.value = Event(Unit)
                }
            }
        }
    }
}
