package com.prm.groupproject_flowershop.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.prm.groupproject_flowershop.dao.CartDao;
import com.prm.groupproject_flowershop.models.Cart;

@Database(entities = {Cart.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CartDao cartDao();
}
