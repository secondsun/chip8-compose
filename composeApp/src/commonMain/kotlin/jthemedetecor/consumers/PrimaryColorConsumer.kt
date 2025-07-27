package jthemedetecor.consumers

import java.awt.Color
import java.util.function.Consumer

class PrimaryColorConsumer(private val consumer: Consumer<Color>) : ThemingConsumer<Color?> {
    override fun accept(t: Color?) {
        consumer.accept(t!!)
    }
}
