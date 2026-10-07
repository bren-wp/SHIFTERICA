package hr.raspored.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.*

private const val TIP_SMALL = "raspored_tip_small"
private const val TIP_MEDIUM = "raspored_tip_medium"
private const val TIP_LARGE = "raspored_tip_large"

@Composable
internal fun SupportDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val manager = remember(context) { DonationBillingManager(context.applicationContext) }

    DisposableEffect(manager) {
        onDispose { manager.close() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RasporedColors.Bg2,
        icon = { Icon(Icons.Rounded.Favorite, null, tint = Color(0xFFFF6FA5)) },
        title = {
            Text(
                "Dobrovoljna podrška",
                color = RasporedColors.Text,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Podrška je potpuno dobrovoljna i ne otključava funkcije. Aplikacija ostaje ista bez kupnje.",
                    color = RasporedColors.Muted
                )

                if (manager.products.isEmpty()) {
                    Text(
                        if (manager.ready) {
                            "Opcije podrške trenutačno nisu dostupne."
                        } else {
                            "Povezivanje s Google Playom…"
                        },
                        color = RasporedColors.Muted
                    )
                } else {
                    manager.products.forEach { product ->
                        val price = product.oneTimePurchaseOfferDetailsList
                            ?.firstOrNull()
                            ?.formattedPrice
                            ?: "—"
                        val label = when (product.productId) {
                            TIP_SMALL -> "Mala podrška"
                            TIP_MEDIUM -> "Srednja podrška"
                            TIP_LARGE -> "Velika podrška"
                            else -> "Podrška"
                        }

                        Surface(
                            onClick = {
                                if (activity != null) manager.purchase(activity, product)
                            },
                            enabled = activity != null,
                            color = RasporedColors.Card,
                            shape = RoundedCornerShape(17.dp),
                            border = BorderStroke(1.dp, RasporedColors.Stroke),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, color = RasporedColors.Text, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.weight(1f))
                                Text(price, color = RasporedColors.Accent, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                manager.message?.let {
                    Text(it, color = RasporedColors.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Zatvori") }
        }
    )
}

private class DonationBillingManager(
    context: Context
) : PurchasesUpdatedListener {
    val products = mutableStateListOf<ProductDetails>()

    var ready by mutableStateOf(false)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                ready = result.responseCode == BillingClient.BillingResponseCode.OK
                if (ready) {
                    queryProducts()
                } else {
                    message = "Google Play naplata nije dostupna na ovom uređaju."
                }
            }

            override fun onBillingServiceDisconnected() {
                ready = false
                message = "Veza s Google Playom je prekinuta. Pokušajte ponovno kasnije."
            }
        })
    }

    private fun queryProducts() {
        val ids = listOf(TIP_SMALL, TIP_MEDIUM, TIP_LARGE)
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ids.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                products.clear()
                products.addAll(
                    detailsResult.productDetailsList.sortedBy { ids.indexOf(it.productId) }
                )
                if (products.isEmpty()) {
                    message = "Opcije podrške još nisu aktivirane u Google Play Consoleu."
                }
            } else {
                message = "Nije moguće učitati opcije podrške."
            }
        }
    }

    fun purchase(activity: Activity, product: ProductDetails) {
        val offer = product.oneTimePurchaseOfferDetailsList?.firstOrNull()
        if (offer == null) {
            message = "Odabrana opcija trenutačno nije dostupna."
            return
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product)
            .setOfferToken(offer.offerToken)
            .build()

        val result = billingClient.launchBillingFlow(
            activity,
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParams))
                .build()
        )

        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            message = "Kupnja se nije mogla pokrenuti."
        }
    }

    override fun onPurchasesUpdated(
        result: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases.orEmpty()
                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                .forEach { purchase ->
                    val params = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                    billingClient.consumeAsync(params) { consumeResult, _ ->
                        message = if (
                            consumeResult.responseCode == BillingClient.BillingResponseCode.OK
                        ) {
                            "Hvala na dobrovoljnoj podršci!"
                        } else {
                            "Podrška je zaprimljena. Google Play dovršava obradu."
                        }
                    }
                }
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            message = "Kupnja nije dovršena."
        }
    }

    fun close() {
        if (billingClient.isReady) billingClient.endConnection()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
