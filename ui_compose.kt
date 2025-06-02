
// Presentation Layer - UI with Jetpack Compose
@Composable
fun SalesScreen(
    viewModel: SalesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Observar mensagens de sucesso e erro
    LaunchedEffect(Unit) {
        viewModel.showSuccessMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.showErrorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Nova Venda",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Campos de entrada
        OutlinedTextField(
            value = uiState.currentSale.productName,
            onValueChange = viewModel::updateProductName,
            label = { Text("Nome do Produto") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.currentSale.quantity.toString(),
                onValueChange = viewModel::updateQuantity,
                label = { Text("Quantidade") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            OutlinedTextField(
                value = if (uiState.currentSale.unitPrice > 0) uiState.currentSale.unitPrice.toString() else "",
                onValueChange = viewModel::updateUnitPrice,
                label = { Text("Preço Unitário") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                leadingIcon = { Text("R$ ") }
            )
        }

        // Exibir total
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Total: R$ %.2f".format(uiState.totalPrice),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.totalPrice > 0) {
                    Text(
                        text = "Com impostos: R$ %.2f".format(uiState.totalPrice * 1.23),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Campo para valor recebido
        OutlinedTextField(
            value = if (uiState.receivedAmount > 0) uiState.receivedAmount.toString() else "",
            onValueChange = viewModel::updateReceivedAmount,
            label = { Text("Valor Recebido") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            leadingIcon = { Text("R$ ") }
        )

        // Exibir troco
        if (uiState.changeAmount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(
                    text = "Troco: R$ %.2f".format(uiState.changeAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Botão para registrar venda
        Button(
            onClick = viewModel::registerSale,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.currentSale.isValid() && uiState.receivedAmount >= uiState.totalPrice
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Registrar Venda")
            }
        }

        // Lista de vendas recentes
        LazyColumn {
            items(uiState.salesList.take(5)) { sale ->
                SaleItem(sale = sale)
            }
        }
    }
}

@Composable
fun SaleItem(sale: Sale) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = sale.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Qtd: ${sale.quantity} | Total: R$ %.2f".format(sale.totalPrice),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(Date(sale.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StockScreen(
    viewModel: StockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Controle de Estoque",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Alertas
        if (uiState.alerts.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.height(200.dp)
            ) {
                items(uiState.alerts) { alert ->
                    AlertItem(
                        alert = alert,
                        onMarkAsRead = { viewModel.markAlertAsRead(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de produtos
        LazyColumn {
            items(uiState.products) { product ->
                ProductItem(
                    product = product,
                    onUpdateStock = { productId, newStock ->
                        viewModel.updateStock(productId, newStock)
                    }
                )
            }
        }
    }
}

@Composable
fun AlertItem(
    alert: StockAlert,
    onMarkAsRead: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.alertType) {
                AlertType.EXPIRING_SOON -> Color(0xFFFFF3E0)
                AlertType.LOW_STOCK -> Color(0xFFFFEBEE)
                AlertType.OUT_OF_STOCK -> Color(0xFFFFCDD2)
                AlertType.EXPIRED -> Color(0xFFE3F2FD)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = alert.productName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!alert.isRead) {
                IconButton(onClick = { onMarkAsRead(alert.id) }) {
                    Icon(Icons.Default.Check, contentDescription = "Marcar como lido")
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onUpdateStock: (String, Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showDialog = true }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Estoque: ${product.stock} | Preço: R$ %.2f".format(product.price),
                style = MaterialTheme.typography.bodyMedium
            )

            if (product.isExpiringSoon()) {
                Text(
                    text = "⚠️ Próximo ao vencimento",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Red
                )
            }
        }
    }

    if (showDialog) {
        StockUpdateDialog(
            product = product,
            onConfirm = { newStock ->
                onUpdateStock(product.id, newStock)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun StockUpdateDialog(
    product: Product,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var newStock by remember { mutableStateOf(product.stock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Atualizar Estoque") },
        text = {
            Column {
                Text("Produto: ${product.name}")
                Text("Estoque atual: ${product.stock}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newStock,
                    onValueChange = { newStock = it },
                    label = { Text("Novo estoque") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    newStock.toIntOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
