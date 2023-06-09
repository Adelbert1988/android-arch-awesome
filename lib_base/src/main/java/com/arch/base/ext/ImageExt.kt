@file:JvmName("ImageExt")
@file:Suppress("unused")

package com.guadou.lib_baselib.ext

/**
 *  多媒体文件通过MediaStore操作
 *  Android Q
 */

import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.guadou.lib_baselib.utils.log.YYLogUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

/**
 * 保存File到图库 保存Input流到图库 保存Bitmap到图库
 */
private val ALBUM_DIR = Environment.DIRECTORY_PICTURES

private class OutputFileTaker(var file: File? = null)

/**
 * 复制图片文件到相册的Pictures文件夹
 *
 * @param context 上下文
 * @param fileName 文件名。 需要携带后缀
 * @param relativePath 相对于Pictures的路径
 */
fun File.copyToAlbum(context: Context, fileName: String, relativePath: String?): Uri? {
    if (!this.canRead() || !this.exists()) {
        YYLogUtils.w("check: read file error: $this")
        return null
    }
    return this.inputStream().use {
        it.saveToAlbum(context, fileName, relativePath)
    }
}

/**
 * 保存图片Stream到相册的Pictures文件夹
 *
 * @param context 上下文
 * @param fileName 文件名。 需要携带后缀
 * @param relativePath 相对于Pictures的路径
 */
fun InputStream.saveToAlbum(context: Context, fileName: String, relativePath: String?): Uri? {
    val resolver = context.contentResolver
    val outputFile = OutputFileTaker()
    val imageUri = resolver.insertMediaImage(fileName, relativePath, outputFile)
    if (imageUri == null) {
        YYLogUtils.w("insert: error: uri == null")
        return null
    }

    (imageUri.outputStream(resolver) ?: return null).use { output ->
        this.use { input ->
            input.copyTo(output)
            imageUri.finishPending(context, resolver, outputFile.file)
        }
    }
    return imageUri
}

/**
 * 保存Bitmap到相册的Pictures文件夹
 *
 * https://developer.android.google.cn/training/data-storage/shared/media
 *
 * @param context 上下文
 * @param fileName 文件名。 需要携带后缀
 * @param relativePath 相对于Pictures的路径
 * @param quality 质量
 */
fun Bitmap.saveToAlbum(
    context: Context,
    fileName: String,
    relativePath: String? = null,
    quality: Int = 75,
): Uri? {
    // 插入图片信息
    val resolver = context.contentResolver
    val outputFile = OutputFileTaker()
    val imageUri = resolver.insertMediaImage(fileName, relativePath, outputFile)
    if (imageUri == null) {
        YYLogUtils.w("insert: error: uri == null")
        return null
    }

    // 保存图片
    (imageUri.outputStream(resolver) ?: return null).use {
        val format = fileName.getBitmapFormat()
        this@saveToAlbum.compress(format, quality, it)
        imageUri.finishPending(context, resolver, outputFile.file)
    }
    return imageUri
}

//根据Uri打开一个输出流 - 准备写入文件
private fun Uri.outputStream(resolver: ContentResolver): OutputStream? {
    return try {
        resolver.openOutputStream(this)
    } catch (e: FileNotFoundException) {
        YYLogUtils.w("save: open stream error: $e")
        null
    }
}

// 文件 / 流 /Bitmap 写入完成之后，通知媒体库更新
private fun Uri.finishPending(
    context: Context,
    resolver: ContentResolver,
    outputFile: File?,
) {
    val imageValues = ContentValues()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        if (outputFile != null) {
            imageValues.put(MediaStore.Images.Media.SIZE, outputFile.length())
        }
        resolver.update(this, imageValues, null, null)
        // 通知媒体库更新
        val intent = Intent(@Suppress("DEPRECATION") Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, this)
        context.sendBroadcast(intent)
    } else {
        // Android Q添加了IS_PENDING状态，为0时其他应用才可见
        imageValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(this, imageValues, null, null)
    }
}

