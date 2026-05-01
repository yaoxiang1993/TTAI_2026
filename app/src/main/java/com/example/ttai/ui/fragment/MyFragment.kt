package com.example.ttai.ui.fragment

import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import com.example.ttai.R
import com.example.ttai.databinding.FragmentMyBinding
import androidx.fragment.app.viewModels
import com.example.ttai.ui.activity.SettingActivity
import com.example.ttai.ui.activity.MembershipActivity
import com.example.ttai.ui.activity.ChargeMoneyActivity
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.MyFragmentIntent
import com.example.ttai.state.MyFragmentState
import com.example.ttai.ui.vm.MyFragmentViewModel
import com.example.ttai.ui.vm.MyFragmentViewModelFactory
import com.example.ttai.utils.Constants
import com.example.ttai.adapter.MyAIAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.ui.activity.CreateAIActivity
import com.example.ttai.ui.activity.EditeNameActivity
import com.example.ttai.ui.activity.ShopActivity
import com.example.ttai.bean.Character
import com.example.ttai.utils.ToastUtils
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.event.AICreatedEvent
import com.example.ttai.event.UpdateUserProfileEvent
import com.example.ttai.event.UserBalanceUpdateEvent
import com.example.ttai.network.NetworkModule
import com.example.ttai.ui.activity.ChatActivity
import com.example.ttai.ui.activity.EditeBriefActivity
import com.example.ttai.ui.activity.FollowersListActivity
import com.example.ttai.ui.activity.FollowingListActivity
import com.example.ttai.ui.activity.PayHistoryListActivity
import com.example.ttai.utils.CommontUtils
import com.example.ttai.utils.ImageUploadUtil
import com.example.ttai.utils.SystemGalleryPickerUtil
import org.greenrobot.eventbus.EventBus

class MyFragment : BaseMviFragment<MyFragmentIntent, MyFragmentState, MyFragmentViewModel, FragmentMyBinding>() {

    var bio :String?=""
    var invitationCode :String?=""

    var endDrawable: Drawable? = null

