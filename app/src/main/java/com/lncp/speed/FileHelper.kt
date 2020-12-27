package com.lncp.speed

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast
import threeDvector.pvat
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/*
 * 使用GSON将对象JSON序列化以及反序列化
 * */
fun serialize(src: Any): String = Gson().toJson(src)
fun serialize(vararg srcs: Any): String = Gson().toJson(srcs)
inline fun <reified T> deserialize(json: String): T = Gson().fromJson<T>(
    json,
    object : TypeToken<T>() {}.type
)

/*
 * 这里定义的是一个文件保存的方法，写入到文件中，所以是输出流
 * */
fun Context.FileSave(fileContent: String, filepath: String? = null, filename: String) {
    //这里我们使用私有模式,创建出来的文件只能被本应用访问,还会覆盖原文件哦
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val myExternalFile: File = File(getExternalFilesDir(filepath), filename)
        try {
            //verifyStoragePermissions()
            val fileOutPutStream = FileOutputStream(myExternalFile)
            fileOutPutStream.write(fileContent.toByteArray())
            fileOutPutStream.close()
            applicationContext.toast("数据写入成功")      //直接省略写toast即this.toast也是可以的，但是可能会内存外泄
        } catch (e: IOException) {
            e.printStackTrace()
            applicationContext.toast("数据写入失败")
        }
    } else applicationContext.toast("SD卡不存在或者不可读写")
}

fun Context.FileSave(fileContent: ByteArray, filepath: String? = null, filename: String) {
    //这里我们使用私有模式,创建出来的文件只能被本应用访问,还会覆盖原文件哦
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val myExternalFile: File = File(getExternalFilesDir(filepath), filename)
        try {
            //verifyStoragePermissions()
            val fileOutPutStream = FileOutputStream(myExternalFile)
            fileOutPutStream.write(fileContent)
            fileOutPutStream.close()
            applicationContext.toast("数据写入成功")      //直接省略写toast即this.toast也是可以的，但是可能会内存外泄
        } catch (e: IOException) {
            e.printStackTrace()
            applicationContext.toast("数据写入失败")
        }
    } else applicationContext.toast("SD卡不存在或者不可读写")
}

fun Context.FileSave(fileContent: List<pvat>, filepath: String? = null, filename: String) {
    //这里我们使用私有模式,创建出来的文件只能被本应用访问,还会覆盖原文件哦
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val myExternalFile: File = File(getExternalFilesDir(filepath), filename)
        try {
            //verifyStoragePermissions()
            val fileOutPutStream = FileOutputStream(myExternalFile)
            for (tmp in fileContent) fileOutPutStream.write(toByteArray(tmp))

            fileOutPutStream.close()
            applicationContext.toast("数据写入成功")      //直接省略写toast即this.toast也是可以的，但是可能会内存外泄
        } catch (e: IOException) {
            e.printStackTrace()
            applicationContext.toast("数据写入失败")
        }
    } else applicationContext.toast("SD卡不存在或者不可读写")
}

/*
 * 这里定义的是文件读取的方法
 * */
fun Context.FileLoad(filepath: String? = null, filename: String): String? {
    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        val myExternalFile: File = File(getExternalFilesDir(filepath), filename)
        try {
            //verifyStoragePermissions()
            var fileInputStream = FileInputStream(myExternalFile)
            var inputStreamReader: InputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder: StringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                stringBuilder.append(text)
            }
            fileInputStream.close()
            return stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    } else applicationContext.toast("SD卡不存在或者不可读写")
    return null
}

fun Context.UploadFile(uploadUrl: String?, filepath: String? = null, filename: String): String {
    if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) return "SD卡不存在或者不可读写"
    val uploadFile: File = File(getExternalFilesDir(filepath), filename)
    val end = "\r\n"
    val Hyphens = "--"
    val uuid = UUID.randomUUID().toString()
    return try {
        val url = URL(uploadUrl)
        val conn = url.openConnection() as HttpURLConnection

        conn.connectTimeout = 5 * 1000
        conn.readTimeout = 5 * 1000

        conn.doInput = true
        conn.doOutput = true
        conn.useCaches = false
        conn.requestMethod = "POST"

        //conn.instanceFollowRedirects = true;

        conn.setRequestProperty("Connection", "Keep-Alive")
        conn.setRequestProperty("Charset", "UTF-8")
        conn.setRequestProperty(
            "Content-Type",
            "multipart/form-data;boundary=$uuid"
        )
        //conn.setChunkedStreamingMode(1024 * 50)
        val bos = DataOutputStream(conn.outputStream)
        bos.writeBytes(Hyphens + uuid + end)
        bos.writeBytes(
            "Content-Disposition: form-data; "
                    + "name=\"MotionRecord\";filename=\"" + filename + "\"" + end
        )
        bos.writeBytes(end)
        val fStream = FileInputStream(uploadFile)
        // 每次写入1024字节
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var length = -1
        // 将文件数据写入到缓冲区
        while (fStream.read(buffer).also { length = it } != -1) {
            bos.write(buffer, 0, length)
        }
        bos.writeBytes(end)
        bos.writeBytes(Hyphens + uuid + Hyphens + end)
        fStream.close()
        bos.flush()
        // 获取返回内容
        val bis = conn.inputStream
        var ch: Int
        val b = StringBuffer()
        while (bis.read().also { ch = it } != -1) {
            b.append(ch.toChar())
        }
        bos.close()
        conn.disconnect()
        "上传成功"
    } catch (e: Exception) {
        e.printStackTrace()
        "上传失败:" + e.message
    }
}
