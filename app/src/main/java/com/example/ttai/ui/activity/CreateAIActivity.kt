package com.example.ttai.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import com.example.ttai.utils.ToastUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ttai.R
import kotlinx.coroutines.launch
import com.example.ttai.databinding.ActivityCreateAiBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Character
import com.example.ttai.bean.PersonalityTag
import com.example.ttai.event.AICreatedEvent
import com.example.ttai.event.SwitchToMyFragmentEvent
import com.example.ttai.intent.CreateAIIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.utils.Constants
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.state.CreateAIState
import com.example.ttai.utils.SafeGlideUtils
import com.example.ttai.ui.vm.CreateAIViewModel
import com.example.ttai.ui.vm.CreateAIViewModelFactory
import com.example.ttai.utils.ImageUploadUtil
import com.example.ttai.utils.GalleryPickerUtil
import com.example.ttai.utils.JsonUtils
import com.example.ttai.utils.SystemGalleryPickerUtil
import org.greenrobot.eventbus.EventBus

/**
 * 创建智能体界面
 */
class CreateAIActivity : BaseMviActivity<CreateAIIntent, CreateAIState, CreateAIViewModel, ActivityCreateAiBinding>() {
    
    override val viewModel: CreateAIViewModel by viewModels { CreateAIViewModelFactory(this) }
    override val binding by viewBinding { ActivityCreateAiBinding.inflate(it) }
    
    // 存储当前已设置的标签列表，用于避免重复设置
    private var currentApiTags: List<PersonalityTag> = emptyList()
    
    // 编辑模式相关变量
    private var isEditMode = false
    private var editMyAI: Character? = null

    // 图片选择工具类
    private lateinit var systemGalleryPickerUtil: SystemGalleryPickerUtil
    override fun setupViews() {
        // 检查是否为编辑模式
        checkEditMode()
        
        // 初始化图片选择工具类
        systemGalleryPickerUtil = SystemGalleryPickerUtil.createForActivity(this) { bitmap ->
            // 上传图片到服务器
            uploadImage(bitmap)
        }
        
        // 设置点击外部区域隐藏键盘
        KeyboardManager.setupHideKeyboardOnTouchOutside(this)
        
        // 设置返回按钮
        binding.ivBack.setOnClickListener {
            sendIntent(CreateAIIntent.Back)
        }

        // 设置图片上传点击事件
        binding.ivUploadImg.setOnClickListener {
            systemGalleryPickerUtil.openSystemGallery()
        }

        binding.tvClearTag.setOnClickListener {
            sendIntent(CreateAIIntent.ClearAllTags)
        }

        // 设置创建智能体按钮
        binding.tvCreateAI.setOnClickListener {

            if (viewModel.state.value.imageUri.isNullOrEmpty()){
                ToastUtils.showShort(this, "请选择图片")
                return@setOnClickListener
            }
            if (binding.edtName.text.isNullOrEmpty()){
                ToastUtils.showShort(this, "请输入昵称")
                return@setOnClickListener
            }
            if (binding.edtSetting.text.isNullOrEmpty()){
                ToastUtils.showShort(this, "请输入智能体设定")
                return@setOnClickListener
            }
            if (binding.edtIntroduce.text.isNullOrEmpty()){
                ToastUtils.showShort(this, "请输入对外简介")
                return@setOnClickListener
            }
            if (binding.edtPrologue.text.isNullOrEmpty()){
                ToastUtils.showShort(this, "请输入开场白")
                return@setOnClickListener
            }
            if ((viewModel.state.value.selectedTags.size + viewModel.state.value.customTags.size) < 1) {
                ToastUtils.showShort(this, "请选择标签")
                return@setOnClickListener
            }


            android.util.Log.d("CreateAIActivity", "创建按钮被点击，当前启用状态: ${binding.tvCreateAI.isEnabled}")
            if (binding.tvCreateAI.isEnabled) {
                android.util.Log.d("CreateAIActivity", "发送CreateAI Intent")
                
                // 立即设置按钮为创建中状态，防止重复点击
                binding.tvCreateAI.text = if (isEditMode) "编辑中..." else "创建中..."
                binding.tvCreateAI.isEnabled = false
                
                sendIntent(CreateAIIntent.CreateAI(editMyAI?.id))
            } else {
                android.util.Log.w("CreateAIActivity", "按钮未启用，无法创建")
                ToastUtils.showShort(this, "请填写完整信息后再创建")
            }
        }
        sendIntent(CreateAIIntent.CharactersTags)
        setupTextWatchers()
        setupGenderSelection()
        setupCustomTagDialog()
        setupPublishTypeSelection()
        setupUnlimitedSelection()
    }
    
