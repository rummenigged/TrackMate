package com.octopus.edu.core.data.entry.di

import com.octopus.edu.core.data.entry.EntryRepositoryImpl
import com.octopus.edu.core.data.entry.store.EntryStore
import com.octopus.edu.core.data.entry.store.EntryStoreImpl
import com.octopus.edu.core.domain.repository.EntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class EntryDataModule {
    @Binds
    abstract fun bindEntryStore(entryStoreImpl: EntryStoreImpl): EntryStore

    @Binds
    abstract fun bindEntryRepository(entryRepositoryImpl: EntryRepositoryImpl): EntryRepository
}
