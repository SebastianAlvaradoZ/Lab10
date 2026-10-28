package com.example.lab10.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.SerieApiService
import com.example.lab10.data.SerieModel
import kotlinx.coroutines.launch

// Vuelve al listado de series quitando las pantallas intermedias de la pila de navegación
private fun irAListado(navController: NavHostController) {
    navController.navigate("series") {
        popUpTo("series") { inclusive = true }
    }
}

// ---------------------------------------------------------------------------
// LISTADO: obtiene todas las series de la API (GET /serie) y las muestra en una lista
// ---------------------------------------------------------------------------
@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    // Lista observable: cuando cambia, Compose vuelve a dibujar la pantalla
    val listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }

    // Se ejecuta una sola vez al entrar a la pantalla; hace la petición en una corrutina
    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectSeries()
            listaSeries.clear()
            listaSeries.addAll(listado)
        } catch (e: Exception) {
            Log.e("SERIE-LISTADO", "Error al obtener las series: ${e.message}")
        }
    }

    LazyColumn {
        // Fila de encabezado
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = "SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text(text = "Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            }
        }

        // Una fila por cada serie, con botones para editar y eliminar
        items(listaSeries) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                IconButton(
                    onClick = {
                        navController.navigate("serieVer/${item.id}")
                        Log.e("SERIE-VER", "ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Editar")
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id}")
                        Log.e("SERIE-DEL", "ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// EDITAR / NUEVO: si pid = 0 crea una serie (POST), si no, carga y actualiza (GET + PUT)
// ---------------------------------------------------------------------------
@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0) {
    val id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf("") }
    var release_date by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Si es edición, se obtienen los datos actuales de la serie (GET /serie/{id})
    if (id != 0) {
        LaunchedEffect(Unit) {
            try {
                val respuesta = servicio.selectSerie(id.toString())
                respuesta.body()?.let { serie ->
                    name = serie.name
                    release_date = serie.release_date
                    rating = serie.rating.toString()
                    category = serie.category
                }
            } catch (e: Exception) {
                Log.e("SERIE-EDITAR", "Error al obtener la serie: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = id.toString(),
            onValueChange = { },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name: ") },
            singleLine = true
        )
        TextField(
            value = release_date,
            onValueChange = { release_date = it },
            label = { Text("Release Date (AAAA-MM-DD):") },
            singleLine = true
        )
        TextField(
            value = rating,
            onValueChange = { rating = it },
            label = { Text("Rating:") },
            singleLine = true
        )
        TextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (horror, comedy, action, drama):") },
            singleLine = true
        )
        Button(
            onClick = {
                val objSerie = SerieModel(id, name, release_date, rating.toIntOrNull() ?: 0, category)
                // La petición se lanza en una corrutina; al terminar se vuelve al listado
                scope.launch {
                    try {
                        val respuesta = if (id == 0)
                            servicio.insertSerie(objSerie)                 // POST /serie
                        else
                            servicio.updateSerie(id.toString(), objSerie)  // PUT /serie/{id}
                        Log.e("SERIE-GRABAR", "Código HTTP = ${respuesta.code()}")
                    } catch (e: Exception) {
                        Log.e("SERIE-GRABAR", "Error al grabar: ${e.message}")
                    }
                    irAListado(navController)
                }
            }
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// ELIMINAR: pide confirmación y elimina la serie (DELETE /serie/{id})
// ---------------------------------------------------------------------------
@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    var showDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                irAListado(navController)
            },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        scope.launch {
                            try {
                                val respuesta = servicio.deleteSerie(id.toString())
                                Log.e("SERIE-ELIMINAR", "Código HTTP = ${respuesta.code()}")
                            } catch (e: Exception) {
                                Log.e("SERIE-ELIMINAR", "Error al eliminar: ${e.message}")
                            }
                            irAListado(navController)
                        }
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        irAListado(navController)
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
