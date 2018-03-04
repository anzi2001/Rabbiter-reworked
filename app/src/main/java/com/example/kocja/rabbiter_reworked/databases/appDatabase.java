package com.example.kocja.rabbiter_reworked.databases;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

/**
 * Created by kocja on 23/01/2018.
 */
@Database(version = appDatabase.VERSION)
public class appDatabase {
    static final int VERSION = 2;

    @Migration(version = 2,database = appDatabase.class)
    public static class migration extends AlterTableMigration<Entry>{

        public migration(Class<Entry> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            addColumn(SQLiteType.TEXT,"secondParent");
        }
    }
}
