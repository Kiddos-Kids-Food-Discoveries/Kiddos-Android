package org.bangkit.kiddos_android.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.bangkit.kiddos_android.R
import org.bangkit.kiddos_android.data.preferences.UserPreference
import org.bangkit.kiddos_android.data.remote.api.ApiConfig
import org.bangkit.kiddos_android.data.repository.UserRepository
import org.bangkit.kiddos_android.databinding.ActivityAccountDetailBinding
import org.bangkit.kiddos_android.ui.viewmodel.AccountDetailViewModel
import org.bangkit.kiddos_android.ui.viewmodel.factory.ViewModelFactory
import java.io.File
import java.io.FileOutputStream

class AccountDetailActivity : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var binding: ActivityAccountDetailBinding
    private lateinit var accountDetailViewModel: AccountDetailViewModel
    private lateinit var userPreference: UserPreference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBarLoading.visibility = View.GONE

        userPreference = UserPreference.getInstance(applicationContext)
        val userRepository = UserRepository(ApiConfig.getApiService())
        accountDetailViewModel = ViewModelProvider(
            this,
            ViewModelFactory(userPreference, userRepository)
        ).get(AccountDetailViewModel::class.java)

        accountDetailViewModel.name.observe(this, Observer { name ->
            binding.editName.setText(name)
        })

        accountDetailViewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBarLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        accountDetailViewModel.user.observe(this) { user ->
            user?.let {
                Log.d("AccountDetailActivity", "User Picture URL: ${it.userPicture}")

                Glide.with(this)
                    .load(it.userPicture + "?timestamp=${System.currentTimeMillis()}") // Add timestamp to invalidate cache
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.placeholder_profile)
                    .into(binding.userImage)
            } ?: run {
                Log.d("AccountDetailActivity", "User is null or guest user")
                binding.editName.setText(getString(R.string.guest))
            }
        }

        lifecycleScope.launch {
            userPreference.getUserId().collect { userId ->
                if (userId.isNotEmpty()) {
                    accountDetailViewModel.fetchUser(userId)
                }
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.userImage.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ).apply {
                setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.saveButton.setOnClickListener {
            val newName = binding.editName.text.toString()
            if (newName.isNotEmpty()) {
                lifecycleScope.launch {
                    userPreference.getUserId().collect { userId ->
                        if (userId.isNotEmpty()) {
                            val userPictureFile = selectedImageUri?.let { getImageFileFromUri(it) }
                            updateUser(newName, userId, userPictureFile)
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Nama wajib diisi", Toast.LENGTH_SHORT).show()
            }
        }

        binding.changePassButton.setOnClickListener {
            val email = binding.editEmail.text.toString()
            if (email.isNotEmpty()) {
                showConfirmationDialog(email)
            } else {
                Toast.makeText(this, "Email wajib diisi", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            userPreference.getEmail().collect { userEmail ->
                binding.editEmail.setText(userEmail)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data

            selectedImageUri?.let { uri ->
                clearOldCache()
                Glide.with(this@AccountDetailActivity)
                    .load(uri)
                    .into(binding.userImage)
                getImageFileFromUri(uri)
            }
        }
    }

    private fun getImageFileFromUri(uri: Uri?): File {
        val uniqueFileName = "user_picture_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, uniqueFileName)

        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = FileOutputStream(file)
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

            outputStream.flush()
            outputStream.close()
            inputStream?.close()
        }

        return file
    }

    private fun clearOldCache() {
        val cacheDir = cacheDir
        if (cacheDir.isDirectory) {
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.delete()
                }
            }
        }
    }


    private fun updateUser(newName: String, userId: String, userPictureFile: File?) {
        binding.progressBarLoading.visibility = View.VISIBLE

        val params = mutableMapOf<String, RequestBody>()
        params["name"] = newName.toRequestBody("text/plain".toMediaTypeOrNull())
        params["userId"] = userId.toRequestBody("text/plain".toMediaTypeOrNull())

        val userPicturePart = userPictureFile?.let {
            val userPictureRequestBody = it.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("user_picture", it.name, userPictureRequestBody)
        }

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService()
                    .updateUser(userId, userPicturePart, params)
                binding.progressBarLoading.visibility = View.GONE

                if (response.isSuccessful) {
                    val updateUserResponse = response.body()
                    if (updateUserResponse?.status == "success") {
                        userPreference.saveName(newName)

                        clearOldCache()

                        finish()
                    } else {
                        Toast.makeText(
                            this@AccountDetailActivity,
                            "Gagal memperbarui pengguna: ${updateUserResponse?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@AccountDetailActivity,
                        "Gagal memperbarui pengguna",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBarLoading.visibility = View.GONE
            }
        }
    }




    private fun showConfirmationDialog(email: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Reset Password")
        builder.setMessage("Apakah Anda ingin mereset password untuk email $email?")
        builder.setPositiveButton("Ya") { dialog, _ ->
            requestPasswordReset(email)
            dialog.dismiss()
        }
        builder.setNegativeButton("Tidak") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun requestPasswordReset(email: String) {
        binding.progressBarLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = ApiConfig.getApiService().resetPassword(email)

                binding.progressBarLoading.visibility = View.GONE

                if (response.error == false) {
                    val resetLink = response.resetlink
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resetLink))
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@AccountDetailActivity,
                        "Gagal mengirim tautan reset",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.progressBarLoading.visibility = View.GONE
                Toast.makeText(
                    this@AccountDetailActivity,
                    "Kesalahan: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}


