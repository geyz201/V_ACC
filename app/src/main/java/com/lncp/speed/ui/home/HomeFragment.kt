package com.lncp.speed.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lncp.speed.R
import com.lncp.speed.SensorRecord
import com.lncp.speed.ServiceCheckUtil
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private val model: HomeViewModel by viewModels()

    private val LOCATION_PERMISSION = 1

    var processState = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        processState =
            ServiceCheckUtil.isRunning(requireContext(), SensorRecord::class.qualifiedName)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Btn_measure.text = if (processState) "停止" else "开始采集数据"
        Btn_measure.setOnClickListener {
            if (processState) {
                val intent = Intent(requireContext(), SensorRecord::class.java)
                intent.action = "com.lncp.server.SensorRecord"
                requireActivity().stopService(intent)
                //关闭蓝牙
                //BluetoothService.cancel()

                Btn_measure.text = "开始采集数据"
            } else {
                //开启蓝牙
                //getPairedDevices()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //requestPermissions是异步执行的
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            LOCATION_PERMISSION
                        )
                        while (checkSelfPermission(requireContext(),Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //requestPermissions是异步执行的
                        requestPermissions(
                            arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ),
                            LOCATION_PERMISSION
                        )
                        while (checkSelfPermission(requireContext(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        }
                    }
                }

                val intent = Intent(requireContext(), SensorRecord::class.java)
                intent.action = "com.lncp.server.SensorRecord"
                requireActivity().startService(intent)

                Btn_measure.text = "停止"
            }
            processState = !processState
        }
    }
}