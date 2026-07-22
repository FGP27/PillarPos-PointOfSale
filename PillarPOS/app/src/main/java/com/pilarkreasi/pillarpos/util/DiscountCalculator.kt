package com.pilarkreasi.pillarpos.util

import com.pilarkreasi.pillarpos.data.model.CartItem
import com.pilarkreasi.pillarpos.data.model.DiscountEntity
import com.pilarkreasi.pillarpos.data.model.DiscountType
import java.util.Date


object DiscountCalculator {

    data class Result(
        val subtotal: Double,
        val discountAmount: Double,
        val finalTotal: Double,
        val appliedDiscountNames: List<String>
    )

    fun calculate(
        cartItems: List<CartItem>,
        discounts: List<DiscountEntity>,
        now: Date = Date()
    ): Result {
        val subtotal = cartItems.sumOf { it.totalPrice }
        val validDiscounts = discounts.filter { it.isActive && isWithinPeriod(it, now) }

        if (validDiscounts.isEmpty() || subtotal <= 0) {
            return Result(subtotal, 0.0, subtotal, emptyList())
        }

        var itemLevelDiscount = 0.0
        val appliedNames = LinkedHashSet<String>()

        cartItems.forEach { item ->
            val productDiscount = validDiscounts
                .filter { it.type == DiscountType.PRODUCT && it.targetId == item.product.id }
                .maxByOrNull { it.percentage }
            val categoryDiscount = validDiscounts
                .filter { it.type == DiscountType.CATEGORY && it.targetId == item.product.categoryId }
                .maxByOrNull { it.percentage }

            val best = listOfNotNull(productDiscount, categoryDiscount).maxByOrNull { it.percentage }
            if (best != null && best.percentage > 0) {
                itemLevelDiscount += item.totalPrice * (best.percentage / 100.0)
                appliedNames.add(best.name)
            }
        }

        val subtotalAfterItemDiscount = subtotal - itemLevelDiscount
        val totalDiscountRule = validDiscounts
            .filter { it.type == DiscountType.TOTAL }
            .maxByOrNull { it.percentage }

        var totalLevelDiscount = 0.0
        if (totalDiscountRule != null) {
            totalLevelDiscount = subtotalAfterItemDiscount * (totalDiscountRule.percentage / 100.0)
            appliedNames.add(totalDiscountRule.name)
        }

        val totalDiscount = itemLevelDiscount + totalLevelDiscount
        val finalTotal = (subtotal - totalDiscount).coerceAtLeast(0.0)

        return Result(subtotal, totalDiscount, finalTotal, appliedNames.toList())
    }

    private fun isWithinPeriod(discount: DiscountEntity, now: Date): Boolean {
        val fromOk = discount.validFrom?.let { !now.before(it) } ?: true
        val untilOk = discount.validUntil?.let { !now.after(it) } ?: true
        return fromOk && untilOk
    }
}