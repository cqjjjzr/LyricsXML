package com.github.charlie.lxml.studio.ui

import javafx.beans.WeakListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import java.lang.ref.WeakReference

object BindingUtil {
    fun <E, F> bindRemoval(
        mapped: ObservableList<F>,
        source: ObservableList<out E>
    ) {
        val contentMapping = ListRemovalMapping<E, F>(mapped)
        source.removeListener(contentMapping)
        source.addListener(contentMapping)
    }

    fun <E, F> mapContent(
        mapped: ObservableList<F>,
        source: ObservableList<out E>,
        mapper: (E) -> F
    ) = map(mapped, source, mapper)

    private fun <E, F> map(
        mapped: ObservableList<F>,
        source: ObservableList<out E>,
        mapper: (E) -> F
    ) {
        val contentMapping = ListContentMapping<E, F>(mapped, mapper)
        mapped.setAll(source.map(mapper).toList())
        source.removeListener(contentMapping)
        source.addListener(contentMapping)
    }

    private class ListRemovalMapping<E, F>(mapped: MutableList<F>)
        : ListChangeListener<E>, WeakListener{
        private val mappedRef = WeakReference<MutableList<F>>(mapped)
        override fun onChanged(change: ListChangeListener.Change<out E>) {
            val mapped = mappedRef.get()
            if (mapped == null) {
                change.list.removeListener(this)
                return
            }

            if (change.wasRemoved()) {
                mapped.subList(change.from, change.from + change.removedSize).clear()
            }
        }

        override fun wasGarbageCollected() = mappedRef.get() == null
        override fun hashCode(): Int = mappedRef.get()?.hashCode() ?: 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return (mappedRef.get() ?: return false) ===
                    (other as? ListRemovalMapping<*, *> ?: return false).mappedRef.get()
        }
    }

    private class ListContentMapping<E, F>(mapped: MutableList<F>, private val mapper: (E) -> F)
            : ListChangeListener<E>, WeakListener {
        private val mappedRef = WeakReference<MutableList<F>>(mapped)

        override fun onChanged(change: ListChangeListener.Change<out E>) {
            val mapped = mappedRef.get()
            if (mapped == null) {
                change.list.removeListener(this)
                return
            }
            while (change.next()) {
                if (change.wasPermutated()) {
                    mapped.subList(change.from, change.to).clear()
                    mapped.addAll(change.from, change.list.subList(change.from, change.to).map(mapper).toList())
                } else {
                    if (change.wasRemoved()) {
                        mapped.subList(change.from, change.from + change.removedSize).clear()
                    }
                    if (change.wasAdded()) {
                        mapped.addAll(change.from, change.addedSubList.map(mapper).toList())
                    }
                }
            }
        }

        override fun wasGarbageCollected(): Boolean = mappedRef.get() == null
        override fun hashCode(): Int = mappedRef.get()?.hashCode() ?: 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return (mappedRef.get() ?: return false) ===
                    (other as? ListContentMapping<*, *> ?: return false).mappedRef.get()
        }
    }
}