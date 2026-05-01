package com.example.ttai.ui.activity

import androidx.activity.viewModels
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.NotificationsEntity
import com.example.ttai.databinding.ActivityNotifyDetailsBinding
import com.example.ttai.intent.NotifyDetailsIntent
import com.example.ttai.state.NotifyDetailsState
import com.example.ttai.ui.vm.NotifyDetailsViewModel
import com.example.ttai.ui.vm.NotifyDetailsViewModelFactory
import com.example.ttai.utils.Constants

class NotifyDetailsActivity : BaseMviActivity<NotifyDetailsIntent, NotifyDetailsState, NotifyDetailsViewModel, ActivityNotifyDetailsBinding>() {

    val notificationMap = mapOf(
        "system" to "系统通知",
        "invitation_reward" to "邀请奖励",
        "follow" to "关注通知",
        "character_review" to "角色审核",
        "purchase" to "购买通知"
    )

    override val viewModel: NotifyDetailsViewModel by viewModels {
        NotifyDetailsViewModelFactory(
            this
        )
    }
    override val binding by viewBinding { ActivityNotifyDetailsBinding.inflate(it) }
    var notifyEntity : NotificationsEntity? = null

    override fun setupViews() {
        notifyEntity = intent.getParcelableExtra(Constants.OBJECT_KEY)
        // 设置返回按钮
        binding.ivBack.setOnClickListener {
         finish()
        }
        binding.tvNotifyType.text = notificationMap[notifyEntity?.type]
        binding.tvContent.text = notifyEntity?.content
        sendIntent(NotifyDetailsIntent.notificationsMarkRead(notifyEntity))
    }
    
    override fun render(state: NotifyDetailsState) {
        super.render(state)

    }
}