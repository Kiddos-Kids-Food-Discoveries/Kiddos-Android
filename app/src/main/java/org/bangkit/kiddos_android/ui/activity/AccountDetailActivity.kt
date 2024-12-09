package org.bangkit.kiddos_android.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.databinding.ActivityAccountDetailBinding
import org.bangkit.kiddos_android.ui.viewmodel.AccountDetailViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.ViewModelFactory
import java.io.File
import java.io.FileOutputStream

class AccountDetailActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityAccountDetailBinding
    private lateinit var accountDetailViewModel: AccountDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide the progress bar initially
        binding.progressBarLoading.visibility = View.GONE

        accountDetailViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(applicationContext))
        ).get(AccountDetailViewModel::class.java)

        accountDetailViewModel.name.observe(this, Observer { name ->
            binding.editName.setText(name)
        })

        accountDetailViewModel.isLoading.observe(this, Observer { isLoading ->
            Log.d("AccountDetailActivity", "Loading state changed: $isLoading")
            // Show or hide the loading indicator based on isLoading
            if (isLoading) {
                binding.progressBarLoading.visibility = View.VISIBLE
                Log.d("AccountDetailActivity", "Progress bar shown")
            } else {
                binding.progressBarLoading.visibility = View.GONE
                Log.d("AccountDetailActivity", "Progress bar hidden")
            }
        })

        accountDetailViewModel.fetchUserData()

        // Load user image from UserPreference
        loadUserImage()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.userImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.saveButton.setOnClickListener {
            val newName = binding.editName.text.toString()
            if (newName.isNotEmpty()) {
                lifecycleScope.launch {
                    UserPreference.getInstance(applicationContext).getUserId().collect { userId ->
                        if (userId.isNotEmpty() && selectedImageUri != null) {
                            val userPictureFile = getImageFileFromUri(selectedImageUri)
                            updateUser(newName, userId, userPictureFile)
                        } else {
                            Toast.makeText(
                                this@AccountDetailActivity,
                                "User ID or image not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            }
        }

        binding.changePassButton.setOnClickListener {
            val email = binding.editEmail.text.toString()
            if (email.isNotEmpty()) {
                showConfirmationDialog(email)
            } else {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            UserPreference.getInstance(applicationContext).getEmail().collect { userEmail ->
                binding.editEmail.setText(userEmail)
            }
        }
    }

    private fun loadUserImage() {
        lifecycleScope.launch {
            UserPreference.getInstance(applicationContext).getUserPicture().collect { pictureUrl ->
                Glide.with(this@AccountDetailActivity)
                    .load(pictureUrl)
                    .into(binding.userImage)
            }
        }
    }

    private var selectedImageUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Ensure selected image URI is valid
            selectedImageUri = data.data

            selectedImageUri?.let { uri ->
                Log.d("AccountDetailActivity", "Selected URI: $uri")

                // Clear old cache before setting the new image
                clearOldCache()

                // Set the new image using Glide
                Glide.with(this@AccountDetailActivity)
                    .load(uri)
                    .into(binding.userImage)

                // Also, store the image file if necessary
                val selectedImageFile = getImageFileFromUri(uri)
                Log.d(
                    "AccountDetailActivity",
                    "New selected image path: ${selectedImageFile.absolutePath}"
                )
            }
                ?: run {
                    Log.e("AccountDetailActivity", "Selected URI is null")
                }
        }
    }

    private fun getImageFileFromUri(uri: Uri?): File {
        val uniqueFileName = "user_picture_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, uniqueFileName)

        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.flush()
            outputStream.close()
        }

        Log.d("AccountDetailActivity", "New file created: ${file.absolutePath}")
        return file
    }


    private fun clearOldCache() {
        val cacheDir = cacheDir
        if (cacheDir.isDirectory) {
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete() // Hapus file dalam cache
                }
            }
        }
    }

    private fun updateUser(newName: String, userId: String, userPictureFile: File) {
        Log.d("AccountDetailActivity", "Start updating user, showing progress bar.")
        binding.progressBarLoading.visibility = View.VISIBLE

        val userPictureRequestBody =
            RequestBody.create("image/jpeg".toMediaTypeOrNull(), userPictureFile)
        val userPicturePart = MultipartBody.Part.createFormData(
            "user_picture",
            userPictureFile.name,
            userPictureRequestBody
        )
        val nameRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), newName)
        val userIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)

        lifecycleScope.launch {
            try {
                Log.d("AccountDetailActivity", "Sending API request to update user")
                Log.d("AccountDetailActivity", "Image file: ${userPictureFile.absolutePath}")
                Log.d("AccountDetailActivity", "Image file size: ${userPictureFile.length()}")
                val response = ApiConfig.getApiService()
                    .updateUser(userId, userPicturePart, nameRequestBody, userIdRequestBody)

                binding.progressBarLoading.visibility = View.GONE

                if (response.isSuccessful) {
                    val updateUserResponse = response.body()
                    if (updateUserResponse?.status == "success") {
                        Log.d("AccountDetailActivity", "User updated successfully")
                        Toast.makeText(
                            this@AccountDetailActivity,
                            updateUserResponse.message,
                            Toast.LENGTH_SHORT
                        ).show()

                        UserPreference.getInstance(applicationContext).apply {
                            saveName(newName)
                        }

                        clearOldCache()
                    } else {
                        Log.e(
                            "AccountDetailActivity",
                            "Failed to update user: ${updateUserResponse?.message}"
                        )
                        Toast.makeText(
                            this@AccountDetailActivity,
                            "Failed to update user: ${updateUserResponse?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(
                        "AccountDetailActivity",
                        "API call failed: ${response.code()} - ${response.message()}"
                    )
                    Toast.makeText(
                        this@AccountDetailActivity,
                        "Failed to update user",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("AccountDetailActivity", "Error: ${e.message}", e)
                binding.progressBarLoading.visibility = View.GONE // Hide progress bar on error
                Toast.makeText(
                    this@AccountDetailActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun showConfirmationDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.reset_password_confirmation_title))
        builder.setMessage(getString(R.string.reset_password_confirmation_message, email))
        builder.setPositiveButton(getString(R.string.reset_password_button_yes)) { dialog, _ ->
            // Proceed with password reset
            requestPasswordReset(email)
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.reset_password_button_no)) { dialog, _ ->
            dialog.dismiss() // Dismiss dialog
        }
        builder.create().show()
    }

    private fun requestPasswordReset(email: String) {
        // Show progress bar when starting the reset request
        binding.progressBarLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().resetPassword(email)

                // Hide progress bar after request is complete
                binding.progressBarLoading.visibility = View.GONE

                if (response.error == false) {
                    val resetLink = response.resetlink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resetLink))
                    startActivity(intent)
                } else {

                    Toast.makeText(
                        this@AccountDetailActivity,
                        getString(R.string.failed_to_send_reset_link),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBarLoading.visibility = View.GONE // Hide progress bar on error
                Toast.makeText(
                    this@AccountDetailActivity,
                    getString(R.string.request_failed, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}