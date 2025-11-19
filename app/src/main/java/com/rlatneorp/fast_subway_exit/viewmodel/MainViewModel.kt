package com.rlatneorp.fast_subway_exit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rlatneorp.fast_subway_exit.model.ElevatorRow
import com.rlatneorp.fast_subway_exit.model.StationRepository
import kotlinx.coroutines.launch

private const val MSG_INPUT_STATION_NAME = "역 이름을 입력해주세요."
private const val MSG_CURRENT_LOCATION_DEFAULT = "현재위치"
private const val MSG_NO_SEARCH_RESULT = "검색 결과 없음"
private const val MSG_LOCATION_UNKNOWN = "위치 정보 없음"
private const val MSG_SEARCH_FAILURE_PREFIX = "검색 실패: "
private const val MSG_LOCATION_FAILURE_PREFIX = "위치 또는 승강기 정보를 가져오는데 실패했습니다: "
private const val STATUS_AVAILABLE = "사용가능"
private const val REGEX_PARENTHESES_PATTERN = "\\(.*\\)"
private const val SUFFIX_PLACE_COUNT = "개"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StationRepository = StationRepository(application)

    private val _elevatorInfo = MutableLiveData<List<ElevatorRow>>()
    val elevatorInfo: LiveData<List<ElevatorRow>> = _elevatorInfo

    private val _currentLocationName = MutableLiveData<String>(MSG_CURRENT_LOCATION_DEFAULT)
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
            _errorMessage.value = Event(MSG_INPUT_STATION_NAME)
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
                updateStateOnFailure(e, isLocationBased)
            }
        )
        _isLoading.value = false
    }

    private fun updateStateOnSuccess(allList: List<ElevatorRow>) {
        val rawStationName = allList.firstOrNull()?.stationName

        if (rawStationName == null) {
            _currentLocationName.value = MSG_NO_SEARCH_RESULT
            _elevatorInfo.value = emptyList()
            _allElevatorsWorking.value = false
            return
        }

        val cleanStationName = rawStationName.replace(Regex(REGEX_PARENTHESES_PATTERN), "")
        val maintenanceList = allList.filter { it.runStatus != STATUS_AVAILABLE }

        _currentLocationName.value = cleanStationName

        if (maintenanceList.isNotEmpty()) {
            _currentLocationName.value = "$cleanStationName(${maintenanceList.size}$SUFFIX_PLACE_COUNT)"
        }

        _elevatorInfo.value = maintenanceList
        _allElevatorsWorking.value = maintenanceList.isEmpty()
    }

    private fun updateStateOnFailure(e: Throwable, isLocationBased: Boolean) {
        var errorMessage = "$MSG_SEARCH_FAILURE_PREFIX${e.message}"
        if (isLocationBased) {
            errorMessage = "$MSG_LOCATION_FAILURE_PREFIX${e.message}"
        }
        _errorMessage.value = Event(errorMessage)
        if (isLocationBased) {
            _currentLocationName.value = MSG_LOCATION_UNKNOWN
        }
    }
}