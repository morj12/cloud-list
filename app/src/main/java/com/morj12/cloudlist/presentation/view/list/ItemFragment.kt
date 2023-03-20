package com.morj12.cloudlist.presentation.view.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.morj12.cloudlist.App
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.FragmentItemBinding
import com.morj12.cloudlist.domain.entity.Item
import com.morj12.cloudlist.presentation.adapter.ItemAdapter
import com.morj12.cloudlist.presentation.dialog.DeleteDialog

class ItemFragment : Fragment() {

    private var _binding: FragmentItemBinding? = null
    private val binding: FragmentItemBinding
        get() = _binding ?: throw RuntimeException("FragmentItemBinding is null")

    private val viewModel: ListViewModel by activityViewModels {
        ListViewModel.ListViewModelFactory((context?.applicationContext as App).db)
    }

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
        }
        adapter.onItemDeleteClickedListener = {
            DeleteDialog.showDialog(requireContext(), it) { viewModel.deleteItem(it) }
        }
    }

    private fun loadItems() {
        if (viewModel.mode == Mode.CLOUD) viewModel.loadItemsFromDb()
    }

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
            } else {
                binding.tvFragmentItemCartPrice.text =
                    getString(R.string.cart_price_text, it.price.toString())
            }
        }
        if (viewModel.mode == Mode.LOCAL)
            viewModel.loadLocalItems().observe(viewLifecycleOwner) {
                viewModel.setCartPrice(it)
                adapter.submitList(it)
            } else
            viewModel.items.observe(viewLifecycleOwner) {
                adapter.submitList(it)
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