private fun String.getBitmapFormat(): Bitmap.CompressFormat {
    val fileName = this.toLowerCase(Locale.getDefault())
    return when {
        fileName.endsWith(".png") -> Bitmap.CompressFormat.PNG
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> Bitmap.CompressFormat.JPEG
        fileName.endsWith(".webp") -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.PNG
    }
}

private fun String.getMimeType(): String? {
    val fileName = this.toLowerCase(Locale.getDefault())
    return when {
        fileName.endsWith(".png") -> "image/png"
        fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
        fileName.endsWith(".webp") -> "image/webp"
        fileName.endsWith(".gif") -> "image/gif"
        else -> null
    }
}

/**
 * 重要方法插入图片到媒体库
 */
private fun ContentResolver.insertMediaImage(
    fileName: String,
    relativePath: String?,
    outputFileTaker: OutputFileTaker? = null,
): Uri? {
    // 图片信息
    val imageValues = ContentValues().apply {
        val mimeType = fileName.getMimeType()
        if (mimeType != null) {
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        }
        val date = System.currentTimeMillis() / 1000
        put(MediaStore.Images.Media.DATE_ADDED, date)
        put(MediaStore.Images.Media.DATE_MODIFIED, date)
    }
    // 保存的位置
    val collection: Uri
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val path = if (relativePath != null) "${ALBUM_DIR}/${relativePath}" else ALBUM_DIR
        imageValues.apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.RELATIVE_PATH, path)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        // 高版本不用查重直接插入，会自动重命名
    } else {
        // 老版本
        val pictures =
            @Suppress("DEPRECATION") Environment.getExternalStoragePublicDirectory(ALBUM_DIR)
        val saveDir = if (relativePath != null) File(pictures, relativePath) else pictures

        if (!saveDir.exists() && !saveDir.mkdirs()) {
            YYLogUtils.e("save: error: can't create Pictures directory")
            return null
        }

        // 文件路径查重，重复的话在文件名后拼接数字
        var imageFile = File(saveDir, fileName)
        val fileNameWithoutExtension = imageFile.nameWithoutExtension
        val fileExtension = imageFile.extension

        var queryUri = this.queryMediaImage28(imageFile.absolutePath)
        var suffix = 1
        while (queryUri != null) {
            val newName = fileNameWithoutExtension + "(${suffix++})." + fileExtension
            imageFile = File(saveDir, newName)
            queryUri = this.queryMediaImage28(imageFile.absolutePath)
        }

        imageValues.apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
            // 保存路径
            val imagePath = imageFile.absolutePath
            YYLogUtils.w("save file: $imagePath")
            put(@Suppress("DEPRECATION") MediaStore.Images.Media.DATA, imagePath)
        }
        outputFileTaker?.file = imageFile// 回传文件路径，用于设置文件大小
        collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
    // 插入图片信息
    return this.insert(collection, imageValues)
}

/**
 * Android Q以下版本，查询媒体库中当前路径是否存在
 * @return Uri 返回null时说明不存在，可以进行图片插入逻辑
 */
private fun ContentResolver.queryMediaImage28(imagePath: String): Uri? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return null

    val imageFile = File(imagePath)
    if (imageFile.canRead() && imageFile.exists()) {
        YYLogUtils.w("query: path: $imagePath exists")
        // 文件已存在，返回一个file://xxx的uri
        return Uri.fromFile(imageFile)
    }
    // 保存的位置
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // 查询是否已经存在相同图片
    val query = this.query(
        collection,
        arrayOf(MediaStore.Images.Media._ID, @Suppress("DEPRECATION") MediaStore.Images.Media.DATA),
        "${@Suppress("DEPRECATION") MediaStore.Images.Media.DATA} == ?",
        arrayOf(imagePath), null
    )
    query?.use {
        while (it.moveToNext()) {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val id = it.getLong(idColumn)
            val existsUri = ContentUris.withAppendedId(collection, id)
            YYLogUtils.w("query: path: $imagePath exists uri: $existsUri")
            return existsUri
        }
    }
    return null
}

