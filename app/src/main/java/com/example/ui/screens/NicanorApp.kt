package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.Product
import com.example.data.Reservation
import com.example.viewmodel.JoyasViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicanorApp(viewModel: JoyasViewModel) {
    val context = LocalContext.current
    val products by viewModel.productsState.collectAsStateWithLifecycle()
    val reservations by viewModel.reservationsState.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProduct.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var isAdminMode by remember { mutableStateOf(false) }
    var showReservationDialog by remember { mutableStateOf(false) }
    var productToReserve by remember { mutableStateOf<Product?>(null) }

    // Navigation and screen state
    val categories = listOf("Todas", "Anillos", "Collares", "Pulseras", "Aros")

    // Filter products
    val filteredProducts = products.filter {
        val matchesCategory = selectedCategory == "Todas" || it.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) || 
                            it.description.contains(searchQuery, ignoreCase = true) ||
                            it.material.contains(searchQuery, ignoreCase = true) ||
                            it.reference.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    // Responsive screen layout sizing
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nicanor_logo),
                            contentDescription = "Logo Joyas Nicanor",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "NICANOR",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "ALTA JOYERÍA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isAdminMode = !isAdminMode },
                        modifier = Modifier
                            .testTag("admin_toggle_btn")
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                            .padding(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isAdminMode) Icons.Default.Close else Icons.Default.Lock,
                            contentDescription = "Modo Diseñador",
                            tint = if (isAdminMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = isAdminMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransition"
            ) { targetIsAdmin ->
                if (targetIsAdmin) {
                    AdminDashboard(
                        viewModel = viewModel,
                        products = products,
                        reservations = reservations,
                        onClose = { isAdminMode = false },
                        isTablet = isTablet
                    )
                } else {
                    MainCatalog(
                        products = filteredProducts,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        onCategorySelect = { viewModel.selectCategory(it) },
                        onProductClick = { viewModel.selectProduct(it) },
                        isTablet = isTablet
                    )
                }
            }

            // Elegant Product Detail Dialog/Sheet
            selectedProduct?.let { product ->
                DetailDialog(
                    product = product,
                    onDismiss = { viewModel.selectProduct(null) },
                    onReserve = {
                        productToReserve = product
                        showReservationDialog = true
                    },
                    onWhatsApp = { coordinateViaWhatsApp(context, product) },
                    onEmail = { coordinateViaEmail(context, product) }
                )
            }

            // Reservation Form Dialog
            if (showReservationDialog && productToReserve != null) {
                ReservationDialog(
                    product = productToReserve!!,
                    onDismiss = {
                        showReservationDialog = false
                        productToReserve = null
                    },
                    onSubmit = { name, phone, email, instructions ->
                        viewModel.makeReservation(
                            productId = productToReserve!!.id,
                            productName = productToReserve!!.name,
                            productReference = productToReserve!!.reference,
                            customerName = name,
                            customerPhone = phone,
                            customerEmail = email,
                            message = instructions,
                            onSuccess = { reservation ->
                                showReservationDialog = false
                                productToReserve = null
                                viewModel.selectProduct(null)
                                Toast.makeText(
                                    context,
                                    "¡Reserva registrada con éxito! Coordinemos por WhatsApp.",
                                    Toast.LENGTH_LONG
                                ).show()
                                sendWhatsAppReservationAlert(context, reservation)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun MainCatalog(
    products: List<Product>,
    categories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    isTablet: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search & Filter Block centered with maximum width restriction for tablet comfort
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 700.dp)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Editorial Branding Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surfaceVariant, Color.Black)
                        )
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "COLECCIÓN NICANOR EXCLUSIVA",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Joyas forjadas a mano en Platino y Oro de 18K",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Light
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Elegant Search Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catalog_search_input"),
                placeholder = {
                    Text(
                        "Buscar anillos, gargantillas, brazaletes...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Category Slider Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onCategorySelect(category) }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Light,
                                letterSpacing = 1.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Product Catalog Grid Layout
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Cargando",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Buscando piezas exclusivas...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            val gridColumns = if (isTablet) 3 else 2
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("products_grid")
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(products.size) { index ->
                    val product = products[index]
                    ProductCard(product = product, onClick = { onProductClick(product) })
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }
    val priceFormatted = formatter.format(product.price).replace("CLP", "$").replace("COP", "$")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Column {
            // Product Image Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black)
            ) {
                ProductImageResolver(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )

                // Category Tag overlay
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Item metadata details
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.material,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Light),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = priceFormatted,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    )
                    Text(
                        text = product.reference,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
fun ProductImageResolver(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val localDrawableRes = when (imageUrl) {
        "img_ring" -> R.drawable.img_ring
        "img_necklace" -> R.drawable.img_necklace
        "img_bracelet" -> R.drawable.img_bracelet
        "img_earrings" -> R.drawable.img_earrings
        else -> null
    }

    if (localDrawableRes != null) {
        Image(
            painter = painterResource(id = localDrawableRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith("content://")) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.img_nicanor_logo),
            placeholder = painterResource(id = R.drawable.img_nicanor_logo)
        )
    } else {
        // Graphic aesthetic placeholder in case of empty input
        Box(
            modifier = modifier
                .background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.colorScheme.surfaceVariant, Color.Black)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_nicanor_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(50.dp),
                alpha = 0.3f
            )
        }
    }
}

// Dialog exhibiting high-end jewelry particulars
@Composable
fun DetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onReserve: () -> Unit,
    onWhatsApp: () -> Unit,
    onEmail: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }
    val priceFormatted = formatter.format(product.price).replace("CLP", "$").replace("COP", "$")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Image and dismiss trigger
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                ) {
                    ProductImageResolver(
                        imageUrl = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                // Metadata Details
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.tertiary, letterSpacing = 2.sp)
                        )
                        Text(
                            text = product.reference,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Material: ${product.material}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = priceFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Justify
                    )
                }

                // Interactive Integration buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onReserve,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reserve_btn"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "RESERVAR ESTA PIEZA EN TIENDA",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, letterSpacing = 1.sp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onWhatsApp,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "WhatsApp",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp)
                            )
                        }

                        Button(
                            onClick = onEmail,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Correo",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reservation Dialog to register contact parameters
@Composable
fun ReservationDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSubmit: (name: String, phone: String, email: String, message: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    var errors by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "RESERVAR DISEÑO",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Especifica tus datos para apartar la joya '${product.name}' (Ref: ${product.reference}). Nos pondremos en contacto prontamente vía WhatsApp o Correo para finalizar detalles de su fabricación.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reserve_name_input"),
                    label = { Text("Nombre Completo") },
                    isError = errors && name.isBlank(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reserve_phone_input"),
                    label = { Text("WhatsApp (e.g. +56912345678)") },
                    isError = errors && phone.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reserve_email_input"),
                    label = { Text("Correo Electrónico") },
                    isError = errors && email.isBlank(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("reserve_notes_input"),
                    label = { Text("Instrucciones/Medida (Opcional)") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCELAR", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || phone.isBlank() || email.isBlank()) {
                                errors = true
                            } else {
                                onSubmit(name, phone, email, instructions)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reserve_submit_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("CONFIRMAR")
                    }
                }
            }
        }
    }
}

