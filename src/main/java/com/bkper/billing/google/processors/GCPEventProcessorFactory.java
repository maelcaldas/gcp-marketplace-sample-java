package com.bkper.billing.google.processors;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.bkper.billing.google.GCPEventType;
import com.bkper.billing.google.GCPModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class GCPEventProcessorFactory {
    
    private static final Logger LOGGER = Logger.getLogger(GCPEventProcessorFactory.class.getName());

    @Inject
    private Injector injector;
    
    /**
     *
     * Gets the properly Event Processor by event type.
     * 
     *  @see GCPModule for bindings
     * 
     */
    public GCPEventProcessor get(GCPEventType eventType) {
        try {
            return injector.getInstance(Key.get(GCPEventProcessor.class, Names.named(eventType.name())));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Did not find a suitable event processor for type: " + eventType);
            return null;
        }
    }

    

}
