package rit.csh.drink.model

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.graphics.drawable.toDrawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import rit.csh.drink.R
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class ProfileImageRepository(private val context: Context) {

    private val baseImageUrl = "https://profiles.csh.rit.edu/image/"
    private val directory: File

    init {
        val wrapper = ContextWrapper(context)
        directory = wrapper.getDir("imageDir", Context.MODE_PRIVATE)
    }

    fun useUserIconDrawable(uid: String, useDrawable: (Drawable) -> Unit){
        if (userHasImage(uid)){
            val file = File(directory, "ic_profile_$uid.jpg")
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            useDrawable.invoke(bitmap.toDrawable(context.resources))
        } else {
            Picasso.get().load("$baseImageUrl$uid").into(object: Target{
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    useDrawable.invoke(context.resources.getDrawable(R.drawable.ic_profile))
                }
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let{
                        writeUserImageToFile(bitmap, uid)
                        Log.i("Profile Image", "BitmapLoaded")
                        useDrawable.invoke(bitmap.toDrawable(context.resources))
                    }
                }
            })
        }
    }

    fun deleteUserIcon(uid: String){
        val file = File(directory, "ic_profile_$uid.jpg")
        file.delete()
    }

    private fun userHasImage(uid: String): Boolean{
        val file = File(directory, "ic_profile_$uid.jpg")
        Log.i("User has image", file.exists().toString())
        return file.exists()
    }

    private fun writeUserImageToFile(bitmap: Bitmap, uid: String){
        val file = File(directory, "ic_profile_$uid.jpg")
        val outStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
    }
}