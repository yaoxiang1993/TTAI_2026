package com.example.ttai.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.MyBluetoothManager
import com.example.ttai.R
import com.example.ttai.utils.ToastUtils

class BluetoothDeviceAdapter(
    private val onDeviceClick: ((BluetoothDevice) -> Unit)? = null,
    private val onToggleClick: ((BluetoothDevice,String) -> Unit)? = null
) : ListAdapter<BluetoothDevice, BluetoothDeviceAdapter.DeviceViewHolder>(DeviceDiffCallback()) {

    public var connectedAddresses:  String = ""

    public fun updateConnectedAddresses(addresses: String) {
        connectedAddresses = addresses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_blue_tooth, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = getItem(position)
        val isConnected = connectedAddresses == device.address
        holder.bind(device, isConnected)

        holder.itemView.setOnClickListener {
            onDeviceClick?.invoke(device)
        }

        holder.toggleView.setOnClickListener {
            onToggleClick?.invoke(device,connectedAddresses)
            if (device.address == connectedAddresses) {
                MyBluetoothManager.cleanup()
                updateConnectedAddresses("")
            } else {
                MyBluetoothManager.pairDevice(device)
                ToastUtils.showShort(holder.toggleView.context,"设备连接中请勿退出")
            }
        }
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
        val toggleView: ImageView = itemView.findViewById(R.id.tvSelected)

        fun bind(device: BluetoothDevice, isConnected: Boolean) {
            val deviceName = device.name ?: device.address ?: "Unknown Device"
            tvDeviceName.text = deviceName
            toggleView.setImageResource(if (isConnected) R.mipmap.icon_switch_select else R.mipmap.icon_switch_normal)
        }
    }

    class DeviceDiffCallback : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem == newItem
        }
    }
}