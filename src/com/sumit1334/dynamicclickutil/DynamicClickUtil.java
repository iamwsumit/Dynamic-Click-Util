package com.sumit1334.dynamicclickutil;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HorizontalArrangement;
import com.google.appinventor.components.runtime.VerticalArrangement;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class DynamicClickUtil extends AndroidNonvisibleComponent implements Component {

    private final String TAG = "Dynamic Click Util";
    private final HashMap<String, ClickListener> listeners = new HashMap<>();
    private final ArrayList<AndroidViewComponent> components = new ArrayList<>();

    public DynamicClickUtil(ComponentContainer container) {
        super(container.$form());
        Log.i(TAG, "DynamicClickUtil: Extension Initialised");
    }

    @SimpleEvent
    public void Clicked(final AndroidViewComponent component, final String id, final Object data) {
        EventDispatcher.dispatchEvent(this, "Clicked", component, id, data);
    }

    @SimpleEvent
    public void LongClicked(final AndroidViewComponent component, final String id, final Object data) {
        EventDispatcher.dispatchEvent(this, "LongClicked", component, id, data);
    }

    @SimpleFunction
    public void AddClickListener(AndroidViewComponent component, String id, Object data) {
        if (!(this.listeners.containsKey(id) || this.components.contains(component))) {
            this.listeners.put(id, new ClickListener(component, data, id));
            this.components.add(component);
            Log.i(TAG, "AddClickListener: Click listener added to " + component.toString() + " with id " + id);
        } else
            Log.e(TAG, "AddClickListener: Failed to add click listener");
    }

    @SimpleFunction
    public boolean InstanceOf(Component component1, Component component2) {
        return component1.getClass().getSimpleName().equals(component2.getClass().getSimpleName());
    }

    @SimpleFunction
    public void SetData(String id, Object data) {
        if (GetData(id) == data)
            return;
        this.listeners.get(id).data = data;
    }

    @SimpleFunction
    public Object GetData(String id) {
        return this.listeners.get(id).data;
    }

    public final class ClickListener implements View.OnClickListener, View.OnLongClickListener {
        final String id;
        final AndroidViewComponent component;
        Object data;

        public ClickListener(AndroidViewComponent component, Object data, String id) {
            this.component = component;
            this.data = data;
            this.id = id;
            View view = this.getFinalView(component);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Clicked(component, id, data);
        }

        @Override
        public boolean onLongClick(View view) {
            LongClicked(component, id, data);
            return false;
        }

        private View getFinalView(AndroidViewComponent component) {
            View view = component.getView();
            final String className = component.getClass().getSimpleName();
            if (className.equals("MakeroidCardView")) {
                ViewGroup viewGroup = (ViewGroup) view;
                view = viewGroup.getChildAt(0);
            } else if (className.equals(HorizontalArrangement.class.getSimpleName()) || className.equalsIgnoreCase(VerticalArrangement.class.getSimpleName())) {
                Method[] methods = component.getClass().getMethods();
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase("IsCard") && !method.getReturnType().getName().equalsIgnoreCase("void")) {
                        try {
                            final boolean isCard = (boolean) method.invoke(component);
                            if (isCard) {
                                ViewGroup viewGroup = (ViewGroup) view;
                                view = viewGroup.getChildAt(0);
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            Log.e(TAG, "getFinalView: " + e.getMessage());
                        }
                    }
                }
            }
            return view;
        }
    }
}
