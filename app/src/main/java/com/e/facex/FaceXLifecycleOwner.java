package com.e.facex;

import android.graphics.LightingColorFilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

public class FaceXLifecycleOwner implements LifecycleOwner {

    private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    FaceXLifecycleOwner(){
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    public void start(){
        lifecycleRegistry.markState(LifecycleRegistry.State.STARTED);
    }

    public void stop(){
        lifecycleRegistry.markState(LifecycleRegistry.State.CREATED);
    }

    public void tearDown(){
        lifecycleRegistry.markState(LifecycleRegistry.State.DESTROYED);
    }

    @Override
    public Lifecycle getLifecycle(){
        return lifecycleRegistry;
    }
}
