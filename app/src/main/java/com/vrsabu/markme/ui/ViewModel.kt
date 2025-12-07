package com.vrsabu.markme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.coffee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ViewModel : ViewModel() {

    private var _coffeeList = MutableStateFlow<List<coffee>>(emptyList())

    public fun getCoffeeList(){
        viewModelScope.launch (Dispatchers.IO){
            val coffeeList = RetrofitInstance.api.getCoffeeList()
            _coffeeList = coffeeList as MutableStateFlow<List<coffee>>
        }
    }
}
