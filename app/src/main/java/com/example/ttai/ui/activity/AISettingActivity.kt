package com.example.ttai.ui.activity

import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.unit.Constraints
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.MyBluetoothManager
import com.example.ttai.R
import com.example.ttai.databinding.ActivityAiSettingBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.AiModel
import com.example.ttai.bean.Character
import com.example.ttai.event.BuyShopEvent
import com.example.ttai.ui.dialog.AiModelSelectionDialogFragment
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.event.ClearChatEvent
import com.example.ttai.event.PermanentMemoryEvent
import com.example.ttai.intent.AISettingIntent
import com.example.ttai.intent.ShopOwnedFragmentIntent
import com.example.ttai.myenum.WorkMode
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.AISettingState
import com.example.ttai.ui.dialog.ScrollPickerDialog
import com.example.ttai.utils.Constants
import com.example.ttai.utils.ToastUtils
import com.example.ttai.ui.vm.AISettingViewModel
import com.example.ttai.ui.vm.AISettingViewModelFactory
import com.google.android.material.snackbar.Snackbar
import org.greenrobot.eventbus.EventBus

class AISettingActivity : BaseMviActivity<AISettingIntent, AISettingState, AISettingViewModel, ActivityAiSettingBinding>() {
    override val viewModel: AISettingViewModel by viewModels { 
        AISettingViewModelFactory(NetworkModule.provideApiService(), this)
    }
    override val binding by viewBinding { ActivityAiSettingBinding.inflate(it) }

    var character : Character? = null
    
