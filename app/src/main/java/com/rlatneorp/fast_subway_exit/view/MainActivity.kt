package com.rlatneorp.fast_subway_exit.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.rlatneorp.fast_subway_exit.R
import com.rlatneorp.fast_subway_exit.viewmodel.Event
import com.rlatneorp.fast_subway_exit.viewmodel.MainViewModel
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var locationInfo: TextView
    private lateinit var elevatorInfo: TextView
    private lateinit var searchButton: ImageButton
    private lateinit var inputSearchText: TextInputEditText

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // (로직이 내부에 있음)
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                viewModel.fetchInfoForCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                viewModel.fetchInfoForCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationInfo = findViewById(R.id.locationInfo)
        elevatorInfo = findViewById(R.id.elevatorInfo)
        searchButton = findViewById(R.id.searchButton)
        inputSearchText = findViewById(R.id.inputSearchText)

        val mailButton: ImageButton = findViewById(R.id.mailButton)
        val currentLocationButton: ImageButton = findViewById(R.id.currentLocationButton)

        setupObservers()
        setupClickListeners(mailButton, currentLocationButton)
        checkLocationPermissionAndLoadData()
    }

    private fun setupObservers() {
        viewModel.elevatorInfo.observe(this) { status ->
            elevatorInfo.text = status.status
        }
        viewModel.currentLocationName.observe(this) { name ->
            locationInfo.text = name
        }
        viewModel.isLoading.observe(this) { isLoading ->
            // progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(this) { event ->
            event.getContentIfNotHandled()?.let { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.navigateToEmail.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                sendEmailInquiry()
            }
        }
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
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.fetchInfoForCurrentLocation()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(this, "현재 위치의 승강기 정보를 위해 위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ))
            }
        }
    }

    private fun sendEmailInquiry() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("rlatneorp@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "승강기 앱 문의")
            putExtra(Intent.EXTRA_TEXT, "문의 내용을 입력하세요:")
        }
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "이메일 앱을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}