package com.pilarkreasi.pillarpos.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.pilarkreasi.pillarpos.R
import com.pilarkreasi.pillarpos.data.local.PillarPosDatabase
import com.pilarkreasi.pillarpos.data.model.TransactionDetailEntity
import com.pilarkreasi.pillarpos.data.model.TransactionEntity
import com.pilarkreasi.pillarpos.data.repository.ProductRepository
import com.pilarkreasi.pillarpos.databinding.FragmentPembayaranBinding
import com.pilarkreasi.pillarpos.util.DiscountCalculator
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import com.pilarkreasi.pillarpos.util.SessionManager

class PaymentFragment : Fragment() {

    private var _binding: FragmentPembayaranBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by activityViewModels {
        val database = PillarPosDatabase.getInstance(requireContext())
        ProductViewModelFactory(
            ProductRepository.create(requireContext(), database.productDao(), database.categoryDao(), database.transactionDao(), database.discountDao())
        )
    }

    private var selectedPaymentMethod = ""

    

    private var latestDiscountResult: DiscountCalculator.Result? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPembayaranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeCart()

        binding.btnConfirmPayment.setOnClickListener {
            processTransaction()
        }

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupUI() {
        
        
        
        
        
        
        
        setPaymentSelection("Tunai")

        binding.cardTunai.setOnClickListener { setPaymentSelection("Tunai") }
        binding.rbTunai.setOnClickListener { setPaymentSelection("Tunai") }

        binding.cardQris.setOnClickListener { setPaymentSelection("QRIS") }
        binding.rbQris.setOnClickListener { setPaymentSelection("QRIS") }

        binding.cardDebit.setOnClickListener { setPaymentSelection("Debit") }
        binding.rbDebit.setOnClickListener { setPaymentSelection("Debit") }
    }

    private fun setPaymentSelection(method: String) {
        selectedPaymentMethod = method
        binding.rbTunai.isChecked = method == "Tunai"
        binding.rbQris.isChecked = method == "QRIS"
        binding.rbDebit.isChecked = method == "Debit"
    }

    

    private fun observeCart() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { recalculateAndRender() }
        productViewModel.activeDiscounts.observe(viewLifecycleOwner) { recalculateAndRender() }
    }

    private fun recalculateAndRender() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val cartItems = cartViewModel.cartItems.value ?: emptyList()
        val discounts = productViewModel.activeDiscounts.value ?: emptyList()

        val result = DiscountCalculator.calculate(cartItems, discounts)
        latestDiscountResult = result

        binding.tvSubtotal.text = formatter.format(result.subtotal).replace("Rp", "Rp ")
        binding.tvTotalBayar.text = formatter.format(result.finalTotal).replace("Rp", "Rp ")

        if (result.discountAmount > 0) {
            binding.layoutDiscountRow.visibility = View.VISIBLE
            binding.tvDiscountLabel.text = "Diskon (${result.appliedDiscountNames.joinToString(", ")})"
            binding.tvDiscountAmount.text = "-" + formatter.format(result.discountAmount).replace("Rp", "Rp ")
        } else {
            binding.layoutDiscountRow.visibility = View.GONE
        }
    }

    private fun processTransaction() {
        val cartItems = cartViewModel.cartItems.value ?: return
        val discountResult = latestDiscountResult
            ?: DiscountCalculator.calculate(cartItems, productViewModel.activeDiscounts.value ?: emptyList())
        val finalTotal = discountResult.finalTotal

        if (cartItems.isEmpty()) {
            Toast.makeText(context, "Keranjang kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(context, "Silakan pilih metode pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        
        
        val sessionManager = SessionManager(requireContext())
        val transaction = TransactionEntity(
            idUser = sessionManager.getIdUser(),
            idOutlet = sessionManager.getIdOutlet(),
            totalAmount = finalTotal,
            paymentType = selectedPaymentMethod,
            transactionDate = Date(),
            notes = if (discountResult.discountAmount > 0) {
                "Diskon otomatis: ${discountResult.appliedDiscountNames.joinToString(", ")} (-Rp ${discountResult.discountAmount.toLong()})"
            } else null
        )

        
        val details = cartItems.map { item ->
            TransactionDetailEntity(
                idTransaction = 0, 
                idProduct = item.product.id,
                quantity = item.quantity,
                priceAtTime = item.product.price
            )
        }

        
        productViewModel.checkout(transaction, details) { newTransactionId ->
            cartViewModel.clearCart()
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, TransactionDetailFragment.newInstance(newTransactionId))
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}