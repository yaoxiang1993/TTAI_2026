package com.example.ttai.ui.activity

import ChatModelListViewModelFactory
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.adapter.ChatModelListAdapter
import com.example.ttai.databinding.ActivityRequestModelBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.intent.ChatModelListIntent
import com.example.ttai.state.ChatModelListState
import com.example.ttai.ui.vm.ChatModelListViewModel
import com.example.ttai.utils.ToastUtils
import com.google.android.material.snackbar.Snackbar

class ChatModelListActivity : BaseMviActivity<ChatModelListIntent, ChatModelListState, ChatModelListViewModel, ActivityRequestModelBinding>() {
    override val viewModel: ChatModelListViewModel by viewModels {
        ChatModelListViewModelFactory(this)
    }
    var chatModel: ChatModelItem? = null;

    override val binding by viewBinding { ActivityRequestModelBinding.inflate(it) }
    private val chatModelListAdapter = ChatModelListAdapter(
        onModelClick = { mode ->
            chatModel = mode
        },
    )

    override fun setupViews() {
        // 返回按钮已在BaseMviActivity中自动处理
        
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatModelListAdapter
            addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: androidx.recyclerview.widget.RecyclerView,
                    state: androidx.recyclerview.widget.RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)

                    // 设置 item 之间的垂直间隔为 5dp
                    val spacing = (5 * resources.displayMetrics.density).toInt()
                    outRect.top = spacing
                    outRect.bottom = spacing
                }
            })
        }
//        binding.tvContent.text = "请求模式 - Character ID: ${characterId ?: Constants.DEFAULT_CHARACTER_ID}"
        sendIntent(ChatModelListIntent.Initialize)
        binding.tvSave.setOnClickListener {
            if (chatModel==null){
                ToastUtils.showShort(this@ChatModelListActivity,"请选选择模型")
            }else{
                sendIntent(ChatModelListIntent.UpdateSelectedModel(chatModel!!))
            }
        }
    }

    override fun render(state: ChatModelListState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        chatModelListAdapter.submitList(state.modes)
        if (state.error != null) {
            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
        }
        if (state.selectedModelSuccess){
            finish()
        }
    }
}