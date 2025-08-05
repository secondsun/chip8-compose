package dev.secondsun.chip8.util

import dev.secondsun.chip8.Chip8
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger

object Chip8Utils {
    @Throws(IOException::class)
    fun createFromRom(rom: URL): Chip8 {
        try {
            return createFromRom(Paths.get(rom.toURI()))
        } catch (ex: URISyntaxException) {
            Logger.getLogger(Chip8Utils::class.java.getName()).log(Level.SEVERE, null, ex)
            throw IOException(ex)
        }
    }

    @Throws(IOException::class)
    fun createFromRom(rom: String): Chip8 {
        return createFromRom(Paths.get(rom))
    }

    @Throws(IOException::class)
    fun createFromRom(rom: File): Chip8 {
        return createFromRom(rom.toPath())
    }

    fun createFromRom(reader : ByteArray): Chip8 {
        var index = 0x200
        val memory = ByteArray(4096) //4k memory
        try {
            for (read in reader) {
                memory[index] = (read)
                index += 1
            }
        } catch (ignore: ArrayIndexOutOfBoundsException) {
            throw IOException("File is too big to fit in memory", ignore)
        }

        val chip8 = Chip8(memory)
        return chip8
    }

    @Throws(IOException::class)
    fun createFromRom(rom: Path): Chip8 {
        return createFromRom(Files.readAllBytes(rom))
    }

    /**
     *
     * Packs a graphics row (8 pixels of the sprite) into a btye
     *
     * @param x
     * @param y
     * @param video
     * @return
     */
    fun getSpriteRow(x: Int, y: Int, video: ByteArray): Byte {
        var x = x
        var y = y
        x = x % 64
        y = y % 32
        val byte1 = video[x + y * 64]
        val byte2 = video[x + 1 + y * 64]
        val byte3 = video[x + 2 + y * 64]
        val byte4 = video[x + 3 + y * 64]
        val byte5 = video[x + 4 + y * 64]
        val byte6 = video[x + 5 + y * 64]
        val byte7 = video[x + 6 + y * 64]
        val byte8 = video[x + 7 + y * 64]

        return (((byte1.toInt() shl 7)
                or (byte2.toInt() shl 6)
                or (byte3.toInt() shl 5)
                or (byte4.toInt() shl 4)
                or (byte5.toInt() shl 3)
                or (byte6.toInt() shl 2)
                or (byte7.toInt() shl 1)
                or (byte8).toInt())).toByte()
    }
}