// Admin Panel for catalogue and reservations
@Composable
fun AdminDashboard(
    viewModel: JoyasViewModel,
    products: List<Product>,
    reservations: List<Reservation>,
    onClose: () -> Unit,
    isTablet: Boolean
) {
    var adminTabSelected by remember { mutableStateOf(0) } // 0: Reservas, 1: Agregar Joya, 2: Catálogo CRUD

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Admin header inside Tablet or Compact
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "CONSOLA DE DISEÑADOR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Joyas Nicanor - Modo Administración de Ventas y Catálogo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar admin",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        TabRow(
            selectedTabIndex = adminTabSelected,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            }
        ) {
            Tab(
                selected = adminTabSelected == 0,
                onClick = { adminTabSelected = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Reservas (${reservations.size})", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
            Tab(
                selected = adminTabSelected == 1,
                onClick = { adminTabSelected = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Agregar Joya", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
            Tab(
                selected = adminTabSelected == 2,
                onClick = { adminTabSelected = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Gestionar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (adminTabSelected) {
                0 -> ReservationsList(reservations = reservations, onDelete = { viewModel.deleteReservation(it) })
                1 -> AddProductForm(
                    onProductAdded = { name, desc, price, img, cat, ref, mat ->
                        viewModel.addProduct(name, desc, price, img, cat, ref, mat)
                        adminTabSelected = 2 // Move back to catalogue checklist after adding
                    }
                )
                2 -> CatalogCrudList(products = products, onDelete = { viewModel.deleteProductById(it) })
            }
        }
    }
}

@Composable
fun ReservationsList(reservations: List<Reservation>, onDelete: (Int) -> Unit) {
    if (reservations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No se han registrado reservas aún.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("admin_reservations_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reservations) { reservation ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reservation_item_${reservation.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = reservation.productName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Ref: ${reservation.productReference}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            IconButton(onClick = { onDelete(reservation.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar Reserva",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Text("Cliente: ${reservation.customerName}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Text("WhatsApp: ${reservation.customerPhone}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable {
                                    openWhatsAppChat(context, reservation.customerPhone, "Hola ${reservation.customerName}, te escribo de Joyas Nicanor sobre la reserva de tu diseño.")
                                })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Text("Email: ${reservation.customerEmail}", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (reservation.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Notas: \"${reservation.message}\"",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Serif),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(reservation.reservationDate))
                        Text(
                            text = "Fecha: $dateString",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddProductForm(
    onProductAdded: (name: String, desc: String, price: Double, img: String, cat: String, ref: String, mat: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var material by remember { mutableStateOf("Platino 950") }
    var priceStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Anillos") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val categoriesList = listOf("Anillos", "Collares", "Pulseras", "Aros")
    val defaultMaterials = listOf("Platino 950", "Oro Blanco 18K", "Oro Amarillo 18K", "Oro Rosado 18K")
    // Mock luxury models
    val stockImages = listOf("img_ring", "img_necklace", "img_bracelet", "img_earrings")

    var errors by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("add_product_form"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "Ingresar un Nuevo Diseño al Catálogo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_prod_name"),
                label = { Text("Nombre de la Joya") },
                isError = errors && name.isBlank(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_prod_ref"),
                    label = { Text("Referencia (e.g. SKU)") },
                    isError = errors && reference.isBlank(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_prod_price"),
                    label = { Text("Precio (CLP/USD)") },
                    isError = errors && (priceStr.isBlank() || priceStr.toDoubleOrNull() == null),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                )
            }
        }

        item {
            // Category selector Row
            Text("Categoría:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriesList.forEach { cat ->
                    val isSelected = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { category = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            // Material selector Row
            Text("Material Principal:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                defaultMaterials.forEach { mat ->
                    val isSelected = material == mat
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { material = mat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            // Stock image selector
            Text("Visual de Joyería (Elige catálogo base o ingresa un enlace):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                stockImages.forEach { img ->
                    val isSelected = imageUrl == img
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { imageUrl = img }
                    ) {
                        val drawableId = when (img) {
                            "img_ring" -> R.drawable.img_ring
                            "img_necklace" -> R.drawable.img_necklace
                            "img_bracelet" -> R.drawable.img_bracelet
                            "img_earrings" -> R.drawable.img_earrings
                            else -> R.drawable.img_nicanor_logo
                        }
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = img,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_prod_img_url"),
                label = { Text("Enlace Externo de Imagen (Opcional)") },
                placeholder = { Text("https://host.com/joya.jpg") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("admin_prod_desc"),
                label = { Text("Descripción Detallada del Diseño") },
                isError = errors && description.isBlank(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }

        item {
            Button(
                onClick = {
                    val parsedPrice = priceStr.toDoubleOrNull()
                    if (name.isBlank() || reference.isBlank() || parsedPrice == null || description.isBlank()) {
                        errors = true
                    } else {
                        val img = if (imageUrl.isBlank()) "img_ring" else imageUrl
                        onProductAdded(name, description, parsedPrice, img, category, reference, material)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("admin_save_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("REGISTRAR EN CATÁLOGO")
            }
        }
    }
}

@Composable
fun CatalogCrudList(products: List<Product>, onDelete: (Int) -> Unit) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay joyas en el catálogo.", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("admin_catalog_crud_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                ListItem(
                    headlineContent = {
                        Text(product.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    },
                    supportingContent = {
                        Text("Ref: ${product.reference} | ${product.material}", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black)
                        ) {
                            ProductImageResolver(
                                imageUrl = product.imageUrl,
                                contentDescription = product.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    trailingContent = {
                        IconButton(onClick = { onDelete(product.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar de base",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

// Logic and integrations for WhatsApp and Email Intents
private fun coordinateViaWhatsApp(context: Context, product: Product) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }
    val priceFormatted = formatter.format(product.price).replace("CLP", "$").replace("COP", "$")

    val msg = "Hola Joyas Nicanor, estoy muy interesado en coordinar la adquisición/fabricación de la joya de alta gama:\n\n" +
            "✨ *Diseño:* ${product.name}\n" +
            "📌 *Referencia/SKU:* ${product.reference}\n" +
            "💎 *Material:* ${product.material}\n" +
            "🏷️ *Valor de catálogo:* $priceFormatted\n\n" +
            "Por favor indíqueme los siguientes pasos. ¡Gracias!"

    openWhatsAppChat(context, "+56912345678", msg) // Example jeweler phone structure
}

private fun coordinateViaEmail(context: Context, product: Product) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
    }
    val priceFormatted = formatter.format(product.price).replace("CLP", "$").replace("COP", "$")

    val subject = "Consulta de Joyería Fina: ${product.name} (${product.reference})"
    val body = "Estimado de Joyas Nicanor,\n\n" +
            "Deseo realizar una consulta sobre el siguiente diseño de su catálogo:\n\n" +
            "• Diseño: ${product.name}\n" +
            "• Referencia: ${product.reference}\n" +
            "• Material: ${product.material}\n" +
            "• Precio de Referencia: $priceFormatted\n\n" +
            "Agradezco sus comentarios y disponibilidad para coordinar los detalles del pedido.\n\n" +
            "Atentamente,"

    openEmailComposer(context, "joyasnicanor@gmail.com", subject, body)
}

private fun sendWhatsAppReservationAlert(context: Context, reservation: Reservation) {
    val msg = "¡Hola Joyas Nicanor! Acabo de registrar una Reserva Exclusiva para un diseño desde la aplicación:\n\n" +
            "⭐ *Detalle de Reserva:* \n" +
            "• *Joya:* ${reservation.productName}\n" +
            "• *Referencia:* ${reservation.productReference}\n" +
            "• *Nombre Cliente:* ${reservation.customerName}\n" +
            "• *WhatsApp:* ${reservation.customerPhone}\n" +
            "• *Correo:* ${reservation.customerEmail}\n" +
            "• *Instrucciones:* \"${reservation.message}\"\n\n" +
            "Por favor, confirmemos la reserva y la planificación de fabricación. ¡Gracias!"

    openWhatsAppChat(context, "+56912345678", msg)
}

private fun openWhatsAppChat(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No se pudo abrir WhatsApp. Copiaremos al portapapeles.", Toast.LENGTH_LONG).show()
    }
}

private fun openEmailComposer(context: Context, email: String, subject: String, body: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No tienes aplicaciones de correo configuradas.", Toast.LENGTH_SHORT).show()
    }
}
