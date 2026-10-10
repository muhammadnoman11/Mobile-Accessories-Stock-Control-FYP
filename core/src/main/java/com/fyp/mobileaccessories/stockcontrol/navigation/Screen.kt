package com.fyp.mobileaccessories.stockcontrol.navigation


sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Admin
    object AdminDashboard : Screen("admin_dashboard")
    object ManageUsers : Screen("admin_manage_users")
    object ManageModerators : Screen("admin_manage_moderators")
    object ManageEmployees : Screen("admin_manage_employees")
    object ManageProducts : Screen("admin_manage_products")
    object AddEditProduct : Screen("admin_add_edit_product/{productId}") {
        fun createRoute(productId: Int = -1) = "admin_add_edit_product/$productId"
    }
    object AdminOrders : Screen("admin_orders")
    object ManagePayments : Screen("admin_payments")
    object ManageCouriers : Screen("admin_couriers")
    object ManageAnnouncements : Screen("admin_announcements")
    object ManageCategories : Screen("admin_categories")
    object StockReport : Screen("admin_stock_report")
    object AdminProfile : Screen("admin_profile")

    // Moderator
    object ModDashboard : Screen("mod_dashboard")
    object ApproveProducts : Screen("mod_approve_products")
    object TrackOrders : Screen("mod_track_orders")
    object ModProfile : Screen("mod_profile")

    // User
    object UserHome : Screen("user_home")
    object ProductList : Screen("user_products")
    object ProductDetail : Screen("user_product_detail/{productId}") {
        fun createRoute(productId: Int) = "user_product_detail/$productId"
    }
    object PlaceOrder : Screen("user_place_order/{productId}") {
        fun createRoute(productId: Int) = "user_place_order/$productId"
    }
    object MyOrders : Screen("user_my_orders")
    object UserProfile : Screen("user_profile")
    object Announcements : Screen("user_announcements")
}