    // 注册Activity结果回调
    private val searchBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedDevice = result.data?.getParcelableExtra<BluetoothDevice>("selected_device")
            selectedDevice?.let { device ->
                // 处理选中的蓝牙设备
                binding.tvDeviceName.text = device.name ?: device.address
                binding.gpLinkBlueTooth.visibility = android.view.View.VISIBLE
                Snackbar.make(binding.root, "已选择设备: ${device.name ?: device.address}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun setupViews() {
        try {
            EventBus.getDefault().register(this)
            Log.d("AISettingActivity", "EventBus注册成功")
        } catch (e: Exception) {
            Log.w("AISettingActivity", "EventBus注册失败: ${e.message}")
        }

        character = intent.getParcelableExtra<Character>(Constants.CHARACTER_KEY)
        character?.let {
            viewModel.characterId = character?.id.toString()
            binding.tvName.text = character?.name
            Glide.with(binding.ivIcon.context)
                .load(character?.avatarUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .transform(CenterCrop(), CircleCrop())
                .into(binding.ivHeard)
        }

        binding.iv1.setOnClickListener {
            // 处于开启状态 则是去关闭
            if(viewModel.state.value.isPermanentMemoryEnabled){
                showPermanentMemoryDialog()
            }else{
                // 处于关闭状态 并且没有记忆之芯，则去商店
                viewModel.state.value.profileData?.permanentMemoryBenefits?.availableBenefits?.let {
                    if (viewModel.state.value.profileData?.permanentMemoryBenefits?.availableBenefits!! < 1) {
                        showToShopDialog()
                    }else{
                        showPermanentMemoryDialog()
                    }
                }?:{ showToShopDialog()}
            }
        }
        binding.iv2.setOnClickListener {
            showRemoveFromSlotDialog()
        }
        binding.llyReload.setOnClickListener {
            showClearHistoryConfirmDialog()
        }
        binding.tvChatSetting.setOnClickListener {
            val intent = Intent(this, MyChatSettingActivity::class.java)
            intent.putExtra(Constants.CHARACTER_ID_KEY,viewModel.characterId)
            startActivity(intent)
        }

        binding.tvBranchManager.setOnClickListener {
            val intent = Intent(this, BranchManagerActivity::class.java)
            intent.putExtra(Constants.CHARACTER_ID_KEY,viewModel.characterId)
            intent.putExtra(Constants.CONVERSATION_ID_KEY,character?.conversationId)
            startActivity(intent)
        }

        binding.tvVoiceConfig.setOnClickListener {
            val intent = Intent(this, VoiceConfigListActivity::class.java)
            intent.putExtra(Constants.CHARACTER_ID_KEY,viewModel.characterId)
            startActivity(intent)
        }
        binding.tvProtManager.setOnClickListener {
            showModePicker()
        }

        binding.tvLinkBlueTooth.setOnClickListener {
            // 跳转到蓝牙搜索页面
            val intent = Intent(this, BlueTestActivity::class.java)
            searchBluetoothLauncher.launch(intent)
        }

        binding.tvModel.setOnClickListener {
            startActivity( Intent(this, ChatModelListActivity::class.java))
        }

        binding.tvMemoryBook.setOnClickListener {
            val intent = Intent(this, EditeMemoryBookActivity::class.java)
            intent.putExtra(Constants.CHARACTER_ID_KEY,viewModel.characterId)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        sendIntent(AISettingIntent.LoadData)
        sendIntent(AISettingIntent.LoadUserProfile)
        sendIntent(AISettingIntent.LoadAiSettings)
        sendIntent(AISettingIntent.LoadPermanentMemoryStatus)
    }

    override fun render(state: AISettingState) {
        super.render(state)

        if (state.isPermanentMemoryEnabled){
            // 更新开关状态
            binding.iv1.setImageResource(R.mipmap.icon_switch_select)
            EventBus.getDefault().post(PermanentMemoryEvent())
        }else{
            binding.iv1.setImageResource(R.mipmap.icon_switch_normal)
            EventBus.getDefault().post(PermanentMemoryEvent())
        }
        
        binding.iv2.setImageResource(
            if (state.isDoubleReplyEnabled) 
                R.mipmap.icon_switch_select
            else 
                R.mipmap.icon_switch_normal
        )


        if (state.currentModelItem!=null) {
            binding.tvModel.text = state.currentModelItem.name
        }

        if (state.clearChatSuccess){
            EventBus.getDefault().post(ClearChatEvent())
            ToastUtils.showShort(this,"重置AI对话成功")
        }

    }

    private fun showPermanentMemoryDialog() {
        val currentState = viewModel.state.value.isPermanentMemoryEnabled
        var message =""
        if (!currentState){
            message = "是否赠予该角色记忆之芯以\n 开通永久记忆？"
        }else{
            message = "关闭后将损失该角色的所有\n 记忆库并返还记忆之芯"
        }

        val dialog = TwoButtonDialogFragment.newInstance(
            message = message,
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                sendIntent(AISettingIntent.TogglePermanentMemory(!currentState))
            }

            override fun onNegativeClick() {
            }
        })
        dialog.show(supportFragmentManager, "RemoveFromSlotDialog")
    }

    private fun showToShopDialog() {

        var message = "您需购买记忆之芯卡才能开通此功\n能，是否前往商店购买？"

        val dialog = TwoButtonDialogFragment.newInstance(
            message = message,
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                val intent = Intent(this@AISettingActivity, ShopActivity::class.java)
                startActivity(intent)
            }

            override fun onNegativeClick() {
            }
        })
        dialog.show(supportFragmentManager, "RemoveFromSlotDialog")
    }

    private fun showModelSelectionDialog() {
        val currentState = viewModel.state.value
        val availableModels = currentState.aiSettings?.availableModels ?: emptyList()
        
        if (availableModels.isNotEmpty()) {
            val dialog =  AiModelSelectionDialogFragment.newInstance(
                title = "选择AI模型",
                message = "请选择您想要使用的AI模型",
                models = availableModels
            )
            
            dialog.setOnModelSelectedListener(object : AiModelSelectionDialogFragment.OnModelSelectedListener {
                override fun onModelSelected(model: AiModel) {
                    // 更新选中的模型
                    sendIntent(AISettingIntent.UpdateSelectedModel(model))
                }
            })
            
            dialog.show(supportFragmentManager, "AiModelSelectionDialogFragment")
        } else {
            Snackbar.make(binding.root, "暂无可用模型", Snackbar.LENGTH_SHORT).show()
        }
    }
    private fun showClearHistoryConfirmDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "确定要清除与该AI的所有聊天记录吗？\n此操作不可恢复。",
            positiveText = "确定清除",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 执行清除聊天历史操作
                sendIntent(AISettingIntent.ClearChatHistory)
            }

            override fun onNegativeClick() {
                // 取消操作，不做任何处理
            }
        })
        dialog.show(supportFragmentManager, "ClearHistoryDialog")
    }

    fun showModePicker() {
        val picker = ScrollPickerDialog(
            onConfirm = { mode ->
                // 这里根据标识执行逻辑，而不是根据文案
                MyBluetoothManager.workMode = mode
            },
            onCancel = {
                // 用户取消了
            }
        )
        picker.show(supportFragmentManager, "mode_picker")
    }

    /**
     * 显示从槽位移除的确认对话框
     */
    private fun showRemoveFromSlotDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "功能还在开发中",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
            }

            override fun onNegativeClick() {
            }
        })
        dialog.show(supportFragmentManager, "RemoveFromSlotDialog")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            EventBus.getDefault().unregister(this)
            Log.d("AISettingActivity", "EventBus注销成功")
        } catch (e: Exception) {
            Log.w("AISettingActivity", "EventBus注销失败: ${e.message}")
        }
    }

    /**
     * 购买商品成功
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onBuyShopEvent(event: BuyShopEvent?) {
        sendIntent(AISettingIntent.LoadUserProfile)
    }
}