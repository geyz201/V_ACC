package com.lncp.speed.ui.ZC

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lncp.speed.FileSave
import com.lncp.speed.serialize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import threeDvector.Vec3D

class ZCViewModel : ViewModel() {
    private var sum = Vec3D()
    private var count = 0
    val Ending: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>(false) }
    private lateinit var sensorManager:SensorManager
    lateinit var app:Activity

    fun ZeroCalibration()= runBlocking{
        viewModelScope.launch {
            delay(10000L)
            sensorManager.unregisterListener(AccRecorder)

            val ans = sum * (1.0 / count)
            app.applicationContext.FileSave(fileContent = serialize(ans), filename = "Avg.JSON")
            sum= Vec3D()
            count=0

            Ending.value=true;
            Ending.value=false;
        }
        viewModelScope.launch {
            delay(2000L)
            Startlistener()
        }
    }

    private fun Startlistener(){
        sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorAcc = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorManager.registerListener(AccRecorder, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL)
    }
    private val AccRecorder = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            val sensor = event.sensor

            when (sensor.type) {
                Sensor.TYPE_LINEAR_ACCELERATION -> {
                    //Data Receive from sensor
                    val tmpVec = Vec3D(event.values)
                    sum = sum + tmpVec
                    //ACCM.add(tmpVec)
                    count++
                }
            }
        }
    }


}