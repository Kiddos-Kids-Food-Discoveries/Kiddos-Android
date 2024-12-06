package org.bangkit.kiddos_android.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import org.bangkit.kiddos_android.R

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private lateinit var previewImageView: ShapeableImageView

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            previewImageView.setImageBitmap(imageBitmap)
            Log.d("ScanFragment", "Take Picture Success")
        } else {
            Log.e("ScanFragment", "Take Picture Failed")
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Log.d("ScanFragment", "Picked Image URI: $it")
            previewImageView.setImageURI(it)
        } ?: Log.e("ScanFragment", "Picked Image URI is null")
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            openCamera()
        } else {
            Log.e("ScanFragment", "Camera permission denied")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previewImageView = view.findViewById(R.id.previewImageView)

        val galleryButton: MaterialButton = view.findViewById(R.id.galleryButton)
        galleryButton.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Option")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            cameraResultLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openGallery() {
        Log.d("ScanFragment", "Opening gallery")
        pickImageLauncher.launch("image/*")
    }
}