    override fun onDestroyView() {
        super.onDestroyView()
        // 注销EventBus
        try {
            EventBus.getDefault().unregister(this)
            android.util.Log.d("MyFragment", "EventBus注销成功")
        } catch (e: Exception) {
            android.util.Log.w("MyFragment", "EventBus注销失败: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        sendIntent(MyFragmentIntent.getCurrencyBalance)
        sendIntent(MyFragmentIntent.Initialize)
        sendIntent(MyFragmentIntent.LoadMyAICharacters)
        endDrawable = ContextCompat.getDrawable(requireContext(), R.mipmap.icon_edit)
    }

    /**
     * 处理AI创建成功事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onAICreated(event: AICreatedEvent) {
        android.util.Log.d("MyFragment", "收到AI创建成功事件: ${event.message}")
        if (event.success) {
            // 刷新AI角色列表
            android.util.Log.d("MyFragment", "开始刷新AI角色列表")
            sendIntent(MyFragmentIntent.LoadMyAICharacters)
        }
    }
    /**
     * 升级会员成功
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onUpdateUserProfile(event: UpdateUserProfileEvent) {
        sendIntent(MyFragmentIntent.Initialize)
    }
    /**
     * 余额发生变化
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onUserBalanceUpdate(event: UserBalanceUpdateEvent) {
        sendIntent(MyFragmentIntent.getCurrencyBalance)
    }


    override val viewModel: MyFragmentViewModel by viewModels { MyFragmentViewModelFactory(requireContext()) }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentMyBinding.inflate(inflater, container, attachToParent)
    }
    // 图片选择工具类
    private lateinit var systemGalleryPickerUtil: SystemGalleryPickerUtil
    
    // MyAI适配器
    private lateinit var myAIAdapter: MyAIAdapter
    
    // 注册Activity结果回调
    private val editNameLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // 从EditeNameActivity返回，获取新的用户名
            val newName = result.data?.getStringExtra("new_name")
            if (!newName.isNullOrEmpty()) {
                sendIntent(MyFragmentIntent.Initialize)
            }
        }
    }
    


    override fun setupViews() {
        try {
            EventBus.getDefault().register(this)
            android.util.Log.d("MyFragment", "EventBus注册成功")
        } catch (e: Exception) {
            android.util.Log.w("MyFragment", "EventBus注册失败: ${e.message}")
        }
        // 初始化图片选择工具类
        systemGalleryPickerUtil = SystemGalleryPickerUtil.createForFragment(this) { bitmap ->
            // 上传图片到服务器
            uploadImage(bitmap)
        }


        
        // 初始化MyAI适配器
        myAIAdapter = MyAIAdapter(
            onItemClick = { myAI ->
                // 点击AI角色时跳转到聊天页面
                navigateToChatActivity(myAI)
            },
            onEditClick = { myAI ->
                // 编辑AI角色
                navigateToEditActivity(myAI)
            },
            onDeleteClick = { myAI ->
                // 删除AI角色，显示确认对话框
                showDeleteConfirmDialog(myAI)
            },
            onTopClick = { myAI ->
                // 设置AI置顶
                if (myAI.isPinned==true){
                    sendIntent(MyFragmentIntent.pushUnPin(myAI.id))
                }else{
                    sendIntent(MyFragmentIntent.pushPin(myAI.id))
                }
            }
        )
        
        binding.ivSetting.setOnClickListener{
            val intent = Intent(requireContext(), SettingActivity::class.java).apply {
            }
            startActivity(intent)
        }
        
        // 为ivPhone添加点击事件，使用工具类处理图片选择
        binding.ivPhone.setOnClickListener {
            systemGalleryPickerUtil.openSystemGallery()
        }
        
        // 为tvName添加点击事件，跳转到编辑名称页面
        binding.tvName.setOnClickListener {
            val intent = Intent(requireContext(), EditeNameActivity::class.java).apply {
                // 传递当前用户名
                putExtra("current_name", binding.tvName.text.toString())
            }
            editNameLauncher.launch(intent)
        }
        
        // 为tvRenew添加点击事件，跳转到会员开通页面
        binding.tvRenew.setOnClickListener {
            val intent = Intent(requireContext(), MembershipActivity::class.java)
            startActivity(intent)
        }
        
        // 为tvChargeMoney添加点击事件，跳转到充值页面
        binding.tvChargeMoney.setOnClickListener {
            val intent = Intent(requireContext(), ChargeMoneyActivity::class.java)
            startActivity(intent)
        }

        binding.tvAddAI.setOnClickListener {
            val intent = Intent(requireContext(), CreateAIActivity::class.java)
            startActivity(intent)
        }
        binding.clyShop.setOnClickListener {
            val intent = Intent(requireContext(), ShopActivity::class.java)
            startActivity(intent)
        }
        binding.tvCopyId.setOnClickListener {
            // 复制到剪贴板
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("invitationCode", invitationCode)
            clipboard.setPrimaryClip(clip)

            // 显示Toast提示
            ToastUtils.showShort(requireContext(), "ID已复制到剪贴板")
        }
        // 设置tvId点击复制功能
        binding.tvId.setOnClickListener {
            copyIdToClipboard()
        }
        // 设置tvId点击复制功能
        binding.clyBrief.setOnClickListener {
            val intent = Intent(requireContext(), EditeBriefActivity::class.java).apply {
                // 传递当前用户名
                putExtra("current_name", bio)
            }
            editNameLauncher.launch(intent)
        }


        binding.clyFollowers.setOnClickListener {
            startActivity( Intent(requireContext(), FollowersListActivity::class.java))
        }

        binding.clyFollowing.setOnClickListener {
            startActivity( Intent(requireContext(), FollowingListActivity::class.java))
        }

        binding.tvPayList.setOnClickListener {
            startActivity( Intent(requireContext(), PayHistoryListActivity::class.java))
        }


        // 设置RecyclerView
        setupRecyclerView()
        
        // 设置下拉刷新
        setupSwipeRefresh()
        
        sendIntent(MyFragmentIntent.Initialize)
        sendIntent(MyFragmentIntent.LoadMyAICharacters)
    }

    override fun render(state: MyFragmentState) {
        super.render(state)
        
        // 添加调试日志
        android.util.Log.d("MyFragment", "Rendering state: isLoading=${state.isLoading}, userName=${state.userName}, id=${state.id}, error=${state.error}")

        // 更新用户名显示
        state.userName?.let {
            binding.tvName.text = state.userName
            android.util.Log.d("MyFragment", "Updated userName: ${state.userName}")
        }
        // 更新id显示
        state.id?.let {
            binding.tvId.text = "ID: ${state.id}"
            android.util.Log.d("MyFragment", "Updated id: ${state.id}")
        }
        
        // 更新头像 - 使用安全的图片加载工具类
        state.ivHeard?.let { avatarUrl ->
            Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.mipmap.icon_defult_avatar)
                .error(R.mipmap.icon_defult_avatar)
                .transform(CenterCrop(), CircleCrop())
                .into(binding.ivHeard)
        }
        binding.tvBrief.isVisible = true
        binding.tvBrief.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            endDrawable,                   // 设置新的 drawableEnd
            null
        )
        if (!TextUtils.isEmpty(state.brief_intro)){
            bio = state.brief_intro
            binding.tvBrief.text = state.brief_intro
            binding.tvBrief.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        }
        // 仙玉数量
        state.fairyJade?.let { fairyJade ->
            binding.tvFairyJade.text = fairyJade.toString()
        }
        // 仙贝数量
        state.fairyShells?.let { fairyShells ->
            binding.tvFairyShells.text = fairyShells.toString()
        }

        // 粉丝数量
        state.followersCount?.let { it ->
            binding.tvFans.text = it
        }
        // 粉丝数量
        state.invitationInfo?.let { it ->
            invitationCode = it.invitationCode
            binding.tvCopyId.text = "UID:${it.invitationCode}"
            binding.tvInviteSize.text = "已邀好友:${it.usedCount}/${it.max_invitations}"
            binding.tvText3.text = "(邀请好友注册TTAI双方各得${it.rewardAmount}仙玉)"
        }

        binding.ivTest.isVisible = state.is_beta_user

        // 关注数量
        state.followingCount?.let { it ->
            binding.tvFocusNumber.text = it
        }
        binding.tvProfessional.text = state.membershipInfo?.name
        binding.tvTime.text = CommontUtils.getExpiryTime(state.membershipInfo?.expiry)
        state.membershipInfo?.let {
            binding.tvRenew.text = "升级会员"
        }
        binding.tvAddAI.text = "+ 免费创建(${state.myCharacters.size}/100)"
        // 更新我的AI角色列表
        android.util.Log.d("MyFragment", "Render: 更新AI角色列表，数量=${state.myCharacters.size}")
        myAIAdapter.updateData(state.myCharacters)

        // 处理加载更多状态
        if (state.isLoadingMore) {
            android.util.Log.d("MyFragment", "显示加载更多加载")
            myAIAdapter.setLoadingMore(false)
        } else {
            myAIAdapter.setLoadingMore(false)
        }
        
        // 显示错误信息
        state.error?.let { error ->
            ToastUtils.showShort(requireContext(), error)
        }
    }
    
    /**
     * 跳转到聊天页面
     */
    private fun navigateToChatActivity(myAI: Character) {
        // 默认情况，尝试进入聊天
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(Constants.CHARACTER_ID_KEY, myAI.id)
            putExtra(Constants.CONVERSATION_ID_KEY, myAI.conversationId)
        }
        startActivity(intent)
    }
    
    /**
     * 跳转到编辑AI角色页面
     */
    private fun navigateToEditActivity(myAI: Character) {
        val intent = Intent(requireContext(), CreateAIActivity::class.java).apply {
            putExtra("my_ai", myAI)
        }
        startActivity(intent)
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(myAI: Character) {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "是否确认删除该智能体？",
            positiveText = "确定",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 确认删除
                sendIntent(MyFragmentIntent.DeleteMyAICharacter(myAI.id))
            }

            override fun onNegativeClick() {
                // 取消删除，不做任何操作
            }
        })
        dialog.show(parentFragmentManager, "delete_confirm_dialog")
    }
    
    /**
     * 显示编辑用户名的对话框
     */
    private fun showEditNameDialog() {
        val editText = EditText(requireContext()).apply {
            setText(binding.tvName.text)
            hint = "请输入用户名"
            setSingleLine()
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("修改用户名")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    sendIntent(MyFragmentIntent.UpdateUserName(newName))
                } else {
                    ToastUtils.showShort(requireContext(), "用户名不能为空")
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 设置下拉刷新
     */
    private fun setupSwipeRefresh() {
        // 设置下拉刷新
      /*  binding.swipeRefreshLayout.setOnRefreshListener {
            android.util.Log.d("MyFragment", "下拉刷新触发")
            sendIntent(MyFragmentIntent.RefreshData)
        }
        // 设置刷新颜色
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
              */
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAIAdapter
            // 添加分割线
            addItemDecoration(
                androidx.recyclerview.widget.DividerItemDecoration(
                    requireContext(),
                    androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
                )
            )
            // 添加滚动监听器
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount
                    
                    // 检查是否需要加载更多
                    if (lastVisibleItemPosition >= totalItemCount - 3 &&
                        !viewModel.state.value.isLoadingMore &&
                        !viewModel.state.value.isRefreshing &&
                        viewModel.state.value.hasMoreData
                    ) {
                        android.util.Log.d("MyFragment", "触发加载更多: lastVisible=$lastVisibleItemPosition, total=$totalItemCount")
                        sendIntent(MyFragmentIntent.LoadMoreData)
                    }
                }
            })
        }
    }
    
    /**
     * 上传图片到服务器
     */
    private fun uploadImage(bitmap: android.graphics.Bitmap) {
        lifecycleScope.launch {
            try {
                android.util.Log.d("MyFragment", "开始上传头像图片...")
                
                // 创建MultipartBody.Part
                val imagePart = ImageUploadUtil.createImagePart(
                    requireContext(), 
                    bitmap
                )
                if (imagePart == null) {
                    ToastUtils.showShort(requireContext(), "图片处理失败，请重试")
                    return@launch
                }
                
                // 调用上传接口
                val apiService = NetworkModule.provideApiService()
                val response = apiService.uploadImage(imagePart)
                
                if (response.code == 200 && response.data != null) {
                    // 上传成功，使用返回的URL更新头像
                    val imageUrl = response.data.imageUrl
                    android.util.Log.d("MyFragment", "头像图片上传成功: $imageUrl")
                    // 通知ViewModel更新用户头像
                    sendIntent(MyFragmentIntent.UpdateAvatar(imageUrl))
                    
                    ToastUtils.showShort(requireContext(), "头像更新成功")
                } else {
                    ToastUtils.showShort(requireContext(), "头像上传失败: ${response.message}")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MyFragment", "头像上传异常", e)
                ToastUtils.showShort(requireContext(), "头像上传失败: ${e.message}")
            } finally {
                // 清理临时文件
                ImageUploadUtil.cleanTempFiles(requireContext())
            }
        }
    }

    
    /**
     * 复制ID到剪贴板
     */
    private fun copyIdToClipboard() {
        val idText = binding.tvId.text.toString()
        // 提取ID部分，去掉"ID:"前缀
        val id = if (idText.startsWith("ID:")) {
            idText.substring(3).trim()
        } else {
            idText
        }
        
        // 复制到剪贴板
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("User ID", id)
        clipboard.setPrimaryClip(clip)
        
        // 显示Toast提示
        ToastUtils.showShort(requireContext(), "ID已复制到剪贴板")
    }
}