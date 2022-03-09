package gcore.videocalls.demo.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

open class BaseViewModel : ViewModel() {

    private val viewModelJob = Job()

    protected val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    protected val errorMessage = SingleLiveEvent<Any>()

    protected val message = SingleLiveEvent<Any>()

    private val closeKeyboard = SingleLiveEvent<Unit>()

    private val progressBarVisibility = MutableLiveData<Boolean>()
    private val refreshProgressBarVisibility = MutableLiveData<Boolean>()

    fun getErrorMessage(): LiveData<Any?> = errorMessage

    fun getMessage(): LiveData<Any?> = message

    fun getProgressBarVisibility(): LiveData<Boolean> = progressBarVisibility

    fun getLisRefreshProgressBarVisibility(): LiveData<Boolean> = refreshProgressBarVisibility

    fun getCloseKeyboard(): LiveData<Unit?> = closeKeyboard

    protected fun closeKeyboard() {
        closeKeyboard.value = Unit
    }

    protected fun showProgressBar() {
        progressBarVisibility.value = true
    }

    protected fun hideProgressBar() {
        progressBarVisibility.value = false
    }
}