package com.example.joinus.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.joinus.model.Event;

import java.util.List;

public class ShareViewModelResult extends ViewModel {
    private final MutableLiveData<List<Event>> result = new MutableLiveData<>();

    public void setList(List<Event> list) {
        result.setValue(list);
    }

    public LiveData<List<Event>> getList() {
        return result;
    }
}
