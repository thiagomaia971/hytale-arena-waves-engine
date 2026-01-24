package com.miilhozinho.arenawavesengine.hud

import org.jsoup.Jsoup

class ForIterable {
    fun addFor(html: String, selectorId: String, componentInstances: Array<String>): String {
        val doc = Jsoup.parseBodyFragment(html).clone()
        val listElement = doc.getElementById(selectorId) ?: return html

        for (componentInstance in componentInstances) {
            listElement.append(componentInstance)
        }
        return doc.html()
    }
}