package com.example.attentrack.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attentrack.data.ApiResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val unauthorized: Boolean = false) : UiState<Nothing>
}

fun <T> ApiResult<T>.toUiState(): UiState<T> = when (this) {
    is ApiResult.Success -> {
        val d = data
        if (d == null) UiState.Error("The server sent an unexpected response.") else UiState.Success(d)
    }
    is ApiResult.Failure -> UiState.Error(message, unauthorized = code == 401)
}

/** Ekbar-i handle korar moto event (Toast, navigation). */
class Event<out T>(private val content: T) {
    private var handled = false
    fun getIfNotHandled(): T? = if (handled) null else { handled = true; content }
}

/**
 * Shob "load kore dekhao" screen er jonno generic ViewModel.
 * option = semester id (jodi dorkar hoy), na hole null.
 */
class LoadViewModel<T>(private val loader: suspend (Int?) -> ApiResult<T>) : ViewModel() {

    private val _state = MutableLiveData<UiState<T>>(UiState.Loading)
    val state: LiveData<UiState<T>> = _state

    var option: Int? = null
        private set

    private var job: Job? = null

    fun load(showLoading: Boolean = true) {
        job?.cancel()
        if (showLoading) _state.value = UiState.Loading
        job = viewModelScope.launch { _state.value = loader(option).toUiState() }
    }

    fun select(value: Int?) {
        option = value
        load(true)
    }

    fun setInitialOption(value: Int?) {
        if (option == null) option = value
    }
}

/** Owner (Activity/Fragment) er scope e key diye ekta LoadViewModel dey; ekhane ekai key = ekai data. */
@Suppress("UNCHECKED_CAST")
fun <T> ViewModelStoreOwner.loader(key: String, load: suspend (Int?) -> ApiResult<T>): LoadViewModel<T> =
    ViewModelProvider(this, viewModelFactory { initializer { LoadViewModel(load) } })[key, LoadViewModel::class.java] as LoadViewModel<T>
