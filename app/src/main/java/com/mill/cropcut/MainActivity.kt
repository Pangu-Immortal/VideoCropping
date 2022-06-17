package com.mill.cropcut

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mill.cropcut.utils.FileUtils
import kotlinx.coroutines.*
import java.io.*
import java.nio.channels.FileChannel

/**
 * Doc说明 (此类核心功能):
 * @date on 2022/6/17 11:52
 * +--------------------------------------------+
 * | @author qihao                              |
 * | @GitHub https://github.com/Pangu-Immortal  |
 * +--------------------------------------------+
 */
private const val REQUEST_CODE_PERMISSIONS = 10
private const val REQUEST_SELECT_FILE = 11

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var srcVideo = Environment.getExternalStorageDirectory().path + "/cc.mp4"


    //获取权限弹窗操作完成回调
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectFile()//去选择文件
        }
    }


    /**
     * 请求存储权限
     */
    private fun requestWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (Environment.isExternalStorageManager()) {
                selectFile()//去选择文件
            } else {
                try {
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }.let { startActivityForResult(it, REQUEST_CODE_PERMISSIONS) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                selectFile()//去选择文件
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    REQUEST_CODE_PERMISSIONS
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FileUtils.CreateFile(srcVideo)
        findViewById<AppCompatButton>(R.id.pic_file).apply {
            setOnClickListener {
                Log.d(TAG, "onCreate: 点击")
                requestWritePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
                    toCrop(data.data!!)//选完文件以后跳转到裁剪界面
                }
            }
        }
    }

    private var someActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK
            && result.data != null && result.data!!.data != null
        ) {
            toCrop(result.data!!.data!!)//选完文件以后跳转到裁剪界面
        }
    }

    private fun selectFile() {
        val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            someActivityResultLauncher.launch(i)//Android 10以上的写法，targetAPI在31以上才这么写
        } else {
            startActivityForResult(i, REQUEST_SELECT_FILE)//Android 10以下的写法
        }
    }

    /**
     * 跳转裁剪页面
     */
    private fun toCrop(data: Uri) {
        val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)
        try {
            val cursor: Cursor? = contentResolver.query(
                data,
                filePathColumn, null, null, null
            )
            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                val videoPath: String = cursor.getString(columnIndex)
                cursor.close()

                if (copyFile(videoPath, srcVideo)){
                    startCrop(srcVideo)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取文件出错", e)
            Toast.makeText(this, "readVideoFileError", Toast.LENGTH_LONG).show()
        }
    }

    private fun startCrop(videoPath: String) {
        Intent(this, VideoCropPreActivity::class.java).apply {
            putExtra("src", videoPath)//在裁剪界面接收这个参数即可
        }.let { startActivity(it) }
    }

    private val fileName = "a23.mp4"

    /**
     * 根据文件路径拷贝文件
     * @param src 源文件
     * @param destPath目标文件路径
     * @return boolean 成功true、失败false
     */
    private fun copyFile(src: File?, destPath: String?): Boolean {
        var result = false
        if (src == null || destPath == null) {
            return result
        }
        val dest = File(destPath + fileName)
        if (dest != null && dest.exists()) {
            dest.delete() // delete file
        }
        try {
            dest.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var srcChannel: FileChannel? = null
        var dstChannel: FileChannel? = null
        try {
            srcChannel = FileInputStream(src).channel
            dstChannel = FileOutputStream(dest).channel
            srcChannel.transferTo(0, srcChannel.size(), dstChannel)
            result = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return result
        }
        try {
            srcChannel.close()
            dstChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return result
    }


    /**
     * 复制单个文件
     *
     * @param oldPathName String 原文件路径+文件名 如：data/user/0/com.test/files/abc.txt
     * @param newPathName String 复制后路径+文件名 如：data/user/0/com.test/cache/abc.txt
     * @return `true` if and only if the file was copied;
     * `false` otherwise
     */
    fun copyFile(`oldPath$Name`: String?, `newPath$Name`: String?): Boolean {
        return try {
            val oldFile = File(`oldPath$Name`)
            if (!oldFile.exists()) {
                Log.e("--Method--", "copyFile:  oldFile not exist.")
                return false
            } else if (!oldFile.isFile) {
                Log.e("--Method--", "copyFile:  oldFile not file.")
                return false
            } else if (!oldFile.canRead()) {
                Log.e("--Method--", "copyFile:  oldFile cannot read.")
                return false
            }

            /* 如果不需要打log，可以使用下面的语句
                     if (!oldFile.exists() || !oldFile.isFile() || !oldFile.canRead()) {
                         return false;
                     }
                     */
            val fileInputStream = FileInputStream(`oldPath$Name`)
            val fileOutputStream = FileOutputStream(`newPath$Name`)
            val buffer = ByteArray(1024)
            var byteRead: Int
            while (-1 != fileInputStream.read(buffer).also { byteRead = it }) {
                fileOutputStream.write(buffer, 0, byteRead)
            }
            fileInputStream.close()
            fileOutputStream.flush()
            fileOutputStream.close()
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 复制文件夹及其中的文件
     *
     * @param oldPath String 原文件夹路径 如：data/user/0/com.test/files
     * @param newPath String 复制后的路径 如：data/user/0/com.test/cache
     * @return `true` if and only if the directory and files were copied;
     * `false` otherwise
     */
    fun copyFolder(oldPath: String, newPath: String): Boolean {
        return try {
            val newFile = File(newPath)
            if (!newFile.exists()) {
                if (!newFile.mkdirs()) {
                    Log.e("--Method--", "copyFolder: cannot create directory.")
                    return false
                }
            }
            val oldFile = File(oldPath)
            val files = oldFile.list()
            var temp: File
            for (file in files) {
                temp = if (oldPath.endsWith(File.separator)) {
                    File(oldPath + file)
                } else {
                    File(oldPath + File.separator.toString() + file)
                }
                if (temp.isDirectory) {   //如果是子文件夹
                    copyFolder("$oldPath/$file", "$newPath/$file")
                } else if (!temp.exists()) {
                    Log.e("--Method--", "copyFolder:  oldFile not exist.")
                    return false
                } else if (!temp.isFile) {
                    Log.e("--Method--", "copyFolder:  oldFile not file.")
                    return false
                } else if (!temp.canRead()) {
                    Log.e("--Method--", "copyFolder:  oldFile cannot read.")
                    return false
                } else {
                    val fileInputStream = FileInputStream(temp)
                    val fileOutputStream = FileOutputStream(newPath + "/" + temp.name)
                    val buffer = ByteArray(1024)
                    var byteRead: Int
                    while (fileInputStream.read(buffer).also { byteRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, byteRead)
                    }
                    fileInputStream.close()
                    fileOutputStream.flush()
                    fileOutputStream.close()
                }

                /* 如果不需要打log，可以使用下面的语句
                     if (temp.isDirectory()) {   //如果是子文件夹
                         copyFolder(oldPath + "/" + file, newPath + "/" + file);
                     } else if (temp.exists() && temp.isFile() && temp.canRead()) {
                         FileInputStream fileInputStream = new FileInputStream(temp);
                         FileOutputStream fileOutputStream = new FileOutputStream(newPath + "/" + temp.getName());
                         byte[] buffer = new byte[1024];
                         int byteRead;
                         while ((byteRead = fileInputStream.read(buffer)) != -1) {
                             fileOutputStream.write(buffer, 0, byteRead);
                         }
                         fileInputStream.close();
                         fileOutputStream.flush();
                         fileOutputStream.close();
                     }
                      */
            }
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

}