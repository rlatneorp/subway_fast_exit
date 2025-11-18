package com.rlatneorp.fast_subway_exit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rlatneorp.fast_subway_exit.model.ElevatorRow
import com.rlatneorp.fast_subway_exit.model.StationRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StationRepository = StationRepository(application)

    private val _elevatorInfo = MutableLiveData<List<ElevatorRow>>()
    val elevatorInfo: LiveData<List<ElevatorRow>> = _elevatorInfo

    private val _currentLocationName = MutableLiveData<String>("현재위치")
    val currentLocationName: LiveData<String> = _currentLocationName

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<Event<String>>()
    val errorMessage: LiveData<Event<String>> = _errorMessage

    private val _navigateToEmail = MutableLiveData<Event<Unit>>()
    val navigateToEmail: LiveData<Event<Unit>> = _navigateToEmail

    private val _allElevatorsWorking = MutableLiveData<Boolean>(false)
    val allElevatorsWorking: LiveData<Boolean> = _allElevatorsWorking


    fun fetchInfoForCurrentLocation() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getElevatorInfoForCurrentLocation()
            handleFetchResult(result, isLocationBased = true)
        }
    }

    fun searchStation(stationName: String) {
        if (stationName.isBlank()) {
            _errorMessage.value = Event("역 이름을 입력해주세요.")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.searchElevatorInfoByName(stationName)
            handleFetchResult(result, isLocationBased = false)
        }
    }

    fun onInquiryClicked() {
        _navigateToEmail.value = Event(Unit)
    }

    private fun handleFetchResult(result: Result<List<ElevatorRow>>, isLocationBased: Boolean) {
        result.fold(
            onSuccess = { elevatorList ->
                updateStateOnSuccess(elevatorList)
            },
            onFailure = { e ->
                updateStateOnFailure(e as IllegalAccessException, isLocationBased)
            }
        )
        _isLoading.value = false
    }

    private fun updateStateOnSuccess(allList: List<ElevatorRow>) {
        val rawStationName = allList.firstOrNull()?.stationName

        if (rawStationName == null) {
            _currentLocationName.value = "검색 결과 없음"
            _elevatorInfo.value = emptyList()
            _allElevatorsWorking.value = false
            return
        }
        val cleanStationName = rawStationName.replace(Regex("\\(.*\\)"), "")
        val maintenanceList = allList.filter { it.runStatus != "사용가능" }
        if (maintenanceList.isNotEmpty()) {
            _currentLocationName.value = "$cleanStationName(${maintenanceList.size}곳)"
        } else {
            _currentLocationName.value = cleanStationName
        }
        _elevatorInfo.value = maintenanceList
        _allElevatorsWorking.value = maintenanceList.isEmpty()
    }

    private fun updateStateOnFailure(e: Exception, isLocationBased: Boolean) {
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