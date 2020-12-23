package com.lncp.speed

import android.content.Context
import android.os.Environment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.toast
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

fun Context.UploadFile(filepath: String? = null, filename: String) {
    val uuid: String = UUID.randomUUID().toString()
    lateinit var file: File
    val NewLine = "\r\n"
    val spec = "http://10.57.1.185:8080/upload"

    if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
        file = File(getExternalFilesDir(filepath), filename)
    else {
        applicationContext.toast("SD卡不存在或者不可读写")
        return
    }

    var fis: FileInputStream? = null
    var bos: DataOutputStream? = null
    var bis: DataInputStream? = null
    val url = URL(spec)
    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
    //打开输出
    connection.setDoOutput(true)
    //打开输入
    connection.setDoInput(true)
    //关闭缓存
    connection.setUseCaches(false)
    //读取超时
    connection.setReadTimeout(50 * 1000)
    //连接超时
    connection.setConnectTimeout(5 * 1000)
    //请求方式POST
    connection.setRequestMethod("POST")
    //设置请求头
//			connection.setRequestProperty("Connection", "Keep-Alive");
//connection.addRequestProperty("user-agent","Mozilla/5.0 (Linux; U; Android 4.4.2; zh-cn; NX507J Build/KVT49L) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
    //必须设置，数据类型，编码方式，分界线
    connection.setRequestProperty(
        "Content-Type",
        "multipart/form-data; charset=utf-8; boundary=$uuid"
    )
    //connection.setRequestProperty("accept-encoding","gzip");
    connection.setChunkedStreamingMode(1024 * 50)
    bos = DataOutputStream(connection.getOutputStream())

    if (file.exists()) {
        fis = FileInputStream(file)
        val buff = ByteArray(1024)
        bis = DataInputStream(fis)
        var cnt = 0
        //数据以--BOUNDARY开始
        bos.write("--$uuid".toByteArray())
        //换行
        bos.write(NewLine.toByteArray())
        //内容描述信息
        val content =
            "Content-Disposition: form-data; name=\"" + filename.toString() + "\"; filename=\"" + file.name + "\""
        bos.write(content.toByteArray())
        bos.write(NewLine.toByteArray())
//				bos.write("Content-Transfer-Encoding: binary".getBytes());
//				bos.write(NewLine.getBytes());
        bos.write(NewLine.toByteArray())
        //空一行后，开始通过流传输文件数据
        while (bis.read(buff).also { cnt = it } != -1) {
            bos.write(buff, 0, cnt)
        }
        bos.write(NewLine.toByteArray())
        //结束标志--BOUNDARY--
        bos.write("--$uuid--".toByteArray())
        bos.write(NewLine.toByteArray())
        bos.flush()
    }
    //开始发送请求，获取请求码和请求结果
    if (connection.getResponseCode() === HttpURLConnection.HTTP_OK) {
        connection.getInputStream()
        System.out.println("url=" + connection.getURL())
        val read = BufferedReader(InputStreamReader(connection.getInputStream(), "utf-8"))
        var dat: String? = null
        while (read.readLine().also { dat = it } != null) {
            println(dat)
        }
        println("请求成功")
    } else {
        System.err.println(
            "请求失败" + connection.getResponseMessage()
                .toString() + "code=" + connection.getResponseCode()
        )
    }
}