    override fun render(state: CreateAIState) {
        android.util.Log.d("CreateAIActivity", "render调用: createSuccess=${state.createSuccess}, isCreating=${state.isCreating} ")
        
        // 更新字符计数
        binding.tvNameLength.text = "${state.nameLength}/30"
        binding.tvSettingLength.text = "${state.settingLength}/10000"
        binding.tvIntroduceLength.text = "${state.introduceLength}/1000"
        binding.tvPrologueLength.text = "${state.prologueLength}/1000"
        
        // 更新性别选择状态
        updateGenderSelection(state.gender)
        
        // 更新动态标签布局（只有当标签列表发生变化时才重新设置）
        if (state.tags.isNotEmpty() && state.tags != currentApiTags) {
            android.util.Log.d("CreateAIActivity", "标签列表发生变化，重新设置动态标签")
            setupDynamicTags(state.tags)
            currentApiTags = state.tags
        } else if (state.tags.isNotEmpty()) {
            android.util.Log.d("CreateAIActivity", "标签列表未变化，跳过重新设置。当前动态标签数量: ${dynamicTagViews.size}")
        }
        
        // 更新标签选择状态（确保动态标签存在）
        if (dynamicTagViews.isNotEmpty() || dynamicCustomTagViews.isNotEmpty()) {
            updateTagSelection(state.selectedTags)
        } else {
            android.util.Log.w("CreateAIActivity", "没有动态标签可更新，跳过选择状态更新")
        }
        
        // 更新自定义标签
        updateCustomTags(state.customTags)
        
        // 显示自定义标签对话框
        if (state.showCustomTagDialog) {
            showCustomTagDialog()
        }
        
        // 更新创建按钮状态
        binding.tvCreateAI.isEnabled = state.isCreateButtonEnabled
        binding.tvCreateAI.text = when {
            state.isCreating -> {
                if (isEditMode) "更新中..." else "创建中..."
            }   else -> {
                if (isEditMode) "更新智能体" else "创建智能体"
            }
        }
        binding.tvTitle.text = if (isEditMode) "编辑智能体" else "创建智能体"

        // 更新发布类型选择状态
        updatePublishTypeSelection(state.isPrivate)
        // 更新发布类型选择状态
        updateUnlimitedSelection(state.isUnlimited)
        
                 // 处理创建结果
         if (state.createSuccess ) {
             val successMessage = if (isEditMode) "智能体编辑成功！" else "智能体创建成功！"
             android.util.Log.d("CreateAIActivity", successMessage)
             ToastUtils.showShort(this, successMessage)
             
             // 设置结果，通知调用者AI创建成功
             val resultIntent = Intent()
             resultIntent.putExtra("ai_created", true)
             resultIntent.putExtra("switch_to_my_fragment", true)
             setResult(android.app.Activity.RESULT_OK, resultIntent)
             
             // 发送EventBus事件通知其他组件（延迟发送）
             try {
                 android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                     try {
                          EventBus.getDefault().post(
                             AICreatedEvent(createSuccessAIID = state.createSuccessAIID )
                         )
                         android.util.Log.d("CreateAIActivity", "AICreatedEvent事件发送成功")
                         
                         // 发送切换到MyFragment的事件
                          EventBus.getDefault().post(
                              SwitchToMyFragmentEvent()
                         )
                         android.util.Log.d("CreateAIActivity", "切换到MyFragment事件发送成功")
                     } catch (e: Exception) {
                         android.util.Log.w("CreateAIActivity", "EventBus事件发送失败: ${e.message}")
                     }
                 }, 200) // 延迟200ms，确保MainActivity已经准备好
                 android.util.Log.d("CreateAIActivity", "已安排延迟发送EventBus事件")
             } catch (e: Exception) {
                 android.util.Log.w("CreateAIActivity", "安排延迟发送EventBus事件失败: ${e.message}")
             }
             
