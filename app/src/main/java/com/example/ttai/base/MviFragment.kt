package com.example.ttai.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// 基于 ViewBinding 的 MVI Fragment 基类
abstract class BaseMviFragment<I : MviIntent, S : MviState, VM : MviViewModel<I, S>, VB : ViewBinding> : Fragment() {
    abstract val viewModel: VM
    abstract val binding: VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeState()
    }

    abstract fun setupViews()

    open fun render(state: S) {
    }

    protected fun sendIntent(intent: I) {
        viewModel.processIntent(intent)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                render(state)
            }
        }
    }
}

inline fun <reified VB : ViewBinding> viewBinding(
    crossinline inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB
): ReadOnlyProperty<Fragment, VB> = object : ReadOnlyProperty<Fragment, VB> {
    private var binding: VB? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
        if (binding == null) {
            binding = inflate(thisRef.layoutInflater, null, false)
            thisRef.viewLifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.LifecycleObserver {
                @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    binding = null
                }
            })
        }
        return binding!!
    }
}