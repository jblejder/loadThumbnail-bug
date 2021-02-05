package com.bugreport.black

import android.content.ContentValues
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.bugreport.black.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("app", Context.MODE_PRIVATE) }

    private var transparentImageUri: Uri?
        set(value) {
            prefs.edit {
                putString("uri", value.toString())
            }
        }
        get() = prefs.getString("uri", null)?.let {
            Uri.parse(it)
        }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshUi()
        binding.writeBtn.setOnClickListener {
            writeImageToStorage()
            refreshUi()
        }
    }

    fun refreshUi() {
        if (transparentImageUri != null) {
            binding.imageUriTv.text = transparentImageUri.toString()
            binding.writeBtn.isEnabled = false
            loadThumbnail()
            loadOriginal()
        } else {
            binding.imageUriTv.text = "Press button to write image"
            binding.writeBtn.isEnabled = true
        }
    }

    fun writeImageToStorage() {
        val imageInputStream = assets.open("transparent/image.png")
        val mediaUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "transparent")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
        )!!
        val outputStream = contentResolver.openOutputStream(mediaUri)!!
        imageInputStream.copyTo(outputStream)
        transparentImageUri = mediaUri
    }

    private fun loadThumbnail() {
        val transparentImageUri = transparentImageUri ?: return

        val image = contentResolver.loadThumbnail(transparentImageUri, Size(1000, 1000), null)
        binding.thumbnail.setImageBitmap(image)
    }

    private fun loadOriginal() {
        val transparentImageUri = transparentImageUri ?: return

        val file = contentResolver.openFileDescriptor(transparentImageUri, "r")!!
        val image = BitmapFactory.decodeFileDescriptor(file.fileDescriptor)
        binding.real.setImageBitmap(image)
    }
}
