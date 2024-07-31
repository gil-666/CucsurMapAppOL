package com.example.cucsurmapol

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.time.Instant
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FeedbackDialogFragment(private val context: Context) : DialogFragment() {

    private lateinit var feedbackListener: FeedbackListener
    private var imageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_feedback, null)

        val nameEditText: EditText = view.findViewById(R.id.name_edit_text)
        val commentsEditText: EditText = view.findViewById(R.id.comments_edit_text)
        val screenshotImageView: ImageView = view.findViewById(R.id.screenshot_image_view)

        builder.setView(view)
            .setPositiveButton("Enviar") { dialog, _ ->
                val name = nameEditText.text.toString()
                val comments = commentsEditText.text.toString()
                val imageUri = this.imageUri

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(comments)) {
                    // Show an error or do nothing
                    return@setPositiveButton
                }

                // Send feedback to the server
                createAndUploadFeedback(name, comments, imageUri)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        // Initialize ActivityResultLaunchers
        initializeLaunchers(view)

        return builder.create()
    }

    private fun initializeLaunchers(view: View) {
        // Initialize the image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                Log.d("FeedbackDialogFragment", "Image URI: $uri") // Confirm URI
                // Ensure view is properly initialized and imageView is accessible
                val screenshotImageView: ImageView = view.findViewById(R.id.screenshot_image_view)
                try {
                    Glide.with(this)
                        .load(uri)
                        .into(screenshotImageView)
                } catch (e: Exception) {
                    Log.e("FeedbackDialogFragment", "Error loading image", e)
                }
                imageUri = uri
            } ?: run {
                Log.d("FeedbackDialogFragment", "No image selected")
            }
        }

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                imagePickerLauncher.launch("image/*")
            } else {
                Log.d("FeedbackDialogFragment", "Permission denied")
                // Handle permission denial (e.g., show a message to the user)
            }
        }

        // Set up the button to add a screenshot
        view.findViewById<Button>(R.id.add_screenshot_button).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Android 14+ - Use READ_MEDIA_IMAGES permission
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    // Launch the image picker
                    imagePickerLauncher.launch("image/*")
                }
            } else {
                // For Android versions below 14
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                } else {
                    // Launch the image picker
                    imagePickerLauncher.launch("image/*")
                }
            }
        }
    }

    private fun createAndUploadFeedback(name: String, comments: String, imageUri: Uri?) {
        // Create and save the feedback text file and append app logs
        val feedbackFile = File(requireContext().filesDir, "feedback.txt")
        val logFile = File(requireContext().filesDir, "app_log.txt")
        val zipFile = File(requireContext().filesDir, "${Date.from(Instant.now())}-$name-feedback.zip")

        FileOutputStream(feedbackFile).use { fos ->
            fos.write("Name: $name\n".toByteArray())
            fos.write("Comments: $comments\n".toByteArray())
            fos.write("DEVICE INFO: ${context?.getString(R.string.app_version)} ${Build.MANUFACTURER}:${Build.MODEL}:${Build.DEVICE}:${Build.BOOTLOADER}:${Build.DISPLAY}:${Build.VERSION.BASE_OS}:${Build.VERSION.RELEASE}:${Build.VERSION.CODENAME}:${Build.VERSION.SDK_INT}:${Build.ID}:${Build.TIME}\n".toByteArray())
            fos.flush()
        }

        // Append logs to the feedback file
        val logContent = getLogcatLogs() // Retrieve your app logs
        FileOutputStream(logFile, true).use { fos ->
            fos.write(logContent.toByteArray())
            fos.flush()
        }

        // Compress files into a zip
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            listOf(feedbackFile, logFile).forEach { file ->
                FileInputStream(file).use { fis ->
                    val zipEntry = ZipEntry(file.name)
                    zos.putNextEntry(zipEntry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
            imageUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val imageFile = File(requireContext().filesDir, "screenshot.png")
                FileOutputStream(imageFile).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }
                FileInputStream(imageFile).use { fis ->
                    val zipEntry = ZipEntry("screenshot.png")
                    zos.putNextEntry(zipEntry)
                    fis.copyTo(zos)
                    zos.closeEntry()
                }
            }
        }

        // Upload the zip file to the server
        FeedbackUploader().uploadFeedback(zipFile, "http://${DatabaseHelper.SERVER_IP}/upload/upload.php") { success, response ->
            if (success) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Los comentarios se enviaron existosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context,"Error al enviar comentarios: ${response?.split("(port")?.get(0)}",Toast.LENGTH_SHORT).show()
                }
                Log.d("Feedback", "Upload failed: $response")
            }
        }
    }

    interface FeedbackListener {
        fun onFeedbackSent()
    }

    fun setFeedbackListener(listener: FeedbackListener) {
        feedbackListener = listener
    }

    fun getLogcatLogs(): String {
        val logcatLogs = StringBuilder()
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                logcatLogs.append(line).append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return logcatLogs.toString()
    }
}
