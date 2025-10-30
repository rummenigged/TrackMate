package com.octopus.edu.core.data.entry

import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDiskIOException
import android.database.sqlite.SQLiteFullException
import com.octopus.edu.core.data.entry.utils.EntryNotFoundException
import com.octopus.edu.core.domain.utils.BaseErrorClassifier
import java.sql.SQLTimeoutException

class DatabaseErrorClassifier : BaseErrorClassifier() {
    override fun isTransient(throwable: Throwable): Boolean =
        when (throwable) {
            is SQLiteDiskIOException,
            is SQLiteFullException,
            is SQLTimeoutException,
            is EntryNotFoundException,
            is SQLiteCantOpenDatabaseException -> true
            else -> false
        }
}
