package com.morj12.cloudlist.presentation.view.list

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morj12.cloudlist.R
import com.morj12.cloudlist.databinding.FragmentCartBinding
import com.morj12.cloudlist.presentation.view.adapter.CartAdapter

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding: FragmentCartBinding
        get() = _binding ?: throw RuntimeException("FragmentCartBinding is null")

    private lateinit var viewModel: ListViewModel

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
        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        initRecyclerView()
        initOnItemClickedListener()
        loadCarts()
        initListeners()
        setupSwipeListener()
        setupRealtimeUpdates()
        observe()
    }

    private fun initRecyclerView() {
        adapter = CartAdapter()
        binding.rcCart.layoutManager = LinearLayoutManager(activity)
        binding.rcCart.adapter = adapter
    }

    private fun initOnItemClickedListener() {
        adapter.onItemClickedListener = { viewModel.setCart(it) }
    }

    private fun loadCarts() = viewModel.loadCartsFromDb()

    private fun initListeners() {
        binding.btChannelDisconnect.setOnClickListener {
            viewModel.setChannel(null)
        }
        binding.fabNewCart.setOnClickListener {
            viewModel.createNewCart()
        }
    }

    private fun setupSwipeListener() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = adapter.currentList[viewHolder.adapterPosition]
                viewModel.deleteCart(item)
            }
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rcCart)
    }

    private fun setupRealtimeUpdates() {
        viewModel.setupRealtimeChannelUpdates()
    }

    private fun observe() {
        viewModel.carts.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
        }
        viewModel.channel.observe(viewLifecycleOwner) {
            if (it == null) requireActivity().supportFragmentManager.popBackStack()
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