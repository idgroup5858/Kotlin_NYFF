package pr.com.kotlin_nyff.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import pr.com.kotlin_nyff.ui.checkout.CheckoutScreen
import pr.com.kotlin_nyff.ui.home.HomeScreen
import pr.com.kotlin_nyff.ui.location.LocationScreen
import pr.com.kotlin_nyff.ui.orders.OrdersScreen
import pr.com.kotlin_nyff.ui.tracking.TrackingScreen

@Serializable
data object HomeRoute

@Serializable
data object LocationRoute

@Serializable
data object CheckoutRoute

@Serializable
data object OrdersRoute

@Serializable
data class TrackingRoute(val orderId: String)

/** Ekranlar: bosh sahifa → (xarita) → rasmiylashtirish → buyurtma holati; "Buyurtmalarim" — bosh sahifadan. */
@Composable
fun NyffNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = HomeRoute) {
        composable<HomeRoute> { entry ->
            HomeScreen(
                onOpenLocation = { nav.go(entry, LocationRoute) },
                onOpenCheckout = { nav.go(entry, CheckoutRoute) },
                onOpenOrders = { nav.go(entry, OrdersRoute) },
                onOpenOrder = { nav.go(entry, TrackingRoute(it)) },
            )
        }
        composable<LocationRoute> { entry ->
            LocationScreen(onBack = { nav.back(entry) })
        }
        composable<CheckoutRoute> { entry ->
            CheckoutScreen(
                onBack = { nav.back(entry) },
                onOpenLocation = { nav.go(entry, LocationRoute) },
                // buyurtmadan keyin "orqaga" — bosh sahifaga (rasmiylashtirishga emas)
                onPlaced = { id ->
                    nav.navigate(TrackingRoute(id)) { popUpTo<HomeRoute>() }
                },
            )
        }
        composable<OrdersRoute> { entry ->
            OrdersScreen(
                onBack = { nav.back(entry) },
                onOpenOrder = { nav.go(entry, TrackingRoute(it)) },
            )
        }
        composable<TrackingRoute> { entry ->
            TrackingScreen(
                onBack = { nav.back(entry) },
                onNewOrder = {
                    if (entry.isResumed()) nav.popBackStack<HomeRoute>(inclusive = false)
                },
            )
        }
    }
}

private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

/** Ikki marta tez bosilsa ekran ikki marta ochilmasin — faqat joriy ekrandan. */
private fun NavHostController.go(from: NavBackStackEntry, route: Any) {
    if (from.isResumed()) navigate(route)
}

/** Oxirgi ekranni yopib, bo'sh oyna qolmasin. */
private fun NavHostController.back(from: NavBackStackEntry) {
    if (from.isResumed() && previousBackStackEntry != null) popBackStack()
}
