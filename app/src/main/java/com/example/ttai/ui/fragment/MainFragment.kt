package com.example.ttai.ui.fragment

import android.content.Intent
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.R
import com.example.ttai.databinding.FragmentMainBinding
import com.example.ttai.ui.activity.ChatActivity
import com.example.ttai.adapter.MixedChatListAdapter
import com.example.ttai.adapter.ChatListItem
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.ui.dialog.AddAISizeDialogFragment
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.event.PermanentMemoryEvent
import com.example.ttai.event.UserBalanceUpdateEvent
import com.example.ttai.intent.MainActivityIntent
import com.example.ttai.intent.MainFragmentIntent
import com.example.ttai.intent.SynthesizeFragmentIntent
import com.example.ttai.state.MainFragmentState
import com.example.ttai.ui.activity.NotifyListActivity
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.MainFragmentViewModel
import com.example.ttai.ui.vm.MainFragmentViewModelFactory
import com.example.ttai.utils.ToastUtils
import org.greenrobot.eventbus.EventBus

class MainFragment : BaseMviFragment<MainFragmentIntent, MainFragmentState, MainFragmentViewModel, FragmentMainBinding>() {
    override val viewModel: MainFragmentViewModel by viewModels { MainFragmentViewModelFactory(requireContext()) }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentMainBinding.inflate(inflater, container, attachToParent)
    }
    private val mixedChatListAdapter = MixedChatListAdapter(
        onConversationClick = { conversation ->
            val intent = Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra(Constants.CHARACTER_ID_KEY, conversation.characterId)
                putExtra(Constants.CONVERSATION_ID_KEY, conversation.id)
            }
            startActivity(intent)
        },
        onAddAIClick = {
            // 跳转到DreamFragment中的FeaturedFragment
            navigateToFeaturedFragment()
        }
    )
    override fun setupViews() {
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mixedChatListAdapter
            // 上拉加载更多
//            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
//                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
//                    val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
//                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                    val totalItemCount = layoutManager.itemCount
//
//                    val state = viewModel.state.value
//                    if (lastVisibleItemPosition >= totalItemCount - 3 &&
//                        !state.isLoadingMore &&
//                        !state.isRefreshing &&
//                        state.hasMoreData
//                    ) {
//                        android.util.Log.d("MainFragment", "触发加载更多: lastVisible=$lastVisibleItemPosition, total=$totalItemCount")
//                        sendIntent(MainFragmentIntent.LoadMoreData)
//                    }
//                }
//            })
        }
        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            android.util.Log.d("MainFragment", "Swipe refresh triggered")
            sendIntent(MainFragmentIntent.RefreshData)
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
        binding.tvAdd.setOnClickListener{
            showAddAISizeDialog()
        }
        binding.ivNotify.setOnClickListener{
            val intent = Intent(requireContext(), NotifyListActivity::class.java)
            startActivity(intent)
        }

        // 注册EventBus
        try {
            EventBus.getDefault().register(this)
            Log.d("MainFragment", "EventBus注册成功")
        } catch (e: Exception) {
            Log.w("MainFragment", "EventBus注册失败: ${e.message}")
        }
        
        // 发送初始化意图 
        sendIntent(MainFragmentIntent.Initialize)
    }

    private fun showDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "添加一位新的AI好友，\n" +
                    "是否继续？\n",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 处理确认逻辑
              showAddAISizeDialog()
            }

            override fun onNegativeClick() {
                // 处理取消逻辑
            }
        })
        dialog.show(childFragmentManager, "TwoButtonDialog")
    }

    private fun showAddAISizeDialog() {
        val dialog = AddAISizeDialogFragment.newInstance()
        dialog.setOnUpgradeClickListener {
            sendIntent(MainFragmentIntent.AddAISize)
        }
        dialog.show(childFragmentManager, "AddAISizeDialogFragment")
    }

    override fun render(state: MainFragmentState) {
        super.render(state)
        
        // 构建混合列表
        val mixedList = mutableListOf<ChatListItem>()
        
        // 添加普通对话
        state.conversationsData?.conversations?.forEach { conversation ->
            mixedList.add(ChatListItem.ConversationItem(conversation))
        }
        
        // 如果maxChatSlots > 0，添加"添加AI"的item
        val availableSlots = state.conversationsData?.availableSlots ?: 0
        if (availableSlots > 0) {
            // 根据maxChatSlots数量添加对应数量的AddAIItem
            repeat(availableSlots) {
                mixedList.add(ChatListItem.AddAIItem)
            }
        }
        if (state.addAISizeSuceesss == true){
            EventBus.getDefault().post(UserBalanceUpdateEvent())
            sendIntent(MainFragmentIntent.Initialize)
            viewModel.state.value.addAISizeSuceesss= false
            ToastUtils.showShort(requireContext(),"聊天槽位添加成功")
        }
        mixedChatListAdapter.submitList(mixedList)

        binding.ivNotify.setImageResource(if (state.hasUnread == true){R.mipmap.icon_notify_show}else{R.mipmap.icon_notify_normal})

        // 处理刷新指示器
        binding.swipeRefreshLayout.isRefreshing = state.isRefreshing
        
        Log.d("MainFragment", "更新列表: 对话数=${state.conversationsData?.conversations?.size ?: 0}, availableSlots=$availableSlots")
    }
    
    /**
     * 跳转到DreamFragment中的FeaturedFragment
     */
    private fun navigateToFeaturedFragment() {
        try {
            // 获取MainActivity的实例
            val mainActivity = requireActivity() as? com.example.ttai.ui.activity.MainActivity
            if (mainActivity != null) {
                // 切换到DreamFragment (index 1)，用户可以在那里选择"精选"tab
                mainActivity.sendIntent(MainActivityIntent.SwitchTab(1))
                 Log.d("MainFragment", "跳转到DreamFragment成功，用户可自行选择精选tab")
            } else {
                 Log.w("MainFragment", "无法获取MainActivity实例")
            }
        } catch (e: Exception) {
             Log.e("MainFragment", "跳转到DreamFragment失败: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // 注销EventBus
        try {
            EventBus.getDefault().unregister(this)
             Log.d("MainFragment", "EventBus注销成功")
        } catch (e: Exception) {
             Log.w("MainFragment", "EventBus注销失败: ${e.message}")
        }
    }
    
    /**
     * 处理聊天槽位变化事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onChatSlotChanged(event: ChatSlotChangedEvent) {
        Log.d("MainFragment", "收到聊天槽位变化事件: action=${event.action}")
        
        // 当聊天槽位发生变化时，重新初始化数据
        sendIntent(MainFragmentIntent.Initialize)
    }

    /**
     * 开启了记忆之芯
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onUpdateUserProfile(event: PermanentMemoryEvent) {
        sendIntent(MainFragmentIntent.Initialize)
    }
}