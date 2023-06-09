package com.morj12.cloudlist.presentation.view.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.morj12.cloudlist.App
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.FragmentChannelBinding
import com.morj12.cloudlist.domain.entity.Channel
import com.morj12.cloudlist.presentation.view.FragmentManager.loadCartFragment
import com.morj12.cloudlist.utils.startLoading
import com.morj12.cloudlist.utils.stopLoading

class ChannelFragment : Fragment() {

    private var _binding: FragmentChannelBinding? = null
    private val binding: FragmentChannelBinding
        get() = _binding ?: throw RuntimeException("FragmentChannelBinding is null")

    private val viewModel: ListViewModel by activityViewModels {
        ListViewModel.ListViewModelFactory((context?.applicationContext as App).db)
    }

    private lateinit var lastChannel: Channel

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
            viewModel.connectToChannel(requireActivity(), name, key)
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
                loadCartFragment(requireActivity())
            } else {
                binding.edChannelName.setText("")
                binding.edChannelKey.setText("")
            }
        }
        viewModel.error.observe(viewLifecycleOwner) {
            binding.btChannelConnect.stopLoading(binding.pbChannelConnect)
            showSnackbar(it)
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

    private fun showSnackbar(it: String) {
        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
            .setTextColor(requireActivity().resources.getColor(R.color.black))
            .setBackgroundTint(requireActivity().resources.getColor(R.color.white))
            .show()
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