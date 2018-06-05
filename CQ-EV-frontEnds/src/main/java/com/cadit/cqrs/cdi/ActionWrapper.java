package com.cadit.cqrs.cdi;

import com.cadit.data.DocEvent;

import javax.enterprise.event.Event;

/**
 * **********************************************API***********************************
 * classe di comodo che riceve un evento ben definito e invoca l'handler locale
 * @param <T>
 * @param <C>
 * ***********************************************************************************
 */
public class  ActionWrapper<T extends Event<C>,C extends DocEvent> {


    private final T localEvent;

    public ActionWrapper(T event) {
      localEvent  = event;
    }

    public  void accept(C event){
        localEvent.fire(event);
    }
}
