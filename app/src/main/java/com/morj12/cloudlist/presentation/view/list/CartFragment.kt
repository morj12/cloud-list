package com.morj12.cloudlist.presentation.view.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.morj12.cloudlist.App
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.FragmentCartBinding
import com.morj12.cloudlist.presentation.adapter.CartAdapter
import com.morj12.cloudlist.presentation.dialog.DeleteDialog
import com.morj12.cloudlist.presentation.view.main.MainActivity


class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding: FragmentCartBinding
        get() = _binding ?: throw RuntimeException("FragmentCartBinding is null")

    private val viewModel: ListViewModel by activityViewModels {
        ListViewModel.ListViewModelFactory((context?.applicationContext as App).db)
    }

    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadCarts()
        initListeners()
        setupRealtimeUpdates()
        observe()
    }

    private fun initRecyclerView() {
        adapter = CartAdapter()
        binding.rcCart.layoutManager = LinearLayoutManager(activity)
        binding.rcCart.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.rcCart.adapter = adapter
        adapter.onItemClickedListener = { viewModel.setCart(it) }
        adapter.onItemDeleteClickedListener = {
            DeleteDialog.showDialog(requireContext(), it) {
                viewModel.deleteCart(it)
            }
        }
    }

    private fun loadCarts() {
        if (viewModel.mode == Mode.CLOUD)
            viewModel.loadCartsFromDb()
    }

    private fun initListeners() {
        if (viewModel.mode == Mode.CLOUD)
            binding.btChannelDisconnect.setOnClickListener { viewModel.setChannel(null) }
        else
            binding.btChannelDisconnect.visibility = View.GONE
        binding.fabNewCart.setOnClickListener { viewModel.createNewCart() }
    }

    private fun setupRealtimeUpdates() = viewModel.setupRealtimeChannelUpdates()

    private fun observe() {
        val cartsSource = if (viewModel.mode == Mode.LOCAL) viewModel.loadLocalCarts
        else viewModel.carts
        cartsSource.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList().reversed()) {
                if (it.isNotEmpty()) {
                    binding.rcCart.post { binding.rcCart.smoothScrollToPosition(0) }
                }
            }
        }
        viewModel.channel.observe(viewLifecycleOwner) {
            if (it == null) {
                if (viewModel.mode == Mode.LOCAL)
                    startActivity(Intent(requireActivity(), MainActivity::class.java))
                else
                    requireActivity().supportFragmentManager.popBackStack()
            }
        }
        viewModel.cart.observe(viewLifecycleOwner) {
            if (it != null) {
                loadItemFragment()
            }
        }
    }

    private fun loadItemFragment() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fl_fragment, ItemFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = CartFragment()
    }
}