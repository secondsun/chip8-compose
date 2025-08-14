package dev.secondsun.chip8.compose

import dev.secondsun.chip8.compose.assembler.Assembler
import dev.secondsun.chip8.compose.assembler.Error
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class Chip8AssemblerTest {

   @Test
   fun programMustContainMain() {
        //First we check no main fails

       val programWithOutMain = """
           : pain
              clear
              jump pain
       """.trimIndent()

       val assembler = Assembler()
       val program = assembler.compile(programWithOutMain)

       assertTrue(program.hasErrors(), "Program with out main should have errors")
       assertEquals(1, program.errors.size)
       assertEquals(Error.NoMain, program.errors[0])

       //Next we chack hasMain passes
       val programWithMain = """
           : main
              clear
              jump main
       """.trimIndent()


       val program2 = assembler.compile(programWithMain)
        assertFalse(program2.hasErrors(), "Program with main should not have errors")
   }

    @Test
    fun labelDirective() {
        val programWithMain = """
           : main
              clear
              jump main
       """.trimIndent()

        val assembler = Assembler()
        val program = assembler.compile(programWithMain)

        assertNotNull(program.labels["main"])
        assertEquals(0, program.labels["main"]?.line)
        assertEquals(0, program.labels["main"]?.address)

    }

}