package org.bangkit.kiddos_android.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.remote.response.PredictResponse
import org.bangkit.kiddos_android.data.repository.PredictRepository
import org.bangkit.kiddos_android.databinding.FragmentScanBinding
import org.bangkit.kiddos_android.ui.activity.ScanResultActivity
import org.bangkit.kiddos_android.ui.viewmodel.PredictViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.PredictViewModelFactory
import org.bangkit.kiddos_android.utils.NetworkUtils
import java.io.File
import java.io.FileOutputStream

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private lateinit var binding: FragmentScanBinding
    private lateinit var predictViewModel: PredictViewModel
    private var selectedImageUri: Uri? = null

    private val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                binding.previewImageView.setImageBitmap(imageBitmap)

                selectedImageUri = saveBitmapToCache(imageBitmap)
                Log.d("ScanFragment", "Take Picture Success, URI: $selectedImageUri")
            } else {
                Log.e("ScanFragment", "Take Picture Failed")
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d("ScanFragment", "Picked Image URI: $it")
                binding.previewImageView.setImageURI(it)
                selectedImageUri = it
            } ?: Log.e("ScanFragment", "Picked Image URI is null")
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                Log.e("ScanFragment", "Camera permission denied")
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentScanBinding.bind(view)

        val apiService = ApiConfig.getApiService()
        val predictRepository = PredictRepository(apiService)
        val viewModelFactory = PredictViewModelFactory(predictRepository)
        predictViewModel =
            ViewModelProvider(this, viewModelFactory).get(PredictViewModel::class.java)

        binding.galleryButton.setOnClickListener {
            showImagePickerDialog()
        }

        binding.buttonPredict.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                selectedImageUri?.let { uri ->
                    val imageFile = getImageFileFromUri(uri)
                    val resizedImageFile = resizeImageFile(imageFile)
                    val imageRequestBody =
                        resizedImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData(
                        "file",
                        resizedImageFile.name,
                        imageRequestBody
                    )

                    lifecycleScope.launch {
                        val userId =
                            UserPreference.getInstance(requireContext()).getUserId().first()
                        val userIdRequestBody =
                            userId.toRequestBody("text/plain".toMediaTypeOrNull())

                        predictViewModel.predict(imagePart, userIdRequestBody)
                    }
                } ?: run {
                    Toast.makeText(
                        requireContext(),
                        "Silahkan pilih gambar terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireContext(), "Tidak ada koneksi internet", Toast.LENGTH_SHORT)
                    .show()
                Log.e("ScanFragment", "No internet connection")
            }
        }


        predictViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        predictViewModel.predictResult.observe(viewLifecycleOwner) { predictResponse: PredictResponse? ->
            if (predictResponse != null) {
                if (predictResponse.status == "success") {
                    Toast.makeText(requireContext(), "Scan berhasil", Toast.LENGTH_SHORT).show()
                    Log.d("ScanFragment", "Prediction successful: $predictResponse")
                    navigateToScanResult(predictResponse)
                    Handler(Looper.getMainLooper()).postDelayed({
                        predictViewModel.resetPredictResult()
                    }, 3500)
                } else {
                    val errorMessage = predictResponse.message.lowercase()

                    if (errorMessage.contains("timeout") || errorMessage.contains("time out") || errorMessage.contains(
                            "connection"
                        )
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Koneksi server bermasalah. Silahkan coba lagi",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Terjadi kesalahan. Silahkan coba lagi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    Log.e("ScanFragment", "Prediction failed: $errorMessage")
                    Handler(Looper.getMainLooper()).postDelayed({
                        predictViewModel.resetPredictResult()
                    }, 1000)
                }
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.navigation_home)
        }
    }

    private fun navigateToScanResult(predictResponse: PredictResponse) {
        val intent = Intent(requireContext(), ScanResultActivity::class.java).apply {
            putExtra("PREDICT_RESPONSE", predictResponse)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Ambil Foto", "Pilih Dari Galeri")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Pilih Opsi")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(requireContext().cacheDir, "camera_image.jpg")
        val outputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(file)
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
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

    private fun getImageFileFromUri(uri: Uri?): File {
        val file = File(requireContext().cacheDir, "user_image.jpg")
        uri?.let {
            val inputStream = requireContext().contentResolver.openInputStream(it)
            val outputStream = FileOutputStream(file)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            inputStream?.close()
            outputStream.flush()
            outputStream.close()
        }
        return file
    }

    private fun resizeImageFile(imageFile: File): File {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

        val maxWidth = 800
        val maxHeight = 800

        val ratioBitmap = bitmap.width.toFloat() / bitmap.height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (bitmap.width > bitmap.height) {
            targetWidth = maxWidth
            targetHeight = (maxWidth / ratioBitmap).toInt()
        } else {
            targetWidth = (maxHeight * ratioBitmap).toInt()
            targetHeight = maxHeight
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)

        val compressedFile = File(requireContext().cacheDir, "compressed_image.jpg")
        val outputStream = FileOutputStream(compressedFile)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()

        return compressedFile
    }
}
