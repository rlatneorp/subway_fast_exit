package com.rlatneorp.fast_subway_exit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rlatneorp.fast_subway_exit.model.ElevatorStatus
import com.rlatneorp.fast_subway_exit.model.StationRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StationRepository = StationRepository(application)
    private val _elevatorInfo = MutableLiveData<ElevatorStatus>()
    val elevatorInfo: LiveData<ElevatorStatus> = _elevatorInfo
    private val _currentLocationName = MutableLiveData<String>("현재위치")
    val currentLocationName: LiveData<String> = _currentLocationName
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage
    private val _navigateToEmail = MutableLiveData<Event<Unit>>()
    val navigateToEmail: LiveData<Event<Unit>> = _navigateToEmail

    fun fetchInfoForCurrentLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getElevatorInfoForCurrentLocation()
            handleFetchResult(result, isLocationBased = true)
        }
    }

    fun searchStation(stationName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.searchElevatorInfoByStation(stationName)
            handleFetchResult(result, isLocationBased = false)
        }
    }

    fun onInquiryClicked() {
        _navigateToEmail.value = Event(Unit)
    }
    private fun handleFetchResult(result: Result<ElevatorStatus>, isLocationBased: Boolean) {
        result.fold(
            onSuccess = { status ->
                updateStateOnSuccess(status)
            },
            onFailure = { e ->
                updateStateOnFailure(e, isLocationBased)
            }
        )
        _isLoading.value = false
    }

    private fun updateStateOnSuccess(status: ElevatorStatus) {
        _elevatorInfo.value = status
        _currentLocationName.value = status.stationName
    }

    private fun updateStateOnFailure(e: IllegalAccessException, isLocationBased: Boolean) {
        var errorMessage = "검색 실패: ${e.message}"
        if (isLocationBased) {
            errorMessage = "위치 또는 승강기 정보를 가져오는데 실패했습니다: ${e.message}"
        }
        _errorMessage.value = Event(errorMessage)
        if (isLocationBased) {
            _currentLocationName.value = "위치 정보 없음"
        }
    }
}