package org.bangkit.kiddos_android.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import retrofit2.Response
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
        binding.progressBarLoading.visibility = android.view.View.GONE

        accountDetailViewModel = ViewModelProvider(this, ViewModelFactory(UserPreference.getInstance(applicationContext)))
            .get(AccountDetailViewModel::class.java)

        accountDetailViewModel.name.observe(this, Observer { name ->
            binding.tvName.text = name
            binding.editName.setText(name)
        })

        accountDetailViewModel.isLoading.observe(this, Observer { isLoading ->
            Log.d("AccountDetailActivity", "Loading state changed: $isLoading")
            // Show or hide the loading indicator based on isLoading
            if (isLoading) {
                binding.progressBarLoading.visibility = android.view.View.VISIBLE
                Log.d("AccountDetailActivity", "Progress bar shown")
            } else {
                binding.progressBarLoading.visibility = android.view.View.GONE
                Log.d("AccountDetailActivity", "Progress bar hidden")
            }
        })

        accountDetailViewModel.fetchUserData()

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.userImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
                            Toast.makeText(this@AccountDetailActivity, "User ID or image not found", Toast.LENGTH_SHORT).show()
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


    private var selectedImageUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Get the selected image URI
            selectedImageUri = data.data

            // Set the image to the ImageView
            binding.userImage.setImageURI(selectedImageUri)

            // Optionally, you can convert the selected image URI to a file
            val selectedImageFile = getImageFileFromUri(selectedImageUri)
            // Now you have the image file, and you can use it later when saving the profile
            // You can save this `selectedImageFile` in a variable or directly use it
        }
    }


    fun getImageFileFromUri(uri: Uri?): File {
        val file = File(cacheDir, "user_picture.jpg")
        val inputStream = contentResolver.openInputStream(uri!!)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.flush()
        outputStream.close()

        return file
    }


    private fun updateUser(newName: String, userId: String, userPictureFile: File) {
        Log.d("AccountDetailActivity", "Start updating user, showing progress bar.")
        binding.progressBarLoading.visibility = android.view.View.VISIBLE

        val userPictureRequestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), userPictureFile)
        val userPicturePart = MultipartBody.Part.createFormData("user_picture", userPictureFile.name, userPictureRequestBody)
        val nameRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), newName)
        val userIdRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), userId)

        lifecycleScope.launch {
            try {
                Log.d("AccountDetailActivity", "Sending API request to update user")
                val response = ApiConfig.getApiService().updateUser(userId, userPicturePart, nameRequestBody, userIdRequestBody)

                Log.d("AccountDetailActivity", "API response received: ${response.code()} - ${response.message()}")
                binding.progressBarLoading.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    val updateUserResponse = response.body()
                    if (updateUserResponse?.status == "success") {
                        Log.d("AccountDetailActivity", "User updated successfully")
                        Toast.makeText(this@AccountDetailActivity, updateUserResponse.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("AccountDetailActivity", "Failed to update user: ${updateUserResponse?.message}")
                        Toast.makeText(this@AccountDetailActivity, "Failed to update user: ${updateUserResponse?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("AccountDetailActivity", "API call failed: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@AccountDetailActivity, "Failed to update user", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AccountDetailActivity", "Error: ${e.message}", e)
                binding.progressBarLoading.visibility = android.view.View.GONE // Hide progress bar on error
                Toast.makeText(this@AccountDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
            dialog.dismiss() // Dismiss the dialog
        }
        builder.show()
    }

    private fun requestPasswordReset(email: String) {
        // Show progress bar when starting the reset request
        binding.progressBarLoading.visibility = android.view.View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiConfig.getApiService().resetPassword(email)
                }

                // Hide progress bar after request is complete
                binding.progressBarLoading.visibility = android.view.View.GONE

                if (response.error == false) {
                    val resetLink = response.resetlink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resetLink))
                    startActivity(intent)
                } else {
                    Toast.makeText(this@AccountDetailActivity, getString(R.string.failed_to_send_reset_link), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBarLoading.visibility = android.view.View.GONE // Hide progress bar on error
                Toast.makeText(this@AccountDetailActivity, getString(R.string.request_failed, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
