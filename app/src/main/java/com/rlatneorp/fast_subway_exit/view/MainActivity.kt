package com.rlatneorp.fast_subway_exit.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rlatneorp.fast_subway_exit.R
import com.rlatneorp.fast_subway_exit.viewmodel.Event
import com.rlatneorp.fast_subway_exit.viewmodel.MainViewModel
import com.google.android.material.textfield.TextInputEditText
import com.rlatneorp.fast_subway_exit.model.ElevatorRow

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

        searchButton = findViewById(R.id.searchButton)
        inputSearchText = findViewById(R.id.inputSearchText)
        val mailButton: ImageButton = findViewById(R.id.mailButton)
        val currentLocationButton: ImageButton = findViewById(R.id.currentLocationButton)

        loadingLayout = findViewById(R.id.loadingLayout)
        initialLayout = findViewById(R.id.initialLayout)
        initialMessageText = findViewById(R.id.initialMessageText)
        rvElevatorList = findViewById(R.id.rvElevatorList)

        stationNameResult = findViewById(R.id.stationNameResult)

        setupRecyclerView()

        setupObservers()

        setupClickListeners(mailButton, currentLocationButton)

        checkLocationPermission()
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

    private fun handleElevatorInfoUpdate(elevatorList: List<ElevatorRow>) {
        loadingLayout.visibility = View.GONE

        if (elevatorList.isNotEmpty()) {
            rvElevatorList.visibility = View.VISIBLE
            initialLayout.visibility = View.GONE
            stationNameResult.visibility = View.VISIBLE

            elevatorAdapter.submitList(elevatorList)
            return
        }

        rvElevatorList.visibility = View.GONE
        stationNameResult.visibility = View.GONE
        initialLayout.visibility = View.VISIBLE

        updateInitialMessage()
    }

    private fun updateInitialMessage() {
        if (viewModel.currentLocationName.value == "검색 결과 없음") {
            initialMessageText.text = "검색 결과가 없습니다."
            return
        }
        initialMessageText.text = "역 이름을 검색하거나 현재 위치를 찾아보세요."
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

    private fun setupClickListeners(mailButton: ImageButton, currentLocationButton: ImageButton) {
        searchButton.setOnClickListener {
            val query = inputSearchText.text.toString()
            viewModel.searchStation(query)
        }

        mailButton.setOnClickListener {
            viewModel.onInquiryClicked()
        }

        currentLocationButton.setOnClickListener {
            checkLocationPermissionAndLoadData()
        }
    }

    private fun checkLocationPermissionAndLoadData() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            viewModel.fetchInfoForCurrentLocation()
            return
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "현재 위치의 승강기 정보를 위해 위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "현재 위치 기능을 위해 위치 권단이 필요합니다.", Toast.LENGTH_SHORT).show()
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun sendEmailInquiry() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("contact@example.com"))
            putExtra(Intent.EXTRA_SUBJECT, "승강기 앱 문의")
            putExtra(Intent.EXTRA_TEXT, "문의 내용을 입력하세요:")
        }

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
            return
        }

        Toast.makeText(this, "이메일 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun handleLocationPermissionResult(permissions: Map<String, Boolean>) {
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            showToast("위치 권한이 승인되었습니다.")
            return
        }


        if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
            showToast("위치 권한이 승인되었습니다.")
            return
        }

        showToast("위치 권한이 필요합니다.")
    }

    private fun handleErrorMessage(event: Event<String>) {
        event.getContentIfNotHandled()?.let { message ->
            showToast(message)
            loadingLayout.visibility = View.GONE
            initialLayout.visibility = View.VISIBLE
            rvElevatorList.visibility = View.GONE
            stationNameResult.visibility = View.GONE
        }
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