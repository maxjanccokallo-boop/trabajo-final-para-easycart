package com.example.easycart.di

import com.example.easycart.data.EasyCartRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object AppModule {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore


    val repo: EasyCartRepository by lazy {
        EasyCartRepository(
            auth = auth,
            db = firestore
        )
    }




    val mainRepo = repo
    val repoForFactory = repo

    val repoForAuthFactory = repo
}