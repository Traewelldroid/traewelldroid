package de.hbch.traewelling.api

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Exclude

class ExcludeAnnotationExclusionStrategy : ExclusionStrategy {
    override fun shouldSkipField(f: FieldAttributes?) = f?.getAnnotation(Exclude::class.java) != null

    override fun shouldSkipClass(clazz: Class<*>?) = false
}