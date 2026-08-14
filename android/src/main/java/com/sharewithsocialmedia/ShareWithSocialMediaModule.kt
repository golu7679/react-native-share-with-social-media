package com.sharewithsocialmedia

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

data class PackageItem(
  val packageName: String, val handlingClass: String
)


object PackageListType {
  val INSTAGRAM = PackageItem(
    packageName = "com.instagram.android",
    handlingClass = "com.instagram.direct.share.handler.DirectShareHandlerActivity"
  )
  val SNAPCHAT = PackageItem(
    packageName = "com.snapchat.android", handlingClass = ""
  )
  val WHATSAPP = PackageItem(
    packageName = "com.whatsapp", handlingClass = ""
  )
  val TELEGRAM = PackageItem(
    packageName = "org.telegram.messenger", handlingClass = ""
  )
}


@ReactModule(name = ShareWithSocialMediaModule.NAME)
class ShareWithSocialMediaModule(var reactContext: ReactApplicationContext) :
  NativeShareWithSocialMediaSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  override fun open(
    type: String?, text: String?, promise: Promise
  ) {
    try {
      when (type) {
        "instagramDm" -> {
          if (!requireApp(PackageListType.INSTAGRAM.packageName, "Instagram", promise)) return

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setComponent(
            ComponentName(
              PackageListType.INSTAGRAM.packageName, PackageListType.INSTAGRAM.handlingClass
            )
          )
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          intentDirect.type = "text/plain"
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)

          launch(intentDirect, "Instagram Direct share handler is not available", promise)
        }

        "snapchat" -> {
          if (!requireApp(PackageListType.SNAPCHAT.packageName, "Snapchat", promise)) return

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.type = "text/plain"
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          intentDirect.setPackage(PackageListType.SNAPCHAT.packageName)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          launch(intentDirect, "Snapchat is not installed", promise)
        }

        "telegram" -> {
          if (!requireApp(PackageListType.TELEGRAM.packageName, "Telegram", promise)) return

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.TELEGRAM.packageName)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)

          launch(intentDirect, "Telegram is not installed", promise)
        }

        "sms" -> {
          val intentDirect = Intent(Intent.ACTION_SENDTO)
          intentDirect.data = "smsto:".toUri()
          intentDirect.putExtra("sms_body", text)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          launch(intentDirect, "SMS share handler is not available", promise)
        }

        "whatsapp" -> {
          if (!requireApp(PackageListType.WHATSAPP.packageName, "Whatsapp", promise)) return

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.WHATSAPP.packageName)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          launch(intentDirect, "Whatsapp is not installed", promise)
        }

        else -> promise.reject("INVALID_TYPE", errorMap("Invalid type provided"))
      }
    } catch (_: ActivityNotFoundException) {
      promise.reject("SOMETHING_WENT_WRONG", errorMap("Something went wrong"))
    }
  }

  private fun errorMap(message: String) = Arguments.createMap().apply {
    putString("error", message)
    putInt("code", 500)
  }

  /**
   * Rejects and sends the user to the Play Store when [packageName] is missing.
   * Returns true when the caller may continue.
   */
  private fun requireApp(packageName: String, label: String, promise: Promise): Boolean {
    if (isAppInstalled(reactContext, packageName)) return true
    openAppInPlayStore(reactContext, packageName)
    promise.reject("NOT_INSTALLED", errorMap("$label is not installed. Redirected to Play Store."))
    return false
  }

  private fun launch(intent: Intent, unavailableMessage: String, promise: Promise) {
    if (reactContext.packageManager.resolveActivity(intent, 0) == null) {
      promise.reject("NOT_INSTALLED", errorMap(unavailableMessage))
      return
    }
    startActivity(reactContext, intent, null)
    promise.resolve(null)
  }

  override fun shareStory(options: ReadableMap?, promise: Promise) {
    if (options == null) {
      promise.reject("INVALID_OPTIONS", "Options cannot be null")
      return
    }

    if (!requireApp(PackageListType.INSTAGRAM.packageName, "Instagram", promise)) return

    thread {
      try {
        val intent = Intent("com.instagram.share.ADD_TO_STORY")
        intent.setPackage(PackageListType.INSTAGRAM.packageName)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        var primaryUri: Uri? = null

        // Resolve Background (Priority 1 for Main Data)
        if (options.hasKey("backgroundImage") && !options.getString("backgroundImage").isNullOrEmpty()) {
          options.getString("backgroundImage")?.let {
            primaryUri = resolveImageUri(it)
          }
        }

        // Resolve Sticker (Priority 2 for Main Data if no Background)
        var stickerUri: Uri? = null
        if (options.hasKey("stickerImage") && !options.getString("stickerImage").isNullOrEmpty()) {
          options.getString("stickerImage")?.let {
             stickerUri = resolveImageUri(it)
          }
        }

        // Set the primary data for the intent
        if (primaryUri != null) {
          intent.setDataAndType(primaryUri, "image/*")
        } else if (stickerUri != null) {
          // If no background, the sticker becomes the primary data
          intent.setDataAndType(stickerUri, "image/*")
        } else {
          // Fallback if neither are provided (though validation should prevent this)
          intent.type = "image/*"
        }

        // Add "Trust" parameters for clickable links
        intent.putExtra("source_application", reactContext.packageName)
        if (options.hasKey("facebookAppId")) {
          val appId = options.getString("facebookAppId")
          intent.putExtra("facebook_app_id", appId)
          intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", appId)
        }

        // Always set the sticker asset keys if we have a sticker
        if (stickerUri != null) {
          intent.putExtra("interactive_asset_uri", stickerUri)
          intent.putExtra("sticker_asset", stickerUri)
        }

        // Handle Link
        if (options.hasKey("attributionLink")) {
          val link = options.getString("attributionLink")
          // Redundant keys for maximum compatibility
          intent.putExtra("content_url", link)
          intent.putExtra("attribution_link", link)
          intent.putExtra("attribution_url", link)
          intent.putExtra("link_sticker", link)
          intent.putExtra("com.facebook.platform.extra.STORY_ATTRIBUTION_URL", link)
        }

        // Handle Colors
        if (options.hasKey("backgroundTopColor")) {
          intent.putExtra("top_background_color", options.getString("backgroundTopColor"))
        }
        if (options.hasKey("backgroundBottomColor")) {
          intent.putExtra("bottom_background_color", options.getString("backgroundBottomColor"))
        }

        // Check if Instagram can handle this intent
        if (reactContext.packageManager.resolveActivity(intent, 0) == null) {
          promise.reject("NOT_SUPPORTED", "Instagram Stories sharing is not supported on this device or version")
          return@thread
        }

        // FLAG_GRANT_READ_URI_PERMISSION only covers intent.data, never URIs passed as
        // extras, so the sticker has to be granted to Instagram explicitly.
        listOfNotNull(primaryUri, stickerUri).forEach { uri ->
          reactContext.grantUriPermission(
            PackageListType.INSTAGRAM.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
          )
        }

        startActivity(reactContext, intent, null)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("SHARE_ERROR", "[ShareWithSocialMedia] " + e.message, e)
      }
    }
  }

  private fun contentUriFor(file: File): Uri = FileProvider.getUriForFile(
    reactContext, "${reactContext.packageName}$FILE_PROVIDER_SUFFIX", file
  )

  private fun resolveImageUri(imagePath: String): Uri {
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
      return downloadImageToCache(imagePath)
    }

    if (imagePath.startsWith("content://")) {
      return imagePath.toUri()
    }

    val file = File(imagePath.replace("file://", ""))

    return try {
      contentUriFor(file)
    } catch (e: Exception) {
      throw Exception("[ShareWithSocialMedia] Could not resolve file path: $imagePath. Error: ${e.message}")
    }
  }

  private fun downloadImageToCache(urlStr: String): Uri {
    val connection = URL(urlStr).openConnection() as HttpURLConnection
    connection.connectTimeout = 10000
    connection.readTimeout = 10000

    try {
      connection.connect()

      if (connection.responseCode != HttpURLConnection.HTTP_OK) {
        throw Exception("[ShareWithSocialMedia] Failed to download image. Response code: ${connection.responseCode}")
      }

      val cacheFile = File(reactContext.cacheDir, "share_story_temp_${System.currentTimeMillis()}")

      connection.inputStream.use { input ->
        cacheFile.outputStream().use { output ->
          input.copyTo(output)
        }
      }

      return contentUriFor(cacheFile)
    } finally {
      connection.disconnect()
    }
  }

  private fun isAppInstalled(context: ReactContext, packageName: String): Boolean {
    return try {
      context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: Exception) {
      println(e)
      false
    }
  }

  private fun openAppInPlayStore(context: Context, packageName: String) {
    try {
      val marketIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
      marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(marketIntent)
    } catch (_: ActivityNotFoundException) {
      val webIntent = Intent(Intent.ACTION_VIEW,
        "https://play.google.com/store/apps/details?id=$packageName".toUri())
      webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(webIntent)
    }
  }

  companion object {
    const val NAME = "ShareWithSocialMedia"

    /**
     * Appended to the host app's package id. Deliberately library-specific: a plain
     * ".fileprovider" collides with the authority most apps already declare, and two
     * providers sharing an authority fail the manifest merge.
     */
    const val FILE_PROVIDER_SUFFIX = ".sharewithsocialmedia.fileprovider"
  }
}
