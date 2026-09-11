package com.example.mayaassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AssistantCore {

    data class Result(val reply: String, val action: (() -> Unit)? = null)

    fun handle(context: Context, raw: String): Result {
        val c = raw.lowercase(Locale.getDefault()).trim()

        return when {
            c.contains("youtube") || c.contains("ইউটিউব") ->
                Result("ইউটিউব খুলছি।") {
                    open(context, "https://www.youtube.com")
                }

            c.contains("facebook") || c.contains("ফেসবুক") ->
                Result("ফেসবুক খুলছি।") {
                    open(context, "https://www.facebook.com")
                }

            c.contains("google") || c.contains("গুগল") -> {
                val q = c.replace("google", "")
                    .replace("গুগল", "")
                    .replace("search", "")
                    .replace("সার্চ", "")
                    .trim()
                if (q.isBlank()) Result("গুগল খুলছি।") {
                    else Result("গুগলে সার্চ করছি।") {
                        open(context, "https://www.google.com/search?q=${Uri.encode(q)}")
                    }
            }

            c.contains("time") || c.contains("কয়টা বাজে") ||
                    c.contains("কয়টা বাজে") || c.contains("সময়") ->
                Result("এখন সময় " +
                    SimpleDateFormat("hটা mm মিনিট", Locale("bn", "BD")).format(Date()))

            c.contains("hello") || c.contains("হ্যালো") || c.contains("হাই") ->
                Result("হ্যালো! আমি মায়া। কী করতে পারি?")

            c.contains("who are you") || c.contains("তুমি কে") ->
                Result("আমি মায়া, তোমার Android voice assistant।")

            else ->
                Result("এই কমান্ডটা এখনো শেখানো হয়নি। YouTube, Facebook, Google search বা সময় জিজ্ঞেস করে চেষ্টা করো।")
        }
    }

    private fun open(context: Context, url: String) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
