package com.pilarkreasi.pillarpos.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pilarkreasi.pillarpos.data.model.Role
import com.pilarkreasi.pillarpos.data.repository.AuthRepository
import com.pilarkreasi.pillarpos.data.repository.LoginResult
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    

    fun login(username: String, password: String, peran: Role) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(username, password, peran)
            _loginResult.value = result
            _isLoading.value = false
        }
    }
}
