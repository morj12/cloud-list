package com.morj12.cloudlist.presentation.view.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.FragmentChannelBinding
import com.morj12.cloudlist.domain.entity.Channel
import com.morj12.cloudlist.utils.startLoading
import com.morj12.cloudlist.utils.stopLoading

class ChannelFragment : Fragment() {

    private var _binding: FragmentChannelBinding? = null
    private val binding: FragmentChannelBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelBinding is null")

    private lateinit var viewModel: ListViewModel

    private lateinit var lastChannel: Channel

    // TODO: use buttons for removing carts and items instead of swipes
    // TODO: recalculate cart price based on its items
    // TODO: beautify, use card layouts
    // TODO: use usecases
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        initListeners()
        searchLastChannel()
        observe()
    }

    private fun initListeners() = with(binding) {
        edChannelName.doOnTextChanged { _, _, _, _ -> validateCredentials() }
        edChannelKey.doOnTextChanged { _, _, _, _ -> validateCredentials() }
        btChannelConnect.setOnClickListener {
            btChannelConnect.startLoading(pbChannelConnect)
            val name = edChannelName.text.toString()
            val key = edChannelKey.text.toString().toLong()
            viewModel.connectToChannel(name, key)
        }
        btChannelCreate.setOnClickListener {
            btChannelCreate.startLoading(pbChannelCreate)
            val name = edChannelName.text.toString()
            val key = edChannelKey.text.toString().toLong()
            viewModel.createNewChannel(name, key)
        }
        btChannelLast.setOnClickListener {
            btChannelLast.startLoading(pbChannelLast)
            viewModel.setChannel(lastChannel)
        }
    }

    private fun searchLastChannel() = viewModel.searchForLastChannel()

    private fun observe() {
        viewModel.channel.observe(viewLifecycleOwner) {
            with(binding) {
                btChannelCreate.stopLoading(pbChannelCreate)
                btChannelLast.stopLoading(pbChannelLast)
                btChannelConnect.stopLoading(pbChannelConnect)
            }
            if (it != null) {
                loadCartFragment()
            } else {
                binding.edChannelName.setText("")
                binding.edChannelKey.setText("")
            }
        }
        viewModel.error.observe(viewLifecycleOwner) {
            binding.btChannelConnect.stopLoading(binding.pbChannelConnect)
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.userLastChannel.observe(viewLifecycleOwner) {
            if (it != null) {
                lastChannel = it
                binding.btChannelLast.text =
                    getString(R.string.connect_to_last_channel, lastChannel.name)
                binding.btChannelLast.visibility = View.VISIBLE
                binding.btChannelLast.stopLoading()
            } else {
                binding.btChannelLast.visibility = View.GONE
            }
        }
    }

    private fun loadCartFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, CartFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    private fun validateCredentials() = with(binding) {
        val credentialsValid = isChannelCredentialsValid()
        btChannelConnect.isEnabled = credentialsValid
        btChannelCreate.isEnabled = credentialsValid
    }

    private fun isChannelCredentialsValid() =
        binding.edChannelName.text.toString().isNotBlank()
                && binding.edChannelKey.text.toString().isNotBlank()

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = ChannelFragment()
    }
}