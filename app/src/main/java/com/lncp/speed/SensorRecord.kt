package com.lncp.speed

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import threeDvector.*
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.PI

class SensorRecord : Service(), SensorEventListener {
    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var powerManager: PowerManager
    private lateinit var m_wkik: PowerManager.WakeLock

    private var Acc0 = Vec3D()


    private val sensorData = ArrayList<pvat>()

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): SensorRecord = this@SensorRecord
    }

    //速度计算
    private val SpeedCalculator = object {
        private var lastT_Acc: Long = 0
        private var lastT_GRV: Long = 0
        private lateinit var lastAcc: Vec3D
        private lateinit var lastGRV: Vec3D
        private lateinit var AccX: Vec3D //已经转换坐标系的加速度

        private var AccCount = 0
        private var AccSum = Vec3D()

        fun GRV_Update(time: Long, GRV: Vec3D) {
            if (this::lastGRV.isInitialized && this::lastAcc.isInitialized && lastT_GRV <= lastT_Acc) {
                AccX = lastAcc.Rotate(
                    Slerp(
                        lastGRV,
                        GRV,
                        (lastT_Acc - lastT_GRV).toDouble() / (time - lastT_GRV).toDouble()
                    )
                );
                AccX_Update(lastT_Acc, AccX)
            }
            lastT_GRV = time
            lastGRV = GRV
        }

        @SuppressLint("MissingPermission")
        fun Acc_Update(time: Long, Acc: Vec3D) {
            lastT_Acc = time
            lastAcc = Acc
        }

        private fun AccX_Update(time: Long, AccX: Vec3D) {
            AccCount++;
            AccSum = AccSum + AccX;
        }

        fun Acc_Clear(): Vec3D {
            val ans = if (AccCount > 0) AccSum * (1.0 / AccCount) else Vec3D()
            AccCount = 0
            AccSum = Vec3D()
            return ans
        }
    }

    val locationListener = object : LocationListener {
        override fun onProviderDisabled(provider: String) {
            toast("关闭了GPS")
        }

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onProviderEnabled(provider: String) {
            toast("打开了GPS")
        }

        @SuppressLint("MissingPermission")
        override fun onLocationChanged(location: Location) {
            val theta = GeomagneticField(
                location.latitude.toFloat(),
                location.longitude.toFloat(),
                location.altitude.toFloat(),
                location.time
            ).declination
            val Acc = SpeedCalculator.Acc_Clear().YXrotate(theta * PI / 180)
            val position = Vec3D(location.latitude, location.longitude, location.altitude)
            sensorData.add(pvat(position, location.speed, Acc, location.time))
            //applicationContext.FileSave(serialize(theta), filename = "theta.JSON")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        val sensorGRV = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        sensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorGRV, SensorManager.SENSOR_DELAY_NORMAL)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            20,
            0F,
            locationListener
        )
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20, 0F, locationListener)

        powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        m_wkik = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            SensorRecord::class.qualifiedName
        )
        m_wkik.acquire()
        //读取校零值
        val tmp = applicationContext.FileLoad(filename = "Avg.JSON")
        if (tmp != null) Acc0 = deserialize<Vec3D>(tmp)
        //定时采样
        //Timer().schedule(timerTask { mHandler.sendEmptyMessage(0x2739) }, 3_000, 1_000)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val range = 1.0 //设定一个精度范围
        val sensor = event.sensor

        when (sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                //Data Receive from sensor
                val tmpVec = Vec3D(event.values)
                SpeedCalculator.Acc_Update(event.timestamp, tmpVec - Acc0)
            }
            Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                val tmpVec = Vec3D(event.values)
                SpeedCalculator.GRV_Update(event.timestamp, tmpVec)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)
        m_wkik.release()

        applicationContext.FileSave(serialize(sensorData), filename = "SensorRecord.JSON")
    }
}