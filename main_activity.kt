
// MainActivity and Navigation
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaborArteTheme {
                LanchoneteApp()
            }
        }
    }
}

@Composable
fun LanchoneteApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "sales",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("sales") {
                SalesScreen()
            }
            composable("stock") {
                StockScreen()
            }
            composable("reports") {
                ReportsScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("sales", "Vendas", Icons.Default.ShoppingCart),
        BottomNavItem("stock", "Estoque", Icons.Default.Inventory),
        BottomNavItem("reports", "Relatórios", Icons.Default.Assessment)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun ReportsScreen() {
    // Implementação da tela de relatórios
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Relatórios",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Funcionalidade em desenvolvimento",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
