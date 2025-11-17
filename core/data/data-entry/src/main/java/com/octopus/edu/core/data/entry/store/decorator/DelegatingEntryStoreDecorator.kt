package com.octopus.edu.core.data.entry.store.decorator

import com.octopus.edu.core.data.entry.store.EntryStore

abstract class DelegatingEntryStoreDecorator(
    protected val delegate: EntryStore
) : EntryStore by delegate
