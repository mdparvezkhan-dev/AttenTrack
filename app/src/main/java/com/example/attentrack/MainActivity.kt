package com.example.attentrack

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.attentrack.databinding.ActivityMainBinding
import com.example.attentrack.ui.BaseActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    /** "teacher" / "student" / null (login kora nai). */
    var role: String? = null
        private set
    val isTeacher: Boolean get() = role == "teacher"

    private var menuIsTeacher: Boolean? = null
    private var bottomVisible = false
    private var systemBottom = 0
    private var imeBottom = 0
    private var expiredHandled = false

    // menu item id -> nav graph destination id (tomar main_nav er id gulo)
    private val teacherTabs = linkedMapOf(
        R.id.teacherDashboardFragment to R.id.teacherDashboardFragment2,
        R.id.teacherCoursesFragment to R.id.teacherCoursesFragment2,
        R.id.teacherProfileFragment to R.id.teacherProfileFragment2,
    )
    private val studentTabs = linkedMapOf(
        R.id.studentDashboardFragment to R.id.studentDashboardFragment,
        R.id.studentCoursesFragment to R.id.studentCoursesFragment,
        R.id.studentProfileFragment to R.id.studentProfileFragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DataStore theke ekbar porte hoy; choto read, tai main thread e thik ache
        val session = runBlocking { container.sessionStore.session.first() }
        role = session?.role?.takeIf { it == "teacher" || it == "student" }

        val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHost.navController
        val graph = navController.navInflater.inflate(R.navigation.main_nav)
        graph.setStartDestination(
            when (role) {
                "teacher" -> R.id.teacherDashboardFragment2
                "student" -> R.id.studentDashboardFragment
                else -> R.id.loginFragment
            },
        )
        navController.setGraph(graph, null)

        binding.bottomNav.setOnItemSelectedListener { item ->
            val tabs = if (menuIsTeacher == true) teacherTabs else studentTabs
            val destination = tabs[item.itemId] ?: return@setOnItemSelectedListener false
            if (navController.currentDestination?.id != destination) {
                navController.navigate(
                    destination,
                    null,
                    NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(tabs.values.first(), false)
                        .build(),
                )
            }
            true
        }
        binding.bottomNav.setOnItemReselectedListener { }

        navController.addOnDestinationChangedListener { _, destination, _ -> updateBottomNav(destination) }

        // Bottom nav hide thakle nav bar / keyboard er jaygay content e padding dao
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            systemBottom = bars.bottom
            imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            applyBottomInsets()
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun updateBottomNav(destination: NavDestination) {
        val teacherTab = teacherTabs.entries.firstOrNull { it.value == destination.id }
        val studentTab = studentTabs.entries.firstOrNull { it.value == destination.id }
        when {
            teacherTab != null -> showTabs(teacher = true, itemId = teacherTab.key)
            studentTab != null -> showTabs(teacher = false, itemId = studentTab.key)
            else -> {
                bottomVisible = false
                binding.bottomNav.visibility = View.GONE
                applyBottomInsets()
            }
        }
    }

    private fun showTabs(teacher: Boolean, itemId: Int) {
        if (menuIsTeacher != teacher) {
            binding.bottomNav.menu.clear()
            binding.bottomNav.inflateMenu(if (teacher) R.menu.bottom_menu else R.menu.bottom_menu_student)
            menuIsTeacher = teacher
        }
        binding.bottomNav.menu.findItem(itemId)?.isChecked = true
        bottomVisible = true
        binding.bottomNav.visibility = View.VISIBLE
        applyBottomInsets()
    }

    private fun applyBottomInsets() {
        // BottomNavigationView nijei nav bar inset handle kore; hide thakle container e dite hobe
        val bottom = if (bottomVisible) 0 else maxOf(systemBottom, imeBottom)
        binding.fragmentContainerView.setPadding(0, 0, 0, bottom)
    }

    /** Login thik hole LoginFragment eta call kore. */
    fun goHome(newRole: String) {
        role = newRole
        val destination = if (newRole == "teacher") R.id.teacherDashboardFragment2 else R.id.studentDashboardFragment
        navController.navigate(
            destination,
            null,
            NavOptions.Builder().setPopUpTo(R.id.loginFragment, true).build(),
        )
    }

    fun confirmLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Log out")
            .setMessage("Do you want to log out?")
            .setPositiveButton("Log out") { _, _ -> logout() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun logout() {
        lifecycleScope.launch {
            container.authRepository.logout()
            restart()
        }
    }

    /** API 401 dile: local session muche login e pathao. */
    fun onSessionExpired() {
        if (expiredHandled) return
        expiredHandled = true
        lifecycleScope.launch {
            container.authRepository.clearSession()
            toast("Your session has expired. Please sign in again.")
            restart()
        }
    }

    /** Notun Activity (clean state) diye shuru, jate ager user er data ar na thake. */
    private fun restart() {
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
    }
}