             finish()
         }
        
        // 处理错误
        if (state.errorMessage != null && state.errorMessage != lastErrorMessage) {
            // 新的错误消息，显示Toast
            ToastUtils.showShort(this, state.errorMessage)
            lastErrorMessage = state.errorMessage
            
            // 创建失败时恢复按钮状态
            if (!state.isCreating) {
                resetCreateButton()
            }
        } else if (state.errorMessage == null && lastErrorMessage != null) {
            // 错误消息被清除，重置错误状态跟踪
            lastErrorMessage = null
        }
    }
    
    private fun setupTextWatchers() {
        // 昵称输入框字符计数
        binding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendIntent(CreateAIIntent.UpdateName(s?.toString() ?: ""))
            }
        })
        
        // 智能体设定输入框字符计数
        binding.edtSetting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendIntent(CreateAIIntent.UpdateSetting(s?.toString() ?: ""))
            }
        })
        
        // 对外简介输入框字符计数
        binding.edtIntroduce.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendIntent(CreateAIIntent.UpdateIntroduce(s?.toString() ?: ""))
            }
        })
        
        // 开场白输入框字符计数
        binding.edtPrologue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendIntent(CreateAIIntent.UpdatePrologue(s?.toString() ?: ""))
            }
        })
    }
    
    private fun setupGenderSelection() {
        // 设置性别选择
        binding.tvMale.setOnClickListener {
            sendIntent(CreateAIIntent.SelectGender(Constants.MALE))
        }
        
        binding.tvFemale.setOnClickListener {
            sendIntent(CreateAIIntent.SelectGender(Constants.FEMALE))
        }

        binding.tvOther.setOnClickListener {
            sendIntent(CreateAIIntent.SelectGender(Constants.OTHER))
        }
    }
    
    private fun updateGenderSelection(gender: String) {
        // 重置所有按钮为未选中状态
        binding.tvMale.apply {
            background = getDrawable(R.drawable.bg_stroke_545454_r50)
            setTextColor(getColor(R.color.color_545454))
        }
        binding.tvFemale.apply {
            background = getDrawable(R.drawable.bg_stroke_545454_r50)
            setTextColor(getColor(R.color.color_545454))
        }
        binding.tvOther.apply {
            background = getDrawable(R.drawable.bg_stroke_545454_r50)
            setTextColor(getColor(R.color.color_545454))
        }
        
        // 设置选中按钮的样式
        when (gender) {
            Constants.MALE -> binding.tvMale.apply {
                background = getDrawable(R.drawable.bg_stroke_primary_r50)
                setTextColor(getColor(R.color.primary_color))
            }
            Constants.FEMALE -> binding.tvFemale.apply {
                background = getDrawable(R.drawable.bg_stroke_primary_r50)
                setTextColor(getColor(R.color.primary_color))
            }
            Constants.OTHER -> binding.tvOther.apply {
                background = getDrawable(R.drawable.bg_stroke_primary_r50)
                setTextColor(getColor(R.color.primary_color))
            }
        }
    }

    // 存储动态创建的标签视图，用于选择状态更新
    private val dynamicTagViews = mutableListOf<android.widget.TextView>()
    // 存储动态创建的自定义标签视图
    private val dynamicCustomTagViews = mutableListOf<android.widget.TextView>()
    
    /**
     * 设置动态标签布局
     * API标签每行4个，均分宽度
     */
    private fun setupDynamicTags(tags: List<PersonalityTag>) {
        // 清除之前的动态标签
        clearDynamicTags()
        
        // API标签完全动态创建，不再需要处理固定标签
        
        val columnCount = 4  // 每行固定4列
        
        // 动态创建API标签视图
        tags.forEachIndexed { index, tag ->
            val row = index / columnCount
            val column = index % columnCount
            
            val tagView = android.widget.TextView(this).apply {
                text = tag.name
                setBackgroundResource(R.drawable.bg_select_tag_selector)
                setTextColor(resources.getColorStateList(R.color.selector_primary_ffffff, null))
                setPadding(
                    (8 * resources.displayMetrics.density).toInt(), // 16dp
                    (4 * resources.displayMetrics.density).toInt(),  // 8dp
                    (8 * resources.displayMetrics.density).toInt(), // 16dp
                    (4 * resources.displayMetrics.density).toInt()   // 8dp
                )
                textSize = 12f
                gravity = android.view.Gravity.CENTER
                setOnClickListener {
                    sendIntent(CreateAIIntent.SelectTag(tag.name))
                }
            }
            
            val params = androidx.gridlayout.widget.GridLayout.LayoutParams().apply {
                width = 0 // 使用0dp让GridLayout根据weight分配宽度
                height = androidx.gridlayout.widget.GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (4 * resources.displayMetrics.density).toInt()  // 4dp
                )
                rowSpec = androidx.gridlayout.widget.GridLayout.spec(row)
                columnSpec = androidx.gridlayout.widget.GridLayout.spec(column, 1f) // 每列权重为1，均分宽度
            }
            
            tagView.layoutParams = params
            binding.tagsGridLayout.addView(tagView)
            dynamicTagViews.add(tagView)
            
            android.util.Log.d("CreateAIActivity", "创建API标签: ${tag.name} at ($row, $column)")
        }
        
        // 计算自定义标签区域的起始行
        val apiTagRowCount = (tags.size + columnCount - 1) / columnCount // 向上取整
        setupCustomTagsArea(apiTagRowCount)
        
        android.util.Log.d("CreateAIActivity", "共创建${tags.size}个API标签，占用${apiTagRowCount}行")
    }
    
    /**
     * 设置自定义标签区域
     * 在API标签下方预留动态自定义标签位置
     */
    private fun setupCustomTagsArea(startRow: Int) {
        // 设置添加按钮的初始位置
        val addButtonParams = binding.btnAddCustomTag.layoutParams as androidx.gridlayout.widget.GridLayout.LayoutParams
        addButtonParams.width = 0 // 使用0dp让GridLayout根据weight分配宽度
        addButtonParams.rowSpec = androidx.gridlayout.widget.GridLayout.spec(startRow)
        addButtonParams.columnSpec = androidx.gridlayout.widget.GridLayout.spec(0, 1f) // 在第一列
        binding.btnAddCustomTag.layoutParams = addButtonParams
        binding.btnAddCustomTag.visibility = android.view.View.VISIBLE
        
        android.util.Log.d("CreateAIActivity", "设置自定义标签区域起始位置: $startRow")
    }
    
    /**
     * 清除之前创建的动态标签
     */
    private fun clearDynamicTags() {
        android.util.Log.d("CreateAIActivity", "清除${dynamicTagViews.size}个API动态标签")
        dynamicTagViews.forEach { tagView ->
            binding.tagsGridLayout.removeView(tagView)
        }
        dynamicTagViews.clear()
    }
    
    /**
     * 清除之前创建的动态自定义标签
     */
    private fun clearDynamicCustomTags() {
        android.util.Log.d("CreateAIActivity", "清除${dynamicCustomTagViews.size}个自定义动态标签")
        dynamicCustomTagViews.forEach { tagView ->
            binding.tagsGridLayout.removeView(tagView)
        }
        dynamicCustomTagViews.clear()
    }
    
    private fun updateTagSelection(selectedTags: Set<String>) {
        android.util.Log.d("CreateAIActivity", "更新标签选择状态，API标签数量: ${dynamicTagViews.size}, 自定义标签数量: ${dynamicCustomTagViews.size}, 选中标签: $selectedTags")
        
        // 更新动态创建的API标签选择状态
        dynamicTagViews.forEach { tagView ->
            val isSelected = selectedTags.contains(tagView.text.toString())
            tagView.isSelected = isSelected
            android.util.Log.d("CreateAIActivity", "API标签 '${tagView.text}' 选中状态: $isSelected")
        }
        
        // 更新动态创建的自定义标签选择状态
        dynamicCustomTagViews.forEach { tagView ->
            val isSelected = selectedTags.contains(tagView.text.toString())
            tagView.isSelected = isSelected
            android.util.Log.d("CreateAIActivity", "自定义标签 '${tagView.text}' 选中状态: $isSelected")
        }
    }
    
    private fun setupCustomTagDialog() {
        // 设置添加自定义标签按钮点击事件
        binding.btnAddCustomTag.setOnClickListener {
            sendIntent(CreateAIIntent.ShowCustomTagDialog(true))
        }
    }
    
    private fun showCustomTagDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom_tag, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        val edtCustomTag = dialogView.findViewById<EditText>(R.id.edtCustomTag)
        val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)
        val tvConfirm = dialogView.findViewById<TextView>(R.id.tvConfirm)
        
        // 设置对话框样式
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // 取消按钮
        tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // 确定按钮
        tvConfirm.setOnClickListener {
            val tagText = edtCustomTag.text.toString().trim()
            if (tagText.isNotEmpty()) {
                sendIntent(CreateAIIntent.AddCustomTag(tagText))
                dialog.dismiss()
            } else {
                ToastUtils.showShort(this, "请输入标签内容")
            }
        }
        
        // 设置软键盘完成按钮
        edtCustomTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                val tagText = edtCustomTag.text.toString().trim()
                if (tagText.isNotEmpty()) {
                    sendIntent(CreateAIIntent.AddCustomTag(tagText))
                    dialog.dismiss()

                } else {
                    ToastUtils.showShort(this, "请输入标签内容")
                }
                true
            } else {
                false
            }
        }
        dialog.setOnDismissListener {
            sendIntent(CreateAIIntent.ShowCustomTagDialog(false))
        }
        
        dialog.show()
        
        // 自动弹出软键盘
        KeyboardManager.showSoftInput(this, edtCustomTag)
    }
    
    private fun updateCustomTags(customTags: List<String>) {
        // 清除之前创建的动态自定义标签
        clearDynamicCustomTags()
        
        val columnCount = 4
        // 计算API标签占用的行数，自定义标签从下一行开始
        val apiTagRowCount = (dynamicTagViews.size + columnCount - 1) / columnCount
        val customTagStartRow = apiTagRowCount
        
        // 动态创建自定义标签（最多10个）
        customTags.take(10).forEachIndexed { index, tag ->
            val row = customTagStartRow + (index / columnCount)
            val column = index % columnCount
            
            val tagView = TextView(this).apply {
                text = tag
                setBackgroundResource(R.drawable.bg_select_tag_selector)
                setTextColor(getColor(R.color.primary_color))
                
                // 添加右侧删除图标
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.icon_delete_1, 0)
                compoundDrawablePadding = (1 * resources.displayMetrics.density).toInt() // 4dp间距
                
                setPadding(
                    (8 * resources.displayMetrics.density).toInt(), // 8dp
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (8 * resources.displayMetrics.density).toInt(), // 8dp
                    (4 * resources.displayMetrics.density).toInt()  // 4dp
                )
                textSize = 12f
                gravity = android.view.Gravity.CENTER_VERTICAL
                
                // 设置触摸监听器来区分点击区域
                setOnTouchListener { view, event ->
                    if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                        lastTouchX = event.x
                        android.util.Log.d("CreateAIActivity", "记录触摸位置: ${event.x}")
                    }
                    false // 返回false让onClick继续处理
                }
                
                // 点击标签选择/取消选择
                setOnClickListener { view ->
                    handleCustomTagClick(view, tag)
                }
            }
            
            val params = androidx.gridlayout.widget.GridLayout.LayoutParams().apply {
                width = 0 // 使用0dp让GridLayout根据weight分配宽度
                height = androidx.gridlayout.widget.GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (4 * resources.displayMetrics.density).toInt(), // 4dp
                    (4 * resources.displayMetrics.density).toInt()  // 4dp
                )
                rowSpec = androidx.gridlayout.widget.GridLayout.spec(row)
                columnSpec = androidx.gridlayout.widget.GridLayout.spec(column, 1f) // 均分宽度
            }
            
            tagView.layoutParams = params
            binding.tagsGridLayout.addView(tagView)
            dynamicCustomTagViews.add(tagView)
            
            android.util.Log.d("CreateAIActivity", "创建自定义标签: $tag at ($row, $column)")
        }
        
        // 更新添加按钮的位置
        updateAddButtonPosition(customTags.size)
        
        android.util.Log.d("CreateAIActivity", "共创建${customTags.size}个自定义标签")
    }
    
    /**
     * 更新添加按钮位置
     */
    private fun updateAddButtonPosition(customTagCount: Int) {
        val columnCount = 4
        val apiTagRowCount = (dynamicTagViews.size + columnCount - 1) / columnCount
        val totalCustomTagPositions = customTagCount
        
        if (customTagCount < 10) {
            // 还可以添加，显示添加按钮
            val addButtonRow = apiTagRowCount + (totalCustomTagPositions / columnCount)
            val addButtonColumn = totalCustomTagPositions % columnCount
            
            val params = binding.btnAddCustomTag.layoutParams as androidx.gridlayout.widget.GridLayout.LayoutParams
            params.width = 0 // 使用0dp让GridLayout根据weight分配宽度
            params.rowSpec = androidx.gridlayout.widget.GridLayout.spec(addButtonRow)
            params.columnSpec = androidx.gridlayout.widget.GridLayout.spec(addButtonColumn, 1f)
            binding.btnAddCustomTag.layoutParams = params
            binding.btnAddCustomTag.visibility = android.view.View.VISIBLE
            
            android.util.Log.d("CreateAIActivity", "添加按钮位置: ($addButtonRow, $addButtonColumn)")
        } else {
            // 已达到最大数量，隐藏添加按钮
            binding.btnAddCustomTag.visibility = android.view.View.GONE
            android.util.Log.d("CreateAIActivity", "自定义标签已达最大数量(10个)，隐藏添加按钮")
        }
    }
    
    /**
     * 处理自定义标签点击事件
     * 区分点击文本区域（选择）和点击图标区域（删除）
     */
    private fun handleCustomTagClick(view: android.view.View, tag: String) {
        val textView = view as android.widget.TextView
        
        // 获取drawable区域
        val drawables = textView.compoundDrawables
        val drawableRight = drawables[2] // 右侧图标
        
        if (drawableRight != null) {
            // 获取TextView的总宽度和图标宽度
            val textViewWidth = textView.width
            val drawableWidth = drawableRight.intrinsicWidth
            val drawablePadding = textView.compoundDrawablePadding
            
            // 计算图标区域的左边界（从右边开始算）
            val iconLeftBound = textViewWidth - textView.paddingEnd - drawableWidth - drawablePadding
            
            android.util.Log.d("CreateAIActivity", "标签点击: $tag, TouchX: $lastTouchX, IconLeft: $iconLeftBound, TextViewWidth: $textViewWidth")
            
            if (lastTouchX > iconLeftBound) {
                // 点击了图标区域，删除标签
                android.util.Log.d("CreateAIActivity", "点击删除图标: $tag")
                sendIntent(CreateAIIntent.RemoveCustomTag(tag))
            } else {
                // 点击了文本区域，选择标签
                android.util.Log.d("CreateAIActivity", "点击标签文本: $tag")
                sendIntent(CreateAIIntent.SelectTag(tag))
            }
        } else {
            // 没有图标，直接选择
            sendIntent(CreateAIIntent.SelectTag(tag))
        }
    }
    
    // 存储最后一次触摸的X坐标
    private var lastTouchX = 0f
    
    // 跟踪上一次的错误状态，用于检测错误消息清除
    private var lastErrorMessage: String? = null
    
    // 上传进度弹框
    private var uploadProgressDialog: android.app.AlertDialog? = null
    
    // 图片上传任务
    private var uploadJob: kotlinx.coroutines.Job? = null


    /**
     * 设置发布类型选择
     */
    private fun setupPublishTypeSelection() {
        // 设置私密选项点击事件
        binding.tvPrivate.setOnClickListener {
            sendIntent(CreateAIIntent.SetPublishType(true))
        }

        // 设置公开选项点击事件
        binding.tvPublic.setOnClickListener {
            sendIntent(CreateAIIntent.SetPublishType(false))
        }
    }

    /**
     * 设置发布类型选择
     */
    private fun setupUnlimitedSelection() {
        // 设置私密选项点击事件
        binding.tvYes.setOnClickListener {
            sendIntent(CreateAIIntent.SetUnlimitedType(true))
        }

        // 设置公开选项点击事件
        binding.tvNO.setOnClickListener {
            sendIntent(CreateAIIntent.SetUnlimitedType(false))
        }
    }

    /**
     * 更新发布类型选择状态
     */
    private fun updatePublishTypeSelection(isPrivate: Boolean) {
        if (isPrivate) {
            // 私密选中状态 - 使用 icon_publis_type_1 (选中图标)
            binding.tvPrivate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_1, 0, 0, 0)

            // 公开未选中状态 - 使用 icon_publis_type_2 (未选中图标)
            binding.tvPublic.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_2, 0, 0, 0)
        } else {
            // 私密未选中状态 - 使用 icon_publis_type_2 (未选中图标)
            binding.tvPrivate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_2, 0, 0, 0)

            // 公开选中状态 - 使用 icon_publis_type_1 (选中图标)
            binding.tvPublic.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_1, 0, 0, 0)
        }

        android.util.Log.d("CreateAIActivity", "更新发布类型状态: isPrivate = $isPrivate")
    }
    /**
     * 更新是否无限制
     */
    private fun updateUnlimitedSelection(isUnlimited: Boolean) {
        if (isUnlimited) {
            // 是无限制 - 使用 icon_publis_type_1 (选中图标)
            binding.tvYes.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_1, 0, 0, 0)

            // 不是无限制 - 使用 icon_publis_type_2 (未选中图标)
            binding.tvNO.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_2, 0, 0, 0)
        } else {
            // 是无限制 - 使用 icon_publis_type_1 (选中图标)
            binding.tvYes.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_2, 0, 0, 0)
            // 不是无限制 - 使用 icon_publis_type_2 (未选中图标)
            binding.tvNO.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_publis_type_1, 0, 0, 0)
        }

        android.util.Log.d("CreateAIActivity", "更新 是否无限制 状态: isUnlimited = $isUnlimited")
    }

        /**
     * 上传图片到服务器
     */
    private fun uploadImage(bitmap: android.graphics.Bitmap) {
        // 取消之前的上传任务（如果存在）
        uploadJob?.cancel()
        
        // 显示上传进度弹框
        showUploadProgressDialog()
        
        // 创建新的上传任务
        uploadJob = lifecycleScope.launch {
            try {
                // 创建MultipartBody.Part
                val imagePart = ImageUploadUtil.createImagePart(this@CreateAIActivity, bitmap)
                if (imagePart == null) {
                    ToastUtils.showShort(this@CreateAIActivity, "图片处理失败，请重试")
                    hideUploadProgressDialog()
                    return@launch
                }
                // 检查图片宽高比例
                checkImageAspectRatio(bitmap)
                // 调用上传接口
                val apiService = NetworkModule.provideApiService()
                val response = apiService.uploadImage(imagePart)
                
                if (response.code == 200 && response.data != null) {
                    // 上传成功，使用返回的URL设置图片
                    val imageUrl = response.data.imageUrl
                    android.util.Log.d("CreateAIActivity", "图片上传成功: $imageUrl")
                    // 使用安全的Glide加载方式
                   SafeGlideUtils.loadImageSafely(
                        context = this@CreateAIActivity,
                        imageView = binding.ivUploadImg,
                        imageUrl = imageUrl
                    )
                    
                    // 通知ViewModel保存图片URL
                    viewModel.updateImageUri(imageUrl)
                    
                    ToastUtils.showShort(this@CreateAIActivity, "图片上传成功")
                } else {
                    ToastUtils.showShort(this@CreateAIActivity, "图片上传失败: ${response.message}")
                }
                
            } catch (e: Exception) {
                // 检查是否是取消异常
                if (e is kotlinx.coroutines.CancellationException) {
                    android.util.Log.d("CreateAIActivity", "图片上传任务被取消")
                    return@launch
                }
                android.util.Log.e("CreateAIActivity", "图片上传异常", e)
                ToastUtils.showShort(this@CreateAIActivity, "图片上传失败: ${e.message}")
            } finally {
                // 隐藏上传进度弹框
                hideUploadProgressDialog()
                // 清理临时文件
              ImageUploadUtil.cleanTempFiles(this@CreateAIActivity)
                // 清除任务引用
                uploadJob = null
            }
        }
    }
    
    /**
     * 检查图片宽高比例是否为9:16
     */
    private fun checkImageAspectRatio(bitmap: android.graphics.Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        
        // 计算宽高比
        val aspectRatio = width.toFloat() / height.toFloat()
        val targetRatio = 9f / 16f // 目标比例 9:16
        
        // 允许一定的误差范围（比如±0.1）
        val tolerance = 0.1f
        val isCorrectRatio = kotlin.math.abs(aspectRatio - targetRatio) <= tolerance
        
        if (!isCorrectRatio) {
            android.util.Log.w("CreateAIActivity", "图片宽高比例不符合要求: ${width}x${height}, 比例: $aspectRatio, 目标比例: $targetRatio")
            ToastUtils.showShort(this, "非9:16图片会导致图片变形")
        } else {
            android.util.Log.d("CreateAIActivity", "图片宽高比例符合要求: ${width}x${height}, 比例: $aspectRatio")
        }
    }
    
    /**
     * 显示上传进度弹框
     */
    private fun showUploadProgressDialog() {
        if (uploadProgressDialog?.isShowing == true) {
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_progress, null)
        uploadProgressDialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView) // 不允许用户取消
            .create()
        
        // 设置对话框样式
        uploadProgressDialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            
            // 获取屏幕宽度
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            
            // 设置弹框宽度为屏幕宽度的70%
            val dialogWidth = (screenWidth * 0.7).toInt()
            
            setLayout(
                dialogWidth,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        uploadProgressDialog?.show()
    }
    
    /**
     * 隐藏上传进度弹框
     */
    private fun hideUploadProgressDialog() {
        uploadProgressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        uploadProgressDialog = null
        
        // 如果弹框被手动关闭，也取消上传任务
        uploadJob?.let { job ->
            if (job.isActive) {
                android.util.Log.d("CreateAIActivity", "弹框被关闭，取消图片上传任务")
                job.cancel()
            }
        }
        uploadJob = null
    }
    
    /**
     * 重置上传按钮状态
     */
    private fun resetUploadButton() {
        binding.tvCreateAI.text = if (isEditMode) "编辑智能体" else "创建智能体"
        binding.tvCreateAI.isEnabled = true
    }
    
    /**
     * 重置创建按钮状态
     */
    private fun resetCreateButton() {
        binding.tvCreateAI.text = if (isEditMode) "编辑智能体" else "创建智能体"
        // 根据当前状态决定是否启用按钮
        val currentState = viewModel.state.value
        binding.tvCreateAI.isEnabled = currentState.isCreateButtonEnabled && !currentState.isCreating
        android.util.Log.d("CreateAIActivity", "重置创建按钮状态: enabled=${binding.tvCreateAI.isEnabled}")
    }
    
    /**
     * 检查是否为编辑模式并初始化数据
     */
    private fun checkEditMode() {
        // 获取传递的MyAI对象
        editMyAI = intent.getParcelableExtra("my_ai")
        if (editMyAI != null) {
            isEditMode = true
            fillEditData(editMyAI!!)
        }
    }
    
    /**
     * 填充编辑数据
     */
    private fun fillEditData(myAI: Character) {
        android.util.Log.d("CreateAIActivity", "填充编辑数据: ${JsonUtils.toJson(myAI)}")
        
        // 填充基本信息
        binding.edtName.setText(myAI.name)
        binding.edtSetting.setText(myAI.description) // MyAI中的description对应setting
        binding.edtIntroduce.setText(myAI.briefIntro ?: "") // MyAI中的briefIntro对应introduce
        binding.edtPrologue.setText(myAI.openingLine ?: "") // MyAI中的openingLine对应prologue
        sendIntent(CreateAIIntent.UpdateName(myAI.name ?: ""))
        sendIntent(CreateAIIntent.UpdateSetting(myAI.description ?: ""))
        sendIntent(CreateAIIntent.UpdateIntroduce(myAI.briefIntro ?: ""))
        sendIntent(CreateAIIntent.UpdatePrologue(myAI.openingLine ?: ""))

        // 设置性别
        myAI.gender?.let { gender ->
            sendIntent(CreateAIIntent.SelectGender(gender))
        }
        
        // 设置标签（如果有的话）
        myAI.personalityTags?.forEach { tag ->
            sendIntent(CreateAIIntent.SelectTag(tag))
        }

        // 设置发布类型（isPublic为true表示公开，false表示私密）
        val isPrivate = myAI.isPublic == false
        sendIntent(CreateAIIntent.SetPublishType(isPrivate))

        // 是否无限制（isUnlimited为true表示无限制，false表示有限制）
        val isUnlimited = myAI.is_unlimited  == true
        android.util.Log.d("CreateAIActivity", "isUnlimited: ${isUnlimited}")
        sendIntent(CreateAIIntent.SetUnlimitedType(isUnlimited))

        // 加载头像
        if (!myAI.avatarUrl.isNullOrEmpty()) {
            SafeGlideUtils.loadImageSafely(
                context = this,
                imageView = binding.ivUploadImg,
                imageUrl = myAI.avatarUrl
            )
            // 通知ViewModel保存图片URL
            viewModel.updateImageUri(myAI.avatarUrl)
        }
        // 更新按钮文案
        binding.tvCreateAI.text = "编辑智能体"
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 取消正在进行的图片上传任务
        uploadJob?.let { job ->
            if (job.isActive) {
                android.util.Log.d("CreateAIActivity", "页面关闭，取消图片上传任务")
                job.cancel()
            }
        }
        uploadJob = null
        
        // 确保上传进度弹框被关闭
        hideUploadProgressDialog()
    }

} 