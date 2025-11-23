package com.rlatneorp.fast_subway_exit.view

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rlatneorp.fast_subway_exit.R
import com.rlatneorp.fast_subway_exit.model.ElevatorUIModel
import com.rlatneorp.fast_subway_exit.viewmodel.Event
import com.rlatneorp.fast_subway_exit.viewmodel.MainViewModel
import com.google.android.material.textfield.TextInputEditText

private const val COLOR_PURPLE_HEX = "#6750A4"
private const val EMAIL_ADDRESS = "rlatneorp@gmail.com"
private const val EMAIL_SUBJECT = "승강기 앱 문의"
private const val EMAIL_BODY = "문의 내용을 입력하세요:"
private const val EMAIL_SCHEME = "mailto:"
private const val MSG_ALL_WORKING = "보수중인 출구가 없습니다."
private const val MSG_NO_RESULT_KEY = "검색 결과 없음"
private const val MSG_NO_RESULT_TEXT = "검색 결과가 없습니다."
private const val MSG_DEFAULT_HINT = "역 이름을 검색하거나 현재 위치를 찾아보세요."
private const val MSG_EMAIL_APP_NOT_FOUND = "이메일을 보낼 수 있는 앱이 없습니다."
private const val MSG_PERMISSION_GRANTED = "위치 권한이 승인되었습니다."
private const val MSG_PERMISSION_REQUIRED = "위치 권한이 필요합니다."
private const val MSG_PERMISSION_RATIONALE_LOAD = "현재 위치의 승강기 정보를 위해 위치 권한이 필요합니다."
private const val MSG_PERMISSION_RATIONALE_CHECK = "현재 위치 기능을 위해 위치 권한이 필요합니다."
private const val MSG_EMAIL_ERROR = "이메일 앱 실행 중 오류가 발생했습니다."

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var elevatorAdapter: ElevatorAdapter
    private lateinit var searchButton: ImageButton
    private lateinit var inputSearchText: TextInputEditText
    private lateinit var loadingLayout: LinearLayout
    private lateinit var initialLayout: LinearLayout
    private lateinit var rvElevatorList: RecyclerView
    private lateinit var stationNameResult: TextView
    private lateinit var initialMessageText: TextView
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handleLocationPermissionResult(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        checkLocationPermission()
    }

    private fun initViews() {
        searchButton = findViewById(R.id.searchButton)
        inputSearchText = findViewById(R.id.inputSearchText)
        loadingLayout = findViewById(R.id.loadingLayout)
        initialLayout = findViewById(R.id.initialLayout)
        initialMessageText = findViewById(R.id.initialMessageText)
        rvElevatorList = findViewById(R.id.rvElevatorList)
        stationNameResult = findViewById(R.id.stationNameResult)
    }

    private fun setupRecyclerView() {
        elevatorAdapter = ElevatorAdapter()
        rvElevatorList.adapter = elevatorAdapter
        rvElevatorList.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.elevatorInfo.observe(this) { elevatorList ->
            handleElevatorInfoUpdate(elevatorList)
        }
        viewModel.allElevatorsWorking.observe(this) { isAllWorking ->
            if (isAllWorking) {
                showAllWorkingMessage()
            }
        }
        viewModel.currentLocationName.observe(this) { name ->
            stationNameResult.text = name
        }
        viewModel.isLoading.observe(this) { isLoading ->
            handleLoadingUpdate(isLoading)
        }
        viewModel.errorMessage.observe(this) { event ->
            handleErrorMessage(event)
        }
        viewModel.navigateToEmail.observe(this) { event ->
            handleEmailNavigation(event)
        }
    }

    private fun handleElevatorInfoUpdate(elevatorList: List<ElevatorUIModel>) {
        loadingLayout.visibility = View.GONE
        if (elevatorList.isNotEmpty()) {
            rvElevatorList.visibility = View.VISIBLE
            initialLayout.visibility = View.GONE
            stationNameResult.visibility = View.VISIBLE
            elevatorAdapter.submitList(elevatorList)
            return
        }
        rvElevatorList.visibility = View.GONE
    }

    private fun showAllWorkingMessage() {
        loadingLayout.visibility = View.GONE
        rvElevatorList.visibility = View.GONE
        initialLayout.visibility = View.VISIBLE
        stationNameResult.visibility = View.VISIBLE
        initialMessageText.text = MSG_ALL_WORKING
        initialMessageText.setTextColor(Color.parseColor(COLOR_PURPLE_HEX))
    }

    private fun updateInitialMessage() {
        val defaultColor = Color.parseColor(COLOR_PURPLE_HEX)
        if (viewModel.currentLocationName.value == MSG_NO_RESULT_KEY) {
            initialMessageText.text = MSG_NO_RESULT_TEXT
            initialMessageText.setTextColor(defaultColor)
            return
        }
        initialMessageText.text = MSG_DEFAULT_HINT
        initialMessageText.setTextColor(defaultColor)
    }

    private fun handleLoadingUpdate(isLoading: Boolean) {
        if (isLoading) {
            loadingLayout.visibility = View.VISIBLE
            initialLayout.visibility = View.GONE
            rvElevatorList.visibility = View.GONE
            stationNameResult.visibility = View.GONE
            return
        }
        loadingLayout.visibility = View.GONE
    }

    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            val query = inputSearchText.text.toString()
            viewModel.searchStation(query)
        }
        findViewById<View>(R.id.mailButton).setOnClickListener {
            viewModel.onInquiryClicked()
        }
        findViewById<View>(R.id.currentLocationButton).setOnClickListener {
            checkLocationPermissionAndLoadData()
        }
    }

    private fun checkLocationPermissionAndLoadData() {
        if (hasLocationPermission()) {
            viewModel.fetchInfoForCurrentLocation()
            return
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showToast(MSG_PERMISSION_RATIONALE_LOAD)
            requestLocationPermissions()
            return
        }
        requestLocationPermissions()
    }

    private fun checkLocationPermission() {
        if (hasLocationPermission()) {
            return
        }
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showToast(MSG_PERMISSION_RATIONALE_CHECK)
        }
        requestLocationPermissions()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun sendEmailInquiry() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse(EMAIL_SCHEME)
            putExtra(Intent.EXTRA_EMAIL, arrayOf(EMAIL_ADDRESS))
            putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT)
            putExtra(Intent.EXTRA_TEXT, EMAIL_BODY)
        }
        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            showToast(MSG_EMAIL_APP_NOT_FOUND)
        } catch (e: Exception) {
            showToast(MSG_EMAIL_ERROR)
        }
    }

    private fun handleLocationPermissionResult(permissions: Map<String, Boolean>) {
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            showToast(MSG_PERMISSION_GRANTED)
            return
        }
        if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            showToast(MSG_PERMISSION_GRANTED)
            return
        }
        showToast(MSG_PERMISSION_REQUIRED)
    }

    private fun handleErrorMessage(event: Event<String>) {
        event.getContentIfNotHandled()?.let { message ->
            showToast(message)
            showErrorState()
        }
    }

    private fun showErrorState() {
        loadingLayout.visibility = View.GONE
        initialLayout.visibility = View.VISIBLE
        rvElevatorList.visibility = View.GONE
        stationNameResult.visibility = View.GONE
        updateInitialMessage()
    }

    private fun handleEmailNavigation(event: Event<Unit>) {
        event.getContentIfNotHandled()?.let {
            sendEmailInquiry()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}