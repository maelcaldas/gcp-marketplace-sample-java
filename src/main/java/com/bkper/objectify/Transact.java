package com.bkper.objectify;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.googlecode.objectify.TxnType;

/**
 * Annotation representing a transaction
 * 
 * @author maelcaldas
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Transact {
    TxnType value();
}
