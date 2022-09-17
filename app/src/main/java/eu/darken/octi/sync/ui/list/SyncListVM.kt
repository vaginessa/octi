package eu.darken.octi.sync.ui.list

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.darken.octi.common.coroutine.DispatcherProvider
import eu.darken.octi.common.debug.logging.Logging.Priority.WARN
import eu.darken.octi.common.debug.logging.log
import eu.darken.octi.common.debug.logging.logTag
import eu.darken.octi.common.uix.ViewModel3
import eu.darken.octi.sync.core.SyncConnector
import eu.darken.octi.sync.core.SyncManager
import eu.darken.octi.sync.core.provider.gdrive.GDriveAppDataConnector
import eu.darken.octi.sync.core.provider.gdrive.GoogleAccountRepo
import eu.darken.octi.sync.ui.list.items.GDriveAppDataVH
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SyncListVM @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val syncManager: SyncManager,
    private val googleAccountRepo: GoogleAccountRepo,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    data class State(
        val connectors: List<SyncListAdapter.Item> = emptyList()
    )

    val state = syncManager.connectors
        .flatMapLatest { connectors ->
            if (connectors.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val withStates = connectors.map { connector ->
                connector.state.mapNotNull<SyncConnector.State, SyncListAdapter.Item> { state ->
                    when (connector) {
                        is GDriveAppDataConnector -> GDriveAppDataVH.Item(
                            account = connector.account,
                            state = state,
                            onWipe = {
                                launch { connector.wipe() }
                            },
                            onRemove = { launch { googleAccountRepo.remove(it.id) } }
                        )
                        else -> {
                            log(TAG, WARN) { "Unknown connector type: $connector" }
                            null
                        }
                    }
                }
            }

            combine(withStates) { it.toList() }
        }
        .map {
            State(
                connectors = it
            )
        }
        .asLiveData2()

    fun addConnector() {
        log(TAG) { "addConnector()" }
        SyncListFragmentDirections.actionSyncListFragmentToSyncAddFragment().navigate()
    }

    companion object {
        private val TAG = logTag("Sync", "List", "Fragment", "VM")
    }
}