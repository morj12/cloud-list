package com.morj12.cloudlist.presentation.view.list

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.FragmentItemBinding
import com.morj12.cloudlist.domain.entity.Item
import com.morj12.cloudlist.presentation.adapter.ItemAdapter

class ItemFragment : Fragment() {

    private var _binding: FragmentItemBinding? = null
    private val binding: FragmentItemBinding
        get() = _binding ?: throw RuntimeException("FragmentItemBinding is null")

    private lateinit var viewModel: ListViewModel

    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        initRecyclerView()
        loadItems()
        initListeners()
        setupRealtimeUpdates()
        observe()
    }

    private fun initRecyclerView() {
        adapter = ItemAdapter()
        binding.rcItem.layoutManager = LinearLayoutManager(activity)
        binding.rcItem.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        binding.rcItem.adapter = adapter
        adapter.onCheckClickedListener = {
            viewModel.addOrUpdateItem(it.copy(isChecked = !it.isChecked), true)
            Log.d("RC_UPD", "${it.name} was updated")
        }
        adapter.onItemDeleteClickedListener = { viewModel.deleteItem(it) }
    }

    private fun loadItems() = viewModel.loadItemsFromDb()

    private fun initListeners() = with(binding) {
        btAddNewItem.setOnClickListener {
            val name = edNewItemName.text.toString()
            val price = edNewItemPrice.text.toString().toDoubleOrNull()
            if (name.isBlank()) {
                edNewItemName.error = getString(R.string.empty_item_name)
            } else {
                val item = Item(name, price ?: 0.0, false)
                edNewItemName.error = null
                edNewItemName.text = null
                edNewItemPrice.text = null
                viewModel.addOrUpdateItem(item)
            }
        }
    }

    private fun setupRealtimeUpdates() = viewModel.setupRealtimeCartUpdates()

    private fun observe() {
        viewModel.cart.observe(viewLifecycleOwner) {
            if (it == null) {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        viewModel.items.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.cartPrice.observe(viewLifecycleOwner) {
            binding.tvFragmentItemCartPrice.text =
                getString(R.string.cart_price_text, it.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() = ItemFragment()
    }
}