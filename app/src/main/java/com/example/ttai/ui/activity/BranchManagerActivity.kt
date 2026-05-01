package com.example.ttai.ui.activity

import BranchManagerViewModelFactory
import ChatModelListViewModelFactory
import android.content.Intent
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.adapter.BranchManagerListAdapter
import com.example.ttai.adapter.ChatModelListAdapter
import com.example.ttai.databinding.ActivityRequestModelBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.BranchItem
import com.example.ttai.bean.Character
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.databinding.ActivityBranchManagerBinding
import com.example.ttai.intent.BranchManagerIntent
import com.example.ttai.intent.ChatModelListIntent
import com.example.ttai.intent.MyFragmentIntent
import com.example.ttai.state.BranchManagerState
import com.example.ttai.state.ChatModelListState
import com.example.ttai.ui.dialog.EditeDialogFragment
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.ui.vm.BranchManagerViewModel
import com.example.ttai.ui.vm.ChatModelListViewModel
import com.example.ttai.utils.Constants
import com.example.ttai.utils.ToastUtils
import com.google.android.material.snackbar.Snackbar
import java.util.logging.Logger

class BranchManagerActivity : BaseMviActivity<BranchManagerIntent, BranchManagerState, BranchManagerViewModel, ActivityBranchManagerBinding>() {
    override val viewModel: BranchManagerViewModel by viewModels {
        BranchManagerViewModelFactory(this)
    }
    var chatModel: BranchItem? = null
    var characterId:String? = ""

    override val binding by viewBinding { ActivityBranchManagerBinding.inflate(it) }
    private val chatModelListAdapter = BranchManagerListAdapter(
        onModelClick = { mode ->
            chatModel = mode
        }, onEditClick = { item ->
            // 编辑分支名称
            showEditeDialog(item)
        },
        onDeleteClick = { item ->
            // 删除分支
            showDeleteConfirmDialog(item)
        },
        onTopClick = { item ->
            // 设置AI置顶
            if (item.is_pinned==true){
                sendIntent(BranchManagerIntent.pushUnPin(item.branch_id))
            }else{
                sendIntent(BranchManagerIntent.pushPin(item.branch_id))
            }
        }
    )
    /**
     * 显示删除确认对话框
     */
    private fun showDeleteConfirmDialog(item: BranchItem) {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "是否确认删除该条聊天内容？",
            positiveText = "确定",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 确认删除
                sendIntent(BranchManagerIntent.delete(item.branch_id))
            }

            override fun onNegativeClick() {
                // 取消删除，不做任何操作
            }
        })
        dialog.show(supportFragmentManager, "delete_confirm_dialog")
    }
    /**
     * 显示删除确认对话框
     */
    private fun showEditeDialog(item: BranchItem) {
        val dialog = EditeDialogFragment.newInstance(
            title = "修改备注",
            hint = "请输入备注",
            message = item?.name,
            positiveText = "确定",
            negativeText = "取消"
        ).setOnButtonClickListener(object : EditeDialogFragment.OnButtonClickListener {
            override fun onPositiveClick(data:String?) {
                // 确认删除
                sendIntent(BranchManagerIntent.editeName(item.branch_id,data))
            }

            override fun onNegativeClick() {
                // 取消删除，不做任何操作
            }
        })
        dialog.show(supportFragmentManager, "showEditeDialog")
    }

    override fun setupViews() {
        // 返回按钮已在BaseMviActivity中自动处理
        characterId = intent.getStringExtra(Constants.CHARACTER_ID_KEY)
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatModelListAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
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
        sendIntent(BranchManagerIntent.Initialize(characterId))
        binding.tvSave.setOnClickListener {
            if (chatModel==null){
                ToastUtils.showShort(this@BranchManagerActivity,"请选选择模型")
            }else{
                sendIntent(BranchManagerIntent.UpdateSelectedModel(characterId,chatModel!!))
            }
        }
        binding.addBranch.setOnClickListener {
            sendIntent(BranchManagerIntent.CreateBranch(characterId))

        }
    }

    override fun render(state: BranchManagerState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        chatModelListAdapter.submitList(state.modes)
        if (state.error != null) {
            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
        }
        if (state.initList){
            sendIntent(BranchManagerIntent.Initialize(characterId))
        }
        if (state.selectedModelSuccess){
            Log.w("YXTEST", "YXTEST selectedModelSuccess character_id: ${state.character_id}   conversation_id: ${state.conversation_id}")
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra(Constants.CHARACTER_ID_KEY, state.character_id)
                putExtra(Constants.CONVERSATION_ID_KEY, state.conversation_id)
            }
            startActivity(intent)
            finish()
        }
    }
}