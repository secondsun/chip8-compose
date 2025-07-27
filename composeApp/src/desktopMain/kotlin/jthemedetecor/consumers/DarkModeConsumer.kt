package jthemedetecor.consumers

import java.util.function.Consumer

class DarkModeConsumer(private val consumer: Consumer<Boolean>) : ThemingConsumer<Boolean?> {
    override fun accept(aBoolean: Boolean?) {
        consumer.accept(aBoolean!!)
    }

}
