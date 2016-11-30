package com.pramati.crawler.utils

import java.io.{FileOutputStream, PrintWriter}
import java.net.URLEncoder

object FileWritter {

    def writeFileToDisk(fileName: String, data: String):Unit= {
      //val file = encodeFileName(fileName,"UTF-8")
      val writer = new PrintWriter( new FileOutputStream(fileName))
      writer.write("Hello Scala")
      writer.close()
    }

    def encodeFileName(fileName: String, encoding: String): String = {
      val fileNameEncoded = URLEncoder.encode(fileName, encoding)
      fileNameEncoded
    }
}