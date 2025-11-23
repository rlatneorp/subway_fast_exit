//package com.rlatneorp.fast_subway_exit.viewmodel
//
//import android.app.Application
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import com.rlatneorp.fast_subway_exit.model.ElevatorRow
//import com.rlatneorp.fast_subway_exit.model.StationRepository
//import io.mockk.coEvery
//import io.mockk.mockk
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.resetMain
//import kotlinx.coroutines.test.runTest
//import kotlinx.coroutines.test.setMain
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//
//private const val STATION_SINSEOLDONG = "신설동"
//private const val STATION_JONGNO_3GA = "종로3가"
//private const val FACILITY_ESC_1 = "에스컬레이터 1호기"
//private const val FACILITY_ELV_1 = "엘리베이터 1호기"
//private const val FACILITY_ELV_2 = "엘리베이터 2호기"
//private const val LOCATION_EXIT_1 = "1번 출구"
//private const val LOCATION_EXIT_2 = "2번 출구"
//private const val STATUS_AVAILABLE = "사용가능"
//private const val STATUS_REPAIR = "보수중"
//private const val STATUS_CHECKING = "점검중"
//private const val EXPECTED_FACILITY_ELV_GROUP = "엘리베이터"
//private const val EXPECTED_LOCATION_GROUP = "1번 출구, 2번 출구"
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class MainViewModelTest {
//
//    @get:Rule
//    val instantExecutorRule = InstantTaskExecutorRule()
//
//    private val application = mockk<Application>(relaxed = true)
//    private val repository = mockk<StationRepository>()
//    private lateinit var viewModel: MainViewModel
//    private val testDispatcher = StandardTestDispatcher()
//
//    @Before
//    fun setup() {
//        Dispatchers.setMain(testDispatcher)
//        viewModel = MainViewModel(application, repository)
//    }
//
//    @After
//    fun tearDown() {
//        Dispatchers.resetMain()
//    }
//
//    @Test
//    fun `사용가능한 승강기는 목록에서 제외되어야 한다`() = runTest {
//        val mockData = listOf(
//            ElevatorRow(STATION_SINSEOLDONG, FACILITY_ESC_1, LOCATION_EXIT_1, STATUS_AVAILABLE),
//            ElevatorRow(STATION_SINSEOLDONG, FACILITY_ELV_1, LOCATION_EXIT_2, STATUS_REPAIR)
//        )
//        coEvery { repository.searchElevatorInfoByName(STATION_SINSEOLDONG) } returns Result.success(mockData)
//        viewModel.searchStation(STATION_SINSEOLDONG)
//        testDispatcher.scheduler.advanceUntilIdle()
//        val resultList = viewModel.elevatorInfo.value
//        assertEquals(1, resultList?.size)
//        assertEquals(STATUS_REPAIR, resultList?.first()?.runStatus)
//    }
//
//    @Test
//    fun `같은 종류의 승강기는 하나로 묶여야 한다`() = runTest {
//        val mockData = listOf(
//            ElevatorRow(STATION_JONGNO_3GA, FACILITY_ELV_1, LOCATION_EXIT_1, STATUS_CHECKING),
//            ElevatorRow(STATION_JONGNO_3GA, FACILITY_ELV_2, LOCATION_EXIT_2, STATUS_CHECKING)
//        )
//        coEvery { repository.searchElevatorInfoByName(STATION_JONGNO_3GA) } returns Result.success(mockData)
//        viewModel.searchStation(STATION_JONGNO_3GA)
//        testDispatcher.scheduler.advanceUntilIdle()
//        val resultList = viewModel.elevatorInfo.value
//        assertEquals(1, resultList?.size)
//        val item = resultList?.first()
//        assertEquals(EXPECTED_FACILITY_ELV_GROUP, item?.facilityName)
//        assertEquals(EXPECTED_LOCATION_GROUP, item?.location)
//    }
//}